package br.com.cyberbank.conta.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ContaJpa extends JpaRepository<ContaEntity, Long> {

    List<ContaEntity> findByAmbienteIdOrderByNomeAsc(Long ambienteId);

    Optional<ContaEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);
}
