package br.com.cyberbank.conta.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ContaRepositoryJpa implements ContaRepository {

    private final ContaJpa jpa;

    ContaRepositoryJpa(ContaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Conta salvar(Conta conta) {
        return paraDominio(jpa.save(paraEntidade(conta)));
    }

    @Override
    public Optional<Conta> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(ContaRepositoryJpa::paraDominio);
    }

    @Override
    public List<Conta> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteIdOrderByNomeAsc(ambienteId).stream()
                .map(ContaRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    private static ContaEntity paraEntidade(Conta c) {
        return new ContaEntity(c.id(), c.ambienteId(), c.nome(), c.tipo(),
                c.entraNoFluxoDeCaixa(), c.entraEmCaixa(), c.inativa(), c.criadaEm());
    }

    private static Conta paraDominio(ContaEntity e) {
        return new Conta(e.getId(), e.getAmbienteId(), e.getNome(), e.getTipo(),
                e.isEntraNoFluxoDeCaixa(), e.isEntraEmCaixa(), e.isInativa(), e.getCriadaEm());
    }
}
