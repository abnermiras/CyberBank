package br.com.cyberbank.ambiente.dominio;

/**
 * Tres papeis, e so tres (docs/02-dominio/ambiente-financeiro.md). Na tela sao duas opcoes:
 * "autorizacao completa" e o EDITOR, "somente leitura" e o LEITOR. Convidar e excluir sao so
 * do DONO, e e isso que impede o ambiente de ficar orfao.
 */
public enum Papel {
    DONO,
    EDITOR,
    LEITOR;

    public boolean podeLancar() {
        return this != LEITOR;
    }

    public boolean podeConvidar() {
        return this == DONO;
    }
}
