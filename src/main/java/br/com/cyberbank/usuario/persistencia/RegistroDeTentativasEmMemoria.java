package br.com.cyberbank.usuario.persistencia;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.cyberbank.usuario.dominio.RegistroDeTentativas;
import br.com.cyberbank.usuario.dominio.Tentativas;

import org.springframework.stereotype.Component;

@Component
public class RegistroDeTentativasEmMemoria implements RegistroDeTentativas {

    private final Map<String, Tentativas> porChave = new ConcurrentHashMap<>();

    @Override
    public Tentativas consultar(String chave) {
        return porChave.getOrDefault(chave, Tentativas.NENHUMA);
    }

    @Override
    public void registrarFalha(String chave, Instant agora) {
        porChave.merge(chave, Tentativas.NENHUMA.maisUma(agora),
                (atual, nova) -> atual.maisUma(agora));
    }

    @Override
    public void limpar(String chave) {
        porChave.remove(chave);
    }
}
