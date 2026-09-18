package br.com.cyberbank.conta.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record ValorInformado(long saldoCentavos, LocalDate informadoEm) {

    public static final int DIAS_ATE_ENVELHECER = 30;

    public boolean nuncaInformado() {
        return informadoEm == null;
    }

    public boolean desatualizadoEm(LocalDate hoje) {
        return !nuncaInformado() && diasDeIdadeEm(hoje) > DIAS_ATE_ENVELHECER;
    }

    public long diasDeIdadeEm(LocalDate hoje) {
        return nuncaInformado() ? 0 : ChronoUnit.DAYS.between(informadoEm, hoje);
    }
}
