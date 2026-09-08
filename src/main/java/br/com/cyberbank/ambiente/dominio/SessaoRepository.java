package br.com.cyberbank.ambiente.dominio;

import java.util.Optional;

public interface SessaoRepository {

    Sessao salvar(Sessao sessao);

    Optional<Sessao> buscarPorIdentificadorHash(String identificadorHash);

    void apagar(Long sessaoId);

    /** Trocar a senha derruba TODAS as sessoes do usuario, inclusive a que trocou (ADR-0009). */
    void apagarDoUsuario(Long usuarioId);
}
