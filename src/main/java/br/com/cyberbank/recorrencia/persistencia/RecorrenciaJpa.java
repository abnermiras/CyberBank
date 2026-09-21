package br.com.cyberbank.recorrencia.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface RecorrenciaJpa extends JpaRepository<RecorrenciaEntity, Long> {

    Optional<RecorrenciaEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    List<RecorrenciaEntity> findByContaIdAndAtivaTrueOrderByIdAsc(Long contaId);

    List<RecorrenciaEntity> findByAmbienteIdOrderByAtivaDescIdDesc(Long ambienteId);
}
