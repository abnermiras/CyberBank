package br.com.cyberbank.categoria.dominio;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {

    List<Categoria> salvarTodas(List<Categoria> categorias);

    Categoria salvar(Categoria categoria);

    /**
     * O {@code ambienteId} vai junto de proposito. A politica de RLS ja filtra por ambiente
     * (V002), e este parametro e a SEGUNDA camada do ADR-0002: as duas erram juntas ou nao
     * erram. Categoria de outro ambiente responde vazio, e a borda traduz isso em 404 — a
     * mesma resposta de inexistente (docs/04-api/erros.md).
     */
    Optional<Categoria> buscarDoAmbiente(Long id, Long ambienteId);

    Optional<Categoria> buscarDeSistema(Long ambienteId, OperacaoDeSistema operacao, Sentido sentido);

    /** Todas as do ambiente, inclusive as de sistema e as inativas. Quem filtra e quem le. */
    List<Categoria> listarDoAmbiente(Long ambienteId);

    void excluir(Long id, Long ambienteId);

    /** Uma raiz com filha — de qualquer estado — nao se exclui: excluir a raiz orfanaria a filha. */
    boolean temFilhas(Long id, Long ambienteId);
}
