package br.com.cyberbank.lancamento.aplicacao;

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
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.lancamento.dominio.Sentido;
import br.com.cyberbank.lancamento.dominio.Situacao;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LancarUseCase {

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final MeioRepository meios;
    private final EscolhaDeCategoria escolhaDeCategoria;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public LancarUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            MeioRepository meios, EscolhaDeCategoria escolhaDeCategoria, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.meios = meios;
        this.escolhaDeCategoria = escolhaDeCategoria;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Lancamento executar(Long ambienteId, Long autorId, Long meioId, Long categoriaId,
            Sentido sentido, Long valorCentavos, LocalDate dataEvento,
            LocalDate dataEfeitoInformada, String descricao, String estabelecimento) {

        Meio meio = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        meio.exigirAtivoParaLancar();

        Conta conta = contas.buscarDoAmbiente(meio.contaId(), ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        conta.exigirAtivaParaLancar();

        escolhaDeCategoria.exigirEscolhivel(ambienteId, categoriaId, sentido);

        LocalDate hoje = diaLocal.hoje();
        LocalDate dataEfeito = meio.dataEfeitoPara(dataEvento, dataEfeitoInformada);
        Situacao situacao = dataEfeito.isAfter(hoje) ? Situacao.PREVISTO : Situacao.REALIZADO;

        Lancamento lancado = lancamentos.salvar(Lancamento.doUsuario(ambienteId, conta.id(),
                meio.id(), categoriaId, autorId, sentido, valorCentavos, dataEvento, dataEfeito,
                descricao, situacao, estabelecimento, relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LANCAMENTO_CRIADO,
                Alvo.lancamento(lancado.id()),
                Evento.dados(
                        "descricao", lancado.descricao(),
                        "valor", lancado.valorCentavos(),
                        "sentido", lancado.sentido().name(),
                        "contaId", lancado.contaId(),
                        "situacao", lancado.situacao().name()),
                hoje, relogio.instant()));

        return lancado;
    }
}
