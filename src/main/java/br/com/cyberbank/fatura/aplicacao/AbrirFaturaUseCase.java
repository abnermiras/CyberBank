package br.com.cyberbank.fatura.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AbrirFaturaUseCase {

    private final FaturaRepository faturas;
    private final NumerosDasFaturas numerosDasFaturas;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AbrirFaturaUseCase(FaturaRepository faturas, NumerosDasFaturas numerosDasFaturas,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.faturas = faturas;
        this.numerosDasFaturas = numerosDasFaturas;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Fatura executar(Long ambienteId, Long autorId, Long faturaId) {
        Fatura fatura = faturas.buscarDoAmbiente(faturaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        var doCartao = numerosDasFaturas.daConta(fatura.contaId());

        FaturaComNumeros alvo = doCartao.stream()
                .filter(f -> f.fatura().id().equals(faturaId))
                .findFirst()
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Fatura reaberta = alvo.fatura()
                .abertaPeloUsuario(alvo.numeros(), ehAUltimaFechada(doCartao, faturaId));

        Optional<Fatura> seguinte = faturas.buscarAbertaDaConta(fatura.contaId());
        seguinte.ifPresent(aberta -> faturas.salvar(aberta.devolvidaAFutura()));

        Fatura gravada = faturas.salvar(reaberta);
        LocalDate hoje = diaLocal.hoje();

        eventos.registrar(Evento.doUsuario(ambienteId, autorId,
                TipoDeEvento.FATURA_ABERTA_PELO_USUARIO, Alvo.fatura(gravada.id()),
                Evento.dados(
                        "competencia", gravada.competencia().toString(),
                        "contaId", gravada.contaId(),
                        "aPagar", alvo.numeros().aPagarCentavos(),
                        "competenciaDevolvida", seguinte
                                .map(aberta -> aberta.competencia().toString()).orElse(null)),
                hoje, relogio.instant()));

        return gravada;
    }

    private static boolean ehAUltimaFechada(List<FaturaComNumeros> doCartao, Long faturaId) {

        return doCartao.stream()
                .filter(f -> f.fatura().status() == StatusDaFatura.FECHADA)
                .max(Comparator.comparing(f -> f.fatura().competencia()))
                .map(ultima -> ultima.fatura().id().equals(faturaId))
                .orElse(false);
    }
}
