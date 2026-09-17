package br.com.cyberbank.usuario.dominio;

import java.time.Duration;
import java.time.Instant;

public final class PoliticaDeLogin {

    public static final int FALHAS_ATE_BLOQUEIO = 5;
    public static final Duration BLOQUEIO = Duration.ofMinutes(15);
    private static final Duration ATRASO_BASE = Duration.ofMillis(250);
    private static final Duration ATRASO_MAXIMO = Duration.ofSeconds(4);

    private PoliticaDeLogin() {
    }

    public static String chaveDaConta(String email) {
        return "conta:" + email;
    }

    public static String chaveDaOrigem(String origem) {
        return "origem:" + origem;
    }

    public static Duration atrasoApos(int falhas) {
        if (falhas <= 0) {
            return Duration.ZERO;
        }
        long fator = 1L << Math.min(falhas - 1, 16);
        Duration atraso = ATRASO_BASE.multipliedBy(fator);
        return atraso.compareTo(ATRASO_MAXIMO) > 0 ? ATRASO_MAXIMO : atraso;
    }

    public static boolean bloqueia(int falhas) {
        return falhas >= FALHAS_ATE_BLOQUEIO;
    }

    public static Instant bloqueadoAte(Instant ultimaFalha) {
        return ultimaFalha.plus(BLOQUEIO);
    }
}
