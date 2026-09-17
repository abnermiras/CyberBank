package br.com.cyberbank.lancamento.dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository {

    Lancamento salvar(Lancamento lancamento);

    List<Lancamento> salvarTodos(List<Lancamento> lancamentos);

    Optional<Lancamento> buscarDoAmbiente(Long id, Long ambienteId);

    List<Lancamento> listarDaTransferencia(Long transferenciaId, Long ambienteId);

    List<Lancamento> listarPrevistosVencidos(Long ambienteId, LocalDate ate);

    Pagina listarDoAmbiente(Long ambienteId, FiltroDeExtrato filtro, Cursor apos, int limite);

    long saldoRealizadoDaConta(Long contaId, LocalDate ate);

    List<SaldoDeConta> saldoRealizadoPorConta(Long ambienteId, LocalDate ate);

    boolean contaTemLancamento(Long contaId);

    boolean meioTemLancamento(Long meioId);

    boolean categoriaTemLancamento(Long categoriaId);

    boolean temEstorno(Long lancamentoId);

    void excluir(Long id, Long ambienteId);

    void excluirPrevistosDaConta(Long contaId, Long ambienteId);

    long proximoIdDeTransferencia();
}
