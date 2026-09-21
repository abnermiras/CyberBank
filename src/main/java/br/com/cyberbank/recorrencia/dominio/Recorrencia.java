package br.com.cyberbank.recorrencia.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Recorrencia(
        Long id,
        Long ambienteId,
        Long contaId,
        Long meioId,
        Long categoriaId,
        long valorCentavos,
        Periodicidade periodicidade,
        int dia,
        LocalDate inicio,
        boolean ativa,
        String descricao,
        Instant criadoEm) {

    public static final int PRIMEIRO_DIA = 1;
    public static final int ULTIMO_DIA = 31;
    public static final int TAMANHO_MAXIMO_DA_DESCRICAO = 200;

    public static Recorrencia nova(Long ambienteId, Long contaId, Long meioId, Long categoriaId,
            Long valorCentavos, Periodicidade periodicidade, Integer dia, LocalDate inicio,
            String descricao, Instant agora) {

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valorCentavos, erros);
        validarDia(dia, erros);
        validarDescricao(descricao, erros);
        if (periodicidade == null) {
            erros.add(new ErroDeValidacao("periodicidade", "OBRIGATORIO",
                    "Informe a periodicidade."));
        }
        if (inicio == null) {
            erros.add(new ErroDeValidacao("inicio", "OBRIGATORIO",
                    "Informe a partir de quando a recorrência vale."));
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        return new Recorrencia(null, ambienteId, contaId, meioId, categoriaId, valorCentavos,
                periodicidade, dia, inicio, true, descricao.trim(), agora);
    }

    public LocalDate dataNaCompetencia(YearMonth competencia) {
        return competencia.atDay(Math.min(dia, competencia.lengthOfMonth()));
    }

    public boolean valeNaCompetencia(YearMonth competencia) {
        return ativa && !dataNaCompetencia(competencia).isBefore(inicio);
    }

    private static void validarValor(Long valorCentavos, List<ErroDeValidacao> erros) {
        if (valorCentavos == null || valorCentavos <= 0) {
            erros.add(new ErroDeValidacao("valor", "POSITIVO",
                    "O valor da ocorrência é sempre positivo."));
        }
    }

    private static void validarDia(Integer dia, List<ErroDeValidacao> erros) {
        if (dia == null || dia < PRIMEIRO_DIA || dia > ULTIMO_DIA) {
            erros.add(new ErroDeValidacao("dia", "FORA_DA_FAIXA",
                    "O dia da cobrança vai de " + PRIMEIRO_DIA + " a " + ULTIMO_DIA + "."));
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
}
