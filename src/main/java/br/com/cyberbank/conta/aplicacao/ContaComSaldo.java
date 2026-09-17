package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.conta.dominio.Conta;

public record ContaComSaldo(Conta conta, long saldoRealizadoCentavos) {
}
