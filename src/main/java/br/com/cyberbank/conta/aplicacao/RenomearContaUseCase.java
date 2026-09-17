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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenomearContaUseCase {

    private final ContaRepository contas;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RenomearContaUseCase(ContaRepository contas, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Conta executar(Long ambienteId, Long autorId, Long contaId, String novoNome) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Conta renomeada = contas.salvar(conta.renomeada(novoNome));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.CONTA_RENOMEADA,
                Alvo.conta(contaId), Evento.deParaDe("nome", conta.nome(), renomeada.nome()),
                diaLocal.hoje(), relogio.instant()));

        return renomeada;
    }
}
