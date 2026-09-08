package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;

/** O par (usuario, ambiente) mais um papel. Um usuario tem no maximo um acesso por ambiente. */
public record Acesso(Long id, Long usuarioId, Long ambienteId, Papel papel, Instant criadoEm) {

    public static Acesso donoDe(Long usuarioId, Long ambienteId, Instant agora) {
        return new Acesso(null, usuarioId, ambienteId, Papel.DONO, agora);
    }
}
