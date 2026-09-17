package br.com.cyberbank.lancamento.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RelatorioDoMes {

    public record CategoriaDoRelatorio(Long id, Long raizId, String nome, String cor,
                                       Sentido sentido, boolean sistema) {
    }

    public record Bucket(Long categoriaId, Sentido sentido, long totalCentavos, long lancamentos) {
    }

    public record GastoDeCategoria(Long categoriaId, String nome, String cor,
                                   long totalCentavos, long lancamentos) {
    }

    private RelatorioDoMes() {
    }

    public static List<GastoDeCategoria> gastoPorCategoria(
            List<Bucket> buckets, Map<Long, CategoriaDoRelatorio> categorias) {

        Map<Long, GastoDeCategoria> porRaiz = new LinkedHashMap<>();

        for (Bucket bucket : buckets) {
            CategoriaDoRelatorio categoria = buscar(categorias, bucket.categoriaId());
            if (!contaComoGasto(bucket, categoria)) {
                continue;
            }
            CategoriaDoRelatorio raiz = raizDe(categoria, categorias);
            Long chave = raiz == null ? null : raiz.id();
            GastoDeCategoria atual = porRaiz.get(chave);
            long total = (atual == null ? 0 : atual.totalCentavos()) + comSinal(bucket);
            long quantidade = (atual == null ? 0 : atual.lancamentos()) + bucket.lancamentos();

            porRaiz.put(chave, new GastoDeCategoria(chave,
                    raiz == null ? null : raiz.nome(),
                    raiz == null ? null : raiz.cor(),
                    total, quantidade));
        }

        List<GastoDeCategoria> linhas = new ArrayList<>(porRaiz.values());
        linhas.removeIf(linha -> linha.totalCentavos() == 0);
        linhas.sort(Comparator.comparingLong(GastoDeCategoria::totalCentavos).reversed());
        return linhas;
    }

    public static long somarSentido(List<Bucket> buckets,
            Map<Long, CategoriaDoRelatorio> categorias, Sentido sentidoDaCategoria) {

        long total = 0;
        for (Bucket bucket : buckets) {
            CategoriaDoRelatorio categoria = buscar(categorias, bucket.categoriaId());
            if (categoria != null && categoria.sistema()) {
                continue;
            }
            if (sentidoDeLeitura(bucket, categoria) != sentidoDaCategoria) {
                continue;
            }
            total += bucket.sentido() == sentidoDaCategoria
                    ? bucket.totalCentavos()
                    : -bucket.totalCentavos();
        }
        return total;
    }

    private static boolean contaComoGasto(Bucket bucket, CategoriaDoRelatorio categoria) {
        if (categoria != null && categoria.sistema()) {
            return false;
        }
        return sentidoDeLeitura(bucket, categoria) == Sentido.SAIDA;
    }

    private static Sentido sentidoDeLeitura(Bucket bucket, CategoriaDoRelatorio categoria) {
        return categoria == null ? bucket.sentido() : categoria.sentido();
    }

    private static long comSinal(Bucket bucket) {
        return bucket.sentido() == Sentido.SAIDA ? bucket.totalCentavos() : -bucket.totalCentavos();
    }

    private static CategoriaDoRelatorio raizDe(
            CategoriaDoRelatorio categoria, Map<Long, CategoriaDoRelatorio> categorias) {
        if (categoria == null) {
            return null;
        }
        CategoriaDoRelatorio raiz = buscar(categorias, categoria.raizId());
        return raiz == null ? categoria : raiz;
    }

    private static CategoriaDoRelatorio buscar(
            Map<Long, CategoriaDoRelatorio> categorias, Long id) {
        return id == null ? null : categorias.get(id);
    }
}
