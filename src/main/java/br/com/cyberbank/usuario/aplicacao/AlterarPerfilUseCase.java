package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Avatar;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarPerfilUseCase {

    private final UsuarioRepository usuarios;

    public AlterarPerfilUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional
    public Usuario executar(Long usuarioId, String nome, String avatar, String emailInformado) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));

        String nomeEscolhido = nome == null ? usuario.nome() : nome;
        String avatarEscolhido = avatar == null ? usuario.avatar().name() : avatar;

        Usuario.validarAlteracaoDePerfil(nomeEscolhido, avatarEscolhido, emailInformado);

        return usuarios.salvar(
                usuario.comPerfil(nomeEscolhido, Avatar.valueOf(avatarEscolhido)));
    }
}
