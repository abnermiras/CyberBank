package br.com.cyberbank.meio.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarMeioUseCase {

    private final MeioRepository meios;
    private final ContaRepository contas;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public CadastrarMeioUseCase(MeioRepository meios, ContaRepository contas,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.meios = meios;
        this.contas = contas;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Meio executar(Long ambienteId, Long autorId, String nome, TipoDeMeio tipo,
            Long contaId) {
        Conta conta = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        if (tipo != null && !tipo.repetePorConta()
                && meios.existeNaConta(contaId, tipo, ambienteId)) {
            throw new RegraDeDominioException(CodigoDeErro.MEIO_DUPLICADO_NA_CONTA);
        }

        Meio criado = meios.salvar(Meio.novo(ambienteId, nome, tipo, contaId,
                conta.tipo().name(), relogio.instant()));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.MEIO_CRIADO,
                Alvo.meio(criado.id()),
                Evento.dados(
                        "nome", criado.nome(),
                        "tipo", criado.tipo().name(),
                        "contaId", contaId,
                        "conta", conta.nome()),
                diaLocal.hoje(), relogio.instant()));

        return criado;
    }
}
