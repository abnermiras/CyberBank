package br.com.cyberbank.ambiente.persistencia;

import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;

import org.springframework.stereotype.Repository;

@Repository
public class AcessoRepositoryJpa implements AcessoRepository {

    private final AcessoJpa jpa;

    AcessoRepositoryJpa(AcessoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Acesso salvar(Acesso acesso) {
        AcessoEntity entidade = jpa.save(new AcessoEntity(acesso.id(), acesso.usuarioId(),
                acesso.ambienteId(), acesso.papel(), acesso.criadoEm()));
        return new Acesso(entidade.getId(), entidade.getUsuarioId(), entidade.getAmbienteId(),
                entidade.getPapel(), entidade.getCriadoEm());
    }

    @Override
    public Optional<Acesso> buscar(Long usuarioId, Long ambienteId) {
        return jpa.findByUsuarioIdAndAmbienteId(usuarioId, ambienteId)
                .map(e -> new Acesso(e.getId(), e.getUsuarioId(), e.getAmbienteId(), e.getPapel(),
                        e.getCriadoEm()));
    }
}
