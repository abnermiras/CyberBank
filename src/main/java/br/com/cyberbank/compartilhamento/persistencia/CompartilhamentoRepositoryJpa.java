package br.com.cyberbank.compartilhamento.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.compartilhamento.dominio.Compartilhamento;
import br.com.cyberbank.compartilhamento.dominio.CompartilhamentoRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CompartilhamentoRepositoryJpa implements CompartilhamentoRepository {

    private static final String OBJETO_CONTA = "CONTA";

    private final VinculoJpa jpa;

    CompartilhamentoRepositoryJpa(VinculoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Compartilhamento salvar(Compartilhamento c) {
        return paraDominio(jpa.saveAndFlush(new VinculoEntity(c.id(), OBJETO_CONTA,
                c.contaId(), c.ambienteOrigemId(), c.ambienteDestinoId(), c.criadoPor(),
                c.criadoEm())));
    }

    @Override
    public Optional<Compartilhamento> buscar(Long contaId, Long ambienteDestinoId) {
        return jpa.findByContaIdAndAmbienteDestinoId(contaId, ambienteDestinoId)
                .map(CompartilhamentoRepositoryJpa::paraDominio);
    }

    @Override
    public List<Compartilhamento> listarDaConta(Long contaId, Long ambienteOrigemId) {
        return jpa.findByContaIdAndAmbienteOrigemIdOrderByIdAsc(contaId, ambienteOrigemId)
                .stream()
                .map(CompartilhamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Compartilhamento> listarRecebidos(Long ambienteDestinoId) {
        return jpa.findByAmbienteDestinoIdOrderByIdAsc(ambienteDestinoId).stream()
                .map(CompartilhamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public void revogar(Long contaId, Long ambienteDestinoId) {
        jpa.deleteByContaIdAndAmbienteDestinoId(contaId, ambienteDestinoId);
    }

    private static Compartilhamento paraDominio(VinculoEntity e) {
        return new Compartilhamento(e.getId(), e.getContaId(), e.getAmbienteOrigemId(),
                e.getAmbienteDestinoId(), e.getCriadoPor(), e.getCriadoEm());
    }
}
