package br.com.cyberbank.recorrencia.aplicacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Situacao;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;
import br.com.cyberbank.recorrencia.dominio.RecorrenciaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerSeriesUseCase {

    private final RecorrenciaRepository recorrencias;
    private final ParcelamentoRepository parcelamentos;
    private final LancamentoRepository lancamentos;

    public VerSeriesUseCase(RecorrenciaRepository recorrencias,
            ParcelamentoRepository parcelamentos, LancamentoRepository lancamentos) {
        this.recorrencias = recorrencias;
        this.parcelamentos = parcelamentos;
        this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public SerieNaLista executar(Long ambienteId) {
        List<SerieNaLista.RecorrenciaViva> vivas = new ArrayList<>();
        for (Recorrencia recorrencia : recorrencias.listarDoAmbiente(ambienteId)) {
            vivas.add(viva(recorrencia, lancamentos.listarDaRecorrencia(recorrencia.id(), ambienteId)));
        }

        List<SerieNaLista.ParcelamentoVivo> compras = new ArrayList<>();
        for (Parcelamento parcelamento : parcelamentos.listarDoAmbiente(ambienteId)) {
            compras.add(vivo(parcelamento,
                    lancamentos.listarDoParcelamento(parcelamento.id(), ambienteId)));
        }

        return new SerieNaLista(vivas, compras);
    }

    private SerieNaLista.RecorrenciaViva viva(Recorrencia recorrencia, List<Lancamento> ocorrencias) {
        Optional<Lancamento> proxima = ocorrencias.stream()
                .filter(o -> o.situacao() == Situacao.PREVISTO)
                .min(Comparator.comparing(Lancamento::dataEvento));

        long cobrado = ocorrencias.stream()
                .filter(o -> o.situacao() != Situacao.PREVISTO)
                .mapToLong(Lancamento::valorCentavos)
                .sum();

        return new SerieNaLista.RecorrenciaViva(recorrencia,
                proxima.map(Lancamento::dataEvento).orElse(null),
                proxima.map(Lancamento::id).orElse(null),
                ocorrencias.size(), cobrado);
    }

    private SerieNaLista.ParcelamentoVivo vivo(Parcelamento parcelamento, List<Lancamento> parcelas) {
        long liquidado = parcelas.stream()
                .filter(p -> p.situacao() == Situacao.REALIZADO)
                .mapToLong(Lancamento::valorCentavos)
                .sum();

        int liquidadas = (int) parcelas.stream()
                .filter(p -> p.situacao() == Situacao.REALIZADO)
                .count();

        LocalDate proxima = parcelas.stream()
                .filter(p -> p.situacao() != Situacao.REALIZADO)
                .map(Lancamento::dataEfeito)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return new SerieNaLista.ParcelamentoVivo(parcelamento, liquidadas, liquidado,
                parcelamento.valorDaCompraCentavos() - liquidado, proxima);
    }
}
