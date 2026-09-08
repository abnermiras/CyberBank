package br.com.cyberbank;

import br.com.cyberbank.comum.configuracao.VariaveisObrigatorias;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CyberbankApplication {

    public static void main(String[] args) {
        SpringApplication aplicacao = new SpringApplication(CyberbankApplication.class);
        // Antes de qualquer bean, e antes de tentar conectar em coisa nenhuma.
        aplicacao.addListeners(new VariaveisObrigatorias());
        aplicacao.run(args);
    }
}
