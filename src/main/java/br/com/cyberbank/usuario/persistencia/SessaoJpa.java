package br.com.cyberbank.usuario.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SessaoJpa extends JpaRepository<SessaoEntity, Long> {

    Optional<SessaoEntity> findByIdentificadorHash(String identificadorHash);

    List<SessaoEntity> findByUsuarioIdOrderByUltimoUsoEmDesc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
