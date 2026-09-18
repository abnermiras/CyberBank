package br.com.cyberbank.lancamento.api;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.lancamento.aplicacao.EditarLancamentoUseCase;
import br.com.cyberbank.lancamento.aplicacao.EstornarLancamentoUseCase;
import br.com.cyberbank.lancamento.aplicacao.ExcluirLancamentoUseCase;
import br.com.cyberbank.lancamento.aplicacao.LancarUseCase;
import br.com.cyberbank.lancamento.aplicacao.ListarExtratoUseCase;
import br.com.cyberbank.lancamento.aplicacao.DetalheDoLancamento;
import br.com.cyberbank.lancamento.aplicacao.TransferirUseCase;
import br.com.cyberbank.lancamento.aplicacao.VerLancamentoUseCase;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.Pagina;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/lancamentos")
public class LancamentoController {

    public record LancamentoResponse(
            Long id,
            Long contaId,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long meioId,
            Long categoriaId,
            Long autorId,
            Sentido sentido,
            long valor,
            LocalDate dataEvento,
            LocalDate dataEfeito,
            String descricao,
            Situacao situacao,
            boolean doCiclo,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long transferenciaId,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long estornoDeId,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long faturaId,
            @JsonInclude(JsonInclude.Include.NON_NULL) String estabelecimento) {
    }

    public record DetalheResponse(
            Long id,
            Sentido sentido,
            long valor,
            String descricao,
            LocalDate dataEvento,
            LocalDate dataEfeito,
            Situacao situacao,
            boolean doCiclo,
            Instant criadoEm,
            @JsonInclude(JsonInclude.Include.NON_NULL) String estabelecimento,
            @JsonInclude(JsonInclude.Include.NON_NULL) DetalheDoLancamento.ContaDoLancamento conta,
            @JsonInclude(JsonInclude.Include.NON_NULL) DetalheDoLancamento.MeioDoLancamento meio,
            @JsonInclude(JsonInclude.Include.NON_NULL) DetalheDoLancamento.CategoriaDoLancamento categoria,
            @JsonInclude(JsonInclude.Include.NON_NULL) DetalheDoLancamento.FaturaDoLancamento fatura,
            @JsonInclude(JsonInclude.Include.NON_NULL) AutorResponse autor,
            @JsonInclude(JsonInclude.Include.NON_NULL) TransferenciaResponse transferencia,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long estornoDeId,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long estornadoPorId) {
    }

    public record AutorResponse(Long id, String nome) {
    }

    public record TransferenciaResponse(Long id, Long outroLadoId) {
    }

    public record ExtratoResponse(List<LancamentoResponse> itens,
            @JsonInclude(JsonInclude.Include.NON_NULL) String proximo) {
    }

    public record LancamentoRequest(Long meioId, Long categoriaId, Sentido sentido, Long valor,
            LocalDate dataEvento, LocalDate dataEfeito, String descricao, String estabelecimento) {
    }

    public record TransferenciaRequest(Long contaDeOrigemId, Long contaDeDestinoId, Long valor,
            LocalDate dataEvento, String descricao) {
    }

    public record CorrecaoRequest(Long contaId, Long meioId, Long categoriaId, Sentido sentido,
            Long valor, LocalDate dataEvento, LocalDate dataEfeito, String descricao,
            Situacao situacao, Long faturaId) {
    }

    public record EstornoRequest(LocalDate dataEvento) {
    }

    private final ListarExtratoUseCase listarExtrato;
    private final LancarUseCase lancar;
    private final TransferirUseCase transferir;
    private final EditarLancamentoUseCase editarLancamento;
    private final EstornarLancamentoUseCase estornarLancamento;
    private final ExcluirLancamentoUseCase excluirLancamento;
    private final VerLancamentoUseCase verLancamento;

    public LancamentoController(ListarExtratoUseCase listarExtrato, LancarUseCase lancar,
            TransferirUseCase transferir, EditarLancamentoUseCase editarLancamento,
            EstornarLancamentoUseCase estornarLancamento,
            ExcluirLancamentoUseCase excluirLancamento,
            VerLancamentoUseCase verLancamento) {
        this.listarExtrato = listarExtrato;
        this.lancar = lancar;
        this.transferir = transferir;
        this.editarLancamento = editarLancamento;
        this.estornarLancamento = estornarLancamento;
        this.excluirLancamento = excluirLancamento;
        this.verLancamento = verLancamento;
    }

