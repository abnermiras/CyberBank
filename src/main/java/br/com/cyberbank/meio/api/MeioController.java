package br.com.cyberbank.meio.api;

import java.net.URI;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;
import br.com.cyberbank.meio.aplicacao.AlterarAtivacaoMeioUseCase;
import br.com.cyberbank.meio.aplicacao.CadastrarMeioUseCase;
import br.com.cyberbank.meio.aplicacao.ExcluirMeioUseCase;
import br.com.cyberbank.meio.aplicacao.ListarMeiosUseCase;
import br.com.cyberbank.meio.aplicacao.RenomearMeioUseCase;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

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
@RequestMapping("/api/v1/ambientes/{ambienteId}/meios-de-pagamento")
public class MeioController {

    public record MeioResponse(
            Long id,
            @JsonInclude(JsonInclude.Include.NON_NULL) String nome,
            TipoDeMeio tipo,
            Long contaId,
            boolean separaAsDuasDatas,
            boolean inativo) {
    }

    public record ListaResponse(List<MeioResponse> itens) {
    }

    public record CadastroRequest(String nome, TipoDeMeio tipo, Long contaId) {
    }

    public record AlteracaoRequest(String nome, Boolean inativo) {
    }

    private final ListarMeiosUseCase listarMeios;
    private final CadastrarMeioUseCase cadastrarMeio;
    private final RenomearMeioUseCase renomearMeio;
    private final AlterarAtivacaoMeioUseCase alterarAtivacao;
    private final ExcluirMeioUseCase excluirMeio;

    public MeioController(ListarMeiosUseCase listarMeios, CadastrarMeioUseCase cadastrarMeio,
            RenomearMeioUseCase renomearMeio, AlterarAtivacaoMeioUseCase alterarAtivacao,
            ExcluirMeioUseCase excluirMeio) {
        this.listarMeios = listarMeios;
        this.cadastrarMeio = cadastrarMeio;
        this.renomearMeio = renomearMeio;
        this.alterarAtivacao = alterarAtivacao;
        this.excluirMeio = excluirMeio;
    }

    @GetMapping
    public ListaResponse listar(@PathVariable Long ambienteId,
            @RequestParam(defaultValue = "false") boolean inativos) {
        return new ListaResponse(listarMeios.executar(ambienteId, inativos).stream()
                .map(MeioController::paraResposta)
                .toList());
    }

    @PostMapping
    public ResponseEntity<MeioResponse> cadastrar(@PathVariable Long ambienteId,
            @RequestBody CadastroRequest requisicao) {

        Meio criado = cadastrarMeio.executar(ambienteId, ContextoDaRequisicao.usuarioId(), requisicao.nome(), requisicao.tipo(),
                requisicao.contaId());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId
                        + "/meios-de-pagamento/" + criado.id()))
                .body(paraResposta(criado));
    }

    @PatchMapping("/{meioId}")
    public MeioResponse alterar(@PathVariable Long ambienteId, @PathVariable Long meioId,
            @RequestBody AlteracaoRequest requisicao) {

        if (requisicao.nome() == null && requisicao.inativo() == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("nome", "OBRIGATORIO",
                    "Informe o que mudar: nome, inativo, ou os dois.")));
        }

        Meio meio = null;
        if (requisicao.nome() != null) {
            meio = renomearMeio.executar(ambienteId, ContextoDaRequisicao.usuarioId(), meioId, requisicao.nome());
        }
        if (requisicao.inativo() != null) {
            meio = alterarAtivacao.executar(ambienteId, ContextoDaRequisicao.usuarioId(), meioId,
                    requisicao.inativo());
        }
        return paraResposta(meio);
    }

    @DeleteMapping("/{meioId}")
    public ResponseEntity<Void> excluir(@PathVariable Long ambienteId,
            @PathVariable Long meioId) {
        excluirMeio.executar(ambienteId, ContextoDaRequisicao.usuarioId(), meioId);
        return ResponseEntity.noContent().build();
    }

    private static MeioResponse paraResposta(Meio meio) {
        return new MeioResponse(meio.id(), meio.nome(), meio.tipo(), meio.contaId(),
                meio.tipo().separaAsDuasDatas(), meio.inativo());
    }
}
