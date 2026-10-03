package br.com.cyberbank.ambiente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class ConviteTest {

    private static final Instant ONTEM = Instant.parse("2026-10-02T12:00:00Z");
    private static final Instant AGORA = Instant.parse("2026-10-03T12:00:00Z");

    private static final Acesso DONO = new Acesso(1L, 10L, 7L, Papel.DONO, ONTEM);
    private static final Acesso EDITOR = new Acesso(2L, 11L, 7L, Papel.EDITOR, ONTEM);

    @Test
    void o_dono_convida_e_o_convite_nasce_pendente_com_o_ambiente_dele() {
        Convite convite = Convite.novo(DONO, "bia@exemplo.com", Papel.EDITOR, false, false, AGORA);

        assertThat(convite.ambienteId()).isEqualTo(7L);
        assertThat(convite.email()).isEqualTo("bia@exemplo.com");
        assertThat(convite.papel()).isEqualTo(Papel.EDITOR);
        assertThat(convite.situacao()).isEqualTo(SituacaoDoConvite.PENDENTE);
        assertThat(convite.convidadoPor()).isEqualTo(10L);
        assertThat(convite.respondidoEm()).isNull();
    }

    @Test
    void o_convite_de_somente_leitura_e_um_convite_de_leitor() {
        Convite convite = Convite.novo(DONO, "bia@exemplo.com", Papel.LEITOR, false, false, AGORA);

        assertThat(convite.papel()).isEqualTo(Papel.LEITOR);
    }

    @Test
    void so_o_dono_convida() {
        assertThatThrownBy(() ->
                Convite.novo(EDITOR, "bia@exemplo.com", Papel.EDITOR, false, false, AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting("codigo").isEqualTo(CodigoDeErro.SEM_PERMISSAO);
    }

    @Test
    void ninguem_e_convidado_para_ser_dono() {
        assertThatThrownBy(() ->
                Convite.novo(DONO, "bia@exemplo.com", Papel.DONO, false, false, AGORA))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .extracting(ErroDeValidacao::campo, ErroDeValidacao::codigo)
                        .containsExactly(tuple("papel", "FORA_DA_LISTA")));
    }

    @Test
    void convite_sem_papel_nao_nasce() {
        assertThatThrownBy(() ->
                Convite.novo(DONO, "bia@exemplo.com", null, false, false, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void quem_ja_tem_acesso_nao_e_convidado() {
        assertThatThrownBy(() ->
                Convite.novo(DONO, "bia@exemplo.com", Papel.EDITOR, true, false, AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting("codigo").isEqualTo(CodigoDeErro.JA_TEM_ACESSO);
    }

    @Test
    void um_email_tem_no_maximo_um_convite_pendente_por_ambiente() {
        assertThatThrownBy(() ->
                Convite.novo(DONO, "bia@exemplo.com", Papel.LEITOR, false, true, AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting("codigo").isEqualTo(CodigoDeErro.CONVITE_JA_PENDENTE);
    }

    @Test
    void aceitar_responde_o_convite_e_da_o_acesso_com_o_papel_convidado() {
        Convite aceito = pendente(Papel.LEITOR).aceitoPor("bia@exemplo.com", false, AGORA);

        assertThat(aceito.situacao()).isEqualTo(SituacaoDoConvite.ACEITO);
        assertThat(aceito.respondidoEm()).isEqualTo(AGORA);

        Acesso acesso = aceito.acessoDe(20L, AGORA);
        assertThat(acesso.usuarioId()).isEqualTo(20L);
        assertThat(acesso.ambienteId()).isEqualTo(7L);
        assertThat(acesso.papel()).isEqualTo(Papel.LEITOR);
    }

    @Test
    void convite_pendente_nao_e_acesso() {
        assertThatThrownBy(() -> pendente(Papel.EDITOR).acessoDe(20L, AGORA))
                .isInstanceOf(RegraDeDominioException.class);
    }

    @Test
    void o_convite_de_outro_email_nao_se_aceita_nem_se_recusa_e_parece_nao_existir() {
        Convite convite = pendente(Papel.EDITOR);

        assertThatThrownBy(() -> convite.aceitoPor("caio@exemplo.com", false, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.NAO_ENCONTRADO);
        assertThatThrownBy(() -> convite.recusadoPor("caio@exemplo.com", AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.NAO_ENCONTRADO);
    }

    @Test
    void convite_e_resposta_unica() {
        Convite recusado = pendente(Papel.EDITOR).recusadoPor("bia@exemplo.com", AGORA);
        assertThat(recusado.situacao()).isEqualTo(SituacaoDoConvite.RECUSADO);

        assertThatThrownBy(() -> recusado.aceitoPor("bia@exemplo.com", false, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.CONVITE_NAO_PENDENTE);
        assertThatThrownBy(() -> recusado.canceladoPor(DONO, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.CONVITE_NAO_PENDENTE);
    }

    @Test
    void aceitar_um_ambiente_em_que_ja_se_esta_e_recusado() {
        assertThatThrownBy(() -> pendente(Papel.EDITOR).aceitoPor("bia@exemplo.com", true, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.JA_TEM_ACESSO);
    }

    @Test
    void so_o_dono_do_ambiente_cancela() {
        Convite cancelado = pendente(Papel.EDITOR).canceladoPor(DONO, AGORA);
        assertThat(cancelado.situacao()).isEqualTo(SituacaoDoConvite.CANCELADO);

        assertThatThrownBy(() -> pendente(Papel.EDITOR).canceladoPor(EDITOR, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.SEM_PERMISSAO);

        Acesso donoDeOutro = new Acesso(3L, 10L, 99L, Papel.DONO, ONTEM);
        assertThatThrownBy(() -> pendente(Papel.EDITOR).canceladoPor(donoDeOutro, AGORA))
                .extracting("codigo").isEqualTo(CodigoDeErro.SEM_PERMISSAO);
    }

    private static Convite pendente(Papel papel) {
        return new Convite(5L, 7L, "bia@exemplo.com", papel, SituacaoDoConvite.PENDENTE, 10L,
                ONTEM, null);
    }
}
