package br.com.cyberbank.categoria.persistencia;

import java.time.Instant;

import br.com.cyberbank.categoria.dominio.CorDeCategoria;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * As colunas geradas {@code nivel} e {@code pai_nivel} nao sao mapeadas de proposito: elas
 * existem para a chave estrangeira que garante os dois niveis da arvore, e quem as escreve e
 * o banco (docs/03-dados/catalogo-tabelas.md).
 */
@Entity
@Table(name = "categoria")
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambiente_id", nullable = false)
    private Long ambienteId;

    @Column(name = "pai_id")
    private Long paiId;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentido sentido;

    @Enumerated(EnumType.STRING)
    @Column(name = "cor")
    private CorDeCategoria cor;

    @Column(nullable = false)
    private boolean sistema;

    @Enumerated(EnumType.STRING)
    @Column(name = "operacao")
    private OperacaoDeSistema operacao;

    @Column(nullable = false)
    private boolean inativa;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected CategoriaEntity() {
    }

    public CategoriaEntity(Long id, Long ambienteId, Long paiId, String nome, Sentido sentido,
            CorDeCategoria cor, boolean sistema, OperacaoDeSistema operacao, boolean inativa,
            Instant criadaEm) {
        this.id = id;
        this.ambienteId = ambienteId;
        this.paiId = paiId;
        this.nome = nome;
        this.sentido = sentido;
        this.cor = cor;
        this.sistema = sistema;
        this.operacao = operacao;
        this.inativa = inativa;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public Long getAmbienteId() {
        return ambienteId;
    }

    public Long getPaiId() {
        return paiId;
    }

    public String getNome() {
        return nome;
    }

    public Sentido getSentido() {
        return sentido;
    }

    public CorDeCategoria getCor() {
        return cor;
    }

    public boolean isSistema() {
        return sistema;
    }

    public OperacaoDeSistema getOperacao() {
        return operacao;
    }

    public boolean isInativa() {
        return inativa;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
