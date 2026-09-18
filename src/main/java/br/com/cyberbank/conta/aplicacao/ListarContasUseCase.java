package br.com.cyberbank.conta.aplicacao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.lancamento.dominio.JanelaDoPrevisto;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.SaldoDeConta;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarContasUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final DiaLocal diaLocal;

    public ListarContasUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            DiaLocal diaLocal) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.diaLocal = diaLocal;
    }

    public JanelaDoPrevisto horizonte() {
        return JanelaDoPrevisto.doMesCorrente(diaLocal.hoje());
    }

    @Transactional(readOnly = true)
    public List<ContaComSaldo> executar(Long ambienteId, boolean inativas) {
        LocalDate hoje = diaLocal.hoje();

        Map<Long, Long> saldos = porConta(
                lancamentos.saldoRealizadoPorConta(ambienteId, hoje));
        Map<Long, Long> previstos = porConta(
                lancamentos.previstoPorConta(ambienteId, horizonte()));

        return contas.listarDoAmbiente(ambienteId).stream()
                .filter(conta -> inativas || !conta.inativa())
                .map(conta -> new ContaComSaldo(conta,
                        saldos.getOrDefault(conta.id(), 0L),
                        previstos.getOrDefault(conta.id(), 0L)))
                .toList();
    }

    private static Map<Long, Long> porConta(List<SaldoDeConta> saldos) {
        return saldos.stream()
                .collect(Collectors.toMap(SaldoDeConta::contaId, SaldoDeConta::saldoCentavos));
    }
}
