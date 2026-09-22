package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.usuario.dominio.Sessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevogarSessaoUseCase {

    private final SessaoRepository sessoes;
    private final IdentificadoresDeSessao identificadores;

    public RevogarSessaoUseCase(SessaoRepository sessoes, IdentificadoresDeSessao identificadores) {
        this.sessoes = sessoes;
        this.identificadores = identificadores;
    }

    @Transactional
    public boolean executar(Long usuarioId, Long sessaoId, String identificadorAtual) {
        Sessao sessao = sessoes.buscar(sessaoId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        sessao.exigirDo(usuarioId);

        sessoes.apagar(sessao.id());
        return identificadorAtual != null
                && sessao.identificadorHash().equals(identificadores.hash(identificadorAtual));
    }
}
