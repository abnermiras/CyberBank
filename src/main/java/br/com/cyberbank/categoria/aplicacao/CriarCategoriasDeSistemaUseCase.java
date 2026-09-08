package br.com.cyberbank.categoria.aplicacao;

import java.time.Clock;
import java.util.List;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Todo ambiente nasce com o jogo completo de categorias de sistema, no mesmo ato que o cria —
 * e com NENHUMA categoria de usuario" (docs/02-dominio/categoria.md).
 */
@Service
public class CriarCategoriasDeSistemaUseCase {

    private final CategoriaRepository categorias;
    private final Clock relogio;

    public CriarCategoriasDeSistemaUseCase(CategoriaRepository categorias, Clock relogio) {
        this.categorias = categorias;
        this.relogio = relogio;
    }

    @Transactional
    public List<Categoria> executar(Long ambienteId) {
        return categorias.salvarTodas(Categoria.jogoDeSistema(ambienteId, relogio.instant()));
    }
}
