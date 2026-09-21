package br.com.cyberbank.recorrencia.persistencia;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.recorrencia.dominio.Periodicidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recorrencia")
public class RecorrenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(name = "meio_id", nullable = false)
    private Long meioId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "valor_centavos", nullable = false)
    private long valorCentavos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Periodicidade periodicidade;

    @Column(nullable = false)
    private Short dia;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private boolean ativa;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected RecorrenciaEntity() {
    }

    public RecorrenciaEntity(Long id, Long ambienteId, Long contaId, Long meioId,
            Long categoriaId, long valorCentavos, Periodicidade periodicidade, Short dia,
            LocalDate inicio, boolean ativa, String descricao, Instant criadoEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.contaId = contaId;
        this.meioId = meioId;
        this.categoriaId = categoriaId;
        this.valorCentavos = valorCentavos;
        this.periodicidade = periodicidade;
        this.dia = dia;
        this.inicio = inicio;
        this.ativa = ativa;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
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

    public Long getMeioId() {
        return meioId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public long getValorCentavos() {
        return valorCentavos;
    }

    public Periodicidade getPeriodicidade() {
        return periodicidade;
    }

    public Short getDia() {
        return dia;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
