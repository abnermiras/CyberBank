package br.com.cyberbank.ambiente.aplicacao;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarAmbientesUseCase {

    private final AmbienteRepository ambientes;

    public ListarAmbientesUseCase(AmbienteRepository ambientes) {
        this.ambientes = ambientes;
    }

    @Transactional(readOnly = true)
    public List<AcessoAoAmbiente> executar(Long usuarioId) {
        return ambientes.listarDoUsuario(usuarioId);
    }
}
