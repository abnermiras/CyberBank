package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FaturaRepository {

    Fatura salvar(Fatura fatura);

    Optional<Fatura> buscarDaCompetencia(Long contaId, YearMonth competencia);

    Optional<Fatura> buscarDoAmbiente(Long id, Long ambienteId);

    Optional<Fatura> buscarAbertaDaConta(Long contaId);

    List<Fatura> listarDaConta(Long contaId);

    List<Fatura> listarDoAmbienteVencendoEntre(Long ambienteId, LocalDate de, LocalDate ate);
}
