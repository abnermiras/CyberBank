package br.com.cyberbank.fatura.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.ContratoDeCartao;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.AgendaDoCiclo;
import br.com.cyberbank.fatura.dominio.CicloDaFatura;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.PassoDoCiclo;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RodarCicloDasFaturasUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(RodarCicloDasFaturasUseCase.class);

    private final ContaRepository contas;
    private final FaturaRepository faturas;
    private final NumerosDasFaturas numerosDasFaturas;
    private final EncerramentoDaFatura encerramento;
    private final LancamentoRepository lancamentos;
    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RodarCicloDasFaturasUseCase(ContaRepository contas, FaturaRepository faturas,
            NumerosDasFaturas numerosDasFaturas, EncerramentoDaFatura encerramento,
            LancamentoRepository lancamentos, CategoriaRepository categorias,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.faturas = faturas;
        this.numerosDasFaturas = numerosDasFaturas;
        this.encerramento = encerramento;
        this.lancamentos = lancamentos;
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public int executar(Long ambienteId, Long donoDoAmbienteId) {
        LocalDate hoje = diaLocal.hoje();
        int passos = 0;

        for (Conta conta : contas.listarDoAmbiente(ambienteId)) {
            if (conta.ehContratoDeCartao()) {
                passos += rodarNoCartao(ambienteId, donoDoAmbienteId, conta, hoje);
            }
        }
        return passos;
    }

    private int rodarNoCartao(Long ambienteId, Long autorId, Conta cartao, LocalDate hoje) {
        ContratoDeCartao contrato = cartao.contrato();
        CicloDaFatura ciclo = new CicloDaFatura(contrato.diaVencimento(),
                contrato.diasAntesFechamento());
        int aplicados = 0;

        while (aplicados < AgendaDoCiclo.PASSOS_POR_RODADA) {
            Optional<PassoDoCiclo> proximo = AgendaDoCiclo.proximoPasso(
                    numerosDasFaturas.daConta(cartao.id()), hoje);

            if (proximo.isEmpty()) {
                return aplicados;
            }
            aplicar(proximo.get(), ambienteId, autorId, cartao, ciclo, hoje);
            aplicados++;
        }

        LOG.error("ciclo da fatura nao convergiu no cartao {} — {} passos numa rodada so",
                cartao.id(), aplicados);
        return aplicados;
    }

    private void aplicar(PassoDoCiclo passo, Long ambienteId, Long autorId, Conta cartao,
            CicloDaFatura ciclo, LocalDate hoje) {

        switch (passo.tipo()) {
            case ROLAR -> rolar(passo, ambienteId, autorId, cartao, hoje);
            case ENCERRAR -> encerrar(passo.fatura(), ambienteId, autorId, hoje);
            case FECHAR -> fechar(passo.fatura(), ambienteId, autorId, ciclo, hoje);
        }
    }

    private void rolar(PassoDoCiclo passo, Long ambienteId, Long autorId, Conta cartao,
            LocalDate hoje) {

        Fatura venceu = passo.fatura();
        Fatura aberta = faturas.buscarAbertaDaConta(cartao.id())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        lancamentos.salvarTodos(Lancamento.parDeRolagem(ambienteId, cartao.id(),
                categoriaDeRolagem(ambienteId, Sentido.ENTRADA),
                categoriaDeRolagem(ambienteId, Sentido.SAIDA),
                autorId, passo.valorCentavos(), passo.dia(), venceu.id(), aberta.id(),
                lancamentos.proximoIdDeRolagem(), relogio.instant()));

        eventos.registrar(Evento.doSistema(ambienteId, autorId, TipoDeEvento.FATURA_ROLADA,
                Alvo.fatura(venceu.id()),
                Evento.dados(
                        "competencia", venceu.competencia().toString(),
                        "valor", passo.valorCentavos(),
                        "contaId", cartao.id(),
                        "faturaDestinoId", aberta.id(),
                        "competenciaDestino", aberta.competencia().toString(),
                        "dia", passo.dia().toString()),
                hoje, relogio.instant()));

        encerrar(venceu, ambienteId, autorId, hoje);
    }

    private void encerrar(Fatura fatura, Long ambienteId, Long autorId, LocalDate hoje) {
        encerramento.liquidar(ambienteId, autorId, fatura, hoje);
    }

    private void fechar(Fatura aberta, Long ambienteId, Long autorId, CicloDaFatura ciclo,
            LocalDate hoje) {

        faturas.salvar(aberta.fechada());

        eventos.registrar(Evento.doSistema(ambienteId, autorId, TipoDeEvento.FATURA_FECHADA,
                Alvo.fatura(aberta.id()),
                Evento.dados(
                        "competencia", aberta.competencia().toString(),
                        "contaId", aberta.contaId(),
                        "dataVencimento", aberta.dataVencimento().toString()),
                hoje, relogio.instant()));

        Fatura seguinte = faturas
                .buscarDaCompetencia(aberta.contaId(), aberta.competencia().plusMonths(1))
                .map(Fatura::abertaPeloCiclo)
                .orElseGet(() -> aberta.seguinte(ciclo, StatusDaFatura.ABERTA, relogio.instant()));

        Fatura gravada = faturas.salvar(seguinte);

        eventos.registrar(Evento.doSistema(ambienteId, autorId,
                TipoDeEvento.FATURA_ABERTA_PELO_CICLO, Alvo.fatura(gravada.id()),
                Evento.dados(
                        "competencia", gravada.competencia().toString(),
                        "contaId", gravada.contaId(),
                        "dataFechamento", gravada.dataFechamento().toString()),
                hoje, relogio.instant()));
    }

    private Long categoriaDeRolagem(Long ambienteId, Sentido sentido) {
        return categorias
                .buscarDeSistema(ambienteId, OperacaoDeSistema.ROLAGEM_DE_FATURA, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();
    }

}
