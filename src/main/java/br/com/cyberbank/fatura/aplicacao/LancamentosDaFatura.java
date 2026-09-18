package br.com.cyberbank.fatura.aplicacao;

import java.time.LocalDate;
import java.util.List;

public record LancamentosDaFatura(
        long totalCentavos,
        LancamentoDaFatura saldoAnterior,
        List<CartaoComLancamentos> cartoes,
        LancamentoDaFatura roladoParaASeguinte) {

    public record CartaoComLancamentos(
            Long meioId,
            String nome,
            long totalCentavos,
            List<LancamentoDaFatura> itens) {
    }

    public record LancamentoDaFatura(
            Long id,
            LocalDate dataEvento,
            String descricao,
            String sentido,
            long valorCentavos,
            String situacao,
            String categoria,
            boolean doCiclo,
            Integer parcela,
            Integer parcelas) {
    }
}
