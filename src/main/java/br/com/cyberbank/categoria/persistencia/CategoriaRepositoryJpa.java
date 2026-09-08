package br.com.cyberbank.categoria.persistencia;

import java.util.List;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepositoryJpa implements CategoriaRepository {

    private final CategoriaJpa jpa;

    CategoriaRepositoryJpa(CategoriaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Categoria> salvarTodas(List<Categoria> categorias) {
        return jpa.saveAll(categorias.stream().map(CategoriaRepositoryJpa::paraEntidade).toList())
                .stream()
                .map(CategoriaRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Categoria> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteId(ambienteId).stream()
                .map(CategoriaRepositoryJpa::paraDominio)
                .toList();
    }

    private static CategoriaEntity paraEntidade(Categoria c) {
        return new CategoriaEntity(c.id(), c.ambienteId(), c.paiId(), c.nome(), c.sentido(),
                c.sistema(), c.operacao(), c.inativa(), c.criadaEm());
    }

    private static Categoria paraDominio(CategoriaEntity e) {
        return new Categoria(e.getId(), e.getAmbienteId(), e.getPaiId(), e.getNome(),
                e.getSentido(), e.isSistema(), e.getOperacao(), e.isInativa(), e.getCriadaEm());
    }
}
