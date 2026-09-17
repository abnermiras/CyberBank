package br.com.cyberbank.categoria.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;

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
    public Categoria salvar(Categoria categoria) {
        return paraDominio(jpa.save(paraEntidade(categoria)));
    }

    @Override
    public Optional<Categoria> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(CategoriaRepositoryJpa::paraDominio);
    }

    @Override
    public Optional<Categoria> buscarDeSistema(Long ambienteId, OperacaoDeSistema operacao,
            Sentido sentido) {
        return jpa.findByAmbienteIdAndOperacaoAndSentido(ambienteId, operacao, sentido)
                .map(CategoriaRepositoryJpa::paraDominio);
    }

    @Override
    public List<Categoria> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteId(ambienteId).stream()
                .map(CategoriaRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    @Override
    public boolean temFilhas(Long id, Long ambienteId) {
        return jpa.existsByPaiIdAndAmbienteId(id, ambienteId);
    }

    private static CategoriaEntity paraEntidade(Categoria c) {
        return new CategoriaEntity(c.id(), c.ambienteId(), c.paiId(), c.nome(), c.sentido(),
                c.cor(), c.sistema(), c.operacao(), c.inativa(), c.criadaEm());
    }

    private static Categoria paraDominio(CategoriaEntity e) {
        return new Categoria(e.getId(), e.getAmbienteId(), e.getPaiId(), e.getNome(),
                e.getSentido(), e.getCor(), e.isSistema(), e.getOperacao(), e.isInativa(),
                e.getCriadaEm());
    }
}
