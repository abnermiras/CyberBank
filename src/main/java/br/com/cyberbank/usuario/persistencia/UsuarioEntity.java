package br.com.cyberbank.usuario.persistencia;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nome;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private String avatar;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(Long id, String email, String nome, String senhaHash, String avatar,
            Long telegramChatId, Instant criadoEm) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.avatar = avatar;
        this.telegramChatId = telegramChatId;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getAvatar() {
        return avatar;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
