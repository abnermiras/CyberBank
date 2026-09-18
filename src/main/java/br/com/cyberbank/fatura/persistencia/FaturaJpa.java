package br.com.cyberbank.fatura.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.fatura.dominio.StatusDaFatura;

import org.springframework.data.jpa.repository.JpaRepository;

interface FaturaJpa extends JpaRepository<FaturaEntity, Long> {

    Optional<FaturaEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    Optional<FaturaEntity> findByContaIdAndStatus(Long contaId, StatusDaFatura status);

    Optional<FaturaEntity> findByContaIdAndCompetencia(Long contaId, LocalDate competencia);

    List<FaturaEntity> findByContaIdOrderByCompetenciaDesc(Long contaId);

    List<FaturaEntity> findByAmbienteIdAndStatusAndDataVencimentoBetween(Long ambienteId,
            StatusDaFatura status, LocalDate de, LocalDate ate);
}
