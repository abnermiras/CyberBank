package br.com.cyberbank.lancamento.persistencia;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lancamento")
public class LancamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(name = "meio_id")
    private Long meioId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentido sentido;

    @Column(name = "valor_centavos", nullable = false)
    private long valorCentavos;

    @Column(name = "data_evento", nullable = false)
    private LocalDate dataEvento;

    @Column(name = "data_efeito", nullable = false)
    private LocalDate dataEfeito;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Situacao situacao;

    @Column(name = "transferencia_id")
    private Long transferenciaId;

    @Column(name = "estorno_de_id")
    private Long estornoDeId;

    @Column(name = "fatura_id")
    private Long faturaId;

    @Column(name = "pagamento_de_fatura_id")
    private Long pagamentoDeFaturaId;

    @Column(name = "rolagem_de_fatura")
    private Long rolagemDeFatura;

    @Column(name = "parcelamento_id")
    private Long parcelamentoId;

    @Column(name = "recorrencia_id")
    private Long recorrenciaId;

    @Column(name = "do_ciclo", nullable = false)
    private boolean doCiclo;

    @Column(name = "estabelecimento")
    private String estabelecimento;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected LancamentoEntity() {
    }

    public LancamentoEntity(Long id, Long ambienteId, Long contaId, Long meioId, Long categoriaId,
            Long autorId, Sentido sentido, long valorCentavos, LocalDate dataEvento,
            LocalDate dataEfeito, String descricao, Situacao situacao, Long transferenciaId,
            Long estornoDeId, Long faturaId, Long pagamentoDeFaturaId, Long rolagemDeFatura,
            Long parcelamentoId, Long recorrenciaId, boolean doCiclo, String estabelecimento,
            Instant criadoEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.contaId = contaId;
        this.meioId = meioId;
        this.categoriaId = categoriaId;
        this.autorId = autorId;
        this.sentido = sentido;
        this.valorCentavos = valorCentavos;
        this.dataEvento = dataEvento;
        this.dataEfeito = dataEfeito;
        this.descricao = descricao;
        this.situacao = situacao;
        this.transferenciaId = transferenciaId;
        this.estornoDeId = estornoDeId;
        this.faturaId = faturaId;
        this.pagamentoDeFaturaId = pagamentoDeFaturaId;
        this.rolagemDeFatura = rolagemDeFatura;
        this.parcelamentoId = parcelamentoId;
        this.recorrenciaId = recorrenciaId;
        this.doCiclo = doCiclo;
        this.estabelecimento = estabelecimento;
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

    public Long getAutorId() {
        return autorId;
    }

    public Sentido getSentido() {
        return sentido;
    }

    public long getValorCentavos() {
        return valorCentavos;
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    public LocalDate getDataEfeito() {
        return dataEfeito;
    }

    public String getDescricao() {
        return descricao;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public Long getTransferenciaId() {
        return transferenciaId;
    }

    public Long getEstornoDeId() {
        return estornoDeId;
    }

    public Long getFaturaId() {
        return faturaId;
    }

    public Long getPagamentoDeFaturaId() {
        return pagamentoDeFaturaId;
    }

    public Long getRolagemDeFatura() {
        return rolagemDeFatura;
    }

    public Long getParcelamentoId() {
        return parcelamentoId;
    }

    public Long getRecorrenciaId() {
        return recorrenciaId;
    }

    public boolean isDoCiclo() {
        return doCiclo;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
