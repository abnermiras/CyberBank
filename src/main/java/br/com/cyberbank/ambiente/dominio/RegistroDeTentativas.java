package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;

/** Onde as falhas de login sao contadas, por conta e por origem. */
public interface RegistroDeTentativas {

    Tentativas consultar(String chave);

    void registrarFalha(String chave, Instant agora);

    void limpar(String chave);
}
