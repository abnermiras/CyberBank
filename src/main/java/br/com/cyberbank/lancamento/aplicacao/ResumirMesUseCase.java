package br.com.cyberbank.lancamento.aplicacao;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.cyberbank.categoria.aplicacao.ListarCategoriasUseCase;
import br.com.cyberbank.categoria.dominio.ArvoreDeCategorias;
import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.aplicacao.ContaComSaldo;
import br.com.cyberbank.conta.aplicacao.ListarContasUseCase;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.RelatorioDoMes;
import br.com.cyberbank.lancamento.dominio.ResumoDoMes;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.TotalPorSentido;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumirMesUseCase {

    private static final int PROXIMOS_NO_HORIZONTE = 8;

    private final LancamentoRepository lancamentos;
    private final ListarContasUseCase listarContas;
    private final ListarCategoriasUseCase listarCategorias;
    private final DiaLocal diaLocal;

    public ResumirMesUseCase(LancamentoRepository lancamentos, ListarContasUseCase listarContas,
            ListarCategoriasUseCase listarCategorias, DiaLocal diaLocal) {
        this.lancamentos = lancamentos;
        this.listarContas = listarContas;
        this.listarCategorias = listarCategorias;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public ResumoDoMes executar(Long ambienteId, YearMonth mes) {
        LocalDate hoje = diaLocal.hoje();
        LocalDate primeiroDia = mes.atDay(1);
        LocalDate ultimoDia = mes.atEndOfMonth();

        List<ContaComSaldo> contas = listarContas.executar(ambienteId, true);
        Set<Long> deFluxo = idsDe(contas, conta -> conta.conta().entraNoFluxoDeCaixa());
        Set<Long> deCaixa = idsDe(contas, conta -> conta.conta().entraEmCaixa());
        Set<Long> foraDoFluxo = idsDe(contas, conta -> !conta.conta().entraNoFluxoDeCaixa());

        long emCaixa = somar(contas, conta -> conta.conta().entraEmCaixa());
        long guardado = somar(contas, conta -> !conta.conta().entraNoFluxoDeCaixa());
        long patrimonio = somar(contas, conta -> true);

        Map<Long, RelatorioDoMes.CategoriaDoRelatorio> categorias = categoriasDoRelatorio(ambienteId);

        List<RelatorioDoMes.Bucket> buckets =
                lancamentos.somarPorCategoria(ambienteId, primeiroDia, ultimoDia, deFluxo);

        LocalDate inicioDoHorizonte = hoje.isAfter(primeiroDia) ? hoje : primeiroDia;
        Map<Sentido, Long> previstos = lancamentos
                .somarPrevistos(ambienteId, inicioDoHorizonte, ultimoDia, deCaixa).stream()
                .collect(Collectors.toMap(TotalPorSentido::sentido, TotalPorSentido::totalCentavos));

        return new ResumoDoMes(
                primeiroDia,
                ultimoDia,
                hoje,
                emCaixa,
                guardado,
                patrimonio,
                previstos.getOrDefault(Sentido.SAIDA, 0L),
                previstos.getOrDefault(Sentido.ENTRADA, 0L),
                RelatorioDoMes.somarSentido(buckets, categorias, Sentido.ENTRADA),
                RelatorioDoMes.somarSentido(buckets, categorias, Sentido.SAIDA),
                lancamentos.somarAportes(ambienteId, primeiroDia, ultimoDia, foraDoFluxo),
                lancamentos.contarPendencias(ambienteId),
                RelatorioDoMes.gastoPorCategoria(buckets, categorias),
                lancamentos.listarPrevistosAte(ambienteId, inicioDoHorizonte, ultimoDia,
                        deCaixa, PROXIMOS_NO_HORIZONTE));
    }

    private static long somar(List<ContaComSaldo> contas,
            java.util.function.Predicate<ContaComSaldo> criterio) {
        return contas.stream().filter(criterio)
                .mapToLong(ContaComSaldo::saldoRealizadoCentavos).sum();
    }

    private static Set<Long> idsDe(List<ContaComSaldo> contas,
            java.util.function.Predicate<ContaComSaldo> criterio) {
        return contas.stream().filter(criterio).map(conta -> conta.conta().id())
                .collect(Collectors.toSet());
    }

    private Map<Long, RelatorioDoMes.CategoriaDoRelatorio> categoriasDoRelatorio(Long ambienteId) {
        Map<Long, RelatorioDoMes.CategoriaDoRelatorio> mapa = new HashMap<>();
        for (ArvoreDeCategorias.No raiz : listarCategorias.executar(ambienteId, true, true)) {
            mapa.put(raiz.categoria().id(), paraRelatorio(raiz.categoria(), raiz.categoria().id()));
            for (ArvoreDeCategorias.No filha : raiz.filhas()) {
                mapa.put(filha.categoria().id(),
                        paraRelatorio(filha.categoria(), raiz.categoria().id()));
            }
        }
        return mapa;
    }

    private static RelatorioDoMes.CategoriaDoRelatorio paraRelatorio(Categoria categoria, Long raizId) {
        return new RelatorioDoMes.CategoriaDoRelatorio(
                categoria.id(), raizId, categoria.nome(),
                categoria.cor() == null ? null : categoria.cor().name(),
                Sentido.valueOf(categoria.sentido().name()),
                categoria.sistema());
    }
}
