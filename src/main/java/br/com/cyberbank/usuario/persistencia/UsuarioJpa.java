package br.com.cyberbank.usuario.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface UsuarioJpa extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByTelegramChatId(Long telegramChatId);
}
