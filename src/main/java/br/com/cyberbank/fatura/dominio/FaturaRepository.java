package br.com.cyberbank.fatura.dominio;

import java.util.List;
import java.util.Optional;

public interface FaturaRepository {

    Fatura salvar(Fatura fatura);

    Optional<Fatura> buscarDoAmbiente(Long id, Long ambienteId);

    Optional<Fatura> buscarAbertaDaConta(Long contaId);

    List<Fatura> listarDaConta(Long contaId);
}
