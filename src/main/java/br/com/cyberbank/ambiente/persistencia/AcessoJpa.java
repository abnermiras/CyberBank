package br.com.cyberbank.ambiente.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface AcessoJpa extends JpaRepository<AcessoEntity, Long> {

    Optional<AcessoEntity> findByUsuarioIdAndAmbienteId(Long usuarioId, Long ambienteId);

    @Query(value = "select id, usuario_id, papel, criado_em from membros_do_ambiente(:ambienteId)",
            nativeQuery = true)
    List<Object[]> listarMembros(Long ambienteId);

    @Query(value = "select remover_acesso(:ambienteId, :usuarioId)", nativeQuery = true)
    int removerAcesso(Long ambienteId, Long usuarioId);
}
