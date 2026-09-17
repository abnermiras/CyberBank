package br.com.cyberbank.conta.dominio;

import java.util.List;
import java.util.Optional;

public interface ContaRepository {

    Conta salvar(Conta conta);

    Optional<Conta> buscarDoAmbiente(Long id, Long ambienteId);

    List<Conta> listarDoAmbiente(Long ambienteId);

    void excluir(Long id, Long ambienteId);
}
