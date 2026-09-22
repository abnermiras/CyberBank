package br.com.cyberbank.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.junit.jupiter.api.Test;

class SessaoTest {

    private static final Instant NASCIMENTO = Instant.parse("2026-09-07T12:00:00Z");

    private Sessao sessao() {
        return Sessao.abrir(1L, "hash", "127.0.0.1", "Firefox", NASCIMENTO);
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

    @Test
    void so_a_dona_alcanca_a_sessao_e_a_de_outro_responde_como_inexistente() {
        assertThatCode(() -> sessao().exigirDo(1L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> sessao().exigirDo(2L))
                .isInstanceOf(RegraDeDominioException.class)
                .satisfies(e -> assertThat(((RegraDeDominioException) e).codigo())
                        .isEqualTo(CodigoDeErro.NAO_ENCONTRADO));
    }

    @Test
    void o_navegador_cabe_na_coluna_e_vazio_vira_ausente() {
        String longo = "x".repeat(Sessao.TAMANHO_MAXIMO_DO_NAVEGADOR + 50);

        assertThat(Sessao.abrir(1L, "h", "o", longo, NASCIMENTO).navegador())
                .hasSize(Sessao.TAMANHO_MAXIMO_DO_NAVEGADOR);
        assertThat(Sessao.abrir(1L, "h", "o", "  ", NASCIMENTO).navegador()).isNull();
        assertThat(Sessao.abrir(1L, "h", "o", null, NASCIMENTO).navegador()).isNull();
    }
}
