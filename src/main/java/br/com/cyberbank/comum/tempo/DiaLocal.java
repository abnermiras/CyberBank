package br.com.cyberbank.comum.tempo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

@Component
public class DiaLocal {

    public static final String FUSO_DE_BRASILIA = "America/Sao_Paulo";

    public static final ZoneId BRASILIA = ZoneId.of(FUSO_DE_BRASILIA);

    private final Clock relogio;

    public DiaLocal(Clock relogio) {
        this.relogio = relogio;
    }

    public LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), BRASILIA);
    }
}
