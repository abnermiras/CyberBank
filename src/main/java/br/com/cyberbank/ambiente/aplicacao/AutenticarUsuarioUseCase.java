package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.IdentificadoresDeSessao;
import br.com.cyberbank.ambiente.dominio.PoliticaDeLogin;
import br.com.cyberbank.ambiente.dominio.RegistroDeTentativas;
import br.com.cyberbank.ambiente.dominio.Senhas;
import br.com.cyberbank.ambiente.dominio.Sessao;
import br.com.cyberbank.ambiente.dominio.SessaoRepository;
import br.com.cyberbank.ambiente.dominio.Tentativas;
import br.com.cyberbank.ambiente.dominio.Usuario;
import br.com.cyberbank.ambiente.dominio.UsuarioRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * As quatro regras do login (docs/01-arquitetura/seguranca.md), todas aqui:
 *
 * <ol>
 *   <li><b>A resposta e sempre a mesma.</b> E-mail inexistente e senha errada dao o mesmo
 *       codigo, a mesma mensagem — e o MESMO TEMPO, porque sem usuario o Argon2id roda do
 *       mesmo jeito, contra um hash de mentira.</li>
 *   <li><b>Atraso progressivo</b>, contado por conta E por origem.</li>
 *   <li><b>Bloqueio temporario</b> depois de N falhas, com desbloqueio pelo tempo.</li>
 *   <li>A recuperacao de senha segue as mesmas quatro — e entra com o ADR-0007.</li>
 * </ol>
 */
@Service
public class AutenticarUsuarioUseCase {

    /** O identificador opaco, que so o cliente ve. No banco fica o hash dele. */
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
        String chaveDaConta = "conta:" + Usuario.normalizarEmail(email);
        String chaveDaOrigem = "origem:" + origem;

        recusarSeBloqueado(chaveDaConta, agora);
        recusarSeBloqueado(chaveDaOrigem, agora);
        atrasar(chaveDaConta, chaveDaOrigem);

        Optional<Usuario> encontrado = usuarios.buscarPorEmail(Usuario.normalizarEmail(email));

        // O tempo tem que ser o mesmo dos dois lados: sem usuario, gasta-se o mesmo Argon2id.
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

    /** O atraso e do par (conta, origem): vale o maior dos dois. */
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
