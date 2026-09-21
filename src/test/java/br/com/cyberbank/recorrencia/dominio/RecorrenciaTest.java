package br.com.cyberbank.recorrencia.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RecorrenciaTest {

    private static final Instant AGORA = Instant.parse("2026-09-20T12:00:00Z");
    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);

    @Test
    void a_recorrencia_nasce_ativa_e_sem_valor_total() {
        Recorrencia netflix = de(3990, 12);

        assertThat(netflix.ativa()).isTrue();
        assertThat(netflix.valorCentavos())
                .as("o valor é o da ocorrência: perguntar o total da Netflix não faz sentido")
                .isEqualTo(3990);
    }

    @ParameterizedTest
    @ValueSource(ints = { 29, 30, 31 })
    void o_dia_que_nao_existe_no_mes_cai_no_ultimo(int dia) {
        assertThat(de(3990, dia).dataNaCompetencia(YearMonth.of(2026, 2)))
                .as("fevereiro não tem dia " + dia + ", e a cobrança não pode sumir")
                .isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void o_dia_que_existe_no_mes_e_o_dia_escolhido() {
        assertThat(de(3990, 12).dataNaCompetencia(YearMonth.of(2026, 10)))
                .isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void a_competencia_anterior_ao_inicio_nao_vale() {
        assertThat(de(3990, 12).valeNaCompetencia(YearMonth.of(2026, 8)))
                .as("a recorrência não retroage para antes de existir")
                .isFalse();
    }

    @Test
    void a_competencia_do_inicio_ja_vale() {
        assertThat(de(3990, 12).valeNaCompetencia(YearMonth.of(2026, 9))).isTrue();
    }

    @Test
    void recorrencia_cancelada_nao_vale_em_competencia_nenhuma() {
        Recorrencia desligada = new Recorrencia(1L, 1L, 9L, 7L, 22L, 3990,
                Periodicidade.MENSAL, 12, INICIO, false, "Netflix", AGORA);

        assertThat(desligada.valeNaCompetencia(YearMonth.of(2026, 10))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 32, 100 })
    void dia_fora_da_faixa_e_recusado(int dia) {
        assertThatThrownBy(() -> de(3990, dia)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void valor_nao_positivo_e_recusado() {
        assertThatThrownBy(() -> Recorrencia.nova(1L, 9L, 7L, 22L, 0L, Periodicidade.MENSAL,
                12, INICIO, "Netflix", AGORA)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void descricao_vazia_e_recusada() {
        assertThatThrownBy(() -> Recorrencia.nova(1L, 9L, 7L, 22L, 3990L, Periodicidade.MENSAL,
                12, INICIO, "   ", AGORA)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void periodicidade_ausente_e_recusada() {
        assertThatThrownBy(() -> Recorrencia.nova(1L, 9L, 7L, 22L, 3990L, null,
                12, INICIO, "Netflix", AGORA)).isInstanceOf(ValidacaoException.class);
    }

    private static Recorrencia de(long valor, int dia) {
        return Recorrencia.nova(1L, 9L, 7L, 22L, valor, Periodicidade.MENSAL, dia, INICIO,
                "Netflix", AGORA);
    }
}
