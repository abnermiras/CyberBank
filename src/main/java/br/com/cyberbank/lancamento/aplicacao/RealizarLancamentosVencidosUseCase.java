package br.com.cyberbank.lancamento.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RealizarLancamentosVencidosUseCase {

    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RealizarLancamentosVencidosUseCase(LancamentoRepository lancamentos,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public int executar(Long ambienteId, Long donoDoAmbienteId) {
        LocalDate hoje = diaLocal.hoje();
        List<Lancamento> vencidos = lancamentos.listarPrevistosVencidos(ambienteId, hoje);
        if (vencidos.isEmpty()) {
            return 0;
        }

        Instant agora = relogio.instant();
        List<Lancamento> realizados = lancamentos.salvarTodos(
                vencidos.stream().map(vencido -> vencido.realizadoPelaData(hoje)).toList());

        eventos.registrarTodos(realizados.stream()
                .map(realizado -> Evento.doSistema(ambienteId, donoDoAmbienteId,
                        TipoDeEvento.LANCAMENTO_REALIZADO, Alvo.lancamento(realizado.id()),
                        Evento.dados(
                                "descricao", realizado.descricao(),
                                "valor", realizado.valorCentavos(),
                                "sentido", realizado.sentido().name(),
                                "contaId", realizado.contaId(),
                                "dataEfeito", realizado.dataEfeito().toString()),
                        hoje, agora))
                .toList());

        return realizados.size();
    }
}
