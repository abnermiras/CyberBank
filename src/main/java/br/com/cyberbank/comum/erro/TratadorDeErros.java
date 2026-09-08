package br.com.cyberbank.comum.erro;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * O tratador UNICO: toda excecao de dominio vira problem+json aqui, e em lugar nenhum mais
 * (docs/01-arquitetura/padroes-de-codigo.md). try/catch em controlador e onde o mapeamento
 * erro-para-resposta comeca a divergir entre endpoints.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);
    private static final String BASE_DO_TIPO = "https://cyberbank/erros/";

    @ExceptionHandler(RegraDeDominioException.class)
    public ProblemDetail regraDeDominio(RegraDeDominioException e) {
        return problema(e.codigo(), null);
    }

    @ExceptionHandler(ValidacaoException.class)
    public ProblemDetail validacao(ValidacaoException e) {
        ProblemDetail problema = problema(CodigoDeErro.VALIDACAO, null);
        problema.setProperty("erros", e.erros());
        return problema;
    }

    /**
     * 500 NUNCA carrega detalhe: sem stack trace, sem mensagem de excecao, sem SQL
     * (docs/04-api/erros.md). O que vai para o cliente e um identificador de ocorrencia; o
     * resto vai para o log — que obedece docs/01-arquitetura/seguranca.md.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        String ocorrencia = UUID.randomUUID().toString();
        LOG.error("Falha inesperada. ocorrencia={}", ocorrencia, e);

        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problema.setType(URI.create(BASE_DO_TIPO + "ERRO_INTERNO"));
        problema.setTitle("Falha nossa");
        problema.setProperty("codigo", "ERRO_INTERNO");
        problema.setProperty("ocorrencia", ocorrencia);
        return problema;
    }

    private ProblemDetail problema(CodigoDeErro codigo, List<ErroDeValidacao> erros) {
        ProblemDetail problema = ProblemDetail.forStatus(codigo.status());
        problema.setType(URI.create(BASE_DO_TIPO + codigo.name()));
        problema.setTitle(codigo.titulo());
        problema.setProperty("codigo", codigo.name());
        if (erros != null) {
            problema.setProperty("erros", erros);
        }
        return problema;
    }
}
