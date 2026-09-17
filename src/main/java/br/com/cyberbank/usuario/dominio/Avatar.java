package br.com.cyberbank.usuario.dominio;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

public enum Avatar {

    VISOR, OLHO, GATO, CAVEIRA, DRONE, CIRCUITO, ONDA, TORRE, PRISMA, ROBO;

    public static Avatar sortear(RandomGenerator sorteio) {
        return values()[sorteio.nextInt(values().length)];
    }

    public static boolean existe(String nome) {
        return Arrays.stream(values()).anyMatch(avatar -> avatar.name().equals(nome));
    }

    public static List<String> nomes() {
        return Arrays.stream(values()).map(Avatar::name).toList();
    }
}
