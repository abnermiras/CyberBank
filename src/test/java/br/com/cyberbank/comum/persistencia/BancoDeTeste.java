package br.com.cyberbank.comum.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * O Postgres da suite de integracao (ADR-0011): mesma versao de producao, e subindo com
 * <strong>o mesmo script</strong> que o {@code compose.yml} usa — o de
 * {@code docker/postgres-init/01-papeis.sh}.
 *
 * <p>Um script so para os dois lados nao e economia de linha: conteiner de teste montado a
 * mao e conteiner que diverge do banco real sem ninguem perceber, e ai o teste passa a
 * provar a montagem que ele mesmo inventou.
 */
public final class BancoDeTeste {

    public static final String BANCO = "cyberbank";
    public static final String DONO = "cyberbank_dono";
    public static final String SENHA_DO_DONO = "dono-de-teste";
    public static final String APLICACAO = "cyberbank_app";
    public static final String SENHA_DA_APLICACAO = "app-de-teste";

    private static final String SCRIPT_DOS_PAPEIS = "docker/postgres-init/01-papeis.sh";

    private BancoDeTeste() {
    }

    public static PostgreSQLContainer novoContainer() {
        return new PostgreSQLContainer(imagem())
                .withDatabaseName(BANCO)
                // O superusuario de bootstrap, como no compose: existe para o script
                // criar os dois papeis, e nao e nenhum deles.
                .withUsername("postgres")
                .withPassword("admin-de-teste")
                .withEnv("DB_OWNER_USER", DONO)
                .withEnv("DB_OWNER_PASSWORD", SENHA_DO_DONO)
                .withEnv("DB_APP_USER", APLICACAO)
                .withEnv("DB_APP_PASSWORD", SENHA_DA_APLICACAO)
                .withCopyFileToContainer(
                        MountableFile.forHostPath(SCRIPT_DOS_PAPEIS),
                        "/docker-entrypoint-initdb.d/01-papeis.sh");
    }

    /** A conexao da aplicacao: o papel nao-dono, que e com o que todo teste conecta. */
    public static Connection conexaoDaAplicacao(PostgreSQLContainer postgres) throws SQLException {
        return DriverManager.getConnection(
                postgres.getJdbcUrl(), APLICACAO, SENHA_DA_APLICACAO);
    }

    /**
     * A versao vem do {@code <postgres.imagem>} do {@code pom.xml}, que e a mesma string do
     * {@code compose.yml}. Sem ela a versao viraria "a que estiver", que e exatamente o que o
     * ADR-0011 recusa — entao falta dela e erro, nao valor padrao.
     */
    private static DockerImageName imagem() {
        String imagem = System.getProperty("postgres.imagem");
        if (imagem == null || imagem.isBlank()) {
            throw new IllegalStateException(
                    "Propriedade postgres.imagem ausente. Rode pelo ./mvnw verify, ou passe"
                            + " -Dpostgres.imagem=<a mesma do compose.yml> na IDE.");
        }
        return DockerImageName.parse(imagem);
    }
}
