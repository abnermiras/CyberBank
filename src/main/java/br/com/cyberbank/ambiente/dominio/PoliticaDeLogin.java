package br.com.cyberbank.ambiente.dominio;

import java.time.Duration;
import java.time.Instant;

/**
 * O login e a superficie mais atacada: e a unica porta que aceita tentativa de quem nao tem
 * nada (docs/01-arquitetura/seguranca.md). Duas das quatro regras moram aqui, e as duas sao
 * aritmetica — por isso o teste delas roda sem Spring e sem banco.
 *
 * <p>A contagem e por CONTA e por ORIGEM, e as duas valem: so por conta, um atacante varre mil
 * contas com uma tentativa cada; so por origem, ele troca de origem.
 */
public final class PoliticaDeLogin {

    public static final int FALHAS_ATE_BLOQUEIO = 5;
    public static final Duration BLOQUEIO = Duration.ofMinutes(15);
    private static final Duration ATRASO_BASE = Duration.ofMillis(250);
    private static final Duration ATRASO_MAXIMO = Duration.ofSeconds(4);

    private PoliticaDeLogin() {
    }

    /** Atraso progressivo: dobra a cada falha, com teto. A primeira falha nao atrasa nada. */
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

    /** O desbloqueio e pelo TEMPO — nunca por uma tela que o proprio atacante alcanca. */
    public static Instant bloqueadoAte(Instant ultimaFalha) {
        return ultimaFalha.plus(BLOQUEIO);
    }
}
