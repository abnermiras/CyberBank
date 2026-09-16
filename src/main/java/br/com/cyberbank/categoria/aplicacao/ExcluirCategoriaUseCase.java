package br.com.cyberbank.categoria.aplicacao;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Excluir e o caminho de quem NUNCA teve lancamento; com historico, o caminho e inativar.
 *
 * <p>FALTA AQUI a verificacao de {@code CATEGORIA_COM_LANCAMENTO}, e falta por um motivo
 * concreto: a tabela {@code lancamento} ainda nao existe. Hoje nenhuma categoria tem
 * lancamento, entao "so se nunca teve lancamento" e verdade para todas e o comportamento esta
 * certo. No dia em que o assunto {@code lancamento} nascer, a verificacao entra AQUI — e neste
 * caso de uso, nao no dominio: quem junta dois assuntos e a camada de aplicacao (ADR-0010).
 */
@Service
public class ExcluirCategoriaUseCase {

    private final CategoriaRepository categorias;

    public ExcluirCategoriaUseCase(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public void executar(Long ambienteId, Long categoriaId) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        categoria.exigirExcluivel();

        if (categorias.temFilhas(categoriaId, ambienteId)) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_COM_SUBCATEGORIA);
        }

        categorias.excluir(categoriaId, ambienteId);
    }
}
