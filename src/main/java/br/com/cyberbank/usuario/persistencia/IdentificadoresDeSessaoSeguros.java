package br.com.cyberbank.usuario.persistencia;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;

import org.springframework.stereotype.Component;

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
