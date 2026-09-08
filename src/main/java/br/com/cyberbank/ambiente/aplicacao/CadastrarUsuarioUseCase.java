package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.ambiente.dominio.Senhas;
import br.com.cyberbank.ambiente.dominio.Usuario;
import br.com.cyberbank.ambiente.dominio.UsuarioRepository;
import br.com.cyberbank.categoria.aplicacao.CriarCategoriasDeSistemaUseCase;
import br.com.cyberbank.comum.contexto.ContextoDoBanco;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UM ATO, TRES EFEITOS (docs/02-dominio/ambiente-financeiro.md): cria o usuario, cria o
 * "Ambiente Pessoal" do qual ele e dono, e cria nele o jogo completo de categorias de sistema.
 *
 * <p>Os tres na MESMA transacao: usuario sem ambiente, ou ambiente sem as categorias, e estado
 * que nenhuma tela sabe mostrar — e que nenhum caminho do sistema sabe consertar depois.
 *
 * <p>E aqui que dois assuntos se encontram, e por isso o encontro e na aplicacao: o dominio de
 * `ambiente` nao conhece o de `categoria`, e nunca vai conhecer (ADR-0010).
 */
@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final AmbienteRepository ambientes;
    private final AcessoRepository acessos;
    private final CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema;
    private final ContextoDoBanco contextoDoBanco;
    private final Senhas senhas;
    private final Clock relogio;

    public CadastrarUsuarioUseCase(
            UsuarioRepository usuarios,
            AmbienteRepository ambientes,
            AcessoRepository acessos,
            CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema,
            ContextoDoBanco contextoDoBanco,
            Senhas senhas,
            Clock relogio) {
        this.usuarios = usuarios;
        this.ambientes = ambientes;
        this.acessos = acessos;
        this.criarCategoriasDeSistema = criarCategoriasDeSistema;
        this.contextoDoBanco = contextoDoBanco;
        this.senhas = senhas;
        this.relogio = relogio;
    }

    @Transactional
    public Usuario executar(String nome, String email, String senha) {
        Usuario.validarCadastro(nome, email, senha);

        String emailNormalizado = Usuario.normalizarEmail(email);
        if (usuarios.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new RegraDeDominioException(CodigoDeErro.EMAIL_JA_CADASTRADO);
        }

        Instant agora = relogio.instant();
        Usuario usuario = usuarios.salvar(Usuario.cadastrar(nome, email, senhas.hash(senha), agora));

        // O usuario acabou de nascer: a transacao abriu sem contexto nenhum, e sem isto o
        // ambiente que ele vai criar seria invisivel para ele mesmo (ADR-0002).
        contextoDoBanco.definirUsuario(usuario.id());

        Ambiente ambiente = ambientes.salvar(Ambiente.pessoalDe(usuario.id(), agora));
        acessos.salvar(Acesso.donoDe(usuario.id(), ambiente.id(), agora));

        contextoDoBanco.definirAmbiente(ambiente.id());
        criarCategoriasDeSistema.executar(ambiente.id());

        return usuario;
    }
}
