package br.com.cyberbank.usuario.dominio;

import java.time.Duration;
import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

public record Sessao(
        Long id,
        Long usuarioId,
        String identificadorHash,
        Instant criadaEm,
        Instant ultimoUsoEm,
        Instant expiraEm,
        String origem,
        String navegador) {

    public static final Duration DURACAO_ABSOLUTA = Duration.ofDays(30);
    public static final Duration DURACAO_POR_INATIVIDADE = Duration.ofDays(7);
    public static final int TAMANHO_MAXIMO_DO_NAVEGADOR = 200;

    public static Sessao abrir(Long usuarioId, String identificadorHash, String origem,
            String navegador, Instant agora) {
        return new Sessao(null, usuarioId, identificadorHash, agora, agora,
                agora.plus(DURACAO_ABSOLUTA), origem, cabendo(navegador));
    }

    public boolean estaValida(Instant agora) {
        return agora.isBefore(expiraEm)
                && agora.isBefore(ultimoUsoEm.plus(DURACAO_POR_INATIVIDADE));
    }

    public Sessao usadaEm(Instant agora) {
        return new Sessao(id, usuarioId, identificadorHash, criadaEm, agora, expiraEm, origem,
                navegador);
    }

    public void exigirDo(Long outroUsuarioId) {
        if (!usuarioId.equals(outroUsuarioId)) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
    }

    private static String cabendo(String navegador) {
        if (navegador == null || navegador.isBlank()) {
            return null;
        }
        String limpo = navegador.trim();
        return limpo.length() > TAMANHO_MAXIMO_DO_NAVEGADOR
                ? limpo.substring(0, TAMANHO_MAXIMO_DO_NAVEGADOR)
                : limpo;
    }
}
