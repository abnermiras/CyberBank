package br.com.cyberbank.ambiente.persistencia;

import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.ambiente.dominio.SituacaoDoConvite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "convite")
public class ConviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoConvite situacao;

    @Column(name = "convidado_por", nullable = false)
    private Long convidadoPor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "respondido_em")
    private Instant respondidoEm;

    protected ConviteEntity() {
    }

    public ConviteEntity(Long id, Long ambienteId, String email, Papel papel,
            SituacaoDoConvite situacao, Long convidadoPor, Instant criadoEm, Instant respondidoEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.email = email;
        this.papel = papel;
        this.situacao = situacao;
        this.convidadoPor = convidadoPor;
        this.criadoEm = criadoEm;
        this.respondidoEm = respondidoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public String getEmail() {
        return email;
    }

    public Papel getPapel() {
        return papel;
    }

    public SituacaoDoConvite getSituacao() {
        return situacao;
    }

    public Long getConvidadoPor() {
        return convidadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getRespondidoEm() {
        return respondidoEm;
    }
}
