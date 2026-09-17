package br.com.cyberbank.ambiente.dominio;

import java.util.List;

public interface AmbienteRepository {

    Ambiente salvar(Ambiente ambiente);

    List<AcessoAoAmbiente> listarDoUsuario(Long usuarioId);

    List<AmbienteDaRotina> listarParaRotina();
}
