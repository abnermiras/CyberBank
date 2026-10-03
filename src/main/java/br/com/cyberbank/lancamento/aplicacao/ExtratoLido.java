package br.com.cyberbank.lancamento.aplicacao;

import java.util.Map;

import br.com.cyberbank.lancamento.dominio.Pagina;
import br.com.cyberbank.usuario.dominio.Avatar;

public record ExtratoLido(Pagina pagina, Map<Long, String> ambientesDeFora,
        Map<Long, AutorDoLancamento> autores) {

    public record AutorDoLancamento(String nome, Avatar avatar) {
    }
}
