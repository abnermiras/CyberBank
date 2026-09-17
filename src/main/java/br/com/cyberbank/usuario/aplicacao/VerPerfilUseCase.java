package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerPerfilUseCase {

    private final UsuarioRepository usuarios;

    public VerPerfilUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Usuario executar(Long usuarioId) {
        return usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));
    }
}
