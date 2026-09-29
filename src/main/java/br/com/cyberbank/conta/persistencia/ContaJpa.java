package br.com.cyberbank.conta.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ContaJpa extends JpaRepository<ContaEntity, Long> {

    List<ContaEntity> findByAmbienteIdOrderByNomeAsc(Long ambienteId);

    Optional<ContaEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);

    @Query(value = """
            select c.* from conta c
             where c.ambiente_id = :ambienteId
                or exists (select 1 from vinculo v
                            where v.conta_id = c.id and v.ambiente_destino_id = :ambienteId)
             order by c.nome asc, c.id asc
            """, nativeQuery = true)
    List<ContaEntity> findAcessiveis(@Param("ambienteId") Long ambienteId);

    @Query(value = """
            select c.* from conta c
             where c.id = :id
               and (c.ambiente_id = :ambienteId
                    or exists (select 1 from vinculo v
                                where v.conta_id = c.id and v.ambiente_destino_id = :ambienteId))
            """, nativeQuery = true)
    Optional<ContaEntity> findAcessivel(@Param("id") Long id,
            @Param("ambienteId") Long ambienteId);
}
