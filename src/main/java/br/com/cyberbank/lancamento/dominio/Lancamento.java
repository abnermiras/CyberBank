package br.com.cyberbank.lancamento.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Lancamento(
        Long id,
        Long ambienteId,
        Long contaId,
        Long meioId,
        Long categoriaId,
        Long autorId,
        Sentido sentido,
        long valorCentavos,
        LocalDate dataEvento,
        LocalDate dataEfeito,
        String descricao,
        Situacao situacao,
        Long transferenciaId,
        Long estornoDeId,
        boolean doCiclo,
        String estabelecimento,
        Instant criadoEm) {

    public static final int TAMANHO_MAXIMO_DA_DESCRICAO = 200;

    public static Lancamento deAbertura(Long ambienteId, Long contaId, Long categoriaDeSistemaId,
            Long autorId, long saldoComSinal, LocalDate dia, Instant agora) {

        if (saldoComSinal == 0) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("saldoInicial", "ZERO",
                    "Zero não é lançamento: uma conta sem saldo inicial nasce sem abertura.")));
        }
        Sentido sentido = Sentido.de(saldoComSinal);
        return new Lancamento(null, ambienteId, contaId, null, categoriaDeSistemaId, autorId,
                sentido, Math.abs(saldoComSinal), dia, dia, "Saldo de abertura",
                Situacao.REALIZADO, null, null, true, null, agora);
    }

    public static Lancamento doUsuario(Long ambienteId, Long contaId, Long meioId,
            Long categoriaId, Long autorId, Sentido sentido, Long valorCentavos,
            LocalDate dataEvento, LocalDate dataEfeito, String descricao, Situacao situacao,
            String estabelecimento, Instant agora) {

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valorCentavos, erros);
        validarDescricao(descricao, erros);
        validarDatas(dataEvento, dataEfeito, erros);
        if (sentido == null) {
            erros.add(new ErroDeValidacao("sentido", "OBRIGATORIO", "Informe o sentido."));
        }
        if (situacao == null) {
            erros.add(new ErroDeValidacao("situacao", "OBRIGATORIO", "Informe a situação."));
        }
        if (meioId == null) {
            erros.add(new ErroDeValidacao("meioId", "OBRIGATORIO",
                    "Gasto e receita reais têm meio de pagamento."));
        }
        recusarSeHouver(erros);

        return new Lancamento(null, ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEfeito, descricao.trim(), situacao, null, null,
                false, estabelecimento, agora);
    }

    public static List<Lancamento> parDeTransferencia(Long ambienteId, Long contaDeOrigemId,
            Long contaDeDestinoId, Long categoriaDaSaidaId, Long categoriaDaEntradaId,
            Long autorId, Long valorCentavos, LocalDate dataEvento, LocalDate dataEfeito,
            String descricao, Situacao situacao, long transferenciaId, Instant agora) {

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valorCentavos, erros);
        validarDescricao(descricao, erros);
        validarDatas(dataEvento, dataEfeito, erros);
        recusarSeHouver(erros);

        if (contaDeOrigemId.equals(contaDeDestinoId)) {
            throw new RegraDeDominioException(CodigoDeErro.TRANSFERENCIA_MESMA_CONTA);
        }

        Lancamento saida = new Lancamento(null, ambienteId, contaDeOrigemId, null,
                categoriaDaSaidaId, autorId, Sentido.SAIDA, valorCentavos, dataEvento,
                dataEfeito, descricao.trim(), situacao, transferenciaId, null, false, null, agora);

        Lancamento entrada = new Lancamento(null, ambienteId, contaDeDestinoId, null,
                categoriaDaEntradaId, autorId, Sentido.ENTRADA, valorCentavos, dataEvento,
                dataEfeito, descricao.trim(), situacao, transferenciaId, null, false, null, agora);

        return List.of(saida, entrada);
    }

    public Lancamento estornadoEm(LocalDate dia, Long autorDoEstornoId, Instant agora) {
        if (id == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        if (dia.isBefore(dataEvento)) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("dataEvento", "ANTERIOR",
                    "O estorno não acontece antes da compra que ele desfaz.")));
        }
        return new Lancamento(null, ambienteId, contaId, meioId, categoriaId, autorDoEstornoId,
                sentido.invertido(), valorCentavos, dia, dia, "Estorno de " + descricao,
                Situacao.REALIZADO, null, id, false, estabelecimento, agora);
    }

    public Lancamento corrigido(Long novaContaId, Long novoMeioId, Long novaCategoriaId,
            Sentido novoSentido, Long novoValorCentavos, LocalDate novaDataEvento,
            LocalDate novaDataEfeito, String novaDescricao, Situacao novaSituacao) {

        exigirDoUsuario();

        Long valor = novoValorCentavos == null ? valorCentavos : novoValorCentavos;
        LocalDate evento = novaDataEvento == null ? dataEvento : novaDataEvento;
        LocalDate efeito = novaDataEfeito == null ? dataEfeito : novaDataEfeito;
        String texto = novaDescricao == null ? descricao : novaDescricao;

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valor, erros);
        validarDescricao(texto, erros);
        validarDatas(evento, efeito, erros);
        recusarSeHouver(erros);

        return new Lancamento(id, ambienteId,
                novaContaId == null ? contaId : novaContaId,
                novoMeioId == null ? meioId : novoMeioId,
                novaCategoriaId == null ? categoriaId : novaCategoriaId,
                autorId,
                novoSentido == null ? sentido : novoSentido,
                valor, evento, efeito, texto.trim(),
                novaSituacao == null ? situacao : novaSituacao,
                transferenciaId, estornoDeId, doCiclo, estabelecimento, criadoEm);
    }

    public Lancamento realizadoPelaData(LocalDate hoje) {
        if (situacao != Situacao.PREVISTO || dataEfeito.isAfter(hoje)) {
            return this;
        }
        return new Lancamento(id, ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEfeito, descricao, Situacao.REALIZADO,
                transferenciaId, estornoDeId, doCiclo, estabelecimento, criadoEm);
    }

    public void exigirExcluivelPeloUsuario(boolean temEstorno) {
        exigirDoUsuario();
        if (temEstorno) {
            throw new RegraDeDominioException(CodigoDeErro.LANCAMENTO_COM_ESTORNO);
        }
    }

    public boolean entraNoSaldoRealizado() {
        return situacao.entraNoSaldoRealizado();
    }

    public boolean pendente() {
        return categoriaId == null;
    }

    public boolean ehTransferencia() {
        return transferenciaId != null;
    }

    public long valorComSinal() {
        return sentido.aplicadoA(valorCentavos);
    }

    private void exigirDoUsuario() {
        if (doCiclo) {
            throw new RegraDeDominioException(CodigoDeErro.LANCAMENTO_DO_CICLO);
        }
    }

    private static void validarValor(Long valorCentavos, List<ErroDeValidacao> erros) {
        if (valorCentavos == null) {
            erros.add(new ErroDeValidacao("valor", "OBRIGATORIO", "Informe o valor."));
        } else if (valorCentavos <= 0) {
            erros.add(new ErroDeValidacao("valor", "POSITIVO",
                    "O valor é sempre positivo: o sinal vem do sentido."));
        }
    }

    private static void validarDescricao(String descricao, List<ErroDeValidacao> erros) {
        if (descricao == null || descricao.isBlank()) {
            erros.add(new ErroDeValidacao("descricao", "OBRIGATORIO", "Informe a descrição."));
        } else if (descricao.trim().length() > TAMANHO_MAXIMO_DA_DESCRICAO) {
            erros.add(new ErroDeValidacao("descricao", "TAMANHO",
                    "A descrição tem no máximo " + TAMANHO_MAXIMO_DA_DESCRICAO + " caracteres."));
        }
    }

    private static void validarDatas(LocalDate dataEvento, LocalDate dataEfeito,
            List<ErroDeValidacao> erros) {
        if (dataEvento == null) {
            erros.add(new ErroDeValidacao("dataEvento", "OBRIGATORIO", "Informe a data."));
        }
        if (dataEfeito == null) {
            erros.add(new ErroDeValidacao("dataEfeito", "OBRIGATORIO", "Informe a data de efeito."));
        }
        if (dataEvento != null && dataEfeito != null && dataEfeito.isBefore(dataEvento)) {
            erros.add(new ErroDeValidacao("dataEfeito", "ANTERIOR",
                    "A data de efeito não é anterior à data do evento."));
        }
    }

    private static void recusarSeHouver(List<ErroDeValidacao> erros) {
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
