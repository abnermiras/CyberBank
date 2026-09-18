package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record FaturaComNumeros(
        Fatura fatura,
        NumerosDaFatura numeros,
        boolean temLancamentoProvisionado) {

    public static Optional<FaturaComNumeros> aQueImporta(List<FaturaComNumeros> doCartao) {
        Optional<FaturaComNumeros> devendo = doCartao.stream()
                .filter(f -> f.fatura().naJanelaDoPagamentoEDaAbertura(f.numeros()))
                .max(Comparator.comparing(f -> f.fatura().competencia()));

        return devendo.isPresent() ? devendo : doCartao.stream()
                .filter(f -> f.fatura().recebeCompraNova())
                .findFirst();
    }

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
