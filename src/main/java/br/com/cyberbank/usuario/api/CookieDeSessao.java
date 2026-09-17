package br.com.cyberbank.usuario.api;

import br.com.cyberbank.usuario.dominio.Sessao;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseCookie;

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
