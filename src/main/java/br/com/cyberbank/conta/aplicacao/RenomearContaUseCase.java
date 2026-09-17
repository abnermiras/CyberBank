package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenomearContaUseCase {

    private final ContaRepository contas;

    public RenomearContaUseCase(ContaRepository contas) {
        this.contas = contas;
    }

    @Transactional
    public Conta executar(Long ambienteId, Long contaId, String novoNome) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return contas.salvar(conta.renomeada(novoNome));
    }
}
