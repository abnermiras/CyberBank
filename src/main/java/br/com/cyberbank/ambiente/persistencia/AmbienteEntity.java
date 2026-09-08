package br.com.cyberbank.ambiente.persistencia;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ambiente")
public class AmbienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "criado_por", nullable = false)
    private Long criadoPor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected AmbienteEntity() {
    }

    public AmbienteEntity(Long id, String nome, Long criadoPor, Instant criadoEm) {
        this.id = id;
        this.nome = nome;
        this.criadoPor = criadoPor;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Long getCriadoPor() {
        return criadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
