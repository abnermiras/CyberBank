package br.com.cyberbank.ambiente.dominio;

public enum Papel {
    DONO,
    EDITOR,
    LEITOR;

    public boolean podeLancar() {
        return this != LEITOR;
    }

    public boolean podeRenomearAmbiente() {
        return this != LEITOR;
    }

    public boolean podeConvidar() {
        return this == DONO;
    }
}
