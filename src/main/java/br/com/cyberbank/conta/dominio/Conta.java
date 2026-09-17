package br.com.cyberbank.conta.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Conta(
        Long id,
        Long ambienteId,
        String nome,
        TipoDeConta tipo,
        boolean entraNoFluxoDeCaixa,
        boolean entraEmCaixa,
        boolean inativa,
        Instant criadaEm) {

    public static final int TAMANHO_MAXIMO_DO_NOME = 80;

    public static Conta nova(Long ambienteId, String nome, TipoDeConta tipo, Instant agora) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(nome, erros);
        if (tipo == null) {
            erros.add(new ErroDeValidacao("tipo", "OBRIGATORIO", "Escolha o tipo da conta."));
        } else if (tipo.dependeDeFatura()) {
            erros.add(new ErroDeValidacao("tipo", "INDISPONIVEL",
                    "Cartão de crédito ainda não pode ser cadastrado."));
        }
        recusarSeHouver(erros);

        return new Conta(null, ambienteId, nome.trim(), tipo,
                tipo.entraNoFluxoDeCaixa(), tipo.entraEmCaixa(), false, agora);
    }

    public void exigirSaldoInicialCompativel(Long saldoInicialCentavos) {
        if (saldoInicialCentavos != null && !tipo.aceitaSaldoInicial()) {
            throw new RegraDeDominioException(CodigoDeErro.CARTAO_SEM_SALDO_INICIAL);
        }
    }

    public void exigirAtivaParaLancar() {
        if (inativa) {
            throw new RegraDeDominioException(CodigoDeErro.CONTA_INATIVA);
        }
    }

    public void exigirTransferivel() {
        if (!tipo.aceitaTransferencia()) {
            throw new RegraDeDominioException(CodigoDeErro.BENEFICIO_NAO_TRANSFERE);
        }
    }

    public void exigirExcluivel(boolean temLancamento) {
        if (temLancamento) {
            throw new RegraDeDominioException(CodigoDeErro.CONTA_COM_LANCAMENTO);
        }
    }

    public Conta renomeada(String novoNome) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(novoNome, erros);
        recusarSeHouver(erros);
        return new Conta(id, ambienteId, novoNome.trim(), tipo,
                entraNoFluxoDeCaixa, entraEmCaixa, inativa, criadaEm);
    }

    public Conta comAtivacao(boolean novaInativa) {
        return new Conta(id, ambienteId, nome, tipo,
                entraNoFluxoDeCaixa, entraEmCaixa, novaInativa, criadaEm);
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
