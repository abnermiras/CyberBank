package br.com.cyberbank.conta.aplicacao;

import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ValorInformado;

public record AplicacaoNaReserva(Conta conta, ValorInformado valor, boolean desatualizada,
        long diasDeIdade) {
}
