package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.aplicacao.CriarAmbientePessoalUseCase;
import br.com.cyberbank.categoria.aplicacao.CriarCategoriasDeSistemaUseCase;
import br.com.cyberbank.comum.contexto.ContextoDoBanco;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Senhas;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final CriarAmbientePessoalUseCase criarAmbientePessoal;
    private final CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema;
    private final ContextoDoBanco contextoDoBanco;
    private final Senhas senhas;
    private final Clock relogio;

    public CadastrarUsuarioUseCase(
            UsuarioRepository usuarios,
            CriarAmbientePessoalUseCase criarAmbientePessoal,
            CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema,
            ContextoDoBanco contextoDoBanco,
            Senhas senhas,
            Clock relogio) {
        this.usuarios = usuarios;
        this.criarAmbientePessoal = criarAmbientePessoal;
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

        contextoDoBanco.definirUsuario(usuario.id());

        Long ambienteId = criarAmbientePessoal.executar(usuario.id());

        contextoDoBanco.definirAmbiente(ambienteId);
        criarCategoriasDeSistema.executar(ambienteId);

        return usuario;
    }
}
