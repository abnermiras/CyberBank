package br.com.cyberbank.fatura.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

class AgendaDoCicloTest {

    private static final Instant AGORA = Instant.parse("2027-02-10T03:05:00Z");
    private static final CicloDaFatura VENCE_DIA_5_FECHA_8_ANTES = new CicloDaFatura(5, 8);

    @Test
    void rodada_que_nao_tem_o_que_fazer_nao_devolve_passo_nenhum() {
        LocalDate hoje = LocalDate.of(2026, 9, 18);

        assertThat(AgendaDoCiclo.proximoPasso(List.of(aberta(YearMonth.of(2026, 10), 30000)), hoje))
                .as("a rotina roda todo dia, e Diário cheio de 'nada aconteceu' é Diário que ninguém lê")
                .isEmpty();
    }

    @Test
    void a_aberta_que_chegou_na_data_de_fechamento_fecha() {
        FaturaComNumeros outubro = aberta(YearMonth.of(2026, 10), 30000);

        PassoDoCiclo passo = AgendaDoCiclo
                .proximoPasso(List.of(outubro), outubro.fatura().dataFechamento())
                .orElseThrow();

        assertThat(passo.tipo()).isEqualTo(PassoDoCiclo.TipoDePasso.FECHAR);
        assertThat(passo.fatura().competencia()).isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    void o_usuario_tem_o_dia_inteiro_do_vencimento_para_pagar() {
        FaturaComNumeros outubro = fechada(YearMonth.of(2026, 10), 100000, 0, 0);

        assertThat(AgendaDoCiclo.proximoPasso(
                List.of(outubro, aberta(YearMonth.of(2026, 11), 0)),
                outubro.fatura().dataVencimento()))
                .as("a rotina só encontra a fatura em aberto no dia seguinte")
                .isEmpty();
    }

    @Test
    void no_dia_seguinte_ao_vencimento_o_que_sobrou_rola_datado_no_vencimento() {
        FaturaComNumeros outubro = fechada(YearMonth.of(2026, 10), 161060, 80000, 0);
        LocalDate diaSeguinte = outubro.fatura().dataVencimento().plusDays(1);

        PassoDoCiclo passo = AgendaDoCiclo
                .proximoPasso(List.of(outubro, aberta(YearMonth.of(2026, 11), 110010)), diaSeguinte)
                .orElseThrow();

        assertThat(passo.tipo()).isEqualTo(PassoDoCiclo.TipoDePasso.ROLAR);
        assertThat(passo.valorCentavos())
                .as("rola o que faltava: total menos pago menos rolado")
                .isEqualTo(81060);
        assertThat(passo.dia())
                .as("a fatura nova mostra o saldo anterior com a data que o banco usaria")
                .isEqualTo(outubro.fatura().dataVencimento());
    }

    @Test
    void a_rolagem_disparada_por_correcao_e_datada_no_dia_em_que_a_rotina_rodou() {
        FaturaComNumeros jaRolada = fechada(YearMonth.of(2026, 10), 200000, 80000, 81060);
        LocalDate hoje = LocalDate.of(2026, 12, 20);

        PassoDoCiclo passo = AgendaDoCiclo
                .proximoPasso(List.of(jaRolada, aberta(YearMonth.of(2027, 1), 0)), hoje)
                .orElseThrow();

        assertThat(passo.tipo()).isEqualTo(PassoDoCiclo.TipoDePasso.ROLAR);
        assertThat(passo.dia())
                .as("o vencimento antigo está num mês já vivido: pôr um lançamento lá seria"
                        + " reescrever o passado")
                .isEqualTo(hoje);
    }

    @Test
    void o_pi_desligado_dois_ciclos_rola_janeiro_antes_de_fechar_fevereiro() {
        FaturaComNumeros janeiro = fechada(YearMonth.of(2027, 1), 100000, 0, 0);
        FaturaComNumeros fevereiro = aberta(YearMonth.of(2027, 2), 50000);
        LocalDate hoje = LocalDate.of(2027, 2, 10);

        PassoDoCiclo passo = AgendaDoCiclo.proximoPasso(List.of(fevereiro, janeiro), hoje)
                .orElseThrow();

        assertThat(passo.tipo())
                .as("encerrar vem antes de fechar: o vencimento de janeiro (05/01) é anterior"
                        + " à dataFechamento de fevereiro (25/01)")
                .isEqualTo(PassoDoCiclo.TipoDePasso.ROLAR);
        assertThat(passo.fatura().competencia()).isEqualTo(YearMonth.of(2027, 1));
        assertThat(janeiro.fatura().dataVencimento())
                .isBefore(fevereiro.fatura().dataFechamento());
    }

    @Test
    void a_fatura_quitada_que_ainda_tem_lancamento_provisionado_encerra() {
        FaturaComNumeros quitada = new FaturaComNumeros(
                fatura(YearMonth.of(2026, 10), StatusDaFatura.FECHADA),
                new NumerosDaFatura(100000, 100000, 0), true);

        PassoDoCiclo passo = AgendaDoCiclo
                .proximoPasso(List.of(quitada), LocalDate.of(2026, 10, 3)).orElseThrow();

        assertThat(passo.tipo())
                .as("o pagamento agendado realiza pela data, e é aqui que a fatura que ele"
                        + " quitou deixa de ser PROVISIONADO")
                .isEqualTo(PassoDoCiclo.TipoDePasso.ENCERRAR);
    }

    @Test
    void encerrar_e_idempotente_a_fatura_ja_liquidada_nao_produz_passo() {
        FaturaComNumeros jaLiquidada = new FaturaComNumeros(
                fatura(YearMonth.of(2026, 10), StatusDaFatura.FECHADA),
                new NumerosDaFatura(100000, 100000, 0), false);

        assertThat(AgendaDoCiclo.proximoPasso(List.of(jaLiquidada), LocalDate.of(2026, 12, 1)))
                .isEmpty();
    }

    @Test
    void a_fatura_rolada_nao_rola_de_novo_e_e_isso_que_impede_o_par_por_dia_para_sempre() {
        FaturaComNumeros rolada = new FaturaComNumeros(
                fatura(YearMonth.of(2026, 10), StatusDaFatura.FECHADA),
                new NumerosDaFatura(161060, 80000, 81060), false);

        assertThat(AgendaDoCiclo.proximoPasso(List.of(rolada), LocalDate.of(2026, 12, 1)))
                .as("o a pagar dela é zero, e o gatilho é o número — não um carimbo")
                .isEmpty();
    }

    @Test
    void fatura_futura_nao_produz_passo_nenhum() {
        FaturaComNumeros futura = new FaturaComNumeros(
                fatura(YearMonth.of(2027, 3), StatusDaFatura.FUTURA), NumerosDaFatura.VAZIA, false);

        assertThat(AgendaDoCiclo.proximoPasso(List.of(futura), LocalDate.of(2027, 2, 10)))
                .as("ela existe só para segurar parcela de mês que ainda não chegou")
                .isEmpty();
    }

    private static FaturaComNumeros aberta(YearMonth competencia, long totalCentavos) {
        return new FaturaComNumeros(fatura(competencia, StatusDaFatura.ABERTA),
                new NumerosDaFatura(totalCentavos, 0, 0), totalCentavos > 0);
    }

    private static FaturaComNumeros fechada(YearMonth competencia, long total, long pago,
            long rolado) {
        return new FaturaComNumeros(fatura(competencia, StatusDaFatura.FECHADA),
                new NumerosDaFatura(total, pago, rolado), true);
    }

    private static Fatura fatura(YearMonth competencia, StatusDaFatura status) {
        Fatura nova = Fatura.nova(7L, 9L, VENCE_DIA_5_FECHA_8_ANTES, competencia, status, AGORA);

        return new Fatura((long) (competencia.getYear() * 100 + competencia.getMonthValue()),
                nova.ambienteId(), nova.contaId(), nova.competencia(), nova.dataFechamento(),
                nova.dataVencimento(), nova.status(), nova.criadaEm());
    }
}
