package br.com.cyberbank.lancamento.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirLancamentoUseCase {

    private final LancamentoRepository lancamentos;

    public ExcluirLancamentoUseCase(LancamentoRepository lancamentos) {
        this.lancamentos = lancamentos;
    }

    @Transactional
    public void executar(Long ambienteId, Long lancamentoId) {
        Lancamento lancamento = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        lancamento.exigirExcluivelPeloUsuario(lancamentos.temEstorno(lancamentoId));

        if (lancamento.ehTransferencia()) {
            lancamentos.listarDaTransferencia(lancamento.transferenciaId(), ambienteId)
                    .forEach(lado -> lancamentos.excluir(lado.id(), ambienteId));
            return;
        }
        lancamentos.excluir(lancamentoId, ambienteId);
    }
}
