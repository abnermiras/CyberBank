package br.com.cyberbank.recorrencia.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParcelamentoTest {

    private static final Instant AGORA = Instant.parse("2026-09-18T12:00:00Z");
    private static final LocalDate COMPRA = LocalDate.of(2026, 9, 18);

    @Test
    void o_exemplo_literal_do_doc_cinco_mil_em_tres_vezes() {
        List<Long> parcelas = de(500000, 3).valoresDasParcelas();

        assertThat(parcelas)
                .as("o centavo que sobra vai na PRIMEIRA parcela, que é o que a maioria dos"
                        + " emissores faz — assim o valor do app bate com o da fatura")
                .containsExactly(166668L, 166666L, 166666L);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 4, 6, 7, 9, 10, 12, 18, 24, 99 })
    void a_soma_das_parcelas_e_sempre_o_valor_da_compra(int emQuantas) {
        for (long valor : new long[] { 1, 100, 99999, 500000, 123457, 999999999 }) {
            List<Long> parcelas = de(valor, emQuantas).valoresDasParcelas();

            assertThat(parcelas).hasSize(emQuantas);
            assertThat(parcelas.stream().mapToLong(Long::longValue).sum())
                    .as("se a soma não bate com o valor da compra, é bug")
                    .isEqualTo(valor);
        }
    }

    @Test
    void so_a_primeira_parcela_difere_e_no_maximo_por_centavos() {
        List<Long> parcelas = de(100001, 10).valoresDasParcelas();

        assertThat(parcelas.subList(1, parcelas.size()))
                .as("as N−1 seguintes são todas iguais")
                .containsOnly(10000L);
        assertThat(parcelas.getFirst() - parcelas.get(1)).isLessThan(10);
    }

    @Test
    void uma_parcela_nao_e_parcelamento_e_e_uma_compra_a_vista() {
        assertThatThrownBy(() -> de(500000, 1)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> de(500000, 0)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> de(500000, 100)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    void editar_muda_o_valor_da_compra_e_o_sistema_redistribui_as_n() {
        Parcelamento dobrado = de(10000, 10).corrigido(20000L, null, null);

        assertThat(dobrado.valoresDasParcelas())
                .as("R$ 100 em 10x viradas para R$ 200 — o cenário do Abner")
                .containsOnly(2000L);
        assertThat(dobrado.parcelas())
                .as("editar altera todas as parcelas, sempre; o N não muda")
                .isEqualTo(10);
        assertThat(dobrado.dataDaCompra())
                .as("a compra aconteceu uma vez, e continua tendo acontecido naquele dia")
                .isEqualTo(COMPRA);
    }

    @Test
    void o_valor_da_compra_e_sempre_positivo() {
        assertThatThrownBy(() -> de(0, 10)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> de(-500000, 10)).isInstanceOf(ValidacaoException.class);
    }

    private static Parcelamento de(long valorCentavos, int parcelas) {
        return Parcelamento.novo(7L, 9L, 5L, 11L, valorCentavos, parcelas, COMPRA,
                "Aulas de espanhol", AGORA);
    }
}
