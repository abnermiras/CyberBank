package br.com.cyberbank.lancamento.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarLancamentoUseCase {

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public EditarLancamentoUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            EscolhaDeCategoria escolhaDeCategoria, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public List<Lancamento> executar(Long ambienteId, Long autorId, Long lancamentoId,
            Long novaContaId, Long novaCategoriaId, Sentido novoSentido, Long novoValorCentavos,
            LocalDate novaDataEvento, LocalDate novaDataEfeito, String novaDescricao,
            Situacao novaSituacao) {

        Lancamento lancamento = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        escolhaDeCategoria.exigirEscolhivel(ambienteId, novaCategoriaId,
                novoSentido == null ? lancamento.sentido() : novoSentido);

        if (novaContaId != null) {
            Conta destino = contas.buscarDoAmbiente(novaContaId, ambienteId)
                    .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
            destino.exigirAtivaParaLancar();
        }

        List<Lancamento> corrigidos = lancamento.ehTransferencia()
                ? editarOParInteiro(ambienteId, lancamento, novoValorCentavos, novaDataEvento,
                        novaDataEfeito, novaDescricao, novaSituacao)
                : List.of(lancamentos.salvar(lancamento.corrigido(novaContaId, null,
                        novaCategoriaId, novoSentido, novoValorCentavos, novaDataEvento,
                        novaDataEfeito, novaDescricao, novaSituacao)));

        Lancamento depois = corrigidos.stream()
                .filter(corrigido -> corrigido.id().equals(lancamentoId))
                .findFirst()
                .orElse(corrigidos.getFirst());

        registrar(ambienteId, autorId, lancamento, depois);

        return corrigidos;
    }

    private void registrar(Long ambienteId, Long autorId, Lancamento antes, Lancamento depois) {
        Map<String, Object> mudancas = Evento.reunir(List.of(
                Evento.deParaDe("descricao", antes.descricao(), depois.descricao()),
                Evento.deParaDe("valor", antes.valorCentavos(), depois.valorCentavos()),
                Evento.deParaDe("sentido", antes.sentido().name(), depois.sentido().name()),
                Evento.deParaDe("situacao", antes.situacao().name(), depois.situacao().name()),
                Evento.deParaDe("contaId", antes.contaId(), depois.contaId()),
                Evento.deParaDe("categoriaId", antes.categoriaId(), depois.categoriaId()),
                Evento.deParaDe("dataEvento", antes.dataEvento().toString(),
                        depois.dataEvento().toString()),
                Evento.deParaDe("dataEfeito", antes.dataEfeito().toString(),
                        depois.dataEfeito().toString())));

        if (mudancas.isEmpty()) {
            return;
        }

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_EDITADO,
                Alvo.lancamento(depois.id()), mudancas, diaLocal.hoje(), relogio.instant()));
    }

    private List<Lancamento> editarOParInteiro(Long ambienteId, Lancamento umDosLados,
            Long novoValorCentavos, LocalDate novaDataEvento, LocalDate novaDataEfeito,
            String novaDescricao, Situacao novaSituacao) {

        return lancamentos.salvarTodos(
                lancamentos.listarDaTransferencia(umDosLados.transferenciaId(), ambienteId).stream()
                        .map(lado -> lado.corrigido(null, null, null, null, novoValorCentavos,
                                novaDataEvento, novaDataEfeito, novaDescricao, novaSituacao))
                        .toList());
    }
}
