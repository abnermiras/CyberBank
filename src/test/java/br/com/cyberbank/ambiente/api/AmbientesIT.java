package br.com.cyberbank.ambiente.api;

import static org.assertj.core.api.Assertions.assertThat;

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
class AmbientesIT {

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
    void o_ambiente_criado_entra_na_lista_depois_do_pessoal_com_o_dono_e_a_data() {
        String cookie = cadastrarEEntrar("Ana", "ana@exemplo.com");

        var criado = enviar("POST", "/api/v1/ambientes", Map.of("nome", "  Casa "), cookie);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criado.getBody()).containsEntry("nome", "Casa").containsEntry("papel", "DONO");
        assertThat(criado.getBody().get("criadoEm")).isNotNull();
        assertThat(criado.getHeaders().getLocation().getPath())
                .isEqualTo("/api/v1/ambientes/" + criado.getBody().get("id"));

        List<Map<String, Object>> itens = itens(get("/api/v1/ambientes", cookie));
        assertThat(itens).extracting(item -> item.get("nome"))
                .containsExactly("Ambiente Pessoal", "Casa");
        assertThat(itens).allSatisfy(item -> assertThat(item.get("criadoEm")).isNotNull());
    }

    @Test
    void o_ambiente_criado_nasce_com_o_jogo_de_categorias_de_sistema_como_o_pessoal() {
        String cookie = cadastrarEEntrar("Bia", "bia@exemplo.com");
        Object pessoal = itens(get("/api/v1/ambientes", cookie)).getFirst().get("id");
        Object casa = enviar("POST", "/api/v1/ambientes", Map.of("nome", "Casa"), cookie)
                .getBody().get("id");

        int doPessoal = sistemas(pessoal, cookie);
        assertThat(doPessoal).isPositive();
        assertThat(sistemas(casa, cookie)).isEqualTo(doPessoal);
    }

    @Test
    void ambiente_sem_nome_nao_nasce() {
        String cookie = cadastrarEEntrar("Caio", "caio@exemplo.com");

        var vazio = enviar("POST", "/api/v1/ambientes", Map.of("nome", "  "), cookie);
        assertThat(vazio.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(vazio.getBody()).containsEntry("codigo", "VALIDACAO");
        assertThat(itens(get("/api/v1/ambientes", cookie))).hasSize(1);
    }

    @Test
    void o_dono_renomeia_e_o_nome_novo_e_o_que_a_lista_devolve() {
        String cookie = cadastrarEEntrar("Dani", "dani@exemplo.com");
        Object pessoal = itens(get("/api/v1/ambientes", cookie)).getFirst().get("id");

        var renomeado = enviar("PATCH", "/api/v1/ambientes/" + pessoal,
                Map.of("nome", "Dani e Edu"), cookie);
        assertThat(renomeado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(renomeado.getBody()).containsEntry("nome", "Dani e Edu");

        assertThat(itens(get("/api/v1/ambientes", cookie)))
                .extracting(item -> item.get("nome")).containsExactly("Dani e Edu");

        var vazio = enviar("PATCH", "/api/v1/ambientes/" + pessoal, Map.of("nome", ""), cookie);
        assertThat(vazio.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void o_ambiente_de_outra_pessoa_nao_se_ve_nem_se_renomeia() {
        String daFabi = cadastrarEEntrar("Fabi", "fabi@exemplo.com");
        String doGui = cadastrarEEntrar("Gui", "gui@exemplo.com");
        Object casaDaFabi = enviar("POST", "/api/v1/ambientes", Map.of("nome", "Casa"), daFabi)
                .getBody().get("id");

        assertThat(itens(get("/api/v1/ambientes", doGui)))
                .extracting(item -> item.get("nome")).containsExactly("Ambiente Pessoal");

        var alheio = enviar("PATCH", "/api/v1/ambientes/" + casaDaFabi,
                Map.of("nome", "Meu"), doGui);
        assertThat(alheio.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(itens(get("/api/v1/ambientes", daFabi)))
                .extracting(item -> item.get("nome")).contains("Casa");
    }

    @Test
    void criar_ambiente_exige_sessao() {
        assertThat(enviar("POST", "/api/v1/ambientes", Map.of("nome", "Casa"), null)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private int sistemas(Object ambienteId, String cookie) {
        return itens(get("/api/v1/ambientes/" + ambienteId + "/categorias?sistema=true", cookie))
                .stream()
                .mapToInt(item -> Boolean.TRUE.equals(item.get("sistema")) ? 1 : 0)
                .sum();
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }

    private String cadastrarEEntrar(String nome, String email) {
        http().post().uri("/api/v1/usuarios")
                .body(Map.of("nome", nome, "email", email, "senha", "uma senha longa"))
                .retrieve().toBodilessEntity();

        ResponseEntity<Void> resposta = http().post()
                .uri("/api/v1/sessoes")
                .body(Map.of("email", email, "senha", "uma senha longa"))
                .retrieve()
                .toBodilessEntity();
        String cookie = resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        return cookie.substring(0, cookie.indexOf(';'));
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> get(String caminho, String cookie) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri(caminho)
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> enviar(
            String metodo, String caminho, Map<String, Object> corpo, String cookie) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http()
                .method(HttpMethod.valueOf(metodo))
                .uri(caminho)
                .headers(cabecalhos -> {
                    if (cookie != null) {
                        cabecalhos.add(HttpHeaders.COOKIE, cookie);
                    }
                })
                .body(corpo)
                .retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }
}
