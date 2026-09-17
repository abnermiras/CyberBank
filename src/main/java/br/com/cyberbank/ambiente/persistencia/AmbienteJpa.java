package br.com.cyberbank.ambiente.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface AmbienteJpa extends JpaRepository<AmbienteEntity, Long> {

    @Query("""
            select a, ac.papel
              from AcessoEntity ac
              join AmbienteEntity a on a.id = ac.ambienteId
             where ac.usuarioId = :usuarioId
             order by a.id
            """)
    List<Object[]> listarComPapel(Long usuarioId);

    @Query(value = "select ambiente_id, dono_id from ambientes_para_rotina()", nativeQuery = true)
    List<Object[]> listarParaRotina();
}
