package br.com.cyberbank.categoria.aplicacao;

import java.util.List;

import br.com.cyberbank.categoria.dominio.ArvoreDeCategorias;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarCategoriasUseCase {

    private final CategoriaRepository categorias;

    public ListarCategoriasUseCase(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<ArvoreDeCategorias.No> executar(Long ambienteId, boolean comSistema, boolean comInativas) {
        List<ArvoreDeCategorias.No> arvore = ArvoreDeCategorias.montar(categorias.listarDoAmbiente(ambienteId));
        if (!comSistema) {
            arvore = ArvoreDeCategorias.semSistema(arvore);
        }
        if (!comInativas) {
            arvore = ArvoreDeCategorias.semInativas(arvore);
        }
        return arvore;
    }
}
