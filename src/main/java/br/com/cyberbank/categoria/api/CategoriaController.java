package br.com.cyberbank.categoria.api;

import java.net.URI;
import java.util.List;

import br.com.cyberbank.categoria.aplicacao.AlterarAtivacaoCategoriaUseCase;
import br.com.cyberbank.categoria.aplicacao.CriarCategoriaUseCase;
import br.com.cyberbank.categoria.aplicacao.ExcluirCategoriaUseCase;
import br.com.cyberbank.categoria.aplicacao.ListarCategoriasUseCase;
import br.com.cyberbank.categoria.aplicacao.RecolorirCategoriaUseCase;
import br.com.cyberbank.categoria.aplicacao.RenomearCategoriaUseCase;
import br.com.cyberbank.categoria.dominio.ArvoreDeCategorias;
import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CorDeCategoria;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

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
@RequestMapping("/api/v1/ambientes/{ambienteId}/categorias")
public class CategoriaController {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaResponse(
            Long id,
            String nome,
            Sentido sentido,
            CorDeCategoria cor,
            boolean sistema,
            boolean inativa,
            boolean escolhivel,
            OperacaoDeSistema operacao,
            List<CategoriaResponse> filhas) {
    }

    public record ListaResponse(List<CategoriaResponse> itens) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaGravadaResponse(
            Long id, Long paiId, String nome, Sentido sentido, CorDeCategoria cor, boolean inativa) {
    }

    public record CriacaoRequest(Long paiId, String nome, Sentido sentido, CorDeCategoria cor) {
    }

    public record AlteracaoRequest(String nome, Boolean inativa, CorDeCategoria cor) {
    }

    private final ListarCategoriasUseCase listarCategorias;
    private final CriarCategoriaUseCase criarCategoria;
    private final RenomearCategoriaUseCase renomearCategoria;
    private final AlterarAtivacaoCategoriaUseCase alterarAtivacao;
    private final RecolorirCategoriaUseCase recolorirCategoria;
    private final ExcluirCategoriaUseCase excluirCategoria;

    public CategoriaController(
            ListarCategoriasUseCase listarCategorias,
            CriarCategoriaUseCase criarCategoria,
            RenomearCategoriaUseCase renomearCategoria,
            AlterarAtivacaoCategoriaUseCase alterarAtivacao,
            RecolorirCategoriaUseCase recolorirCategoria,
            ExcluirCategoriaUseCase excluirCategoria) {
        this.listarCategorias = listarCategorias;
        this.criarCategoria = criarCategoria;
        this.renomearCategoria = renomearCategoria;
        this.alterarAtivacao = alterarAtivacao;
        this.recolorirCategoria = recolorirCategoria;
        this.excluirCategoria = excluirCategoria;
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

    @PostMapping
    public ResponseEntity<CategoriaGravadaResponse> criar(
            @PathVariable Long ambienteId, @RequestBody CriacaoRequest requisicao) {
        Categoria criada = criarCategoria.executar(ambienteId, ContextoDaRequisicao.usuarioId(), requisicao.paiId(),
                requisicao.nome(), requisicao.sentido(), requisicao.cor());
        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/categorias/" + criada.id()))
                .body(paraRespostaGravada(criada));
    }

    @PatchMapping("/{categoriaId}")
    public CategoriaGravadaResponse alterar(
            @PathVariable Long ambienteId,
            @PathVariable Long categoriaId,
            @RequestBody AlteracaoRequest requisicao) {

        if (requisicao.nome() == null && requisicao.inativa() == null && requisicao.cor() == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("nome", "OBRIGATORIO",
                    "Informe o que mudar: nome, cor, inativa, ou uma combinação deles.")));
        }

        Categoria categoria = null;
        if (requisicao.nome() != null) {
            categoria = renomearCategoria.executar(ambienteId, ContextoDaRequisicao.usuarioId(), categoriaId,
                    requisicao.nome());
        }
        if (requisicao.cor() != null) {
            categoria = recolorirCategoria.executar(ambienteId, ContextoDaRequisicao.usuarioId(), categoriaId,
                    requisicao.cor());
        }
        if (requisicao.inativa() != null) {
            categoria = alterarAtivacao.executar(ambienteId, ContextoDaRequisicao.usuarioId(), categoriaId,
                    requisicao.inativa());
        }
        return paraRespostaGravada(categoria);
    }

    @DeleteMapping("/{categoriaId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long ambienteId, @PathVariable Long categoriaId) {
        excluirCategoria.executar(ambienteId, ContextoDaRequisicao.usuarioId(), categoriaId);
        return ResponseEntity.noContent().build();
    }

    private static CategoriaGravadaResponse paraRespostaGravada(Categoria categoria) {
        return new CategoriaGravadaResponse(categoria.id(), categoria.paiId(), categoria.nome(),
                categoria.sentido(), categoria.cor(), categoria.inativa());
    }

    private static CategoriaResponse paraResposta(ArvoreDeCategorias.No no) {
        var categoria = no.categoria();
        return new CategoriaResponse(
                categoria.id(),
                categoria.nome(),
                categoria.sentido(),
                categoria.cor(),
                categoria.sistema(),
                categoria.inativa(),
                no.escolhivel(),
                categoria.operacao(),
                no.filhas().stream().map(CategoriaController::paraResposta).toList());
    }
}
