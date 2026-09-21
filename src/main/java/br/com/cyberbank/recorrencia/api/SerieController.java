package br.com.cyberbank.recorrencia.api;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.recorrencia.aplicacao.SerieNaLista;
import br.com.cyberbank.recorrencia.aplicacao.VerSeriesUseCase;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/series")
public class SerieController {

    public record RecorrenciaNaListaResponse(Long id, Long contaId, Long meioId, Long categoriaId,
            long valorCentavos, String periodicidade, int dia, LocalDate inicio, boolean ativa,
            String descricao, LocalDate proximaCobrancaEm, Long proximaOcorrenciaId,
            int ocorrenciasLancadas, long jaCobradoCentavos) {
    }

    public record ParcelamentoNaListaResponse(Long id, Long contaId, Long meioId,
            Long categoriaId, long valorDaCompraCentavos, int parcelas, LocalDate dataDaCompra,
            String descricao, int parcelasLiquidadas, long liquidadoCentavos,
            long restanteCentavos, LocalDate proximaParcelaEm) {
    }

    public record SeriesResponse(List<RecorrenciaNaListaResponse> recorrencias,
            List<ParcelamentoNaListaResponse> parcelamentos) {
    }

    private final VerSeriesUseCase verSeries;

    public SerieController(VerSeriesUseCase verSeries) {
        this.verSeries = verSeries;
    }

    @GetMapping
    public SeriesResponse listar(@PathVariable Long ambienteId) {
        SerieNaLista series = verSeries.executar(ambienteId);

        return new SeriesResponse(
                series.recorrencias().stream().map(SerieController::paraResposta).toList(),
                series.parcelamentos().stream().map(SerieController::paraResposta).toList());
    }

    private static RecorrenciaNaListaResponse paraResposta(SerieNaLista.RecorrenciaViva viva) {
        Recorrencia r = viva.recorrencia();
        return new RecorrenciaNaListaResponse(r.id(), r.contaId(), r.meioId(), r.categoriaId(),
                r.valorCentavos(), r.periodicidade().name(), r.dia(), r.inicio(), r.ativa(),
                r.descricao(), viva.proximaCobrancaEm(), viva.proximaOcorrenciaId(),
                viva.ocorrenciasLancadas(), viva.jaCobradoCentavos());
    }

    private static ParcelamentoNaListaResponse paraResposta(SerieNaLista.ParcelamentoVivo vivo) {
        Parcelamento p = vivo.parcelamento();
        return new ParcelamentoNaListaResponse(p.id(), p.contaId(), p.meioId(), p.categoriaId(),
                p.valorDaCompraCentavos(), p.parcelas(), p.dataDaCompra(), p.descricao(),
                vivo.parcelasLiquidadas(), vivo.liquidadoCentavos(), vivo.restanteCentavos(),
                vivo.proximaParcelaEm());
    }
}
