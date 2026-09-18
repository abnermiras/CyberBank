package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record CicloDaFatura(int diaVencimento, int diasAntesFechamento) {

    public static final int DIAS_ANTES_DO_FECHAMENTO_MAXIMO = 28;

    public CicloDaFatura {
        List<ErroDeValidacao> erros = new ArrayList<>();
        if (diaVencimento < 1 || diaVencimento > 31) {
            erros.add(new ErroDeValidacao("diaVencimento", "FORA_DO_MES",
                    "O dia do vencimento está entre 1 e 31."));
        }
        if (diasAntesFechamento < 1 || diasAntesFechamento > DIAS_ANTES_DO_FECHAMENTO_MAXIMO) {
            erros.add(new ErroDeValidacao("diasAntesFechamento", "FORA_DA_FAIXA",
                    "A fatura fecha de 1 a " + DIAS_ANTES_DO_FECHAMENTO_MAXIMO
                            + " dias antes de vencer."));
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public LocalDate vencimentoDe(YearMonth competencia) {
        return competencia.atDay(Math.min(diaVencimento, competencia.lengthOfMonth()));
    }

    public LocalDate fechamentoDe(YearMonth competencia) {
        return vencimentoDe(competencia).minusDays(diasAntesFechamento);
    }

    public YearMonth competenciaCorrenteEm(LocalDate hoje) {
        YearMonth competencia = YearMonth.from(hoje);
        while (fechamentoDe(competencia).isBefore(hoje)) {
            competencia = competencia.plusMonths(1);
        }
        return competencia;
    }
}
