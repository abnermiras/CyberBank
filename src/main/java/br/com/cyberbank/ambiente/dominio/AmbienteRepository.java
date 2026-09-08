package br.com.cyberbank.ambiente.dominio;

import java.util.List;

public interface AmbienteRepository {

    Ambiente salvar(Ambiente ambiente);

    /** Os ambientes a que o usuario tem acesso, com o papel dele em cada um. Ordem: por id. */
    List<AcessoAoAmbiente> listarDoUsuario(Long usuarioId);
}
