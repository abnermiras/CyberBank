package br.com.cyberbank.meio.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirMeioUseCase {

    private final MeioRepository meios;
    private final LancamentoRepository lancamentos;

    public ExcluirMeioUseCase(MeioRepository meios, LancamentoRepository lancamentos) {
        this.meios = meios;
        this.lancamentos = lancamentos;
    }

    @Transactional
    public void executar(Long ambienteId, Long meioId) {
        Meio meio = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        meio.exigirExcluivel(lancamentos.meioTemLancamento(meioId));
        meios.excluir(meioId, ambienteId);
    }
}
