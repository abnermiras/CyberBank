package br.com.cyberbank.meio.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class MeioTest {

    private static final Instant AGORA = Instant.parse("2026-09-16T12:00:00Z");
    private static final Long AMBIENTE = 7L;
    private static final Long CONTA = 3L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 16);

    @ParameterizedTest
    @CsvSource({
            "DEBITO,CORRENTE",
            "PIX,CORRENTE",
            "TED,CORRENTE",
            "DESCONTO_EM_FOLHA,CORRENTE",
            "BOLETO,CORRENTE",
            "DINHEIRO,CARTEIRA",
            "BENEFICIO,BENEFICIO",
            "CREDITO,CARTAO"
    })
    void cada_tipo_de_meio_move_exatamente_um_tipo_de_conta(TipoDeMeio meio, String conta) {
        assertThat(meio.servePara(conta)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(TipoDeMeio.class)
    void nenhum_meio_aponta_para_aplicacao_porque_nao_se_paga_com_ela(TipoDeMeio tipo) {
        assertThat(tipo.servePara("APLICACAO")).isFalse();
    }

    @Test
    void so_o_credito_tem_nome() {
        assertThat(TipoDeMeio.CREDITO.temNome())
                .as("o nome é a identidade do cartão: físico, virtual, adicional")
                .isTrue();
        assertThat(java.util.Arrays.stream(TipoDeMeio.values())
                .filter(t -> t != TipoDeMeio.CREDITO)
                .noneMatch(TipoDeMeio::temNome))
                .as("o par (conta, tipo) já identifica o meio em todo o resto")
                .isTrue();
    }

    @Test
    void nome_em_meio_que_nao_e_cartao_e_recusado_em_vez_de_ignorado() {
        assertThatThrownBy(() -> Meio.novo(AMBIENTE, "Pix da Nubank", TipoDeMeio.PIX, CONTA,
                "CORRENTE", AGORA))
                .as("o sistema não guarda calado um valor que ele não vai usar")
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void so_o_credito_se_repete_na_mesma_conta() {
        assertThat(TipoDeMeio.CREDITO.repetePorConta())
                .as("um contrato tem físico, virtual e adicional")
                .isTrue();
        assertThat(java.util.Arrays.stream(TipoDeMeio.values())
                .filter(t -> t != TipoDeMeio.CREDITO)
                .noneMatch(TipoDeMeio::repetePorConta))
                .as("uma conta tem no máximo um Pix, um débito, um boleto")
                .isTrue();
    }

    @Test
    void os_tipos_de_vocabulario_nao_mudam_regra_nenhuma() {
        var vocabulario = java.util.List.of(TipoDeMeio.DEBITO, TipoDeMeio.PIX,
                TipoDeMeio.TED, TipoDeMeio.DESCONTO_EM_FOLHA);

        assertThat(vocabulario).allSatisfy(tipo -> {
            assertThat(tipo.tipoDeContaExigido()).isEqualTo("CORRENTE");
            assertThat(tipo.separaAsDuasDatas()).isFalse();
            assertThat(tipo.dependeDeFatura()).isFalse();
            assertThat(tipo.temNome()).isFalse();
            assertThat(tipo.repetePorConta()).isFalse();
        });
    }

    @Test
    void a_carteira_so_aceita_dinheiro_e_o_dinheiro_so_serve_a_ela() {
        assertThat(TipoDeMeio.daConta("CARTEIRA"))
                .as("é isso que faz o dinheiro ser exclusivo, sem precisar de regra própria")
                .containsExactly(TipoDeMeio.DINHEIRO);
        assertThat(TipoDeMeio.daConta("APLICACAO"))
                .as("não se paga com uma aplicação: resgata-se antes")
                .isEmpty();
        assertThat(TipoDeMeio.daConta("CORRENTE"))
                .containsExactly(TipoDeMeio.DEBITO, TipoDeMeio.PIX, TipoDeMeio.TED,
                        TipoDeMeio.DESCONTO_EM_FOLHA, TipoDeMeio.BOLETO);
    }

    @Test
    void meio_de_tipo_incompativel_com_a_conta_e_recusado() {
        assertThatThrownBy(() -> Meio.novo(AMBIENTE, null, TipoDeMeio.DINHEIRO, CONTA, "CORRENTE", AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.MEIO_INCOMPATIVEL_COM_CONTA);
    }

    @Test
    void cartao_de_credito_ainda_nao_se_cadastra_porque_depende_da_fatura() {
        assertThatThrownBy(() -> Meio.novo(AMBIENTE, "Físico ****1234", TipoDeMeio.CREDITO, CONTA, "CARTAO", AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void em_meio_a_vista_as_duas_datas_sao_a_mesma() {
        Meio pix = Meio.novo(AMBIENTE, null, TipoDeMeio.PIX, CONTA, "CORRENTE", AGORA);

        assertThat(pix.dataEfeitoPara(DIA, null)).isEqualTo(DIA);
        assertThat(pix.dataEfeitoPara(DIA, DIA)).isEqualTo(DIA);
    }

    @Test
    void meio_a_vista_recusa_data_de_efeito_diferente_em_vez_de_corrigir_calado() {
        Meio pix = Meio.novo(AMBIENTE, null, TipoDeMeio.PIX, CONTA, "CORRENTE", AGORA);

        assertThatThrownBy(() -> pix.dataEfeitoPara(DIA, DIA.plusDays(4)))
                .as("o sistema não conserta valor informado pelo usuário")
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void o_boleto_e_o_unico_com_duas_datas_de_verdade() {
        Meio boleto = Meio.novo(AMBIENTE, null, TipoDeMeio.BOLETO, CONTA, "CORRENTE", AGORA);
        LocalDate vencimento = DIA.plusDays(10);

        assertThat(boleto.dataEfeitoPara(DIA, vencimento)).isEqualTo(vencimento);
        assertThat(boleto.dataEfeitoPara(DIA, null)).isEqualTo(DIA);
    }

    @Test
    void meio_inativo_nao_recebe_lancamento_novo() {
        Meio inativo = Meio.novo(AMBIENTE, null, TipoDeMeio.PIX, CONTA, "CORRENTE", AGORA)
                .comAtivacao(true);

        assertThatThrownBy(inativo::exigirAtivoParaLancar)
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.MEIO_INATIVO);
    }

    @Test
    void meio_com_lancamento_nao_se_exclui() {
        Meio meio = Meio.novo(AMBIENTE, null, TipoDeMeio.PIX, CONTA, "CORRENTE", AGORA);

        meio.exigirExcluivel(false);

        assertThatThrownBy(() -> meio.exigirExcluivel(true))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.MEIO_COM_LANCAMENTO);
    }

    @Test
    void o_lancamento_move_a_conta_do_meio_e_nao_outra() {
        Meio meio = Meio.novo(AMBIENTE, null, TipoDeMeio.PIX, CONTA, "CORRENTE", AGORA);

        meio.exigirDaConta(CONTA);

        assertThatThrownBy(() -> meio.exigirDaConta(99L))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.MEIO_INCOMPATIVEL_COM_CONTA);
    }
}
