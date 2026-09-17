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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarAtivacaoContaUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AlterarAtivacaoContaUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Conta executar(Long ambienteId, Long autorId, Long contaId, boolean inativa) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (inativa == conta.inativa()) {
            return conta;
        }
        if (inativa) {
            lancamentos.excluirPrevistosDaConta(contaId, ambienteId);
        }
        Conta alterada = contas.salvar(conta.comAtivacao(inativa));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId,
                inativa ? TipoDeEvento.CONTA_INATIVADA : TipoDeEvento.CONTA_REATIVADA,
                Alvo.conta(contaId), Evento.dados("nome", alterada.nome()),
                diaLocal.hoje(), relogio.instant()));

        return alterada;
    }
}
