package br.com.cyberbank.meio.dominio;

import java.util.List;
import java.util.Optional;

public interface MeioRepository {

    Meio salvar(Meio meio);

    Optional<Meio> buscarDoAmbiente(Long id, Long ambienteId);

    List<Meio> listarDoAmbiente(Long ambienteId);

    List<Meio> listarDaConta(Long contaId, Long ambienteId);

    boolean existeNaConta(Long contaId, TipoDeMeio tipo, Long ambienteId);

    void excluir(Long id, Long ambienteId);
}
