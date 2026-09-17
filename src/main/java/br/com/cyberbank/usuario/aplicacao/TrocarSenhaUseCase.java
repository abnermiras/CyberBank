package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.PoliticaDeLogin;
import br.com.cyberbank.usuario.dominio.RegistroDeTentativas;
import br.com.cyberbank.usuario.dominio.Senhas;
import br.com.cyberbank.usuario.dominio.SessaoRepository;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrocarSenhaUseCase {

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final Senhas senhas;
    private final RegistroDeTentativas tentativas;
    private final Clock relogio;

    public TrocarSenhaUseCase(
            UsuarioRepository usuarios,
            SessaoRepository sessoes,
            Senhas senhas,
            RegistroDeTentativas tentativas,
            Clock relogio) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.senhas = senhas;
        this.tentativas = tentativas;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));

        Instant agora = relogio.instant();
        String chaveDaConta = PoliticaDeLogin.chaveDaConta(usuario.email());

        if (tentativas.consultar(chaveDaConta).bloqueadaEm(agora)) {
            throw new RegraDeDominioException(CodigoDeErro.MUITAS_TENTATIVAS);
        }

        if (senhaAtual == null || !senhas.confere(senhaAtual, usuario.senhaHash())) {
            tentativas.registrarFalha(chaveDaConta, agora);
            throw new RegraDeDominioException(CodigoDeErro.SENHA_ATUAL_INVALIDA);
        }

        Usuario.validarNovaSenha(novaSenha);

        if (senhas.confere(novaSenha, usuario.senhaHash())) {
            throw Usuario.senhaRepetida();
        }

        tentativas.limpar(chaveDaConta);
        usuarios.salvar(usuario.comSenhaHash(senhas.hash(novaSenha)));
        sessoes.apagarDoUsuario(usuarioId);
    }
}
