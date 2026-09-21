package br.com.cyberbank.recorrencia.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ParcelamentoJpa extends JpaRepository<ParcelamentoEntity, Long> {

    Optional<ParcelamentoEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    List<ParcelamentoEntity> findByAmbienteIdOrderByIdDesc(Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);
}
