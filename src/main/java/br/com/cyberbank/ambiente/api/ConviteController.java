package br.com.cyberbank.ambiente.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import br.com.cyberbank.ambiente.api.AmbienteController.AmbienteResponse;
import br.com.cyberbank.ambiente.aplicacao.AceitarConviteUseCase;
import br.com.cyberbank.ambiente.aplicacao.CancelarConviteUseCase;
import br.com.cyberbank.ambiente.aplicacao.ConvidarParaAmbienteUseCase;
import br.com.cyberbank.ambiente.aplicacao.ConviteRecebido;
import br.com.cyberbank.ambiente.aplicacao.ListarConvitesDoAmbienteUseCase;
import br.com.cyberbank.ambiente.aplicacao.ListarConvitesRecebidosUseCase;
import br.com.cyberbank.ambiente.aplicacao.RecusarConviteUseCase;
import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.Papel;
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
@RequestMapping("/api/v1")
public class ConviteController {

    public record ConviteRequest(String email, Papel papel) {
    }

    public record ConviteResponse(Long id, String email, Papel papel, Instant criadoEm) {
    }

    public record ConvitesResponse(List<ConviteResponse> itens) {
    }

    public record ConviteRecebidoResponse(Long id, Long ambienteId, String ambienteNome,
            Papel papel, String convidadoPor, Instant criadoEm) {
    }

    public record ConvitesRecebidosResponse(List<ConviteRecebidoResponse> itens) {
    }

    private final ConvidarParaAmbienteUseCase convidar;
    private final ListarConvitesDoAmbienteUseCase listarDoAmbiente;
    private final CancelarConviteUseCase cancelar;
    private final ListarConvitesRecebidosUseCase listarRecebidos;
    private final AceitarConviteUseCase aceitar;
    private final RecusarConviteUseCase recusar;

    public ConviteController(ConvidarParaAmbienteUseCase convidar,
            ListarConvitesDoAmbienteUseCase listarDoAmbiente, CancelarConviteUseCase cancelar,
            ListarConvitesRecebidosUseCase listarRecebidos, AceitarConviteUseCase aceitar,
            RecusarConviteUseCase recusar) {
        this.convidar = convidar;
        this.listarDoAmbiente = listarDoAmbiente;
        this.cancelar = cancelar;
        this.listarRecebidos = listarRecebidos;
        this.aceitar = aceitar;
        this.recusar = recusar;
    }

    @GetMapping("/ambientes/{ambienteId}/convites")
    public ConvitesResponse listarDoAmbiente(@PathVariable Long ambienteId) {
        return new ConvitesResponse(
                listarDoAmbiente.executar(ContextoDaRequisicao.usuarioId(), ambienteId).stream()
                        .map(ConviteController::paraResposta)
                        .toList());
    }

    @PostMapping("/ambientes/{ambienteId}/convites")
    public ResponseEntity<ConviteResponse> convidar(@PathVariable Long ambienteId,
            @RequestBody ConviteRequest requisicao) {
        Convite criado = convidar.executar(ContextoDaRequisicao.usuarioId(), ambienteId,
                requisicao.email(), requisicao.papel());
        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/convites/" + criado.id()))
                .body(paraResposta(criado));
    }

    @DeleteMapping("/ambientes/{ambienteId}/convites/{conviteId}")
    public ResponseEntity<Void> cancelar(@PathVariable Long ambienteId,
            @PathVariable Long conviteId) {
        cancelar.executar(ContextoDaRequisicao.usuarioId(), ambienteId, conviteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/convites")
    public ConvitesRecebidosResponse listarRecebidos() {
        return new ConvitesRecebidosResponse(
                listarRecebidos.executar(ContextoDaRequisicao.usuarioId()).stream()
                        .map(ConviteController::paraResposta)
                        .toList());
    }

    @PostMapping("/convites/{conviteId}/aceite")
    public AmbienteResponse aceitar(@PathVariable Long conviteId) {
        AcessoAoAmbiente acesso = aceitar.executar(ContextoDaRequisicao.usuarioId(), conviteId);
        return new AmbienteResponse(acesso.ambiente().id(), acesso.ambiente().nome(),
                acesso.papel(), acesso.ambiente().criadoEm());
    }

    @PostMapping("/convites/{conviteId}/recusa")
    public ResponseEntity<Void> recusar(@PathVariable Long conviteId) {
        recusar.executar(ContextoDaRequisicao.usuarioId(), conviteId);
        return ResponseEntity.noContent().build();
    }

    private static ConviteResponse paraResposta(Convite convite) {
        return new ConviteResponse(convite.id(), convite.email(), convite.papel(),
                convite.criadoEm());
    }

    private static ConviteRecebidoResponse paraResposta(ConviteRecebido recebido) {
        Convite convite = recebido.convite();
        return new ConviteRecebidoResponse(convite.id(), convite.ambienteId(),
                recebido.ambienteNome(), convite.papel(), recebido.convidadoPorNome(),
                convite.criadoEm());
    }
}