    @GetMapping
    public ExtratoResponse extrato(@PathVariable Long ambienteId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(defaultValue = "false") boolean pendentes,
            @RequestParam(required = false) String apos,
            @RequestParam(required = false) Integer limite) {

        Pagina pagina = listarExtrato.executar(ambienteId, contaId, pendentes, apos, limite);

        return new ExtratoResponse(
                pagina.itens().stream().map(LancamentoController::paraResposta).toList(),
                pagina.proximo() == null ? null : pagina.proximo().codificado());
    }

    @GetMapping("/{lancamentoId}")
    public DetalheResponse detalhe(@PathVariable Long ambienteId,
            @PathVariable Long lancamentoId) {

        DetalheDoLancamento detalhe = verLancamento.executar(ambienteId, lancamentoId);
        Lancamento l = detalhe.lancamento();

        return new DetalheResponse(l.id(), l.sentido(), l.valorCentavos(), l.descricao(),
                l.dataEvento(), l.dataEfeito(), l.situacao(), l.doCiclo(), l.criadoEm(),
                l.estabelecimento(), detalhe.conta(), detalhe.meio(), detalhe.categoria(),
                detalhe.fatura(),
                detalhe.autor() == null ? null
                        : new AutorResponse(l.autorId(), detalhe.autor()),
                l.transferenciaId() == null ? null
                        : new TransferenciaResponse(l.transferenciaId(),
                                detalhe.outroLadoDaTransferenciaId()),
                l.estornoDeId(), detalhe.estornadoPorId());
    }

    @PostMapping
    public ResponseEntity<LancamentoResponse> lancar(@PathVariable Long ambienteId,
            @RequestBody LancamentoRequest requisicao) {

        Lancamento criado = lancar.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                requisicao.meioId(), requisicao.categoriaId(), requisicao.sentido(),
                requisicao.valor(), requisicao.dataEvento(), requisicao.dataEfeito(),
                requisicao.descricao(), requisicao.estabelecimento());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId
                        + "/lancamentos/" + criado.id()))
                .body(paraResposta(criado));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<ExtratoResponse> transferir(@PathVariable Long ambienteId,
            @RequestBody TransferenciaRequest requisicao) {

        List<Lancamento> par = transferir.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                requisicao.contaDeOrigemId(), requisicao.contaDeDestinoId(), requisicao.valor(),
                requisicao.dataEvento(), requisicao.descricao());

        return ResponseEntity.created(URI.create("/api/v1/ambientes/" + ambienteId
                        + "/lancamentos?contaId=" + requisicao.contaDeOrigemId()))
                .body(new ExtratoResponse(
                        par.stream().map(LancamentoController::paraResposta).toList(), null));
    }

    @PatchMapping("/{lancamentoId}")
    public ExtratoResponse corrigir(@PathVariable Long ambienteId,
            @PathVariable Long lancamentoId, @RequestBody CorrecaoRequest requisicao) {

        List<Lancamento> alterados = editarLancamento.executar(ambienteId,
                ContextoDaRequisicao.usuarioId(), lancamentoId, requisicao.contaId(),
                requisicao.meioId(), requisicao.categoriaId(), requisicao.sentido(),
                requisicao.valor(), requisicao.dataEvento(), requisicao.dataEfeito(),
                requisicao.descricao(), requisicao.situacao(), requisicao.faturaId());

        return new ExtratoResponse(
                alterados.stream().map(LancamentoController::paraResposta).toList(), null);
    }

    @PostMapping("/{lancamentoId}/estorno")
    public ResponseEntity<LancamentoResponse> estornar(@PathVariable Long ambienteId,
            @PathVariable Long lancamentoId, @RequestBody(required = false) EstornoRequest requisicao) {

        Lancamento estorno = estornarLancamento.executar(ambienteId,
                ContextoDaRequisicao.usuarioId(), lancamentoId,
                requisicao == null ? null : requisicao.dataEvento());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId
                        + "/lancamentos/" + estorno.id()))
                .body(paraResposta(estorno));
    }

    @DeleteMapping("/{lancamentoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long ambienteId,
            @PathVariable Long lancamentoId) {
        excluirLancamento.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                lancamentoId);
        return ResponseEntity.noContent().build();
    }

    private static LancamentoResponse paraResposta(Lancamento l) {
        return new LancamentoResponse(l.id(), l.contaId(), l.meioId(), l.categoriaId(),
                l.autorId(), l.sentido(), l.valorCentavos(), l.dataEvento(), l.dataEfeito(),
                l.descricao(), l.situacao(), l.doCiclo(), l.transferenciaId(), l.estornoDeId(),
                l.faturaId(), l.estabelecimento());
    }
}
