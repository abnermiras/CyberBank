package br.com.cyberbank.conta.api;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;
import br.com.cyberbank.conta.aplicacao.AbrirContaUseCase;
import br.com.cyberbank.conta.aplicacao.AlterarAtivacaoContaUseCase;
import br.com.cyberbank.conta.aplicacao.ContaComSaldo;
import br.com.cyberbank.conta.aplicacao.ExcluirContaUseCase;
import br.com.cyberbank.conta.aplicacao.ListarContasUseCase;
import br.com.cyberbank.conta.aplicacao.RenomearContaUseCase;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.TipoDeConta;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

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
@RequestMapping("/api/v1/ambientes/{ambienteId}/contas")
public class ContaController {

    public record ContaResponse(
            Long id,
            String nome,
            TipoDeConta tipo,
            boolean entraNoFluxoDeCaixa,
            boolean entraEmCaixa,
            boolean inativa,
            long saldoRealizadoCentavos,
            List<TipoDeMeio> tiposDeMeioDisponiveis) {
    }

    public record ListaResponse(List<ContaResponse> itens, long emCaixaCentavos,
            long patrimonioCentavos, List<TipoDisponivelResponse> tiposDisponiveis) {
    }

    public record TipoDisponivelResponse(TipoDeConta tipo, boolean entraNoFluxoDeCaixa,
            boolean entraEmCaixa, boolean aceitaSaldoInicial, List<TipoDeMeio> meios) {
    }

    public record ContaGravadaResponse(Long id, String nome, TipoDeConta tipo,
            boolean entraNoFluxoDeCaixa, boolean entraEmCaixa, boolean inativa) {
    }

    public record AberturaRequest(String nome, TipoDeConta tipo, Long saldoInicial,
            List<TipoDeMeio> meios) {
    }

    public record AlteracaoRequest(String nome, Boolean inativa) {
    }

    private final ListarContasUseCase listarContas;
    private final AbrirContaUseCase abrirConta;
    private final RenomearContaUseCase renomearConta;
    private final AlterarAtivacaoContaUseCase alterarAtivacao;
    private final ExcluirContaUseCase excluirConta;

    public ContaController(ListarContasUseCase listarContas, AbrirContaUseCase abrirConta,
            RenomearContaUseCase renomearConta, AlterarAtivacaoContaUseCase alterarAtivacao,
            ExcluirContaUseCase excluirConta) {
        this.listarContas = listarContas;
        this.abrirConta = abrirConta;
        this.renomearConta = renomearConta;
        this.alterarAtivacao = alterarAtivacao;
        this.excluirConta = excluirConta;
    }

    @GetMapping
    public ListaResponse listar(@PathVariable Long ambienteId,
            @RequestParam(defaultValue = "false") boolean inativas) {

        List<ContaComSaldo> contas = listarContas.executar(ambienteId, inativas);

        long emCaixa = contas.stream()
                .filter(c -> c.conta().entraEmCaixa())
                .mapToLong(ContaComSaldo::saldoRealizadoCentavos)
                .sum();

        long patrimonio = contas.stream()
                .mapToLong(ContaComSaldo::saldoRealizadoCentavos)
                .sum();

        return new ListaResponse(contas.stream().map(ContaController::paraResposta).toList(),
                emCaixa, patrimonio, tiposDisponiveis());
    }

    @PostMapping
    public ResponseEntity<ContaGravadaResponse> abrir(@PathVariable Long ambienteId,
            @RequestBody AberturaRequest requisicao) {

        Conta criada = abrirConta.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                requisicao.nome(), requisicao.tipo(), requisicao.saldoInicial(),
                requisicao.meios() == null ? List.of() : requisicao.meios()).conta();

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/contas/" + criada.id()))
                .body(paraRespostaGravada(criada));
    }

    @PatchMapping("/{contaId}")
    public ContaGravadaResponse alterar(@PathVariable Long ambienteId,
            @PathVariable Long contaId, @RequestBody AlteracaoRequest requisicao) {

        if (requisicao.nome() == null && requisicao.inativa() == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("nome", "OBRIGATORIO",
                    "Informe o que mudar: nome, inativa, ou os dois.")));
        }

        Conta conta = null;
        if (requisicao.nome() != null) {
            conta = renomearConta.executar(ambienteId, ContextoDaRequisicao.usuarioId(), contaId, requisicao.nome());
        }
        if (requisicao.inativa() != null) {
            conta = alterarAtivacao.executar(ambienteId, ContextoDaRequisicao.usuarioId(), contaId,
                    requisicao.inativa());
        }
        return paraRespostaGravada(conta);
    }

    @DeleteMapping("/{contaId}")
    public ResponseEntity<Void> excluir(@PathVariable Long ambienteId,
            @PathVariable Long contaId) {
        excluirConta.executar(ambienteId, ContextoDaRequisicao.usuarioId(), contaId);
        return ResponseEntity.noContent().build();
    }

    private static List<TipoDisponivelResponse> tiposDisponiveis() {
        return Arrays.stream(TipoDeConta.values())
                .filter(tipo -> !tipo.dependeDeFatura())
                .map(tipo -> new TipoDisponivelResponse(tipo, tipo.entraNoFluxoDeCaixa(),
                        tipo.entraEmCaixa(), tipo.aceitaSaldoInicial(),
                        TipoDeMeio.daConta(tipo.name())))
                .toList();
    }

    private static ContaResponse paraResposta(ContaComSaldo comSaldo) {
        Conta conta = comSaldo.conta();
        return new ContaResponse(conta.id(), conta.nome(), conta.tipo(),
                conta.entraNoFluxoDeCaixa(), conta.entraEmCaixa(), conta.inativa(),
                comSaldo.saldoRealizadoCentavos(),
                TipoDeMeio.daConta(conta.tipo().name()));
    }

    private static ContaGravadaResponse paraRespostaGravada(Conta conta) {
        return new ContaGravadaResponse(conta.id(), conta.nome(), conta.tipo(),
                conta.entraNoFluxoDeCaixa(), conta.entraEmCaixa(), conta.inativa());
    }
}
