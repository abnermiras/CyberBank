package br.com.cyberbank.compartilhamento.persistencia;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vinculo")
public class VinculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String objeto;

    @Column(name = "conta_id")
    private Long contaId;

    @Column(name = "ambiente_origem_id", nullable = false)
    private Long ambienteOrigemId;

    @Column(name = "ambiente_destino_id", nullable = false)
    private Long ambienteDestinoId;

    @Column(name = "criado_por", nullable = false)
    private Long criadoPor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected VinculoEntity() {
    }

    public VinculoEntity(Long id, String objeto, Long contaId, Long ambienteOrigemId,
            Long ambienteDestinoId, Long criadoPor, Instant criadoEm) {
        this.id = id;
        this.objeto = objeto;
        this.contaId = contaId;
        this.ambienteOrigemId = ambienteOrigemId;
        this.ambienteDestinoId = ambienteDestinoId;
        this.criadoPor = criadoPor;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getContaId() {
        return contaId;
    }

    public Long getAmbienteOrigemId() {
        return ambienteOrigemId;
    }

    public Long getAmbienteDestinoId() {
        return ambienteDestinoId;
    }

    public Long getCriadoPor() {
        return criadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
