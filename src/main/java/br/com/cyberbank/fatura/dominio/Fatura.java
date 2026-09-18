package br.com.cyberbank.fatura.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

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

    public boolean recebeCompraNova() {
        return status.recebeCompraNova();
    }

    public boolean naJanelaDoPagamentoEDaAbertura(NumerosDaFatura numeros) {
        return status == StatusDaFatura.FECHADA && numeros.aPagarCentavos() > 0;
    }
}
