package br.com.cyberbank.categoria.aplicacao;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inativar e reativar sao a mesma escrita em direcoes opostas, e por isso um caso de uso so.
 *
 * <p>Ele grava em UMA linha, sempre. Inativar uma raiz esconde as filhas na leitura
 * ({@code ArvoreDeCategorias}) e nao toca no campo {@code inativa} delas — cascata na escrita
 * destruiria quais filhas o usuario ja tinha inativado a mao, e reativar a raiz nao teria como
 * reconstruir o jogo que estava ativo antes.
 */
@Service
public class AlterarAtivacaoCategoriaUseCase {

    private final CategoriaRepository categorias;

    public AlterarAtivacaoCategoriaUseCase(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long categoriaId, boolean inativa) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return categorias.salvar(categoria.comAtivacao(inativa));
    }
}
