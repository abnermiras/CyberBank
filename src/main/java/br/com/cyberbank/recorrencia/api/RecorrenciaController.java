package br.com.cyberbank.recorrencia.api;

import java.net.URI;
import java.time.LocalDate;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.recorrencia.aplicacao.CriarRecorrenciaUseCase;
import br.com.cyberbank.recorrencia.aplicacao.RecorrenciaComOcorrencia;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ambientes/{ambienteId}/recorrencias")
public class RecorrenciaController {

    public record OcorrenciaResponse(Long id, long valorCentavos, LocalDate dataEvento,
            Long faturaId, String situacao) {
    }

    public record RecorrenciaResponse(Long id, Long contaId, Long meioId, Long categoriaId,
            long valorCentavos, String periodicidade, int dia, LocalDate inicio, boolean ativa,
            String descricao, OcorrenciaResponse ocorrenciaDoCicloAberto) {
    }

    public record RecorrenciaRequest(Long meioId, Long categoriaId, Long valor, Integer dia,
            LocalDate inicio, String descricao) {
    }

    private final CriarRecorrenciaUseCase criarRecorrencia;

    public RecorrenciaController(CriarRecorrenciaUseCase criarRecorrencia) {
        this.criarRecorrencia = criarRecorrencia;
    }

    @PostMapping
    public ResponseEntity<RecorrenciaResponse> criar(@PathVariable Long ambienteId,
            @RequestBody RecorrenciaRequest requisicao) {

        RecorrenciaComOcorrencia criada = criarRecorrencia.executar(ambienteId,
                ContextoDaRequisicao.usuarioId(), requisicao.meioId(), requisicao.categoriaId(),
                requisicao.valor(), requisicao.dia(), requisicao.inicio(), requisicao.descricao());

        return ResponseEntity
                .created(URI.create("/api/v1/ambientes/" + ambienteId + "/recorrencias/"
                        + criada.recorrencia().id()))
                .body(paraResposta(criada));
    }

    static RecorrenciaResponse paraResposta(RecorrenciaComOcorrencia criada) {
        Recorrencia r = criada.recorrencia();
        Lancamento ocorrencia = criada.ocorrenciaDoCiclo();

        return new RecorrenciaResponse(r.id(), r.contaId(), r.meioId(), r.categoriaId(),
                r.valorCentavos(), r.periodicidade().name(), r.dia(), r.inicio(), r.ativa(),
                r.descricao(),
                ocorrencia == null ? null
                        : new OcorrenciaResponse(ocorrencia.id(), ocorrencia.valorCentavos(),
                                ocorrencia.dataEvento(), ocorrencia.faturaId(),
                                ocorrencia.situacao().name()));
    }
}
