package br.com.cyberbank.comum.configuracao;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;

import jakarta.persistence.EntityManager;

import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Toda transacao nasce carregando o contexto para o RLS.
 *
 * <p>O {@code SET LOCAL} podia ser a primeira linha de cada caso de uso — e seria a defesa que
 * depende de alguem lembrar (docs/01-arquitetura/seguranca.md). Aqui ele acontece no
 * {@code doBegin}, antes de qualquer consulta da transacao, e nao ha caso de uso que possa
 * esquecer.
 */
public class GerenciadorDeTransacaoComContexto extends JpaTransactionManager {

    public GerenciadorDeTransacaoComContexto(jakarta.persistence.EntityManagerFactory fabrica) {
        super(fabrica);
    }

    @Override
    protected void doBegin(Object transacao, TransactionDefinition definicao) {
        super.doBegin(transacao, definicao);

        EntityManagerHolder suporte =
                (EntityManagerHolder) TransactionSynchronizationManager.getResource(getEntityManagerFactory());
        if (suporte == null) {
            return;
        }
        EntityManager entityManager = suporte.getEntityManager();

        definir(entityManager, "app.usuario_id", ContextoDaRequisicao.usuarioId());
        definir(entityManager, "app.ambiente_id", ContextoDaRequisicao.ambienteId());
    }

    private void definir(EntityManager entityManager, String variavel, Long valor) {
        entityManager
                .createNativeQuery("select set_config(:variavel, :valor, true)")
                .setParameter("variavel", variavel)
                .setParameter("valor", valor == null ? "" : String.valueOf(valor))
                .getSingleResult();
    }
}
