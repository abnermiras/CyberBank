package br.com.cyberbank.compartilhamento.aplicacao;

import java.time.Instant;

public record CompartilhamentoDaConta(Long ambienteDestinoId, String ambienteDestinoNome,
        Instant criadoEm) {
}
