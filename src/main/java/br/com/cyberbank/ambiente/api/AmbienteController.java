package br.com.cyberbank.ambiente.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import br.com.cyberbank.ambiente.aplicacao.CriarAmbienteUseCase;
import br.com.cyberbank.ambiente.aplicacao.ListarAmbientesUseCase;
import br.com.cyberbank.ambiente.aplicacao.RenomearAmbienteUseCase;
import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes")
public class AmbienteController {

    public record AmbienteResponse(Long id, String nome, Papel papel, Instant criadoEm) {
    }

    public record ListaResponse(List<AmbienteResponse> itens) {
    }

    public record NomeRequest(String nome) {
    }

    private final ListarAmbientesUseCase listarAmbientes;
    private final CriarAmbienteUseCase criarAmbiente;
    private final RenomearAmbienteUseCase renomearAmbiente;

    public AmbienteController(
            ListarAmbientesUseCase listarAmbientes,
            CriarAmbienteUseCase criarAmbiente,
            RenomearAmbienteUseCase renomearAmbiente) {
        this.listarAmbientes = listarAmbientes;
        this.criarAmbiente = criarAmbiente;
        this.renomearAmbiente = renomearAmbiente;
    }

    @GetMapping
    public ListaResponse listar() {
        return new ListaResponse(listarAmbientes.executar(ContextoDaRequisicao.usuarioId()).stream()
                .map(AmbienteController::paraResposta)
                .toList());
    }

    @PostMapping
    public ResponseEntity<AmbienteResponse> criar(@RequestBody NomeRequest requisicao) {
        AcessoAoAmbiente criado =
                criarAmbiente.executar(ContextoDaRequisicao.usuarioId(), requisicao.nome());
        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + criado.ambiente().id()))
                .body(paraResposta(criado));
    }

    @PatchMapping("/{ambienteId}")
    public AmbienteResponse renomear(
            @PathVariable Long ambienteId, @RequestBody NomeRequest requisicao) {
        return paraResposta(renomearAmbiente.executar(
                ContextoDaRequisicao.usuarioId(), ambienteId, requisicao.nome()));
    }

    private static AmbienteResponse paraResposta(AcessoAoAmbiente acesso) {
        return new AmbienteResponse(acesso.ambiente().id(), acesso.ambiente().nome(),
                acesso.papel(), acesso.ambiente().criadoEm());
    }
}
