package br.com.cyberbank.evento.persistencia;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.evento.dominio.TipoDeAlvo;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventoJpa extends JpaRepository<EventoEntity, Long> {

    List<EventoEntity> findByAmbienteIdAndDiaOrderByInstanteDescIdDesc(Long ambienteId, LocalDate dia);

    List<EventoEntity> findByAmbienteIdAndAlvoTipoAndAlvoIdOrderByInstanteDescIdDesc(
            Long ambienteId, TipoDeAlvo alvoTipo, Long alvoId);
}
