package br.com.cyberbank.categoria.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

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

    /** O mesmo do `varchar(80)` da tabela: o dominio recusa antes de o banco recusar. */
    public static final int TAMANHO_MAXIMO_DO_NOME = 80;

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

    /**
     * Raiz do usuario. O sentido e escolhido aqui e so aqui — a subcategoria HERDA o da raiz e
     * nao pode divergir dela.
     */
    public static Categoria novaRaiz(Long ambienteId, String nome, Sentido sentido, Instant agora) {
        validar(nome, sentido);
        return new Categoria(null, ambienteId, null, nome.trim(), sentido, false, null, false, agora);
    }

    /**
     * Subcategoria do usuario. Recebe o PAI inteiro porque as tres coisas que ela nao escolhe
     * saem dele: o ambiente, o sentido e o direito de existir.
     *
     * <p>Duas recusas, e as duas sao invariantes escritas: o pai nao pode ser subcategoria (a
     * arvore tem dois niveis) nem categoria de sistema (que e sempre raiz e nunca tem filhos).
     */
    public static Categoria novaSubcategoria(Categoria pai, String nome, Instant agora) {
        if (pai.sistema()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
        }
        if (!pai.ehRaiz()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_PAI_INVALIDO);
        }
        validar(nome, pai.sentido());
        return new Categoria(null, pai.ambienteId(), pai.id(), nome.trim(), pai.sentido(),
                false, null, false, agora);
    }

    /**
     * Renomear e LIVRE: o lancamento referencia a categoria por identidade, entao o historico
     * acompanha sem reescrever nada. Livre para a do usuario — a de sistema nao se renomeia.
     */
    public Categoria renomeada(String novoNome) {
        exigirDoUsuario();
        validar(novoNome, sentido);
        return new Categoria(id, ambienteId, paiId, novoNome.trim(), sentido,
                sistema, operacao, inativa, criadaEm);
    }

    /**
     * Inativar e o EXCLUIR DE QUEM TEM HISTORICO, e reativar e a volta exata. Nao ha cascata:
     * inativar a raiz esconde as filhas na leitura (ver {@link ArvoreDeCategorias}) e o campo
     * {@code inativa} delas NAO MUDA — gravar nos filhos destruiria quais deles o usuario ja
     * tinha inativado a mao.
     */
    public Categoria comAtivacao(boolean novaInativa) {
        exigirDoUsuario();
        return new Categoria(id, ambienteId, paiId, nome, sentido,
                sistema, operacao, novaInativa, criadaEm);
    }

    /** Excluir e so para quem nunca teve lancamento; com historico, o caminho e inativar. */
    public void exigirExcluivel() {
        exigirDoUsuario();
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

    /**
     * "Nao se renomeia, nao se move, nao se inativa e nao se exclui." O motivo tem nome: o
     * sistema depende dela POR IDENTIDADE, e sem ela o ciclo nao consegue lancar.
     */
    private void exigirDoUsuario() {
        if (sistema) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
        }
    }

    private static void validar(String nome, Sentido sentido) {
        List<ErroDeValidacao> erros = new ArrayList<>();

        if (nome == null || nome.isBlank()) {
            erros.add(new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome."));
        } else if (nome.trim().length() > TAMANHO_MAXIMO_DO_NOME) {
            erros.add(new ErroDeValidacao("nome", "TAMANHO",
                    "O nome tem no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres."));
        }

        if (sentido == null) {
            erros.add(new ErroDeValidacao("sentido", "OBRIGATORIO", "Informe o sentido."));
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
