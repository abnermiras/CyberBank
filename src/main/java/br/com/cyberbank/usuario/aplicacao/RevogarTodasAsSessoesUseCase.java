package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevogarTodasAsSessoesUseCase {

    private final SessaoRepository sessoes;

    public RevogarTodasAsSessoesUseCase(SessaoRepository sessoes) {
        this.sessoes = sessoes;
    }

    @Transactional
    public void executar(Long usuarioId) {
        sessoes.apagarDoUsuario(usuarioId);
    }
}
