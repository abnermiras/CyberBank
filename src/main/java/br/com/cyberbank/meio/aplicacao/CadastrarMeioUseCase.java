package br.com.cyberbank.meio.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarMeioUseCase {

    private final MeioRepository meios;
    private final ContaRepository contas;
    private final Clock relogio;

    public CadastrarMeioUseCase(MeioRepository meios, ContaRepository contas, Clock relogio) {
        this.meios = meios;
        this.contas = contas;
        this.relogio = relogio;
    }

    @Transactional
    public Meio executar(Long ambienteId, String nome, TipoDeMeio tipo, Long contaId) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (tipo != null && !tipo.repetePorConta()
                && meios.existeNaConta(contaId, tipo, ambienteId)) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_DUPLICADO_NA_CONTA);
        }

        return meios.salvar(Meio.novo(ambienteId, nome, tipo, contaId, conta.tipo().name(),
                relogio.instant()));
    }
}
