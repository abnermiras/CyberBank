package br.com.cyberbank.fatura.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
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
class CartaoEFaturaIT {

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

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    @LocalServerPort
    private int porta;

    @Test
    void a_conta_cartao_nasce_com_a_fatura_aberta_do_ciclo_corrente_e_ela_nasce_vazia() {
        var sessao = novaSessao("cartaonasce");
        Integer cartao = criarCartao(sessao, "UltraVioleta", 5, 8, 1500000L);

        var faturas = faturas(sessao, cartao);

        assertThat(faturas).as("exatamente uma fatura, e ela é a ABERTA").singleElement()
                .satisfies(fatura -> {
                    assertThat(fatura).containsEntry("status", "ABERTA");
                    assertThat(fatura).containsEntry("totalCentavos", 0);
                    assertThat(fatura).containsEntry("aPagarCentavos", 0);
                    assertThat(fatura).containsEntry("competencia", competenciaCorrente(5, 8));
                });

        assertThat(extrato(sessao))
                .as("a CARTAO nasce zerada: nenhum lançamento de abertura")
                .isEmpty();
    }

    @Test
    void cartao_de_credito_nao_tem_saldo_de_abertura_e_e_a_unica_excecao() {
        var sessao = novaSessao("cartaosemabertura");

        Map<String, Object> corpo = new HashMap<>(Map.of("nome", "UltraVioleta",
                "tipo", "CARTAO", "diaVencimento", 5, "diasAntesFechamento", 8));
        corpo.put("saldoInicial", 50000L);

        var resposta = post(sessao, "/contas", corpo);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
        assertThat(resposta.getBody()).containsEntry("codigo", "CARTAO_SEM_SALDO_INICIAL");
    }

