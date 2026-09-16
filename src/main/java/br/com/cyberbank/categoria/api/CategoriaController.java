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
            CorDeCategoria cor,
            boolean sistema,
            boolean inativa,
            boolean escolhivel,
            OperacaoDeSistema operacao,
            List<CategoriaResponse> filhas) {
    }

    public record ListaResponse(List<CategoriaResponse> itens) {
    }

    /**
     * A resposta de escrita NAO tem {@code escolhivel}, e e de proposito: escolhivel e derivado
     * da arvore inteira, nao da linha. Criar uma subcategoria muda o {@code escolhivel} DA
     * RAIZ — entao a tela tem mesmo que reler a arvore depois de escrever, e uma resposta com
     * um escolhivel calculado no vazio so faria ela confiar num valor errado.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoriaGravadaResponse(
            Long id, Long paiId, String nome, Sentido sentido, CorDeCategoria cor, boolean inativa) {
    }

    /** {@code sentido} e nulo numa subcategoria: ela herda o da raiz e nao pode divergir. */
    public record CriacaoRequest(Long paiId, String nome, Sentido sentido, CorDeCategoria cor) {
    }

    /** Ambos opcionais, e ausente e diferente de nulo: o que nao veio nao muda. */
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
        Categoria criada = criarCategoria.executar(ambienteId, requisicao.paiId(),
                requisicao.nome(), requisicao.sentido(), requisicao.cor());
        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/categorias/" + criada.id()))
                .body(paraRespostaGravada(criada));
    }

    /**
     * Renomear e inativar sao dois verbos, e por isso dois casos de uso — o PATCH despacha para
     * o que foi pedido. Inativar vai por PATCH porque e ESTADO, nao exclusao
     * (docs/04-api/convencoes.md).
     */
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
            categoria = renomearCategoria.executar(ambienteId, categoriaId, requisicao.nome());
        }
        if (requisicao.cor() != null) {
            categoria = recolorirCategoria.executar(ambienteId, categoriaId, requisicao.cor());
        }
        if (requisicao.inativa() != null) {
            categoria = alterarAtivacao.executar(ambienteId, categoriaId, requisicao.inativa());
        }
        return paraRespostaGravada(categoria);
    }

    /** DELETE no sentido restrito do dominio: o que nunca correspondeu a nada. Inativar e PATCH. */
    @DeleteMapping("/{categoriaId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long ambienteId, @PathVariable Long categoriaId) {
        excluirCategoria.executar(ambienteId, categoriaId);
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
