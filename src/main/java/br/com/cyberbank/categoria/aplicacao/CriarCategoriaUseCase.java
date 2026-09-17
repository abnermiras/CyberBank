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
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarCategoriaUseCase {

    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public CriarCategoriaUseCase(CategoriaRepository categorias, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Categoria executar(Long ambienteId, Long autorId, Long paiId, String nome,
            Sentido sentido, CorDeCategoria cor) {
        Categoria criada = paiId == null
                ? categorias.salvar(
                        Categoria.novaRaiz(ambienteId, nome, sentido, cor, relogio.instant()))
                : criarSob(ambienteId, paiId, nome, sentido);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.CATEGORIA_CRIADA,
                Alvo.categoria(criada.id()),
                Evento.dados(
                        "nome", criada.nome(),
                        "sentido", criada.sentido().name(),
                        "paiId", criada.paiId()),
                diaLocal.hoje(), relogio.instant()));

        return criada;
    }

    private Categoria criarSob(Long ambienteId, Long paiId, String nome, Sentido sentido) {
        Categoria pai = categorias.buscarDoAmbiente(paiId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        exigirSentidoCompativel(pai, sentido);
        return categorias.salvar(Categoria.novaSubcategoria(pai, nome, relogio.instant()));
    }

    private static void exigirSentidoCompativel(Categoria pai, Sentido sentido) {
        if (sentido != null && sentido != pai.sentido()) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("sentido", "HERDADO",
                    "A subcategoria herda o sentido da categoria raiz.")));
        }
    }
}
