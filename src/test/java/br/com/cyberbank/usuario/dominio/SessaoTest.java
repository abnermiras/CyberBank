package br.com.cyberbank.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class SessaoTest {

    private static final Instant NASCIMENTO = Instant.parse("2026-09-07T12:00:00Z");

    private Sessao sessao() {
        return Sessao.abrir(1L, "hash", "127.0.0.1", NASCIMENTO);
    }

    @Test
    void vale_enquanto_as_duas_expiracoes_valem() {
        assertThat(sessao().estaValida(NASCIMENTO.plus(Duration.ofHours(1)))).isTrue();
    }

    @Test
    void a_absoluta_derruba_a_sessao_mesmo_com_uso_recente() {
        Instant quaseNoFim = NASCIMENTO.plus(Sessao.DURACAO_ABSOLUTA).minus(Duration.ofMinutes(1));
        Sessao usadaAgorinha = sessao().usadaEm(quaseNoFim);

        assertThat(usadaAgorinha.estaValida(quaseNoFim)).isTrue();
        assertThat(usadaAgorinha.estaValida(NASCIMENTO.plus(Sessao.DURACAO_ABSOLUTA))).isFalse();
    }

    @Test
    void a_inatividade_derruba_a_sessao_mesmo_dentro_da_absoluta() {
        Instant depoisDeSumir = NASCIMENTO.plus(Sessao.DURACAO_POR_INATIVIDADE).plusSeconds(1);

        assertThat(depoisDeSumir).isBefore(NASCIMENTO.plus(Sessao.DURACAO_ABSOLUTA));
        assertThat(sessao().estaValida(depoisDeSumir)).isFalse();
    }

    @Test
    void usar_empurra_a_inatividade_e_nunca_a_absoluta() {
        Instant maisTarde = NASCIMENTO.plus(Duration.ofDays(5));
        Sessao usada = sessao().usadaEm(maisTarde);

        assertThat(usada.ultimoUsoEm()).isEqualTo(maisTarde);
        assertThat(usada.expiraEm()).isEqualTo(sessao().expiraEm());
    }
}
