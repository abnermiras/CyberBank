package br.com.cyberbank.comum.configuracao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoDeTempo {

    /**
     * INSTANTE em UTC (regra 5 do CLAUDE.md). O dia local de Brasilia e outra coisa, e entra
     * onde houver data de dominio — que nesta fatia ainda nao existe.
     */
    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }
}
