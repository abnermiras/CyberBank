package br.com.cyberbank.categoria.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.persistencia.BancoDeTeste;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A tela de Cadastro inteira contra Postgres real (ADR-0011): criar raiz e subcategoria,
 * renomear, inativar, reativar e excluir — e cada recusa do catalogo com o status que
 * docs/04-api/erros.md promete.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CadastroDeCategoriaIT {

    @Container
    static final PostgreSQLContainer POSTGRES = BancoDeTeste.novoContainer();

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", () -> BancoDeTeste.APLICACAO);
        registro.add("spring.datasource.password", () -> BancoDeTeste.SENHA_DA_APLICACAO);

        registro.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registro.add("spring.flyway.user", () -> BancoDeTeste.DONO);
        registro.add("spring.flyway.password", () -> BancoDeTeste.SENHA_DO_DONO);

        registro.add("cyberbank.argon2.memoria", () -> 1024);
        registro.add("cyberbank.argon2.iteracoes", () -> 1);
        registro.add("cyberbank.argon2.paralelismo", () -> 1);
    }

    @LocalServerPort
    private int porta;

    @Test
    void a_arvore_nasce_vazia_ganha_raiz_e_subcategoria_e_a_raiz_deixa_de_ser_escolhivel() {
        var sessao = novaSessao("arvore");

        assertThat(itens(arvore(sessao))).as("o sistema não cria categoria de usuário nenhuma").isEmpty();

        Integer transporte = criar(sessao, raiz("Transporte", "SAIDA", "OCRE"));

        assertThat(itens(arvore(sessao))).singleElement().satisfies(raiz -> {
            assertThat(raiz).containsEntry("nome", "Transporte");
            assertThat(raiz).containsEntry("escolhivel", true);
            assertThat((List<?>) raiz.get("filhas")).isEmpty();
        });

        criar(sessao, Map.of("nome", "Gasolina", "paiId", transporte));

        assertThat(itens(arvore(sessao))).singleElement().satisfies(raiz -> {
            assertThat(raiz)
                    .as("existe um destino mais específico agora")
                    .containsEntry("escolhivel", false);
            assertThat(filhas(raiz)).singleElement().satisfies(filha -> {
                assertThat(filha).containsEntry("nome", "Gasolina");
                assertThat(filha).as("a subcategoria herda o sentido da raiz")
                        .containsEntry("sentido", "SAIDA");
                assertThat(filha).containsEntry("escolhivel", true);
            });
        });
    }

    @Test
    void inativar_a_ultima_filha_ativa_devolve_a_raiz_para_a_escolha() {
        var sessao = novaSessao("oscila");
        Integer raiz = criar(sessao, raiz("Sustento", "SAIDA", "OLIVA"));
        Integer filha = criar(sessao, Map.of("nome", "Mercado", "paiId", raiz));

        assertThat(primeiraRaiz(sessao)).containsEntry("escolhivel", false);

        patch(sessao, filha, campos("inativa", true));

        assertThat(primeiraRaiz(sessao))
                .as("inativar a última filha ativa é o caminho para simplificar a árvore")
                .containsEntry("escolhivel", true);
        assertThat(filhas(primeiraRaiz(sessao)))
                .as("dado inativo fica escondido por padrão")
                .isEmpty();

        patch(sessao, filha, campos("inativa", false));
        assertThat(primeiraRaiz(sessao)).containsEntry("escolhivel", false);
    }

    @Test
    void inativar_a_raiz_esconde_a_filha_sem_gravar_nada_nela() {
        var sessao = novaSessao("cascata");
        Integer raiz = criar(sessao, raiz("Lazer", "SAIDA", "MALVA"));
        criar(sessao, Map.of("nome", "Cinema", "paiId", raiz));

        patch(sessao, raiz, campos("inativa", true));

        assertThat(itens(get(sessao, "/categorias?inativas=true"))).singleElement().satisfies(r -> {
            assertThat(r).containsEntry("inativa", true);
            assertThat(filhas(r)).singleElement().satisfies(filha -> assertThat(filha)
                    .as("herança na leitura, nunca cascata na escrita")
                    .containsEntry("inativa", false));
        });
    }

    @Test
    void renomear_muda_so_o_nome_e_mantem_a_identidade() {
        var sessao = novaSessao("renome");
        Integer raiz = criar(sessao, raiz("Academia", "SAIDA", "TEAL"));

        var resposta = patch(sessao, raiz, campos("nome", "Saúde"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).containsEntry("id", raiz);
        assertThat(resposta.getBody()).containsEntry("nome", "Saúde");
    }

    @Test
    void excluir_tira_a_categoria_e_a_raiz_com_filha_e_recusada() {
        var sessao = novaSessao("excluir");
        Integer raiz = criar(sessao, raiz("Transporte", "SAIDA", "OCRE"));
        Integer filha = criar(sessao, Map.of("nome", "Uber", "paiId", raiz));

        var comFilha = excluir(sessao, raiz);
        assertThat(comFilha.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(comFilha.getBody()).containsEntry("codigo", "CATEGORIA_COM_SUBCATEGORIA");

        assertThat(excluir(sessao, filha).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(excluir(sessao, raiz).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(itens(arvore(sessao))).isEmpty();
    }

    @Test
    void a_categoria_de_sistema_nao_se_renomeia_nao_se_inativa_e_nao_se_exclui() {
        var sessao = novaSessao("sistema");
        Integer deSistema = (Integer) itens(get(sessao, "/categorias?sistema=true")).get(0).get("id");

        assertThat(patch(sessao, deSistema, campos("nome", "Outra")).getBody())
                .containsEntry("codigo", "CATEGORIA_DE_SISTEMA_PROTEGIDA");
        assertThat(patch(sessao, deSistema, campos("inativa", true)).getBody())
                .containsEntry("codigo", "CATEGORIA_DE_SISTEMA_PROTEGIDA");

        var exclusao = excluir(sessao, deSistema);
        assertThat(exclusao.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exclusao.getBody()).containsEntry("codigo", "CATEGORIA_DE_SISTEMA_PROTEGIDA");

        assertThat(criarCru(sessao, Map.of("nome", "Minha", "paiId", deSistema)).getBody())
                .containsEntry("codigo", "CATEGORIA_DE_SISTEMA_PROTEGIDA");
    }

    @Test
    void a_arvore_tem_dois_niveis_e_o_terceiro_e_recusado() {
        var sessao = novaSessao("niveis");
        Integer raiz = criar(sessao, raiz("Transporte", "SAIDA", "OCRE"));
        Integer filha = criar(sessao, Map.of("nome", "Gasolina", "paiId", raiz));

        var neta = criarCru(sessao, Map.of("nome", "Aditivada", "paiId", filha));

        assertThat(neta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(neta.getBody()).containsEntry("codigo", "CATEGORIA_PAI_INVALIDO");
    }

    @Test
    void a_subcategoria_recusa_sentido_divergente_em_vez_de_corrigir_em_silencio() {
        var sessao = novaSessao("sentido");
        Integer raiz = criar(sessao, raiz("Salário", "ENTRADA", "AZUL"));

        var divergente = criarCru(sessao, Map.of("nome", "Bônus", "paiId", raiz, "sentido", "SAIDA"));

        assertThat(divergente.getStatusCode().value()).isEqualTo(422);
        assertThat(divergente.getBody()).containsEntry("codigo", "VALIDACAO");
    }

    @Test
    void criar_sem_nome_e_sem_sentido_devolve_os_dois_campos_de_uma_vez() {
        var sessao = novaSessao("validacao");

        var resposta = criarCru(sessao, Map.of("nome", "  "));

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat((List<?>) resposta.getBody().get("erros"))
                .as("nome, sentido e cor voltam juntos")
                .hasSize(3);
    }

    @Test
    void categoria_de_outro_ambiente_responde_igual_a_inexistente() {
        var dona = novaSessao("dona");
        Integer daDona = criar(dona, raiz("Particular", "SAIDA", "VIOLETA"));

        var curioso = novaSessao("curioso");
        var alvo = new Sessao(curioso.ambienteId(), curioso.cookie());

        assertThat(patch(alvo, daDona, campos("nome", "Invadida")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(excluir(alvo, daDona).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void a_cor_e_da_raiz_a_filha_herda_e_trocar_e_livre() {
        var sessao = novaSessao("cores");
        Integer raizId = criar(sessao, raiz("Moradia", "SAIDA", "OCRE"));
        Integer filha = criar(sessao, Map.of("nome", "Aluguel", "paiId", raizId));

        var arvore = itens(arvore(sessao)).get(0);
        assertThat(arvore).containsEntry("cor", "OCRE");
        assertThat(filhas(arvore)).singleElement().satisfies(f -> assertThat(f)
                .as("campo que não se aplica não vem: a filha lê a cor da raiz")
                .doesNotContainKey("cor"));

        assertThat(patch(sessao, raizId, campos("cor", "TEAL")).getBody())
                .containsEntry("cor", "TEAL");
        assertThat(itens(arvore(sessao)).get(0)).containsEntry("cor", "TEAL");

        var naFilha = patch(sessao, filha, campos("cor", "MALVA"));
        assertThat(naFilha.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(naFilha.getBody()).containsEntry("codigo", "CATEGORIA_SEM_COR_PROPRIA");
    }

    @Test
    void cor_fora_da_paleta_nao_entra() {
        var sessao = novaSessao("paleta");

        var resposta = criarCru(sessao, Map.of("nome", "Neon", "sentido", "SAIDA", "cor", "PINK"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody())
                .as("a paleta de identidade não inclui as cores de sinalização")
                .containsEntry("codigo", "CORPO_INVALIDO");
        assertThat(resposta.getBody().toString())
                .as("o erro não repete o valor que o cliente mandou")
                .doesNotContain("PINK");
    }

    @Test
    void a_categoria_de_sistema_nao_tem_cor() {
        var sessao = novaSessao("semcor");

        assertThat(itens(get(sessao, "/categorias?sistema=true")))
                .allSatisfy(c -> assertThat(c).doesNotContainKey("cor"));
    }

    @Test
    void sem_sessao_nao_se_cria_categoria_nenhuma() {
        var sessao = novaSessao("semcookie");
        var semCookie = new Sessao(sessao.ambienteId(), null);

        assertThat(criarCru(semCookie, raiz("X", "SAIDA", "OCRE")).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- o que faz a conversa com a API ---

    private record Sessao(Integer ambienteId, String cookie) {
    }

    /** {@code Map.of} nao aceita valor nulo, e inativa=false precisa viajar. */
    private static Map<String, Object> raiz(String nome, String sentido, String cor) {
        return Map.of("nome", nome, "sentido", sentido, "cor", cor);
    }

    private static Map<String, Object> campos(String chave, Object valor) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put(chave, valor);
        return corpo;
    }

    @SuppressWarnings("unchecked")
    private Sessao novaSessao(String apelido) {
        String email = apelido + "@exemplo.com";
        http().post().uri("/api/v1/usuarios")
                .body(Map.of("nome", apelido, "email", email, "senha", "uma senha longa"))
                .retrieve().toBodilessEntity();

        String cookie = http().post().uri("/api/v1/sessoes")
                .body(Map.of("email", email, "senha", "uma senha longa"))
                .retrieve().toBodilessEntity()
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];

        var ambientes = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri("/api/v1/ambientes")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(Map.class);
        return new Sessao((Integer) itens(ambientes).get(0).get("id"), cookie);
    }

    private Integer criar(Sessao sessao, Map<String, Object> corpo) {
        var resposta = criarCru(sessao, corpo);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> criarCru(Sessao sessao, Map<String, Object> corpo) {
        return troca(http().post().uri(caminho(sessao, "/categorias")).body(corpo), sessao);
    }

    private ResponseEntity<Map<String, Object>> patch(Sessao sessao, Integer id, Map<String, Object> corpo) {
        return troca(http().patch().uri(caminho(sessao, "/categorias/" + id)).body(corpo), sessao);
    }

    private ResponseEntity<Map<String, Object>> excluir(Sessao sessao, Integer id) {
        return troca(http().delete().uri(caminho(sessao, "/categorias/" + id)), sessao);
    }

    private ResponseEntity<Map<String, Object>> arvore(Sessao sessao) {
        return get(sessao, "/categorias");
    }

    private Map<String, Object> primeiraRaiz(Sessao sessao) {
        return itens(arvore(sessao)).get(0);
    }

    private ResponseEntity<Map<String, Object>> get(Sessao sessao, String sufixo) {
        return troca(http().get().uri(caminho(sessao, sufixo)), sessao);
    }

    private String caminho(Sessao sessao, String sufixo) {
        return "/api/v1/ambientes/" + sessao.ambienteId() + sufixo;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> troca(RestClient.RequestHeadersSpec<?> spec, Sessao sessao) {
        if (sessao.cookie() != null) {
            spec = spec.header(HttpHeaders.COOKIE, sessao.cookie());
        }
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) spec.retrieve().toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> filhas(Map<String, Object> no) {
        return (List<Map<String, Object>>) no.get("filhas");
    }

    /** Sem tratador de erro: aqui a resposta 4xx E O QUE ESTA SENDO TESTADO, nao uma excecao. */
    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }
}
