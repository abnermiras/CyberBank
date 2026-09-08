package br.com.cyberbank.ambiente.api;

import java.util.List;

import br.com.cyberbank.ambiente.aplicacao.ListarAmbientesUseCase;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes")
public class AmbienteController {

    public record AmbienteResponse(Long id, String nome, Papel papel) {
    }

    public record ListaResponse(List<AmbienteResponse> itens) {
    }

    private final ListarAmbientesUseCase listarAmbientes;

    public AmbienteController(ListarAmbientesUseCase listarAmbientes) {
        this.listarAmbientes = listarAmbientes;
    }

    @GetMapping
    public ListaResponse listar() {
        return new ListaResponse(listarAmbientes.executar(ContextoDaRequisicao.usuarioId()).stream()
                .map(acesso -> new AmbienteResponse(
                        acesso.ambiente().id(), acesso.ambiente().nome(), acesso.papel()))
                .toList());
    }
}
