package br.com.cyberbank.lancamento.aplicacao;

import br.com.cyberbank.lancamento.dominio.Lancamento;

public record DetalheDoLancamento(
        Lancamento lancamento,
        ContaDoLancamento conta,
        MeioDoLancamento meio,
        CategoriaDoLancamento categoria,
        FaturaDoLancamento fatura,
        SerieDoLancamento serie,
        String autor,
        Long outroLadoDaTransferenciaId,
        Long estornadoPorId) {

    public record ContaDoLancamento(Long id, String nome, String tipo) {
    }

    public record MeioDoLancamento(Long id, String tipo, String nome) {
    }

    public record CategoriaDoLancamento(Long id, String nome, RaizDaCategoria raiz) {
    }

    public record RaizDaCategoria(Long id, String nome, String cor) {
    }

    public record FaturaDoLancamento(Long id, String competencia, String status,
            String dataFechamento, String dataVencimento) {
    }

    public record SerieDoLancamento(Long parcelamentoId, int numero, int parcelas,
            long valorDaCompraCentavos, String dataDaCompra) {
    }
}
