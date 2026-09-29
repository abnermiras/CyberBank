package br.com.cyberbank.compartilhamento.aplicacao;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.compartilhamento.dominio.CompartilhamentoRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.ContaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarCompartilhamentosUseCase {

    private final CompartilhamentoRepository compartilhamentos;
    private final ContaRepository contas;
    private final AmbienteRepository ambientes;

    public ListarCompartilhamentosUseCase(CompartilhamentoRepository compartilhamentos,
            ContaRepository contas, AmbienteRepository ambientes) {
        this.compartilhamentos = compartilhamentos;
        this.contas = contas;
        this.ambientes = ambientes;
    }

    @Transactional(readOnly = true)
    public List<CompartilhamentoDaConta> executar(Long ambienteOrigemId, Long contaId) {
        contas.buscarDoAmbiente(contaId, ambienteOrigemId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        return compartilhamentos.listarDaConta(contaId, ambienteOrigemId).stream()
                .map(c -> new CompartilhamentoDaConta(c.ambienteDestinoId(),
                        ambientes.buscar(c.ambienteDestinoId()).map(Ambiente::nome).orElse(null),
                        c.criadoEm()))
                .toList();
    }
}
