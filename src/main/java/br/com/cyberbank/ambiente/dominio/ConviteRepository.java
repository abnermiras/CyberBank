package br.com.cyberbank.ambiente.dominio;

import java.util.List;
import java.util.Optional;

public interface ConviteRepository {

    Convite salvar(Convite convite);

    Optional<Convite> buscar(Long conviteId);

    Optional<Convite> buscarPendente(Long ambienteId, String email);

    List<Convite> listarPendentesDoAmbiente(Long ambienteId);

    List<Convite> listarPendentesDoEmail(String email);
}
