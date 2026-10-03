package br.com.cyberbank.ambiente.api;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.ambiente.aplicacao.ListarMembrosUseCase;
import br.com.cyberbank.ambiente.aplicacao.Membro;
import br.com.cyberbank.ambiente.aplicacao.RemoverAcessoUseCase;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.usuario.dominio.Avatar;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/membros")
public class MembroController {

    public record MembroResponse(Long usuarioId, String nome, String email, Avatar avatar,
            Papel papel, Instant desde, boolean voce) {
    }

    public record MembrosResponse(List<MembroResponse> itens) {
    }

    private final ListarMembrosUseCase listar;
    private final RemoverAcessoUseCase remover;

    public MembroController(ListarMembrosUseCase listar, RemoverAcessoUseCase remover) {
        this.listar = listar;
        this.remover = remover;
    }

    @GetMapping
    public MembrosResponse listar(@PathVariable Long ambienteId) {
        Long eu = ContextoDaRequisicao.usuarioId();
        return new MembrosResponse(listar.executar(ambienteId).stream()
                .map(membro -> paraResposta(membro, eu))
                .toList());
    }

    @AbertoAoLeitor
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> remover(@PathVariable Long ambienteId,
            @PathVariable Long usuarioId) {
        remover.executar(ContextoDaRequisicao.usuarioId(), ambienteId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private static MembroResponse paraResposta(Membro membro, Long eu) {
        return new MembroResponse(membro.acesso().usuarioId(), membro.nome(), membro.email(),
                membro.avatar(), membro.acesso().papel(), membro.acesso().criadoEm(),
                membro.acesso().usuarioId().equals(eu));
    }
}
