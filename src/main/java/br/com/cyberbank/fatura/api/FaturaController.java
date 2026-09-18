package br.com.cyberbank.fatura.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.fatura.aplicacao.AbrirFaturaUseCase;
import br.com.cyberbank.fatura.aplicacao.CartaoComFaturas;
import br.com.cyberbank.fatura.aplicacao.FecharFaturaUseCase;
import br.com.cyberbank.fatura.aplicacao.PagarFaturaUseCase;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.fatura.aplicacao.VerFaturasDoCartaoUseCase;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.NumerosDaFatura;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/faturas")
public class FaturaController {

    public record FaturaResponse(
            Long id,
            String competencia,
            LocalDate dataFechamento,
            LocalDate dataVencimento,
            StatusDaFatura status,
            long totalCentavos,
            long pagoCentavos,
            long roladoCentavos,
            long agendadoCentavos,
            long aPagarCentavos,
            boolean encerrada,
            boolean rolada,
            boolean recebePagamento) {
    }

    public record CartaoResponse(
            Long id,
            String nome,
            int diaVencimento,
            int diasAntesFechamento,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long limiteCentavos,
            @JsonInclude(JsonInclude.Include.NON_NULL) LocalDate limiteInformadoEm,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long contaPagadoraPadraoId,
            long dividaCentavos,
            @JsonInclude(JsonInclude.Include.NON_NULL) Long limiteDisponivelCentavos,
            boolean limitePodeEstarDesatualizado) {
    }

    public record ListaResponse(CartaoResponse cartao, List<FaturaResponse> itens) {
    }

    public record PagamentoRequest(Long contaPagadoraId, Long valor, LocalDate dataEvento) {
    }

    private final VerFaturasDoCartaoUseCase verFaturas;
    private final PagarFaturaUseCase pagarFatura;
    private final FecharFaturaUseCase fecharFatura;
    private final AbrirFaturaUseCase abrirFatura;

    public FaturaController(VerFaturasDoCartaoUseCase verFaturas, PagarFaturaUseCase pagarFatura,
            FecharFaturaUseCase fecharFatura, AbrirFaturaUseCase abrirFatura) {
        this.verFaturas = verFaturas;
        this.pagarFatura = pagarFatura;
        this.fecharFatura = fecharFatura;
        this.abrirFatura = abrirFatura;
    }

    @GetMapping
    public ListaResponse listar(@PathVariable Long ambienteId, @RequestParam Long contaId) {
        CartaoComFaturas cartao = verFaturas.executar(ambienteId, contaId);

        return new ListaResponse(paraResposta(cartao),
                cartao.faturas().stream().map(FaturaController::paraResposta).toList());
    }

    @PostMapping("/{faturaId}/pagamentos")
    public ListaResponse pagar(@PathVariable Long ambienteId, @PathVariable Long faturaId,
            @RequestBody PagamentoRequest requisicao) {

        pagarFatura.executar(ambienteId, ContextoDaRequisicao.usuarioId(), faturaId,
                requisicao.contaPagadoraId(), requisicao.valor(), requisicao.dataEvento());

        return listar(ambienteId, contaDaFatura(ambienteId, faturaId));
    }

    @PostMapping("/{faturaId}/fechamento")
    public ListaResponse fechar(@PathVariable Long ambienteId, @PathVariable Long faturaId) {
        Fatura fechada = fecharFatura.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                faturaId);

        return listar(ambienteId, fechada.contaId());
    }

    @PostMapping("/{faturaId}/abertura")
    public ListaResponse abrir(@PathVariable Long ambienteId, @PathVariable Long faturaId) {
        Fatura aberta = abrirFatura.executar(ambienteId, ContextoDaRequisicao.usuarioId(),
                faturaId);

        return listar(ambienteId, aberta.contaId());
    }

    private Long contaDaFatura(Long ambienteId, Long faturaId) {
        return verFaturas.contaDaFatura(ambienteId, faturaId);
    }

    private static CartaoResponse paraResposta(CartaoComFaturas cartao) {
        Long disponivel = cartao.limiteDisponivelCentavos();

        return new CartaoResponse(cartao.cartao().id(), cartao.cartao().nome(),
                cartao.cartao().contrato().diaVencimento(),
                cartao.cartao().contrato().diasAntesFechamento(),
                cartao.cartao().contrato().limiteCentavos(),
                cartao.cartao().contrato().limiteInformadoEm(),
                cartao.cartao().contrato().contaPagadoraPadraoId(),
                cartao.dividaCentavos(), disponivel, disponivel != null && disponivel < 0);
    }

    private static FaturaResponse paraResposta(FaturaComNumeros comNumeros) {
        Fatura fatura = comNumeros.fatura();
        NumerosDaFatura numeros = comNumeros.numeros();

        return new FaturaResponse(fatura.id(), competencia(fatura.competencia()),
                fatura.dataFechamento(), fatura.dataVencimento(), fatura.status(),
                numeros.totalCentavos(), numeros.pagoCentavos(), numeros.roladoCentavos(),
                numeros.agendadoCentavos(), numeros.aPagarCentavos(), numeros.encerrada(),
                numeros.rolada(), fatura.naJanelaDoPagamentoEDaAbertura(numeros));
    }

    private static String competencia(YearMonth competencia) {
        return competencia.toString();
    }
}
