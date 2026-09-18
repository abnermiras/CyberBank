package br.com.cyberbank.conta.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ValorInformadoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 17);

    @Test
    void no_trigesimo_dia_ainda_esta_no_prazo_e_no_trigesimo_primeiro_nao() {
        assertThat(new ValorInformado(100000L, HOJE.minusDays(30)).desatualizadoEm(HOJE))
                .as("trinta dias é o prazo, e prazo se cumpre no último dia")
                .isFalse();
        assertThat(new ValorInformado(100000L, HOJE.minusDays(31)).desatualizadoEm(HOJE)).isTrue();
    }

    @Test
    void a_idade_e_sempre_visivel_porque_e_ela_que_torna_o_numero_velho_honesto() {
        assertThat(new ValorInformado(100000L, HOJE.minusDays(45)).diasDeIdadeEm(HOJE))
                .isEqualTo(45L);
    }

    @Test
    void aplicacao_que_nunca_teve_valor_informado_nao_e_chamada_de_desatualizada() {
        ValorInformado nunca = new ValorInformado(0L, null);

        assertThat(nunca.nuncaInformado()).isTrue();
        assertThat(nunca.desatualizadoEm(HOJE))
                .as("marcar de velho o que nunca teve valor é ruído, não aviso")
                .isFalse();
    }
}
