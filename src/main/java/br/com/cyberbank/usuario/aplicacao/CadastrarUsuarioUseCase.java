package br.com.cyberbank.usuario.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.random.RandomGenerator;

import br.com.cyberbank.ambiente.aplicacao.CriarAmbienteUseCase;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.comum.contexto.ContextoDoBanco;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Avatar;
import br.com.cyberbank.usuario.dominio.Senhas;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final CriarAmbienteUseCase criarAmbiente;
    private final ContextoDoBanco contextoDoBanco;
    private final Senhas senhas;
    private final RandomGenerator sorteio;
    private final Clock relogio;

    public CadastrarUsuarioUseCase(
            UsuarioRepository usuarios,
            CriarAmbienteUseCase criarAmbiente,
            ContextoDoBanco contextoDoBanco,
            Senhas senhas,
            RandomGenerator sorteio,
            Clock relogio) {
        this.usuarios = usuarios;
        this.criarAmbiente = criarAmbiente;
        this.contextoDoBanco = contextoDoBanco;
        this.senhas = senhas;
        this.sorteio = sorteio;
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
        Usuario usuario = usuarios.salvar(Usuario.cadastrar(
                nome, email, senhas.hash(senha), Avatar.sortear(sorteio), agora));

        contextoDoBanco.definirUsuario(usuario.id());

        criarAmbiente.executar(usuario.id(), Ambiente.NOME_PADRAO);

        return usuario;
    }
}
