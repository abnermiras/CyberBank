package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecusarConviteUseCase {

    private final ConviteRepository convites;
    private final UsuarioRepository usuarios;
    private final Clock relogio;

    public RecusarConviteUseCase(ConviteRepository convites, UsuarioRepository usuarios,
            Clock relogio) {
        this.convites = convites;
        this.usuarios = usuarios;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, Long conviteId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));
        Convite convite = convites.buscar(conviteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        convites.salvar(convite.recusadoPor(usuario.email(), relogio.instant()));
    }
}
