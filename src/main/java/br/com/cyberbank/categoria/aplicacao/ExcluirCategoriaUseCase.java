package br.com.cyberbank.categoria.aplicacao;

import java.time.Clock;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcluirCategoriaUseCase {

    private final CategoriaRepository categorias;
    private final LancamentoRepository lancamentos;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ExcluirCategoriaUseCase(CategoriaRepository categorias,
            LancamentoRepository lancamentos, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.categorias = categorias;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long ambienteId, Long autorId, Long categoriaId) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        categoria.exigirExcluivel();

        if (categorias.temFilhas(categoriaId, ambienteId)) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_COM_SUBCATEGORIA);
        }
        if (lancamentos.categoriaTemLancamento(categoriaId)) {
            throw new RegraDeDominioException(CodigoDeErro.CATEGORIA_COM_LANCAMENTO);
        }

        categorias.excluir(categoriaId, ambienteId);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.CATEGORIA_EXCLUIDA,
                Alvo.categoria(categoriaId), Evento.dados("nome", categoria.nome()),
                diaLocal.hoje(), relogio.instant()));
    }
}
