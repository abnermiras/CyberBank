package br.com.cyberbank.recorrencia.aplicacao;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.aplicacao.EscolhaDeCategoria;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarParcelamentoUseCase {

    private final ParcelamentoRepository parcelamentos;
    private final LancamentoRepository lancamentos;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public EditarParcelamentoUseCase(ParcelamentoRepository parcelamentos,
            LancamentoRepository lancamentos, EscolhaDeCategoria escolhaDeCategoria,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.parcelamentos = parcelamentos;
        this.lancamentos = lancamentos;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public ParcelamentoComParcelas executar(Long ambienteId, Long autorId, Long parcelamentoId,
            Long novoValorDaCompra, Long novaCategoriaId, String novaDescricao) {

        Parcelamento antes = parcelamentos.buscarDoAmbiente(parcelamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        escolhaDeCategoria.exigirEscolhivel(ambienteId, novaCategoriaId, Sentido.SAIDA);

        Parcelamento depois = parcelamentos.salvar(
                antes.corrigido(novoValorDaCompra, novaCategoriaId, novaDescricao));

        List<Long> valores = depois.valoresDasParcelas();
        List<Lancamento> atuais = lancamentos.listarDoParcelamento(parcelamentoId, ambienteId);

        List<Lancamento> redistribuidas = new ArrayList<>(atuais.size());
        for (int parcela = 0; parcela < atuais.size(); parcela++) {
            redistribuidas.add(atuais.get(parcela).comValorDaParcela(valores.get(parcela),
                    novaCategoriaId, novaDescricao == null ? null : depois.descricao()));
        }

        List<Lancamento> gravadas = lancamentos.salvarTodos(redistribuidas);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.SERIE_ALTERADA,
                Alvo.serie(depois.id()),
                Evento.reunir(List.of(
                        Evento.dados(
                                "descricao", depois.descricao(),
                                "parcelas", depois.parcelas(),
                                "contaId", depois.contaId()),
                        Evento.deParaDe("valor", antes.valorDaCompraCentavos(),
                                depois.valorDaCompraCentavos()))),
                diaLocal.hoje(), relogio.instant()));

        return new ParcelamentoComParcelas(depois, gravadas);
    }
}
