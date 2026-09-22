package br.com.cyberbank.ambiente.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteDaRotina;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.ambiente.dominio.Papel;

import org.springframework.stereotype.Repository;

@Repository
public class AmbienteRepositoryJpa implements AmbienteRepository {

    private final AmbienteJpa jpa;

    AmbienteRepositoryJpa(AmbienteJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Ambiente salvar(Ambiente ambiente) {
        AmbienteEntity entidade = jpa.save(new AmbienteEntity(ambiente.id(), ambiente.nome(),
                ambiente.criadoPor(), ambiente.criadoEm()));
        return paraDominio(entidade);
    }

    @Override
    public Optional<Ambiente> buscar(Long ambienteId) {
        return jpa.findById(ambienteId).map(AmbienteRepositoryJpa::paraDominio);
    }

    @Override
    public List<AcessoAoAmbiente> listarDoUsuario(Long usuarioId) {
        return jpa.listarComPapel(usuarioId).stream()
                .map(linha -> new AcessoAoAmbiente(
                        paraDominio((AmbienteEntity) linha[0]), (Papel) linha[1]))
                .toList();
    }

    @Override
    public List<AmbienteDaRotina> listarParaRotina() {
        return jpa.listarParaRotina().stream()
                .map(linha -> new AmbienteDaRotina(
                        ((Number) linha[0]).longValue(), ((Number) linha[1]).longValue()))
                .toList();
    }

    private static Ambiente paraDominio(AmbienteEntity e) {
        return new Ambiente(e.getId(), e.getNome(), e.getCriadoPor(), e.getCriadoEm());
    }
}
