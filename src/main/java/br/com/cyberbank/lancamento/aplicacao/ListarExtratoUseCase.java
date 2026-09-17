package br.com.cyberbank.lancamento.aplicacao;

import br.com.cyberbank.lancamento.dominio.Cursor;
import br.com.cyberbank.lancamento.dominio.FiltroDeExtrato;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Pagina;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarExtratoUseCase {

    public static final int LIMITE_PADRAO = 50;
    public static final int LIMITE_MAXIMO = 200;

    private final LancamentoRepository lancamentos;

    public ListarExtratoUseCase(LancamentoRepository lancamentos) {
        this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = true)
    public Pagina executar(Long ambienteId, Long contaId, boolean somentePendentes,
            String cursorRecebido, Integer limiteRecebido) {

        int limite = limiteRecebido == null
                ? LIMITE_PADRAO
                : Math.clamp(limiteRecebido, 1, LIMITE_MAXIMO);

        return lancamentos.listarDoAmbiente(ambienteId,
                new FiltroDeExtrato(contaId, somentePendentes),
                Cursor.decodificar(cursorRecebido),
                limite);
    }
}
