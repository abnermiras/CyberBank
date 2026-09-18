package br.com.cyberbank.conta.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.conta.dominio.Conta;

public record Reserva(List<AplicacaoNaReserva> aplicacoes, List<Conta> contasDeCaixa,
        long guardadoCentavos, long emCaixaCentavos, long patrimonioCentavos, LocalDate hoje) {

    public boolean algumaDesatualizada() {
        return aplicacoes.stream().anyMatch(AplicacaoNaReserva::desatualizada);
    }
}
