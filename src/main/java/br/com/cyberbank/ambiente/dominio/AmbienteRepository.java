package br.com.cyberbank.ambiente.dominio;

import java.util.List;
import java.util.Optional;

public interface AmbienteRepository {

    Ambiente salvar(Ambiente ambiente);

    Optional<Ambiente> buscar(Long ambienteId);

    List<AcessoAoAmbiente> listarDoUsuario(Long usuarioId);

    List<AmbienteDaRotina> listarParaRotina();
}
