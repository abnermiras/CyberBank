package br.com.cyberbank.ambiente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;

import org.junit.jupiter.api.Test;

class AcessoTest {

    private static final Instant ONTEM = Instant.parse("2026-10-02T12:00:00Z");

    private static final Acesso DONO = new Acesso(1L, 10L, 7L, Papel.DONO, ONTEM);
    private static final Acesso EDITOR = new Acesso(2L, 11L, 7L, Papel.EDITOR, ONTEM);
    private static final Acesso LEITOR = new Acesso(3L, 12L, 7L, Papel.LEITOR, ONTEM);

    @Test
    void o_dono_remove_editor_e_leitor() {
        assertThatCode(() -> EDITOR.exigirRemovivelPor(DONO)).doesNotThrowAnyException();
        assertThatCode(() -> LEITOR.exigirRemovivelPor(DONO)).doesNotThrowAnyException();
    }

    @Test
    void editor_e_leitor_saem_por_conta_propria() {
        assertThatCode(() -> EDITOR.exigirRemovivelPor(EDITOR)).doesNotThrowAnyException();
        assertThatCode(() -> LEITOR.exigirRemovivelPor(LEITOR)).doesNotThrowAnyException();
    }

    @Test
    void editor_nao_remove_outra_pessoa() {
        assertThatThrownBy(() -> LEITOR.exigirRemovivelPor(EDITOR))
                .extracting("codigo").isEqualTo(CodigoDeErro.SEM_PERMISSAO);
    }

    @Test
    void o_dono_nao_sai_nem_e_removido() {
        assertThatThrownBy(() -> DONO.exigirRemovivelPor(DONO))
                .extracting("codigo").isEqualTo(CodigoDeErro.DONO_NAO_SAI);
        assertThatThrownBy(() -> DONO.exigirRemovivelPor(EDITOR))
                .extracting("codigo").isEqualTo(CodigoDeErro.DONO_NAO_SAI);
    }

    @Test
    void leitor_nao_altera_o_dado_e_editor_altera() {
        assertThat(Papel.LEITOR.podeAlterarODado()).isFalse();
        assertThat(Papel.EDITOR.podeAlterarODado()).isTrue();
        assertThat(Papel.DONO.podeAlterarODado()).isTrue();
    }
}
