package br.com.cyberbank.evento.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Evento(
        Long id,
        Long ambienteId,
        LocalDate dia,
        Instant instante,
        OrigemDeEvento origem,
        Long autorId,
        TipoDeEvento tipo,
        Alvo alvo,
        Map<String, Object> dados) {

    public Evento {
        Objects.requireNonNull(ambienteId);
        Objects.requireNonNull(dia);
        Objects.requireNonNull(instante);
        Objects.requireNonNull(autorId);
        Objects.requireNonNull(tipo);
        dados = dados == null || dados.isEmpty() ? null : Map.copyOf(dados);
    }

    public static Evento doUsuario(Long ambienteId, Long autorId, TipoDeEvento tipo, Alvo alvo,
            Map<String, Object> dados, LocalDate hoje, Instant agora) {

        if (tipo.doSistema()) {
            throw new IllegalArgumentException(tipo.name());
        }
        return new Evento(null, ambienteId, hoje, agora, OrigemDeEvento.USUARIO, autorId, tipo,
                alvo, dados);
    }

    public static Evento doSistema(Long ambienteId, Long donoDoAmbienteId, TipoDeEvento tipo,
            Alvo alvo, Map<String, Object> dados, LocalDate hoje, Instant agora) {

        if (!tipo.doSistema()) {
            throw new IllegalArgumentException(tipo.name());
        }
        return new Evento(null, ambienteId, hoje, agora, OrigemDeEvento.SISTEMA,
                donoDoAmbienteId, tipo, alvo, dados);
    }

    public static Map<String, Object> dados(Object... paresDeChaveEValor) {
        Map<String, Object> dados = new LinkedHashMap<>();
        for (int par = 0; par < paresDeChaveEValor.length; par += 2) {
            Object valor = paresDeChaveEValor[par + 1];
            if (valor != null) {
                dados.put(String.valueOf(paresDeChaveEValor[par]), valor);
            }
        }
        return dados;
    }

    public static Map<String, Object> deParaDe(String campo, Object antes, Object depois) {
        if (Objects.equals(antes, depois)) {
            return Map.of();
        }
        return dados(campo + "De", antes, campo + "Para", depois);
    }

    public static Map<String, Object> reunir(List<Map<String, Object>> partes) {
        Map<String, Object> tudo = new LinkedHashMap<>();
        partes.forEach(tudo::putAll);
        return tudo;
    }
}
