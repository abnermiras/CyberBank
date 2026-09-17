package br.com.cyberbank.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

class UsuarioTest {

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
}
