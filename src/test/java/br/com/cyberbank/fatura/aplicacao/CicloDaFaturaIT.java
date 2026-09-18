package br.com.cyberbank.fatura.aplicacao;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(CicloDaFaturaIT.RelogioDoTeste.class)
class CicloDaFaturaIT {

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
    void o_ciclo_fecha_a_aberta_abre_a_seguinte_e_rola_o_que_venceu_sem_ser_pago() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("ciclo");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 161060, "Mercado do mês");

        assertThat(faturas(sessao, cartao)).singleElement().satisfies(outubro -> {
            assertThat(outubro).containsEntry("competencia", "2026-10");
            assertThat(outubro).containsEntry("status", "ABERTA");
            assertThat(outubro).containsEntry("totalCentavos", 161060);
        });

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();

        var depoisDoFechamento = faturas(sessao, cartao);
        assertThat(depoisDoFechamento).hasSize(2);
        assertThat(competencia(depoisDoFechamento, "2026-10"))
                .as("chegou a dataFechamento: o ciclo acabou, e fechar não liquida nada")
                .containsEntry("status", "FECHADA");
        assertThat(competencia(depoisDoFechamento, "2026-11"))
                .as("a FUTURA seguinte vira ABERTA — criada na hora porque não existia")
                .containsEntry("status", "ABERTA");
        assertThat(situacoesDosLancamentos(sessao))
                .as("nenhuma situação muda no fechamento")
                .containsOnly("PROVISIONADO");

        comprar(sessao, cartao, 110010, "Novembro");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 5));
        rotinaDiaria.executar();
        assertThat(competencia(faturas(sessao, cartao), "2026-10"))
                .as("o usuário tem o dia inteiro do vencimento para pagar")
                .containsEntry("aPagarCentavos", 161060);

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();

        var depoisDaRolagem = faturas(sessao, cartao);
        assertThat(competencia(depoisDaRolagem, "2026-10")).satisfies(outubro -> {
            assertThat(outubro)
                    .as("o total histórico não cai: outubro continua tendo sido R$ 1.610,60")
                    .containsEntry("totalCentavos", 161060);
            assertThat(outubro).containsEntry("roladoCentavos", 161060);
            assertThat(outubro).containsEntry("aPagarCentavos", 0);
            assertThat(outubro).containsEntry("rolada", true);
            assertThat(outubro).containsEntry("encerrada", true);
        });
        assertThat(competencia(depoisDaRolagem, "2026-11"))
                .as("o débito entra no total da seguinte: é o saldo anterior da fatura de papel")
                .containsEntry("totalCentavos", 271070);

        assertThat(dividaDoCartao(sessao, cartao))
                .as("os dois lados somam zero: a rolagem move dívida de período, não cria dívida")
                .isEqualTo(271070);
    }

    @Test
    void o_par_de_rolagem_e_datado_no_vencimento_e_o_debito_nasce_provisionado() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("pardarolagem");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 100000, "Compra");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();

        var rolagem = extrato(sessao).stream()
                .filter(l -> String.valueOf(l.get("descricao")).contains("fatura"))
                .toList();

        assertThat(rolagem).as("a rolagem tem sempre dois lados").hasSize(2);
        assertThat(rolagem).allSatisfy(lado -> {
            assertThat(lado)
                    .as("datados no vencimento, não no dia em que a rotina rodou")
                    .containsEntry("dataEvento", "2026-10-05");
            assertThat(lado).containsEntry("doCiclo", true);
        });

        assertThat(rolagem).anySatisfy(credito -> {
            assertThat(credito).containsEntry("descricao", "Rolado para a fatura seguinte");
            assertThat(credito).containsEntry("sentido", "ENTRADA");
            assertThat(credito).containsEntry("situacao", "REALIZADO");
        });
        assertThat(rolagem).anySatisfy(debito -> {
            assertThat(debito).containsEntry("descricao", "Saldo da fatura anterior");
            assertThat(debito).containsEntry("sentido", "SAIDA");
            assertThat(debito)
                    .as("PREVISTO apagaria a dívida: o crédito entraria no saldo e o débito não")
                    .containsEntry("situacao", "PROVISIONADO");
        });
    }

    @Test
    void a_rolagem_nunca_rola_para_si_mesma_e_o_par_por_dia_para_sempre_nao_acontece() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("semautorolagem");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 5000, "Compra");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();
        int depoisDaPrimeira = extrato(sessao).size();

        for (int dia = 7; dia <= 12; dia++) {
            sessao = avancarPara(sessao, LocalDate.of(2026, 10, dia));
            rotinaDiaria.executar();
        }

        assertThat(extrato(sessao))
                .as("um par e um FATURA_ROLADA por dia, para sempre, é o que isto impede")
                .hasSize(depoisDaPrimeira);
        assertThat(eventosDoDia(sessao, "2026-10-12"))
                .as("rodada que não faz nada não grava nada")
                .isEmpty();
    }

    @Test
    void o_pi_desligado_dois_ciclos_recupera_em_ordem_cronologica() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("pidesligado");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 100000, "Outubro");

        sessao = avancarPara(sessao, LocalDate.of(2026, 12, 20));
        rotinaDiaria.executar();

        var todas = faturas(sessao, cartao);
        assertThat(todas).as("outubro, novembro, dezembro e a de janeiro, aberta").hasSize(4);

        assertThat(competencia(todas, "2026-10")).containsEntry("roladoCentavos", 100000);
        assertThat(competencia(todas, "2026-11"))
                .as("outubro rolou para a fatura que estava aberta quando outubro venceu")
                .containsEntry("totalCentavos", 100000);
        assertThat(competencia(todas, "2026-11")).containsEntry("roladoCentavos", 100000);
        assertThat(competencia(todas, "2026-12")).containsEntry("totalCentavos", 100000);
        assertThat(competencia(todas, "2027-01")).containsEntry("status", "ABERTA");

        assertThat(dividaDoCartao(sessao, cartao))
                .as("a dívida do cartão não mudou em nenhum momento")
                .isEqualTo(100000);
    }

    @Test
    void rodar_duas_vezes_no_mesmo_dia_nao_fecha_a_mesma_fatura_duas_vezes() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("idempotente");
        Integer cartao = criarCartao(sessao, "UltraVioleta");

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();
        var primeira = faturas(sessao, cartao);

        rotinaDiaria.executar();

        assertThat(faturas(sessao, cartao)).hasSameSizeAs(primeira);
        assertThat(competencia(faturas(sessao, cartao), "2026-11")).containsEntry("status", "ABERTA");
    }

    @Test
    void o_exemplo_literal_do_doc_paga_800_de_1610_60_e_o_resto_rola() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("exemploliteral");
        Integer nubank = criarCorrente(sessao, "Nubank");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 161060, "Setembro");

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();
        comprar(sessao, cartao, 110010, "Outubro");

        Integer outubro = (Integer) competencia(faturas(sessao, cartao), "2026-10").get("id");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 1));
        var agendado = post(sessao, "/faturas/" + outubro + "/pagamentos", new HashMap<>(Map.of(
                "contaPagadoraId", nubank, "valor", 80000, "dataEvento", "2026-10-04")));
        assertThat(agendado.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(competencia(faturas(sessao, cartao), "2026-10")).satisfies(fatura -> {
            assertThat(fatura)
                    .as("o pagamento agendado ainda não pagou: pago é só o REALIZADO")
                    .containsEntry("pagoCentavos", 0);
            assertThat(fatura).containsEntry("agendadoCentavos", 80000);
            assertThat(fatura).containsEntry("aPagarCentavos", 161060);
        });

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 4));
        rotinaDiaria.executar();

        assertThat(competencia(faturas(sessao, cartao), "2026-10"))
                .as("dia 4 o previsto realiza, e aí sim ele pagou")
                .containsEntry("pagoCentavos", 80000);

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();

        assertThat(competencia(faturas(sessao, cartao), "2026-10")).satisfies(outubroDepois -> {
            assertThat(outubroDepois)
                    .as("o total histórico não cai: outubro continua tendo sido R$ 1.610,60")
                    .containsEntry("totalCentavos", 161060);
            assertThat(outubroDepois).containsEntry("roladoCentavos", 81060);
            assertThat(outubroDepois).containsEntry("aPagarCentavos", 0);
        });

        assertThat(competencia(faturas(sessao, cartao), "2026-11"))
                .as("novembro, que tinha R$ 1.100,10 de compras, fecha em R$ 1.910,70")
                .containsEntry("totalCentavos", 191070);
    }

    @Test
    void quitar_encerra_a_fatura_na_hora_e_o_agendado_encerra_quando_a_rotina_o_realiza() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("quitacao");
        Integer nubank = criarCorrente(sessao, "Nubank");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 100000, "Compra");

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();
        Integer outubro = (Integer) competencia(faturas(sessao, cartao), "2026-10").get("id");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 1));
        post(sessao, "/faturas/" + outubro + "/pagamentos", new HashMap<>(Map.of(
                "contaPagadoraId", nubank, "valor", 100000, "dataEvento", "2026-10-03")));

        assertThat(situacoesDaFatura(sessao, outubro))
                .as("pagamento agendado não liquida nada: a fatura ainda não foi paga")
                .contains("PROVISIONADO");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 3));
        rotinaDiaria.executar();

        assertThat(situacoesDaFatura(sessao, outubro))
                .as("quem realizou o previsto foi a rotina — e é ela que tem de encerrar")
                .containsOnly("REALIZADO");
        assertThat(competencia(faturas(sessao, cartao), "2026-10"))
                .containsEntry("encerrada", true);

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();
        assertThat(competencia(faturas(sessao, cartao), "2026-10"))
                .as("quitada não rola")
                .containsEntry("roladoCentavos", 0);
    }

    @Test
    void so_a_fechada_que_ainda_deve_recebe_pagamento_e_abre() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("janelaunica");
        Integer nubank = criarCorrente(sessao, "Nubank");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 100000, "Compra");

        Integer aberta = (Integer) faturas(sessao, cartao).get(0).get("id");

        var naAberta = post(sessao, "/faturas/" + aberta + "/pagamentos", new HashMap<>(Map.of(
                "contaPagadoraId", nubank, "valor", 100000)));
        assertThat(naAberta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(naAberta.getBody()).containsEntry("codigo", "FATURA_NAO_RECEBE_PAGAMENTO");

        var abrirAAberta = post(sessao, "/faturas/" + aberta + "/abertura", Map.of());
        assertThat(abrirAAberta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(abrirAAberta.getBody()).containsEntry("codigo", "FATURA_NAO_ABRE");

        sessao = avancarPara(sessao, LocalDate.of(2026, 10, 6));
        rotinaDiaria.executar();

        var rolada = competencia(faturas(sessao, cartao), "2026-10");
        var naRolada = post(sessao, "/faturas/" + rolada.get("id") + "/abertura", Map.of());
        assertThat(naRolada.getStatusCode())
                .as("fatura encerrada não abre — é o que impede a rolagem de rolar para si mesma")
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(naRolada.getBody()).containsEntry("codigo", "FATURA_NAO_ABRE");
    }

    @Test
    void abrir_a_ultima_fechada_devolve_a_seguinte_para_futura_sem_mexer_no_que_ela_tem() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("abriramao");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        comprar(sessao, cartao, 100000, "Setembro");

        sessao = avancarPara(sessao, LocalDate.of(2026, 9, 27));
        rotinaDiaria.executar();
        comprar(sessao, cartao, 5000, "Caiu em novembro por engano");

        Integer outubro = (Integer) competencia(faturas(sessao, cartao), "2026-10").get("id");
        var abertura = post(sessao, "/faturas/" + outubro + "/abertura", Map.of());

        assertThat(abertura.getStatusCode()).isEqualTo(HttpStatus.OK);

        var depois = faturas(sessao, cartao);
        assertThat(competencia(depois, "2026-10")).containsEntry("status", "ABERTA");
        assertThat(competencia(depois, "2026-11"))
                .as("a seguinte volta a FUTURA na hora, e o que já estava dentro dela fica")
                .containsEntry("status", "FUTURA");
        assertThat(competencia(depois, "2026-11")).containsEntry("totalCentavos", 5000);
    }

    @Test
    void fechar_a_mao_e_contingencia_e_o_diario_diz_que_foi_o_usuario() {
        RELOGIO.em(LocalDate.of(2026, 9, 10));
        Sessao sessao = novaSessao("fecharamao");
        Integer cartao = criarCartao(sessao, "UltraVioleta");
        Integer aberta = (Integer) faturas(sessao, cartao).get(0).get("id");

        var resposta = post(sessao, "/faturas/" + aberta + "/fechamento", Map.of());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(competencia(faturas(sessao, cartao), "2026-10")).containsEntry("status", "FECHADA");
        assertThat(competencia(faturas(sessao, cartao), "2026-11")).containsEntry("status", "ABERTA");

        assertThat(eventosDoDia(sessao, "2026-09-10")).anySatisfy(evento -> {
            assertThat(evento).containsEntry("tipo", "FATURA_FECHADA_PELO_USUARIO");
            assertThat(evento)
                    .as("o Diário responde por quem, e a rotina não pediu isto")
                    .containsEntry("origem", "USUARIO");
        });
    }

    private List<String> situacoesDaFatura(Sessao sessao, Integer faturaId) {
        return extrato(sessao).stream()
                .filter(l -> faturaId.equals(l.get("faturaId")))
                .map(l -> String.valueOf(l.get("situacao")))
                .toList();
    }

    private Integer criarCorrente(Sessao sessao, String nome) {
        var resposta = post(sessao, "/contas", new HashMap<>(Map.of(
                "nome", nome, "tipo", "CORRENTE", "saldoInicial", 5000000,
                "meios", List.of("PIX"))));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Map<String, Object> competencia(List<Map<String, Object>> faturas, String qual) {
        return faturas.stream()
                .filter(f -> qual.equals(f.get("competencia")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("não achou a fatura de " + qual));
    }

    private long dividaDoCartao(Sessao sessao, Integer cartao) {
        return ((Number) ((Map<?, ?>) get(sessao, "/faturas?contaId=" + cartao).getBody()
                .get("cartao")).get("dividaCentavos")).longValue();
    }

    private List<String> situacoesDosLancamentos(Sessao sessao) {
        return extrato(sessao).stream().map(l -> String.valueOf(l.get("situacao"))).toList();
    }

    private List<Map<String, Object>> eventosDoDia(Sessao sessao, String dia) {
        return itens(get(sessao, "/eventos?dia=" + dia));
    }

    private Integer criarCartao(Sessao sessao, String nome) {
        var resposta = post(sessao, "/contas", new HashMap<>(Map.of(
                "nome", nome, "tipo", "CARTAO", "diaVencimento", 5, "diasAntesFechamento", 8,
                "cartoes", List.of("Físico ****1234"))));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private void comprar(Sessao sessao, Integer cartao, int valor, String descricao) {
        Integer meio = itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(m -> cartao.equals(m.get("contaId")))
                .map(m -> (Integer) m.get("id"))
                .findFirst().orElseThrow();

        var resposta = post(sessao, "/lancamentos", new HashMap<>(Map.of(
                "meioId", meio, "sentido", "SAIDA", "valor", valor,
                "dataEvento", LocalDate.ofInstant(RELOGIO.instant(), DiaLocal.BRASILIA).toString(),
                "descricao", descricao)));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private List<Map<String, Object>> faturas(Sessao sessao, Integer cartao) {
        return itens(get(sessao, "/faturas?contaId=" + cartao));
    }

    private List<Map<String, Object>> extrato(Sessao sessao) {
        return itens(get(sessao, "/lancamentos"));
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

    private Sessao avancarPara(Sessao sessao, LocalDate dia) {
        RELOGIO.em(dia);
        return new Sessao(sessao.ambienteId(), entrar(sessao.apelido()), sessao.apelido());
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

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }
}
