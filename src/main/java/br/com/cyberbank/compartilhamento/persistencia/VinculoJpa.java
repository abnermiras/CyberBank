package br.com.cyberbank.compartilhamento.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface VinculoJpa extends JpaRepository<VinculoEntity, Long> {

    Optional<VinculoEntity> findByContaIdAndAmbienteDestinoId(Long contaId, Long ambienteDestinoId);

    List<VinculoEntity> findByContaIdAndAmbienteOrigemIdOrderByIdAsc(Long contaId,
            Long ambienteOrigemId);

    List<VinculoEntity> findByAmbienteDestinoIdOrderByIdAsc(Long ambienteDestinoId);

    void deleteByContaIdAndAmbienteDestinoId(Long contaId, Long ambienteDestinoId);
}
