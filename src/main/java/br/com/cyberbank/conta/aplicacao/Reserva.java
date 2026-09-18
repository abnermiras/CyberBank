package br.com.cyberbank.conta.aplicacao;

import java.time.LocalDate;
import java.util.List;

public record Reserva(List<AplicacaoNaReserva> aplicacoes, long guardadoCentavos,
        long emCaixaCentavos, long patrimonioCentavos, LocalDate hoje) {

    public boolean algumaDesatualizada() {
        return aplicacoes.stream().anyMatch(AplicacaoNaReserva::desatualizada);
    }
}
