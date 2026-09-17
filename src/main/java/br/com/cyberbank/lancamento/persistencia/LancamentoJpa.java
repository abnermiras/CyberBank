package br.com.cyberbank.lancamento.persistencia;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LancamentoJpa extends JpaRepository<LancamentoEntity, Long> {

    Optional<LancamentoEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    List<LancamentoEntity> findByTransferenciaIdAndAmbienteIdOrderByIdAsc(
            Long transferenciaId, Long ambienteId);

    boolean existsByContaId(Long contaId);

    boolean existsByMeioId(Long meioId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByEstornoDeId(Long estornoDeId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);

    @Modifying
    @Query("""
            delete from LancamentoEntity l
             where l.contaId = :contaId and l.ambienteId = :ambienteId
               and l.situacao = :previsto
            """)
    void apagarPrevistosDaConta(@Param("contaId") Long contaId,
            @Param("ambienteId") Long ambienteId,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and (:contaId is null or l.contaId = :contaId)
               and (:somentePendentes = false or l.categoriaId is null)
             order by l.dataEvento desc, l.id desc
            """)
    List<LancamentoEntity> primeiraPagina(@Param("ambienteId") Long ambienteId,
            @Param("contaId") Long contaId,
            @Param("somentePendentes") boolean somentePendentes,
            Pageable pagina);

    @Query("""
            select l from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and (:contaId is null or l.contaId = :contaId)
               and (:somentePendentes = false or l.categoriaId is null)
               and (l.dataEvento < :dataEvento
                    or (l.dataEvento = :dataEvento and l.id < :id))
             order by l.dataEvento desc, l.id desc
            """)
    List<LancamentoEntity> paginaApos(@Param("ambienteId") Long ambienteId,
            @Param("contaId") Long contaId,
            @Param("somentePendentes") boolean somentePendentes,
            @Param("dataEvento") LocalDate dataEvento,
            @Param("id") Long id,
            Pageable pagina);

    @Query("""
            select coalesce(sum(case when l.sentido = :entrada
                                     then l.valorCentavos else -l.valorCentavos end), 0)
              from LancamentoEntity l
             where l.contaId = :contaId
               and l.situacao <> :previsto
               and l.dataEfeito <= :ate
            """)
    long somarRealizadoDaConta(@Param("contaId") Long contaId,
            @Param("ate") LocalDate ate,
            @Param("entrada") Sentido entrada,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l.contaId, coalesce(sum(case when l.sentido = :entrada
                                                then l.valorCentavos else -l.valorCentavos end), 0)
              from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao <> :previsto
               and l.dataEfeito <= :ate
             group by l.contaId
            """)
    List<Object[]> somarRealizadoPorConta(@Param("ambienteId") Long ambienteId,
            @Param("ate") LocalDate ate,
            @Param("entrada") Sentido entrada,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao = :previsto
               and l.dataEfeito <= :ate
             order by l.dataEfeito asc, l.id asc
            """)
    List<LancamentoEntity> previstosVencidos(@Param("ambienteId") Long ambienteId,
            @Param("ate") LocalDate ate,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l.categoriaId, l.sentido, sum(l.valorCentavos), count(l)
              from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao <> :previsto
               and l.dataEvento between :de and :ate
               and l.contaId in :contas
             group by l.categoriaId, l.sentido
            """)
    List<Object[]> somarPorCategoria(@Param("ambienteId") Long ambienteId,
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("contas") Collection<Long> contas,
            @Param("previsto") Situacao previsto);

    @Query("""
            select coalesce(sum(l.valorCentavos), 0)
              from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao <> :previsto
               and l.sentido = :entrada
               and l.transferenciaId is not null
               and l.dataEvento between :de and :ate
               and l.contaId in :contas
            """)
    long somarAportes(@Param("ambienteId") Long ambienteId,
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("contas") Collection<Long> contas,
            @Param("entrada") Sentido entrada,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l.sentido, coalesce(sum(l.valorCentavos), 0)
              from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao = :previsto
               and l.dataEfeito between :de and :ate
               and l.contaId in :contas
             group by l.sentido
            """)
    List<Object[]> somarPrevistos(@Param("ambienteId") Long ambienteId,
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("contas") Collection<Long> contas,
            @Param("previsto") Situacao previsto);

    @Query("""
            select l from LancamentoEntity l
             where l.ambienteId = :ambienteId
               and l.situacao = :previsto
               and l.dataEfeito between :de and :ate
               and l.contaId in :contas
             order by l.dataEfeito asc, l.id asc
            """)
    List<LancamentoEntity> previstosDoHorizonte(@Param("ambienteId") Long ambienteId,
            @Param("de") LocalDate de,
            @Param("ate") LocalDate ate,
            @Param("contas") Collection<Long> contas,
            @Param("previsto") Situacao previsto,
            Pageable pagina);

    long countByAmbienteIdAndCategoriaIdIsNull(Long ambienteId);

    @Query(value = "select nextval('transferencia_id_seq')", nativeQuery = true)
    long proximoIdDeTransferencia();
}
