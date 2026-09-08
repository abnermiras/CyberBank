package br.com.cyberbank.ambiente.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SessaoJpa extends JpaRepository<SessaoEntity, Long> {

    Optional<SessaoEntity> findByIdentificadorHash(String identificadorHash);

    void deleteByUsuarioId(Long usuarioId);
}
