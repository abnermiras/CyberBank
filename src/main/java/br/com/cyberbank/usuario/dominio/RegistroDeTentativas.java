package br.com.cyberbank.usuario.dominio;

import java.time.Instant;

public interface RegistroDeTentativas {

    Tentativas consultar(String chave);

    void registrarFalha(String chave, Instant agora);

    void limpar(String chave);
}
