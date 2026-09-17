package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirContaUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final MeioRepository meios;

    public ExcluirContaUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            MeioRepository meios) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.meios = meios;
    }

    @Transactional
    public void executar(Long ambienteId, Long contaId) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        conta.exigirExcluivel(lancamentos.contaTemLancamento(contaId));

        meios.listarDaConta(contaId, ambienteId)
                .forEach(meio -> meios.excluir(meio.id(), ambienteId));

        contas.excluir(contaId, ambienteId);
    }
}
