package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.usuario.dominio.Sessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarSessoesUseCase {

    public record SessaoListada(Sessao sessao, boolean atual) {
    }

    private final SessaoRepository sessoes;
    private final IdentificadoresDeSessao identificadores;
    private final Clock relogio;

    public ListarSessoesUseCase(SessaoRepository sessoes, IdentificadoresDeSessao identificadores,
            Clock relogio) {
        this.sessoes = sessoes;
        this.identificadores = identificadores;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<SessaoListada> executar(Long usuarioId, String identificadorAtual) {
        Instant agora = relogio.instant();
        String hashAtual = identificadorAtual == null ? null : identificadores.hash(identificadorAtual);

        return sessoes.listarDoUsuario(usuarioId).stream()
                .filter(sessao -> sessao.estaValida(agora))
                .map(sessao -> new SessaoListada(sessao,
                        sessao.identificadorHash().equals(hashAtual)))
                .toList();
    }
}
