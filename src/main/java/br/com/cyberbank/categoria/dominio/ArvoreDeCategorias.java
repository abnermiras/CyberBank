package br.com.cyberbank.categoria.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A lista plana do banco vira a arvore de dois niveis que a tela mostra — e e aqui que
 * "escolhivel" e calculado, uma vez so.
 *
 * <p>A RAIZ ESCONDE A ARVORE: filha de raiz inativa nao e escolhivel, e o campo {@code inativa}
 * dela NAO MUDA. Heranca na leitura, nunca cascata na escrita — gravar nos filhos destruiria
 * quais deles o usuario ja tinha inativado a mao, e a volta nao teria como reconstruir.
 */
public final class ArvoreDeCategorias {

    private ArvoreDeCategorias() {
    }

    public record No(Categoria categoria, boolean escolhivel, List<No> filhas) {
    }

    public static List<No> montar(List<Categoria> categorias) {
        Map<Long, List<Categoria>> filhasPorPai = categorias.stream()
                .filter(c -> !c.ehRaiz())
                .collect(Collectors.groupingBy(Categoria::paiId));

        Set<Long> raizesComFilhaAtiva = filhasPorPai.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(f -> !f.inativa()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Comparator<Categoria> porNome = Comparator.comparing(Categoria::nome, String.CASE_INSENSITIVE_ORDER);

        List<No> arvore = new ArrayList<>();
        categorias.stream().filter(Categoria::ehRaiz).sorted(porNome).forEach(raiz -> {
            List<No> filhas = filhasPorPai.getOrDefault(raiz.id(), List.of()).stream()
                    .sorted(porNome)
                    .map(filha -> new No(filha, filha.escolhivel(!raiz.inativa(), false), List.of()))
                    .toList();

            arvore.add(new No(raiz,
                    raiz.escolhivel(!raiz.inativa(), raizesComFilhaAtiva.contains(raiz.id())),
                    filhas));
        });
        return arvore;
    }

    /** O filtro unico que substituiu a lista de excecoes: relatorio nenhum ve categoria de sistema. */
    public static List<No> semSistema(List<No> arvore) {
        return filtrar(arvore, no -> !no.categoria().sistema());
    }

    /** Dado inativo fica escondido por padrao, e a tela diz que escondeu. */
    public static List<No> semInativas(List<No> arvore) {
        return filtrar(arvore, no -> !no.categoria().inativa());
    }

    private static List<No> filtrar(List<No> arvore, Function<No, Boolean> criterio) {
        return arvore.stream()
                .filter(criterio::apply)
                .map(no -> new No(no.categoria(), no.escolhivel(), filtrar(no.filhas(), criterio)))
                .toList();
    }
}
