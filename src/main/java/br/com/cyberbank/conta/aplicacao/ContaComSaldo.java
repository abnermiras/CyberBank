package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.conta.dominio.Conta;

public record ContaComSaldo(Conta conta, long saldoRealizadoCentavos,
        long previstoNoHorizonteCentavos, CompartilhadaDe compartilhadaDe) {

    public record CompartilhadaDe(Long ambienteId, String nome) {
    }

    public ContaComSaldo(Conta conta, long saldoRealizadoCentavos,
            long previstoNoHorizonteCentavos) {
        this(conta, saldoRealizadoCentavos, previstoNoHorizonteCentavos, null);
    }

    public long saldoProjetadoCentavos() {
        return saldoRealizadoCentavos + previstoNoHorizonteCentavos;
    }
}
