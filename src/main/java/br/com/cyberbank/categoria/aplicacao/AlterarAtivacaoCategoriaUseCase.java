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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarAtivacaoCategoriaUseCase {

    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AlterarAtivacaoCategoriaUseCase(CategoriaRepository categorias,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long autorId, Long categoriaId, boolean inativa) {
        Categoria categoria = categorias.buscarDoAmbiente(categoriaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (inativa == categoria.inativa()) {
            return categoria;
        }
        Categoria alterada = categorias.salvar(categoria.comAtivacao(inativa));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId,
                inativa ? TipoDeEvento.CATEGORIA_INATIVADA : TipoDeEvento.CATEGORIA_REATIVADA,
                Alvo.categoria(categoriaId), Evento.dados("nome", alterada.nome()),
                diaLocal.hoje(), relogio.instant()));

        return alterada;
    }
}
