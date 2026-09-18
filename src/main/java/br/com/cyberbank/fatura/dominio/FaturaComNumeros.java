package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;

public record FaturaComNumeros(
        Fatura fatura,
        NumerosDaFatura numeros,
        boolean temLancamentoProvisionado) {

    public boolean venceuSemQuitar(LocalDate hoje) {
        return fatura.status() == StatusDaFatura.FECHADA
                && fatura.dataVencimento().isBefore(hoje)
                && numeros.aPagarCentavos() > 0;
    }

    public boolean encerrouEAindaNaoLiquidou() {
        return fatura.status() == StatusDaFatura.FECHADA
                && numeros.encerrada()
                && temLancamentoProvisionado;
    }

    public boolean chegouAoFechamento(LocalDate hoje) {
        return fatura.status() == StatusDaFatura.ABERTA
                && !fatura.dataFechamento().isAfter(hoje);
    }
}
