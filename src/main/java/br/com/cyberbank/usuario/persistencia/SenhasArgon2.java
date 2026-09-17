package br.com.cyberbank.usuario.persistencia;

import java.security.SecureRandom;
import java.util.Base64;

import br.com.cyberbank.usuario.dominio.Senhas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SenhasArgon2 implements Senhas {

    private static final int TAMANHO_DO_SAL = 16;
    private static final int TAMANHO_DO_HASH = 32;

    private final Argon2PasswordEncoder codificador;

    private final String hashDeMentira;

    public SenhasArgon2(
            @Value("${cyberbank.argon2.memoria}") int memoria,
            @Value("${cyberbank.argon2.iteracoes}") int iteracoes,
            @Value("${cyberbank.argon2.paralelismo}") int paralelismo) {
        this.codificador = new Argon2PasswordEncoder(
                TAMANHO_DO_SAL, TAMANHO_DO_HASH, paralelismo, memoria, iteracoes);

        byte[] aleatorio = new byte[32];
        new SecureRandom().nextBytes(aleatorio);
        this.hashDeMentira = codificador.encode(Base64.getEncoder().encodeToString(aleatorio));
    }

    @Override
    public String hash(String senha) {
        return codificador.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return codificador.matches(senha, hash);
    }

    @Override
    public boolean conferirEmVao(String senha) {
        codificador.matches(senha, hashDeMentira);
        return false;
    }
}
