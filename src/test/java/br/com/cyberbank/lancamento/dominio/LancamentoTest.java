package br.com.cyberbank.lancamento.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class LancamentoTest {

    private static final Instant AGORA = Instant.parse("2026-09-16T12:00:00Z");
    private static final LocalDate HOJE = LocalDate.of(2026, 9, 16);
    private static final Long AMBIENTE = 7L;
    private static final Long CONTA = 3L;
    private static final Long OUTRA_CONTA = 4L;
    private static final Long MEIO = 5L;
    private static final Long CATEGORIA = 11L;
    private static final Long AUTOR = 1L;

    private Lancamento mercado() {
        return Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, CATEGORIA, AUTOR, Sentido.SAIDA,
                30000L, HOJE, HOJE, "Mercado", Situacao.REALIZADO, null, null, AGORA);
    }

    @Test
    void o_valor_e_sempre_positivo_e_o_sinal_vem_do_sentido() {
        assertThatThrownBy(() -> Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, CATEGORIA, AUTOR,
                Sentido.SAIDA, -30000L, HOJE, HOJE, "Mercado", Situacao.REALIZADO, null, null, AGORA))
                .isInstanceOf(ValidacaoException.class);

        assertThat(mercado().valorComSinal()).isEqualTo(-30000L);
    }

    @Test
    void zero_nao_e_lancamento() {
        assertThatThrownBy(() -> Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, CATEGORIA, AUTOR,
                Sentido.SAIDA, 0L, HOJE, HOJE, "Nada", Situacao.REALIZADO, null, null, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void a_data_de_efeito_nunca_antecede_a_do_evento() {
        assertThatThrownBy(() -> Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, CATEGORIA, AUTOR,
                Sentido.SAIDA, 30000L, HOJE, HOJE.minusDays(1), "Mercado", Situacao.REALIZADO,
                null, null, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void gasto_real_tem_meio_de_pagamento() {
        assertThatThrownBy(() -> Lancamento.doUsuario(AMBIENTE, CONTA, null, CATEGORIA, AUTOR,
                Sentido.SAIDA, 30000L, HOJE, HOJE, "Mercado", Situacao.REALIZADO, null, null, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void entra_no_saldo_e_diferente_de_previsto_nunca_igual_a_realizado() {
        assertThat(Situacao.PREVISTO.entraNoSaldoRealizado()).isFalse();
        assertThat(Situacao.PROVISIONADO.entraNoSaldoRealizado())
                .as("aconteceu, falta liquidar — e já pesa no saldo")
                .isTrue();
        assertThat(Situacao.REALIZADO.entraNoSaldoRealizado()).isTrue();
    }

    @Test
    void a_automacao_so_anda_para_frente() {
        assertThat(Situacao.PREVISTO.andaParaFrenteAte(Situacao.REALIZADO)).isTrue();
        assertThat(Situacao.PREVISTO.andaParaFrenteAte(Situacao.PROVISIONADO)).isTrue();
        assertThat(Situacao.REALIZADO.andaParaFrenteAte(Situacao.PREVISTO)).isFalse();
    }

    @Test
    void a_data_realiza_o_previsto_e_so_quando_ela_chega() {
        Lancamento boleto = Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, CATEGORIA, AUTOR,
                Sentido.SAIDA, 19900L, HOJE, HOJE.plusDays(10), "Luz", Situacao.PREVISTO, null, null,
                AGORA);

        assertThat(boleto.realizadoPelaData(HOJE).situacao()).isEqualTo(Situacao.PREVISTO);
        assertThat(boleto.realizadoPelaData(HOJE.plusDays(10)).situacao())
                .isEqualTo(Situacao.REALIZADO);
    }

    @Test
    void a_correcao_do_usuario_devolve_o_boleto_para_previsto() {
        Lancamento realizado = comId(20L, mercado());

        assertThat(realizado.corrigido(null, null, null, null, null, null, null, null,
                Situacao.PREVISTO).situacao())
                .as("a correção descreve o registro, não o dinheiro — e anda nos dois sentidos")
                .isEqualTo(Situacao.PREVISTO);
    }

    @Test
    void a_abertura_nasce_realizada_do_ciclo_e_sem_meio() {
        Lancamento abertura = Lancamento.deAbertura(AMBIENTE, CONTA, CATEGORIA, AUTOR,
                1123600L, HOJE, AGORA);

        assertThat(abertura.situacao()).isEqualTo(Situacao.REALIZADO);
        assertThat(abertura.doCiclo()).isTrue();
        assertThat(abertura.meioId()).as("ninguém pagou nada: o dinheiro já estava lá").isNull();
        assertThat(abertura.sentido()).isEqualTo(Sentido.ENTRADA);
        assertThat(abertura.categoriaId())
                .as("nasce com a categoria de sistema, e por isso nunca aparece nas pendências")
                .isEqualTo(CATEGORIA);
        assertThat(abertura.pendente()).isFalse();
    }

    @Test
    void conta_no_vermelho_abre_com_saida_e_valor_positivo() {
        Lancamento abertura = Lancamento.deAbertura(AMBIENTE, CONTA, CATEGORIA, AUTOR,
                -50000L, HOJE, AGORA);

        assertThat(abertura.sentido()).isEqualTo(Sentido.SAIDA);
        assertThat(abertura.valorCentavos()).isEqualTo(50000L);
        assertThat(abertura.valorComSinal()).isEqualTo(-50000L);
    }

    @Test
    void a_abertura_e_do_ciclo_e_o_usuario_nao_a_exclui() {
        Lancamento abertura = comId(21L,
                Lancamento.deAbertura(AMBIENTE, CONTA, CATEGORIA, AUTOR, 1000L, HOJE, AGORA));

        assertThatThrownBy(() -> abertura.exigirExcluivelPeloUsuario(false))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.LANCAMENTO_DO_CICLO);
    }

    @Test
    void a_transferencia_e_um_par_de_sentidos_opostos_que_soma_zero() {
        List<Lancamento> par = Lancamento.parDeTransferencia(AMBIENTE, CONTA, OUTRA_CONTA,
                CATEGORIA, CATEGORIA, AUTOR, 50000L, HOJE, HOJE, "Aporte", Situacao.REALIZADO,
                77L, AGORA);

        assertThat(par).hasSize(2);
        assertThat(par.get(0).sentido()).isEqualTo(Sentido.SAIDA);
        assertThat(par.get(1).sentido()).isEqualTo(Sentido.ENTRADA);
        assertThat(par.get(0).contaId()).isEqualTo(CONTA);
        assertThat(par.get(1).contaId()).isEqualTo(OUTRA_CONTA);
        assertThat(par.stream().mapToLong(Lancamento::valorComSinal).sum()).isZero();
        assertThat(par).allSatisfy(l -> assertThat(l.transferenciaId()).isEqualTo(77L));
        assertThat(par).allSatisfy(l -> assertThat(l.meioId())
                .as("em transferência o dinheiro não foi pago, só mudou de lugar").isNull());
    }

    @Test
    void transferencia_para_a_propria_conta_nao_existe() {
        assertThatThrownBy(() -> Lancamento.parDeTransferencia(AMBIENTE, CONTA, CONTA,
                CATEGORIA, CATEGORIA, AUTOR, 50000L, HOJE, HOJE, "Aporte", Situacao.REALIZADO,
                77L, AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.TRANSFERENCIA_MESMA_CONTA);
    }

    @Test
    void o_estorno_inverte_o_sentido_herda_a_categoria_e_aponta_para_o_original() {
        Lancamento compra = comId(30L, mercado());
        Lancamento estorno = compra.estornadoEm(HOJE.plusDays(3), AUTOR, null,
                Situacao.REALIZADO, AGORA);

        assertThat(estorno.sentido()).isEqualTo(Sentido.ENTRADA);
        assertThat(estorno.valorCentavos()).isEqualTo(compra.valorCentavos());
        assertThat(estorno.categoriaId())
                .as("o relatório de gasto é líquido: o estorno abate o mês em que aconteceu")
                .isEqualTo(compra.categoriaId());
        assertThat(estorno.estornoDeId()).isEqualTo(30L);
        assertThat(estorno.dataEvento()).isEqualTo(HOJE.plusDays(3));
    }

    @Test
    void lancamento_com_estorno_apontando_para_ele_nao_se_exclui() {
        Lancamento compra = comId(30L, mercado());

        compra.exigirExcluivelPeloUsuario(false);

        assertThatThrownBy(() -> compra.exigirExcluivelPeloUsuario(true))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.LANCAMENTO_COM_ESTORNO);
    }

    @Test
    void pendencia_e_categoria_nula_e_nada_alem_disso() {
        Lancamento semCategoria = Lancamento.doUsuario(AMBIENTE, CONTA, MEIO, null, AUTOR,
                Sentido.SAIDA, 5000L, HOJE, HOJE, "MEDTECH 24H", Situacao.REALIZADO, null, null, AGORA);

        assertThat(semCategoria.pendente()).isTrue();
        assertThat(mercado().pendente()).isFalse();
    }

    @Test
    void na_transferencia_a_data_de_efeito_acompanha_a_de_evento() {
        Lancamento saida = comId(40L, Lancamento.parDeTransferencia(AMBIENTE, CONTA, OUTRA_CONTA,
                CATEGORIA, CATEGORIA, AUTOR, 50000L, HOJE, HOJE, "Para a poupança",
                Situacao.REALIZADO, 9L, AGORA).getFirst());

        Lancamento adiada = saida.corrigidoNaTransferencia(null, HOJE.plusDays(5), null, null);

        assertThat(adiada.dataEvento()).isEqualTo(HOJE.plusDays(5));
        assertThat(adiada.dataEfeito())
                .as("mover dinheiro entre contas não tem vencimento: as duas datas são a mesma")
                .isEqualTo(HOJE.plusDays(5));
    }

    @Test
    void a_abertura_corrige_o_valor_e_recusa_todo_o_resto() {
        Lancamento abertura = comId(50L, Lancamento.deAbertura(AMBIENTE, CONTA, CATEGORIA, AUTOR,
                1123600L, HOJE, AGORA));

        assertThat(abertura.corrigido(null, null, null, null, 980000L, null, null, null, null)
                .valorCentavos())
                .as("o saldo de abertura se corrige editando o valor")
                .isEqualTo(980000L);

        assertThatThrownBy(() -> abertura.corrigido(null, null, null, null, 980000L, null, null,
                "Outra coisa", null))
                .as("o resto do lançamento do ciclo não é do usuário para mexer")
                .isInstanceOf(RegraDeDominioException.class)
                .hasMessage(CodigoDeErro.LANCAMENTO_DO_CICLO.name());

        assertThatThrownBy(() -> abertura.corrigido(OUTRA_CONTA, null, null, null, null, null,
                null, null, null))
                .isInstanceOf(RegraDeDominioException.class);

        assertThatThrownBy(() -> abertura.exigirExcluivelPeloUsuario(false))
                .as("corrigir o valor é permitido; excluir a abertura continua não sendo")
                .isInstanceOf(RegraDeDominioException.class);
    }

    @Test
    void o_valor_informado_da_aplicacao_vira_a_diferenca_e_nunca_sobrescreve_o_saldo() {
        Lancamento rendeu = Lancamento.deRendimento(AMBIENTE, CONTA, CATEGORIA, AUTOR,
                12500L, HOJE, AGORA);

        assertThat(rendeu.sentido()).isEqualTo(Sentido.ENTRADA);
        assertThat(rendeu.valorCentavos())
                .as("o lançamento é a diferença: o saldo continua sendo a soma dos lançamentos")
                .isEqualTo(12500L);
        assertThat(rendeu.situacao()).isEqualTo(Situacao.REALIZADO);
        assertThat(rendeu.meioId()).as("ninguém pagou nada: a aplicação rendeu").isNull();
        assertThat(rendeu.doCiclo())
                .as("quem informou foi o usuário, e ele pode corrigir o que digitou errado")
                .isFalse();

        assertThat(Lancamento.deRendimento(AMBIENTE, CONTA, CATEGORIA, AUTOR, -3000L, HOJE, AGORA)
                .sentido())
                .as("perder também é informar")
                .isEqualTo(Sentido.SAIDA);
    }

    @Test
    void informar_o_valor_que_a_aplicacao_ja_vale_nao_e_lancamento() {
        assertThatThrownBy(() ->
                Lancamento.deRendimento(AMBIENTE, CONTA, CATEGORIA, AUTOR, 0L, HOJE, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    private static Lancamento comId(Long id, Lancamento l) {
        return new Lancamento(id, l.ambienteId(), l.contaId(), l.meioId(), l.categoriaId(),
                l.autorId(), l.sentido(), l.valorCentavos(), l.dataEvento(), l.dataEfeito(),
                l.descricao(), l.situacao(), l.transferenciaId(), l.estornoDeId(), l.faturaId(),
                l.pagamentoDeFaturaId(), l.rolagemDeFatura(), l.parcelamentoId(), l.doCiclo(),
                l.estabelecimento(), l.criadoEm());
    }
}
