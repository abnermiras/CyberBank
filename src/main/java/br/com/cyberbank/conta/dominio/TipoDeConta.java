package br.com.cyberbank.conta.dominio;

public enum TipoDeConta {

    CORRENTE(true, true, true),
    CARTEIRA(true, true, true),
    APLICACAO(false, false, true),
    BENEFICIO(true, false, true),
    CARTAO(true, false, false);

    private final boolean entraNoFluxoDeCaixa;
    private final boolean entraEmCaixa;
    private final boolean aceitaSaldoInicial;

    TipoDeConta(boolean entraNoFluxoDeCaixa, boolean entraEmCaixa, boolean aceitaSaldoInicial) {
        this.entraNoFluxoDeCaixa = entraNoFluxoDeCaixa;
        this.entraEmCaixa = entraEmCaixa;
        this.aceitaSaldoInicial = aceitaSaldoInicial;
    }

    public boolean entraNoFluxoDeCaixa() {
        return entraNoFluxoDeCaixa;
    }

    public boolean entraEmCaixa() {
        return entraEmCaixa;
    }

    public boolean aceitaSaldoInicial() {
        return aceitaSaldoInicial;
    }

    public boolean aceitaTransferencia() {
        return this != BENEFICIO;
    }

    public boolean ehContratoDeCartao() {
        return this == CARTAO;
    }
}
