package br.com.cyberbank.evento.aplicacao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.ambiente.dominio.Usuario;
import br.com.cyberbank.ambiente.dominio.UsuarioRepository;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.DiaDoDiario;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarDiarioUseCase {

    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final DiaLocal diaLocal;

    public ListarDiarioUseCase(EventoRepository eventos, UsuarioRepository usuarios,
            DiaLocal diaLocal) {
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public Diario executar(Long ambienteId, LocalDate diaPedido) {
        LocalDate dia = DiaDoDiario.de(diaPedido, diaLocal.hoje()).dia();
        List<Evento> doDia = eventos.listarDoDia(ambienteId, dia);

        return new Diario(dia, doDia, nomesDosAutores(doDia));
    }

    private Map<Long, String> nomesDosAutores(List<Evento> doDia) {
        Map<Long, String> nomes = new HashMap<>();
        doDia.stream().map(Evento::autorId).distinct().forEach(autorId ->
                usuarios.buscarPorId(autorId).map(Usuario::nome)
                        .ifPresent(nome -> nomes.put(autorId, nome)));
        return nomes;
    }

    public record Diario(LocalDate dia, List<Evento> itens, Map<Long, String> autores) {
    }
}
