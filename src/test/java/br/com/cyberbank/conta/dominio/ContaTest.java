package br.com.cyberbank.conta.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContaTest {

    private static final Instant AGORA = Instant.parse("2026-09-16T12:00:00Z");
    private static final Long AMBIENTE = 7L;
    private static final LocalDate DIA = LocalDate.of(2026, 9, 16);

    @ParameterizedTest
    @EnumSource(TipoDeConta.class)
    void entra_em_caixa_implica_entra_no_fluxo_de_caixa(TipoDeConta tipo) {
        assertThat(!tipo.entraEmCaixa() || tipo.entraNoFluxoDeCaixa())
                .as("o contrário não vale: BENEFICIO e CARTAO são de fluxo e não são caixa")
                .isTrue();
    }

    @Test
    void so_corrente_e_carteira_sao_caixa() {
        assertThat(TipoDeConta.CORRENTE.entraEmCaixa()).isTrue();
        assertThat(TipoDeConta.CARTEIRA.entraEmCaixa()).isTrue();
        assertThat(TipoDeConta.BENEFICIO.entraEmCaixa())
                .as("saldo que só compra comida não paga um boleto")
                .isFalse();
        assertThat(TipoDeConta.CARTAO.entraEmCaixa())
                .as("o saldo da CARTAO é dívida, não dinheiro")
                .isFalse();
        assertThat(TipoDeConta.APLICACAO.entraEmCaixa()).isFalse();
    }

    @Test
    void aplicacao_e_a_unica_fora_do_fluxo_de_caixa() {
        assertThat(TipoDeConta.APLICACAO.entraNoFluxoDeCaixa())
                .as("mover dinheiro para a aplicação não é gasto, é guardar")
                .isFalse();
        assertThat(TipoDeConta.CARTAO.entraNoFluxoDeCaixa())
                .as("comprar no cartão é gasto da vida")
                .isTrue();
    }

    @Test
    void a_conta_nasce_com_os_dois_eixos_do_tipo_e_ativa() {
        Conta poupanca = Conta.nova(AMBIENTE, "Poupança do Itaú", TipoDeConta.APLICACAO, null, AGORA);

        assertThat(poupanca.entraNoFluxoDeCaixa()).isFalse();
        assertThat(poupanca.entraEmCaixa()).isFalse();
        assertThat(poupanca.inativa()).isFalse();
        assertThat(poupanca.ambienteId()).isEqualTo(AMBIENTE);
    }

    @Test
    void o_nome_e_obrigatorio() {
        assertThatThrownBy(() -> Conta.nova(AMBIENTE, "  ", TipoDeConta.CORRENTE, null, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void cartao_sem_ciclo_nao_nasce_porque_a_fatura_dele_nao_teria_datas() {
        assertThatThrownBy(() ->
                Conta.nova(AMBIENTE, "UltraVioleta", TipoDeConta.CARTAO, null, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void so_o_cartao_tem_ciclo_de_fatura() {
        assertThatThrownBy(() -> Conta.nova(AMBIENTE, "Nubank", TipoDeConta.CORRENTE,
                ContratoDeCartao.semLimite(5, 8, null), AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void o_limite_disponivel_e_o_limite_menos_a_divida() {
        Conta cartao = Conta.nova(AMBIENTE, "UltraVioleta", TipoDeConta.CARTAO,
                new ContratoDeCartao(1500000L, DIA, 5, 8, null), AGORA);

        assertThat(cartao.limiteDisponivelCentavos(356080L)).isEqualTo(1143920L);
    }

    @Test
    void o_limite_informado_carrega_a_data_em_que_foi_informado() {
        assertThatThrownBy(() -> new ContratoDeCartao(1500000L, null, 5, 8, null))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void cartao_nao_tem_saldo_de_abertura_e_e_a_unica_excecao() {
        Conta cartao = Conta.nova(AMBIENTE, "UltraVioleta", TipoDeConta.CARTAO,
                ContratoDeCartao.semLimite(5, 8, null), AGORA);

        assertThatThrownBy(() -> cartao.exigirSaldoInicialCompativel(50000L))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CARTAO_SEM_SALDO_INICIAL);
    }

    @Test
    void toda_conta_que_nao_e_cartao_aceita_saldo_de_abertura() {
        Conta corrente = Conta.nova(AMBIENTE, "Nubank", TipoDeConta.CORRENTE, null, AGORA);

        corrente.exigirSaldoInicialCompativel(1123600L);
        corrente.exigirSaldoInicialCompativel(null);
    }

    @Test
    void conta_inativa_nao_recebe_lancamento_do_usuario() {
        Conta inativa = Conta.nova(AMBIENTE, "Antiga", TipoDeConta.CORRENTE, null, AGORA)
                .comAtivacao(true);

        assertThatThrownBy(inativa::exigirAtivaParaLancar)
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CONTA_INATIVA);
    }

    @Test
    void beneficio_nao_e_origem_nem_destino_de_transferencia() {
        Conta vale = Conta.nova(AMBIENTE, "Vale-refeição", TipoDeConta.BENEFICIO, null, AGORA);

        assertThatThrownBy(vale::exigirTransferivel)
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.BENEFICIO_NAO_TRANSFERE);
    }

    @Test
    void conta_com_lancamento_nao_se_exclui_o_caminho_e_inativar() {
        Conta conta = Conta.nova(AMBIENTE, "Nubank", TipoDeConta.CORRENTE, null, AGORA);

        conta.exigirExcluivel(false);

        assertThatThrownBy(() -> conta.exigirExcluivel(true))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CONTA_COM_LANCAMENTO);
    }

    @Test
    void renomear_e_livre_e_o_tipo_nao_acompanha() {
        Conta conta = Conta.nova(AMBIENTE, "Nubank", TipoDeConta.CORRENTE, null, AGORA);
        Conta renomeada = conta.renomeada("  Nubank Conta  ");

        assertThat(renomeada.nome()).isEqualTo("Nubank Conta");
        assertThat(renomeada.tipo()).isEqualTo(conta.tipo());
        assertThat(renomeada.criadaEm()).isEqualTo(conta.criadaEm());
    }
}
