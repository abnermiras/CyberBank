package br.com.cyberbank.recorrencia.dominio;

import java.util.Optional;

public interface ParcelamentoRepository {

    Parcelamento salvar(Parcelamento parcelamento);

    Optional<Parcelamento> buscarDoAmbiente(Long id, Long ambienteId);

    void excluir(Long id, Long ambienteId);
}
