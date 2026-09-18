package br.com.cyberbank.fatura.aplicacao;

import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerFaturasQueImportamUseCase {

    private final ContaRepository contas;
    private final NumerosDasFaturas numerosDasFaturas;
    private final LancamentoRepository lancamentos;
    private final DiaLocal diaLocal;

    public VerFaturasQueImportamUseCase(ContaRepository contas,
            NumerosDasFaturas numerosDasFaturas, LancamentoRepository lancamentos,
            DiaLocal diaLocal) {
        this.contas = contas;
        this.numerosDasFaturas = numerosDasFaturas;
        this.lancamentos = lancamentos;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public List<FaturaNaHome> executar(Long ambienteId) {
        List<FaturaNaHome> naHome = new ArrayList<>();

        for (Conta conta : contas.listarDoAmbiente(ambienteId)) {
            if (!conta.ehContratoDeCartao() || conta.inativa()) {
                continue;
            }
            FaturaComNumeros.aQueImporta(numerosDasFaturas.daConta(conta.id()))
                    .ifPresent(fatura -> naHome.add(new FaturaNaHome(conta, fatura,
                            -lancamentos.saldoRealizadoDaConta(conta.id(), diaLocal.hoje()))));
        }
        return naHome;
    }
}
