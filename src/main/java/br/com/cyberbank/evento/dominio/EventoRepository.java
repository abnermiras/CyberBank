package br.com.cyberbank.evento.dominio;

import java.time.LocalDate;
import java.util.List;

public interface EventoRepository {

    Evento registrar(Evento evento);

    List<Evento> registrarTodos(List<Evento> eventos);

    List<Evento> listarDoDia(Long ambienteId, LocalDate dia);

    List<Evento> listarDoAlvo(Long ambienteId, TipoDeAlvo tipo, Long alvoId);
}
