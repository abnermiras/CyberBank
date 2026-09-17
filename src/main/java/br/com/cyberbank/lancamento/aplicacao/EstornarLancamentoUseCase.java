package br.com.cyberbank.lancamento.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

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
public class EstornarLancamentoUseCase {

    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public EstornarLancamentoUseCase(LancamentoRepository lancamentos, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Lancamento executar(Long ambienteId, Long autorId, Long lancamentoId, LocalDate dia) {
        Lancamento original = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        LocalDate quando = dia == null ? diaLocal.hoje() : dia;
        Lancamento estorno = lancamentos.salvar(
                original.estornadoEm(quando, autorId, relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_ESTORNADO,
                Alvo.lancamento(lancamentoId),
                Evento.dados(
                        "descricao", original.descricao(),
                        "valor", original.valorCentavos(),
                        "estornoId", estorno.id(),
                        "dataDoEstorno", quando.toString()),
                diaLocal.hoje(), relogio.instant()));

        return estorno;
    }
}
