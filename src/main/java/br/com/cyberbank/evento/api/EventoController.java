package br.com.cyberbank.evento.api;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;
import br.com.cyberbank.evento.aplicacao.ListarDiarioUseCase;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.OrigemDeEvento;
import br.com.cyberbank.evento.dominio.TipoDeAlvo;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/eventos")
public class EventoController {

    public record AlvoResponse(TipoDeAlvo tipo, Long id) {
    }

    public record EventoResponse(
            Long id,
            Instant instante,
            OrigemDeEvento origem,
            Long autorId,
            @JsonInclude(JsonInclude.Include.NON_NULL) String autor,
            TipoDeEvento tipo,
            @JsonInclude(JsonInclude.Include.NON_NULL) AlvoResponse alvo,
            @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, Object> dados) {
    }

    public record DiarioResponse(LocalDate dia, List<EventoResponse> itens) {
    }

    private final ListarDiarioUseCase listarDiario;

    public EventoController(ListarDiarioUseCase listarDiario) {
        this.listarDiario = listarDiario;
    }

    @GetMapping
    public DiarioResponse listar(@PathVariable Long ambienteId,
            @RequestParam(required = false) String dia) {

        var diario = listarDiario.executar(ambienteId, interpretar(dia));

        return new DiarioResponse(diario.dia(), diario.itens().stream()
                .map(evento -> paraResposta(evento, diario.autores()))
                .toList());
    }

    private static LocalDate interpretar(String dia) {
        if (dia == null || dia.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dia);
        } catch (DateTimeParseException erro) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("dia", "FORMATO",
                    "Use AAAA-MM-DD.")));
        }
    }

    private static EventoResponse paraResposta(Evento evento, Map<Long, String> autores) {
        return new EventoResponse(evento.id(), evento.instante(), evento.origem(),
                evento.autorId(), autores.get(evento.autorId()), evento.tipo(),
                evento.alvo() == null ? null
                        : new AlvoResponse(evento.alvo().tipo(), evento.alvo().id()),
                evento.dados());
    }
}
