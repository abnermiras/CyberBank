package br.com.cyberbank.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final Instant AGORA = Instant.parse("2026-09-17T12:00:00Z");

    @Test
    void o_email_e_normalizado_porque_unico_no_sistema_inteiro_depende_disso() {
        assertThat(Usuario.normalizarEmail("  Ana@Exemplo.COM ")).isEqualTo("ana@exemplo.com");
    }

    @Test
    void a_validacao_devolve_todos_os_campos_de_uma_vez() {
        assertThatThrownBy(() -> Usuario.validarCadastro("", "sem-arroba", "curta"))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .extracting(ErroDeValidacao::campo)
                        .containsExactlyInAnyOrder("nome", "email", "senha"));
    }

    @Test
    void senha_curta_nao_passa() {
        assertThatThrownBy(() -> Usuario.validarCadastro("Ana", "ana@exemplo.com", "1234567"))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .singleElement()
                        .satisfies(erro -> assertThat(erro.campo()).isEqualTo("senha")));
    }

    @Test
    void cadastro_valido_nao_reclama_de_nada() {
        assertThatCode(() -> Usuario.validarCadastro("Ana", "Ana@Exemplo.com", "uma senha longa"))
                .doesNotThrowAnyException();
    }

    @Test
    void quem_nasce_ja_tem_avatar_e_nao_tem_telegram() {
        Usuario ana = Usuario.cadastrar("Ana", "ana@exemplo.com", "hash", Avatar.DRONE, AGORA);

        assertThat(ana.avatar()).isEqualTo(Avatar.DRONE);
        assertThat(ana.telegramChatId()).isNull();
    }

    @Test
    void o_perfil_tambem_devolve_todos_os_campos_de_uma_vez() {
        assertThatThrownBy(() -> Usuario.validarAlteracaoDePerfil(
                "", "CACHORRO", "outro@exemplo.com"))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .extracting(ErroDeValidacao::campo)
                        .containsExactlyInAnyOrder("nome", "avatar", "email"));
    }

    @Test
    void mandar_email_no_corpo_e_erro_e_nao_silencio() {
        assertThatThrownBy(() -> Usuario.validarAlteracaoDePerfil(
                "Ana", "GATO", "novo@exemplo.com"))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .singleElement()
                        .satisfies(erro -> {
                            assertThat(erro.campo()).isEqualTo("email");
                            assertThat(erro.codigo()).isEqualTo("IMUTAVEL");
                        }));
    }

    @Test
    void perfil_sem_email_no_corpo_nao_reclama_de_nada() {
        assertThatCode(() -> Usuario.validarAlteracaoDePerfil("Ana", "PRISMA", null))
                .doesNotThrowAnyException();
    }

    @Test
    void zero_nao_e_chat_de_ninguem_e_ausente_tambem_nao() {
        assertThatThrownBy(() -> Usuario.validarTelegramChatId(0L))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> Usuario.validarTelegramChatId(null))
                .isInstanceOf(ValidacaoException.class);
        assertThatCode(() -> Usuario.validarTelegramChatId(-4712993L))
                .as("chat de grupo tem id negativo, e quem decide se ele serve é o bot")
                .doesNotThrowAnyException();
    }

    @Test
    void desvincular_o_telegram_nao_mexe_em_mais_nada() {
        Usuario ana = Usuario.cadastrar("Ana", "ana@exemplo.com", "hash", Avatar.ONDA, AGORA)
                .comTelegramChatId(42L);

        Usuario depois = ana.comTelegramChatId(null);

        assertThat(depois.telegramChatId()).isNull();
        assertThat(depois.nome()).isEqualTo("Ana");
        assertThat(depois.avatar()).isEqualTo(Avatar.ONDA);
    }

    @Test
    void trocar_o_perfil_nao_mexe_no_email_nem_na_senha() {
        Usuario ana = Usuario.cadastrar("Ana", "ana@exemplo.com", "hash", Avatar.DRONE, AGORA);

        Usuario depois = ana.comPerfil("  Ana Miras  ", Avatar.ROBO);

        assertThat(depois.nome()).isEqualTo("Ana Miras");
        assertThat(depois.avatar()).isEqualTo(Avatar.ROBO);
        assertThat(depois.email()).isEqualTo(ana.email());
        assertThat(depois.senhaHash()).isEqualTo(ana.senhaHash());
    }

    @Test
    void a_nova_senha_curta_reclama_no_campo_da_nova_senha() {
        assertThatThrownBy(() -> Usuario.validarNovaSenha("1234567"))
                .isInstanceOf(ValidacaoException.class)
                .satisfies(e -> assertThat(((ValidacaoException) e).erros())
                        .singleElement()
                        .satisfies(erro -> assertThat(erro.campo()).isEqualTo("novaSenha")));
    }
}
