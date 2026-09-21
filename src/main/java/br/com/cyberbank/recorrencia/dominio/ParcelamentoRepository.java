package br.com.cyberbank.recorrencia.dominio;

import java.util.List;
import java.util.Optional;

public interface ParcelamentoRepository {

    Parcelamento salvar(Parcelamento parcelamento);

    Optional<Parcelamento> buscarDoAmbiente(Long id, Long ambienteId);

    List<Parcelamento> listarDoAmbiente(Long ambienteId);

    void excluir(Long id, Long ambienteId);
}
