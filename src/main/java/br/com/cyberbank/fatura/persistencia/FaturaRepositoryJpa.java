package br.com.cyberbank.fatura.persistencia;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;

import org.springframework.stereotype.Repository;

@Repository
public class FaturaRepositoryJpa implements FaturaRepository {

    private final FaturaJpa jpa;

    FaturaRepositoryJpa(FaturaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Fatura salvar(Fatura fatura) {
        return paraDominio(jpa.save(paraEntidade(fatura)));
    }

    @Override
    public Optional<Fatura> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(FaturaRepositoryJpa::paraDominio);
    }

    @Override
    public Optional<Fatura> buscarDaCompetencia(Long contaId, YearMonth competencia) {
        return jpa.findByContaIdAndCompetencia(contaId, competencia.atDay(1))
                .map(FaturaRepositoryJpa::paraDominio);
    }

    @Override
    public Optional<Fatura> buscarAbertaDaConta(Long contaId) {
        return jpa.findByContaIdAndStatus(contaId, StatusDaFatura.ABERTA)
                .map(FaturaRepositoryJpa::paraDominio);
    }

    @Override
    public List<Fatura> listarDaConta(Long contaId) {
        return jpa.findByContaIdOrderByCompetenciaDesc(contaId).stream()
                .map(FaturaRepositoryJpa::paraDominio)
                .toList();
    }

    private static FaturaEntity paraEntidade(Fatura f) {
        return new FaturaEntity(f.id(), f.ambienteId(), f.contaId(), f.competencia().atDay(1),
                f.dataFechamento(), f.dataVencimento(), f.status(), f.criadaEm());
    }

    private static Fatura paraDominio(FaturaEntity e) {
        return new Fatura(e.getId(), e.getAmbienteId(), e.getContaId(),
                YearMonth.from(e.getCompetencia()), e.getDataFechamento(), e.getDataVencimento(),
                e.getStatus(), e.getCriadaEm());
    }
}
