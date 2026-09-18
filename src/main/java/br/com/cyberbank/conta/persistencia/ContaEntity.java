package br.com.cyberbank.conta.persistencia;

import java.time.Instant;
import java.time.LocalDate;

import br.com.cyberbank.conta.dominio.TipoDeConta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conta")
public class ContaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeConta tipo;

    @Column(name = "entra_no_fluxo_de_caixa", nullable = false)
    private boolean entraNoFluxoDeCaixa;

    @Column(name = "entra_em_caixa", nullable = false)
    private boolean entraEmCaixa;

    @Column(name = "limite_centavos")
    private Long limiteCentavos;

    @Column(name = "limite_informado_em")
    private LocalDate limiteInformadoEm;

    @Column(name = "dia_vencimento")
    private Short diaVencimento;

    @Column(name = "dias_antes_fechamento")
    private Short diasAntesFechamento;

    @Column(name = "conta_pagadora_padrao_id")
    private Long contaPagadoraPadraoId;

    @Column(nullable = false)
    private boolean inativa;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected ContaEntity() {
    }

    public ContaEntity(Long id, Long ambienteId, String nome, TipoDeConta tipo,
            boolean entraNoFluxoDeCaixa, boolean entraEmCaixa, Long limiteCentavos,
            LocalDate limiteInformadoEm, Short diaVencimento, Short diasAntesFechamento,
            Long contaPagadoraPadraoId, boolean inativa, Instant criadaEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.nome = nome;
        this.tipo = tipo;
        this.entraNoFluxoDeCaixa = entraNoFluxoDeCaixa;
        this.entraEmCaixa = entraEmCaixa;
        this.limiteCentavos = limiteCentavos;
        this.limiteInformadoEm = limiteInformadoEm;
        this.diaVencimento = diaVencimento;
        this.diasAntesFechamento = diasAntesFechamento;
        this.contaPagadoraPadraoId = contaPagadoraPadraoId;
        this.inativa = inativa;
        this.criadaEm = criadaEm;
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

    public TipoDeConta getTipo() {
        return tipo;
    }

    public boolean isEntraNoFluxoDeCaixa() {
        return entraNoFluxoDeCaixa;
    }

    public boolean isEntraEmCaixa() {
        return entraEmCaixa;
    }

    public Long getLimiteCentavos() {
        return limiteCentavos;
    }

    public LocalDate getLimiteInformadoEm() {
        return limiteInformadoEm;
    }

    public Short getDiaVencimento() {
        return diaVencimento;
    }

    public Short getDiasAntesFechamento() {
        return diasAntesFechamento;
    }

    public Long getContaPagadoraPadraoId() {
        return contaPagadoraPadraoId;
    }

    public boolean isInativa() {
        return inativa;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
