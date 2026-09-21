package br.com.cyberbank.recorrencia.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.persistencia.BancoDeTeste;
import br.com.cyberbank.comum.rotina.RotinaDiaria;
import br.com.cyberbank.comum.tempo.DiaLocal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SeriesIT.RelogioDoTeste.class)
class SeriesIT {

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

    static final RelogioAjustavel RELOGIO = new RelogioAjustavel();

    @TestConfiguration
    static class RelogioDoTeste {
        @Bean
        @Primary
        Clock relogioDoTeste() {
            return RELOGIO;
        }
    }

    static final class RelogioAjustavel extends Clock {
        private Instant instante = LocalDate.of(2026, 9, 10)
                .atTime(15, 0).atZone(DiaLocal.BRASILIA).toInstant();

        void em(LocalDate dia) {
            instante = dia.atTime(3, 5).atZone(DiaLocal.BRASILIA).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }

    @LocalServerPort
    private int porta;

    @Autowired
    private RotinaDiaria rotinaDiaria;

    @Test
    void a_recorrencia_nasce_com_a_ocorrencia_do_ciclo_aberto_e_ela_e_prevista() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("assina");
        Integer cartao = criarCartao(sessao, "UltraVioleta");

        var criada = criarRecorrencia(sessao, cartao, 3990, 12, "Netflix");

        assertThat(criada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(criada.getBody()).containsEntry("periodicidade", "MENSAL");
        assertThat(criada.getBody()).containsEntry("ativa", true);

        @SuppressWarnings("unchecked")
        Map<String, Object> ocorrencia =
                (Map<String, Object>) criada.getBody().get("ocorrenciaDoCicloAberto");

        assertThat(ocorrencia)
                .as("a assinatura ainda não foi cobrada: no cartão, PREVISTO é o que sobra"
                        + " para a ocorrência que não aconteceu")
                .containsEntry("situacao", "PREVISTO")
                .containsEntry("valorCentavos", 3990)
                .containsEntry("dataEvento", "2026-10-12");
    }

    @Test
    void a_ocorrencia_prevista_ja_conta_no_total_da_fatura_aberta() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("naosoma");
        Integer cartao = criarCartao(sessao, "UltraVioleta");

        criarRecorrencia(sessao, cartao, 3990, 12, "Netflix");

        assertThat(faturas(sessao, cartao).get(0))
                .as("a fatura aberta mostra o que vai ser cobrado, e o previsto segura limite")
                .containsEntry("totalCentavos", 3990);
    }

    @Test
    void o_fechamento_lanca_a_ocorrencia_na_fatura_recem_aberta_e_nunca_duas_vezes() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("fechaelanca");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        criarRecorrencia(sessao, cartao, 3990, 12, "Netflix");

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();
        rotinaDiaria.executar();

        assertThat(ocorrenciasDe(sessao, "Netflix"))
                .as("uma por ciclo: a de outubro, do cadastro, e a de novembro, do fechamento")
                .hasSize(2);

        assertThat(recorrencias(sessao).get(0))
                .containsEntry("ocorrenciasLancadas", 2)
                .containsEntry("proximaCobrancaEm", "2026-10-12");

