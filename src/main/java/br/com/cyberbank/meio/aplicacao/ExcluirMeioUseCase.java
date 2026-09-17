package br.com.cyberbank.meio.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirMeioUseCase {

    private final MeioRepository meios;
    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ExcluirMeioUseCase(MeioRepository meios, LancamentoRepository lancamentos,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.meios = meios;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long ambienteId, Long autorId, Long meioId) {
        Meio meio = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        meio.exigirExcluivel(lancamentos.meioTemLancamento(meioId));
        meios.excluir(meioId, ambienteId);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.MEIO_EXCLUIDO,
                Alvo.meio(meioId),
                Evento.dados("nome", meio.nome(), "tipo", meio.tipo().name()),
                diaLocal.hoje(), relogio.instant()));
    }
}
