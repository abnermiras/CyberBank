package br.com.cyberbank.lancamento.aplicacao;

import java.util.Map;

import br.com.cyberbank.lancamento.dominio.Pagina;

public record ExtratoLido(Pagina pagina, Map<Long, String> ambientesDeFora) {
}
