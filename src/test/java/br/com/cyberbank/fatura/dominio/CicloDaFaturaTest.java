package br.com.cyberbank.fatura.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;

import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class CicloDaFaturaTest {

    private static final CicloDaFatura VENCE_DIA_5_FECHA_8_ANTES = new CicloDaFatura(5, 8);

    @Test
    void o_exemplo_literal_do_doc_marco_de_2027_fecha_em_fevereiro() {
        YearMonth marco = YearMonth.of(2027, 3);

        assertThat(VENCE_DIA_5_FECHA_8_ANTES.vencimentoDe(marco))
                .isEqualTo(LocalDate.of(2027, 3, 5));
        assertThat(VENCE_DIA_5_FECHA_8_ANTES.fechamentoDe(marco))
                .as("os dias corridos jogam o fechamento para o mês anterior")
                .isEqualTo(LocalDate.of(2027, 2, 25));
    }

    @Test
    void dia_maior_que_o_mes_cai_no_ultimo_dia_dele() {
        CicloDaFatura venceDia31 = new CicloDaFatura(31, 10);

        assertThat(venceDia31.vencimentoDe(YearMonth.of(2027, 2)))
                .isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(venceDia31.vencimentoDe(YearMonth.of(2028, 2)))
                .as("2028 é bissexto")
                .isEqualTo(LocalDate.of(2028, 2, 29));
        assertThat(venceDia31.vencimentoDe(YearMonth.of(2027, 4)))
                .isEqualTo(LocalDate.of(2027, 4, 30));
    }

    @Test
    void a_competencia_corrente_e_a_primeira_cujo_fechamento_ainda_nao_passou() {
        assertThat(VENCE_DIA_5_FECHA_8_ANTES.competenciaCorrenteEm(LocalDate.of(2026, 9, 18)))
                .as("a de setembro fechou em 28/08: quem compra hoje cai na de outubro")
                .isEqualTo(YearMonth.of(2026, 10));

        assertThat(VENCE_DIA_5_FECHA_8_ANTES.competenciaCorrenteEm(LocalDate.of(2026, 8, 20)))
                .isEqualTo(YearMonth.of(2026, 9));
    }

    @Test
    void no_proprio_dia_do_fechamento_o_ciclo_ainda_e_o_corrente() {
        assertThat(VENCE_DIA_5_FECHA_8_ANTES.competenciaCorrenteEm(LocalDate.of(2026, 8, 28)))
                .as("a dataFechamento ainda não passou: ela é hoje")
                .isEqualTo(YearMonth.of(2026, 9));
    }

    @Test
    void a_fatura_sempre_fecha_antes_de_vencer() {
        CicloDaFatura ciclo = new CicloDaFatura(1, 28);

        for (int mes = 1; mes <= 12; mes++) {
            YearMonth competencia = YearMonth.of(2027, mes);
            assertThat(ciclo.fechamentoDe(competencia))
                    .isBefore(ciclo.vencimentoDe(competencia));
        }
    }

    @Test
    void o_dia_do_vencimento_esta_dentro_do_mes() {
        assertThatThrownBy(() -> new CicloDaFatura(0, 8)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> new CicloDaFatura(32, 8)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void fechar_no_mesmo_dia_do_vencimento_nao_e_ciclo() {
        assertThatThrownBy(() -> new CicloDaFatura(5, 0)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> new CicloDaFatura(5, 29)).isInstanceOf(ValidacaoException.class);
    }
}