        assertThat(eventosDoDia(sessao, "2026-09-27")).anySatisfy(evento -> {
            assertThat(evento).containsEntry("tipo", "OCORRENCIA_DE_RECORRENCIA");
            assertThat(evento)
                    .as("ninguém pediu: a rotina lançou, e o Diário responde por quem")
                    .containsEntry("origem", "SISTEMA");
        });
    }

    @Test
    void a_serie_aparece_na_lista_com_o_parcelamento_e_o_que_falta_dele() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("listaserie");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        Integer meio = meioDo(sessao, cartao);

        post(sessao, "/parcelamentos", new HashMap<>(Map.of(
                "meioId", meio, "valor", 500000, "parcelas", 3,
                "dataEvento", "2026-09-10", "descricao", "Aulas de espanhol")));
        criarRecorrencia(sessao, cartao, 3990, 12, "Netflix");

        assertThat(parcelamentos(sessao)).singleElement().satisfies(compra -> {
            assertThat(compra).containsEntry("valorDaCompraCentavos", 500000);
            assertThat(compra).containsEntry("parcelas", 3);
            assertThat(compra)
                    .as("nenhuma fatura foi paga ainda: tudo em aberto")
                    .containsEntry("parcelasLiquidadas", 0)
                    .containsEntry("restanteCentavos", 500000);
        });

        assertThat(recorrencias(sessao)).singleElement().satisfies(assinatura -> {
            assertThat(assinatura).containsEntry("descricao", "Netflix");
            assertThat(assinatura).containsEntry("valorCentavos", 3990);
            assertThat(assinatura).containsEntry("dia", 12);
        });
    }

    @Test
    void recorrencia_fora_do_cartao_e_recusada_porque_nao_ha_fatura_que_a_dispare() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("semfatura");
        Integer corrente = criarCorrente(sessao, "Nubank");

        var resposta = post(sessao, "/recorrencias", new HashMap<>(Map.of(
                "meioId", meioDo(sessao, corrente), "valor", 3990, "dia", 12,
                "descricao", "Netflix no débito")));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("codigo", "MEIO_NAO_RECORRE");
    }

    @Test
    void dia_fora_da_faixa_nao_cria_recorrencia() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("diaruim");
        Integer cartao = criarCartao(sessao, "UltraVioleta");

        var resposta = criarRecorrencia(sessao, cartao, 3990, 32, "Netflix");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
        assertThat(resposta.getBody()).containsEntry("codigo", "VALIDACAO");
    }

    private ResponseEntity<Map<String, Object>> criarRecorrencia(Sessao sessao, Integer cartao,
            int valor, int dia, String descricao) {

        return post(sessao, "/recorrencias", new HashMap<>(Map.of(
                "meioId", meioDo(sessao, cartao), "valor", valor, "dia", dia,
                "descricao", descricao)));
    }

    private List<Map<String, Object>> recorrencias(Sessao sessao) {
        return lista(get(sessao, "/series"), "recorrencias");
    }

    private List<Map<String, Object>> parcelamentos(Sessao sessao) {
        return lista(get(sessao, "/series"), "parcelamentos");
    }

    private List<Map<String, Object>> ocorrenciasDe(Sessao sessao, String descricao) {
        return itens(get(sessao, "/lancamentos")).stream()
                .filter(lancamento -> descricao.equals(lancamento.get("descricao")))
                .toList();
    }

    private List<Map<String, Object>> eventosDoDia(Sessao sessao, String dia) {
        return itens(get(sessao, "/eventos?dia=" + dia));
    }

    private List<Map<String, Object>> faturas(Sessao sessao, Integer contaId) {
        return itens(get(sessao, "/faturas?contaId=" + contaId));
    }

    private Integer meioDo(Sessao sessao, Integer contaId) {
        return itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(meio -> contaId.equals(meio.get("contaId")))
                .map(meio -> (Integer) meio.get("id"))
                .findFirst().orElseThrow();
    }

    private Integer criarCartao(Sessao sessao, String nome) {
        var resposta = post(sessao, "/contas", new HashMap<>(Map.of(
                "nome", nome, "tipo", "CARTAO", "diaVencimento", 5, "diasAntesFechamento", 8,
                "cartoes", List.of("Físico ****1234"))));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer criarCorrente(Sessao sessao, String nome) {
        var resposta = post(sessao, "/contas", new HashMap<>(Map.of(
                "nome", nome, "tipo", "CORRENTE", "saldoInicial", 5000000,
                "meios", List.of("PIX"))));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Sessao avancarPara(Sessao sessao, LocalDate dia) {
        RELOGIO.em(dia);
        return new Sessao(sessao.ambienteId(), entrar(sessao.apelido()), sessao.apelido());
    }

    private ResponseEntity<Map<String, Object>> get(Sessao sessao, String sufixo) {
        return troca(http().get().uri(caminho(sessao, sufixo)), sessao);
    }

    private ResponseEntity<Map<String, Object>> post(Sessao sessao, String sufixo,
            Map<String, Object> corpo) {
        return troca(http().post().uri(caminho(sessao, sufixo)).body(corpo), sessao);
    }

    private String caminho(Sessao sessao, String sufixo) {
        return "/api/v1/ambientes/" + sessao.ambienteId() + sufixo;
    }

    private record Sessao(Integer ambienteId, String cookie, String apelido) {
    }

    @SuppressWarnings("unchecked")
    private Sessao novaSessao(String apelido) {
        http().post().uri("/api/v1/usuarios")
                .body(Map.of("nome", apelido, "email", apelido + "@exemplo.com",
                        "senha", "uma senha longa"))
                .retrieve().toBodilessEntity();

        String cookie = entrar(apelido);

        var ambientes = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri("/api/v1/ambientes")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(Map.class);
        return new Sessao((Integer) itens(ambientes).get(0).get("id"), cookie, apelido);
    }

    private String entrar(String apelido) {
        return http().post().uri("/api/v1/sessoes")
                .body(Map.of("email", apelido + "@exemplo.com", "senha", "uma senha longa"))
                .retrieve().toBodilessEntity()
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> troca(RestClient.RequestHeadersSpec<?> spec,
            Sessao sessao) {
        if (sessao.cookie() != null) {
            spec = spec.header(HttpHeaders.COOKIE, sessao.cookie());
        }
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) spec.retrieve()
                .toEntity(Map.class);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta) {
        return (List<Map<String, Object>>) resposta.getBody().get("itens");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(ResponseEntity<Map<String, Object>> resposta,
            String chave) {
        return (List<Map<String, Object>>) resposta.getBody().get(chave);
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }
}
