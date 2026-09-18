package br.com.cyberbank.fatura.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Component;

@Component
public class EncerramentoDaFatura {

    private final NumerosDasFaturas numerosDasFaturas;
    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final Clock relogio;

    public EncerramentoDaFatura(NumerosDasFaturas numerosDasFaturas,
            LancamentoRepository lancamentos, EventoRepository eventos, Clock relogio) {
        this.numerosDasFaturas = numerosDasFaturas;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public boolean liquidarSeEncerrou(Long ambienteId, Long autorId, Fatura fatura,
            LocalDate hoje) {

        FaturaComNumeros comNumeros = numerosDasFaturas.de(fatura);
        if (!comNumeros.encerrouEAindaNaoLiquidou()) {
            return false;
        }
        liquidar(ambienteId, autorId, fatura, hoje);
        return true;
    }

    public void liquidar(Long ambienteId, Long autorId, Fatura fatura, LocalDate hoje) {
        lancamentos.liquidarProvisionadosDaFatura(fatura.id());

        eventos.registrar(Evento.doSistema(ambienteId, autorId, TipoDeEvento.FATURA_ENCERRADA,
                Alvo.fatura(fatura.id()),
                Evento.dados(
                        "competencia", fatura.competencia().toString(),
                        "contaId", fatura.contaId()),
                hoje, relogio.instant()));
    }
}
