package br.com.cyberbank.meio.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MeioJpa extends JpaRepository<MeioEntity, Long> {

    List<MeioEntity> findByAmbienteIdOrderByNomeAsc(Long ambienteId);

    List<MeioEntity> findByContaIdAndAmbienteIdOrderByNomeAsc(Long contaId, Long ambienteId);

    Optional<MeioEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    boolean existsByContaIdAndTipoAndAmbienteId(Long contaId, TipoDeMeio tipo, Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);

    @Query(value = """
            select m.* from meio m
             where m.ambiente_id = :ambienteId
                or exists (select 1 from vinculo v
                            where v.ambiente_destino_id = :ambienteId
                              and (v.conta_id = m.conta_id or v.meio_id = m.id))
             order by m.nome asc, m.id asc
            """, nativeQuery = true)
    List<MeioEntity> findAcessiveis(@Param("ambienteId") Long ambienteId);

    @Query(value = """
            select m.* from meio m
             where m.id = :id
               and (m.ambiente_id = :ambienteId
                    or exists (select 1 from vinculo v
                                where v.ambiente_destino_id = :ambienteId
                                  and (v.conta_id = m.conta_id or v.meio_id = m.id)))
            """, nativeQuery = true)
    Optional<MeioEntity> findAcessivel(@Param("id") Long id,
            @Param("ambienteId") Long ambienteId);
}
