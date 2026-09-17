package br.com.cyberbank.conta.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirContaUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final MeioRepository meios;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ExcluirContaUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            MeioRepository meios, EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.meios = meios;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long ambienteId, Long autorId, Long contaId) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        conta.exigirExcluivel(lancamentos.contaTemLancamento(contaId));

        meios.listarDaConta(contaId, ambienteId)
                .forEach(meio -> meios.excluir(meio.id(), ambienteId));

        contas.excluir(contaId, ambienteId);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.CONTA_EXCLUIDA,
                Alvo.conta(contaId),
                Evento.dados("nome", conta.nome(), "tipo", conta.tipo().name()),
                diaLocal.hoje(), relogio.instant()));
    }
}
