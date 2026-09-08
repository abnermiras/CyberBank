package br.com.cyberbank.ambiente.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface AcessoJpa extends JpaRepository<AcessoEntity, Long> {

    Optional<AcessoEntity> findByUsuarioIdAndAmbienteId(Long usuarioId, Long ambienteId);
}
