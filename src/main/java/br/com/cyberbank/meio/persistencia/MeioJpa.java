package br.com.cyberbank.meio.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.data.jpa.repository.JpaRepository;

interface MeioJpa extends JpaRepository<MeioEntity, Long> {

    List<MeioEntity> findByAmbienteIdOrderByNomeAsc(Long ambienteId);

    List<MeioEntity> findByContaIdAndAmbienteIdOrderByNomeAsc(Long contaId, Long ambienteId);

    Optional<MeioEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    boolean existsByContaIdAndTipoAndAmbienteId(Long contaId, TipoDeMeio tipo, Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);
}
