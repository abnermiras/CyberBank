package br.com.cyberbank.recorrencia.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.aplicacao.EscolhaDeCategoria;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.recorrencia.dominio.Periodicidade;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;
import br.com.cyberbank.recorrencia.dominio.RecorrenciaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarRecorrenciaUseCase {

    private final RecorrenciaRepository recorrencias;
    private final MeioRepository meios;
    private final ContaRepository contas;
    private final FaturaRepository faturas;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final OcorrenciaDoCiclo ocorrencia;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public CriarRecorrenciaUseCase(RecorrenciaRepository recorrencias, MeioRepository meios,
            ContaRepository contas, FaturaRepository faturas,
            EscolhaDeCategoria escolhaDeCategoria, OcorrenciaDoCiclo ocorrencia,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.recorrencias = recorrencias;
        this.meios = meios;
        this.contas = contas;
        this.faturas = faturas;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.ocorrencia = ocorrencia;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public RecorrenciaComOcorrencia executar(Long ambienteId, Long autorId, Long meioId,
            Long categoriaId, Long valorCentavos, Integer dia, LocalDate inicio,
            String descricao) {

        Meio cartao = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        cartao.exigirAtivoParaLancar();

        if (!cartao.temFatura()) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_NAO_RECORRE);
        }

        Conta conta = contas.buscarDoAmbiente(cartao.contaId(), ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        conta.exigirAtivaParaLancar();

        escolhaDeCategoria.exigirEscolhivel(ambienteId, categoriaId, Sentido.SAIDA);

        Recorrencia gravada = recorrencias.salvar(Recorrencia.nova(ambienteId, conta.id(),
                cartao.id(), categoriaId, valorCentavos, Periodicidade.MENSAL, dia,
                inicio == null ? diaLocal.hoje() : inicio, descricao, relogio.instant()));

        Fatura aberta = faturas.buscarAbertaDaConta(conta.id())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.SERIE_CRIADA,
                Alvo.serie(gravada.id()),
                Evento.dados(
                        "descricao", gravada.descricao(),
                        "valor", gravada.valorCentavos(),
                        "sentido", Sentido.SAIDA.name(),
                        "periodicidade", gravada.periodicidade().name(),
                        "dia", gravada.dia(),
                        "contaId", conta.id()),
                diaLocal.hoje(), relogio.instant()));

        return new RecorrenciaComOcorrencia(gravada,
                ocorrencia.lancarSeFaltar(gravada, autorId, aberta.id(), aberta.competencia())
                        .orElse(null));
    }
}
