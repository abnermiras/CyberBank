package br.com.cyberbank.ambiente.api;

import java.util.Set;

import br.com.cyberbank.ambiente.aplicacao.ValidarSessaoUseCase;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolve o cookie e poe QUEM ESTA FALANDO no contexto, antes de qualquer transacao — e e
 * desse contexto que sai o {@code app.usuario_id} das politicas de RLS.
 *
 * <p>Tres rotas passam sem sessao, e so tres: cadastrar, entrar e sair.
 */
@Component
public class InterceptadorDeSessao implements HandlerInterceptor {

    private static final Set<String> ABERTAS = Set.of(
            "POST /api/v1/usuarios",
            "POST /api/v1/sessoes",
            "DELETE /api/v1/sessoes/atual");

    private final ValidarSessaoUseCase validarSessao;

    public InterceptadorDeSessao(ValidarSessaoUseCase validarSessao) {
        this.validarSessao = validarSessao;
    }

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler) {
        validarSessao.executar(CookieDeSessao.ler(requisicao))
                .ifPresent(ContextoDaRequisicao::definirUsuario);

        if (ContextoDaRequisicao.usuarioId() == null && !ehAberta(requisicao)) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO);
        }
        return true;
    }

    /** Thread de pool que guarda contexto de outra requisicao e sessao vazando. */
    @Override
    public void afterCompletion(HttpServletRequest requisicao, HttpServletResponse resposta,
            Object handler, Exception excecao) {
        ContextoDaRequisicao.limpar();
    }

    private boolean ehAberta(HttpServletRequest requisicao) {
        return ABERTAS.contains(requisicao.getMethod() + " " + requisicao.getRequestURI());
    }
}
