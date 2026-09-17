package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.usuario.dominio.PoliticaDeLogin;
import br.com.cyberbank.usuario.dominio.RegistroDeTentativas;
import br.com.cyberbank.usuario.dominio.Senhas;
import br.com.cyberbank.usuario.dominio.Sessao;
import br.com.cyberbank.usuario.dominio.SessaoRepository;
import br.com.cyberbank.usuario.dominio.Tentativas;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticarUsuarioUseCase {

    public record SessaoAberta(String identificador, Instant expiraEm) {
    }

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final Senhas senhas;
    private final IdentificadoresDeSessao identificadores;
    private final RegistroDeTentativas tentativas;
    private final Clock relogio;

    public AutenticarUsuarioUseCase(
            UsuarioRepository usuarios,
            SessaoRepository sessoes,
            Senhas senhas,
            IdentificadoresDeSessao identificadores,
            RegistroDeTentativas tentativas,
            Clock relogio) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.senhas = senhas;
        this.identificadores = identificadores;
        this.tentativas = tentativas;
        this.relogio = relogio;
    }

    @Transactional
    public SessaoAberta executar(String email, String senha, String origem) {
        Instant agora = relogio.instant();
        String chaveDaConta = PoliticaDeLogin.chaveDaConta(Usuario.normalizarEmail(email));
        String chaveDaOrigem = PoliticaDeLogin.chaveDaOrigem(origem);

        recusarSeBloqueado(chaveDaConta, agora);
        recusarSeBloqueado(chaveDaOrigem, agora);
        atrasar(chaveDaConta, chaveDaOrigem);

        Optional<Usuario> encontrado = usuarios.buscarPorEmail(Usuario.normalizarEmail(email));

        boolean confere = encontrado
                .map(usuario -> senhas.confere(senha, usuario.senhaHash()))
                .orElseGet(() -> senhas.conferirEmVao(senha));

        if (!confere) {
            tentativas.registrarFalha(chaveDaConta, agora);
            tentativas.registrarFalha(chaveDaOrigem, agora);
            throw new RegraDeDominioException(CodigoDeErro.CREDENCIAIS_INVALIDAS);
        }

        tentativas.limpar(chaveDaConta);
        tentativas.limpar(chaveDaOrigem);

        String identificador = identificadores.gerar();
        Sessao sessao = sessoes.salvar(Sessao.abrir(
                encontrado.orElseThrow().id(), identificadores.hash(identificador), origem, agora));

        return new SessaoAberta(identificador, sessao.expiraEm());
    }

    private void recusarSeBloqueado(String chave, Instant agora) {
        if (tentativas.consultar(chave).bloqueadaEm(agora)) {
            throw new RegraDeDominioException(CodigoDeErro.MUITAS_TENTATIVAS);
        }
    }

    private void atrasar(String chaveDaConta, String chaveDaOrigem) {
        Tentativas daConta = tentativas.consultar(chaveDaConta);
        Tentativas daOrigem = tentativas.consultar(chaveDaOrigem);
        Duration atraso = PoliticaDeLogin.atrasoApos(Math.max(daConta.falhas(), daOrigem.falhas()));
        if (atraso.isZero()) {
            return;
        }
        try {
            Thread.sleep(atraso.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
