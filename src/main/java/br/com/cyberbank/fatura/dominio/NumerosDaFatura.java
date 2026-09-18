package br.com.cyberbank.fatura.dominio;

public record NumerosDaFatura(long totalCentavos, long pagoCentavos, long roladoCentavos) {

    public static final NumerosDaFatura VAZIA = new NumerosDaFatura(0, 0, 0);

    public long aPagarCentavos() {
        return totalCentavos - pagoCentavos - roladoCentavos;
    }

    public boolean encerrada() {
        return aPagarCentavos() <= 0;
    }

    public boolean rolada() {
        return roladoCentavos > 0;
    }

    public boolean quitada() {
        return pagoCentavos >= totalCentavos && totalCentavos > 0;
    }

    public boolean parcial() {
        return pagoCentavos > 0 && pagoCentavos < totalCentavos;
    }

    public boolean emAberto() {
        return pagoCentavos == 0 && roladoCentavos == 0;
    }
}
