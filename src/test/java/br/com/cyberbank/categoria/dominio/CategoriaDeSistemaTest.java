package br.com.cyberbank.categoria.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class CategoriaDeSistemaTest {

    private static final Instant AGORA = Instant.parse("2026-09-07T12:00:00Z");

    private final List<Categoria> jogo = Categoria.jogoDeSistema(7L, AGORA);

    @Test
    void sao_sete_operacoes_vezes_dois_sentidos() {
        assertThat(jogo).hasSize(14);
        assertThat(Categoria.QUANTIDADE_DE_SISTEMA).isEqualTo(14);
    }

    @Test
    void cada_operacao_existe_nos_dois_sentidos_com_o_mesmo_nome() {
        for (OperacaoDeSistema operacao : OperacaoDeSistema.values()) {
            assertThat(jogo)
                    .filteredOn(c -> c.operacao() == operacao)
                    .as("as duas metades de %s", operacao)
                    .hasSize(2)
                    .allSatisfy(c -> assertThat(c.nome()).isEqualTo(operacao.nome()))
                    .extracting(Categoria::sentido)
                    .containsExactlyInAnyOrder(Sentido.ENTRADA, Sentido.SAIDA);
        }
    }

    @Test
    void nascem_raiz_ativas_e_do_ambiente_que_as_criou() {
        assertThat(jogo).allSatisfy(c -> {
            assertThat(c.sistema()).isTrue();
            assertThat(c.ehRaiz()).isTrue();
            assertThat(c.inativa()).isFalse();
            assertThat(c.ambienteId()).isEqualTo(7L);
        });
    }

    @Test
    void nenhuma_categoria_de_sistema_e_escolhivel() {
        assertThat(jogo).allSatisfy(c -> assertThat(c.escolhivel(true, false)).isFalse());
    }
}
