package br.com.cyberbank.lancamento.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
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
public class ExcluirLancamentoUseCase {

    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ExcluirLancamentoUseCase(LancamentoRepository lancamentos, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long ambienteId, Long autorId, Long lancamentoId) {
        Lancamento lancamento = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        lancamento.exigirExcluivelPeloUsuario(lancamentos.temEstorno(lancamentoId));

        if (lancamento.ehTransferencia()) {
            lancamentos.listarDaTransferencia(lancamento.transferenciaId(), ambienteId)
                    .forEach(lado -> lancamentos.excluir(lado.id(), ambienteId));
        } else {
            lancamentos.excluir(lancamentoId, ambienteId);
        }

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_EXCLUIDO,
                Alvo.lancamento(lancamentoId),
                Evento.dados(
                        "descricao", lancamento.descricao(),
                        "valor", lancamento.valorCentavos(),
                        "sentido", lancamento.sentido().name(),
                        "situacao", lancamento.situacao().name(),
                        "contaId", lancamento.contaId(),
                        "categoriaId", lancamento.categoriaId(),
                        "dataEvento", lancamento.dataEvento().toString(),
                        "dataEfeito", lancamento.dataEfeito().toString(),
                        "transferenciaId", lancamento.transferenciaId()),
                diaLocal.hoje(), relogio.instant()));
    }
}
