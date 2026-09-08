package br.com.cyberbank.categoria.api;

import java.util.List;

import br.com.cyberbank.categoria.aplicacao.ListarCategoriasUseCase;
import br.com.cyberbank.categoria.dominio.ArvoreDeCategorias;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Familia do ambiente: categoria tem {@code ambiente_id}, entao o endpoint dela vive sob
 * {@code /ambientes/{ambienteId}/}, sem excecao (docs/04-api/convencoes.md). O
 * {@code ambienteId} do caminho ja passou pelo interceptador que validou o acesso.
 */
@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/categorias")
public class CategoriaController {

    /** {@code operacao} so vem em categoria de sistema: campo que nao se aplica NAO VEM. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaResponse(
            Long id,
            String nome,
            Sentido sentido,
            boolean sistema,
            boolean inativa,
            boolean escolhivel,
            OperacaoDeSistema operacao,
            List<CategoriaResponse> filhas) {
    }

    public record ListaResponse(List<CategoriaResponse> itens) {
    }

    private final ListarCategoriasUseCase listarCategorias;

    public CategoriaController(ListarCategoriasUseCase listarCategorias) {
        this.listarCategorias = listarCategorias;
    }

    @GetMapping
    public ListaResponse listar(
            @PathVariable Long ambienteId,
            @RequestParam(defaultValue = "false") boolean sistema,
            @RequestParam(defaultValue = "false") boolean inativas) {
        return new ListaResponse(
                listarCategorias.executar(ambienteId, sistema, inativas).stream()
                        .map(CategoriaController::paraResposta)
                        .toList());
    }

    private static CategoriaResponse paraResposta(ArvoreDeCategorias.No no) {
        var categoria = no.categoria();
        return new CategoriaResponse(
                categoria.id(),
                categoria.nome(),
                categoria.sentido(),
                categoria.sistema(),
                categoria.inativa(),
                no.escolhivel(),
                categoria.operacao(),
                no.filhas().stream().map(CategoriaController::paraResposta).toList());
    }
}