    @Test
    void o_cartao_sem_ciclo_nao_nasce_porque_a_fatura_dele_nao_teria_datas() {
        var sessao = novaSessao("cartaosemciclo");

        var resposta = post(sessao, "/contas", Map.of("nome", "Sem ciclo", "tipo", "CARTAO"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
    }

    @Test
    void comprar_no_credito_cai_na_fatura_aberta_provisionado_e_a_divida_sobe_na_hora() {
        var sessao = novaSessao("compranocredito");
        Integer cartao = criarCartao(sessao, "UltraVioleta", 5, 8, 1500000L);
        Integer fisico = cartaoDe(sessao, cartao);
        Integer categoria = criarCategoria(sessao, "Mercado", "SAIDA");

        Integer aberta = (Integer) faturas(sessao, cartao).get(0).get("id");

        var compra = post(sessao, "/lancamentos", campos(Map.of(
                "meioId", fisico, "categoriaId", categoria, "sentido", "SAIDA",
                "valor", 30000, "dataEvento", hoje().toString(), "descricao", "Mercado")));

        assertThat(compra.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(compra.getBody())
                .as("comprou, deve: PROVISIONADO desde a compra, e as duas datas são a mesma")
                .containsEntry("situacao", "PROVISIONADO")
                .containsEntry("faturaId", aberta)
                .containsEntry("dataEfeito", hoje().toString());

        assertThat(faturas(sessao, cartao)).singleElement().satisfies(fatura -> {
            assertThat(fatura).containsEntry("totalCentavos", 30000);
            assertThat(fatura).containsEntry("aPagarCentavos", 30000);
            assertThat(fatura).containsEntry("encerrada", false);
        });
    }

    @Test
    void a_divida_e_o_saldo_da_conta_e_o_limite_disponivel_e_o_limite_menos_ela() {
        var sessao = novaSessao("dividaelimite");
        Integer cartao = criarCartao(sessao, "UltraVioleta", 5, 8, 1500000L);
        Integer fisico = cartaoDe(sessao, cartao);
        Integer categoria = criarCategoria(sessao, "Mercado", "SAIDA");

        comprar(sessao, fisico, categoria, 356080);

        var cabecalho = (Map<String, Object>) get(sessao, "/faturas?contaId=" + cartao)
                .getBody().get("cartao");

        assertThat(cabecalho)
                .as("a dívida é o saldo da conta CARTAO, sem cálculo próprio")
                .containsEntry("dividaCentavos", 356080)
                .containsEntry("limiteDisponivelCentavos", 1143920)
                .containsEntry("limitePodeEstarDesatualizado", false);

        var contas = get(sessao, "/contas");
        assertThat(contas.getBody())
                .as("o cartão não é caixa, e o patrimônio soma a dívida como qualquer conta")
                .containsEntry("emCaixaCentavos", 0)
                .containsEntry("patrimonioCentavos", -356080);
    }

    @Test
    void o_limite_avisa_quando_a_divida_passa_dele_em_vez_de_mostrar_numero_que_nao_descreve_nada() {
        var sessao = novaSessao("limitevelho");
        Integer cartao = criarCartao(sessao, "UltraVioleta", 5, 8, 100000L);
        Integer fisico = cartaoDe(sessao, cartao);
        Integer categoria = criarCategoria(sessao, "Mercado", "SAIDA");

        comprar(sessao, fisico, categoria, 150000);

        var cabecalho = (Map<String, Object>) get(sessao, "/faturas?contaId=" + cartao)
                .getBody().get("cartao");

        assertThat(cabecalho)
                .as("o limite nunca trava um lançamento: ele orienta")
                .containsEntry("limiteDisponivelCentavos", -50000)
                .containsEntry("limitePodeEstarDesatualizado", true);
    }

    @Test
    void o_estorno_de_compra_no_credito_cai_na_aberta_e_nasce_provisionado() {
        var sessao = novaSessao("estornocredito");
        Integer cartao = criarCartao(sessao, "UltraVioleta", 5, 8, 1500000L);
        Integer fisico = cartaoDe(sessao, cartao);
        Integer categoria = criarCategoria(sessao, "Mercado", "SAIDA");

        Integer compra = comprar(sessao, fisico, categoria, 30000);
        Integer aberta = (Integer) faturas(sessao, cartao).get(0).get("id");

        var estorno = post(sessao, "/lancamentos/" + compra + "/estorno", Map.of());

        assertThat(estorno.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(estorno.getBody())
                .containsEntry("sentido", "ENTRADA")
                .containsEntry("situacao", "PROVISIONADO")
                .containsEntry("faturaId", aberta);

        assertThat(faturas(sessao, cartao)).singleElement()
                .as("compra e estorno se compensam na própria fatura")
                .satisfies(fatura -> assertThat(fatura).containsEntry("totalCentavos", 0));
    }

    @Test
    void a_fatura_e_do_contrato_de_cartao_e_de_nenhuma_outra_conta() {
        var sessao = novaSessao("faturasocartao");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", List.of("PIX"));

        var resposta = get(sessao, "/faturas?contaId=" + nubank);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("codigo", "CONTA_NAO_E_CARTAO");
    }

    @Test
    void o_cartao_entra_no_catalogo_de_tipos_agora_que_a_fatura_existe() {
        var sessao = novaSessao("catalogodetipos");

        var tipos = itens(get(sessao, "/contas"), "tiposDisponiveis");

        assertThat(tipos).anySatisfy(tipo -> {
            assertThat(tipo).containsEntry("tipo", "CARTAO");
            assertThat(tipo).containsEntry("aceitaSaldoInicial", false);
            assertThat(tipo).containsEntry("entraEmCaixa", false);
            assertThat(tipo).containsEntry("meios", List.of("CREDITO"));
        });
    }

    private LocalDate hoje() {
        return LocalDate.now(BRASILIA);
    }

    private String competenciaCorrente(int diaVencimento, int diasAntesFechamento) {
        YearMonth competencia = YearMonth.from(hoje());
        while (competencia.atDay(Math.min(diaVencimento, competencia.lengthOfMonth()))
                .minusDays(diasAntesFechamento).isBefore(hoje())) {
            competencia = competencia.plusMonths(1);
        }
        return competencia.toString();
    }

    private Integer criarCartao(Sessao sessao, String nome, int diaVencimento,
            int diasAntesFechamento, Long limite) {

        var resposta = post(sessao, "/contas", campos(Map.of(
                "nome", nome, "tipo", "CARTAO",
                "diaVencimento", diaVencimento, "diasAntesFechamento", diasAntesFechamento,
                "limite", limite, "cartoes", List.of("Físico ****1234"))));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer criarConta(Sessao sessao, String nome, String tipo, List<String> meios) {
        var resposta = post(sessao, "/contas",
                campos(Map.of("nome", nome, "tipo", tipo, "meios", meios)));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer cartaoDe(Sessao sessao, Integer contaId) {
        return itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(meio -> contaId.equals(meio.get("contaId")))
                .map(meio -> (Integer) meio.get("id"))
                .findFirst()
                .orElseThrow();
    }

    private Integer criarCategoria(Sessao sessao, String nome, String sentido) {
        var resposta = post(sessao, "/categorias",
                Map.of("nome", nome, "sentido", sentido, "cor", "OCRE"));
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer comprar(Sessao sessao, Integer meioId, Integer categoriaId, int valor) {
        var resposta = post(sessao, "/lancamentos", campos(Map.of(
                "meioId", meioId, "categoriaId", categoriaId, "sentido", "SAIDA",
                "valor", valor, "dataEvento", hoje().toString(), "descricao", "Mercado")));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private List<Map<String, Object>> faturas(Sessao sessao, Integer contaId) {
        return itens(get(sessao, "/faturas?contaId=" + contaId));
    }

    private List<Map<String, Object>> extrato(Sessao sessao) {
        return itens(get(sessao, "/lancamentos"));
    }

    private static Map<String, Object> campos(Map<String, Object> mapa) {
        return new HashMap<>(mapa);
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

    private record Sessao(Integer ambienteId, String cookie) {
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
    private static List<Map<String, Object>> itens(ResponseEntity<Map<String, Object>> resposta,
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
