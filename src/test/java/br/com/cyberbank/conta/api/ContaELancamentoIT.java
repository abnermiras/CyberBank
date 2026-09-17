package br.com.cyberbank.conta.api;

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
class ContaELancamentoIT {

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
    void o_saldo_inicial_vira_um_lancamento_de_abertura_e_o_saldo_e_a_soma_dele() {
        var sessao = novaSessao("abertura");

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 1123600L);

        assertThat(conta(sessao, nubank))
                .as("saldo é sempre soma dos lançamentos, nunca campo")
                .containsEntry("saldoRealizadoCentavos", 1123600);

        assertThat(extrato(sessao)).singleElement().satisfies(abertura -> {
            assertThat(abertura).containsEntry("descricao", "Saldo de abertura");
            assertThat(abertura).containsEntry("situacao", "REALIZADO");
            assertThat(abertura).containsEntry("sentido", "ENTRADA");
            assertThat(abertura).containsEntry("doCiclo", true);
            assertThat(abertura)
                    .as("campo que não se aplica não vem: ninguém pagou nada")
                    .doesNotContainKey("meioId");
        });
    }

    @Test
    void conta_sem_saldo_inicial_nasce_zerada_e_sem_abertura() {
        var sessao = novaSessao("zerada");
        Integer conta = criarConta(sessao, "Carteira", "CARTEIRA", null);

        assertThat(conta(sessao, conta)).containsEntry("saldoRealizadoCentavos", 0);
        assertThat(extrato(sessao)).isEmpty();
    }

    @Test
    void o_vale_refeicao_esta_no_patrimonio_e_nao_esta_em_caixa() {
        var sessao = novaSessao("vale");
        criarConta(sessao, "Nubank", "CORRENTE", 1123600L);
        criarConta(sessao, "Vale-refeição", "BENEFICIO", 88000L);
        criarConta(sessao, "Poupança", "APLICACAO", 1342400L);

        var lista = contas(sessao).getBody();

        assertThat(lista)
                .as("em caixa é só o que paga qualquer coisa — o vale só compra comida")
                .containsEntry("emCaixaCentavos", 1123600);
        assertThat(lista)
                .as("patrimônio é o saldo realizado de todas as contas, sem exceção")
                .containsEntry("patrimonioCentavos", 1123600 + 88000 + 1342400);
    }

    @Test
    void lancar_um_gasto_derruba_o_saldo_e_a_transferencia_nao_mexe_no_patrimonio() {
        var sessao = novaSessao("gasto");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 1000000L);
        Integer poupanca = criarConta(sessao, "Poupança", "APLICACAO", 0L);
        Integer pix = criarMeio(sessao, "PIX", nubank);
        Integer mercado = criarCategoria(sessao, "Mercado", "SAIDA");

        lancar(sessao, campos(Map.of("meioId", pix, "categoriaId", mercado, "sentido", "SAIDA",
                "valor", 30000, "dataEvento", "2026-09-10", "descricao", "Feira")));

        assertThat(conta(sessao, nubank)).containsEntry("saldoRealizadoCentavos", 970000);
        long patrimonioAntes = patrimonio(sessao);

        transferir(sessao, nubank, poupanca, 200000, "Aporte de setembro");

        assertThat(conta(sessao, nubank)).containsEntry("saldoRealizadoCentavos", 770000);
        assertThat(conta(sessao, poupanca)).containsEntry("saldoRealizadoCentavos", 200000);
        assertThat(patrimonio(sessao))
                .as("transferência é dinheiro mudando de bolso: o patrimônio não muda")
                .isEqualTo(patrimonioAntes);
    }

    @Test
    void o_boleto_previsto_nao_entra_no_saldo_realizado_e_carrega_as_duas_datas() {
        var sessao = novaSessao("boleto");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L);
        Integer boleto = criarMeio(sessao, "BOLETO", nubank);
        Integer luz = criarCategoria(sessao, "Luz", "SAIDA");

        var lancado = lancar(sessao, campos(Map.of("meioId", boleto, "categoriaId", luz,
                "sentido", "SAIDA", "valor", 19900, "dataEvento", "2026-09-01",
                "dataEfeito", "2099-12-10", "descricao", "Conta de luz")));

        assertThat(lancado.getBody()).containsEntry("situacao", "PREVISTO");
        assertThat(lancado.getBody()).containsEntry("dataEvento", "2026-09-01");
        assertThat(lancado.getBody()).containsEntry("dataEfeito", "2099-12-10");
        assertThat(conta(sessao, nubank))
                .as("o teste é situacao != PREVISTO, e o previsto fica de fora")
                .containsEntry("saldoRealizadoCentavos", 500000);
    }

    @Test
    void meio_a_vista_recusa_data_de_efeito_diferente_em_vez_de_corrigir() {
        var sessao = novaSessao("duasdatas");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L);
        Integer pix = criarMeio(sessao, "PIX", nubank);

        var resposta = lancar(sessao, campos(Map.of("meioId", pix, "sentido", "SAIDA",
                "valor", 1000, "dataEvento", "2026-09-01", "dataEfeito", "2026-09-05",
                "descricao", "Pix")));

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    void o_lancamento_de_abertura_e_do_ciclo_e_o_usuario_nao_o_exclui() {
        var sessao = novaSessao("ciclo");
        criarConta(sessao, "Nubank", "CORRENTE", 100000L);
        Integer abertura = (Integer) extrato(sessao).get(0).get("id");

        var recusa = excluirLancamento(sessao, abertura);

        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(recusa.getBody()).containsEntry("codigo", "LANCAMENTO_DO_CICLO");
    }

    @Test
    void excluir_um_lado_da_transferencia_exclui_o_par_inteiro() {
        var sessao = novaSessao("par");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L);
        Integer carteira = criarConta(sessao, "Carteira", "CARTEIRA", 0L);

        transferir(sessao, nubank, carteira, 10000, "Saque");
        Integer umLado = (Integer) extrato(sessao).stream()
                .filter(l -> l.get("transferenciaId") != null)
                .findFirst().orElseThrow().get("id");

        assertThat(excluirLancamento(sessao, umLado).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(extrato(sessao))
                .as("não existe metade de transferência")
                .noneMatch(l -> l.get("transferenciaId") != null);
        assertThat(conta(sessao, nubank)).containsEntry("saldoRealizadoCentavos", 500000);
        assertThat(conta(sessao, carteira)).containsEntry("saldoRealizadoCentavos", 0);
    }

    @Test
    void o_estorno_abate_e_nao_apaga_o_original() {
        var sessao = novaSessao("estorno");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L);
        Integer pix = criarMeio(sessao, "PIX", nubank);
        Integer compras = criarCategoria(sessao, "Compras", "SAIDA");

        Integer compra = (Integer) lancar(sessao, campos(Map.of("meioId", pix,
                "categoriaId", compras, "sentido", "SAIDA", "valor", 30000,
                "dataEvento", "2026-09-10", "descricao", "Tênis"))).getBody().get("id");

        var estorno = troca(http().post()
                .uri(caminho(sessao, "/lancamentos/" + compra + "/estorno"))
                .body(campos(Map.of("dataEvento", "2026-09-14"))), sessao);

        assertThat(estorno.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(estorno.getBody()).containsEntry("sentido", "ENTRADA");
        assertThat(estorno.getBody()).containsEntry("estornoDeId", compra);
        assertThat(estorno.getBody())
                .as("o estorno herda a categoria: o relatório de gasto é líquido")
                .containsEntry("categoriaId", compras);

        assertThat(conta(sessao, nubank)).containsEntry("saldoRealizadoCentavos", 100000);
        assertThat(extrato(sessao)).as("a compra continua no extrato, como no banco").hasSize(3);

        var exclusao = excluirLancamento(sessao, compra);
        assertThat(exclusao.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exclusao.getBody()).containsEntry("codigo", "LANCAMENTO_COM_ESTORNO");
    }

    @Test
    void inativar_a_conta_descarta_os_previstos_dela_e_barra_lancamento_novo() {
        var sessao = novaSessao("inativa");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L);
        Integer boleto = criarMeio(sessao, "BOLETO", nubank);

        lancar(sessao, campos(Map.of("meioId", boleto, "sentido", "SAIDA", "valor", 19900,
                "dataEvento", "2026-09-01", "dataEfeito", "2099-12-10", "descricao", "Luz")));
        assertThat(extrato(sessao)).hasSize(2);

        patchConta(sessao, nubank, campos(Map.of("inativa", true)));

        assertThat(extrato(sessao))
                .as("os PREVISTO dela não vão acontecer: a conta saiu da sua vida")
                .hasSize(1);
        assertThat(conta(sessao, nubank, true))
                .as("histórico e saldo continuam existindo")
                .containsEntry("saldoRealizadoCentavos", 500000);

        var recusa = lancar(sessao, campos(Map.of("meioId", boleto, "sentido", "SAIDA",
                "valor", 1000, "dataEvento", "2026-09-02", "dataEfeito", "2026-09-02",
                "descricao", "Outra")));
        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(recusa.getBody()).containsEntry("codigo", "CONTA_INATIVA");
    }

    @Test
    void conta_com_lancamento_nao_se_exclui_e_sem_lancamento_se_exclui() {
        var sessao = novaSessao("excluirconta");
        Integer comHistorico = criarConta(sessao, "Nubank", "CORRENTE", 100000L);
        Integer vazia = criarConta(sessao, "Vazia", "CARTEIRA", null);

        var recusa = excluirConta(sessao, comHistorico);
        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(recusa.getBody()).containsEntry("codigo", "CONTA_COM_LANCAMENTO");

        assertThat(excluirConta(sessao, vazia).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void a_conta_nasce_com_os_meios_escolhidos_no_mesmo_ato() {
        var sessao = novaSessao("meiosjuntos");

        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 500000L,
                List.of("PIX", "DEBITO", "BOLETO", "DESCONTO_EM_FOLHA"));

        assertThat(itens(get(sessao, "/meios-de-pagamento")))
                .as("não há cadastro de meio à parte")
                .hasSize(4)
                .allSatisfy(meio -> {
                    assertThat(meio).containsEntry("contaId", nubank);
                    assertThat(meio)
                            .as("só o cartão de crédito tem nome")
                            .doesNotContainKey("nome");
                });
    }

    @Test
    void a_conta_diz_quais_meios_cabem_nela_e_a_carteira_so_aceita_dinheiro() {
        var sessao = novaSessao("disponiveis");
        criarConta(sessao, "Nubank", "CORRENTE", null);
        criarConta(sessao, "Cofre", "CARTEIRA", null);
        criarConta(sessao, "Poupança", "APLICACAO", null);

        var porNome = itens(get(sessao, "/contas")).stream()
                .collect(java.util.stream.Collectors.toMap(c -> c.get("nome"),
                        c -> c.get("tiposDeMeioDisponiveis")));

        assertThat(porNome.get("Cofre"))
                .as("dinheiro é exclusivo porque nenhum outro tipo de conta o aceita")
                .isEqualTo(List.of("DINHEIRO"));
        assertThat(porNome.get("Poupança"))
                .as("não se paga com uma aplicação: resgata-se antes")
                .isEqualTo(List.of());
        assertThat(porNome.get("Nubank"))
                .isEqualTo(List.of("DEBITO", "PIX", "TED", "DESCONTO_EM_FOLHA", "BOLETO"));
    }

    @Test
    void a_conta_tem_no_maximo_um_meio_de_cada_tipo() {
        var sessao = novaSessao("duplicado");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", null, List.of("PIX"));

        var repetido = criarMeioCru(sessao, "PIX", nubank);

        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(repetido.getBody()).containsEntry("codigo", "MEIO_DUPLICADO_NA_CONTA");
    }

    @Test
    void nome_em_meio_que_nao_e_cartao_e_recusado_em_vez_de_guardado_calado() {
        var sessao = novaSessao("nomeinutil");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", null);

        var comNome = troca(http().post().uri(caminho(sessao, "/meios-de-pagamento"))
                .body(Map.of("nome", "Pix da Nubank", "tipo", "PIX", "contaId", nubank)), sessao);

        assertThat(comNome.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    void meio_de_tipo_incompativel_com_a_conta_e_recusado() {
        var sessao = novaSessao("incompativel");
        Integer carteira = criarConta(sessao, "Carteira", "CARTEIRA", null);

        var resposta = criarMeioCru(sessao, "PIX", carteira);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("codigo", "MEIO_INCOMPATIVEL_COM_CONTA");
    }

    @Test
    void cartao_de_credito_ainda_nao_se_cadastra() {
        var sessao = novaSessao("cartao");

        var resposta = criarContaCru(sessao, campos(Map.of("nome", "UltraVioleta",
                "tipo", "CARTAO")));

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    void conta_de_outro_ambiente_responde_igual_a_inexistente() {
        var dona = novaSessao("dona-conta");
        Integer daDona = criarConta(dona, "Particular", "CORRENTE", 100000L);

        var curioso = novaSessao("curioso-conta");

        assertThat(contas(curioso).getBody().get("itens").toString())
                .as("o isolamento do ADR-0002 vale para conta como vale para categoria")
                .isEqualTo("[]");
        assertThat(extrato(curioso)).isEmpty();
        assertThat(patchConta(curioso, daDona, campos(Map.of("nome", "Invadida")))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(excluirConta(curioso, daDona).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void o_extrato_pagina_por_cursor_e_o_proximo_some_quando_acaba() {
        var sessao = novaSessao("cursor");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 1000000L);
        Integer pix = criarMeio(sessao, "PIX", nubank);

        for (int dia = 1; dia <= 5; dia++) {
            lancar(sessao, campos(Map.of("meioId", pix, "sentido", "SAIDA", "valor", 1000,
                    "dataEvento", "2026-09-0" + dia, "descricao", "Gasto " + dia)));
        }

        var primeira = get(sessao, "/lancamentos?limite=4");
        assertThat(itens(primeira)).hasSize(4);
        assertThat(primeira.getBody()).containsKey("proximo");

        var segunda = get(sessao, "/lancamentos?limite=4&apos=" + primeira.getBody().get("proximo"));
        assertThat(itens(segunda)).hasSize(2);
        assertThat(segunda.getBody())
                .as("proximo ausente significa acabou")
                .doesNotContainKey("proximo");
    }

    @Test
    void categoria_do_outro_sentido_nao_recebe_lancamento() {
        var sessao = novaSessao("sentido");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank);
        Integer salario = criarCategoria(sessao, "Salário", "ENTRADA");

        var trocado = lancar(sessao, campos(Map.of("meioId", pix, "categoriaId", salario,
                "sentido", "SAIDA", "valor", 30000, "dataEvento", "2026-09-10",
                "descricao", "Mercado")));

        assertThat(trocado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(trocado.getBody())
                .as("sem isso nada impede categorizar o salário como mercado")
                .containsEntry("codigo", "CATEGORIA_DE_OUTRO_SENTIDO");
    }

    @Test
    void a_raiz_com_filha_ativa_deixa_de_receber_lancamento() {
        var sessao = novaSessao("raizcheia");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L, List.of("PIX"));
        Integer pix = meioDa(sessao, nubank);
        Integer transporte = criarCategoria(sessao, "Transporte", "SAIDA");

        assertThat(lancar(sessao, campos(Map.of("meioId", pix, "categoriaId", transporte,
                "sentido", "SAIDA", "valor", 5000, "dataEvento", "2026-09-10",
                "descricao", "Uber"))).getStatusCode())
                .as("raiz sem filha nasce escolhível")
                .isEqualTo(HttpStatus.CREATED);

        criarSubcategoria(sessao, "Gasolina", transporte);

        var naRaiz = lancar(sessao, campos(Map.of("meioId", pix, "categoriaId", transporte,
                "sentido", "SAIDA", "valor", 5000, "dataEvento", "2026-09-11",
                "descricao", "Uber de novo")));

        assertThat(naRaiz.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(naRaiz.getBody())
                .as("existe um destino mais específico agora")
                .containsEntry("codigo", "CATEGORIA_NAO_ESCOLHIVEL");
    }

    @Test
    void a_fila_de_pendencias_e_exatamente_categoria_nula() {
        var sessao = novaSessao("pendencia");
        Integer nubank = criarConta(sessao, "Nubank", "CORRENTE", 100000L);
        Integer pix = criarMeio(sessao, "PIX", nubank);
        Integer mercado = criarCategoria(sessao, "Mercado", "SAIDA");

        lancar(sessao, campos(Map.of("meioId", pix, "sentido", "SAIDA", "valor", 5000,
                "dataEvento", "2026-09-10", "descricao", "MEDTECH 24H")));
        lancar(sessao, campos(Map.of("meioId", pix, "categoriaId", mercado, "sentido", "SAIDA",
                "valor", 5000, "dataEvento", "2026-09-11", "descricao", "Feira")));

        assertThat(itens(get(sessao, "/lancamentos?pendentes=true")))
                .as("a abertura nasce com categoria de sistema e nunca esteve na fila")
                .singleElement()
                .satisfies(l -> assertThat(l).containsEntry("descricao", "MEDTECH 24H"));
    }

    private record Sessao(Integer ambienteId, String cookie) {
    }

    private static Map<String, Object> campos(Map<String, Object> valores) {
        return new HashMap<>(valores);
    }

    private Integer criarConta(Sessao sessao, String nome, String tipo, Long saldoInicial) {
        return criarConta(sessao, nome, tipo, saldoInicial, List.of());
    }

    private Integer criarConta(Sessao sessao, String nome, String tipo, Long saldoInicial,
            List<String> meios) {
        Map<String, Object> corpo = campos(Map.of("nome", nome, "tipo", tipo, "meios", meios));
        corpo.put("saldoInicial", saldoInicial);

        var resposta = criarContaCru(sessao, corpo);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> criarContaCru(Sessao sessao, Map<String, Object> corpo) {
        return troca(http().post().uri(caminho(sessao, "/contas")).body(corpo), sessao);
    }

    private Integer criarMeio(Sessao sessao, String tipo, Integer contaId) {
        var resposta = criarMeioCru(sessao, tipo, contaId);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> criarMeioCru(Sessao sessao, String tipo,
            Integer contaId) {
        return troca(http().post().uri(caminho(sessao, "/meios-de-pagamento"))
                .body(Map.of("tipo", tipo, "contaId", contaId)), sessao);
    }

    private Integer meioDa(Sessao sessao, Integer contaId) {
        return (Integer) itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(m -> contaId.equals(m.get("contaId")))
                .findFirst().orElseThrow().get("id");
    }

    private Integer criarSubcategoria(Sessao sessao, String nome, Integer paiId) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "paiId", paiId)), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer criarCategoria(Sessao sessao, String nome, String sentido) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "sentido", sentido, "cor", "OCRE")), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> lancar(Sessao sessao, Map<String, Object> corpo) {
        return troca(http().post().uri(caminho(sessao, "/lancamentos")).body(corpo), sessao);
    }

    private void transferir(Sessao sessao, Integer origem, Integer destino, int valor,
            String descricao) {
        var resposta = troca(http().post().uri(caminho(sessao, "/lancamentos/transferencias"))
                .body(Map.of("contaDeOrigemId", origem, "contaDeDestinoId", destino,
                        "valor", valor, "dataEvento", "2026-09-12", "descricao", descricao)),
                sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private ResponseEntity<Map<String, Object>> patchConta(Sessao sessao, Integer contaId,
            Map<String, Object> corpo) {
        return troca(http().patch().uri(caminho(sessao, "/contas/" + contaId)).body(corpo), sessao);
    }

    private ResponseEntity<Map<String, Object>> excluirConta(Sessao sessao, Integer contaId) {
        return troca(http().delete().uri(caminho(sessao, "/contas/" + contaId)), sessao);
    }

    private ResponseEntity<Map<String, Object>> excluirLancamento(Sessao sessao, Integer id) {
        return troca(http().delete().uri(caminho(sessao, "/lancamentos/" + id)), sessao);
    }

    private ResponseEntity<Map<String, Object>> contas(Sessao sessao) {
        return get(sessao, "/contas");
    }

    private Map<String, Object> conta(Sessao sessao, Integer contaId) {
        return conta(sessao, contaId, false);
    }

    private Map<String, Object> conta(Sessao sessao, Integer contaId, boolean inativas) {
        return itens(get(sessao, "/contas?inativas=" + inativas)).stream()
                .filter(c -> contaId.equals(c.get("id")))
                .findFirst().orElseThrow();
    }

    private long patrimonio(Sessao sessao) {
        return ((Number) contas(sessao).getBody().get("patrimonioCentavos")).longValue();
    }

    private List<Map<String, Object>> extrato(Sessao sessao) {
        return itens(get(sessao, "/lancamentos"));
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
