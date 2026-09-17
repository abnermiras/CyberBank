package br.com.cyberbank.meio.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Meio(
        Long id,
        Long ambienteId,
        String nome,
        TipoDeMeio tipo,
        Long contaId,
        boolean inativo,
        Instant criadoEm) {

    public static final int TAMANHO_MAXIMO_DO_NOME = 80;

    public static Meio novo(Long ambienteId, String nome, TipoDeMeio tipo, Long contaId,
            String tipoDaConta, Instant agora) {

        List<ErroDeValidacao> erros = new ArrayList<>();
        if (tipo == null) {
            erros.add(new ErroDeValidacao("tipo", "OBRIGATORIO", "Escolha o tipo do meio."));
        } else if (tipo.dependeDeFatura()) {
            erros.add(new ErroDeValidacao("tipo", "INDISPONIVEL",
                    "Cartão de crédito ainda não pode ser cadastrado."));
        } else {
            validarNome(nome, tipo, erros);
        }
        if (contaId == null) {
            erros.add(new ErroDeValidacao("contaId", "OBRIGATORIO",
                    "Todo meio aponta para uma conta."));
        }
        recusarSeHouver(erros);

        if (!tipo.servePara(tipoDaConta)) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_INCOMPATIVEL_COM_CONTA);
        }

        return new Meio(null, ambienteId, tipo.temNome() ? nome.trim() : null, tipo, contaId,
                false, agora);
    }

    public LocalDate dataEfeitoPara(LocalDate dataEvento, LocalDate dataEfeitoInformada) {
        if (!tipo.separaAsDuasDatas()) {
            if (dataEfeitoInformada != null && !dataEfeitoInformada.equals(dataEvento)) {
                throw new ValidacaoException(List.of(new ErroDeValidacao("dataEfeito", "A_VISTA",
                        "Neste meio o dinheiro sai no dia do evento: as duas datas são a mesma.")));
            }
            return dataEvento;
        }
        return dataEfeitoInformada == null ? dataEvento : dataEfeitoInformada;
    }

    public void exigirAtivoParaLancar() {
        if (inativo) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_INATIVO);
        }
    }

    public void exigirExcluivel(boolean temLancamento) {
        if (temLancamento) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_COM_LANCAMENTO);
        }
    }

    public void exigirDaConta(Long contaDoLancamentoId) {
        if (!contaId.equals(contaDoLancamentoId)) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_INCOMPATIVEL_COM_CONTA);
        }
    }

    public Meio renomeado(String novoNome) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(novoNome, tipo, erros);
        recusarSeHouver(erros);
        return new Meio(id, ambienteId, novoNome.trim(), tipo, contaId, inativo, criadoEm);
    }

    public Meio comAtivacao(boolean novoInativo) {
        return new Meio(id, ambienteId, nome, tipo, contaId, novoInativo, criadoEm);
    }

    private static void validarNome(String nome, TipoDeMeio tipo, List<ErroDeValidacao> erros) {
        if (!tipo.temNome()) {
            if (nome != null && !nome.isBlank()) {
                erros.add(new ErroDeValidacao("nome", "NAO_SE_APLICA",
                        "Só o cartão de crédito tem nome: o meio é identificado pela conta e pelo tipo."));
            }
            return;
        }
        if (nome == null || nome.isBlank()) {
            erros.add(new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome do cartão."));
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
