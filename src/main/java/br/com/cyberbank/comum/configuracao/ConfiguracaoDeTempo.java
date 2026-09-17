package br.com.cyberbank.comum.configuracao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoDeTempo {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }
}
