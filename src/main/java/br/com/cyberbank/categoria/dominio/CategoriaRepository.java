package br.com.cyberbank.categoria.dominio;

import java.util.List;

public interface CategoriaRepository {

    List<Categoria> salvarTodas(List<Categoria> categorias);

    /** Todas as do ambiente, inclusive as de sistema e as inativas. Quem filtra e quem le. */
    List<Categoria> listarDoAmbiente(Long ambienteId);
}
