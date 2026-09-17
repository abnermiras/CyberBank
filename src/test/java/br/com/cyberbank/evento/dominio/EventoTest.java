package br.com.cyberbank.evento.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class EventoTest {

    private static final Long AMBIENTE = 1L;
    private static final Long AUTOR = 7L;
    private static final LocalDate HOJE = LocalDate.of(2026, 9, 17);
    private static final Instant AGORA = Instant.parse("2026-09-17T14:30:00Z");

    @Test
    void o_evento_do_usuario_carrega_quem_fez_e_o_dia_em_que_aconteceu() {
        Evento evento = Evento.doUsuario(AMBIENTE, AUTOR, TipoDeEvento.LANCAMENTO_CRIADO,
                Alvo.lancamento(42L), Evento.dados("valor", 3000L), HOJE, AGORA);

        assertThat(evento.origem()).isEqualTo(OrigemDeEvento.USUARIO);
        assertThat(evento.autorId()).isEqualTo(AUTOR);
        assertThat(evento.dia()).isEqualTo(HOJE);
        assertThat(evento.instante()).isEqualTo(AGORA);
        assertThat(evento.alvo()).isEqualTo(new Alvo(TipoDeAlvo.LANCAMENTO, 42L));
    }

    @Test
    void o_evento_do_sistema_tem_o_dono_do_ambiente_como_autor() {
        Evento evento = Evento.doSistema(AMBIENTE, AUTOR, TipoDeEvento.LANCAMENTO_REALIZADO,
                Alvo.lancamento(42L), Map.of(), HOJE, AGORA);

        assertThat(evento.origem()).isEqualTo(OrigemDeEvento.SISTEMA);
        assertThat(evento.autorId())
                .as("todo evento tem autor, e no do sistema ele é o dono do ambiente")
                .isEqualTo(AUTOR);
    }

    @Test
    void tipo_de_sistema_nao_se_grava_como_acao_do_usuario_e_o_contrario_tambem_nao() {
        assertThatThrownBy(() -> Evento.doUsuario(AMBIENTE, AUTOR,
                TipoDeEvento.LANCAMENTO_REALIZADO, null, Map.of(), HOJE, AGORA))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Evento.doSistema(AMBIENTE, AUTOR,
                TipoDeEvento.LANCAMENTO_CRIADO, null, Map.of(), HOJE, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void o_de_para_de_um_campo_que_nao_mudou_nao_vira_dado() {
        assertThat(Evento.deParaDe("nome", "Nubank", "Nubank")).isEmpty();
        assertThat(Evento.deParaDe("nome", "Nubank", "Nu"))
                .containsExactly(Map.entry("nomeDe", "Nubank"), Map.entry("nomePara", "Nu"));
    }

    @Test
    void dado_nulo_nao_ocupa_espaco_e_dados_vazio_vira_ausencia_de_dados() {
        Evento semDados = Evento.doUsuario(AMBIENTE, AUTOR, TipoDeEvento.CONTA_CRIADA,
                Alvo.conta(1L), Evento.dados("nome", "Cofre", "saldoInicial", null), HOJE, AGORA);

        assertThat(semDados.dados()).containsOnlyKeys("nome");

        assertThat(Evento.doUsuario(AMBIENTE, AUTOR, TipoDeEvento.CONTA_CRIADA, Alvo.conta(1L),
                Map.of(), HOJE, AGORA).dados()).isNull();
    }

    @Test
    void reunir_junta_as_mudancas_de_varios_campos_numa_linha_so() {
        Map<String, Object> tudo = Evento.reunir(List.of(
                Evento.deParaDe("valor", 1000L, 2000L),
                Evento.deParaDe("descricao", "Feira", "Feira"),
                Evento.deParaDe("situacao", "PREVISTO", "REALIZADO")));

        assertThat(tudo).containsOnlyKeys("valorDe", "valorPara", "situacaoDe", "situacaoPara");
    }

    @Test
    void o_alvo_e_o_par_inteiro_ou_nenhum() {
        assertThatThrownBy(() -> new Alvo(TipoDeAlvo.CONTA, null))
                .isInstanceOf(NullPointerException.class);
    }
}
