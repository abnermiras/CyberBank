package br.com.cyberbank.ambiente.api;

import br.com.cyberbank.ambiente.dominio.Sessao;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseCookie;

/**
 * O cookie do ADR-0009: identificador OPACO, {@code HttpOnly}, {@code Secure} e
 * {@code SameSite=Lax}.
 *
 * <p>{@code Secure} nao tem interruptor, e nao ha "em desenvolvimento e http": sem TLS o cookie
 * viaja em claro e nada do resto importa (docs/01-arquitetura/seguranca.md). Desenho que so
 * funciona sem TLS e desenho nao testado.
 */
final class CookieDeSessao {

    static final String NOME = "cyberbank_sessao";

    private CookieDeSessao() {
    }

    static ResponseCookie novo(String identificador) {
        return base(identificador).maxAge(Sessao.DURACAO_ABSOLUTA).build();
    }

    static ResponseCookie expirado() {
        return base("").maxAge(0).build();
    }

    static String ler(HttpServletRequest requisicao) {
        if (requisicao.getCookies() == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : requisicao.getCookies()) {
            if (NOME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOME, valor)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/");
    }
}
