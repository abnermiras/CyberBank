package br.com.cyberbank.ambiente.api;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.cyberbank.ambiente.aplicacao.ResolverAmbienteAtivoUseCase;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InterceptadorDeAmbiente implements HandlerInterceptor {

    private static final Pattern SOB_AMBIENTE =
            Pattern.compile("^/api/v1/ambientes/(\\d+)(/.*)?$");

    private static final Set<String> METODOS_DE_LEITURA = Set.of("GET", "HEAD", "OPTIONS");

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

        resolverAmbiente.executar(usuarioId, ambienteId, alteraODado(requisicao, handler));
        ContextoDaRequisicao.definirAmbiente(ambienteId);
        return true;
    }

    private static boolean alteraODado(HttpServletRequest requisicao, Object handler) {
        if (METODOS_DE_LEITURA.contains(requisicao.getMethod())) {
            return false;
        }
        return !(handler instanceof HandlerMethod metodo
                && metodo.hasMethodAnnotation(AbertoAoLeitor.class));
    }

    private Long lerId(String bruto) {
        try {
            return Long.valueOf(bruto);
        } catch (NumberFormatException e) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
    }
}
