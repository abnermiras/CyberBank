package br.com.cyberbank.categoria.dominio;

/**
 * Toda categoria tem sentido, e a invariante nao abre excecao para ninguem — nem para a
 * categoria de sistema. E por isso que cada operacao existe DUAS vezes.
 */
public enum Sentido {
    ENTRADA,
    SAIDA
}
