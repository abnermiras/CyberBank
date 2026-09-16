package br.com.cyberbank.categoria.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Categoria(
        Long id,
        Long ambienteId,
        Long paiId,
        String nome,
        Sentido sentido,
        CorDeCategoria cor,
        boolean sistema,
        OperacaoDeSistema operacao,
        boolean inativa,
        Instant criadaEm) {

    public static final int QUANTIDADE_DE_SISTEMA =
            OperacaoDeSistema.values().length * Sentido.values().length;

    public static final int TAMANHO_MAXIMO_DO_NOME = 80;

    public static List<Categoria> jogoDeSistema(Long ambienteId, Instant agora) {
        List<Categoria> jogo = new ArrayList<>(QUANTIDADE_DE_SISTEMA);
        for (OperacaoDeSistema operacao : OperacaoDeSistema.values()) {
            for (Sentido sentido : Sentido.values()) {
                jogo.add(new Categoria(null, ambienteId, null, operacao.nome(), sentido,
                        null, true, operacao, false, agora));
            }
        }
        return jogo;
    }

    public static Categoria novaRaiz(Long ambienteId, String nome, Sentido sentido,
            CorDeCategoria cor, Instant agora) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(nome, erros);
        if (sentido == null) {
            erros.add(new ErroDeValidacao("sentido", "OBRIGATORIO", "Informe o sentido."));
        }
        if (cor == null) {
            erros.add(new ErroDeValidacao("cor", "OBRIGATORIO", "Escolha uma cor."));
        }
        recusarSeHouver(erros);

        return new Categoria(null, ambienteId, null, nome.trim(), sentido, cor,
                false, null, false, agora);
    }

    public static Categoria novaSubcategoria(Categoria pai, String nome, Instant agora) {
        if (pai.sistema()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
        }
        if (!pai.ehRaiz()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_PAI_INVALIDO);
        }
        exigirNomeValido(nome);

        return new Categoria(null, pai.ambienteId(), pai.id(), nome.trim(), pai.sentido(),
                null, false, null, false, agora);
    }

    public Categoria renomeada(String novoNome) {
        exigirDoUsuario();
        exigirNomeValido(novoNome);
        return new Categoria(id, ambienteId, paiId, novoNome.trim(), sentido, cor,
                sistema, operacao, inativa, criadaEm);
    }

    public Categoria comAtivacao(boolean novaInativa) {
        exigirDoUsuario();
        return new Categoria(id, ambienteId, paiId, nome, sentido, cor,
                sistema, operacao, novaInativa, criadaEm);
    }

    public Categoria recolorida(CorDeCategoria novaCor) {
        exigirDoUsuario();
        if (!ehRaiz()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_SEM_COR_PROPRIA);
        }
        if (novaCor == null) {
            throw new ValidacaoException(List.of(
                    new ErroDeValidacao("cor", "OBRIGATORIO", "Escolha uma cor.")));
        }
        return new Categoria(id, ambienteId, paiId, nome, sentido, novaCor,
                sistema, operacao, inativa, criadaEm);
    }

    public void exigirExcluivel() {
        exigirDoUsuario();
    }

    public boolean ehRaiz() {
        return paiId == null;
    }

    public boolean escolhivel(boolean raizAtiva, boolean temFilhaAtiva) {
        return !sistema && !inativa && raizAtiva && !temFilhaAtiva;
    }

    private void exigirDoUsuario() {
        if (sistema) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
        }
    }

    private static void exigirNomeValido(String nome) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(nome, erros);
        recusarSeHouver(erros);
    }

    private static void validarNome(String nome, List<ErroDeValidacao> erros) {
        if (nome == null || nome.isBlank()) {
            erros.add(new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome."));
        } else if (nome.trim().length() > TAMANHO_MAXIMO_DO_NOME) {
            erros.add(new ErroDeValidacao("nome", "TAMANHO",
                    "O nome tem no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres."));
        }
    }

    private static void recusarSeHouver(List<ErroDeValidacao> erros) {
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
