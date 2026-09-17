package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
