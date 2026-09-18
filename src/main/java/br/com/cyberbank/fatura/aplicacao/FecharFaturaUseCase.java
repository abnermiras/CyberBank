package br.com.cyberbank.fatura.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

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
import br.com.cyberbank.fatura.dominio.CicloDaFatura;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FecharFaturaUseCase {

    private final FaturaRepository faturas;
    private final ContaRepository contas;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public FecharFaturaUseCase(FaturaRepository faturas, ContaRepository contas,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.faturas = faturas;
        this.contas = contas;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Fatura executar(Long ambienteId, Long autorId, Long faturaId) {
        Fatura aberta = faturas.buscarDoAmbiente(faturaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Conta cartao = contas.buscarDoAmbiente(aberta.contaId(), ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        ContratoDeCartao contrato = cartao.exigirContratoDeCartao();

        Fatura fechada = faturas.salvar(aberta.fechada());
        LocalDate hoje = diaLocal.hoje();

        eventos.registrar(Evento.doUsuario(ambienteId, autorId,
                TipoDeEvento.FATURA_FECHADA_PELO_USUARIO, Alvo.fatura(fechada.id()),
                Evento.dados(
                        "competencia", fechada.competencia().toString(),
                        "contaId", fechada.contaId(),
                        "dataFechamento", fechada.dataFechamento().toString(),
                        "dataVencimento", fechada.dataVencimento().toString()),
                hoje, relogio.instant()));

        CicloDaFatura ciclo = new CicloDaFatura(contrato.diaVencimento(),
                contrato.diasAntesFechamento());

        Fatura seguinte = faturas
                .buscarDaCompetencia(fechada.contaId(), fechada.competencia().plusMonths(1))
                .map(Fatura::abertaPeloCiclo)
                .orElseGet(() -> fechada.seguinte(ciclo, StatusDaFatura.ABERTA,
                        relogio.instant()));

        Fatura gravada = faturas.salvar(seguinte);

        eventos.registrar(Evento.doSistema(ambienteId, autorId,
                TipoDeEvento.FATURA_ABERTA_PELO_CICLO, Alvo.fatura(gravada.id()),
                Evento.dados(
                        "competencia", gravada.competencia().toString(),
                        "contaId", gravada.contaId(),
                        "dataFechamento", gravada.dataFechamento().toString()),
                hoje, relogio.instant()));

        return fechada;
    }
}
