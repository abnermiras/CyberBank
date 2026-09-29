package br.com.cyberbank.compartilhamento.dominio;

import java.util.List;
import java.util.Optional;

public interface CompartilhamentoRepository {

    Compartilhamento salvar(Compartilhamento compartilhamento);

    Optional<Compartilhamento> buscar(Long contaId, Long ambienteDestinoId);

    List<Compartilhamento> listarDaConta(Long contaId, Long ambienteOrigemId);

    void revogar(Long contaId, Long ambienteDestinoId);
}
