package br.com.cyberbank.conta.aplicacao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.ContaRepository;
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

    @Transactional(readOnly = true)
    public List<ContaComSaldo> executar(Long ambienteId, boolean inativas) {
        Map<Long, Long> saldos = lancamentos
                .saldoRealizadoPorConta(ambienteId, diaLocal.hoje()).stream()
                .collect(Collectors.toMap(SaldoDeConta::contaId, SaldoDeConta::saldoCentavos));

        return contas.listarDoAmbiente(ambienteId).stream()
                .filter(conta -> inativas || !conta.inativa())
                .map(conta -> new ContaComSaldo(conta, saldos.getOrDefault(conta.id(), 0L)))
                .toList();
    }
}
