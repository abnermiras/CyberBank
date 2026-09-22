package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Ambiente(Long id, String nome, Long criadoPor, Instant criadoEm) {

    public static final String NOME_PADRAO = "Ambiente Pessoal";

    public static final int TAMANHO_MAXIMO_DO_NOME = 80;

    public static Ambiente novo(String nome, Long usuarioId, Instant agora) {
        exigirNomeValido(nome);
        return new Ambiente(null, nome.trim(), usuarioId, agora);
    }

    public Ambiente renomeadoPor(Papel papel, String novoNome) {
        if (!papel.podeRenomearAmbiente()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
        exigirNomeValido(novoNome);
        return new Ambiente(id, novoNome.trim(), criadoPor, criadoEm);
    }

    private static void exigirNomeValido(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException(List.of(
                    new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome.")));
        }
        if (nome.trim().length() > TAMANHO_MAXIMO_DO_NOME) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("nome", "TAMANHO",
                    "O nome tem no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres.")));
        }
    }
}
