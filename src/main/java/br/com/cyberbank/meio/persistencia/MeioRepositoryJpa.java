package br.com.cyberbank.meio.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.stereotype.Repository;

@Repository
public class MeioRepositoryJpa implements MeioRepository {

    private final MeioJpa jpa;

    MeioRepositoryJpa(MeioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Meio salvar(Meio meio) {
        return paraDominio(jpa.save(paraEntidade(meio)));
    }

    @Override
    public Optional<Meio> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(MeioRepositoryJpa::paraDominio);
    }

    @Override
    public List<Meio> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteIdOrderByNomeAsc(ambienteId).stream()
                .map(MeioRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Meio> listarDaConta(Long contaId, Long ambienteId) {
        return jpa.findByContaIdAndAmbienteIdOrderByNomeAsc(contaId, ambienteId).stream()
                .map(MeioRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public boolean existeNaConta(Long contaId, TipoDeMeio tipo, Long ambienteId) {
        return jpa.existsByContaIdAndTipoAndAmbienteId(contaId, tipo, ambienteId);
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    private static MeioEntity paraEntidade(Meio m) {
        return new MeioEntity(m.id(), m.ambienteId(), m.nome(), m.tipo(), m.contaId(),
                m.inativo(), m.criadoEm());
    }

    private static Meio paraDominio(MeioEntity e) {
        return new Meio(e.getId(), e.getAmbienteId(), e.getNome(), e.getTipo(), e.getContaId(),
                e.isInativo(), e.getCriadoEm());
    }
}
