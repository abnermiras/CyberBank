package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.Convite;

public record ConviteRecebido(Convite convite, String ambienteNome, String convidadoPorNome) {
}
