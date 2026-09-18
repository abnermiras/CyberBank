package br.com.cyberbank.recorrencia.persistencia;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parcelamento")
public class ParcelamentoEntity {

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

    @Column(name = "valor_da_compra_centavos", nullable = false)
    private long valorDaCompraCentavos;

    @Column(nullable = false)
    private Short parcelas;

    @Column(name = "data_da_compra", nullable = false)
    private LocalDate dataDaCompra;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ParcelamentoEntity() {
    }

    public ParcelamentoEntity(Long id, Long ambienteId, Long contaId, Long meioId,
            Long categoriaId, long valorDaCompraCentavos, Short parcelas, LocalDate dataDaCompra,
            String descricao, Instant criadoEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.contaId = contaId;
        this.meioId = meioId;
        this.categoriaId = categoriaId;
        this.valorDaCompraCentavos = valorDaCompraCentavos;
        this.parcelas = parcelas;
        this.dataDaCompra = dataDaCompra;
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

    public long getValorDaCompraCentavos() {
        return valorDaCompraCentavos;
    }

    public Short getParcelas() {
        return parcelas;
    }

    public LocalDate getDataDaCompra() {
        return dataDaCompra;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
