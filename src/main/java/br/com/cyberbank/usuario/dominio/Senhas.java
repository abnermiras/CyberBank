package br.com.cyberbank.usuario.dominio;

public interface Senhas {

    String hash(String senha);

    boolean confere(String senha, String hash);

    boolean conferirEmVao(String senha);

    boolean estaAbaixoDoPadrao(String hash);
}
