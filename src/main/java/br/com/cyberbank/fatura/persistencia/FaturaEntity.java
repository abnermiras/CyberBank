package br.com.cyberbank.fatura.persistencia;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.fatura.dominio.StatusDaFatura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fatura")
public class FaturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(nullable = false)
    private LocalDate competencia;

    @Column(name = "data_fechamento", nullable = false)
    private LocalDate dataFechamento;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDaFatura status;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected FaturaEntity() {
    }

    public FaturaEntity(Long id, Long ambienteId, Long contaId, LocalDate competencia,
            LocalDate dataFechamento, LocalDate dataVencimento, StatusDaFatura status,
            Instant criadaEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.contaId = contaId;
        this.competencia = competencia;
        this.dataFechamento = dataFechamento;
        this.dataVencimento = dataVencimento;
        this.status = status;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public Long getContaId() {
        return contaId;
    }

    public LocalDate getCompetencia() {
        return competencia;
    }

    public LocalDate getDataFechamento() {
        return dataFechamento;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusDaFatura getStatus() {
        return status;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
