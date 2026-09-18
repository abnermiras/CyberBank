package br.com.cyberbank.fatura.aplicacao;

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
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Situacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagarFaturaUseCase {

    private final FaturaRepository faturas;
    private final ContaRepository contas;
    private final NumerosDasFaturas numerosDasFaturas;
    private final EncerramentoDaFatura encerramento;
    private final LancamentoRepository lancamentos;
    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public PagarFaturaUseCase(FaturaRepository faturas, ContaRepository contas,
            NumerosDasFaturas numerosDasFaturas, EncerramentoDaFatura encerramento,
            LancamentoRepository lancamentos, CategoriaRepository categorias,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.faturas = faturas;
        this.contas = contas;
        this.numerosDasFaturas = numerosDasFaturas;
        this.encerramento = encerramento;
        this.lancamentos = lancamentos;
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public List<Lancamento> executar(Long ambienteId, Long autorId, Long faturaId,
            Long contaPagadoraId, Long valorCentavos, LocalDate dataEvento) {

        Fatura fatura = faturas.buscarDoAmbiente(faturaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        fatura.exigirQueRecebaPagamento(numerosDasFaturas.de(fatura).numeros());

        Conta pagadora = contas.buscarDoAmbiente(contaPagadoraId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        pagadora.exigirAtivaParaLancar();
        pagadora.exigirTransferivel();

        Conta cartao = contas.buscarDoAmbiente(fatura.contaId(), ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        LocalDate hoje = diaLocal.hoje();
        LocalDate dia = dataEvento == null ? hoje : dataEvento;
        Situacao situacao = dia.isAfter(hoje) ? Situacao.PREVISTO : Situacao.REALIZADO;

        List<Lancamento> par = lancamentos.salvarTodos(Lancamento.parDePagamentoDeFatura(
                ambienteId, pagadora.id(), cartao.id(),
                categoriaDePagamento(ambienteId, Sentido.SAIDA),
                categoriaDePagamento(ambienteId, Sentido.ENTRADA),
                autorId, valorCentavos, dia, "Pagamento de fatura", situacao,
                lancamentos.proximoIdDeTransferencia(), fatura.id(), relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.FATURA_PAGA,
                Alvo.fatura(fatura.id()),
                Evento.dados(
                        "competencia", fatura.competencia().toString(),
                        "valor", valorCentavos,
                        "contaId", cartao.id(),
                        "contaPagadoraId", pagadora.id(),
                        "situacao", situacao.name(),
                        "dia", dia.toString()),
                hoje, relogio.instant()));

        encerramento.liquidarSeEncerrou(ambienteId, autorId, fatura, hoje);

        return par;
    }

    private Long categoriaDePagamento(Long ambienteId, Sentido sentido) {
        return categorias
                .buscarDeSistema(ambienteId, OperacaoDeSistema.PAGAMENTO_DE_FATURA, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();
    }
}
