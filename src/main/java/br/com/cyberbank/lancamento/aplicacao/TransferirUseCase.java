package br.com.cyberbank.lancamento.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.TipoDeConta;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Situacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferirUseCase {

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public TransferirUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            CategoriaRepository categorias, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public List<Lancamento> executar(Long ambienteId, Long autorId, Long contaDeOrigemId,
            Long contaDeDestinoId, Long valorCentavos, LocalDate dataEvento, String descricao) {

        Conta origem = buscar(contaDeOrigemId, ambienteId);
        Conta destino = buscar(contaDeDestinoId, ambienteId);

        origem.exigirAtivaParaLancar();
        destino.exigirAtivaParaLancar();
        origem.exigirTransferivel();
        destino.exigirTransferivel();

        OperacaoDeSistema operacao = operacaoDe(origem, destino);
        Situacao situacao = dataEvento.isAfter(diaLocal.hoje())
                ? Situacao.PREVISTO
                : Situacao.REALIZADO;

        long transferenciaId = lancamentos.proximoIdDeTransferencia();

        List<Lancamento> par = lancamentos.salvarTodos(Lancamento.parDeTransferencia(ambienteId,
                contaDeOrigemId, contaDeDestinoId,
                categoriaDeSistema(ambienteId, operacao, Sentido.SAIDA),
                categoriaDeSistema(ambienteId, operacao, Sentido.ENTRADA),
                autorId, valorCentavos, dataEvento, dataEvento, descricao, situacao,
                transferenciaId, relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_CRIADO,
                Alvo.lancamento(par.getFirst().id()),
                Evento.dados(
                        "descricao", descricao,
                        "valor", valorCentavos,
                        "operacao", operacao.name(),
                        "contaDeOrigemId", contaDeOrigemId,
                        "contaDeDestinoId", contaDeDestinoId,
                        "transferenciaId", transferenciaId),
                diaLocal.hoje(), relogio.instant()));

        return par;
    }

    private static OperacaoDeSistema operacaoDe(Conta origem, Conta destino) {
        if (destino.tipo() == TipoDeConta.APLICACAO) {
            return OperacaoDeSistema.APORTE;
        }
        if (origem.tipo() == TipoDeConta.APLICACAO) {
            return OperacaoDeSistema.RESGATE;
        }
        return OperacaoDeSistema.TRANSFERENCIA;
    }

    private Long categoriaDeSistema(Long ambienteId, OperacaoDeSistema operacao, Sentido sentido) {
        return categorias.buscarDeSistema(ambienteId, operacao, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();
    }

    private Conta buscar(Long contaId, Long ambienteId) {
        return contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
    }
}
