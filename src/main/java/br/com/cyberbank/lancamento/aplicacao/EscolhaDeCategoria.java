package br.com.cyberbank.lancamento.aplicacao;

import java.util.List;
import java.util.stream.Stream;

import br.com.cyberbank.categoria.dominio.ArvoreDeCategorias;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.lancamento.dominio.Sentido;

import org.springframework.stereotype.Component;

@Component
public class EscolhaDeCategoria {

    private final CategoriaRepository categorias;

    public EscolhaDeCategoria(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    public void exigirEscolhivel(Long ambienteId, Long categoriaId, Sentido sentido) {
        if (categoriaId == null) {
            return;
        }

        ArvoreDeCategorias.No escolhida = achatar(
                ArvoreDeCategorias.montar(categorias.listarDoAmbiente(ambienteId))).stream()
                .filter(no -> categoriaId.equals(no.categoria().id()))
                .findFirst()
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (!escolhida.escolhivel()) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_NAO_ESCOLHIVEL);
        }
        if (sentido != null && !sentido.name().equals(escolhida.categoria().sentido().name())) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_DE_OUTRO_SENTIDO);
        }
    }

    private static List<ArvoreDeCategorias.No> achatar(List<ArvoreDeCategorias.No> arvore) {
        return arvore.stream()
                .flatMap(no -> Stream.concat(Stream.of(no), achatar(no.filhas()).stream()))
                .toList();
    }
}
