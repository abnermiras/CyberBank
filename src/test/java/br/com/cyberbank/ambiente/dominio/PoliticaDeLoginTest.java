package br.com.cyberbank.ambiente.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class PoliticaDeLoginTest {

    private static final Instant AGORA = Instant.parse("2026-09-07T12:00:00Z");

    @Test
    void a_primeira_tentativa_nao_atrasa() {
        assertThat(PoliticaDeLogin.atrasoApos(0)).isZero();
    }

    @Test
    void o_atraso_e_progressivo_e_tem_teto() {
        Duration primeira = PoliticaDeLogin.atrasoApos(1);
        Duration segunda = PoliticaDeLogin.atrasoApos(2);

        assertThat(segunda).isEqualTo(primeira.multipliedBy(2));
        assertThat(PoliticaDeLogin.atrasoApos(40)).isEqualTo(PoliticaDeLogin.atrasoApos(30));
    }

    @Test
    void bloqueia_na_enesima_falha_e_solta_pelo_tempo() {
        Tentativas noLimite = new Tentativas(PoliticaDeLogin.FALHAS_ATE_BLOQUEIO, AGORA);

        assertThat(noLimite.bloqueadaEm(AGORA)).isTrue();
        assertThat(noLimite.bloqueadaEm(AGORA.plus(PoliticaDeLogin.BLOQUEIO))).isFalse();
    }

    @Test
    void uma_falha_a_menos_nao_bloqueia() {
        Tentativas quase = new Tentativas(PoliticaDeLogin.FALHAS_ATE_BLOQUEIO - 1, AGORA);
        assertThat(quase.bloqueadaEm(AGORA)).isFalse();
    }
}
