package br.com.cyberbank.ambiente.dominio;

import java.util.List;
import java.util.Optional;

public interface AcessoRepository {

    Acesso salvar(Acesso acesso);

    Optional<Acesso> buscar(Long usuarioId, Long ambienteId);

    List<Acesso> listarDoAmbiente(Long ambienteId);

    boolean remover(Acesso acesso);
}
