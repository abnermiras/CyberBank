package br.com.cyberbank.ambiente.aplicacao;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarConvitesRecebidosUseCase {

    private final ConviteRepository convites;
    private final AmbienteRepository ambientes;
    private final UsuarioRepository usuarios;

    public ListarConvitesRecebidosUseCase(ConviteRepository convites,
            AmbienteRepository ambientes, UsuarioRepository usuarios) {
        this.convites = convites;
        this.ambientes = ambientes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<ConviteRecebido> executar(Long usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));

        return convites.listarPendentesDoEmail(usuario.email()).stream()
                .map(convite -> new ConviteRecebido(convite,
                        ambientes.buscar(convite.ambienteId()).map(Ambiente::nome).orElse(""),
                        usuarios.buscarPorId(convite.convidadoPor()).map(Usuario::nome)
                                .orElse("")))
                .toList();
    }
}
