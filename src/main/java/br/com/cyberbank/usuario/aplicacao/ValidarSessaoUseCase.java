package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.usuario.dominio.Sessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidarSessaoUseCase {

    private final SessaoRepository sessoes;
    private final IdentificadoresDeSessao identificadores;
    private final Clock relogio;

    public ValidarSessaoUseCase(SessaoRepository sessoes, IdentificadoresDeSessao identificadores,
            Clock relogio) {
        this.sessoes = sessoes;
        this.identificadores = identificadores;
        this.relogio = relogio;
    }

    @Transactional
    public Optional<Long> executar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        Instant agora = relogio.instant();

        return sessoes.buscarPorIdentificadorHash(identificadores.hash(identificador))
                .flatMap(sessao -> {
                    if (!sessao.estaValida(agora)) {

                        sessoes.apagar(sessao.id());
                        return Optional.empty();
                    }
                    return Optional.of(renovar(sessao, agora));
                });
    }

    private Long renovar(Sessao sessao, Instant agora) {
        sessoes.salvar(sessao.usadaEm(agora));
        return sessao.usuarioId();
    }
}
