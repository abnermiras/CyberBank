package br.com.cyberbank.ambiente.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface AmbienteJpa extends JpaRepository<AmbienteEntity, Long> {

    /**
     * O RLS ja limita as duas pontas — acesso pelo usuario, ambiente pelo acesso. A condicao
     * aqui e a mesma coisa dita na aplicacao, e e o desenho do ADR-0002: duas camadas.
     */
    @Query("""
            select a, ac.papel
              from AcessoEntity ac
              join AmbienteEntity a on a.id = ac.ambienteId
             where ac.usuarioId = :usuarioId
             order by a.id
            """)
    List<Object[]> listarComPapel(Long usuarioId);
}
