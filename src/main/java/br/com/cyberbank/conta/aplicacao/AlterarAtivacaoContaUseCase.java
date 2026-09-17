package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarAtivacaoContaUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;

    public AlterarAtivacaoContaUseCase(ContaRepository contas, LancamentoRepository lancamentos) {
        this.contas = contas;
        this.lancamentos = lancamentos;
    }

    @Transactional
    public Conta executar(Long ambienteId, Long contaId, boolean inativa) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (inativa && !conta.inativa()) {
            lancamentos.excluirPrevistosDaConta(contaId, ambienteId);
        }
        return contas.salvar(conta.comAtivacao(inativa));
    }
}
