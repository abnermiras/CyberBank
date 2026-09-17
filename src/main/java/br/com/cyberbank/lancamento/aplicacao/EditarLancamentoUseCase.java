package br.com.cyberbank.lancamento.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
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

    public EditarLancamentoUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            EscolhaDeCategoria escolhaDeCategoria) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.escolhaDeCategoria = escolhaDeCategoria;
    }

    @Transactional
    public List<Lancamento> executar(Long ambienteId, Long lancamentoId, Long novaContaId,
            Long novaCategoriaId, Sentido novoSentido, Long novoValorCentavos,
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

        if (lancamento.ehTransferencia()) {
            return editarOParInteiro(ambienteId, lancamento, novoValorCentavos, novaDataEvento,
                    novaDataEfeito, novaDescricao, novaSituacao);
        }

        return List.of(lancamentos.salvar(lancamento.corrigido(novaContaId, null, novaCategoriaId,
                novoSentido, novoValorCentavos, novaDataEvento, novaDataEfeito, novaDescricao,
                novaSituacao)));
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
