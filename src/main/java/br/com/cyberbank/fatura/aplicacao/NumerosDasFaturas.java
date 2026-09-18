package br.com.cyberbank.fatura.aplicacao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.NumerosDaFatura;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.TotaisDeFatura;

import org.springframework.stereotype.Component;

@Component
public class NumerosDasFaturas {

    private final FaturaRepository faturas;
    private final LancamentoRepository lancamentos;

    public NumerosDasFaturas(FaturaRepository faturas, LancamentoRepository lancamentos) {
        this.faturas = faturas;
        this.lancamentos = lancamentos;
    }

    public List<FaturaComNumeros> daConta(Long contaId) {
        return juntar(faturas.listarDaConta(contaId));
    }

    public FaturaComNumeros de(Fatura fatura) {
        return juntar(List.of(fatura)).getFirst();
    }

    public long aPagarDasQueVencemEntre(Long ambienteId, LocalDate de, LocalDate ate) {
        return juntar(faturas.listarDoAmbienteVencendoEntre(ambienteId, de, ate)).stream()
                .mapToLong(fatura -> fatura.numeros().aPagarDescontandoOAgendadoCentavos())
                .sum();
    }

    private List<FaturaComNumeros> juntar(List<Fatura> faturasDaConta) {
        Map<Long, TotaisDeFatura> totais = new HashMap<>();
        for (TotaisDeFatura linha : lancamentos.totaisDasFaturas(
                faturasDaConta.stream().map(Fatura::id).toList())) {
            totais.put(linha.faturaId(), linha);
        }

        return faturasDaConta.stream().map(fatura -> {
            TotaisDeFatura linha = totais.get(fatura.id());
            return linha == null
                    ? new FaturaComNumeros(fatura, NumerosDaFatura.VAZIA, false)
                    : new FaturaComNumeros(fatura,
                            new NumerosDaFatura(linha.totalCentavos(), linha.pagoCentavos(),
                                    linha.roladoCentavos(), linha.agendadoCentavos()),
                            linha.temProvisionado());
        }).toList();
    }
}
