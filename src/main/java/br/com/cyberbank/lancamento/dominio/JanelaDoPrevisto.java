package br.com.cyberbank.lancamento.dominio;

import java.time.LocalDate;
import java.time.YearMonth;

public record JanelaDoPrevisto(LocalDate de, LocalDate ate) {

    public static JanelaDoPrevisto doMes(LocalDate hoje, YearMonth mes) {
        LocalDate primeiroDia = mes.atDay(1);
        return new JanelaDoPrevisto(hoje.isAfter(primeiroDia) ? hoje : primeiroDia,
                mes.atEndOfMonth());
    }

    public static JanelaDoPrevisto doMesCorrente(LocalDate hoje) {
        return doMes(hoje, YearMonth.from(hoje));
    }

    public boolean vazia() {
        return de.isAfter(ate);
    }
}
