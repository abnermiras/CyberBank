package br.com.cyberbank.usuario.dominio;

import java.util.Optional;

public interface SessaoRepository {

    Sessao salvar(Sessao sessao);

    Optional<Sessao> buscarPorIdentificadorHash(String identificadorHash);

    void apagar(Long sessaoId);

    void apagarDoUsuario(Long usuarioId);
}
