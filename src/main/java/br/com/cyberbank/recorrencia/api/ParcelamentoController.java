package br.com.cyberbank.recorrencia.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.recorrencia.aplicacao.EditarParcelamentoUseCase;
import br.com.cyberbank.recorrencia.aplicacao.ExcluirParcelamentoUseCase;
import br.com.cyberbank.recorrencia.aplicacao.ParcelamentoComParcelas;
import br.com.cyberbank.recorrencia.aplicacao.ParcelarCompraUseCase;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/parcelamentos")
public class ParcelamentoController {

    public record ParcelaResponse(Long id, int numero, long valorCentavos, Long faturaId,
            String situacao) {
    }

    public record ParcelamentoResponse(Long id, Long contaId, Long meioId, Long categoriaId,
            long valorDaCompraCentavos, int parcelas, LocalDate dataDaCompra, String descricao,
            List<ParcelaResponse> itens) {
    }

    public record CompraParceladaRequest(Long meioId, Long categoriaId, Long valor,
            Integer parcelas, LocalDate dataEvento, String descricao) {
    }

    public record CorrecaoRequest(Long valor, Long categoriaId, String descricao) {
    }

    private final ParcelarCompraUseCase parcelarCompra;
    private final EditarParcelamentoUseCase editarParcelamento;
    private final ExcluirParcelamentoUseCase excluirParcelamento;

    public ParcelamentoController(ParcelarCompraUseCase parcelarCompra,
            EditarParcelamentoUseCase editarParcelamento,
            ExcluirParcelamentoUseCase excluirParcelamento) {
        this.parcelarCompra = parcelarCompra;
        this.editarParcelamento = editarParcelamento;
        this.excluirParcelamento = excluirParcelamento;
    }

    @PostMapping
    public ResponseEntity<ParcelamentoResponse> parcelar(@PathVariable Long ambienteId,
            @RequestBody CompraParceladaRequest requisicao) {

        ParcelamentoComParcelas criado = parcelarCompra.executar(ambienteId,
                ContextoDaRequisicao.usuarioId(), requisicao.meioId(), requisicao.categoriaId(),
                requisicao.valor(), requisicao.parcelas(), requisicao.dataEvento(),
                requisicao.descricao());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/parcelamentos/"
                        + criado.parcelamento().id()))
                .body(paraResposta(criado));
    }

    @PatchMapping("/{parcelamentoId}")
    public ParcelamentoResponse corrigir(@PathVariable Long ambienteId,
            @PathVariable Long parcelamentoId, @RequestBody CorrecaoRequest requisicao) {

        return paraResposta(editarParcelamento.executar(ambienteId,
                ContextoDaRequisicao.usuarioId(), parcelamentoId, requisicao.valor(),
                requisicao.categoriaId(), requisicao.descricao()));
    }

    @DeleteMapping("/{parcelamentoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long ambienteId,
            @PathVariable Long parcelamentoId) {

        excluirParcelamento.executar(ambienteId, ContextoDaRequisicao.usuarioId(), parcelamentoId);
        return ResponseEntity.noContent().build();
    }

    private static ParcelamentoResponse paraResposta(ParcelamentoComParcelas comParcelas) {
        Parcelamento p = comParcelas.parcelamento();
        List<Lancamento> parcelas = comParcelas.parcelas();

        List<ParcelaResponse> itens = new ArrayList<>(parcelas.size());
        for (int indice = 0; indice < parcelas.size(); indice++) {
            Lancamento parcela = parcelas.get(indice);
            itens.add(new ParcelaResponse(parcela.id(), indice + 1, parcela.valorCentavos(),
                    parcela.faturaId(), parcela.situacao().name()));
        }

        return new ParcelamentoResponse(p.id(), p.contaId(), p.meioId(), p.categoriaId(),
                p.valorDaCompraCentavos(), p.parcelas(), p.dataDaCompra(), p.descricao(), itens);
    }
}
