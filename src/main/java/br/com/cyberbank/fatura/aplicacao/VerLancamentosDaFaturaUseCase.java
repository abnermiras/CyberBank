package br.com.cyberbank.fatura.aplicacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerLancamentosDaFaturaUseCase {

    private final FaturaRepository faturas;
    private final LancamentoRepository lancamentos;
    private final MeioRepository meios;
    private final CategoriaRepository categorias;

    public VerLancamentosDaFaturaUseCase(FaturaRepository faturas,
            LancamentoRepository lancamentos, MeioRepository meios,
            CategoriaRepository categorias) {
        this.faturas = faturas;
        this.lancamentos = lancamentos;
        this.meios = meios;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public LancamentosDaFatura executar(Long ambienteId, Long faturaId) {
        Fatura fatura = faturas.buscarDoAmbiente(faturaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        List<Lancamento> daFatura = lancamentos.listarDaFatura(faturaId, ambienteId);
        Map<Long, String> nomeDaCategoria = nomesDasCategorias(ambienteId);
        Map<Long, int[]> posicaoDaParcela = posicoesDasParcelas(daFatura, ambienteId);

        List<Meio> doContrato = meios.listarDaConta(fatura.contaId(), ambienteId);

        Map<Long, List<LancamentosDaFatura.LancamentoDaFatura>> porCartao = new LinkedHashMap<>();
        Map<Long, Long> totalPorCartao = new HashMap<>();
        Map<Long, String> nomeDoCartao = new HashMap<>();
        for (Meio cartao : doContrato) {
            porCartao.put(cartao.id(), new ArrayList<>());
            totalPorCartao.put(cartao.id(), 0L);
            nomeDoCartao.put(cartao.id(), cartao.nome());
        }

        LancamentosDaFatura.LancamentoDaFatura saldoAnterior = null;
        LancamentosDaFatura.LancamentoDaFatura rolado = null;
        long total = 0;

        for (Lancamento lancamento : daFatura) {
            var linha = paraLinha(lancamento, nomeDaCategoria, posicaoDaParcela);

            if (lancamento.rolagemDeFatura() != null) {
                if (lancamento.sentido() == Sentido.ENTRADA) {
                    rolado = linha;
                } else {
                    saldoAnterior = linha;
                    total += lancamento.valorCentavos();
                }
                continue;
            }

            total += -lancamento.valorComSinal();

            Long cartaoId = lancamento.meioId();
            porCartao.computeIfAbsent(cartaoId, qual -> new ArrayList<>()).add(linha);
            totalPorCartao.merge(cartaoId, -lancamento.valorComSinal(), Long::sum);
        }

        List<LancamentosDaFatura.CartaoComLancamentos> cartoes = porCartao.entrySet().stream()
                .filter(cartao -> !cartao.getValue().isEmpty())
                .map(cartao -> new LancamentosDaFatura.CartaoComLancamentos(cartao.getKey(),
                        nomeDoCartao.getOrDefault(cartao.getKey(), "—"),
                        totalPorCartao.getOrDefault(cartao.getKey(), 0L), cartao.getValue()))
                .toList();

        return new LancamentosDaFatura(total, saldoAnterior, cartoes, rolado);
    }

    private LancamentosDaFatura.LancamentoDaFatura paraLinha(Lancamento lancamento,
            Map<Long, String> nomeDaCategoria, Map<Long, int[]> posicaoDaParcela) {

        int[] parcela = posicaoDaParcela.get(lancamento.id());

        return new LancamentosDaFatura.LancamentoDaFatura(lancamento.id(),
                lancamento.dataEvento(), lancamento.descricao(), lancamento.sentido().name(),
                lancamento.valorCentavos(), lancamento.situacao().name(),
                nomeDaCategoria.get(lancamento.categoriaId()), lancamento.doCiclo(),
                parcela == null ? null : parcela[0], parcela == null ? null : parcela[1]);
    }

    private Map<Long, String> nomesDasCategorias(Long ambienteId) {
        Map<Long, String> nomes = new HashMap<>();
        for (Categoria categoria : categorias.listarDoAmbiente(ambienteId)) {
            nomes.put(categoria.id(), categoria.nome());
        }
        return nomes;
    }

    private Map<Long, int[]> posicoesDasParcelas(List<Lancamento> daFatura, Long ambienteId) {
        Map<Long, int[]> posicao = new HashMap<>();

        daFatura.stream()
                .map(Lancamento::parcelamentoId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(parcelamentoId -> {
                    List<Lancamento> irmas =
                            lancamentos.listarDoParcelamento(parcelamentoId, ambienteId);
                    for (int indice = 0; indice < irmas.size(); indice++) {
                        posicao.put(irmas.get(indice).id(),
                                new int[] { indice + 1, irmas.size() });
                    }
                });
        return posicao;
    }
}
