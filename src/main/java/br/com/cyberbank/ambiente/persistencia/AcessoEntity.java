package br.com.cyberbank.ambiente.persistencia;

import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Papel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "acesso")
public class AcessoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    /** varchar com CHECK no banco; STRING aqui. O ordinal amarraria a ordem do enum ao dado. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected AcessoEntity() {
    }

    public AcessoEntity(Long id, Long usuarioId, Long ambienteId, Papel papel, Instant criadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.ambienteId = ambienteId;
        this.papel = papel;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public Papel getPapel() {
        return papel;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
