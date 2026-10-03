package br.com.cyberbank.ambiente.aplicacao;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarMembrosUseCase {

    private final AcessoRepository acessos;
    private final UsuarioRepository usuarios;

    public ListarMembrosUseCase(AcessoRepository acessos, UsuarioRepository usuarios) {
        this.acessos = acessos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<Membro> executar(Long ambienteId) {
        return acessos.listarDoAmbiente(ambienteId).stream()
                .flatMap(acesso -> usuarios.buscarPorId(acesso.usuarioId()).stream()
                        .map(usuario -> new Membro(acesso, usuario.nome(), usuario.email(),
                                usuario.avatar())))
                .toList();
    }
}
