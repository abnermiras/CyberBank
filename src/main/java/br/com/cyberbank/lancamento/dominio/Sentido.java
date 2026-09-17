package br.com.cyberbank.lancamento.dominio;

public enum Sentido {
    ENTRADA,
    SAIDA;

    public Sentido invertido() {
        return this == ENTRADA ? SAIDA : ENTRADA;
    }

    public static Sentido de(long centavosComSinal) {
        return centavosComSinal >= 0 ? ENTRADA : SAIDA;
    }

    public long aplicadoA(long valorCentavos) {
        return this == ENTRADA ? valorCentavos : -valorCentavos;
    }
}
