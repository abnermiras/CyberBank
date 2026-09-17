package br.com.cyberbank.lancamento.api;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResumoIT {

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
    void o_resumo_do_mes_responde_o_cockpit_inteiro() {
        var sessao = novaSessao("cockpit");
        LocalDate hoje = LocalDate.now(BRASILIA);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 1000000L, List.of("PIX", "BOLETO"));
        Integer poupanca = criarConta(sessao, "Poupança", "APLICACAO", 0L, List.of());
        Integer pix = meioDa(sessao, nubank, "PIX");

        Integer mercado = criarCategoria(sessao, "Mercado", "SAIDA");
        Integer feira = criarSubcategoria(sessao, "Feira", mercado);
        Integer padaria = criarSubcategoria(sessao, "Padaria", mercado);
        Integer salario = criarCategoria(sessao, "Salário", "ENTRADA");

        Integer compra = lancar(sessao, pix, feira, "SAIDA", 30000, hoje, "Mercado do mês");
        lancar(sessao, pix, padaria, "SAIDA", 12000, hoje, "Pão de todo dia");
        lancar(sessao, pix, salario, "ENTRADA", 500000, hoje, "Salário");
        lancar(sessao, pix, null, "SAIDA", 7000, hoje, "MEDTECH 24H");
        estornar(sessao, compra, hoje);
        transferir(sessao, nubank, poupanca, 20000, hoje);

        var resumo = resumo(sessao, null);
        assertThat(resumo.getStatusCode()).isEqualTo(HttpStatus.OK);
        var corpo = resumo.getBody();

        assertThat(corpo).containsEntry("mes", YearMonth.from(hoje).toString());
        assertThat(centavos(corpo, "emCaixaCentavos"))
                .as("1.000.000 + 500.000 − 30.000 − 12.000 + 30.000 de estorno − 7.000 − 20.000")
                .isEqualTo(1461000);
        assertThat(centavos(corpo, "guardadoCentavos"))
                .as("o saldo da poupança, que não entra no fluxo de caixa")
                .isEqualTo(20000);
        assertThat(centavos(corpo, "patrimonioCentavos"))
                .as("todas as contas, sem exceção")
                .isEqualTo(1481000);
        assertThat(centavos(corpo, "entrouNoMesCentavos"))
                .as("o saldo de abertura é de sistema e não é receita da vida")
                .isEqualTo(500000);
        assertThat(centavos(corpo, "saiuNoMesCentavos"))
                .as("30.000 + 12.000 − 30.000 de estorno + 7.000 sem categoria")
                .isEqualTo(19000);
        assertThat(centavos(corpo, "guardadoNoMesCentavos"))
                .as("o aporte não é gasto, e por isso tem linha própria")
                .isEqualTo(20000);
        assertThat(((Number) corpo.get("pendencias")).longValue()).isEqualTo(1);

        List<Map<String, Object>> gasto = gastoPorCategoria(corpo);
        assertThat(gasto)
                .as("a transferência e o saldo de abertura são de sistema e ficam fora")
                .hasSize(2);
        assertThat(gasto.get(0)).containsEntry("nome", "Mercado");
        assertThat(((Number) gasto.get(0).get("totalCentavos")).longValue())
                .as("as duas subcategorias somam na raiz, e o estorno abate")
                .isEqualTo(12000);
        assertThat(((Number) gasto.get(0).get("lancamentos")).longValue()).isEqualTo(3);
        assertThat(gasto.get(1).get("categoriaId"))
                .as("o dinheiro saiu: sem categoria aparece na lista")
                .isNull();
        assertThat(((Number) gasto.get(1).get("totalCentavos")).longValue()).isEqualTo(7000);

        assertThat(gasto.stream().mapToLong(g -> ((Number) g.get("totalCentavos")).longValue()).sum())
                .as("a soma das barras é o que saiu no mês")
                .isEqualTo(centavos(corpo, "saiuNoMesCentavos"));
    }

    @Test
    void o_que_vem_por_ai_sustenta_a_aritmetica_da_sobra() {
        var sessao = novaSessao("horizonte");
        LocalDate hoje = LocalDate.now(BRASILIA);
        YearMonth proximoMes = YearMonth.from(hoje).plusMonths(1);
        LocalDate vencimento = proximoMes.atDay(10);

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("BOLETO"));
        Integer boleto = meioDa(sessao, nubank, "BOLETO");
        Integer luz = criarCategoria(sessao, "Moradia", "SAIDA");

        var lancado = lancarBoleto(sessao, boleto, luz, 40000, hoje, vencimento, "Luz");
        assertThat(lancado.getBody()).containsEntry("situacao", "PREVISTO");

        var deste = resumo(sessao, null).getBody();
        assertThat(centavos(deste, "aPagarCentavos"))
                .as("o boleto vence no mês que vem: ele não pesa neste")
                .isZero();

        var doProximo = resumo(sessao, proximoMes.toString()).getBody();
        assertThat(centavos(doProximo, "aPagarCentavos")).isEqualTo(40000);
        assertThat(centavos(doProximo, "sobraAteOFimDoMesCentavos"))
                .as("em caixa + a receber − a pagar, e a tela mostra a conta")
                .isEqualTo(centavos(doProximo, "emCaixaCentavos") - 40000);

        List<Map<String, Object>> proximos = proximos(doProximo);
        assertThat(proximos).singleElement().satisfies(linha -> {
            assertThat(linha).containsEntry("descricao", "Luz");
            assertThat(linha).containsEntry("sentido", "SAIDA");
            assertThat(linha).containsEntry("dataEfeito", vencimento.toString());
        });

        assertThat(gastoPorCategoria(doProximo))
                .as("o que não aconteceu ainda não é gasto")
                .isEmpty();
    }

    @Test
    void mes_malformado_e_recusado_em_vez_de_virar_o_mes_corrente() {
        var sessao = novaSessao("mesruim");

        var resposta = resumo(sessao, "setembro");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody()).containsEntry("codigo", "VALIDACAO");
    }

    @Test
    void ambiente_de_outro_responde_como_inexistente() {
        var dona = novaSessao("dona-do-resumo");
        var curioso = novaSessao("curioso-do-resumo");

        var resposta = troca(http().get()
                .uri("/api/v1/ambientes/" + dona.ambienteId() + "/relatorios/resumo"), curioso);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsEntry("codigo", "NAO_ENCONTRADO");
    }

    private record Sessao(Integer ambienteId, String cookie) {
    }

    private long centavos(Map<String, Object> corpo, String campo) {
        return ((Number) corpo.get(campo)).longValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> gastoPorCategoria(Map<String, Object> corpo) {
        return (List<Map<String, Object>>) corpo.get("gastoPorCategoria");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> proximos(Map<String, Object> corpo) {
        return (List<Map<String, Object>>) corpo.get("proximos");
    }

    private ResponseEntity<Map<String, Object>> resumo(Sessao sessao, String mes) {
        return troca(http().get().uri(caminho(sessao, "/relatorios/resumo"
                + (mes == null ? "" : "?mes=" + mes))), sessao);
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
            int valor, LocalDate dia, String descricao) {
        Map<String, Object> corpo = new HashMap<>(Map.of("meioId", meioId, "sentido", sentido,
                "valor", valor, "dataEvento", dia.toString(), "descricao", descricao));
        corpo.put("categoriaId", categoriaId);
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos")).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> lancarBoleto(Sessao sessao, Integer meioId,
            Integer categoriaId, int valor, LocalDate dia, LocalDate vencimento, String descricao) {
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos"))
                .body(Map.of("meioId", meioId, "categoriaId", categoriaId, "sentido", "SAIDA",
                        "valor", valor, "dataEvento", dia.toString(),
                        "dataEfeito", vencimento.toString(), "descricao", descricao)), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resposta;
    }

    private void estornar(Sessao sessao, Integer lancamentoId, LocalDate dia) {
        var resposta = troca(http().post()
                .uri(caminho(sessao, "/lancamentos/" + lancamentoId + "/estorno"))
                .body(Map.of("dataEvento", dia.toString())), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private void transferir(Sessao sessao, Integer origem, Integer destino, int valor, LocalDate dia) {
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos/transferencias"))
                .body(Map.of("contaDeOrigemId", origem, "contaDeDestinoId", destino,
                        "valor", valor, "dataEvento", dia.toString(), "descricao", "Guardando")),
                sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
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
