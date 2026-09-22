package br.com.cyberbank.usuario.api;

import static org.assertj.core.api.Assertions.assertThat;

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
class SessoesIT {

    private static final String FIREFOX = "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0";
    private static final String CELULAR = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/129.0 Mobile Safari/537.36";

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
    void a_lista_traz_as_sessoes_abertas_com_o_navegador_e_marca_a_desta_requisicao() {
        cadastrar("ana@exemplo.com");
        String noNotebook = entrar("ana@exemplo.com", FIREFOX);
        entrar("ana@exemplo.com", CELULAR);

        var lista = get("/api/v1/sessoes", noNotebook);
        assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> itens = itens(lista);
        assertThat(itens).hasSize(2);
        assertThat(itens).extracting(item -> item.get("navegador"))
                .containsExactlyInAnyOrder(FIREFOX, CELULAR);
        assertThat(itens).filteredOn(item -> Boolean.TRUE.equals(item.get("atual")))
                .singleElement()
                .satisfies(item -> assertThat(item.get("navegador")).isEqualTo(FIREFOX));
        assertThat(itens).allSatisfy(item -> {
            assertThat(item.get("origem")).isNotNull();
            assertThat(item.get("criadaEm")).isNotNull();
            assertThat(item.get("ultimoUsoEm")).isNotNull();
            assertThat(item).doesNotContainKey("identificadorHash");
        });
    }

    @Test
    void encerrar_outra_sessao_derruba_ela_no_clique_seguinte_e_mantem_esta() {
        cadastrar("bia@exemplo.com");
        String estaAqui = entrar("bia@exemplo.com", FIREFOX);
        String laNoCelular = entrar("bia@exemplo.com", CELULAR);
        Object idDoCelular = idDe(estaAqui, CELULAR);

        var revogada = delete("/api/v1/sessoes/" + idDoCelular, estaAqui);
        assertThat(revogada.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(revogada.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .as("quem encerrou outra sessão continua dentro")
                .isNull();

        assertThat(get("/api/v1/usuarios/atual", laNoCelular).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(get("/api/v1/usuarios/atual", estaAqui).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(itens(get("/api/v1/sessoes", estaAqui))).hasSize(1);
    }

    @Test
    void encerrar_a_propria_sessao_pela_lista_e_sair() {
        cadastrar("caio@exemplo.com");
        String cookie = entrar("caio@exemplo.com", FIREFOX);
        Object idDestaSessao = idDe(cookie, FIREFOX);

        var revogada = delete("/api/v1/sessoes/" + idDestaSessao, cookie);
        assertThat(revogada.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(revogada.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");
        assertThat(get("/api/v1/usuarios/atual", cookie).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void encerrar_todas_derruba_inclusive_a_que_pediu() {
        cadastrar("dani@exemplo.com");
        String primeira = entrar("dani@exemplo.com", FIREFOX);
        String segunda = entrar("dani@exemplo.com", CELULAR);

        var todas = delete("/api/v1/sessoes", primeira);
        assertThat(todas.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(todas.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");

        assertThat(get("/api/v1/usuarios/atual", primeira).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(get("/api/v1/usuarios/atual", segunda).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void a_sessao_de_outra_pessoa_responde_como_inexistente_e_continua_viva() {
        cadastrar("edu@exemplo.com");
        cadastrar("fabi@exemplo.com");
        String doEdu = entrar("edu@exemplo.com", FIREFOX);
        String daFabi = entrar("fabi@exemplo.com", CELULAR);
        Object idDaFabi = idDe(daFabi, CELULAR);

        assertThat(itens(get("/api/v1/sessoes", doEdu)))
                .as("a lista é só das sessões de quem pergunta")
                .hasSize(1);

        var alheia = delete("/api/v1/sessoes/" + idDaFabi, doEdu);
        assertThat(alheia.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/v1/usuarios/atual", daFabi).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(delete("/api/v1/sessoes/999999", doEdu).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sem_sessao_nao_ha_lista_nem_revogacao() {
        assertThat(get("/api/v1/sessoes", null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(delete("/api/v1/sessoes", null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private Object idDe(String cookie, String navegador) {
        return itens(get("/api/v1/sessoes", cookie)).stream()
                .filter(item -> navegador.equals(item.get("navegador")))
                .findFirst().orElseThrow().get("id");
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }

    private void cadastrar(String email) {
        http().post().uri("/api/v1/usuarios")
                .body(Map.of("nome", "Alguém", "email", email, "senha", "uma senha longa"))
                .retrieve().toBodilessEntity();
    }

    private String entrar(String email, String navegador) {
        ResponseEntity<Void> resposta = http().post()
                .uri("/api/v1/sessoes")
                .header(HttpHeaders.USER_AGENT, navegador)
                .body(Map.of("email", email, "senha", "uma senha longa"))
                .retrieve()
                .toBodilessEntity();
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        String cookie = resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        return cookie.substring(0, cookie.indexOf(';'));
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> get(String caminho, String cookie) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri(caminho)
                .headers(cabecalhos -> {
                    if (cookie != null) {
                        cabecalhos.add(HttpHeaders.COOKIE, cookie);
                    }
                })
                .retrieve()
                .toEntity(Map.class);
    }

    private ResponseEntity<Void> delete(String caminho, String cookie) {
        return http().delete()
                .uri(caminho)
                .headers(cabecalhos -> {
                    if (cookie != null) {
                        cabecalhos.add(HttpHeaders.COOKIE, cookie);
                    }
                })
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }
}
