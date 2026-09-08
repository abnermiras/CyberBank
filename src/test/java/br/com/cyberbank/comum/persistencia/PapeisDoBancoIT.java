package br.com.cyberbank.comum.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * O RLS do ADR-0002 nao depende da politica estar escrita: depende de <strong>com qual papel a
 * aplicacao conecta</strong>. O Postgres nao aplica politica ao dono da tabela nem a quem tem
 * {@code BYPASSRLS} — entao, se o {@code DB_APP_USER} apontar um dia para o dono, tudo
 * continua funcionando, nenhum teste fica vermelho, e o isolamento simplesmente deixa de
 * existir, calado.
 *
 * <p>Este teste e a montagem do banco virando verificacao:
 * <em>defesa que depende de alguem lembrar nao e defesa</em>
 * (docs/01-arquitetura/seguranca.md). Ele nao precisa de tabela nenhuma — o que ele prova
 * vale antes da primeira migration.
 */
@Testcontainers
class PapeisDoBancoIT {

    @Container
    static final PostgreSQLContainer POSTGRES = BancoDeTeste.novoContainer();

    @Test
    void o_papel_da_aplicacao_nao_e_superusuario() throws SQLException {
        assertThat(consultaBooleana("select rolsuper from pg_roles where rolname = current_user"))
                .as("superusuario ignora politica de RLS sempre, e em toda tabela")
                .isFalse();
    }

    @Test
    void o_papel_da_aplicacao_nao_tem_bypassrls() throws SQLException {
        assertThat(consultaBooleana("select rolbypassrls from pg_roles where rolname = current_user"))
                .as("BYPASSRLS e a permissao de ignorar a politica sem ser dono nem superusuario")
                .isFalse();
    }

    @Test
    void o_papel_da_aplicacao_nao_e_dono_do_schema_public() throws SQLException {
        assertThat(consultaBooleana(
                "select pg_get_userbyid(nspowner) = current_user"
                        + " from pg_namespace where nspname = 'public'"))
                .as("dono do schema e dono das tabelas que nascem nele, e dono nao leva politica")
                .isFalse();
    }

    /** Sempre com a conexao da aplicacao: o papel sob suspeita e quem responde. */
    private boolean consultaBooleana(String sql) throws SQLException {
        try (Connection conexao = BancoDeTeste.conexaoDaAplicacao(POSTGRES);
                Statement comando = conexao.createStatement();
                ResultSet resultado = comando.executeQuery(sql)) {
            assertThat(resultado.next()).as("a consulta nao devolveu linha: %s", sql).isTrue();
            return resultado.getBoolean(1);
        }
    }
}
