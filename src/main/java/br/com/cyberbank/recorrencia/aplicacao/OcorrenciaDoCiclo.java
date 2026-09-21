package br.com.cyberbank.recorrencia.aplicacao;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Optional;

import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;

import org.springframework.stereotype.Component;

@Component
public class OcorrenciaDoCiclo {

    private final LancamentoRepository lancamentos;
    private final Clock relogio;

    public OcorrenciaDoCiclo(LancamentoRepository lancamentos, Clock relogio) {
        this.lancamentos = lancamentos;
        this.relogio = relogio;
    }

    public Optional<Lancamento> lancarSeFaltar(Recorrencia recorrencia, Long autorId,
            Long faturaId, YearMonth competencia) {

        if (!recorrencia.valeNaCompetencia(competencia)
                || lancamentos.temOcorrenciaNaFatura(recorrencia.id(), faturaId)) {
            return Optional.empty();
        }

        return Optional.of(lancamentos.salvar(Lancamento.deOcorrenciaNoCredito(
                recorrencia.ambienteId(), recorrencia.contaId(), recorrencia.meioId(),
                recorrencia.categoriaId(), autorId, recorrencia.valorCentavos(),
                recorrencia.dataNaCompetencia(competencia), recorrencia.descricao(), faturaId,
                recorrencia.id(), relogio.instant())));
    }
}
