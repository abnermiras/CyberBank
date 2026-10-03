package br.com.cyberbank.compartilhamento.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.compartilhamento.dominio.Compartilhamento;
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
public class CompartilharContaUseCase {

    private final CompartilhamentoRepository compartilhamentos;
    private final ContaRepository contas;
    private final AcessoRepository acessos;
    private final AmbienteRepository ambientes;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public CompartilharContaUseCase(CompartilhamentoRepository compartilhamentos,
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
    public Compartilhamento executar(Long usuarioId, Long ambienteOrigemId, Long contaId,
            Long ambienteDestinoId) {

        Acesso acessoNaOrigem = acessos.buscar(usuarioId, ambienteOrigemId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        if (!acessoNaOrigem.papel().podeCompartilhar()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }

        Conta conta = contas.buscarDoAmbiente(contaId, ambienteOrigemId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        boolean podeAlterarODestino = ambienteDestinoId != null
                && acessos.buscar(usuarioId, ambienteDestinoId)
                        .map(acesso -> acesso.papel().podeAlterarODado())
                        .orElse(false);

        Compartilhamento novo = Compartilhamento.novo(contaId, conta.ehContratoDeCartao(),
                ambienteOrigemId, ambienteDestinoId, podeAlterarODestino, usuarioId,
                relogio.instant());

        if (compartilhamentos.buscar(contaId, ambienteDestinoId).isPresent()) {
            throw new RegraDeDominioException(CodigoDeErro.COMPARTILHAMENTO_JA_EXISTE);
        }

        Compartilhamento gravado = compartilhamentos.salvar(novo);

        String nomeDoDestino = ambientes.buscar(ambienteDestinoId).map(Ambiente::nome).orElse(null);
        eventos.registrar(Evento.doUsuario(ambienteOrigemId, usuarioId,
                TipoDeEvento.VINCULO_CRIADO, Alvo.conta(contaId),
                Evento.dados("conta", conta.nome(), "destinoId", ambienteDestinoId,
                        "destino", nomeDoDestino),
                diaLocal.hoje(), relogio.instant()));

        return gravado;
    }
}
