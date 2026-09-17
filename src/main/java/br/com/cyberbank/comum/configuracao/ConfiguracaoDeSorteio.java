package br.com.cyberbank.comum.configuracao;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoDeSorteio {

    @Bean
    public RandomGenerator sorteio() {
        return new SecureRandom();
    }
}
