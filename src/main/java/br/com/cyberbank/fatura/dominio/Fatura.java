package br.com.cyberbank.fatura.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

public record Fatura(
        Long id,
        Long ambienteId,
        Long contaId,
        YearMonth competencia,
        LocalDate dataFechamento,
        LocalDate dataVencimento,
        StatusDaFatura status,
        Instant criadaEm) {

    public static Fatura nova(Long ambienteId, Long contaId, CicloDaFatura ciclo,
            YearMonth competencia, StatusDaFatura status, Instant agora) {

        return new Fatura(null, ambienteId, contaId, competencia,
                ciclo.fechamentoDe(competencia), ciclo.vencimentoDe(competencia), status, agora);
    }

    public static Fatura doCicloCorrente(Long ambienteId, Long contaId, CicloDaFatura ciclo,
            LocalDate hoje, Instant agora) {

        return nova(ambienteId, contaId, ciclo, ciclo.competenciaCorrenteEm(hoje),
                StatusDaFatura.ABERTA, agora);
    }

    public Fatura fechada() {
        exigirStatus(StatusDaFatura.ABERTA);
        return comStatus(StatusDaFatura.FECHADA);
    }

    public Fatura abertaPeloCiclo() {
        exigirStatus(StatusDaFatura.FUTURA);
        return comStatus(StatusDaFatura.ABERTA);
    }

    public Fatura seguinte(CicloDaFatura ciclo, StatusDaFatura status, Instant agora) {
        return nova(ambienteId, contaId, ciclo, competencia.plusMonths(1), status, agora);
    }

    public boolean recebeCompraNova() {
        return status.recebeCompraNova();
    }

    public boolean naJanelaDoPagamentoEDaAbertura(NumerosDaFatura numeros) {
        return status == StatusDaFatura.FECHADA && numeros.aPagarCentavos() > 0;
    }

    public void exigirQueRecebaPagamento(NumerosDaFatura numeros) {
        if (!naJanelaDoPagamentoEDaAbertura(numeros)) {
            throw new RegraDeDominioException(CodigoDeErro.FATURA_NAO_RECEBE_PAGAMENTO);
        }
    }

    public Fatura abertaPeloUsuario(NumerosDaFatura numeros, boolean ehAUltimaFechada) {
        if (!ehAUltimaFechada || !naJanelaDoPagamentoEDaAbertura(numeros)) {
            throw new RegraDeDominioException(CodigoDeErro.FATURA_NAO_ABRE);
        }
        return comStatus(StatusDaFatura.ABERTA);
    }

    public Fatura devolvidaAFutura() {
        exigirStatus(StatusDaFatura.ABERTA);
        return comStatus(StatusDaFatura.FUTURA);
    }

    private Fatura comStatus(StatusDaFatura novo) {
        return new Fatura(id, ambienteId, contaId, competencia, dataFechamento, dataVencimento,
                novo, criadaEm);
    }

    private void exigirStatus(StatusDaFatura esperado) {
        if (status != esperado) {
            throw new RegraDeDominioException(CodigoDeErro.FATURA_FORA_DO_CICLO);
        }
    }
}
