package br.com.cyberbank.conta.aplicacao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
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
    private final AmbienteRepository ambientes;
    private final DiaLocal diaLocal;

    public ListarContasUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            AmbienteRepository ambientes, DiaLocal diaLocal) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.ambientes = ambientes;
        this.diaLocal = diaLocal;
    }

    public JanelaDoPrevisto horizonte() {
        return JanelaDoPrevisto.doMesCorrente(diaLocal.hoje());
    }

    @Transactional(readOnly = true)
    public List<ContaComSaldo> executar(Long ambienteId, boolean inativas) {
        LocalDate hoje = diaLocal.hoje();

        List<Conta> acessiveis = contas.listarAcessiveis(ambienteId).stream()
                .filter(conta -> inativas || !conta.inativa())
                .toList();
        List<Long> ids = acessiveis.stream().map(Conta::id).toList();

        Map<Long, Long> saldos = porConta(lancamentos.saldoRealizadoDasContas(ids, hoje));
        Map<Long, Long> previstos = porConta(lancamentos.previstoDasContas(ids, horizonte()));
        Map<Long, String> nomesDasOrigens = new HashMap<>();

        return acessiveis.stream()
                .map(conta -> new ContaComSaldo(conta,
                        saldos.getOrDefault(conta.id(), 0L),
                        previstos.getOrDefault(conta.id(), 0L),
                        compartilhadaDe(conta, ambienteId, nomesDasOrigens)))
                .toList();
    }

    private ContaComSaldo.CompartilhadaDe compartilhadaDe(Conta conta, Long ambienteId,
            Map<Long, String> nomesDasOrigens) {
        if (conta.ambienteId().equals(ambienteId)) {
            return null;
        }
        String nome = nomesDasOrigens.computeIfAbsent(conta.ambienteId(),
                origem -> ambientes.buscar(origem).map(Ambiente::nome).orElse(""));
        return new ContaComSaldo.CompartilhadaDe(conta.ambienteId(), nome);
    }

    private static Map<Long, Long> porConta(List<SaldoDeConta> saldos) {
        return saldos.stream()
                .collect(Collectors.toMap(SaldoDeConta::contaId, SaldoDeConta::saldoCentavos));
    }
}
