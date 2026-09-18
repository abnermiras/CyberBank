package br.com.cyberbank.conta.aplicacao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.TipoDeConta;
import br.com.cyberbank.conta.dominio.ValorInformado;
import br.com.cyberbank.lancamento.dominio.DiaDeConta;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerReservaUseCase {

    private static final List<OperacaoDeSistema> DIZEM_QUANTO_VALE =
            List.of(OperacaoDeSistema.RENDIMENTO, OperacaoDeSistema.SALDO_DE_ABERTURA);

    private final ListarContasUseCase listarContas;
    private final CategoriaRepository categorias;
    private final LancamentoRepository lancamentos;
    private final DiaLocal diaLocal;

    public VerReservaUseCase(ListarContasUseCase listarContas, CategoriaRepository categorias,
            LancamentoRepository lancamentos, DiaLocal diaLocal) {
        this.listarContas = listarContas;
        this.categorias = categorias;
        this.lancamentos = lancamentos;
        this.diaLocal = diaLocal;
    }

    @Transactional(readOnly = true)
    public Reserva executar(Long ambienteId) {
        LocalDate hoje = diaLocal.hoje();
        List<ContaComSaldo> contas = listarContas.executar(ambienteId, true);

        Map<Long, LocalDate> informados = lancamentos
                .ultimoValorInformadoPorConta(ambienteId, idsQueDizemQuantoVale(ambienteId))
                .stream()
                .collect(Collectors.toMap(DiaDeConta::contaId, DiaDeConta::dia));

        List<AplicacaoNaReserva> aplicacoes = contas.stream()
                .filter(comSaldo -> comSaldo.conta().tipo() == TipoDeConta.APLICACAO)
                .map(comSaldo -> paraReserva(comSaldo, informados, hoje))
                .toList();

        return new Reserva(
                aplicacoes,
                contas.stream()
                        .filter(comSaldo -> comSaldo.conta().entraEmCaixa())
                        .filter(comSaldo -> !comSaldo.conta().inativa())
                        .map(ContaComSaldo::conta)
                        .toList(),
                somar(contas, comSaldo -> !comSaldo.conta().entraNoFluxoDeCaixa()),
                somar(contas, comSaldo -> comSaldo.conta().entraEmCaixa()),
                somar(contas, comSaldo -> true),
                hoje);
    }

    private static AplicacaoNaReserva paraReserva(ContaComSaldo comSaldo,
            Map<Long, LocalDate> informados, LocalDate hoje) {

        ValorInformado valor = new ValorInformado(comSaldo.saldoRealizadoCentavos(),
                informados.get(comSaldo.conta().id()));

        return new AplicacaoNaReserva(comSaldo.conta(), valor, valor.desatualizadoEm(hoje),
                valor.diasDeIdadeEm(hoje));
    }

    private List<Long> idsQueDizemQuantoVale(Long ambienteId) {
        return DIZEM_QUANTO_VALE.stream()
                .flatMap(operacao -> List.of(Sentido.ENTRADA, Sentido.SAIDA).stream()
                        .map(sentido -> categorias.buscarDeSistema(ambienteId, operacao, sentido)))
                .flatMap(Optional::stream)
                .map(categoria -> categoria.id())
                .toList();
    }

    private static long somar(List<ContaComSaldo> contas, Predicate<ContaComSaldo> criterio) {
        return contas.stream().filter(criterio)
                .mapToLong(ContaComSaldo::saldoRealizadoCentavos).sum();
    }
}
