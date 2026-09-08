package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;

/** O que se sabe sobre as falhas de uma chave — uma conta, ou uma origem. */
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
