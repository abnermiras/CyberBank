package br.com.cyberbank.comum.erro;

import java.util.List;

public class ValidacaoException extends RuntimeException {

    private final transient List<ErroDeValidacao> erros;

    public ValidacaoException(List<ErroDeValidacao> erros) {
        super(CodigoDeErro.VALIDACAO.name());
        this.erros = List.copyOf(erros);
    }

    public List<ErroDeValidacao> erros() {
        return erros;
    }
}
