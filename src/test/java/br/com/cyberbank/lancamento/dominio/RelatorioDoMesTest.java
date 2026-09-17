package br.com.cyberbank.lancamento.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import br.com.cyberbank.lancamento.dominio.RelatorioDoMes.Bucket;
import br.com.cyberbank.lancamento.dominio.RelatorioDoMes.CategoriaDoRelatorio;
import br.com.cyberbank.lancamento.dominio.RelatorioDoMes.GastoDeCategoria;

import org.junit.jupiter.api.Test;

class RelatorioDoMesTest {

    private static final CategoriaDoRelatorio MERCADO =
            new CategoriaDoRelatorio(1L, 1L, "Mercado", "OLIVA", Sentido.SAIDA, false);
    private static final CategoriaDoRelatorio FEIRA =
            new CategoriaDoRelatorio(2L, 1L, "Feira", "OLIVA", Sentido.SAIDA, false);
    private static final CategoriaDoRelatorio LAZER =
            new CategoriaDoRelatorio(3L, 3L, "Lazer", "MALVA", Sentido.SAIDA, false);
    private static final CategoriaDoRelatorio SALARIO =
            new CategoriaDoRelatorio(4L, 4L, "Salário", "TEAL", Sentido.ENTRADA, false);
    private static final CategoriaDoRelatorio ABERTURA =
            new CategoriaDoRelatorio(9L, 9L, "Saldo de abertura", null, Sentido.ENTRADA, true);

    private static final Map<Long, CategoriaDoRelatorio> CATEGORIAS = Map.of(
            1L, MERCADO, 2L, FEIRA, 3L, LAZER, 4L, SALARIO, 9L, ABERTURA);

    @Test
    void a_subcategoria_soma_na_raiz() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(1L, Sentido.SAIDA, 30000, 2),
                new Bucket(2L, Sentido.SAIDA, 12000, 1)), CATEGORIAS);

        assertThat(gasto).singleElement().satisfies(linha -> {
            assertThat(linha.nome()).isEqualTo("Mercado");
            assertThat(linha.totalCentavos()).isEqualTo(42000);
            assertThat(linha.lancamentos()).isEqualTo(3);
        });
    }

    @Test
    void o_estorno_abate_o_mes_porque_quem_manda_e_o_sentido_da_categoria() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(3L, Sentido.SAIDA, 20000, 1),
                new Bucket(3L, Sentido.ENTRADA, 5000, 1)), CATEGORIAS);

        assertThat(gasto).singleElement().satisfies(linha ->
                assertThat(linha.totalCentavos())
                        .as("quem compra e devolve não pode ver o gasto cheio")
                        .isEqualTo(15000));
    }

    @Test
    void categoria_de_sistema_nunca_e_gasto_da_vida() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(9L, Sentido.SAIDA, 500000, 1),
                new Bucket(3L, Sentido.SAIDA, 20000, 1)), CATEGORIAS);

        assertThat(gasto).singleElement()
                .satisfies(linha -> assertThat(linha.nome()).isEqualTo("Lazer"));
    }

    @Test
    void o_que_esta_sem_categoria_aparece_quando_e_saida_e_some_quando_e_entrada() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(null, Sentido.SAIDA, 7000, 1),
                new Bucket(null, Sentido.ENTRADA, 90000, 1)), CATEGORIAS);

        assertThat(gasto).singleElement().satisfies(linha -> {
            assertThat(linha.categoriaId()).isNull();
            assertThat(linha.nome()).isNull();
            assertThat(linha.totalCentavos()).isEqualTo(7000);
        });
    }

    @Test
    void a_lista_desce_do_maior_para_o_menor() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(3L, Sentido.SAIDA, 9000, 1),
                new Bucket(1L, Sentido.SAIDA, 45000, 1),
                new Bucket(null, Sentido.SAIDA, 20000, 1)), CATEGORIAS);

        assertThat(gasto).extracting(GastoDeCategoria::totalCentavos)
                .containsExactly(45000L, 20000L, 9000L);
    }

    @Test
    void categoria_que_zerou_no_mes_sai_da_lista() {
        List<GastoDeCategoria> gasto = RelatorioDoMes.gastoPorCategoria(List.of(
                new Bucket(3L, Sentido.SAIDA, 20000, 1),
                new Bucket(3L, Sentido.ENTRADA, 20000, 1)), CATEGORIAS);

        assertThat(gasto)
                .as("comprou e devolveu tudo: a barra não existe, e zero não é informação")
                .isEmpty();
    }

    @Test
    void a_receita_do_mes_tambem_e_liquida_de_estorno() {
        long entrou = RelatorioDoMes.somarSentido(List.of(
                new Bucket(4L, Sentido.ENTRADA, 500000, 1),
                new Bucket(4L, Sentido.SAIDA, 50000, 1),
                new Bucket(9L, Sentido.ENTRADA, 1000000, 1),
                new Bucket(3L, Sentido.SAIDA, 20000, 1)), CATEGORIAS, Sentido.ENTRADA);

        assertThat(entrou)
                .as("o saldo de abertura é de sistema e não é receita da vida")
                .isEqualTo(450000);
    }

    @Test
    void o_gasto_do_mes_e_a_soma_das_barras() {
        List<Bucket> buckets = List.of(
                new Bucket(1L, Sentido.SAIDA, 30000, 1),
                new Bucket(3L, Sentido.SAIDA, 20000, 1),
                new Bucket(3L, Sentido.ENTRADA, 5000, 1));

        long saiu = RelatorioDoMes.somarSentido(buckets, CATEGORIAS, Sentido.SAIDA);
        long somaDasBarras = RelatorioDoMes.gastoPorCategoria(buckets, CATEGORIAS).stream()
                .mapToLong(GastoDeCategoria::totalCentavos).sum();

        assertThat(saiu).isEqualTo(somaDasBarras).isEqualTo(45000);
    }
}
