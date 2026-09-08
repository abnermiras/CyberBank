package br.com.cyberbank.ambiente.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.junit.jupiter.api.Test;

/**
 * A regra do TEMPO IGUAL nao se prova com cronometro — medicao de milissegundo oscila com GC,
 * com carga da maquina e com o atraso progressivo. O que se prova aqui e o MECANISMO: exista
 * ou nao o usuario, o Argon2id roda uma vez, e a falha e contada nas duas chaves dos dois
 * jeitos. Sem isso, o caminho "nao achei o usuario" volta na hora e entrega a lista de
 * usuarios (docs/01-arquitetura/seguranca.md).
 */
class AutenticarUsuarioUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-09-07T12:00:00Z");
    private static final String ORIGEM = "10.0.0.7";

    private final List<String> trabalhoDeSenha = new ArrayList<>();
    private final Map<String, Tentativas> tentativas = new HashMap<>();

    private Usuario ana;

    private AutenticarUsuarioUseCase useCase(Usuario cadastrado) {
        this.ana = cadastrado;
        return new AutenticarUsuarioUseCase(usuarios(), sessoes(), senhas(), identificadores(),
                registro(), Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void usuario_inexistente_gasta_o_mesmo_argon2_de_uma_conferencia_de_verdade() {
        var autenticar = useCase(null);

        assertThatThrownBy(() -> autenticar.executar("ninguem@exemplo.com", "senha", ORIGEM))
                .isInstanceOf(RegraDeDominioException.class)
                .satisfies(e -> assertThat(((RegraDeDominioException) e).codigo())
                        .isEqualTo(CodigoDeErro.CREDENCIAIS_INVALIDAS));

        assertThat(trabalhoDeSenha)
                .as("o Argon2id roda mesmo sem usuario para conferir")
                .containsExactly("em-vao");
    }

    @Test
    void senha_errada_de_usuario_existente_da_o_mesmo_codigo() {
        var autenticar = useCase(new Usuario(1L, "ana@exemplo.com", "Ana", "hash-certo", AGORA));

        assertThatThrownBy(() -> autenticar.executar("ana@exemplo.com", "errada", ORIGEM))
                .isInstanceOf(RegraDeDominioException.class)
                .satisfies(e -> assertThat(((RegraDeDominioException) e).codigo())
                        .isEqualTo(CodigoDeErro.CREDENCIAIS_INVALIDAS));

        assertThat(trabalhoDeSenha).containsExactly("confere");
    }

    @Test
    void a_falha_e_contada_por_conta_e_por_origem_inclusive_para_email_inexistente() {
        var autenticar = useCase(null);

        assertThatThrownBy(() -> autenticar.executar("ninguem@exemplo.com", "senha", ORIGEM))
                .isInstanceOf(RegraDeDominioException.class);

        assertThat(tentativas.keySet())
                .as("contar so o e-mail que existe seria o proprio oraculo")
                .containsExactlyInAnyOrder("conta:ninguem@exemplo.com", "origem:" + ORIGEM);
    }

    @Test
    void conta_bloqueada_nao_chega_a_conferir_senha_nenhuma() {
        var autenticar = useCase(new Usuario(1L, "ana@exemplo.com", "Ana", "hash-certo", AGORA));
        tentativas.put("conta:ana@exemplo.com",
                new Tentativas(PoliticaDeLogin.FALHAS_ATE_BLOQUEIO, AGORA));

        assertThatThrownBy(() -> autenticar.executar("ana@exemplo.com", "certa", ORIGEM))
                .isInstanceOf(RegraDeDominioException.class)
                .satisfies(e -> assertThat(((RegraDeDominioException) e).codigo())
                        .isEqualTo(CodigoDeErro.MUITAS_TENTATIVAS));

        assertThat(trabalhoDeSenha).isEmpty();
    }

    @Test
    void login_certo_abre_sessao_e_limpa_as_duas_contagens() {
        var autenticar = useCase(new Usuario(1L, "ana@exemplo.com", "Ana", "hash-certo", AGORA));
        tentativas.put("conta:ana@exemplo.com", new Tentativas(1, AGORA));
        tentativas.put("origem:" + ORIGEM, new Tentativas(1, AGORA));

        var sessao = autenticar.executar("Ana@Exemplo.com", "certa", ORIGEM);

        assertThat(sessao.identificador()).isEqualTo("opaco");
        assertThat(sessao.expiraEm()).isEqualTo(AGORA.plus(Sessao.DURACAO_ABSOLUTA));
        assertThat(tentativas).isEmpty();
    }

    // --- dublês, escritos a mão: o caso de uso não conhece Spring nem banco ---

    private UsuarioRepository usuarios() {
        return new UsuarioRepository() {
            @Override
            public Usuario salvar(Usuario usuario) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<Usuario> buscarPorEmail(String email) {
                return Optional.ofNullable(ana).filter(u -> u.email().equals(email));
            }

            @Override
            public Optional<Usuario> buscarPorId(Long id) {
                return Optional.ofNullable(ana);
            }
        };
    }

    private SessaoRepository sessoes() {
        return new SessaoRepository() {
            @Override
            public Sessao salvar(Sessao sessao) {
                return new Sessao(1L, sessao.usuarioId(), sessao.identificadorHash(),
                        sessao.criadaEm(), sessao.ultimoUsoEm(), sessao.expiraEm(), sessao.origem());
            }

            @Override
            public Optional<Sessao> buscarPorIdentificadorHash(String identificadorHash) {
                return Optional.empty();
            }

            @Override
            public void apagar(Long sessaoId) {
                // nada
            }

            @Override
            public void apagarDoUsuario(Long usuarioId) {
                // nada
            }
        };
    }

    private Senhas senhas() {
        return new Senhas() {
            @Override
            public String hash(String senha) {
                return "hash-" + senha;
            }

            @Override
            public boolean confere(String senha, String hash) {
                trabalhoDeSenha.add("confere");
                return "certa".equals(senha);
            }

            @Override
            public boolean conferirEmVao(String senha) {
                trabalhoDeSenha.add("em-vao");
                return false;
            }
        };
    }

    private IdentificadoresDeSessao identificadores() {
        return new IdentificadoresDeSessao() {
            @Override
            public String gerar() {
                return "opaco";
            }

            @Override
            public String hash(String identificador) {
                return "hash-" + identificador;
            }
        };
    }

    private RegistroDeTentativas registro() {
        return new RegistroDeTentativas() {
            @Override
            public Tentativas consultar(String chave) {
                return tentativas.getOrDefault(chave, Tentativas.NENHUMA);
            }

            @Override
            public void registrarFalha(String chave, Instant agora) {
                tentativas.merge(chave, Tentativas.NENHUMA.maisUma(agora),
                        (atual, nova) -> atual.maisUma(agora));
            }

            @Override
            public void limpar(String chave) {
                tentativas.remove(chave);
            }
        };
    }
}
