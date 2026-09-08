package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;

/**
 * O ambiente e o dono do dado; o usuario so tem ACESSO a ambientes
 * (docs/02-dominio/ambiente-financeiro.md).
 */
public record Ambiente(Long id, String nome, Long criadoPor, Instant criadoEm) {

    /** O nome com que o Ambiente Pessoal nasce no cadastro. Renomeavel dali em diante. */
    public static final String NOME_PADRAO = "Ambiente Pessoal";

    public static Ambiente pessoalDe(Long usuarioId, Instant agora) {
        return new Ambiente(null, NOME_PADRAO, usuarioId, agora);
    }
}
