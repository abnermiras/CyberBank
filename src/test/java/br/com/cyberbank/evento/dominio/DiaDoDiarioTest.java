package br.com.cyberbank.evento.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class DiaDoDiarioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 17);

    @Test
    void sem_dia_pedido_o_diario_e_o_de_hoje() {
        assertThat(DiaDoDiario.de(null, HOJE).dia()).isEqualTo(HOJE);
    }

    @Test
    void o_seletor_anda_para_tras_livremente() {
        assertThat(DiaDoDiario.de(HOJE.minusYears(3), HOJE).dia()).isEqualTo(HOJE.minusYears(3));
        assertThat(DiaDoDiario.de(HOJE, HOJE).dia()).isEqualTo(HOJE);
    }

    @Test
    void para_frente_do_dia_corrente_nao_ha_diario() {
        assertThatThrownBy(() -> DiaDoDiario.de(HOJE.plusDays(1), HOJE))
                .isInstanceOf(ValidacaoException.class);
    }
}
