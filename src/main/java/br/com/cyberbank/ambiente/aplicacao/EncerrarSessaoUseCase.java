package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.ambiente.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sair e apagar a linha, e revogar tem efeito no clique seguinte (ADR-0009).
 *
 * <p>IDEMPOTENTE de proposito: sem cookie, com cookie invalido ou com a sessao ja apagada, o
 * resultado e o mesmo. Sair nao e operacao que possa falhar — e um erro aqui contaria ao
 * cliente que aquele identificador nao vale mais.
 */
@Service
public class EncerrarSessaoUseCase {

    private final SessaoRepository sessoes;
    private final IdentificadoresDeSessao identificadores;

    public EncerrarSessaoUseCase(SessaoRepository sessoes, IdentificadoresDeSessao identificadores) {
        this.sessoes = sessoes;
        this.identificadores = identificadores;
    }

    @Transactional
    public void executar(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return;
        }
        sessoes.buscarPorIdentificadorHash(identificadores.hash(identificador))
                .ifPresent(sessao -> sessoes.apagar(sessao.id()));
    }
}
