package br.com.cyberbank.ambiente.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.cyberbank.ambiente.aplicacao.ResolverAmbienteAtivoUseCase;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * O ambiente ativo vem do CAMINHO da URL, e so vira contexto depois de o acesso do usuario ser
 * validado (ADR-0002, docs/01-arquitetura/seguranca.md). Nenhum controlador recebe
 * {@code ambienteId} de corpo ou de query — se recebesse, seria o caminho sem validacao.
 *
 * <p>Ambiente inexistente e ambiente sem acesso respondem a MESMA coisa: 404.
 */
@Component
public class InterceptadorDeAmbiente implements HandlerInterceptor {

    private static final Pattern SOB_AMBIENTE =
            Pattern.compile("^/api/v1/ambientes/(\\d+)(/.*)?$");

    private final ResolverAmbienteAtivoUseCase resolverAmbiente;

    public InterceptadorDeAmbiente(ResolverAmbienteAtivoUseCase resolverAmbiente) {
        this.resolverAmbiente = resolverAmbiente;
    }

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler) {
        Matcher caminho = SOB_AMBIENTE.matcher(requisicao.getRequestURI());
        if (!caminho.matches()) {
            return true;
        }

        Long ambienteId = lerId(caminho.group(1));
        Long usuarioId = ContextoDaRequisicao.usuarioId();
        if (usuarioId == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO);
        }

        resolverAmbiente.executar(usuarioId, ambienteId);
        ContextoDaRequisicao.definirAmbiente(ambienteId);
        return true;
    }

    private Long lerId(String bruto) {
        try {
            return Long.valueOf(bruto);
        } catch (NumberFormatException e) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
    }
}
