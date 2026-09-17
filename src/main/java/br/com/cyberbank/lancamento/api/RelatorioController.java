package br.com.cyberbank.lancamento.api;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.lancamento.aplicacao.ResumirMesUseCase;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.RelatorioDoMes;
import br.com.cyberbank.lancamento.dominio.ResumoDoMes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/relatorios")
public class RelatorioController {

    public record GastoResponse(Long categoriaId, String nome, String cor,
                                long totalCentavos, long lancamentos) {
    }

    public record PrevistoResponse(Long id, LocalDate dataEfeito, String descricao, String sentido,
                                   long valorCentavos, Long contaId, Long categoriaId) {
    }

    public record ResumoResponse(String mes, LocalDate primeiroDia, LocalDate ultimoDia,
                                 LocalDate hoje, int diasAteOFimDoMes,
                                 long emCaixaCentavos, long guardadoCentavos,
                                 long patrimonioCentavos, long sobraAteOFimDoMesCentavos,
                                 long aPagarCentavos, long aReceberCentavos,
                                 long entrouNoMesCentavos, long saiuNoMesCentavos,
                                 long guardadoNoMesCentavos, long pendencias,
                                 List<GastoResponse> gastoPorCategoria,
                                 List<PrevistoResponse> proximos) {
    }

    private final ResumirMesUseCase resumirMes;
    private final DiaLocal diaLocal;

    public RelatorioController(ResumirMesUseCase resumirMes, DiaLocal diaLocal) {
        this.resumirMes = resumirMes;
        this.diaLocal = diaLocal;
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo(@RequestParam(required = false) String mes) {
        YearMonth escolhido = mesEscolhido(mes);
        ResumoDoMes resumo = resumirMes.executar(ContextoDaRequisicao.ambienteId(), escolhido);

        return new ResumoResponse(
                escolhido.toString(),
                resumo.primeiroDia(),
                resumo.ultimoDia(),
                resumo.hoje(),
                resumo.diasAteOFimDoMes(),
                resumo.emCaixaCentavos(),
                resumo.guardadoCentavos(),
                resumo.patrimonioCentavos(),
                resumo.sobraAteOFimDoMesCentavos(),
                resumo.aPagarCentavos(),
                resumo.aReceberCentavos(),
                resumo.entrouNoMesCentavos(),
                resumo.saiuNoMesCentavos(),
                resumo.guardadoNoMesCentavos(),
                resumo.pendencias(),
                resumo.gastoPorCategoria().stream().map(RelatorioController::gasto).toList(),
                resumo.proximos().stream().map(RelatorioController::previsto).toList());
    }

    private YearMonth mesEscolhido(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.from(diaLocal.hoje());
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException(List.of(
                    new ErroDeValidacao("mes", "FORMATO", "Use AAAA-MM.")));
        }
    }

    private static GastoResponse gasto(RelatorioDoMes.GastoDeCategoria linha) {
        return new GastoResponse(linha.categoriaId(), linha.nome(), linha.cor(),
                linha.totalCentavos(), linha.lancamentos());
    }

    private static PrevistoResponse previsto(Lancamento lancamento) {
        return new PrevistoResponse(lancamento.id(), lancamento.dataEfeito(),
                lancamento.descricao(), lancamento.sentido().name(),
                lancamento.valorCentavos(), lancamento.contaId(), lancamento.categoriaId());
    }
}
