package br.com.cyberbank.ambiente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class AmbienteTest {

    private static final Instant AGORA = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void o_ambiente_novo_nasce_com_o_nome_pedido_sem_as_bordas_em_branco() {
        Ambiente casa = Ambiente.novo("  Casa ", 7L, AGORA);

        assertThat(casa.nome()).isEqualTo("Casa");
        assertThat(casa.criadoPor()).isEqualTo(7L);
        assertThat(casa.criadoEm()).isEqualTo(AGORA);
    }

    @Test
    void ambiente_sem_nome_nao_nasce() {
        assertThatThrownBy(() -> Ambiente.novo("   ", 7L, AGORA))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .extracting(ErroDeValidacao::codigo)
                        .containsExactly("OBRIGATORIO"));
    }

    @Test
    void o_nome_cabe_na_coluna() {
        String longo = "x".repeat(Ambiente.TAMANHO_MAXIMO_DO_NOME + 1);

        assertThatThrownBy(() -> Ambiente.novo(longo, 7L, AGORA))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .extracting(ErroDeValidacao::codigo)
                        .containsExactly("TAMANHO"));
    }

    @Test
    void dono_e_editor_renomeiam_e_so_o_nome_muda() {
        Ambiente pessoal = new Ambiente(3L, Ambiente.NOME_PADRAO, 7L, AGORA);

        for (Papel papel : new Papel[] {Papel.DONO, Papel.EDITOR}) {
            Ambiente renomeado = pessoal.renomeadoPor(papel, " Casa ");

            assertThat(renomeado.nome()).isEqualTo("Casa");
            assertThat(renomeado.id()).isEqualTo(3L);
            assertThat(renomeado.criadoPor()).isEqualTo(7L);
            assertThat(renomeado.criadoEm()).isEqualTo(AGORA);
        }
    }

    @Test
    void leitor_nao_renomeia() {
        Ambiente pessoal = new Ambiente(3L, Ambiente.NOME_PADRAO, 7L, AGORA);

        assertThatThrownBy(() -> pessoal.renomeadoPor(Papel.LEITOR, "Casa"))
                .isInstanceOf(RegraDeDominioException.class)
                .satisfies(e -> assertThat(((RegraDeDominioException) e).codigo())
                        .isEqualTo(CodigoDeErro.SEM_PERMISSAO));
    }

    @Test
    void renomear_para_vazio_e_recusado() {
        Ambiente pessoal = new Ambiente(3L, Ambiente.NOME_PADRAO, 7L, AGORA);

        assertThatThrownBy(() -> pessoal.renomeadoPor(Papel.DONO, ""))
                .isInstanceOf(ValidacaoException.class);
    }
}
