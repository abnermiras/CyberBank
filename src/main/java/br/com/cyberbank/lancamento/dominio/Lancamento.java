package br.com.cyberbank.lancamento.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
        Long faturaId,
        Long pagamentoDeFaturaId,
        Long rolagemDeFatura,
        Long parcelamentoId,
        Long recorrenciaId,
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
                Situacao.REALIZADO, null, null, null, null, null, null, null, true, null, agora);
    }

    public static Lancamento deRendimento(Long ambienteId, Long contaId, Long categoriaId,
            Long autorId, long diferencaComSinal, LocalDate dia, Instant agora) {

        if (diferencaComSinal == 0) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("valorAtual", "SEM_DIFERENCA",
                    "O valor informado é o que a aplicação já vale: não há rendimento a gravar.")));
        }
        return new Lancamento(null, ambienteId, contaId, null, categoriaId, autorId,
                Sentido.de(diferencaComSinal), Math.abs(diferencaComSinal), dia, dia,
                "Rendimento", Situacao.REALIZADO, null, null, null, null, null, null, null, false, null, agora);
    }

    public static Lancamento doUsuario(Long ambienteId, Long contaId, Long meioId,
            Long categoriaId, Long autorId, Sentido sentido, Long valorCentavos,
            LocalDate dataEvento, LocalDate dataEfeito, String descricao, Situacao situacao,
            Long faturaId, String estabelecimento, Instant agora) {

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
                faturaId, null, null, null, null, false, estabelecimento, agora);
    }

    public static List<Lancamento> parcelasDeUmaCompra(Long ambienteId, Long contaDoCartaoId,
            Long meioId, Long categoriaId, Long autorId, List<Long> valores,
            List<Long> faturasNaOrdem, LocalDate dataDaCompra, String descricao,
            Long parcelamentoId, Instant agora) {

        if (valores.size() != faturasNaOrdem.size()) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }

        List<Lancamento> parcelas = new ArrayList<>(valores.size());
        for (int parcela = 0; parcela < valores.size(); parcela++) {
            parcelas.add(new Lancamento(null, ambienteId, contaDoCartaoId, meioId, categoriaId,
                    autorId, Sentido.SAIDA, valores.get(parcela), dataDaCompra, dataDaCompra,
                    descricao, Situacao.PROVISIONADO, null, null, faturasNaOrdem.get(parcela),
                    null, null, parcelamentoId, null, false, null, agora));
        }
        return parcelas;
    }

    public static Lancamento deOcorrenciaNoCredito(Long ambienteId, Long contaDoCartaoId,
            Long meioId, Long categoriaId, Long autorId, long valorCentavos, LocalDate dia,
            String descricao, Long faturaId, Long recorrenciaId, Instant agora) {

        if (faturaId == null || recorrenciaId == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        return new Lancamento(null, ambienteId, contaDoCartaoId, meioId, categoriaId, autorId,
                Sentido.SAIDA, valorCentavos, dia, dia, descricao, Situacao.PREVISTO, null, null,
                faturaId, null, null, null, recorrenciaId, true, null, agora);
    }

    public boolean ehOcorrenciaDeRecorrencia() {
        return recorrenciaId != null;
    }

    public boolean ehParcela() {
        return parcelamentoId != null;
    }

    public Lancamento comValorDaParcela(long novoValorCentavos, Long novaCategoriaId,
            String novaDescricao) {

        return new Lancamento(id, ambienteId, contaId, meioId,
                novaCategoriaId == null ? categoriaId : novaCategoriaId, autorId, sentido,
                novoValorCentavos, dataEvento, dataEfeito,
                novaDescricao == null ? descricao : novaDescricao, situacao, transferenciaId,
                estornoDeId, faturaId, pagamentoDeFaturaId, rolagemDeFatura, parcelamentoId, recorrenciaId,
                doCiclo, estabelecimento, criadoEm);
    }

    public static Lancamento deCompraNoCredito(Long ambienteId, Long contaId, Long meioId,
            Long categoriaId, Long autorId, Sentido sentido, Long valorCentavos,
            LocalDate dataEvento, String descricao, Long faturaId, String estabelecimento,
            Instant agora) {

        if (faturaId == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        return doUsuario(ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEvento, descricao, Situacao.PROVISIONADO,
                faturaId, estabelecimento, agora);
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
                dataEfeito, descricao.trim(), situacao, transferenciaId, null, null, null, null,
                null, null, false, null, agora);

        Lancamento entrada = new Lancamento(null, ambienteId, contaDeDestinoId, null,
                categoriaDaEntradaId, autorId, Sentido.ENTRADA, valorCentavos, dataEvento,
                dataEfeito, descricao.trim(), situacao, transferenciaId, null, null, null, null,
                null, null, false, null, agora);

        return List.of(saida, entrada);
    }

    public static List<Lancamento> parDeRolagem(Long ambienteId, Long contaDoCartaoId,
            Long categoriaDoCreditoId, Long categoriaDoDebitoId, Long autorId,
            long valorCentavos, LocalDate dia, Long faturaQueVenceuId, Long faturaAbertaId,
            long rolagemDeFatura, Instant agora) {

        Lancamento credito = new Lancamento(null, ambienteId, contaDoCartaoId, null,
                categoriaDoCreditoId, autorId, Sentido.ENTRADA, valorCentavos, dia, dia,
                "Rolado para a fatura seguinte", Situacao.REALIZADO, null, null,
                faturaQueVenceuId, null, rolagemDeFatura, null, null, true, null, agora);

        Lancamento debito = new Lancamento(null, ambienteId, contaDoCartaoId, null,
                categoriaDoDebitoId, autorId, Sentido.SAIDA, valorCentavos, dia, dia,
                "Saldo da fatura anterior", Situacao.PROVISIONADO, null, null,
                faturaAbertaId, null, rolagemDeFatura, null, null, true, null, agora);

        return List.of(credito, debito);
    }

    public static List<Lancamento> parDePagamentoDeFatura(Long ambienteId, Long contaPagadoraId,
            Long contaDoCartaoId, Long categoriaDaSaidaId, Long categoriaDaEntradaId,
            Long autorId, Long valorCentavos, LocalDate dia, String descricao, Situacao situacao,
            long transferenciaId, Long faturaQuitadaId, Instant agora) {

        List<Lancamento> par = parDeTransferencia(ambienteId, contaPagadoraId, contaDoCartaoId,
                categoriaDaSaidaId, categoriaDaEntradaId, autorId, valorCentavos, dia, dia,
                descricao, situacao, transferenciaId, agora);

        return List.of(par.getFirst(), par.getLast().quitando(faturaQuitadaId));
    }

    public Lancamento quitando(Long faturaQuitadaId) {
        return new Lancamento(id, ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEfeito, descricao, situacao, transferenciaId,
                estornoDeId, faturaId, faturaQuitadaId, rolagemDeFatura, parcelamentoId, recorrenciaId, doCiclo,
                estabelecimento, criadoEm);
    }

    public Lancamento estornadoEm(LocalDate dia, Long autorDoEstornoId, Long faturaDoEstornoId,
            Situacao situacaoDoEstorno, Instant agora) {

        if (id == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        if (dia.isBefore(dataEvento)) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("dataEvento", "ANTERIOR",
                    "O estorno não acontece antes da compra que ele desfaz.")));
        }
        return new Lancamento(null, ambienteId, contaId, meioId, categoriaId, autorDoEstornoId,
                sentido.invertido(), valorCentavos, dia, dia, "Estorno de " + descricao,
                situacaoDoEstorno, null, id, faturaDoEstornoId, null, null, null, null, false,
                estabelecimento, agora);
    }

    public Lancamento corrigido(Long novaContaId, Long novoMeioId, Long novaCategoriaId,
            Sentido novoSentido, Long novoValorCentavos, LocalDate novaDataEvento,
            LocalDate novaDataEfeito, String novaDescricao, Situacao novaSituacao) {

        if (doCiclo) {
            exigirCorrecaoSoDeValor(novaContaId, novoMeioId, novaCategoriaId, novoSentido,
                    novaDataEvento, novaDataEfeito, novaDescricao, novaSituacao);
        }

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
                transferenciaId, estornoDeId, faturaId, pagamentoDeFaturaId, rolagemDeFatura,
                parcelamentoId, recorrenciaId, doCiclo, estabelecimento, criadoEm);
    }

    public Lancamento corrigidoNaTransferencia(Long novoValorCentavos,
            LocalDate novaDataEvento, String novaDescricao, Situacao novaSituacao) {

        return corrigido(null, null, null, null, novoValorCentavos, novaDataEvento,
                novaDataEvento, novaDescricao, novaSituacao);
    }

    public Lancamento realizadoPelaData(LocalDate hoje) {
        if (situacao != Situacao.PREVISTO || dataEfeito.isAfter(hoje)) {
            return this;
        }
        return comSituacao(Situacao.REALIZADO);
    }

    public Lancamento naFatura(Long novaFaturaId) {
        if (Objects.equals(faturaId, novaFaturaId)) {
            return this;
        }
        exigirDoUsuario();

        return new Lancamento(id, ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEfeito, descricao, situacao, transferenciaId,
                estornoDeId, novaFaturaId, pagamentoDeFaturaId, rolagemDeFatura, parcelamentoId, recorrenciaId,
                doCiclo, estabelecimento, criadoEm);
    }

    public Lancamento comSituacao(Situacao nova) {
        return new Lancamento(id, ambienteId, contaId, meioId, categoriaId, autorId, sentido,
                valorCentavos, dataEvento, dataEfeito, descricao, nova, transferenciaId,
                estornoDeId, faturaId, pagamentoDeFaturaId, rolagemDeFatura, parcelamentoId, recorrenciaId,
                doCiclo, estabelecimento, criadoEm);
    }

    public void exigirExcluivelPeloUsuario(boolean temEstorno) {
        exigirDoUsuario();
        if (ehParcela()) {
            throw new RegraDeDominioException(CodigoDeErro.PARCELA_ISOLADA);
        }
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

    public boolean entraEmFatura() {
        return faturaId != null;
    }

    public long valorComSinal() {
        return sentido.aplicadoA(valorCentavos);
    }

    private void exigirDoUsuario() {
        if (doCiclo) {
            throw new RegraDeDominioException(CodigoDeErro.LANCAMENTO_DO_CICLO);
        }
    }

    private static void exigirCorrecaoSoDeValor(Object... camposQueOCicloNaoEntrega) {
        if (Stream.of(camposQueOCicloNaoEntrega).anyMatch(Objects::nonNull)) {
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
