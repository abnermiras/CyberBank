package br.com.cyberbank.comum.contexto;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

/**
 * Poe {@code app.usuario_id} e {@code app.ambiente_id} na transacao corrente, que e o que as
 * politicas de RLS leem.
 *
 * <p>{@code set_config(..., true)} vale ate o fim da TRANSACAO, e nao da conexao — que e
 * exatamente o que se quer com pool: a conexao volta para o pool sem contexto nenhum.
 */
@Component
public class ContextoDoBanco {

    private final EntityManager entityManager;

    public ContextoDoBanco(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void aplicar(Long usuarioId, Long ambienteId) {
        definir("app.usuario_id", usuarioId);
        definir("app.ambiente_id", ambienteId);
    }

    /**
     * O cadastro e o unico lugar em que o usuario NASCE no meio da transacao: o contexto dele
     * nao existia quando ela abriu, e sem isto o ambiente que ele acabou de criar seria
     * invisivel para ele mesmo.
     */
    public void definirUsuario(Long usuarioId) {
        definir("app.usuario_id", usuarioId);
    }

    public void definirAmbiente(Long ambienteId) {
        definir("app.ambiente_id", ambienteId);
    }

    private void definir(String variavel, Long valor) {
        entityManager
                .createNativeQuery("select set_config(:variavel, :valor, true)")
                .setParameter("variavel", variavel)
                .setParameter("valor", valor == null ? "" : String.valueOf(valor))
                .getSingleResult();
    }
}
