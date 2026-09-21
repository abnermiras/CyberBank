package br.com.cyberbank.lancamento.persistencia;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.cyberbank.lancamento.dominio.Cursor;
import br.com.cyberbank.lancamento.dominio.FiltroDeExtrato;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Pagina;
import br.com.cyberbank.lancamento.dominio.RelatorioDoMes;
import br.com.cyberbank.lancamento.dominio.DiaDeConta;
import br.com.cyberbank.lancamento.dominio.JanelaDoPrevisto;
import br.com.cyberbank.lancamento.dominio.SaldoDeConta;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;
import br.com.cyberbank.lancamento.dominio.TotaisDeFatura;
import br.com.cyberbank.lancamento.dominio.TotalPorSentido;

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
    public List<Lancamento> listarDaFatura(Long faturaId, Long ambienteId) {
        return jpa.findByFaturaIdAndAmbienteIdOrderByDataEventoAscIdAsc(faturaId, ambienteId)
                .stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Lancamento> listarDoParcelamento(Long parcelamentoId, Long ambienteId) {
        return jpa.findByParcelamentoIdAndAmbienteIdOrderByIdAsc(parcelamentoId, ambienteId)
                .stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<Lancamento> listarDaRecorrencia(Long recorrenciaId, Long ambienteId) {
        return jpa.findByRecorrenciaIdAndAmbienteIdOrderByIdAsc(recorrenciaId, ambienteId)
                .stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public boolean temOcorrenciaNaFatura(Long recorrenciaId, Long faturaId) {
        return jpa.existsByRecorrenciaIdAndFaturaId(recorrenciaId, faturaId);
    }

    @Override
    public void excluirDoParcelamento(Long parcelamentoId, Long ambienteId) {
        jpa.deleteByParcelamentoIdAndAmbienteId(parcelamentoId, ambienteId);
    }

    @Override
    public Optional<Lancamento> buscarEstornoDe(Long lancamentoId, Long ambienteId) {
        return jpa.findByEstornoDeIdAndAmbienteId(lancamentoId, ambienteId)
                .map(LancamentoRepositoryJpa::paraDominio);
    }

    @Override
    public List<RelatorioDoMes.Bucket> somarPorCategoria(
            Long ambienteId, LocalDate de, LocalDate ate, Collection<Long> contas) {
        if (contas.isEmpty()) {
            return List.of();
        }
        return jpa.somarPorCategoria(ambienteId, de, ate, contas, Situacao.PREVISTO).stream()
                .map(linha -> new RelatorioDoMes.Bucket(
                        (Long) linha[0], (Sentido) linha[1],
                        ((Number) linha[2]).longValue(), ((Number) linha[3]).longValue()))
                .toList();
    }

    @Override
    public long somarAportes(Long ambienteId, LocalDate de, LocalDate ate, Collection<Long> contas) {
        if (contas.isEmpty()) {
            return 0;
        }
        return jpa.somarAportes(ambienteId, de, ate, contas, Sentido.ENTRADA, Situacao.PREVISTO);
    }

    @Override
    public List<TotalPorSentido> somarPrevistos(
            Long ambienteId, LocalDate de, LocalDate ate, Collection<Long> contas) {
        if (contas.isEmpty()) {
            return List.of();
        }
        return jpa.somarPrevistos(ambienteId, de, ate, contas, Situacao.PREVISTO).stream()
                .map(linha -> new TotalPorSentido(
                        (Sentido) linha[0], ((Number) linha[1]).longValue()))
                .toList();
    }

    @Override
    public List<Lancamento> listarPrevistosAte(
            Long ambienteId, LocalDate de, LocalDate ate, Collection<Long> contas, int limite) {
        if (contas.isEmpty()) {
            return List.of();
        }
        return jpa.previstosDoHorizonte(ambienteId, de, ate, contas, Situacao.PREVISTO,
                        PageRequest.of(0, limite)).stream()
                .map(LancamentoRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public List<TotaisDeFatura> totaisDasFaturas(Collection<Long> faturaIds) {
        if (faturaIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> pagoPorFatura = new HashMap<>();
        Map<Long, Long> agendadoPorFatura = new HashMap<>();
        for (Object[] linha : jpa.somarPagamentosPorFatura(faturaIds, Situacao.REALIZADO,
                Situacao.PREVISTO)) {
            pagoPorFatura.put((Long) linha[0], (Long) linha[1]);
            agendadoPorFatura.put((Long) linha[0], (Long) linha[2]);
        }

        Map<Long, TotaisDeFatura> porFatura = new HashMap<>();
        for (Object[] linha : jpa.somarPorFatura(faturaIds, Sentido.ENTRADA,
                Situacao.PROVISIONADO)) {
            Long faturaId = (Long) linha[0];
            porFatura.put(faturaId, new TotaisDeFatura(faturaId, (Long) linha[1],
                    pagoPorFatura.getOrDefault(faturaId, 0L), (Long) linha[2],
                    agendadoPorFatura.getOrDefault(faturaId, 0L), ((Long) linha[3]) > 0));
        }

        return faturaIds.stream()
                .map(faturaId -> porFatura.getOrDefault(faturaId, new TotaisDeFatura(faturaId, 0,
                        pagoPorFatura.getOrDefault(faturaId, 0L), 0,
                        agendadoPorFatura.getOrDefault(faturaId, 0L), false)))
                .toList();
    }

    @Override
    public long contarPendencias(Long ambienteId) {
        return jpa.countByAmbienteIdAndCategoriaIdIsNull(ambienteId);
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
    public List<SaldoDeConta> previstoPorConta(Long ambienteId, JanelaDoPrevisto janela) {
        if (janela.vazia()) {
            return List.of();
        }
        return jpa.somarPrevistoPorConta(ambienteId, janela.de(), janela.ate(),
                        Sentido.ENTRADA, Situacao.PREVISTO)
                .stream()
                .map(linha -> new SaldoDeConta((Long) linha[0], ((Number) linha[1]).longValue()))
                .toList();
    }

    @Override
    public List<DiaDeConta> ultimoValorInformadoPorConta(Long ambienteId,
            Collection<Long> categorias) {
        if (categorias.isEmpty()) {
            return List.of();
        }
        return jpa.ultimoDiaPorConta(ambienteId, categorias).stream()
                .map(linha -> new DiaDeConta((Long) linha[0], (LocalDate) linha[1]))
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

    @Override
    public long proximoIdDeRolagem() {
        return jpa.proximoIdDeRolagem();
    }

    @Override
    public int liquidarProvisionadosDaFatura(Long faturaId) {
        return jpa.liquidarProvisionadosDaFatura(faturaId, Situacao.PROVISIONADO,
                Situacao.REALIZADO);
    }

    private static LancamentoEntity paraEntidade(Lancamento l) {
        return new LancamentoEntity(l.id(), l.ambienteId(), l.contaId(), l.meioId(),
                l.categoriaId(), l.autorId(), l.sentido(), l.valorCentavos(), l.dataEvento(),
                l.dataEfeito(), l.descricao(), l.situacao(), l.transferenciaId(), l.estornoDeId(),
                l.faturaId(), l.pagamentoDeFaturaId(), l.rolagemDeFatura(), l.parcelamentoId(),
                l.recorrenciaId(),
                l.doCiclo(), l.estabelecimento(), l.criadoEm());
    }

    private static Lancamento paraDominio(LancamentoEntity e) {
        return new Lancamento(e.getId(), e.getAmbienteId(), e.getContaId(), e.getMeioId(),
                e.getCategoriaId(), e.getAutorId(), e.getSentido(), e.getValorCentavos(),
                e.getDataEvento(), e.getDataEfeito(), e.getDescricao(), e.getSituacao(),
                e.getTransferenciaId(), e.getEstornoDeId(), e.getFaturaId(),
                e.getPagamentoDeFaturaId(), e.getRolagemDeFatura(), e.getParcelamentoId(),
                e.getRecorrenciaId(), e.isDoCiclo(), e.getEstabelecimento(), e.getCriadoEm());
    }
}
