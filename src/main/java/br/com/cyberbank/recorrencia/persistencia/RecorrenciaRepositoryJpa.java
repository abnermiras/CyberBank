package br.com.cyberbank.recorrencia.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.recorrencia.dominio.Recorrencia;
import br.com.cyberbank.recorrencia.dominio.RecorrenciaRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RecorrenciaRepositoryJpa implements RecorrenciaRepository {

    private final RecorrenciaJpa jpa;

    RecorrenciaRepositoryJpa(RecorrenciaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Recorrencia salvar(Recorrencia recorrencia) {
        return paraDominio(jpa.saveAndFlush(paraEntidade(recorrencia)));
    }

    @Override
    public Optional<Recorrencia> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId)
                .map(RecorrenciaRepositoryJpa::paraDominio);
    }

    @Override
    public List<Recorrencia> listarAtivasDaConta(Long contaId) {
        return jpa.findByContaIdAndAtivaTrueOrderByIdAsc(contaId).stream()
                .map(RecorrenciaRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Recorrencia> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteIdOrderByAtivaDescIdDesc(ambienteId).stream()
                .map(RecorrenciaRepositoryJpa::paraDominio)
                .toList();
    }

    private static RecorrenciaEntity paraEntidade(Recorrencia r) {
        return new RecorrenciaEntity(r.id(), r.ambienteId(), r.contaId(), r.meioId(),
                r.categoriaId(), r.valorCentavos(), r.periodicidade(), (short) r.dia(),
                r.inicio(), r.ativa(), r.descricao(), r.criadoEm());
    }

    private static Recorrencia paraDominio(RecorrenciaEntity e) {
        return new Recorrencia(e.getId(), e.getAmbienteId(), e.getContaId(), e.getMeioId(),
                e.getCategoriaId(), e.getValorCentavos(), e.getPeriodicidade(),
                e.getDia().intValue(), e.getInicio(), e.isAtiva(), e.getDescricao(),
                e.getCriadoEm());
    }
}
