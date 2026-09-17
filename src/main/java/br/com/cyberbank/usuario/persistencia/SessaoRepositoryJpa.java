package br.com.cyberbank.usuario.persistencia;

import java.util.Optional;

import br.com.cyberbank.usuario.dominio.Sessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Repository;

@Repository
public class SessaoRepositoryJpa implements SessaoRepository {

    private final SessaoJpa jpa;

    SessaoRepositoryJpa(SessaoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Sessao salvar(Sessao sessao) {
        SessaoEntity entidade = jpa.save(new SessaoEntity(sessao.id(), sessao.usuarioId(),
                sessao.identificadorHash(), sessao.criadaEm(), sessao.ultimoUsoEm(),
                sessao.expiraEm(), sessao.origem()));
        return paraDominio(entidade);
    }

    @Override
    public Optional<Sessao> buscarPorIdentificadorHash(String identificadorHash) {
        return jpa.findByIdentificadorHash(identificadorHash).map(SessaoRepositoryJpa::paraDominio);
    }

    @Override
    public void apagar(Long sessaoId) {
        jpa.deleteById(sessaoId);
    }

    @Override
    public void apagarDoUsuario(Long usuarioId) {
        jpa.deleteByUsuarioId(usuarioId);
    }

    private static Sessao paraDominio(SessaoEntity e) {
        return new Sessao(e.getId(), e.getUsuarioId(), e.getIdentificadorHash(), e.getCriadaEm(),
                e.getUltimoUsoEm(), e.getExpiraEm(), e.getOrigem());
    }
}
