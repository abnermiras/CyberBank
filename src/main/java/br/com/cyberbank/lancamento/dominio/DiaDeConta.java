package br.com.cyberbank.lancamento.dominio;

import java.time.LocalDate;

public record DiaDeConta(Long contaId, LocalDate dia) {
}
