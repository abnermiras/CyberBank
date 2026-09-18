package br.com.cyberbank.fatura.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import br.com.cyberbank.fatura.aplicacao.CartaoComFaturas;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.fatura.aplicacao.VerFaturasDoCartaoUseCase;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.NumerosDaFatura;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            long aPagarCentavos,
            boolean encerrada,
            boolean rolada) {
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

    private final VerFaturasDoCartaoUseCase verFaturas;

    public FaturaController(VerFaturasDoCartaoUseCase verFaturas) {
        this.verFaturas = verFaturas;
    }

    @GetMapping
    public ListaResponse listar(@PathVariable Long ambienteId, @RequestParam Long contaId) {
        CartaoComFaturas cartao = verFaturas.executar(ambienteId, contaId);

        return new ListaResponse(paraResposta(cartao),
                cartao.faturas().stream().map(FaturaController::paraResposta).toList());
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
                numeros.aPagarCentavos(), numeros.encerrada(), numeros.rolada());
    }

    private static String competencia(YearMonth competencia) {
        return competencia.toString();
    }
}
