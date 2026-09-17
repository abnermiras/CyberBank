package br.com.cyberbank.comum.tempo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

@Component
public class DiaLocal {

    public static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    private final Clock relogio;

    public DiaLocal(Clock relogio) {
        this.relogio = relogio;
    }

    public LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), BRASILIA);
    }
}
