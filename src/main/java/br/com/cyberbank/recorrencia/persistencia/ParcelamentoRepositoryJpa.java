package br.com.cyberbank.recorrencia.persistencia;

import java.util.Optional;

import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ParcelamentoRepositoryJpa implements ParcelamentoRepository {

    private final ParcelamentoJpa jpa;

    ParcelamentoRepositoryJpa(ParcelamentoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Parcelamento salvar(Parcelamento parcelamento) {
        return paraDominio(jpa.saveAndFlush(paraEntidade(parcelamento)));
    }

    @Override
    public Optional<Parcelamento> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId)
                .map(ParcelamentoRepositoryJpa::paraDominio);
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    private static ParcelamentoEntity paraEntidade(Parcelamento p) {
        return new ParcelamentoEntity(p.id(), p.ambienteId(), p.contaId(), p.meioId(),
                p.categoriaId(), p.valorDaCompraCentavos(), (short) p.parcelas(),
                p.dataDaCompra(), p.descricao(), p.criadoEm());
    }

    private static Parcelamento paraDominio(ParcelamentoEntity e) {
        return new Parcelamento(e.getId(), e.getAmbienteId(), e.getContaId(), e.getMeioId(),
                e.getCategoriaId(), e.getValorDaCompraCentavos(), e.getParcelas().intValue(),
                e.getDataDaCompra(), e.getDescricao(), e.getCriadoEm());
    }
}
