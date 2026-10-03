package br.com.cyberbank.ambiente.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.ambiente.dominio.SituacaoDoConvite;

import org.springframework.stereotype.Repository;

@Repository
public class ConviteRepositoryJpa implements ConviteRepository {

    private final ConviteJpa jpa;

    ConviteRepositoryJpa(ConviteJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Convite salvar(Convite convite) {
        return paraDominio(jpa.save(new ConviteEntity(convite.id(), convite.ambienteId(),
                convite.email(), convite.papel(), convite.situacao(), convite.convidadoPor(),
                convite.criadoEm(), convite.respondidoEm())));
    }

    @Override
    public Optional<Convite> buscar(Long conviteId) {
        return jpa.findById(conviteId).map(ConviteRepositoryJpa::paraDominio);
    }

    @Override
    public Optional<Convite> buscarPendente(Long ambienteId, String email) {
        return jpa.findByAmbienteIdAndEmailAndSituacao(ambienteId, email,
                SituacaoDoConvite.PENDENTE).map(ConviteRepositoryJpa::paraDominio);
    }

    @Override
    public List<Convite> listarPendentesDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteIdAndSituacaoOrderByCriadoEmAscIdAsc(ambienteId,
                SituacaoDoConvite.PENDENTE).stream().map(ConviteRepositoryJpa::paraDominio).toList();
    }

    @Override
    public List<Convite> listarPendentesDoEmail(String email) {
        return jpa.findByEmailAndSituacaoOrderByCriadoEmAscIdAsc(email,
                SituacaoDoConvite.PENDENTE).stream().map(ConviteRepositoryJpa::paraDominio).toList();
    }

    private static Convite paraDominio(ConviteEntity e) {
        return new Convite(e.getId(), e.getAmbienteId(), e.getEmail(), e.getPapel(),
                e.getSituacao(), e.getConvidadoPor(), e.getCriadoEm(), e.getRespondidoEm());
    }
}
