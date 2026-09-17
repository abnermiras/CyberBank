package br.com.cyberbank.meio.persistencia;

import java.time.Instant;

import br.com.cyberbank.meio.dominio.TipoDeMeio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meio")
public class MeioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(name = "nome")
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeMeio tipo;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(nullable = false)
    private boolean inativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected MeioEntity() {
    }

    public MeioEntity(Long id, Long ambienteId, String nome, TipoDeMeio tipo, Long contaId,
            boolean inativo, Instant criadoEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.nome = nome;
        this.tipo = tipo;
        this.contaId = contaId;
        this.inativo = inativo;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public String getNome() {
        return nome;
    }

    public TipoDeMeio getTipo() {
        return tipo;
    }

    public Long getContaId() {
        return contaId;
    }

    public boolean isInativo() {
        return inativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
