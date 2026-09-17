package br.com.cyberbank.conta.aplicacao;

import java.time.Clock;
import java.util.List;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.TipoDeConta;
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
    private final CadastrarMeioUseCase cadastrarMeio;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AbrirContaUseCase(ContaRepository contas, LancamentoRepository lancamentos,
            CategoriaRepository categorias, CadastrarMeioUseCase cadastrarMeio,
            DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.categorias = categorias;
        this.cadastrarMeio = cadastrarMeio;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public ContaComSaldo executar(Long ambienteId, Long autorId, String nome, TipoDeConta tipo,
            Long saldoInicialCentavos, List<TipoDeMeio> meios) {

        Conta conta = Conta.nova(ambienteId, nome, tipo, relogio.instant());
        conta.exigirSaldoInicialCompativel(saldoInicialCentavos);

        Conta gravada = contas.salvar(conta);

        meios.stream().distinct().forEach(tipoDeMeio ->
                cadastrarMeio.executar(ambienteId, null, tipoDeMeio, gravada.id()));

        if (saldoInicialCentavos == null || saldoInicialCentavos == 0) {
            return new ContaComSaldo(gravada, 0);
        }

        Sentido sentido = saldoInicialCentavos >= 0 ? Sentido.ENTRADA : Sentido.SAIDA;

        Long categoriaDeAbertura = categorias
                .buscarDeSistema(ambienteId, OperacaoDeSistema.SALDO_DE_ABERTURA, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();

        lancamentos.salvarTodos(List.of(Lancamento.deAbertura(ambienteId, gravada.id(),
                categoriaDeAbertura, autorId, saldoInicialCentavos, diaLocal.hoje(),
                relogio.instant())));

        return new ContaComSaldo(gravada, saldoInicialCentavos);
    }
}
