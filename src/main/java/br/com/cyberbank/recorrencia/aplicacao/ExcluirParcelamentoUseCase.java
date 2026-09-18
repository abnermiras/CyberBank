package br.com.cyberbank.recorrencia.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirParcelamentoUseCase {

    private final ParcelamentoRepository parcelamentos;
    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ExcluirParcelamentoUseCase(ParcelamentoRepository parcelamentos,
            LancamentoRepository lancamentos, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.parcelamentos = parcelamentos;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long ambienteId, Long autorId, Long parcelamentoId) {
        Parcelamento parcelamento = parcelamentos.buscarDoAmbiente(parcelamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        int parcelas = lancamentos.listarDoParcelamento(parcelamentoId, ambienteId).size();

        lancamentos.excluirDoParcelamento(parcelamentoId, ambienteId);
        parcelamentos.excluir(parcelamentoId, ambienteId);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.SERIE_CANCELADA,
                Alvo.serie(parcelamentoId),
                Evento.dados(
                        "descricao", parcelamento.descricao(),
                        "valor", parcelamento.valorDaCompraCentavos(),
                        "parcelas", parcelas,
                        "contaId", parcelamento.contaId()),
                diaLocal.hoje(), relogio.instant()));
    }
}
