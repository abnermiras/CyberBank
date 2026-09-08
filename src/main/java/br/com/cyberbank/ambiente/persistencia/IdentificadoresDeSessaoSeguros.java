package br.com.cyberbank.ambiente.persistencia;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import br.com.cyberbank.ambiente.dominio.IdentificadoresDeSessao;

import org.springframework.stereotype.Component;

/**
 * O identificador do cookie e OPACO: 256 bits de aleatorio, sem nada dentro (ADR-0009).
 *
 * <p>No banco fica o SHA-256 dele. Nao e hash de senha e nao precisa de Argon2id: o valor ja e
 * aleatorio de 256 bits, entao nao ha o que adivinhar por forca bruta — o que o hash resolve
 * aqui e outra coisa, que copia do banco nao vire sessao viva.
 */
@Component
public class IdentificadoresDeSessaoSeguros implements IdentificadoresDeSessao {

    private static final int TAMANHO_EM_BYTES = 32;

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String gerar() {
        byte[] bytes = new byte[TAMANHO_EM_BYTES];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String identificador) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    sha256.digest(identificador.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível na JVM", e);
        }
    }
}
