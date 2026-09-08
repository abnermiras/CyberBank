package br.com.cyberbank.comum.configuracao;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ConfiguracaoDePersistencia {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory fabrica) {
        return new GerenciadorDeTransacaoComContexto(fabrica);
    }
}
