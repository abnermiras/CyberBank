package br.com.cyberbank.compartilhamento.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class CompartilhamentoTest {

    private static final Instant AGORA = Instant.parse("2026-09-28T12:00:00Z");

    @Test
    void conta_comum_se_compartilha_com_outro_ambiente() {
        Compartilhamento novo = Compartilhamento.novo(7L, false, 1L, 2L, 9L, AGORA);

        assertThat(novo.contaId()).isEqualTo(7L);
        assertThat(novo.ambienteOrigemId()).isEqualTo(1L);
        assertThat(novo.ambienteDestinoId()).isEqualTo(2L);
        assertThat(novo.criadoPor()).isEqualTo(9L);
    }

    @Test
    void conta_de_cartao_nao_se_compartilha_inteira() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, true, 1L, 2L, 9L, AGORA))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo())
                                .isEqualTo(CodigoDeErro.CONTA_CARTAO_NAO_SE_COMPARTILHA));
    }

    @Test
    void nao_se_compartilha_com_o_proprio_ambiente_de_origem() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, false, 1L, 1L, 9L, AGORA))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo())
                                .isEqualTo(CodigoDeErro.AMBIENTE_DESTINO_INVALIDO));
    }

    @Test
    void sem_destino_e_erro_de_validacao() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, false, 1L, null, 9L, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }
}
