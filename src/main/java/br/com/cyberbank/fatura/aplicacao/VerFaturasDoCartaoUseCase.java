package br.com.cyberbank.fatura.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerFaturasDoCartaoUseCase {

    private final ContaRepository contas;
    private final FaturaRepository faturas;
    private final NumerosDasFaturas numerosDasFaturas;
    private final LancamentoRepository lancamentos;
    private final DiaLocal diaLocal;

    public VerFaturasDoCartaoUseCase(ContaRepository contas, FaturaRepository faturas,
            NumerosDasFaturas numerosDasFaturas, LancamentoRepository lancamentos,
            DiaLocal diaLocal) {
        this.contas = contas;
        this.faturas = faturas;
        this.numerosDasFaturas = numerosDasFaturas;
        this.lancamentos = lancamentos;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public Long contaDaFatura(Long ambienteId, Long faturaId) {
        return faturas.buscarDoAmbiente(faturaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .contaId();
    }

    @Transactional(readOnly = true)
    public CartaoComFaturas executar(Long ambienteId, Long contaId) {
        Conta cartao = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        cartao.exigirContratoDeCartao();

        return new CartaoComFaturas(cartao,
                -lancamentos.saldoRealizadoDaConta(contaId, diaLocal.hoje()),
                numerosDasFaturas.daConta(contaId));
    }
}
