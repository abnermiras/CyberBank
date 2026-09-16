package br.com.cyberbank.categoria.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class ArvoreDeCategoriasTest {

    private static final Instant AGORA = Instant.parse("2026-09-07T12:00:00Z");

    private Categoria raiz(long id, String nome, boolean inativa) {
        return new Categoria(id, 1L, null, nome, Sentido.SAIDA, CorDeCategoria.OCRE, false, null, inativa, AGORA);
    }

    private Categoria filha(long id, long paiId, String nome, boolean inativa) {
        return new Categoria(id, 1L, paiId, nome, Sentido.SAIDA, null, false, null, inativa, AGORA);
    }

    private ArvoreDeCategorias.No primeira(List<Categoria> categorias) {
        return ArvoreDeCategorias.montar(categorias).get(0);
    }

    @Test
    void raiz_sem_filha_e_escolhivel() {
        assertThat(primeira(List.of(raiz(1, "Sustento", false))).escolhivel()).isTrue();
    }

    @Test
    void raiz_com_filha_ativa_deixa_de_ser_escolhivel() {
        var arvore = primeira(List.of(raiz(1, "Sustento", false), filha(2, 1, "Mercado", false)));

        assertThat(arvore.escolhivel()).isFalse();
        assertThat(arvore.filhas()).singleElement()
                .satisfies(filha -> assertThat(filha.escolhivel()).isTrue());
    }

    @Test
    void inativar_a_ultima_filha_ativa_devolve_a_raiz_para_a_escolha() {
        var arvore = primeira(List.of(raiz(1, "Sustento", false), filha(2, 1, "Mercado", true)));

        assertThat(arvore.escolhivel()).isTrue();
    }

    @Test
    void a_raiz_esconde_a_arvore_sem_tocar_no_campo_da_filha() {
        var arvore = primeira(List.of(raiz(1, "Sustento", true), filha(2, 1, "Mercado", false)));

        assertThat(arvore.escolhivel()).isFalse();
        assertThat(arvore.filhas().get(0).escolhivel()).isFalse();
        assertThat(arvore.filhas().get(0).categoria().inativa())
                .as("heranca na leitura, nunca cascata na escrita")
                .isFalse();
    }

    @Test
    void as_de_sistema_saem_do_filtro_padrao() {
        List<Categoria> tudo = new java.util.ArrayList<>(Categoria.jogoDeSistema(1L, AGORA));
        tudo.add(raiz(99, "Transporte", false));

        var semSistema = ArvoreDeCategorias.semSistema(ArvoreDeCategorias.montar(tudo));

        assertThat(semSistema).singleElement()
                .satisfies(no -> assertThat(no.categoria().nome()).isEqualTo("Transporte"));
    }
}
