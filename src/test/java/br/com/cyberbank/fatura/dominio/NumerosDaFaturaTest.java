package br.com.cyberbank.fatura.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class NumerosDaFaturaTest {

    private static final Instant AGORA = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    void o_exemplo_literal_do_doc_agosto_paga_800_e_rola_810_60() {
        NumerosDaFatura agosto = new NumerosDaFatura(161060, 80000, 81060);

        assertThat(agosto.aPagarCentavos()).isZero();
        assertThat(agosto.encerrada()).isTrue();
        assertThat(agosto.rolada()).isTrue();
        assertThat(agosto.parcial())
                .as("agosto fica parcial · rolada, e o total histórico continua R$ 1.610,60")
                .isTrue();
        assertThat(agosto.totalCentavos()).isEqualTo(161060);
    }

    @Test
    void a_pagar_e_total_menos_pago_menos_rolado() {
        assertThat(new NumerosDaFatura(100000, 30000, 0).aPagarCentavos()).isEqualTo(70000);
        assertThat(new NumerosDaFatura(100000, 0, 0).emAberto()).isTrue();
    }

    @Test
    void pagar_mais_que_a_fatura_deixa_credito_e_isso_e_encerrada() {
        NumerosDaFatura comCredito = new NumerosDaFatura(100000, 120000, 0);

        assertThat(comCredito.aPagarCentavos()).isEqualTo(-20000);
        assertThat(comCredito.encerrada()).isTrue();
        assertThat(comCredito.quitada()).isTrue();
    }

    @Test
    void a_janela_do_pagamento_e_a_da_abertura_sao_a_mesma() {
        Fatura fechada = fechada();

        assertThat(fechada.naJanelaDoPagamentoEDaAbertura(new NumerosDaFatura(100000, 0, 0)))
                .isTrue();
        assertThat(fechada.naJanelaDoPagamentoEDaAbertura(new NumerosDaFatura(100000, 100000, 0)))
                .as("quitada não recebe pagamento e não abre")
                .isFalse();
        assertThat(fechada.naJanelaDoPagamentoEDaAbertura(new NumerosDaFatura(100000, 0, 100000)))
                .as("rolada não recebe pagamento e não abre — é o que impede a rolagem infinita")
                .isFalse();
        assertThat(fechada.naJanelaDoPagamentoEDaAbertura(NumerosDaFatura.VAZIA))
                .as("fatura de total zero também não abre")
                .isFalse();
    }

    @Test
    void fatura_futura_e_fatura_aberta_estao_fora_da_janela() {
        NumerosDaFatura devendo = new NumerosDaFatura(100000, 0, 0);

        assertThat(comStatus(StatusDaFatura.FUTURA).naJanelaDoPagamentoEDaAbertura(devendo))
                .isFalse();
        assertThat(comStatus(StatusDaFatura.ABERTA).naJanelaDoPagamentoEDaAbertura(devendo))
                .as("pagar antes do fechamento é antecipar, e isso é outra mecânica")
                .isFalse();
    }

    @Test
    void so_a_aberta_recebe_compra_nova() {
        assertThat(comStatus(StatusDaFatura.ABERTA).recebeCompraNova()).isTrue();
        assertThat(comStatus(StatusDaFatura.FUTURA).recebeCompraNova()).isFalse();
        assertThat(comStatus(StatusDaFatura.FECHADA).recebeCompraNova()).isFalse();
    }

    private static Fatura fechada() {
        return comStatus(StatusDaFatura.FECHADA);
    }

    private static Fatura comStatus(StatusDaFatura status) {
        return new Fatura(42L, 7L, 9L, YearMonth.of(2026, 8), LocalDate.of(2026, 7, 28),
                LocalDate.of(2026, 8, 5), status, AGORA);
    }
}
