package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenomearAmbienteUseCase {

    private final AmbienteRepository ambientes;
    private final AcessoRepository acessos;

    public RenomearAmbienteUseCase(AmbienteRepository ambientes, AcessoRepository acessos) {
        this.ambientes = ambientes;
        this.acessos = acessos;
    }

    @Transactional
    public AcessoAoAmbiente executar(Long usuarioId, Long ambienteId, String nome) {
        Acesso acesso = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        Ambiente ambiente = ambientes.buscar(ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Ambiente renomeado = ambientes.salvar(ambiente.renomeadoPor(acesso.papel(), nome));
        return new AcessoAoAmbiente(renomeado, acesso.papel());
    }
}
