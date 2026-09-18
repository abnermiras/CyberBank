package br.com.cyberbank.recorrencia.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.ContratoDeCartao;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.CicloDaFatura;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.fatura.dominio.StatusDaFatura;
import br.com.cyberbank.lancamento.aplicacao.EscolhaDeCategoria;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelarCompraUseCase {

    private final ParcelamentoRepository parcelamentos;
    private final LancamentoRepository lancamentos;
    private final MeioRepository meios;
    private final ContaRepository contas;
    private final FaturaRepository faturas;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public ParcelarCompraUseCase(ParcelamentoRepository parcelamentos,
            LancamentoRepository lancamentos, MeioRepository meios, ContaRepository contas,
            FaturaRepository faturas, EscolhaDeCategoria escolhaDeCategoria,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.parcelamentos = parcelamentos;
        this.lancamentos = lancamentos;
        this.meios = meios;
        this.contas = contas;
        this.faturas = faturas;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public ParcelamentoComParcelas executar(Long ambienteId, Long autorId, Long meioId,
            Long categoriaId, Long valorDaCompraCentavos, Integer parcelas, LocalDate dataDaCompra,
            String descricao) {

        Meio cartao = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        cartao.exigirAtivoParaLancar();

        if (!cartao.temFatura()) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_NAO_PARCELA);
        }

        Conta conta = contas.buscarDoAmbiente(cartao.contaId(), ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        conta.exigirAtivaParaLancar();

        escolhaDeCategoria.exigirEscolhivel(ambienteId, categoriaId, Sentido.SAIDA);

        Parcelamento gravado = parcelamentos.salvar(Parcelamento.novo(ambienteId, conta.id(),
                cartao.id(), categoriaId, valorDaCompraCentavos, parcelas, dataDaCompra,
                descricao, relogio.instant()));

        List<Lancamento> criadas = lancamentos.salvarTodos(Lancamento.parcelasDeUmaCompra(
                ambienteId, conta.id(), cartao.id(), categoriaId, autorId,
                gravado.valoresDasParcelas(), faturasDasParcelas(conta, gravado.parcelas()),
                gravado.dataDaCompra(), gravado.descricao(), gravado.id(), relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.SERIE_CRIADA,
                Alvo.serie(gravado.id()),
                Evento.dados(
                        "descricao", gravado.descricao(),
                        "valor", gravado.valorDaCompraCentavos(),
                        "sentido", Sentido.SAIDA.name(),
                        "parcelas", gravado.parcelas(),
                        "contaId", conta.id(),
                        "primeiraParcela", criadas.getFirst().valorCentavos()),
                diaLocal.hoje(), relogio.instant()));

        return new ParcelamentoComParcelas(gravado, criadas);
    }

    private List<Long> faturasDasParcelas(Conta cartao, int parcelas) {
        ContratoDeCartao contrato = cartao.exigirContratoDeCartao();
        CicloDaFatura ciclo = new CicloDaFatura(contrato.diaVencimento(),
                contrato.diasAntesFechamento());

        Fatura aberta = faturas.buscarAbertaDaConta(cartao.id())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        List<Long> destino = new ArrayList<>(parcelas);
        destino.add(aberta.id());

        for (int parcela = 2; parcela <= parcelas; parcela++) {
            YearMonth competencia = aberta.competencia().plusMonths(parcela - 1L);
            destino.add(faturas.buscarDaCompetencia(cartao.id(), competencia)
                    .orElseGet(() -> faturas.salvar(Fatura.nova(cartao.ambienteId(), cartao.id(),
                            ciclo, competencia, StatusDaFatura.FUTURA, relogio.instant())))
                    .id());
        }
        return destino;
    }
}
