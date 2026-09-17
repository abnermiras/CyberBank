package br.com.cyberbank.meio.dominio;

import java.util.Arrays;
import java.util.List;

public enum TipoDeMeio {

    DEBITO("CORRENTE", false),
    CREDITO("CARTAO", false),
    PIX("CORRENTE", false),
    TED("CORRENTE", false),
    DESCONTO_EM_FOLHA("CORRENTE", false),
    DINHEIRO("CARTEIRA", false),
    BENEFICIO("BENEFICIO", false),
    BOLETO("CORRENTE", true);

    private final String tipoDeContaExigido;
    private final boolean separaAsDuasDatas;

    TipoDeMeio(String tipoDeContaExigido, boolean separaAsDuasDatas) {
        this.tipoDeContaExigido = tipoDeContaExigido;
        this.separaAsDuasDatas = separaAsDuasDatas;
    }

    public String tipoDeContaExigido() {
        return tipoDeContaExigido;
    }

    public boolean separaAsDuasDatas() {
        return separaAsDuasDatas;
    }

    public boolean servePara(String tipoDaConta) {
        return tipoDeContaExigido.equals(tipoDaConta);
    }

    public boolean dependeDeFatura() {
        return this == CREDITO;
    }

    public boolean temNome() {
        return this == CREDITO;
    }

    public boolean repetePorConta() {
        return this == CREDITO;
    }

    public static List<TipoDeMeio> daConta(String tipoDaConta) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.servePara(tipoDaConta))
                .toList();
    }
}
