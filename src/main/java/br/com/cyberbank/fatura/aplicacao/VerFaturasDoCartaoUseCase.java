package br.com.cyberbank.fatura.aplicacao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.NumerosDaFatura;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.TotaisDeFatura;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerFaturasDoCartaoUseCase {

    private final ContaRepository contas;
    private final FaturaRepository faturas;
    private final LancamentoRepository lancamentos;
    private final DiaLocal diaLocal;

    public VerFaturasDoCartaoUseCase(ContaRepository contas, FaturaRepository faturas,
            LancamentoRepository lancamentos, DiaLocal diaLocal) {
        this.contas = contas;
        this.faturas = faturas;
        this.lancamentos = lancamentos;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public CartaoComFaturas executar(Long ambienteId, Long contaId) {
        Conta cartao = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        cartao.exigirContratoDeCartao();

        List<Fatura> doCartao = faturas.listarDaConta(contaId);
        Map<Long, NumerosDaFatura> numeros = numerosPorFatura(doCartao);

        return new CartaoComFaturas(cartao,
                -lancamentos.saldoRealizadoDaConta(contaId, diaLocal.hoje()),
                doCartao.stream()
                        .map(fatura -> new FaturaComNumeros(fatura,
                                numeros.getOrDefault(fatura.id(), NumerosDaFatura.VAZIA)))
                        .toList());
    }

    private Map<Long, NumerosDaFatura> numerosPorFatura(List<Fatura> doCartao) {
        Map<Long, NumerosDaFatura> numeros = new HashMap<>();
        for (TotaisDeFatura totais : lancamentos.totaisDasFaturas(
                doCartao.stream().map(Fatura::id).toList())) {
            numeros.put(totais.faturaId(), new NumerosDaFatura(totais.totalCentavos(),
                    totais.pagoCentavos(), totais.roladoCentavos()));
        }
        return numeros;
    }
}
