package br.com.cyberbank.usuario.api;

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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerfilIT {

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
    void quem_se_cadastra_ja_nasce_com_avatar_e_sem_telegram() {
        var cadastro = cadastrar("Ana", "ana@exemplo.com", "uma senha longa");
        assertThat(cadastro.getBody()).containsKey("avatar");

        String cookie = entrar("ana@exemplo.com", "uma senha longa");
        var perfil = get("/api/v1/usuarios/atual", cookie);

        assertThat(perfil.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(perfil.getBody()).containsEntry("email", "ana@exemplo.com");
        assertThat(perfil.getBody().get("telegramChatId")).isNull();
        assertThat(avatares(perfil))
                .as("a tela recebe a lista do servidor e não mantém uma segunda cópia")
                .hasSize(10)
                .contains((String) perfil.getBody().get("avatar"));
    }

    @Test
    void o_perfil_troca_nome_e_avatar_e_nunca_o_email() {
        cadastrar("Bia", "bia@exemplo.com", "uma senha longa");
        String cookie = entrar("bia@exemplo.com", "uma senha longa");

        var trocado = patch(Map.of("nome", "Bia Miras", "avatar", "ROBO"), cookie);
        assertThat(trocado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(trocado.getBody()).containsEntry("nome", "Bia Miras");
        assertThat(trocado.getBody()).containsEntry("avatar", "ROBO");

        var comEmail = patch(Map.of("email", "outra@exemplo.com"), cookie);
        assertThat(comEmail.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(comEmail.getBody()).containsEntry("codigo", "VALIDACAO");
        assertThat(get("/api/v1/usuarios/atual", cookie).getBody())
                .containsEntry("email", "bia@exemplo.com");

        var avatarInventado = patch(Map.of("avatar", "CACHORRO"), cookie);
        assertThat(avatarInventado.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void o_vinculo_do_telegram_se_poe_e_se_tira_sem_tocar_no_resto_do_perfil() {
        cadastrar("Caio", "caio@exemplo.com", "uma senha longa");
        String cookie = entrar("caio@exemplo.com", "uma senha longa");

        var vinculado = vincularTelegram(184712993L, cookie);
        assertThat(vinculado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vinculado.getBody().get("telegramChatId")).isEqualTo(184712993);

        patch(Map.of("nome", "Caio Miras"), cookie);

        var depois = get("/api/v1/usuarios/atual", cookie);
        assertThat(depois.getBody()).containsEntry("nome", "Caio Miras");
        assertThat(depois.getBody().get("telegramChatId"))
                .as("trocar o nome não desvincula o chat")
                .isEqualTo(184712993);

        assertThat(desvincularTelegram(cookie).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get("/api/v1/usuarios/atual", cookie).getBody().get("telegramChatId")).isNull();

        assertThat(desvincularTelegram(cookie).getStatusCode())
                .as("desvincular o que não está vinculado não é erro")
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void dois_usuarios_nao_apontam_para_o_mesmo_chat() {
        cadastrar("Dani", "dani@exemplo.com", "uma senha longa");
        cadastrar("Edu", "edu@exemplo.com", "uma senha longa");

        String daDani = entrar("dani@exemplo.com", "uma senha longa");
        String doEdu = entrar("edu@exemplo.com", "uma senha longa");

        assertThat(vincularTelegram(555000L, daDani).getStatusCode()).isEqualTo(HttpStatus.OK);

        var repetido = vincularTelegram(555000L, doEdu);
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(repetido.getBody()).containsEntry("codigo", "TELEGRAM_JA_VINCULADO");

        assertThat(vincularTelegram(555000L, daDani).getStatusCode())
                .as("reenviar o próprio chat id não é conflito consigo mesmo")
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void zero_nao_e_chat_de_ninguem() {
        cadastrar("Fabi", "fabi@exemplo.com", "uma senha longa");
        String cookie = entrar("fabi@exemplo.com", "uma senha longa");

        assertThat(vincularTelegram(0L, cookie).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void trocar_a_senha_exige_a_atual_e_derruba_todas_as_sessoes() {
        cadastrar("Gabi", "gabi@exemplo.com", "uma senha longa");
        String primeira = entrar("gabi@exemplo.com", "uma senha longa");
        String segunda = entrar("gabi@exemplo.com", "uma senha longa");

        var errada = trocarSenha("a errada", "outra senha longa", primeira);
        assertThat(errada.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(errada.getBody()).containsEntry("codigo", "SENHA_ATUAL_INVALIDA");

        var curta = trocarSenha("uma senha longa", "1234567", primeira);
        assertThat(curta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(curta.getBody()).containsEntry("codigo", "VALIDACAO");

        var igual = trocarSenha("uma senha longa", "uma senha longa", primeira);
        assertThat(igual.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        assertThat(trocarSenha("uma senha longa", "outra senha longa", primeira).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(get("/api/v1/usuarios/atual", primeira).getStatusCode())
                .as("inclusive a sessão que trocou")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(get("/api/v1/usuarios/atual", segunda).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(get("/api/v1/usuarios/atual", entrar("gabi@exemplo.com", "outra senha longa"))
                .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void o_perfil_nao_existe_sem_sessao() {
        assertThat(get("/api/v1/usuarios/atual", null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> cadastrar(String nome, String email, String senha) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().post()
                .uri("/api/v1/usuarios")
                .body(Map.of("nome", nome, "email", email, "senha", senha))
                .retrieve()
                .toEntity(Map.class);
    }

    private String entrar(String email, String senha) {
        ResponseEntity<Void> resposta = http().post()
                .uri("/api/v1/sessoes")
                .body(Map.of("email", email, "senha", senha))
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

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> patch(Map<String, Object> corpo, String cookie) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().patch()
                .uri("/api/v1/usuarios/atual")
                .header(HttpHeaders.COOKIE, cookie)
                .body(corpo)
                .retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> vincularTelegram(Long chatId, String cookie) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("chatId", chatId);
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().put()
                .uri("/api/v1/usuarios/atual/telegram")
                .header(HttpHeaders.COOKIE, cookie)
                .body(corpo)
                .retrieve()
                .toEntity(Map.class);
    }

    private ResponseEntity<Void> desvincularTelegram(String cookie) {
        return http().delete()
                .uri("/api/v1/usuarios/atual/telegram")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> trocarSenha(
            String senhaAtual, String novaSenha, String cookie) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().put()
                .uri("/api/v1/usuarios/atual/senha")
                .header(HttpHeaders.COOKIE, cookie)
                .body(Map.of("senhaAtual", senhaAtual, "novaSenha", novaSenha))
                .retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<String> avatares(ResponseEntity<Map<String, Object>> perfil) {
        return (List<String>) perfil.getBody().get("avataresDisponiveis");
    }
}
