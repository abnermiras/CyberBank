package br.com.cyberbank.categoria.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Categoria responde PARA QUE SERVIU O DINHEIRO. Arvore de exatamente dois niveis, sempre de
 * um ambiente, e nunca muda de ambiente (docs/02-dominio/categoria.md).
 */
public record Categoria(
        Long id,
        Long ambienteId,
        Long paiId,
        String nome,
        Sentido sentido,
        boolean sistema,
        OperacaoDeSistema operacao,
        boolean inativa,
        Instant criadaEm) {

    /** Sete operacoes x dois sentidos. O jogo completo nasce COM O AMBIENTE, no mesmo ato. */
    public static final int QUANTIDADE_DE_SISTEMA = OperacaoDeSistema.values().length * Sentido.values().length;

    public static List<Categoria> jogoDeSistema(Long ambienteId, Instant agora) {
        List<Categoria> jogo = new ArrayList<>(QUANTIDADE_DE_SISTEMA);
        for (OperacaoDeSistema operacao : OperacaoDeSistema.values()) {
            for (Sentido sentido : Sentido.values()) {
                jogo.add(new Categoria(null, ambienteId, null, operacao.nome(), sentido,
                        true, operacao, false, agora));
            }
        }
        return jogo;
    }

    public boolean ehRaiz() {
        return paiId == null;
    }

    /**
     * Escolhivel e DERIVADO, nunca coluna: nada derivado e armazenado
     * (docs/03-dados/modelo-de-dados.md).
     *
     * <p>Sao quatro condicoes, e a terceira e a que oscila de proposito: a raiz deixa de ser
     * escolhivel no primeiro filho ATIVO e volta a ser quando o ultimo some — a regra responde
     * "existe um destino mais especifico agora?", e a resposta muda com o tempo.
     */
    public boolean escolhivel(boolean raizAtiva, boolean temFilhaAtiva) {
        return !sistema && !inativa && raizAtiva && !temFilhaAtiva;
    }
}
