package br.com.cyberbank.lancamento.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class JanelaDoPrevistoTest {

    @Test
    void no_mes_corrente_a_janela_comeca_hoje_e_nao_no_dia_primeiro() {
        JanelaDoPrevisto janela =
                JanelaDoPrevisto.doMesCorrente(LocalDate.of(2026, 9, 17));

        assertThat(janela.de())
                .as("o que estava previsto e já passou não é mais projeção")
                .isEqualTo(LocalDate.of(2026, 9, 17));
        assertThat(janela.ate()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    void num_mes_futuro_a_janela_e_o_mes_inteiro() {
        JanelaDoPrevisto janela = JanelaDoPrevisto.doMes(LocalDate.of(2026, 9, 17),
                YearMonth.of(2026, 10));

        assertThat(janela.de()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(janela.ate()).isEqualTo(LocalDate.of(2026, 10, 31));
    }

    @Test
    void num_mes_que_ja_acabou_a_janela_e_vazia() {
        JanelaDoPrevisto janela = JanelaDoPrevisto.doMes(LocalDate.of(2026, 9, 17),
                YearMonth.of(2026, 8));

        assertThat(janela.vazia())
                .as("mês que acabou não tem futuro para projetar")
                .isTrue();
    }
}
