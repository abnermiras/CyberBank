package br.com.cyberbank.usuario.persistencia;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessao")
public class SessaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "identificador_hash", nullable = false)
    private String identificadorHash;

    @Column(name = "criado_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "ultimo_uso_em", nullable = false)
    private Instant ultimoUsoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "origem")
    private String origem;

    protected SessaoEntity() {
    }

    public SessaoEntity(Long id, Long usuarioId, String identificadorHash, Instant criadaEm,
            Instant ultimoUsoEm, Instant expiraEm, String origem) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.identificadorHash = identificadorHash;
        this.criadaEm = criadaEm;
        this.ultimoUsoEm = ultimoUsoEm;
        this.expiraEm = expiraEm;
        this.origem = origem;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getIdentificadorHash() {
        return identificadorHash;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getUltimoUsoEm() {
        return ultimoUsoEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public String getOrigem() {
        return origem;
    }
}
