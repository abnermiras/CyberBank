package br.com.cyberbank.comum.erro;

/**
 * Excecao de dominio carrega o codigo do catalogo, e nada mais
 * (docs/01-arquitetura/padroes-de-codigo.md). Quem a traduz em problem+json e o
 * {@link TratadorDeErros}; controlador nao trata excecao.
 */
public class RegraDeDominioException extends RuntimeException {

    private final CodigoDeErro codigo;

    public RegraDeDominioException(CodigoDeErro codigo) {
        super(codigo.name());
        this.codigo = codigo;
    }

    public CodigoDeErro codigo() {
        return codigo;
    }
}
