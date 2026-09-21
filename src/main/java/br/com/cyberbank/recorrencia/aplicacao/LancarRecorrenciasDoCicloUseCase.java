package br.com.cyberbank.recorrencia.aplicacao;

import java.time.Clock;
import java.time.YearMonth;

import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;
import br.com.cyberbank.recorrencia.dominio.RecorrenciaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LancarRecorrenciasDoCicloUseCase {

    private final RecorrenciaRepository recorrencias;
    private final OcorrenciaDoCiclo ocorrencia;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public LancarRecorrenciasDoCicloUseCase(RecorrenciaRepository recorrencias,
            OcorrenciaDoCiclo ocorrencia, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.recorrencias = recorrencias;
        this.ocorrencia = ocorrencia;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public int executar(Long contaId, Long autorId, Long faturaId, YearMonth competencia) {
        int lancadas = 0;
        for (Recorrencia ativa : recorrencias.listarAtivasDaConta(contaId)) {
            Lancamento lancada = ocorrencia
                    .lancarSeFaltar(ativa, autorId, faturaId, competencia)
                    .orElse(null);

            if (lancada != null) {
                registrar(ativa, lancada, competencia, autorId);
                lancadas++;
            }
        }
        return lancadas;
    }

    private void registrar(Recorrencia recorrencia, Lancamento lancada, YearMonth competencia,
            Long autorId) {

        eventos.registrar(Evento.doSistema(recorrencia.ambienteId(), autorId,
                TipoDeEvento.OCORRENCIA_DE_RECORRENCIA, Alvo.lancamento(lancada.id()),
                Evento.dados(
                        "descricao", recorrencia.descricao(),
                        "valor", lancada.valorCentavos(),
                        "competencia", competencia.toString(),
                        "contaId", recorrencia.contaId(),
                        "serieId", recorrencia.id(),
                        "dia", lancada.dataEvento().toString()),
                diaLocal.hoje(), relogio.instant()));
    }
}
