package br.com.cyberbank.compartilhamento.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
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
class CompartilhamentoDeContaIT {

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

    private record Sessao(Integer usuarioId, Integer ambienteId, String cookie) {
    }

    private record Par(Sessao a, Sessao b) {
    }

    @Test
    void a_conta_emprestada_aparece_no_destino_marcada_e_com_os_meios_dela() {
        Par par = doisAmbientes("marcada");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        Integer pix = meioDa(par.a(), nubank);

        assertThat(compartilhar(par.a(), nubank, par.b().ambienteId()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        var noDestino = conta(par.b(), nubank);
        assertThat(noDestino).containsEntry("nome", "Nubank");
        assertThat(mapa(noDestino.get("compartilhadaDe")))
                .containsEntry("ambienteId", par.a().ambienteId())
                .containsEntry("nome", "Ambiente Pessoal")
                .containsEntry("emprestadaPorId", par.a().usuarioId());

        assertThat(conta(par.a(), nubank))
                .as("na origem a conta é dela mesma, sem marca")
                .doesNotContainKey("compartilhadaDe");

        assertThat(itens(get(par.b(), "/meios-de-pagamento")))
                .extracting(meio -> meio.get("id"))
                .as("os meios da conta vão junto com ela")
                .contains(pix);
    }

    @Test
    void o_extrato_da_conta_mostra_os_dois_ambientes_e_o_extrato_geral_nao_mistura() {
        Par par = doisAmbientes("extrato");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        Integer pixDoA = meioDa(par.a(), nubank);
        compartilhar(par.a(), nubank, par.b().ambienteId());

        Integer transporte = criarCategoria(par.a(), "Transporte");
        Integer trip = criarCategoria(par.b(), "Trip");

        lancar(par.a(), pixDoA, transporte, 1000, "Estacionamento");
        lancar(par.b(), pixDoA, trip, 2000, "Compra de moeda");

        for (Sessao olhando : List.of(par.a(), par.b())) {
            var extratoDaConta = itens(get(olhando, "/lancamentos?contaId=" + nubank));

            assertThat(extratoDaConta)
                    .extracting(l -> l.get("descricao"))
                    .as("o extrato da conta é o dela inteira, de qualquer ambiente")
                    .containsExactlyInAnyOrder("Saldo de abertura", "Estacionamento",
                            "Compra de moeda");

            assertThat(conta(olhando, nubank))
                    .as("conta conjunta tem um saldo só, igual para os dois")
                    .containsEntry("saldoRealizadoCentavos", 97000);
        }

        var doB = linha(itens(get(par.a(), "/lancamentos?contaId=" + nubank)),
                "Compra de moeda");
        assertThat(doB).containsEntry("deOutroAmbiente", true);
        assertThat(doB).containsEntry("ambienteNome", "Viagem");
        assertThat(doB.get("categoriaId"))
                .as("a categoria do outro ambiente é mascarada, nunca vaza o id")
                .isNull();

        var doA = linha(itens(get(par.a(), "/lancamentos?contaId=" + nubank)),
                "Estacionamento");
        assertThat(doA).containsEntry("deOutroAmbiente", false);
        assertThat(doA).containsEntry("categoriaId", transporte);

        assertThat(itens(get(par.a(), "/lancamentos")))
                .extracting(l -> l.get("descricao"))
                .as("o extrato geral de cada ambiente é só o que ele lançou")
                .containsExactlyInAnyOrder("Saldo de abertura", "Estacionamento");
        assertThat(itens(get(par.b(), "/lancamentos")))
                .extracting(l -> l.get("descricao"))
                .containsExactly("Compra de moeda");
    }

    @Test
    void a_conta_emprestada_entra_no_patrimonio_dos_dois_ambientes() {
        Par par = doisAmbientes("patrimonio");
        Integer nubank = criarConta(par.a(), "Nubank", 500000L);

        assertThat(patrimonio(par.b())).isZero();

        compartilhar(par.a(), nubank, par.b().ambienteId());

        assertThat(patrimonio(par.a())).isEqualTo(500000);
        assertThat(patrimonio(par.b())).isEqualTo(500000);
    }

    @Test
    void o_destino_ve_o_lancamento_da_origem_em_modo_leitura_e_nao_o_altera_nem_exclui() {
        Par par = doisAmbientes("leitura");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        Integer pix = meioDa(par.a(), nubank);
        compartilhar(par.a(), nubank, par.b().ambienteId());
        Integer transporte = criarCategoria(par.a(), "Transporte");
        Integer doA = (Integer) lancar(par.a(), pix, transporte, 1000, "Estacionamento")
                .getBody().get("id");

        var detalhe = get(par.b(), "/lancamentos/" + doA);
        assertThat(detalhe.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detalhe.getBody()).containsEntry("deOutroAmbiente", true);
        assertThat(detalhe.getBody()).containsEntry("ambienteNome", "Ambiente Pessoal");
        assertThat(detalhe.getBody()).doesNotContainKey("categoria");

        assertThat(troca(http().patch().uri(caminho(par.b(), "/lancamentos/" + doA))
                .body(Map.of("descricao", "Adulterado")), par.b()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(troca(http().delete().uri(caminho(par.b(), "/lancamentos/" + doA)), par.b())
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(troca(http().post().uri(caminho(par.b(), "/lancamentos/" + doA + "/estorno"))
                .body(Map.of()), par.b()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(linha(itens(get(par.a(), "/lancamentos")), "Estacionamento"))
                .containsEntry("valor", 1000);
    }

    @Test
    void no_banco_o_destino_le_o_lancamento_da_origem_e_nao_consegue_apagar_nem_alterar()
            throws SQLException {
        Par par = doisAmbientes("banco");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        Integer pix = meioDa(par.a(), nubank);
        compartilhar(par.a(), nubank, par.b().ambienteId());
        Integer doA = (Integer) lancar(par.a(), pix, null, 1000, "Estacionamento")
                .getBody().get("id");

        try (Connection conexao = BancoDeTeste.conexaoDaAplicacao(POSTGRES)) {
            conexao.setAutoCommit(false);
            comoAmbiente(conexao, par.b());

            assertThat(contar(conexao, "SELECT count(*) FROM lancamento WHERE id = " + doA))
                    .as("o destino enxerga a conta emprestada")
                    .isEqualTo(1);
            assertThat(executar(conexao, "DELETE FROM lancamento WHERE id = " + doA))
                    .as("ler não é poder apagar: o OR é do SELECT e só dele")
                    .isZero();
            assertThat(executar(conexao,
                    "UPDATE lancamento SET descricao = 'x' WHERE id = " + doA))
                    .isZero();
            assertThat(executar(conexao, "UPDATE conta SET nome = 'x' WHERE id = " + nubank))
                    .as("o uso atravessa, a posse nunca")
                    .isZero();
            assertThat(executar(conexao, "DELETE FROM conta WHERE id = " + nubank)).isZero();
            conexao.rollback();
        }
    }

    @Test
    void no_banco_so_o_dono_da_origem_cria_e_revoga_o_vinculo() throws SQLException {
        Par par = doisAmbientes("vinculo");
        Integer nubank = criarConta(par.a(), "Nubank", null);
        Sessao intruso = novaSessao("intruso-vinculo");

        try (Connection conexao = BancoDeTeste.conexaoDaAplicacao(POSTGRES)) {
            conexao.setAutoCommit(false);
            comoAmbiente(conexao, intruso);

            try (PreparedStatement insere = conexao.prepareStatement(
                    "INSERT INTO vinculo (objeto, conta_id, ambiente_origem_id, "
                            + "ambiente_destino_id, criado_por) VALUES ('CONTA', ?, ?, ?, ?)")) {
                insere.setLong(1, nubank);
                insere.setLong(2, par.a().ambienteId());
                insere.setLong(3, intruso.ambienteId());
                insere.setLong(4, intruso.usuarioId());
                org.junit.jupiter.api.Assertions.assertThrows(SQLException.class,
                        insere::executeUpdate);
            }
            conexao.rollback();
        }
    }

    @Test
    void revogar_tira_a_conta_do_destino_e_deixa_os_lancamentos_onde_estao() {
        Par par = doisAmbientes("revoga");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        Integer pix = meioDa(par.a(), nubank);
        compartilhar(par.a(), nubank, par.b().ambienteId());
        lancar(par.b(), pix, null, 2000, "Compra de moeda");

        var revogado = troca(http().delete().uri(caminho(par.a(),
                "/contas/" + nubank + "/compartilhamentos/" + par.b().ambienteId())), par.a());
        assertThat(revogado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(itens(get(par.b(), "/contas")))
                .as("o destino para de ver a conta")
                .isEmpty();
        assertThat(lancar(par.b(), pix, null, 500, "Depois de revogar").getStatusCode())
                .as("e de poder lançar nela")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(itens(get(par.b(), "/lancamentos")))
                .extracting(l -> l.get("descricao"))
                .as("o que já foi lançado é de quem lançou: fica tudo")
                .containsExactly("Compra de moeda");
        assertThat(itens(get(par.a(), "/lancamentos?contaId=" + nubank)))
                .extracting(l -> l.get("descricao"))
                .contains("Compra de moeda");
        assertThat(conta(par.a(), nubank))
                .containsEntry("saldoRealizadoCentavos", 98000);
    }

    @Test
    void o_destino_transfere_da_conta_emprestada_para_uma_conta_dele() {
        Par par = doisAmbientes("transfere");
        Integer nubank = criarConta(par.a(), "Nubank", 100000L);
        compartilhar(par.a(), nubank, par.b().ambienteId());
        Integer cofrinho = criarConta(par.b(), "Cofrinho", null);

        var resposta = troca(http().post().uri(caminho(par.b(), "/lancamentos/transferencias"))
                .body(Map.of("contaDeOrigemId", nubank, "contaDeDestinoId", cofrinho,
                        "valor", 30000, "dataEvento", "2026-09-12",
                        "descricao", "Reserva")), par.b());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(conta(par.a(), nubank)).containsEntry("saldoRealizadoCentavos", 70000);
        assertThat(conta(par.b(), cofrinho)).containsEntry("saldoRealizadoCentavos", 30000);
    }

    @Test
    void as_recusas_do_compartilhamento() {
        Par par = doisAmbientes("recusas");
        Integer nubank = criarConta(par.a(), "Nubank", null);
        Sessao estranho = novaSessao("estranho-recusas");

        assertThat(compartilhar(par.a(), nubank, par.a().ambienteId()))
                .as("emprestar para si mesmo não é compartilhar")
                .satisfies(r -> {
                    assertThat(r.getStatusCode().value()).isEqualTo(422);
                    assertThat(r.getBody()).containsEntry("codigo", "AMBIENTE_DESTINO_INVALIDO");
                });
        assertThat(compartilhar(par.a(), nubank, estranho.ambienteId()))
                .as("ambiente de outra pessoa não é destino nesta entrega")
                .satisfies(r -> {
                    assertThat(r.getStatusCode().value()).isEqualTo(422);
                    assertThat(r.getBody()).containsEntry("codigo", "AMBIENTE_DESTINO_INVALIDO");
                });

        assertThat(compartilhar(par.a(), nubank, par.b().ambienteId()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(compartilhar(par.a(), nubank, par.b().ambienteId()))
                .satisfies(r -> {
                    assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(r.getBody()).containsEntry("codigo", "COMPARTILHAMENTO_JA_EXISTE");
                });

        assertThat(compartilhar(par.b(), nubank, par.a().ambienteId()).getStatusCode())
                .as("quem recebeu não repassa: a conta não é dele")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(troca(http().post()
                .uri(caminho(estranho, "/contas/" + nubank + "/compartilhamentos"))
                .body(Map.of("ambienteDestinoId", estranho.ambienteId())), estranho)
                .getStatusCode())
                .as("conta de outro ambiente responde igual a inexistente")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(itens(get(estranho, "/contas"))).isEmpty();
    }

    @Test
    void conta_de_cartao_nao_se_compartilha_inteira() {
        Par par = doisAmbientes("cartao");
        Map<String, Object> corpo = new HashMap<>(Map.of("nome", "UltraVioleta", "tipo", "CARTAO",
                "diaVencimento", 10, "diasAntesFechamento", 7, "limite", 500000));
        var criada = troca(http().post().uri(caminho(par.a(), "/contas")).body(corpo), par.a());
        assertThat(criada.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var resposta = compartilhar(par.a(), (Integer) criada.getBody().get("id"),
                par.b().ambienteId());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("codigo", "CONTA_CARTAO_NAO_SE_COMPARTILHA");
    }

    @Test
    void a_lista_de_compartilhamentos_diz_com_quem_a_conta_esta_dividida() {
        Par par = doisAmbientes("lista");
        Integer nubank = criarConta(par.a(), "Nubank", null);
        compartilhar(par.a(), nubank, par.b().ambienteId());

        assertThat(itens(get(par.a(), "/contas/" + nubank + "/compartilhamentos")))
                .singleElement()
                .satisfies(c -> {
                    assertThat(c).containsEntry("ambienteDestinoId", par.b().ambienteId());
                    assertThat(c).containsEntry("ambienteDestinoNome", "Viagem");
                });
    }

    @Test
    void criar_e_revogar_gravam_evento_no_ambiente_de_origem() {
        Par par = doisAmbientes("evento");
        Integer nubank = criarConta(par.a(), "Nubank", null);
        String hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

        compartilhar(par.a(), nubank, par.b().ambienteId());
        troca(http().delete().uri(caminho(par.a(),
                "/contas/" + nubank + "/compartilhamentos/" + par.b().ambienteId())), par.a());

        assertThat(itens(get(par.a(), "/eventos?dia=" + hoje)))
                .extracting(e -> e.get("tipo"))
                .contains("VINCULO_CRIADO", "VINCULO_REVOGADO");
        assertThat(itens(get(par.b(), "/eventos?dia=" + hoje)))
                .extracting(e -> e.get("tipo"))
                .as("o Diário do destino não mostra o que a origem fez")
                .doesNotContain("VINCULO_CRIADO", "VINCULO_REVOGADO");
    }

    @Test
    void o_convidado_traz_a_conta_dele_para_o_ambiente_que_recebeu_e_cada_um_ve_quem_lancou() {
        Par abner = doisAmbientes("cenario");
        Integer nubank = criarConta(abner.a(), "Nubank", 100000L);
        assertThat(compartilhar(abner.a(), nubank, abner.b().ambienteId()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        Sessao bia = novaSessao("cenario-bia");
        convidarEAceitar(abner.b(), "cenario-bia@exemplo.com", "EDITOR", bia);
        Integer itau = criarConta(bia, "Itaú", 50000L);

        assertThat(compartilhar(bia, itau, abner.b().ambienteId()).getStatusCode())
                .as("a convidada empresta a conta dela para o ambiente que recebeu")
                .isEqualTo(HttpStatus.CREATED);

        assertThat(itens(get(abner.b(), "/contas")))
                .extracting(c -> c.get("nome"))
                .containsExactlyInAnyOrder("Nubank", "Itaú");
        assertThat(mapa(conta(abner.b(), itau).get("compartilhadaDe")))
                .as("o ambiente e a pessoa que emprestaram são legíveis para quem recebeu")
                .containsEntry("ambienteId", bia.ambienteId())
                .containsEntry("nome", "Ambiente Pessoal")
                .containsEntry("emprestadaPorId", bia.usuarioId())
                .containsEntry("emprestadaPor", "cenario-bia");

        Sessao biaNoB = new Sessao(bia.usuarioId(), abner.b().ambienteId(), bia.cookie());
        assertThat(lancar(abner.b(), meioDa(abner.b(), itau), null, 1500, "Mercado do Abner")
                .getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(lancar(biaNoB, meioDa(biaNoB, nubank), null, 700, "Padaria da Bia")
                .getStatusCode())
                .as("a convidada lança na conta que o Abner trouxe")
                .isEqualTo(HttpStatus.CREATED);

        var noItauDaBia = linha(itens(get(bia, "/lancamentos?contaId=" + itau)),
                "Mercado do Abner");
        assertThat(noItauDaBia).containsEntry("deOutroAmbiente", true)
                .containsEntry("ambienteNome", "Viagem")
                .containsEntry("categoriaId", null);
        assertThat(mapa(noItauDaBia.get("autor"))).containsEntry("nome", "cenario");
        assertThat(conta(bia, itau)).containsEntry("saldoRealizadoCentavos", 48500);
    }

    @Test
    void quem_so_le_o_ambiente_nao_traz_conta_para_ele() {
        Par abner = doisAmbientes("leitora");
        Sessao bia = novaSessao("leitora-bia");
        convidarEAceitar(abner.b(), "leitora-bia@exemplo.com", "LEITOR", bia);
        Integer itau = criarConta(bia, "Itaú", null);

        assertThat(compartilhar(bia, itau, abner.b().ambienteId()))
                .satisfies(r -> {
                    assertThat(r.getStatusCode().value()).isEqualTo(422);
                    assertThat(r.getBody()).containsEntry("codigo", "AMBIENTE_DESTINO_INVALIDO");
                });
    }

    @Test
    void o_dono_do_destino_devolve_a_conta_emprestada_e_o_editor_nao() {
        Par abner = doisAmbientes("devolve");
        Integer nubank = criarConta(abner.a(), "Nubank", null);
        compartilhar(abner.a(), nubank, abner.b().ambienteId());
        Sessao bia = novaSessao("devolve-bia");
        convidarEAceitar(abner.b(), "devolve-bia@exemplo.com", "EDITOR", bia);
        Integer itau = criarConta(bia, "Itaú", null);
        compartilhar(bia, itau, abner.b().ambienteId());
        Sessao biaNoB = new Sessao(bia.usuarioId(), abner.b().ambienteId(), bia.cookie());
        String hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();

        assertThat(devolver(biaNoB, nubank).getStatusCode())
                .as("o editor do destino não devolve")
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(devolver(abner.b(), itau).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(itens(get(abner.b(), "/contas")))
                .extracting(c -> c.get("nome")).containsExactly("Nubank");
        assertThat(itens(get(bia, "/contas/" + itau + "/compartilhamentos"))).isEmpty();

        assertThat(itens(get(abner.b(), "/eventos?dia=" + hoje)))
                .filteredOn(e -> "VINCULO_REVOGADO".equals(e.get("tipo")))
                .singleElement()
                .satisfies(e -> assertThat(mapa(e.get("dados")))
                        .containsEntry("conta", "Itaú").containsEntry("devolvida", true));
    }

    @Test
    void quem_sai_do_ambiente_leva_a_conta_que_emprestou_e_quem_e_removido_tambem() {
        Par abner = doisAmbientes("saida");
        Sessao bia = novaSessao("saida-bia");
        Sessao caio = novaSessao("saida-caio");
        convidarEAceitar(abner.b(), "saida-bia@exemplo.com", "EDITOR", bia);
        convidarEAceitar(abner.b(), "saida-caio@exemplo.com", "EDITOR", caio);
        Integer itau = criarConta(bia, "Itaú", null);
        Integer inter = criarConta(caio, "Inter", null);
        compartilhar(bia, itau, abner.b().ambienteId());
        compartilhar(caio, inter, abner.b().ambienteId());
        Sessao biaNoB = new Sessao(bia.usuarioId(), abner.b().ambienteId(), bia.cookie());

        assertThat(troca(http().delete().uri(caminho(biaNoB, "/membros/" + bia.usuarioId())),
                biaNoB).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(troca(http().delete().uri(caminho(abner.b(), "/membros/" + caio.usuarioId())),
                abner.b()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(itens(get(abner.b(), "/contas")))
                .as("as duas contas saíram com quem as emprestou")
                .isEmpty();
        assertThat(itens(get(bia, "/contas")))
                .extracting(c -> c.get("nome")).containsExactly("Itaú");
    }

    private Par doisAmbientes(String apelido) {
        Sessao a = novaSessao(apelido);
        var criado = troca(http().post().uri("/api/v1/ambientes").body(Map.of("nome", "Viagem")),
                a);
        assertThat(criado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return new Par(a, new Sessao(a.usuarioId(), (Integer) criado.getBody().get("id"),
                a.cookie()));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    private void convidarEAceitar(Sessao dono, String email, String papel, Sessao convidado) {
        var convite = troca(http().post().uri(caminho(dono, "/convites"))
                .body(Map.of("email", email, "papel", papel)), dono);
        assertThat(convite.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(troca(http().post().uri("/api/v1/convites/" + convite.getBody().get("id")
                + "/aceite"), convidado).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> devolver(Sessao destino, Integer contaId) {
        return troca(http().delete().uri(caminho(destino, "/contas/" + contaId
                + "/compartilhamentos/" + destino.ambienteId())), destino);
    }

    private ResponseEntity<Map<String, Object>> compartilhar(Sessao origem, Integer contaId,
            Integer destinoId) {
        return troca(http().post()
                .uri(caminho(origem, "/contas/" + contaId + "/compartilhamentos"))
                .body(Map.of("ambienteDestinoId", destinoId)), origem);
    }

    private Integer criarConta(Sessao sessao, String nome, Long saldoInicial) {
        Map<String, Object> corpo = new HashMap<>(Map.of("nome", nome, "tipo", "CORRENTE",
                "meios", List.of("PIX")));
        corpo.put("saldoInicial", saldoInicial);
        var resposta = troca(http().post().uri(caminho(sessao, "/contas")).body(corpo), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private Integer meioDa(Sessao sessao, Integer contaId) {
        return (Integer) itens(get(sessao, "/meios-de-pagamento")).stream()
                .filter(m -> contaId.equals(m.get("contaId")))
                .findFirst().orElseThrow().get("id");
    }

    private Integer criarCategoria(Sessao sessao, String nome) {
        var resposta = troca(http().post().uri(caminho(sessao, "/categorias"))
                .body(Map.of("nome", nome, "sentido", "SAIDA", "cor", "OCRE")), sessao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Integer) resposta.getBody().get("id");
    }

    private ResponseEntity<Map<String, Object>> lancar(Sessao sessao, Integer meioId,
            Integer categoriaId, int valor, String descricao) {
        Map<String, Object> corpo = new HashMap<>(Map.of("meioId", meioId, "sentido", "SAIDA",
                "valor", valor, "dataEvento", "2026-09-10", "descricao", descricao));
        if (categoriaId != null) {
            corpo.put("categoriaId", categoriaId);
        }
        return troca(http().post().uri(caminho(sessao, "/lancamentos")).body(corpo), sessao);
    }

    private Map<String, Object> conta(Sessao sessao, Integer contaId) {
        return itens(get(sessao, "/contas")).stream()
                .filter(c -> contaId.equals(c.get("id")))
                .findFirst().orElseThrow();
    }

    private long patrimonio(Sessao sessao) {
        return ((Number) get(sessao, "/contas").getBody().get("patrimonioCentavos")).longValue();
    }

    private static Map<String, Object> linha(List<Map<String, Object>> linhas, String descricao) {
        return linhas.stream().filter(l -> descricao.equals(l.get("descricao")))
                .findFirst().orElseThrow();
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

        var perfil = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) http().get()
                .uri("/api/v1/usuarios/atual")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(Map.class);

        return new Sessao((Integer) perfil.getBody().get("id"),
                (Integer) itens(ambientes).get(0).get("id"), cookie);
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

    private static void comoAmbiente(Connection conexao, Sessao sessao) throws SQLException {
        executar(conexao, "SELECT set_config('app.usuario_id', '" + sessao.usuarioId()
                + "', true), set_config('app.ambiente_id', '" + sessao.ambienteId()
                + "', true)");
    }

    private static int executar(Connection conexao, String sql) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            if (comando.execute()) {
                return 0;
            }
            return comando.getUpdateCount();
        }
    }

    private static int contar(Connection conexao, String sql) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql);
                var resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    private RestClient http() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> { })
                .build();
    }
}
