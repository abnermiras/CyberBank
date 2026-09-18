package br.com.cyberbank.comum.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import br.com.cyberbank.comum.persistencia.BancoDeTeste;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FrontEstaticoIT {

    private static final List<String> ARQUIVOS_DO_FRONT = List.of(
            "/app.html",
            "/assets/js/telas/contas.js",
            "/assets/js/telas/detalhe.js",
            "/assets/js/lancar.js",
            "/assets/css/app.css");

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
    void nenhum_arquivo_do_front_e_servido_sem_mandar_revalidar() {
        RestClient http = RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();

        assertThat(ARQUIVOS_DO_FRONT).allSatisfy(caminho -> {
            HttpHeaders cabecalhos = http.get().uri(caminho).retrieve()
                    .toBodilessEntity().getHeaders();

            assertThat(cabecalhos.getCacheControl())
                    .as("%s sem Cache-Control cai no cache heurístico do navegador, "
                            + "e um arquivo antigo fica ao lado de um novo", caminho)
                    .isNotNull()
                    .contains("no-cache");
        });
    }
}
