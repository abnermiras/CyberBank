package br.com.cyberbank.usuario.dominio;

import java.util.List;
import java.util.Optional;

public interface SessaoRepository {

    Sessao salvar(Sessao sessao);

    Optional<Sessao> buscar(Long sessaoId);

    Optional<Sessao> buscarPorIdentificadorHash(String identificadorHash);

    List<Sessao> listarDoUsuario(Long usuarioId);

    void apagar(Long sessaoId);

    void apagarDoUsuario(Long usuarioId);
}
