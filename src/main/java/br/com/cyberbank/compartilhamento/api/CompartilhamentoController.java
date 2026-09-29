package br.com.cyberbank.compartilhamento.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import br.com.cyberbank.compartilhamento.aplicacao.CompartilharContaUseCase;
import br.com.cyberbank.compartilhamento.aplicacao.ListarCompartilhamentosUseCase;
import br.com.cyberbank.compartilhamento.aplicacao.RevogarCompartilhamentoUseCase;
import br.com.cyberbank.compartilhamento.dominio.Compartilhamento;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/contas/{contaId}/compartilhamentos")
public class CompartilhamentoController {

    public record CompartilhamentoRequest(Long ambienteDestinoId) {
    }

    public record CompartilhamentoResponse(Long ambienteDestinoId, String ambienteDestinoNome,
            Instant criadoEm) {
    }

    public record ListaResponse(List<CompartilhamentoResponse> itens) {
    }

    private final CompartilharContaUseCase compartilhar;
    private final RevogarCompartilhamentoUseCase revogar;
    private final ListarCompartilhamentosUseCase listar;

    public CompartilhamentoController(CompartilharContaUseCase compartilhar,
            RevogarCompartilhamentoUseCase revogar, ListarCompartilhamentosUseCase listar) {
        this.compartilhar = compartilhar;
        this.revogar = revogar;
        this.listar = listar;
    }

    @GetMapping
    public ListaResponse listar(@PathVariable Long ambienteId, @PathVariable Long contaId) {
        return new ListaResponse(listar.executar(ambienteId, contaId).stream()
                .map(c -> new CompartilhamentoResponse(c.ambienteDestinoId(),
                        c.ambienteDestinoNome(), c.criadoEm()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<CompartilhamentoResponse> compartilhar(@PathVariable Long ambienteId,
            @PathVariable Long contaId, @RequestBody CompartilhamentoRequest requisicao) {

        Compartilhamento criado = compartilhar.executar(ContextoDaRequisicao.usuarioId(),
                ambienteId, contaId, requisicao.ambienteDestinoId());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/contas/" + contaId
                        + "/compartilhamentos"))
                .body(new CompartilhamentoResponse(criado.ambienteDestinoId(), null,
                        criado.criadoEm()));
    }

    @DeleteMapping("/{ambienteDestinoId}")
    public ResponseEntity<Void> revogar(@PathVariable Long ambienteId,
            @PathVariable Long contaId, @PathVariable Long ambienteDestinoId) {
        revogar.executar(ContextoDaRequisicao.usuarioId(), ambienteId, contaId,
                ambienteDestinoId);
        return ResponseEntity.noContent().build();
    }
}
