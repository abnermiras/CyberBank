package br.com.cyberbank.lancamento.aplicacao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.lancamento.dominio.Cursor;
import br.com.cyberbank.lancamento.dominio.FiltroDeExtrato;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Pagina;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarExtratoUseCase {

    public static final int LIMITE_PADRAO = 50;
    public static final int LIMITE_MAXIMO = 200;

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final AmbienteRepository ambientes;

    public ListarExtratoUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            AmbienteRepository ambientes) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.ambientes = ambientes;
    }

    @Transactional(readOnly = true)
    public ExtratoLido executar(Long ambienteId, Long contaId, boolean somentePendentes,
            String cursorRecebido, Integer limiteRecebido) {

        int limite = limiteRecebido == null
                ? LIMITE_PADRAO
                : Math.clamp(limiteRecebido, 1, LIMITE_MAXIMO);

        if (contaId != null && contas.buscarAcessivel(contaId, ambienteId).isEmpty()) {
            return new ExtratoLido(Pagina.de(List.of(), limite), Map.of());
        }

        Pagina pagina = lancamentos.listarDoAmbiente(ambienteId,
                new FiltroDeExtrato(contaId, somentePendentes),
                Cursor.decodificar(cursorRecebido),
                limite);

        return new ExtratoLido(pagina, nomesDosAmbientesDeFora(pagina.itens(), ambienteId));
    }

    private Map<Long, String> nomesDosAmbientesDeFora(List<Lancamento> itens, Long ambienteId) {
        Map<Long, String> nomes = new HashMap<>();
        for (Lancamento lancamento : itens) {
            Long origem = lancamento.ambienteId();
            if (!origem.equals(ambienteId) && !nomes.containsKey(origem)) {
                nomes.put(origem, ambientes.buscar(origem).map(Ambiente::nome).orElse(""));
            }
        }
        return nomes;
    }
}
