package br.com.cyberbank.evento.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.persistencia.BancoDeTeste;
import br.com.cyberbank.comum.rotina.RotinaDiaria;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.OrigemDeEvento;
import br.com.cyberbank.evento.dominio.TipoDeAlvo;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiarioERotinaIT {

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

    @Autowired
    private RotinaDiaria rotinaDiaria;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private DiaLocal diaLocal;

    @Autowired
    private PlatformTransactionManager transacoes;

    @Test
    void a_rotina_vira_o_previsto_vencido_em_realizado_e_registra_o_dia_em_que_fez_isso() {
        var sessao = novaSessao("rotina");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L, List.of("BOLETO"));
        Integer boleto = meioDa(sessao, nubank);

        Integer luz = (Integer) lancar(sessao, Map.of("meioId", boleto, "sentido", "SAIDA",
                "valor", 19900, "dataEvento", "2026-09-01", "dataEfeito", "2099-12-10",
                "descricao", "Conta de luz")).getBody().get("id");

        venceOnteem(sessao, luz);
        assertThat(saldo(sessao, nubank))
                .as("enquanto é PREVISTO, o dinheiro ainda está lá")
                .isEqualTo(500000);

        rotinaDiaria.executar();

        assertThat(lancamento(sessao, luz)).containsEntry("situacao", "REALIZADO");
        assertThat(saldo(sessao, nubank)).isEqualTo(500000 - 19900);

        var registrado = doDia(sessao).stream()
                .filter(e -> e.tipo() == TipoDeEvento.LANCAMENTO_REALIZADO)
                .toList();

        assertThat(registrado).singleElement().satisfies(evento -> {
            assertThat(evento.origem()).isEqualTo(OrigemDeEvento.SISTEMA);
            assertThat(evento.dia())
                    .as("o evento é datado no dia em que a rotina rodou")
                    .isEqualTo(diaLocal.hoje());
            assertThat(evento.alvo().tipo()).isEqualTo(TipoDeAlvo.LANCAMENTO);
            assertThat(evento.alvo().id()).isEqualTo(luz.longValue());
            assertThat(evento.dados()).containsEntry("descricao", "Conta de luz");
        });
    }

    @Test
    void rodar_a_rotina_de_novo_no_mesmo_dia_nao_grava_nada() {
        var sessao = novaSessao("idempotente");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("BOLETO"));
        Integer boleto = meioDa(sessao, nubank);

        Integer conta = (Integer) lancar(sessao, Map.of("meioId", boleto, "sentido", "SAIDA",
                "valor", 5000, "dataEvento", "2026-09-01", "dataEfeito", "2099-12-10",
                "descricao", "Água")).getBody().get("id");
        venceOnteem(sessao, conta);

        rotinaDiaria.executar();
        int depoisDaPrimeira = doDia(sessao).size();

        rotinaDiaria.executar();

        assertThat(doDia(sessao))
                .as("passo de ciclo que não fez nada não grava evento")
                .hasSize(depoisDaPrimeira);
    }

    @Test
    void o_evento_de_exclusao_guarda_a_linha_inteira_porque_o_alvo_deixou_de_existir() {
        var sessao = novaSessao("exclusao");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank);

        Integer tenis = (Integer) lancar(sessao, Map.of("meioId", pix, "sentido", "SAIDA",
                "valor", 30000, "dataEvento", "2026-09-10", "descricao", "Tênis"))
                .getBody().get("id");

        assertThat(troca(http().delete().uri(caminho(sessao, "/lancamentos/" + tenis)), sessao)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(doDia(sessao))
                .filteredOn(e -> e.tipo() == TipoDeEvento.LANCAMENTO_EXCLUIDO)
                .singleElement()
                .satisfies(evento -> {
                    assertThat(evento.origem()).isEqualTo(OrigemDeEvento.USUARIO);
                    assertThat(evento.dados())
                            .as("a linha sumiu do extrato; o que ela era fica aqui")
                            .containsEntry("descricao", "Tênis")
                            .containsEntry("valor", 30000)
                            .containsEntry("sentido", "SAIDA")
                            .containsEntry("dataEvento", "2026-09-10");
                });
    }

    @Test
    void o_que_a_pessoa_faz_com_conta_meio_e_categoria_tambem_entra_no_diario() {
        var sessao = novaSessao("mexidas");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("PIX"));
        Integer mercado = criarCategoria(sessao, "Mercado");

        troca(http().patch().uri(caminho(sessao, "/contas/" + nubank))
                .body(Map.of("nome", "Nu")), sessao);
        troca(http().patch().uri(caminho(sessao, "/categorias/" + mercado))
                .body(Map.of("inativa", true)), sessao);

        assertThat(doDia(sessao)).extracting(Evento::tipo)
                .contains(TipoDeEvento.CONTA_CRIADA, TipoDeEvento.MEIO_CRIADO,
                        TipoDeEvento.CATEGORIA_CRIADA, TipoDeEvento.CONTA_RENOMEADA,
                        TipoDeEvento.CATEGORIA_INATIVADA);

        assertThat(doDia(sessao))
                .filteredOn(e -> e.tipo() == TipoDeEvento.CONTA_RENOMEADA)
                .singleElement()
                .satisfies(evento -> assertThat(evento.dados())
                        .containsEntry("nomeDe", "Nubank")
                        .containsEntry("nomePara", "Nu"));
    }

    @Test
    void o_diario_de_um_ambiente_nao_aparece_no_de_outro() {
        var dona = novaSessao("dona-diario");
        criarConta(dona, "Particular", "CORRENTE", 100000L, List.of());

        var curioso = novaSessao("curioso-diario");

        assertThat(doDia(dona)).isNotEmpty();
        assertThat(doDia(curioso))
                .as("o isolamento do ADR-0002 vale para o evento como vale para o lançamento")
                .isEmpty();
    }

    @Test
    void categoria_com_lancamento_nao_se_exclui() {
        var sessao = novaSessao("categoriausada");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank);
        Integer mercado = criarCategoria(sessao, "Mercado");

        lancar(sessao, Map.of("meioId", pix, "categoriaId", mercado, "sentido", "SAIDA",
                "valor", 5000, "dataEvento", "2026-09-10", "descricao", "Feira"));

        var recusa = troca(http().delete().uri(caminho(sessao, "/categorias/" + mercado)), sessao);

        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(recusa.getBody())
                .as("com histórico, o caminho é inativar")
                .containsEntry("codigo", "CATEGORIA_COM_LANCAMENTO");
    }

    private List<Evento> doDia(Sessao sessao) {
        ContextoDaRequisicao.definirAmbiente(sessao.ambienteId().longValue());
        try {
            return new TransactionTemplate(transacoes).execute(status ->
                    eventos.listarDoDia(sessao.ambienteId().longValue(), diaLocal.hoje()));
        } finally {
            ContextoDaRequisicao.limpar();
        }
    }

    private void venceOnteem(Sessao sessao, Integer lancamentoId) {
        var resposta = troca(http().patch().uri(caminho(sessao, "/lancamentos/" + lancamentoId))
                .body(Map.of("dataEfeito", "2026-09-02", "situacao", "PREVISTO")), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private record Sessao(Integer ambienteId, String cookie) {
    }

    private Integer criarConta(Sessao sessao, String nome, String tipo, Long saldoInicial,
            List<String> meios) {
        Map<String, Object> corpo = new HashMap<>(Map.of("nome", nome, "tipo", tipo,
                "meios", meios));
        corpo.put("saldoInicial", saldoInicial);

        var resposta = troca(http().post().uri(caminho(sessao, "/contas")).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer criarCategoria(Sessao sessao, String nome) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "sentido", "SAIDA", "cor", "OCRE")), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer meioDa(Sessao sessao, Integer contaId) {
        return (Integer) itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(m -> contaId.equals(m.get("contaId")))
                .findFirst().orElseThrow().get("id");
    }

    private ResponseEntity<Map<String, Object>> lancar(Sessao sessao, Map<String, Object> corpo) {
        return troca(http().post().uri(caminho(sessao, "/lancamentos"))
                .body(new HashMap<>(corpo)), sessao);
    }

    private Map<String, Object> lancamento(Sessao sessao, Integer id) {
        return itens(get(sessao, "/lancamentos")).stream()
                .filter(l -> id.equals(l.get("id")))
                .findFirst().orElseThrow();
    }

    private long saldo(Sessao sessao, Integer contaId) {
        return ((Number) itens(get(sessao, "/contas")).stream()
                .filter(c -> contaId.equals(c.get("id")))
                .findFirst().orElseThrow().get("saldoRealizadoCentavos")).longValue();
    }

    private ResponseEntity<Map<String, Object>> get(Sessao sessao, String sufixo) {
        return troca(http().get().uri(caminho(sessao, sufixo)), sessao);
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

        var ambientes = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri("/api/v1/ambientes")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(Map.class);
        return new Sessao((Integer) itens(ambientes).get(0).get("id"), cookie);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> troca(RestClient.RequestHeadersSpec<?> spec,
            Sessao sessao) {
        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) spec
                .header(HttpHeaders.COOKIE, sessao.cookie())
                .retrieve().toEntity(Map.class);
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
