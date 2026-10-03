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
        Compartilhamento novo = Compartilhamento.novo(7L, false, 1L, 2L, true, 9L, AGORA);

        assertThat(novo.contaId()).isEqualTo(7L);
        assertThat(novo.ambienteOrigemId()).isEqualTo(1L);
        assertThat(novo.ambienteDestinoId()).isEqualTo(2L);
        assertThat(novo.criadoPor()).isEqualTo(9L);
    }

    @Test
    void conta_de_cartao_nao_se_compartilha_inteira() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, true, 1L, 2L, true, 9L, AGORA))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo())
                                .isEqualTo(CodigoDeErro.CONTA_CARTAO_NAO_SE_COMPARTILHA));
    }

    @Test
    void nao_se_compartilha_com_o_proprio_ambiente_de_origem() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, false, 1L, 1L, true, 9L, AGORA))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo())
                                .isEqualTo(CodigoDeErro.AMBIENTE_DESTINO_INVALIDO));
    }

    @Test
    void sem_destino_e_erro_de_validacao() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, false, 1L, null, false, 9L, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void so_se_traz_conta_para_ambiente_em_que_se_altera_o_dado() {
        assertThatThrownBy(() -> Compartilhamento.novo(7L, false, 1L, 2L, false, 9L, AGORA))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo())
                                .isEqualTo(CodigoDeErro.AMBIENTE_DESTINO_INVALIDO));
    }

    @Test
    void as_duas_pontas_revogam_e_so_o_dono_de_cada_uma() {
        Compartilhamento vinculo = new Compartilhamento(3L, 7L, 1L, 2L, 9L, AGORA);

        vinculo.exigirRevogavelPor(1L, true);
        vinculo.exigirRevogavelPor(2L, true);
        assertThat(vinculo.devolvidoPor(2L)).isTrue();
        assertThat(vinculo.devolvidoPor(1L)).isFalse();

        assertThatThrownBy(() -> vinculo.exigirRevogavelPor(2L, false))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoDeErro.SEM_PERMISSAO));
        assertThatThrownBy(() -> vinculo.exigirRevogavelPor(5L, true))
                .isInstanceOfSatisfying(RegraDeDominioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoDeErro.NAO_ENCONTRADO));
    }

    @Test
    void quem_emprestou_e_quem_criou_o_vinculo() {
        Compartilhamento vinculo = new Compartilhamento(3L, 7L, 1L, 2L, 9L, AGORA);

        assertThat(vinculo.emprestadoPor(9L)).isTrue();
        assertThat(vinculo.emprestadoPor(4L)).isFalse();
    }
}
