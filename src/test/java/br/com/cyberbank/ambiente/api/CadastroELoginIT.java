package br.com.cyberbank.ambiente.api;

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
import org.springframework.web.client.RestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A fatia inteira, contra Postgres real (ADR-0011): cadastrar, entrar, ver que o Ambiente
 * Pessoal nasceu com as quatorze categorias de sistema — e que um segundo usuario nao enxerga
 * NADA do primeiro.
 *
 * <p>A aplicacao conecta com o papel NAO-DONO e o Flyway com o dono, como em producao. Se o
 * isolamento quebrar, e aqui que fica vermelho.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CadastroELoginIT {

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

        // Parametros baixos so para a suite nao gastar segundos por login. O numero de verdade
        // e calibrado no host (docs/01-arquitetura/seguranca.md).
        registro.add("cyberbank.argon2.memoria", () -> 1024);
        registro.add("cyberbank.argon2.iteracoes", () -> 1);
        registro.add("cyberbank.argon2.paralelismo", () -> 1);
    }

    @LocalServerPort
    private int porta;

    /** Sem tratador de erro: aqui a resposta 4xx E O QUE ESTA SENDO TESTADO, nao uma excecao. */
    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }

    @Test
    void o_cadastro_cria_usuario_ambiente_pessoal_e_as_quatorze_categorias() {
        var usuario = cadastrar("Ana", "ana@exemplo.com", "uma senha longa");
        assertThat(usuario.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(usuario.getBody()).containsEntry("email", "ana@exemplo.com");

        String cookie = entrar("ana@exemplo.com", "uma senha longa");

        var ambientes = get("/api/v1/ambientes", cookie);
        assertThat(itens(ambientes)).singleElement().satisfies(ambiente -> {
            assertThat(ambiente).containsEntry("nome", "Ambiente Pessoal");
            assertThat(ambiente).containsEntry("papel", "DONO");
        });

        Integer ambienteId = (Integer) itens(ambientes).get(0).get("id");
        var categorias = get("/api/v1/ambientes/" + ambienteId + "/categorias?sistema=true", cookie);
        assertThat(itens(categorias))
                .as("sete operações × dois sentidos")
                .hasSize(14)
                .allSatisfy(categoria -> assertThat(categoria).containsEntry("sistema", true));
    }

    @Test
    void o_sistema_nao_cria_categoria_de_usuario_nenhuma() {
        cadastrar("Vazio", "vazio@exemplo.com", "uma senha longa");
        String cookie = entrar("vazio@exemplo.com", "uma senha longa");
        Integer ambienteId = (Integer) itens(get("/api/v1/ambientes", cookie)).get(0).get("id");

        var doUsuario = get("/api/v1/ambientes/" + ambienteId + "/categorias", cookie);

        assertThat(itens(doUsuario))
                .as("tela vazia no primeiro lançamento é preço aceito")
                .isEmpty();
    }

    @Test
    void um_segundo_usuario_nao_enxerga_nada_do_primeiro() {
        cadastrar("Dona", "dona@exemplo.com", "uma senha longa");
        String cookieDaDona = entrar("dona@exemplo.com", "uma senha longa");
        Integer ambienteDaDona = (Integer) itens(get("/api/v1/ambientes", cookieDaDona)).get(0).get("id");

        cadastrar("Curioso", "curioso@exemplo.com", "uma senha longa");
        String cookieDoCurioso = entrar("curioso@exemplo.com", "uma senha longa");

        assertThat(itens(get("/api/v1/ambientes", cookieDoCurioso)))
                .as("cada um vê só o próprio Ambiente Pessoal")
                .hasSize(1)
                .allSatisfy(a -> assertThat(a.get("id")).isNotEqualTo(ambienteDaDona));

        var invasao = get("/api/v1/ambientes/" + ambienteDaDona + "/categorias?sistema=true", cookieDoCurioso);
        assertThat(invasao.getStatusCode())
                .as("ambiente sem acesso responde igual a inexistente")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(invasao.getBody()).containsEntry("codigo", "NAO_ENCONTRADO");

        var inexistente = get("/api/v1/ambientes/999999/categorias", cookieDoCurioso);
        assertThat(inexistente.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(inexistente.getBody()).containsEntry("codigo", "NAO_ENCONTRADO");
    }

    @Test
    void email_inexistente_e_senha_errada_dao_a_mesma_resposta() {
        cadastrar("Alvo", "alvo@exemplo.com", "uma senha longa");

        var senhaErrada = tentarEntrar("alvo@exemplo.com", "outra senha longa");
        var emailInexistente = tentarEntrar("ninguem@exemplo.com", "outra senha longa");

        assertThat(senhaErrada.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(emailInexistente.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(emailInexistente.getBody()).isEqualTo(senhaErrada.getBody());
    }

    @Test
    void o_mesmo_email_nao_se_cadastra_duas_vezes() {
        cadastrar("Primeira", "repetido@exemplo.com", "uma senha longa");

        var segunda = cadastrar("Segunda", "REPETIDO@exemplo.com", "uma senha longa");

        assertThat(segunda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(segunda.getBody()).containsEntry("codigo", "EMAIL_JA_CADASTRADO");
    }

    @Test
    void o_cadastro_invalido_devolve_todos_os_campos_de_uma_vez() {
        var resposta = cadastrar("", "sem-arroba", "curta");

        // 422 pelo numero: o Spring 7 tem UNPROCESSABLE_ENTITY e UNPROCESSABLE_CONTENT, e as
        // duas constantes nao sao a mesma — o contrato e o codigo, nao o nome da constante.
        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat(resposta.getBody()).containsEntry("codigo", "VALIDACAO");
        assertThat((List<?>) resposta.getBody().get("erros")).hasSize(3);
    }

    @Test
    void sem_sessao_nao_se_le_nada_e_sair_derruba_a_sessao() {
        assertThat(get("/api/v1/ambientes", null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        cadastrar("Passageiro", "passageiro@exemplo.com", "uma senha longa");
        String cookie = entrar("passageiro@exemplo.com", "uma senha longa");
        assertThat(get("/api/v1/ambientes", cookie).getStatusCode()).isEqualTo(HttpStatus.OK);

        var saida = http().delete()
                .uri("/api/v1/sessoes/atual")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toBodilessEntity();
        assertThat(saida.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(get("/api/v1/ambientes", cookie).getStatusCode())
                .as("revogar tem efeito no clique seguinte")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- o que faz a conversa com a API ---

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> cadastrar(String nome, String email, String senha) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().post()
                .uri("/api/v1/usuarios")
                .body(Map.of("nome", nome, "email", email, "senha", senha))
                .retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> tentarEntrar(String email, String senha) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().post()
                .uri("/api/v1/sessoes")
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .toEntity(Map.class);
    }

    /** O cookie e Secure: o cliente o repassa a mao, como o curl faz (docs/04-api/convencoes.md). */
    private String entrar(String email, String senha) {
        ResponseEntity<Void> resposta = http().post()
                .uri("/api/v1/sessoes")
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .toBodilessEntity();

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        String cookie = resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("HttpOnly").contains("Secure").contains("SameSite=Lax");
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }
}
