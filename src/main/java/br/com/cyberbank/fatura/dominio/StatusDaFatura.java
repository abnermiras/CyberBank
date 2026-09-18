package br.com.cyberbank.fatura.dominio;

public enum StatusDaFatura {

    FUTURA(false),
    ABERTA(true),
    FECHADA(false);

    private final boolean recebeCompraNova;

    StatusDaFatura(boolean recebeCompraNova) {
        this.recebeCompraNova = recebeCompraNova;
    }

    public boolean recebeCompraNova() {
        return recebeCompraNova;
    }
}
