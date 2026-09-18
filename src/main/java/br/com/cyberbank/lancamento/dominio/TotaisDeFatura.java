package br.com.cyberbank.lancamento.dominio;

public record TotaisDeFatura(
        Long faturaId,
        long totalCentavos,
        long pagoCentavos,
        long roladoCentavos) {
}
