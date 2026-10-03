package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.usuario.dominio.Avatar;

public record Membro(Acesso acesso, String nome, String email, Avatar avatar) {
}
