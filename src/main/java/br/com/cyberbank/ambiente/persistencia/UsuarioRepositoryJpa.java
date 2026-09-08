package br.com.cyberbank.ambiente.persistencia;

import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.Usuario;
import br.com.cyberbank.ambiente.dominio.UsuarioRepository;

import org.springframework.stereotype.Repository;

/** A conversao JPA -> dominio e escrita a mao, e e de proposito (padroes-de-codigo.md). */
@Repository
public class UsuarioRepositoryJpa implements UsuarioRepository {

    private final UsuarioJpa jpa;

    UsuarioRepositoryJpa(UsuarioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entidade = jpa.save(new UsuarioEntity(usuario.id(), usuario.email(),
                usuario.nome(), usuario.senhaHash(), usuario.criadoEm()));
        return paraDominio(entidade);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmail(email).map(UsuarioRepositoryJpa::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(UsuarioRepositoryJpa::paraDominio);
    }

    private static Usuario paraDominio(UsuarioEntity e) {
        return new Usuario(e.getId(), e.getEmail(), e.getNome(), e.getSenhaHash(), e.getCriadoEm());
    }
}
