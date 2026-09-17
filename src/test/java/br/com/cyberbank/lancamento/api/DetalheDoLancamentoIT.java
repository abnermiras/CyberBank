package br.com.cyberbank.lancamento.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DetalheDoLancamentoIT {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

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
    @SuppressWarnings("unchecked")
    void o_detalhe_resolve_os_nomes_que_o_extrato_devolve_como_id() {
        var sessao = novaSessao("detalhe");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank, "PIX");
        Integer transporte = criarCategoria(sessao, "Transporte", "SAIDA");
        Integer gasolina = criarSubcategoria(sessao, "Gasolina", transporte);

        Integer id = lancar(sessao, pix, gasolina, "SAIDA", 15450, hoje, "Gasolina",
                "POSTO IPIRANGA");

        var corpo = detalhe(sessao, id).getBody();

        assertThat(corpo).containsEntry("descricao", "Gasolina");
        assertThat(corpo).containsEntry("situacao", "REALIZADO");
        assertThat(corpo).containsEntry("estabelecimento", "POSTO IPIRANGA");
        assertThat((String) corpo.get("criadoEm"))
                .as("o instante do cadastro é o único lugar com hora, e nunca foi exposto")
                .endsWith("Z");

        assertThat((Map<String, Object>) corpo.get("conta"))
                .containsEntry("nome", "Nubank").containsEntry("tipo", "CORRENTE");
        assertThat((Map<String, Object>) corpo.get("meio")).containsEntry("tipo", "PIX");
        assertThat((Map<String, Object>) corpo.get("autor")).containsEntry("nome", "detalhe");

        var categoria = (Map<String, Object>) corpo.get("categoria");
        assertThat(categoria).containsEntry("nome", "Gasolina");
        assertThat((Map<String, Object>) categoria.get("raiz"))
                .as("a subcategoria mostra de que raiz ela é, com a cor da raiz")
                .containsEntry("nome", "Transporte").containsEntry("cor", "OCRE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void a_transferencia_mostra_o_outro_lado_e_nao_mostra_meio() {
        var sessao = novaSessao("otherside");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer poupanca = criarConta(sessao, "Poupança", "APLICACAO", 0L, List.of());

        var par = transferir(sessao, nubank, poupanca, 20000, hoje);
        Integer saida = (Integer) par.get(0).get("id");
        Integer entrada = (Integer) par.get(1).get("id");

        var corpo = detalhe(sessao, saida).getBody();

        assertThat(corpo)
                .as("ninguém pagou nada numa transferência: o campo não se aplica e não vem")
                .doesNotContainKey("meio");
        assertThat((Map<String, Object>) corpo.get("transferencia"))
                .containsEntry("outroLadoId", entrada);
    }

    @Test
    void o_original_aponta_para_o_estorno_e_o_estorno_para_o_original() {
        var sessao = novaSessao("estornado");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank, "PIX");
        Integer compras = criarCategoria(sessao, "Compras", "SAIDA");

        Integer compra = lancar(sessao, pix, compras, "SAIDA", 30000, hoje, "Tênis", null);
        Integer estorno = estornar(sessao, compra, hoje);

        assertThat(detalhe(sessao, compra).getBody())
                .as("o original não guarda ponteiro no banco: quem responde é a consulta inversa")
                .containsEntry("estornadoPorId", estorno);
        assertThat(detalhe(sessao, estorno).getBody()).containsEntry("estornoDeId", compra);
    }

    @Test
    void lancamento_de_outro_ambiente_responde_como_inexistente() {
        var dona = novaSessao("dona-do-detalhe");
        var curioso = novaSessao("curioso-do-detalhe");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(dona, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer pix = meioDa(dona, nubank, "PIX");
        Integer id = lancar(dona, pix, null, "SAIDA", 1000, hoje, "Segredo", null);

        var resposta = troca(http().get()
                .uri("/api/v1/ambientes/" + dona.ambienteId() + "/lancamentos/" + id), curioso);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsEntry("codigo", "NAO_ENCONTRADO");
    }

    @Test
    @SuppressWarnings("unchecked")
    void o_historico_do_lancamento_conta_o_que_ja_fizeram_com_ele() {
        var sessao = novaSessao("historico");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank, "PIX");
        Integer mercado = criarCategoria(sessao, "Mercado", "SAIDA");

        Integer id = lancar(sessao, pix, mercado, "SAIDA", 12000, hoje, "Feira", null);
        corrigir(sessao, id, Map.of("valor", 15450));

        var corpo = historico(sessao, "LANCAMENTO", id).getBody();

        assertThat((Map<String, Object>) corpo.get("alvo"))
                .containsEntry("tipo", "LANCAMENTO").containsEntry("id", id);

        List<Map<String, Object>> itens = (List<Map<String, Object>>) corpo.get("itens");
        assertThat(itens).extracting(evento -> evento.get("tipo"))
                .as("histórico se lê do começo, ao contrário do Diário")
                .containsExactly("LANCAMENTO_CRIADO", "LANCAMENTO_EDITADO");
        assertThat(itens).allSatisfy(evento ->
                assertThat(evento).containsEntry("autor", "historico"));

        var correcao = (Map<String, Object>) itens.get(1).get("dados");
        assertThat(correcao)
                .as("o de/para é o que cumpre o \"sempre com histórico\" do lancamento.md")
                .containsEntry("valorDe", 12000)
                .containsEntry("valorPara", 15450);
    }

    @Test
    @SuppressWarnings("unchecked")
    void o_historico_sobrevive_ao_lancamento_excluido() {
        var sessao = novaSessao("excluido");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank, "PIX");
        Integer id = lancar(sessao, pix, null, "SAIDA", 5000, hoje, "Some daqui", null);

        assertThat(troca(http().delete().uri(caminho(sessao, "/lancamentos/" + id)), sessao)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(detalhe(sessao, id).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        List<Map<String, Object>> itens =
                (List<Map<String, Object>>) historico(sessao, "LANCAMENTO", id).getBody().get("itens");

        assertThat(itens).extracting(evento -> evento.get("tipo"))
                .contains("LANCAMENTO_EXCLUIDO");
        assertThat((Map<String, Object>) itens.get(itens.size() - 1).get("dados"))
                .as("o alvo não existe mais, então os dados precisam bastar sozinhos")
                .containsEntry("descricao", "Some daqui")
                .containsEntry("valor", 5000);
    }

    @Test
    void o_alvo_e_o_alvoId_andam_juntos_e_nao_se_misturam_com_o_dia() {
        var sessao = novaSessao("alvoruim");

        assertThat(troca(http().get().uri(caminho(sessao, "/eventos?alvo=LANCAMENTO")), sessao)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        assertThat(troca(http().get().uri(caminho(sessao, "/eventos?alvoId=7")), sessao)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        assertThat(troca(http().get()
                .uri(caminho(sessao, "/eventos?alvo=LANCAMENTO&alvoId=7&dia=2026-09-17")), sessao)
                .getStatusCode())
                .as("são duas perguntas, e responder as duas esconde qual venceu")
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        assertThat(troca(http().get()
                .uri(caminho(sessao, "/eventos?alvo=NAVE&alvoId=7")), sessao)
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    private record Sessao(Integer ambienteId, String cookie) {
    }

    private ResponseEntity<Map<String, Object>> detalhe(Sessao sessao, Integer id) {
        return troca(http().get().uri(caminho(sessao, "/lancamentos/" + id)), sessao);
    }

    private ResponseEntity<Map<String, Object>> historico(Sessao sessao, String tipo, Integer id) {
        return troca(http().get()
                .uri(caminho(sessao, "/eventos?alvo=" + tipo + "&alvoId=" + id)), sessao);
    }

    private Integer criarConta(Sessao sessao, String nome, String tipo, Long saldoInicial,
            List<String> meios) {
        Map<String, Object> corpo = new HashMap<>(Map.of("nome", nome, "tipo", tipo, "meios", meios));
        corpo.put("saldoInicial", saldoInicial);
        var resposta = troca(http().post().uri(caminho(sessao, "/contas")).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    @SuppressWarnings("unchecked")
    private Integer meioDa(Sessao sessao, Integer contaId, String tipo) {
        var meios = (List<Map<String, Object>>) troca(
                http().get().uri(caminho(sessao, "/meios-de-pagamento")), sessao)
                .getBody().get("itens");
        return (Integer) meios.stream()
                .filter(m -> contaId.equals(m.get("contaId")) && tipo.equals(m.get("tipo")))
                .findFirst().orElseThrow().get("id");
    }

    private Integer criarCategoria(Sessao sessao, String nome, String sentido) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "sentido", sentido, "cor", "OCRE")), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer criarSubcategoria(Sessao sessao, String nome, Integer paiId) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "paiId", paiId)), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer lancar(Sessao sessao, Integer meioId, Integer categoriaId, String sentido,
            int valor, LocalDate dia, String descricao, String estabelecimento) {
        Map<String, Object> corpo = new HashMap<>(Map.of("meioId", meioId, "sentido", sentido,
                "valor", valor, "dataEvento", dia.toString(), "descricao", descricao));
        corpo.put("categoriaId", categoriaId);
        corpo.put("estabelecimento", estabelecimento);
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos")).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private void corrigir(Sessao sessao, Integer id, Map<String, Object> corpo) {
        var resposta = troca(http().patch()
                .uri(caminho(sessao, "/lancamentos/" + id)).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private Integer estornar(Sessao sessao, Integer id, LocalDate dia) {
        var resposta = troca(http().post()
                .uri(caminho(sessao, "/lancamentos/" + id + "/estorno"))
                .body(Map.of("dataEvento", dia.toString())), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> transferir(Sessao sessao, Integer origem, Integer destino,
            int valor, LocalDate dia) {
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos/transferencias"))
                .body(Map.of("contaDeOrigemId", origem, "contaDeDestinoId", destino,
                        "valor", valor, "dataEvento", dia.toString(), "descricao", "Guardando")),
                sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }

    private String caminho(Sessao sessao, String sufixo) {
        return "/api/v1/ambientes/" + sessao.ambienteId() + sufixo;
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> troca(RestClient.RequestHeadersSpec<?> spec,
            Sessao sessao) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) spec
                .header(HttpHeaders.COOKIE, sessao.cookie())
                .retrieve()
                .toEntity(Map.class);
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

        List<Map<String, Object>> itens =
                (List<Map<String, Object>>) ambientes.getBody().get("itens");
        return new Sessao((Integer) itens.get(0).get("id"), cookie);
    }
}
