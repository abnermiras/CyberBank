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
    private static final CorDeCategoria COR = CorDeCategoria.OCRE;

    private final Categoria transporte = comId(11L,
            Categoria.novaRaiz(AMBIENTE, "Transporte", Sentido.SAIDA, COR, AGORA));

    // --- criar ---

    @Test
    void a_raiz_nasce_ativa_sem_pai_e_do_usuario() {
        Categoria raiz = Categoria.novaRaiz(AMBIENTE, "Transporte", Sentido.SAIDA, COR, AGORA);

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
        assertThatThrownBy(() -> Categoria.novaRaiz(AMBIENTE, "   ", null, null, AGORA))
                .isInstanceOf(ValidacaoException.class)
                .extracting(e -> ((ValidacaoException) e).erros())
                .satisfies(erros -> assertThat((List<ErroDeValidacao>) erros)
                        .extracting(ErroDeValidacao::campo)
                        .containsExactly("nome", "sentido", "cor"));
    }

    @Test
    void o_nome_nao_passa_do_tamanho_da_coluna() {
        String longo = "x".repeat(Categoria.TAMANHO_MAXIMO_DO_NOME + 1);

        assertThatThrownBy(() -> Categoria.novaRaiz(AMBIENTE, longo, Sentido.SAIDA, COR, AGORA))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void o_nome_e_gravado_sem_o_espaco_das_pontas() {
        assertThat(Categoria.novaRaiz(AMBIENTE, "  Transporte  ", Sentido.SAIDA, COR, AGORA).nome())
                .isEqualTo("Transporte");
    }

    // --- cor ---

    @Test
    void a_raiz_nasce_com_a_cor_que_o_usuario_escolheu() {
        assertThat(Categoria.novaRaiz(AMBIENTE, "Moradia", Sentido.SAIDA, CorDeCategoria.MALVA, AGORA).cor())
                .isEqualTo(CorDeCategoria.MALVA);
    }

    @Test
    void a_subcategoria_nao_tem_cor_propria_ela_herda_a_da_raiz() {
        assertThat(Categoria.novaSubcategoria(transporte, "Gasolina", AGORA).cor())
                .as("cor vazia é o dado dizendo 'olhe a raiz', não ausência de cor na tela")
                .isNull();
    }

    @Test
    void trocar_a_cor_da_raiz_e_livre_e_nao_mexe_em_mais_nada() {
        Categoria recolorida = transporte.recolorida(CorDeCategoria.TEAL);

        assertThat(recolorida.cor()).isEqualTo(CorDeCategoria.TEAL);
        assertThat(recolorida.nome()).isEqualTo(transporte.nome());
        assertThat(recolorida.sentido()).isEqualTo(transporte.sentido());
        assertThat(recolorida.inativa()).isEqualTo(transporte.inativa());
    }

    @Test
    void subcategoria_nao_se_recolore() {
        Categoria gasolina = comId(12L, Categoria.novaSubcategoria(transporte, "Gasolina", AGORA));

        assertThatThrownBy(() -> gasolina.recolorida(CorDeCategoria.TEAL))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CATEGORIA_SEM_COR_PROPRIA);
    }

    @Test
    void categoria_de_sistema_nao_tem_cor_e_nao_se_recolore() {
        Categoria deSistema = comId(1L, Categoria.jogoDeSistema(AMBIENTE, AGORA).get(0));

        assertThat(deSistema.cor()).isNull();
        assertThatThrownBy(() -> deSistema.recolorida(CorDeCategoria.TEAL))
                .isInstanceOf(RegraDeDominioException.class)
                .extracting(e -> ((RegraDeDominioException) e).codigo())
                .isEqualTo(CodigoDeErro.CATEGORIA_DE_SISTEMA_PROTEGIDA);
    }

    @Test
    void a_paleta_de_identidade_tem_oito_tons() {
        assertThat(CorDeCategoria.values())
                .as("oito é o limite do que alguém separa de relance")
                .hasSize(8);
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
                sem.cor(), sem.sistema(), sem.operacao(), sem.inativa(), sem.criadaEm());
    }
}
