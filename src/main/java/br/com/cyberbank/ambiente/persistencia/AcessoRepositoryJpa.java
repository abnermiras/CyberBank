package br.com.cyberbank.ambiente.persistencia;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Papel;

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

    @Override
    public List<Acesso> listarDoAmbiente(Long ambienteId) {
        return jpa.listarMembros(ambienteId).stream()
                .map(linha -> new Acesso(
                        ((Number) linha[0]).longValue(),
                        ((Number) linha[1]).longValue(),
                        ambienteId,
                        Papel.valueOf((String) linha[2]),
                        instante(linha[3])))
                .toList();
    }

    @Override
    public boolean remover(Acesso acesso) {
        return jpa.removerAcesso(acesso.ambienteId(), acesso.usuarioId()) > 0;
    }

    private static Instant instante(Object valor) {
        return switch (valor) {
            case Instant instante -> instante;
            case OffsetDateTime dataHora -> dataHora.toInstant();
            case Timestamp carimbo -> carimbo.toInstant();
            default -> throw new IllegalStateException(valor.getClass().getName());
        };
    }
}
