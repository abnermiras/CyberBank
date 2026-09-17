package br.com.cyberbank.lancamento.dominio;

public enum Situacao {
    PREVISTO,
    PROVISIONADO,
    REALIZADO;

    public boolean entraNoSaldoRealizado() {
        return this != PREVISTO;
    }

    public boolean andaParaFrenteAte(Situacao destino) {
        return destino.ordinal() > this.ordinal();
    }
}
