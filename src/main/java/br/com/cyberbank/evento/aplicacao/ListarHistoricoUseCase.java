package br.com.cyberbank.evento.aplicacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeAlvo;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarHistoricoUseCase {

    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;

    public ListarHistoricoUseCase(EventoRepository eventos, UsuarioRepository usuarios) {
        this.eventos = eventos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Historico executar(Long ambienteId, TipoDeAlvo tipo, Long alvoId) {
        List<Evento> doAlvo = new ArrayList<>(eventos.listarDoAlvo(ambienteId, tipo, alvoId));
        doAlvo.sort((um, outro) -> um.instante().compareTo(outro.instante()));

        return new Historico(tipo, alvoId, doAlvo, nomesDosAutores(doAlvo));
    }

    private Map<Long, String> nomesDosAutores(List<Evento> doAlvo) {
        Map<Long, String> nomes = new HashMap<>();
        doAlvo.stream().map(Evento::autorId).distinct().forEach(autorId ->
                usuarios.buscarPorId(autorId).map(Usuario::nome)
                        .ifPresent(nome -> nomes.put(autorId, nome)));
        return nomes;
    }

    public record Historico(TipoDeAlvo tipo, Long alvoId, List<Evento> itens,
                            Map<Long, String> autores) {
    }
}
