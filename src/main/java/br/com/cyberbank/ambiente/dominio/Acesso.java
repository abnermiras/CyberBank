package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

public record Acesso(Long id, Long usuarioId, Long ambienteId, Papel papel, Instant criadoEm) {

    public static Acesso donoDe(Long usuarioId, Long ambienteId, Instant agora) {
        return new Acesso(null, usuarioId, ambienteId, Papel.DONO, agora);
    }

    public void exigirRemovivelPor(Acesso quemRemove) {
        if (papel == Papel.DONO) {
            throw new RegraDeDominioException(CodigoDeErro.DONO_NAO_SAI);
        }
        boolean saindo = quemRemove.usuarioId().equals(usuarioId);
        if (!saindo && !quemRemove.papel().podeRemoverPessoas()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
    }

    public boolean removidoPorSiMesmo(Long quemRemoveId) {
        return usuarioId.equals(quemRemoveId);
    }
}
