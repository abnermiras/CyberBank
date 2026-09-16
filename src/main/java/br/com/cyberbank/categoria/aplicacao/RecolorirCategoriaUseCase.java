package br.com.cyberbank.categoria.aplicacao;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.CorDeCategoria;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecolorirCategoriaUseCase {

    private final CategoriaRepository categorias;

    public RecolorirCategoriaUseCase(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long categoriaId, CorDeCategoria cor) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return categorias.salvar(categoria.recolorida(cor));
    }
}
