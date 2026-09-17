package br.com.cyberbank.evento.persistencia;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeAlvo;

import org.springframework.stereotype.Repository;

@Repository
public class EventoRepositoryJpa implements EventoRepository {

    private final EventoJpa jpa;

    EventoRepositoryJpa(EventoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Evento registrar(Evento evento) {
        return paraDominio(jpa.save(paraEntidade(evento)));
    }

    @Override
    public List<Evento> registrarTodos(List<Evento> eventos) {
        return jpa.saveAll(eventos.stream().map(EventoRepositoryJpa::paraEntidade).toList())
                .stream()
                .map(EventoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Evento> listarDoDia(Long ambienteId, LocalDate dia) {
        return jpa.findByAmbienteIdAndDiaOrderByInstanteDescIdDesc(ambienteId, dia).stream()
                .map(EventoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Evento> listarDoAlvo(Long ambienteId, TipoDeAlvo tipo, Long alvoId) {
        return jpa.findByAmbienteIdAndAlvoTipoAndAlvoIdOrderByInstanteDescIdDesc(
                        ambienteId, tipo, alvoId).stream()
                .map(EventoRepositoryJpa::paraDominio)
                .toList();
    }

    private static EventoEntity paraEntidade(Evento e) {
        return new EventoEntity(e.id(), e.ambienteId(), e.dia(), e.instante(), e.origem(),
                e.autorId(), e.tipo(),
                e.alvo() == null ? null : e.alvo().tipo(),
                e.alvo() == null ? null : e.alvo().id(),
                e.dados());
    }

    private static Evento paraDominio(EventoEntity e) {
        return new Evento(e.getId(), e.getAmbienteId(), e.getDia(), e.getInstante(),
                e.getOrigem(), e.getAutorId(), e.getTipo(),
                e.getAlvoTipo() == null ? null : new Alvo(e.getAlvoTipo(), e.getAlvoId()),
                e.getDados());
    }
}
