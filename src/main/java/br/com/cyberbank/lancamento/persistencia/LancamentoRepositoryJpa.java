package br.com.cyberbank.lancamento.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import br.com.cyberbank.lancamento.dominio.Cursor;
import br.com.cyberbank.lancamento.dominio.FiltroDeExtrato;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Pagina;
import br.com.cyberbank.lancamento.dominio.SaldoDeConta;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class LancamentoRepositoryJpa implements LancamentoRepository {

    private final LancamentoJpa jpa;

    LancamentoRepositoryJpa(LancamentoJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Lancamento salvar(Lancamento lancamento) {
        return paraDominio(jpa.save(paraEntidade(lancamento)));
    }

    @Override
    public List<Lancamento> salvarTodos(List<Lancamento> lancamentos) {
        return jpa.saveAll(lancamentos.stream().map(LancamentoRepositoryJpa::paraEntidade).toList())
                .stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public Optional<Lancamento> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(LancamentoRepositoryJpa::paraDominio);
    }

    @Override
    public List<Lancamento> listarDaTransferencia(Long transferenciaId, Long ambienteId) {
        return jpa.findByTransferenciaIdAndAmbienteIdOrderByIdAsc(transferenciaId, ambienteId)
                .stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Lancamento> listarPrevistosVencidos(Long ambienteId, LocalDate ate) {
        return jpa.previstosVencidos(ambienteId, ate, Situacao.PREVISTO).stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public Pagina listarDoAmbiente(Long ambienteId, FiltroDeExtrato filtro, Cursor apos,
            int limite) {
        var pedido = PageRequest.of(0, limite + 1);

        List<LancamentoEntity> lidos = apos == null
                ? jpa.primeiraPagina(ambienteId, filtro.contaId(), filtro.somentePendentes(), pedido)
                : jpa.paginaApos(ambienteId, filtro.contaId(), filtro.somentePendentes(),
                        apos.dataEvento(), apos.id(), pedido);

        return Pagina.de(lidos.stream().map(LancamentoRepositoryJpa::paraDominio).toList(), limite);
    }

    @Override
    public long saldoRealizadoDaConta(Long contaId, LocalDate ate) {
        return jpa.somarRealizadoDaConta(contaId, ate, Sentido.ENTRADA, Situacao.PREVISTO);
    }

    @Override
    public List<SaldoDeConta> saldoRealizadoPorConta(Long ambienteId, LocalDate ate) {
        return jpa.somarRealizadoPorConta(ambienteId, ate, Sentido.ENTRADA, Situacao.PREVISTO)
                .stream()
                .map(linha -> new SaldoDeConta((Long) linha[0], ((Number) linha[1]).longValue()))
                .toList();
    }

    @Override
    public boolean contaTemLancamento(Long contaId) {
        return jpa.existsByContaId(contaId);
    }

    @Override
    public boolean meioTemLancamento(Long meioId) {
        return jpa.existsByMeioId(meioId);
    }

    @Override
    public boolean categoriaTemLancamento(Long categoriaId) {
        return jpa.existsByCategoriaId(categoriaId);
    }

    @Override
    public boolean temEstorno(Long lancamentoId) {
        return jpa.existsByEstornoDeId(lancamentoId);
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    @Override
    public void excluirPrevistosDaConta(Long contaId, Long ambienteId) {
        jpa.apagarPrevistosDaConta(contaId, ambienteId, Situacao.PREVISTO);
    }

    @Override
    public long proximoIdDeTransferencia() {
        return jpa.proximoIdDeTransferencia();
    }

    private static LancamentoEntity paraEntidade(Lancamento l) {
        return new LancamentoEntity(l.id(), l.ambienteId(), l.contaId(), l.meioId(),
                l.categoriaId(), l.autorId(), l.sentido(), l.valorCentavos(), l.dataEvento(),
                l.dataEfeito(), l.descricao(), l.situacao(), l.transferenciaId(), l.estornoDeId(),
                l.doCiclo(), l.estabelecimento(), l.criadoEm());
    }

    private static Lancamento paraDominio(LancamentoEntity e) {
        return new Lancamento(e.getId(), e.getAmbienteId(), e.getContaId(), e.getMeioId(),
                e.getCategoriaId(), e.getAutorId(), e.getSentido(), e.getValorCentavos(),
                e.getDataEvento(), e.getDataEfeito(), e.getDescricao(), e.getSituacao(),
                e.getTransferenciaId(), e.getEstornoDeId(), e.isDoCiclo(), e.getEstabelecimento(),
                e.getCriadoEm());
    }
}
