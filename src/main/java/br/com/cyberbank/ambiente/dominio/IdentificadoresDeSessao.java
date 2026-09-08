package br.com.cyberbank.ambiente.dominio;

public interface IdentificadoresDeSessao {

    /** O valor opaco que vai no cookie. Sem informacao dentro (ADR-0009). */
    String gerar();

    /** O que se guarda no banco. Copia do banco nao vira sessao viva de ninguem. */
    String hash(String identificador);
}
