package br.com.cyberbank.compartilhamento.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.compartilhamento.dominio.CompartilhamentoRepository;
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
public class RevogarCompartilhamentoUseCase {

    private final CompartilhamentoRepository compartilhamentos;
    private final ContaRepository contas;
    private final AcessoRepository acessos;
    private final AmbienteRepository ambientes;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RevogarCompartilhamentoUseCase(CompartilhamentoRepository compartilhamentos,
            ContaRepository contas, AcessoRepository acessos, AmbienteRepository ambientes,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.compartilhamentos = compartilhamentos;
        this.contas = contas;
        this.acessos = acessos;
        this.ambientes = ambientes;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, Long ambienteOrigemId, Long contaId,
            Long ambienteDestinoId) {

        Acesso acessoNaOrigem = acessos.buscar(usuarioId, ambienteOrigemId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        if (!acessoNaOrigem.papel().podeCompartilhar()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }

        Conta conta = contas.buscarDoAmbiente(contaId, ambienteOrigemId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        compartilhamentos.buscar(contaId, ambienteDestinoId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        compartilhamentos.revogar(contaId, ambienteDestinoId);

        String nomeDoDestino = ambientes.buscar(ambienteDestinoId).map(Ambiente::nome).orElse(null);
        eventos.registrar(Evento.doUsuario(ambienteOrigemId, usuarioId,
                TipoDeEvento.VINCULO_REVOGADO, Alvo.conta(contaId),
                Evento.dados("conta", conta.nome(), "destinoId", ambienteDestinoId,
                        "destino", nomeDoDestino),
                diaLocal.hoje(), relogio.instant()));
    }
}
