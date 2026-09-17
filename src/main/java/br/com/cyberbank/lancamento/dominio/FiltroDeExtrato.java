package br.com.cyberbank.lancamento.dominio;

public record FiltroDeExtrato(Long contaId, boolean somentePendentes) {

    public static FiltroDeExtrato tudo() {
        return new FiltroDeExtrato(null, false);
    }
}
