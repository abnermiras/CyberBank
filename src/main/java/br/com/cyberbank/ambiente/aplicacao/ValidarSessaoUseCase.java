package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.ambiente.dominio.Sessao;
import br.com.cyberbank.ambiente.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uma leitura no banco por requisicao autenticada: e o preco do ADR-0009, e o que compra e
 * revogacao com efeito NO CLIQUE SEGUINTE.
 */
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

    /** O id do usuario, se a sessao existe e vale. Vazio em qualquer outro caso. */
    @Transactional
    public Optional<Long> executar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        Instant agora = relogio.instant();

        return sessoes.buscarPorIdentificadorHash(identificadores.hash(identificador))
                .flatMap(sessao -> {
                    if (!sessao.estaValida(agora)) {
                        // Sessao vencida nao fica ocupando lugar: quem a apresenta a apaga.
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
