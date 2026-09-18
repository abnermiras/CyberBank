package br.com.cyberbank.conta.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
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
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.aplicacao.CadastrarMeioUseCase;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AbrirContaUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final CategoriaRepository categorias;
    private final FaturaRepository faturas;
    private final CadastrarMeioUseCase cadastrarMeio;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AbrirContaUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            CategoriaRepository categorias, FaturaRepository faturas,
            CadastrarMeioUseCase cadastrarMeio, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.categorias = categorias;
        this.faturas = faturas;
        this.cadastrarMeio = cadastrarMeio;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public ContaComSaldo executar(Long ambienteId, Long autorId, AberturaDeConta abertura) {
        LocalDate hoje = diaLocal.hoje();
        Long saldoInicialCentavos = abertura.saldoInicialCentavos();

        ContratoDeCartao contrato = contratoDe(ambienteId, abertura, hoje);

        Conta conta = Conta.nova(ambienteId, abertura.nome(), abertura.tipo(), contrato,
                relogio.instant());
        conta.exigirSaldoInicialCompativel(saldoInicialCentavos);

        CicloDaFatura ciclo = conta.ehContratoDeCartao()
                ? new CicloDaFatura(contrato.diaVencimento(), contrato.diasAntesFechamento())
                : null;

        Conta gravada = contas.salvar(conta);

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.CONTA_CRIADA,
                Alvo.conta(gravada.id()),
                Evento.dados(
                        "nome", gravada.nome(),
                        "tipo", gravada.tipo().name(),
                        "saldoInicial", saldoInicialCentavos),
                hoje, relogio.instant()));

        if (ciclo != null) {
            faturas.salvar(Fatura.doCicloCorrente(ambienteId, gravada.id(), ciclo, hoje,
                    relogio.instant()));
        }

        abertura.meios().stream().distinct().forEach(tipoDeMeio ->
                cadastrarMeio.executar(ambienteId, autorId, null, tipoDeMeio, gravada.id()));

        abertura.cartoes().forEach(cartao ->
                cadastrarMeio.executar(ambienteId, autorId, cartao, TipoDeMeio.CREDITO,
                        gravada.id()));

        if (saldoInicialCentavos == null || saldoInicialCentavos == 0) {
            return new ContaComSaldo(gravada, 0, 0);
        }

        Sentido sentido = saldoInicialCentavos >= 0 ? Sentido.ENTRADA : Sentido.SAIDA;

        Long categoriaDeAbertura = categorias
                .buscarDeSistema(ambienteId, OperacaoDeSistema.SALDO_DE_ABERTURA, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();

        lancamentos.salvarTodos(List.of(Lancamento.deAbertura(ambienteId, gravada.id(),
                categoriaDeAbertura, autorId, saldoInicialCentavos, hoje, relogio.instant())));

        return new ContaComSaldo(gravada, saldoInicialCentavos, 0);
    }

    private ContratoDeCartao contratoDe(Long ambienteId, AberturaDeConta abertura,
            LocalDate hoje) {

        if (!abertura.temCicloDeFatura()) {
            return null;
        }
        if (abertura.contaPagadoraPadraoId() != null
                && contas.buscarDoAmbiente(abertura.contaPagadoraPadraoId(), ambienteId)
                        .isEmpty()) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        return new ContratoDeCartao(abertura.limiteCentavos(),
                abertura.limiteCentavos() == null ? null : hoje,
                abertura.diaVencimento(), abertura.diasAntesFechamento(),
                abertura.contaPagadoraPadraoId());
    }
}
