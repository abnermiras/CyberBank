package br.com.cyberbank.evento.dominio;

import java.util.Objects;

public record Alvo(TipoDeAlvo tipo, Long id) {

    public Alvo {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(id);
    }

    public static Alvo lancamento(Long id) {
        return new Alvo(TipoDeAlvo.LANCAMENTO, id);
    }

    public static Alvo conta(Long id) {
        return new Alvo(TipoDeAlvo.CONTA, id);
    }

    public static Alvo meio(Long id) {
        return new Alvo(TipoDeAlvo.MEIO, id);
    }

    public static Alvo categoria(Long id) {
        return new Alvo(TipoDeAlvo.CATEGORIA, id);
    }

    public static Alvo fatura(Long id) {
        return new Alvo(TipoDeAlvo.FATURA, id);
    }
}
