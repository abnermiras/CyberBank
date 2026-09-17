package br.com.cyberbank.comum.rotina;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.AmbienteDaRotina;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.lancamento.aplicacao.RealizarLancamentosVencidosUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RotinaDiaria {

    private static final Logger LOG = LoggerFactory.getLogger(RotinaDiaria.class);

    private final AmbienteRepository ambientes;
    private final RealizarLancamentosVencidosUseCase realizarVencidos;

    public RotinaDiaria(AmbienteRepository ambientes,
            RealizarLancamentosVencidosUseCase realizarVencidos) {
        this.ambientes = ambientes;
        this.realizarVencidos = realizarVencidos;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = DiaLocal.FUSO_DE_BRASILIA)
    public void executar() {
        List<AmbienteDaRotina> todos = ambientes.listarParaRotina();
        int realizados = 0;

        for (AmbienteDaRotina ambiente : todos) {
            try {
                realizados += executarNoAmbiente(ambiente);
            } catch (RuntimeException falha) {
                LOG.error("rotina diaria falhou no ambiente {}", ambiente.ambienteId(), falha);
            }
        }

        if (realizados > 0) {
            LOG.info("rotina diaria: {} lancamentos realizados em {} ambientes",
                    realizados, todos.size());
        }
    }

    private int executarNoAmbiente(AmbienteDaRotina ambiente) {
        ContextoDaRequisicao.definirUsuario(ambiente.donoId());
        ContextoDaRequisicao.definirAmbiente(ambiente.ambienteId());
        try {
            return realizarVencidos.executar(ambiente.ambienteId(), ambiente.donoId());
        } finally {
            ContextoDaRequisicao.limpar();
        }
    }
}
