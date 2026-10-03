package br.com.cyberbank.ambiente.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.springframework.http.HttpMethod;
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
class ConvitesIT {

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

    private record Sessao(Integer usuarioId, Integer ambienteId, String cookie, String email) {
    }

    @Test
    void o_convidado_aceita_e_passa_a_lancar_no_ambiente_e_cada_linha_diz_quem_lancou() {
        Sessao ana = novaSessao("ana");
        Sessao bia = novaSessao("bia");
        String hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

        var convite = convidar(ana, "  BIA@Exemplo.com ", "EDITOR");
        assertThat(convite.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(convite.getBody()).containsEntry("email", "bia@exemplo.com")
                .containsEntry("papel", "EDITOR");

        assertThat(enviar(bia, "GET", "/api/v1/ambientes/" + ana.ambienteId() + "/contas", null)
                .getStatusCode())
                .as("convite pendente não é acesso")
                .isEqualTo(HttpStatus.NOT_FOUND);

        var recebidos = itens(enviar(bia, "GET", "/api/v1/convites", null));
        assertThat(recebidos).singleElement().satisfies(c -> {
            assertThat(c).containsEntry("ambienteId", ana.ambienteId());
            assertThat(c).containsEntry("ambienteNome", "Ambiente Pessoal");
            assertThat(c).containsEntry("convidadoPor", "ana");
            assertThat(c).containsEntry("papel", "EDITOR");
        });

        var aceite = enviar(bia, "POST", "/api/v1/convites/" + recebidos.getFirst().get("id")
                + "/aceite", null);
        assertThat(aceite.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aceite.getBody()).containsEntry("id", ana.ambienteId())
                .containsEntry("papel", "EDITOR");

        assertThat(itens(enviar(bia, "GET", "/api/v1/convites", null))).isEmpty();
        assertThat(itens(enviar(bia, "GET", "/api/v1/ambientes", null)))
                .extracting(a -> a.get("id"), a -> a.get("papel"))
                .contains(tuple(ana.ambienteId(), "EDITOR"));

        Integer nubank = criarConta(ana, "Nubank");
        Integer pix = meioDa(ana, nubank);
        Sessao biaNaAna = new Sessao(bia.usuarioId(), ana.ambienteId(), bia.cookie(),
                bia.email());
        assertThat(lancar(ana, pix, 1000, "Feira").getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(lancar(biaNaAna, pix, 2500, "Farmácia").getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        var extrato = itens(get(ana, "/lancamentos"));
        assertThat(mapa(linha(extrato, "Feira").get("autor")))
                .containsEntry("id", ana.usuarioId()).containsEntry("nome", "ana")
                .containsKey("avatar");
        assertThat(mapa(linha(extrato, "Farmácia").get("autor")))
                .containsEntry("id", bia.usuarioId()).containsEntry("nome", "bia");

        assertThat(itens(get(ana, "/membros")))
                .extracting(m -> m.get("nome"), m -> m.get("papel"))
                .containsExactly(tuple("ana", "DONO"),
                        tuple("bia", "EDITOR"));

        assertThat(itens(get(ana, "/eventos?dia=" + hoje)))
                .filteredOn(e -> "ACESSO_CONCEDIDO".equals(e.get("tipo")))
                .singleElement()
                .satisfies(e -> assertThat(mapa(e.get("dados")))
                        .containsEntry("pessoa", "bia").containsEntry("papel", "EDITOR"));
    }

    @Test
    void o_convite_para_quem_ainda_nao_tem_cadastro_aparece_quando_a_pessoa_se_cadastra() {
        Sessao caio = novaSessao("caio");

        assertThat(convidar(caio, "dani@exemplo.com", "LEITOR").getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(itens(get(caio, "/convites")))
                .extracting(c -> c.get("email")).containsExactly("dani@exemplo.com");

        Sessao dani = novaSessao("dani");
        assertThat(itens(enviar(dani, "GET", "/api/v1/convites", null)))
                .singleElement()
                .satisfies(c -> assertThat(c).containsEntry("ambienteId", caio.ambienteId())
                        .containsEntry("papel", "LEITOR"));
    }

    @Test
    void so_o_dono_convida_e_cada_recusa_tem_o_seu_codigo() {
        Sessao edu = novaSessao("edu");
        Sessao fabi = novaSessao("fabi");
        aceitarConviteDe(edu, fabi, "EDITOR");
        Sessao fabiNoEdu = new Sessao(fabi.usuarioId(), edu.ambienteId(), fabi.cookie(),
                fabi.email());

        assertThat(convidar(fabiNoEdu, "gui@exemplo.com", "EDITOR"))
                .satisfies(r -> {
                    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(r.getBody()).containsEntry("codigo", "SEM_PERMISSAO");
                });

        assertThat(convidar(edu, "gui@exemplo.com", "EDITOR").getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(convidar(edu, "GUI@exemplo.com", "LEITOR").getBody())
                .containsEntry("codigo", "CONVITE_JA_PENDENTE");
        assertThat(convidar(edu, "fabi@exemplo.com", "LEITOR").getBody())
                .containsEntry("codigo", "JA_TEM_ACESSO");
        assertThat(convidar(edu, "edu@exemplo.com", "EDITOR").getBody())
                .as("o dono já tem acesso")
                .containsEntry("codigo", "JA_TEM_ACESSO");
        assertThat(convidar(edu, "hugo@exemplo.com", "DONO").getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(convidar(edu, "sem arroba", "EDITOR").getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void o_leitor_le_tudo_nao_altera_nada_e_pode_sair() {
        Sessao ivo = novaSessao("ivo");
        Sessao jade = novaSessao("jade");
        Integer conta = criarConta(ivo, "Itaú");
        Integer pix = meioDa(ivo, conta);
        lancar(ivo, pix, 500, "Padaria");

        aceitarConviteDe(ivo, jade, "LEITOR");
        Sessao jadeNoIvo = new Sessao(jade.usuarioId(), ivo.ambienteId(), jade.cookie(),
                jade.email());

        assertThat(get(jadeNoIvo, "/lancamentos").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(itens(get(jadeNoIvo, "/lancamentos")))
                .extracting(l -> l.get("descricao")).contains("Padaria");

        assertThat(lancar(jadeNoIvo, pix, 900, "Não deveria"))
                .satisfies(r -> {
                    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(r.getBody()).containsEntry("codigo", "SEM_PERMISSAO");
                });
        assertThat(enviar(jadeNoIvo, "PATCH", "/api/v1/ambientes/" + ivo.ambienteId(),
                Map.of("nome", "Meu")).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(enviar(jadeNoIvo, "DELETE", caminho(jadeNoIvo, "/membros/"
                + jade.usuarioId()), null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get(jadeNoIvo, "/lancamentos").getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(itens(get(ivo, "/membros"))).extracting(m -> m.get("nome"))
                .containsExactly("ivo");
    }

    @Test
    void o_dono_remove_quem_entrou_e_o_que_a_pessoa_lancou_fica() {
        Sessao lia = novaSessao("lia");
        Sessao max = novaSessao("max");
        Sessao nina = novaSessao("nina");
        aceitarConviteDe(lia, max, "EDITOR");
        aceitarConviteDe(lia, nina, "LEITOR");
        Sessao maxNaLia = new Sessao(max.usuarioId(), lia.ambienteId(), max.cookie(),
                max.email());
        Integer conta = criarConta(lia, "Inter");
        lancar(maxNaLia, meioDa(lia, conta), 3000, "Mercado do Max");
        String hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

        assertThat(enviar(maxNaLia, "DELETE", caminho(lia, "/membros/" + nina.usuarioId()),
                null).getStatusCode())
                .as("editor não remove outra pessoa")
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(enviar(maxNaLia, "DELETE", caminho(lia, "/membros/" + lia.usuarioId()),
                null).getBody())
                .containsEntry("codigo", "DONO_NAO_SAI");
        assertThat(enviar(lia, "DELETE", caminho(lia, "/membros/" + lia.usuarioId()), null)
                .getBody())
                .containsEntry("codigo", "DONO_NAO_SAI");

        assertThat(enviar(lia, "DELETE", caminho(lia, "/membros/" + max.usuarioId()), null)
                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(get(maxNaLia, "/lancamentos").getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(linha(itens(get(lia, "/lancamentos")), "Mercado do Max").get("autor"))
                .as("remover acesso nunca apaga lançamento, e o autor continua com nome")
                .satisfies(autor -> assertThat(mapa(autor)).containsEntry("nome", "max"));
        assertThat(itens(get(lia, "/eventos?dia=" + hoje)))
                .filteredOn(e -> "ACESSO_REVOGADO".equals(e.get("tipo")))
                .singleElement()
                .satisfies(e -> assertThat(mapa(e.get("dados")))
                        .containsEntry("pessoa", "max").containsEntry("saiu", false));
    }

    @Test
    void cancelar_e_recusar_respondem_o_convite_uma_vez_so() {
        Sessao otto = novaSessao("otto");
        Sessao pia = novaSessao("pia");
        Sessao rui = novaSessao("rui");

        Integer paraPia = (Integer) convidar(otto, "pia@exemplo.com", "EDITOR").getBody()
                .get("id");
        Integer paraRui = (Integer) convidar(otto, "rui@exemplo.com", "EDITOR").getBody()
                .get("id");

        assertThat(enviar(rui, "POST", "/api/v1/convites/" + paraPia + "/aceite", null)
                .getStatusCode())
                .as("o convite de outra pessoa parece não existir")
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(enviar(otto, "DELETE", caminho(otto, "/convites/" + paraPia), null)
                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(itens(enviar(pia, "GET", "/api/v1/convites", null))).isEmpty();
        assertThat(enviar(pia, "POST", "/api/v1/convites/" + paraPia + "/aceite", null)
                .getBody())
                .containsEntry("codigo", "CONVITE_NAO_PENDENTE");

        assertThat(enviar(rui, "POST", "/api/v1/convites/" + paraRui + "/recusa", null)
                .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(itens(get(otto, "/convites"))).isEmpty();
        assertThat(itens(enviar(rui, "GET", "/api/v1/ambientes", null)))
                .extracting(a -> a.get("id")).doesNotContain(otto.ambienteId());
    }

    @Test
    void o_banco_nao_deixa_ninguem_se_dar_acesso_sem_convite_nem_ler_convite_alheio()
            throws SQLException {
        Sessao sara = novaSessao("sara");
        Sessao tito = novaSessao("tito");
        convidar(sara, "outra@exemplo.com", "EDITOR");

        try (Connection conexao = BancoDeTeste.conexaoDaAplicacao(POSTGRES)) {
            conexao.setAutoCommit(false);
            executar(conexao, "SELECT set_config('app.usuario_id', '" + tito.usuarioId()
                    + "', true)");

            assertThat(contar(conexao, "SELECT count(*) FROM convite")).isZero();
            assertThat(contar(conexao, "SELECT count(*) FROM membros_do_ambiente("
                    + sara.ambienteId() + ")")).isZero();
            assertThat(contar(conexao, "SELECT remover_acesso(" + sara.ambienteId() + ", "
                    + sara.usuarioId() + ")")).isZero();

            assertThatThrownBy(() -> executar(conexao,
                    "INSERT INTO acesso (usuario_id, ambiente_id, papel) VALUES ("
                            + tito.usuarioId() + ", " + sara.ambienteId() + ", 'EDITOR')"))
                    .hasMessageContaining("row-level security");
            conexao.rollback();
        }
    }

    private void aceitarConviteDe(Sessao dono, Sessao convidado, String papel) {
        Integer id = (Integer) convidar(dono, convidado.email(), papel).getBody().get("id");
        assertThat(enviar(convidado, "POST", "/api/v1/convites/" + id + "/aceite", null)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> convidar(Sessao dono, String email,
            String papel) {
        return enviar(dono, "POST", caminho(dono, "/convites"),
                Map.of("email", email, "papel", papel));
    }

    private Integer criarConta(Sessao sessao, String nome) {
        var resposta = enviar(sessao, "POST", caminho(sessao, "/contas"),
                Map.of("nome", nome, "tipo", "CORRENTE", "meios", List.of("PIX")));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer meioDa(Sessao sessao, Integer contaId) {
        return (Integer) itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(m -> contaId.equals(m.get("contaId")))
                .findFirst().orElseThrow().get("id");
    }

    private ResponseEntity<Map<String, Object>> lancar(Sessao sessao, Integer meioId, int valor,
            String descricao) {
        Map<String, Object> corpo = new HashMap<>(Map.of("meioId", meioId, "sentido", "SAIDA",
                "valor", valor, "dataEvento", "2026-09-10", "descricao", descricao));
        return enviar(sessao, "POST", caminho(sessao, "/lancamentos"), corpo);
    }

    private static Map<String, Object> linha(List<Map<String, Object>> linhas, String descricao) {
        return linhas.stream().filter(l -> descricao.equals(l.get("descricao")))
                .findFirst().orElseThrow();
    }

    private ResponseEntity<Map<String, Object>> get(Sessao sessao, String sufixo) {
        return enviar(sessao, "GET", caminho(sessao, sufixo), null);
    }

    private String caminho(Sessao sessao, String sufixo) {
        return "/api/v1/ambientes/" + sessao.ambienteId() + sufixo;
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

        Sessao provisoria = new Sessao(null, null, cookie, email);
        var ambientes = enviar(provisoria, "GET", "/api/v1/ambientes", null);
        var perfil = enviar(provisoria, "GET", "/api/v1/usuarios/atual", null);

        return new Sessao((Integer) perfil.getBody().get("id"),
                (Integer) itens(ambientes).get(0).get("id"), cookie, email);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> enviar(Sessao sessao, String metodo,
            String caminho, Object corpo) {
        var requisicao = http().method(HttpMethod.valueOf(metodo))
                .uri(caminho)
                .header(HttpHeaders.COOKIE, sessao.cookie());
        if (corpo != null) {
            requisicao = requisicao.body(corpo);
        }
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) requisicao.retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        Object itens = resposta.getBody() == null ? null : resposta.getBody().get("itens");
        return itens == null ? List.of() : (List<Map<String, Object>>) itens;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    private static void executar(Connection conexao, String sql) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.execute();
        }
    }

    private static int contar(Connection conexao, String sql) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql);
                var resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }
}
