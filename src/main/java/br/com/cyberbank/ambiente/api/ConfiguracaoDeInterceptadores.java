package br.com.cyberbank.ambiente.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** A ordem importa: sem saber QUEM esta falando, nao ha como validar acesso a ambiente nenhum. */
@Configuration
public class ConfiguracaoDeInterceptadores implements WebMvcConfigurer {

    private final InterceptadorDeSessao sessao;
    private final InterceptadorDeAmbiente ambiente;

    public ConfiguracaoDeInterceptadores(InterceptadorDeSessao sessao, InterceptadorDeAmbiente ambiente) {
        this.sessao = sessao;
        this.ambiente = ambiente;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(sessao).addPathPatterns("/api/**").order(1);
        registro.addInterceptor(ambiente).addPathPatterns("/api/**").order(2);
    }
}
