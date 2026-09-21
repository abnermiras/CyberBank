package br.com.cyberbank.recorrencia.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;

public record SerieNaLista(
        List<RecorrenciaViva> recorrencias,
        List<ParcelamentoVivo> parcelamentos) {

    public record RecorrenciaViva(
            Recorrencia recorrencia,
            LocalDate proximaCobrancaEm,
            Long proximaOcorrenciaId,
            int ocorrenciasLancadas,
            long jaCobradoCentavos) {
    }

    public record ParcelamentoVivo(
            Parcelamento parcelamento,
            int parcelasLiquidadas,
            long liquidadoCentavos,
            long restanteCentavos,
            LocalDate proximaParcelaEm) {
    }
}
