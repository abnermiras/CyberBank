package br.com.cyberbank.categoria.aplicacao;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Renomear e livre: o lancamento referencia a categoria por IDENTIDADE, entao o historico passa
 * a exibir o nome novo sem que nada seja reescrito (docs/02-dominio/categoria.md).
 */
@Service
public class RenomearCategoriaUseCase {

    private final CategoriaRepository categorias;

    public RenomearCategoriaUseCase(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long categoriaId, String nome) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return categorias.salvar(categoria.renomeada(nome));
    }
}
