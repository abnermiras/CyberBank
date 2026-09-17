package br.com.cyberbank.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.jupiter.api.Test;

class AvatarTest {

    @Test
    void sao_dez_e_a_tela_recebe_a_lista_do_servidor() {
        assertThat(Avatar.nomes())
                .hasSize(10)
                .containsExactly("VISOR", "OLHO", "GATO", "CAVEIRA", "DRONE",
                        "CIRCUITO", "ONDA", "TORRE", "PRISMA", "ROBO");
    }

    @Test
    void o_sorteio_vem_de_fora_e_por_isso_o_teste_e_deterministico() {
        assertThat(Avatar.sortear(new Random(7))).isEqualTo(Avatar.sortear(new Random(7)));
    }

    @Test
    void o_sorteio_so_devolve_avatar_que_existe() {
        Random sorteio = new Random(1);
        for (int i = 0; i < 100; i++) {
            assertThat(Avatar.nomes()).contains(Avatar.sortear(sorteio).name());
        }
    }

    @Test
    void avatar_fora_da_lista_nao_existe() {
        assertThat(Avatar.existe("GATO")).isTrue();
        assertThat(Avatar.existe("CACHORRO")).isFalse();
        assertThat(Avatar.existe("gato")).isFalse();
    }
}
