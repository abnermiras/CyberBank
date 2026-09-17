package br.com.cyberbank.usuario.dominio;

public interface IdentificadoresDeSessao {

    String gerar();

    String hash(String identificador);
}
