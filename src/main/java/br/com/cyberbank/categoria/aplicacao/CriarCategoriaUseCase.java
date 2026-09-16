package br.com.cyberbank.categoria.aplicacao;

import java.time.Clock;
import java.util.List;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.CorDeCategoria;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Raiz ou subcategoria, e quem decide e o {@code paiId}. Toda a regra esta no dominio: aqui so
 * se busca o pai e se persiste.
 *
 * <p>NAO HA verificacao de nome repetido, e e de proposito: o doc dono aceita nomes iguais em
 * raizes diferentes ("Academia inativa sob Lazer e Academia ativa sob Saude sao duas
 * categorias, porque foram dois periodos da vida do usuario") e nao proibe irmas homonimas.
 * Regra que o doc nao decidiu nao se inventa aqui (docs/08-fluxos/novo-caso-de-uso.md).
 */
@Service
public class CriarCategoriaUseCase {

    private final CategoriaRepository categorias;
    private final Clock relogio;

    public CriarCategoriaUseCase(CategoriaRepository categorias, Clock relogio) {
        this.categorias = categorias;
        this.relogio = relogio;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long paiId, String nome, Sentido sentido,
            CorDeCategoria cor) {
        if (paiId == null) {
            return categorias.salvar(
                    Categoria.novaRaiz(ambienteId, nome, sentido, cor, relogio.instant()));
        }

        Categoria pai = categorias.buscarDoAmbiente(paiId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        exigirSentidoCompativel(pai, sentido);
        return categorias.salvar(Categoria.novaSubcategoria(pai, nome, relogio.instant()));
    }

    /**
     * "Subcategoria HERDA o sentido da raiz e nao pode divergir dele." O cliente pode omitir o
     * sentido — e o normal —, e se mandar um diferente do pai a resposta e recusa, nunca
     * correcao silenciosa: o sistema nao conserta valor informado pelo usuario (regra 7).
     */
    private static void exigirSentidoCompativel(Categoria pai, Sentido sentido) {
        if (sentido != null && sentido != pai.sentido()) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("sentido", "HERDADO",
                    "A subcategoria herda o sentido da categoria raiz.")));
        }
    }
}
