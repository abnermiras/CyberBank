package br.com.cyberbank.comum.configuracao;

import java.util.List;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

/**
 * Falta de variavel obrigatoria derruba a aplicacao na subida, com o nome dela na
 * mensagem (docs/07-operacao/build-e-run.md).
 *
 * <p>Nao existe valor padrao para segredo: padrao silencioso e como uma senha de
 * desenvolvimento chega em producao. Por isso a checagem acontece aqui, com o ambiente
 * ja montado e antes do primeiro bean — e nao como um {@code :padrao} no
 * {@code application.yml}.
 */
public class VariaveisObrigatorias implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final List<String> NOMES = List.of(
            "DB_URL",
            "DB_APP_USER",
            "DB_APP_PASSWORD",
            "DB_OWNER_USER",
            "DB_OWNER_PASSWORD",
            "ARGON2_MEMORIA",
            "ARGON2_ITERACOES",
            "ARGON2_PARALELISMO",
            "SMTP_USUARIO",
            "SMTP_SENHA_DE_APP",
            "CYBERBANK_URL_BASE");

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent evento) {
        List<String> ausentes = NOMES.stream()
                .filter(nome -> !StringUtils.hasText(evento.getEnvironment().getProperty(nome)))
                .toList();

        if (!ausentes.isEmpty()) {
            throw new IllegalStateException(
                    "Variavel de ambiente obrigatoria ausente: " + String.join(", ", ausentes)
                            + ". Copie o .env.exemplo para .env e preencha"
                            + " (docs/07-operacao/build-e-run.md).");
        }
    }
}
