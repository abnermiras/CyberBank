package br.com.cyberbank.recorrencia.dominio;

import java.util.List;
import java.util.Optional;

public interface RecorrenciaRepository {

    Recorrencia salvar(Recorrencia recorrencia);

    Optional<Recorrencia> buscarDoAmbiente(Long id, Long ambienteId);

    List<Recorrencia> listarAtivasDaConta(Long contaId);

    List<Recorrencia> listarDoAmbiente(Long ambienteId);
}
