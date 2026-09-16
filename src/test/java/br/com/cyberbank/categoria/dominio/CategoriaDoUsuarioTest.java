package br.com.cyberbank.categoria.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.junit.jupiter.api.Test;

/**
 * As invariantes de docs/02-dominio/categoria.md que governam CRIAR, RENOMEAR e INATIVAR uma
 * categoria do usuario. Sem Spring e sem banco — se um dia precisarem, o desenho quebrou.
 */
class CategoriaDoUsuarioTest {

    private static final Instant AGORA = Instant.parse("2026-09-15T12:00:00Z");
    private static final Long AMBIENTE = 7L;

    private final Categoria transporte = comId(11L,
            Categoria.novaRaiz(AMBIENTE, "Transporte", Sentido.SAIDA, AGORA));

    // --- criar ---

    @Test
    void a_raiz_nasce_ativa_sem_pai_e_do_usuario() {
        Categoria raiz = Categoria.novaRaiz(AMBIENTE, "Transporte", Sentido.SAIDA, AGORA);

        assertThat(raiz.ehRaiz()).isTrue();
        assertThat(raiz.sistema()).isFalse();
        assertThat(raiz.operacao()).isNull();
        assertThat(raiz.inativa()).isFalse();
        assertThat(raiz.ambienteId()).isEqualTo(AMBIENTE);
    }

    @Test
    void a_subcategoria_herda_ambiente_e_sentido_da_raiz() {
        Categoria gasolina = Categoria.novaSubcategoria(transporte, "Gasolina", AGORA);

        assertThat(gasolina.paiId()).isEqualTo(transporte.id());
        assertThat(gasolina.ambienteId()).isEqualTo(transporte.ambienteId());
        assertThat(gasolina.sentido())
                .as("subcategoria tem o mesmo sentido da raiz e nao pode divergir")
                .isEqualTo(transporte.sentido());
    }

    @Test
    void subcategoria_nao_tem_filhos_a_arvore_tem_dois_niveis() {
        Categoria gasolina = comId(12L, Categoria.novaSubcategoria(transporte, "Gasolina", AGORA));

        assertThatThrownBy(() -> Categoria.novaSubcategoria(gasolina, "Aditivada", AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CATEGORIA_PAI_INVALIDO);
    }

    @Test
    void nao_se_pendura_subcategoria_numa_categoria_de_sistema() {
        Categoria deSistema = comId(1L, Categoria.jogoDeSistema(AMBIENTE, AGORA).get(0));

        assertThatThrownBy(() -> Categoria.novaSubcategoria(deSistema, "Minha", AGORA))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
    }

    @Test
    void nome_vazio_e_sentido_ausente_voltam_juntos_numa_resposta_so() {
        assertThatThrownBy(() -> Categoria.novaRaiz(AMBIENTE, "   ", null, AGORA))
                .isInstanceOf(ValidacaoException.class)
                .extracting(e -> ((ValidacaoException) e).erros())
                .satisfies(erros -> assertThat((List<ErroDeValidacao>) erros)
                        .extracting(ErroDeValidacao::campo)
                        .containsExactly("nome", "sentido"));
    }

    @Test
    void o_nome_nao_passa_do_tamanho_da_coluna() {
        String longo = "x".repeat(Categoria.TAMANHO_MAXIMO_DO_NOME + 1);

        assertThatThrownBy(() -> Categoria.novaRaiz(AMBIENTE, longo, Sentido.SAIDA, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void o_nome_e_gravado_sem_o_espaco_das_pontas() {
        assertThat(Categoria.novaRaiz(AMBIENTE, "  Transporte  ", Sentido.SAIDA, AGORA).nome())
                .isEqualTo("Transporte");
    }

    // --- renomear e inativar ---

    @Test
    void renomear_e_livre_e_nao_mexe_em_mais_nada() {
        Categoria renomeada = transporte.renomeada("Locomoção");

        assertThat(renomeada.nome()).isEqualTo("Locomoção");
        assertThat(renomeada.id()).isEqualTo(transporte.id());
        assertThat(renomeada.sentido()).isEqualTo(transporte.sentido());
        assertThat(renomeada.criadaEm()).isEqualTo(transporte.criadaEm());
    }

    @Test
    void inativar_e_reativar_e_a_volta_exata() {
        Categoria inativada = transporte.comAtivacao(true);

        assertThat(inativada.inativa()).isTrue();
        assertThat(inativada.comAtivacao(false)).isEqualTo(transporte);
    }

    @Test
    void inativar_a_raiz_nao_grava_nada_na_filha() {
        Categoria gasolina = comId(12L, Categoria.novaSubcategoria(transporte, "Gasolina", AGORA));

        transporte.comAtivacao(true);

        assertThat(gasolina.inativa())
                .as("heranca na leitura, nunca cascata na escrita")
                .isFalse();
    }

    // --- a protecao da categoria de sistema ---

    @Test
    void categoria_de_sistema_nao_se_renomeia_nao_se_inativa_e_nao_se_exclui() {
        Categoria deSistema = comId(1L, Categoria.jogoDeSistema(AMBIENTE, AGORA).get(0));

        assertThatThrownBy(() -> deSistema.renomeada("Outra")).isInstanceOf(RegraDeDominioException.class);
        assertThatThrownBy(() -> deSistema.comAtivacao(true)).isInstanceOf(RegraDeDominioException.class);
        assertThatThrownBy(deSistema::exigirExcluivel).isInstanceOf(RegraDeDominioException.class);
    }

    private static Categoria comId(Long id, Categoria sem) {
        return new Categoria(id, sem.ambienteId(), sem.paiId(), sem.nome(), sem.sentido(),
                sem.sistema(), sem.operacao(), sem.inativa(), sem.criadaEm());
    }
}
