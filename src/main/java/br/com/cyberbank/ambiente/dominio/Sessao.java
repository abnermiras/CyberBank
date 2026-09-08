package br.com.cyberbank.ambiente.dominio;

import java.time.Duration;
import java.time.Instant;

/**
 * Sessao com estado no servidor (ADR-0009). O cookie carrega um identificador OPACO; aqui fica
 * o hash dele, e o estado — quem, quando nasceu, quando foi usada e por onde entrou.
 *
 * <p>Duas expiracoes, e AS DUAS VALEM: a absoluta, contada da criacao, e a por inatividade,
 * contada do ultimo uso.
 */
public record Sessao(
        Long id,
        Long usuarioId,
        String identificadorHash,
        Instant criadaEm,
        Instant ultimoUsoEm,
        Instant expiraEm,
        String origem) {

    public static final Duration DURACAO_ABSOLUTA = Duration.ofDays(30);
    public static final Duration DURACAO_POR_INATIVIDADE = Duration.ofDays(7);

    public static Sessao abrir(Long usuarioId, String identificadorHash, String origem, Instant agora) {
        return new Sessao(null, usuarioId, identificadorHash, agora, agora,
                agora.plus(DURACAO_ABSOLUTA), origem);
    }

    public boolean estaValida(Instant agora) {
        return agora.isBefore(expiraEm)
                && agora.isBefore(ultimoUsoEm.plus(DURACAO_POR_INATIVIDADE));
    }

    /** Cada requisicao autenticada empurra a inatividade — e nunca a expiracao absoluta. */
    public Sessao usadaEm(Instant agora) {
        return new Sessao(id, usuarioId, identificadorHash, criadaEm, agora, expiraEm, origem);
    }
}
