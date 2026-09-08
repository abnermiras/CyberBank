package br.com.cyberbank.ambiente.persistencia;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.cyberbank.ambiente.dominio.RegistroDeTentativas;
import br.com.cyberbank.ambiente.dominio.Tentativas;

import org.springframework.stereotype.Component;

/**
 * As falhas de login vivem na memoria do processo, e o preco esta dito: REINICIAR ZERA A
 * CONTAGEM, e um segundo processo teria a sua.
 *
 * <p>E aceitavel aqui pelo que a contagem protege — ela atrasa e bloqueia forca bruta, nao
 * autoriza ninguem. A sessao nao podia ser assim (ADR-0009) porque sessao em memoria SOME no
 * restart e derruba todo mundo; contagem de tentativa que some no restart devolve, no pior
 * caso, algumas tentativas ao atacante.
 *
 * <p>Vira tabela no dia em que a contencao do cadastro aberto entrar
 * (docs/01-arquitetura/seguranca.md).
 */
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
