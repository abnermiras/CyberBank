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
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarLancamentoUseCase {

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final MeioRepository meios;
    private final FaturaRepository faturas;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public EditarLancamentoUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            MeioRepository meios, FaturaRepository faturas,
            EscolhaDeCategoria escolhaDeCategoria, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.meios = meios;
        this.faturas = faturas;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public List<Lancamento> executar(Long ambienteId, Long autorId, Long lancamentoId,
            Long novaContaId, Long novoMeioId, Long novaCategoriaId, Sentido novoSentido,
            Long novoValorCentavos, LocalDate novaDataEvento, LocalDate novaDataEfeito,
            String novaDescricao, Situacao novaSituacao, Long novaFaturaId) {

        Lancamento lancamento = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        escolhaDeCategoria.exigirEscolhivel(ambienteId, novaCategoriaId,
                novoSentido == null ? lancamento.sentido() : novoSentido);

        List<Lancamento> corrigidos = lancamento.ehTransferencia()
                ? editarOParInteiro(ambienteId, lancamento, novoValorCentavos, novaDataEvento,
                        novaDescricao, novaSituacao)
                : List.of(lancamentos.salvar(corrigirUmSo(ambienteId, lancamento, novaContaId,
                        novoMeioId, novaCategoriaId, novoSentido, novoValorCentavos,
                        novaDataEvento, novaDataEfeito, novaDescricao, novaSituacao,
                        novaFaturaId)));

        Lancamento depois = corrigidos.stream()
                .filter(corrigido -> corrigido.id().equals(lancamentoId))
                .findFirst()
                .orElse(corrigidos.getFirst());

        registrar(ambienteId, autorId, lancamento, depois);

        return corrigidos;
    }

    private Lancamento corrigirUmSo(Long ambienteId, Lancamento lancamento, Long novaContaId,
            Long novoMeioId, Long novaCategoriaId, Sentido novoSentido, Long novoValorCentavos,
            LocalDate novaDataEvento, LocalDate novaDataEfeito, String novaDescricao,
            Situacao novaSituacao, Long novaFaturaId) {

        Meio meio = meioResultante(ambienteId, lancamento, novoMeioId);
        if (meio == null) {
            exigirContaDisponivel(ambienteId, novaContaId);
            return lancamento.corrigido(novaContaId, null, novaCategoriaId, novoSentido,
                    novoValorCentavos, novaDataEvento, novaDataEfeito, novaDescricao,
                    novaSituacao);
        }

        if (novaContaId != null) {
            meio.exigirDaConta(novaContaId);
        }
        if (!meio.id().equals(lancamento.meioId())) {
            meio.exigirAtivoParaLancar();
            exigirContaDisponivel(ambienteId, meio.contaId());
        }

        LocalDate dataEvento = novaDataEvento == null ? lancamento.dataEvento() : novaDataEvento;
        LocalDate dataEfeito = meio.dataEfeitoPara(dataEvento,
                dataEfeitoInformada(lancamento, meio, novaDataEfeito));

        Long faturaId = faturaResultante(ambienteId, lancamento, meio, novaFaturaId);

        Lancamento corrigido = lancamento
                .corrigido(meio.contaId(), meio.id(), novaCategoriaId, novoSentido,
                        novoValorCentavos, dataEvento, dataEfeito, novaDescricao, novaSituacao)
                .naFatura(faturaId);

        if (novaSituacao != null || lancamento.entraEmFatura() == corrigido.entraEmFatura()) {
            return corrigido;
        }
        return corrigido.comSituacao(corrigido.entraEmFatura()
                ? Situacao.PROVISIONADO
                : situacaoPelaData(dataEfeito));
    }

    private Long faturaResultante(Long ambienteId, Lancamento lancamento, Meio meio,
            Long novaFaturaId) {

        if (!meio.temFatura()) {
            return null;
        }
        if (novaFaturaId != null) {
            Fatura escolhida = faturas.buscarDoAmbiente(novaFaturaId, ambienteId)
                    .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
            if (!escolhida.contaId().equals(meio.contaId())) {
                throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
            }
            return escolhida.id();
        }
        if (lancamento.entraEmFatura() && meio.contaId().equals(lancamento.contaId())) {
            return lancamento.faturaId();
        }
        return faturas.buscarAbertaDaConta(meio.contaId())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();
    }

    private Situacao situacaoPelaData(LocalDate dataEfeito) {
        return dataEfeito.isAfter(diaLocal.hoje()) ? Situacao.PREVISTO : Situacao.REALIZADO;
    }

    private static LocalDate dataEfeitoInformada(Lancamento lancamento, Meio meio,
            LocalDate novaDataEfeito) {
        if (novaDataEfeito != null) {
            return novaDataEfeito;
        }
        return meio.tipo().separaAsDuasDatas() ? lancamento.dataEfeito() : null;
    }

    private Meio meioResultante(Long ambienteId, Lancamento lancamento, Long novoMeioId) {
        Long meioId = novoMeioId == null ? lancamento.meioId() : novoMeioId;
        if (meioId == null) {
            return null;
        }
        return meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
    }

    private void exigirContaDisponivel(Long ambienteId, Long contaId) {
        if (contaId == null) {
            return;
        }
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        conta.exigirAtivaParaLancar();
    }

    private void registrar(Long ambienteId, Long autorId, Lancamento antes, Lancamento depois) {
        Map<String, Object> mudancas = Evento.reunir(List.of(
                Evento.deParaDe("descricao", antes.descricao(), depois.descricao()),
                Evento.deParaDe("valor", antes.valorCentavos(), depois.valorCentavos()),
                Evento.deParaDe("sentido", antes.sentido().name(), depois.sentido().name()),
                Evento.deParaDe("situacao", antes.situacao().name(), depois.situacao().name()),
                Evento.deParaDe("contaId", antes.contaId(), depois.contaId()),
                Evento.deParaDe("meioId", antes.meioId(), depois.meioId()),
                Evento.deParaDe("categoriaId", antes.categoriaId(), depois.categoriaId()),
                Evento.deParaDe("dataEvento", antes.dataEvento().toString(),
                        depois.dataEvento().toString()),
                Evento.deParaDe("dataEfeito", antes.dataEfeito().toString(),
                        depois.dataEfeito().toString())));

        if (mudancas.isEmpty()) {
            return;
        }

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_EDITADO,
                Alvo.lancamento(depois.id()),
                Evento.reunir(List.of(Evento.dados("descricao", antes.descricao()), mudancas)),
                diaLocal.hoje(), relogio.instant()));
    }

    private List<Lancamento> editarOParInteiro(Long ambienteId, Lancamento umDosLados,
            Long novoValorCentavos, LocalDate novaDataEvento, String novaDescricao,
            Situacao novaSituacao) {

        return lancamentos.salvarTodos(
                lancamentos.listarDaTransferencia(umDosLados.transferenciaId(), ambienteId).stream()
                        .map(lado -> lado.corrigidoNaTransferencia(novoValorCentavos,
                                novaDataEvento, novaDescricao, novaSituacao))
                        .toList());
    }
}
