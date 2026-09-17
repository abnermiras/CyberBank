package br.com.cyberbank.evento.persistencia;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import br.com.cyberbank.evento.dominio.OrigemDeEvento;
import br.com.cyberbank.evento.dominio.TipoDeAlvo;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(name = "evento")
public class EventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(nullable = false)
    private LocalDate dia;

    @Column(nullable = false)
    private Instant instante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemDeEvento origem;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeEvento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "alvo_tipo")
    private TipoDeAlvo alvoTipo;

    @Column(name = "alvo_id")
    private Long alvoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados")
    private Map<String, Object> dados;

    protected EventoEntity() {
    }

    public EventoEntity(Long id, Long ambienteId, LocalDate dia, Instant instante,
            OrigemDeEvento origem, Long autorId, TipoDeEvento tipo, TipoDeAlvo alvoTipo,
            Long alvoId, Map<String, Object> dados) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.dia = dia;
        this.instante = instante;
        this.origem = origem;
        this.autorId = autorId;
        this.tipo = tipo;
        this.alvoTipo = alvoTipo;
        this.alvoId = alvoId;
        this.dados = dados;
    }

    public Long getId() {
        return id;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public LocalDate getDia() {
        return dia;
    }

    public Instant getInstante() {
        return instante;
    }

    public OrigemDeEvento getOrigem() {
        return origem;
    }

    public Long getAutorId() {
        return autorId;
    }

    public TipoDeEvento getTipo() {
        return tipo;
    }

    public TipoDeAlvo getAlvoTipo() {
        return alvoTipo;
    }

    public Long getAlvoId() {
        return alvoId;
    }

    public Map<String, Object> getDados() {
        return dados;
    }
}
