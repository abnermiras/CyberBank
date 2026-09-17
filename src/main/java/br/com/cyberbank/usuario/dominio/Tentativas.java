package br.com.cyberbank.usuario.dominio;

import java.time.Instant;

public record Tentativas(int falhas, Instant ultimaFalha) {

    public static final Tentativas NENHUMA = new Tentativas(0, null);

    public boolean bloqueadaEm(Instant agora) {
        return ultimaFalha != null
                && PoliticaDeLogin.bloqueia(falhas)
                && agora.isBefore(PoliticaDeLogin.bloqueadoAte(ultimaFalha));
    }

    public Tentativas maisUma(Instant agora) {
        return new Tentativas(falhas + 1, agora);
    }
}
