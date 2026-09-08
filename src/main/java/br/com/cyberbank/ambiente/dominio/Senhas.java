package br.com.cyberbank.ambiente.dominio;

/** Argon2id, com parametros calibrados no host (docs/01-arquitetura/seguranca.md). */
public interface Senhas {

    String hash(String senha);

    boolean confere(String senha, String hash);

    /**
     * Gasta o MESMO tempo de uma conferencia de verdade, e responde sempre falso.
     *
     * <p>Existe por uma razao so: e-mail inexistente e senha errada tem que ter a mesma
     * resposta E O MESMO TEMPO. Sem isto, a diferenca de milissegundos entre "nao achei o
     * usuario" e "conferi o Argon2id" entrega a lista de usuarios antes de qualquer senha ser
     * tentada.
     */
    boolean conferirEmVao(String senha);
}
