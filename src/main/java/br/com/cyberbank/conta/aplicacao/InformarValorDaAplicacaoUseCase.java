package br.com.cyberbank.conta.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.TipoDeConta;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformarValorDaAplicacaoUseCase {

    private final ContaRepository contas;
    private final LancamentoRepository lancamentos;
    private final CategoriaRepository categorias;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public InformarValorDaAplicacaoUseCase(ContaRepository contas,
            LancamentoRepository lancamentos, CategoriaRepository categorias,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.categorias = categorias;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Optional<Lancamento> executar(Long ambienteId, Long autorId, Long contaId,
            long valorAtualCentavos) {

        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (conta.tipo() != TipoDeConta.APLICACAO) {
            throw new RegraDeDominioException(CodigoDeErro.CONTA_NAO_E_APLICACAO);
        }

        LocalDate hoje = diaLocal.hoje();
        long saldo = lancamentos.saldoRealizadoDaConta(contaId, hoje);
        long diferenca = valorAtualCentavos - saldo;

        if (diferenca == 0) {
            return Optional.empty();
        }

        Sentido sentido = diferenca > 0 ? Sentido.ENTRADA : Sentido.SAIDA;
        Long categoriaDeRendimento = categorias
                .buscarDeSistema(ambienteId, OperacaoDeSistema.RENDIMENTO, sentido)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO))
                .id();

        Lancamento rendimento = lancamentos.salvar(Lancamento.deRendimento(ambienteId, contaId,
                categoriaDeRendimento, autorId, diferenca, hoje, relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId,
                TipoDeEvento.VALOR_DE_APLICACAO_INFORMADO, Alvo.conta(contaId),
                Evento.reunir(List.of(
                        Evento.dados("nome", conta.nome(), "diferenca", diferenca),
                        Evento.deParaDe("valor", saldo, valorAtualCentavos))),
                hoje, relogio.instant()));

        return Optional.of(rendimento);
    }
}
