package br.com.cyberbank.ambiente.dominio;

import java.util.Optional;

public interface AcessoRepository {

    Acesso salvar(Acesso acesso);

    Optional<Acesso> buscar(Long usuarioId, Long ambienteId);
}
