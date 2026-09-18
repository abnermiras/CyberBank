package br.com.cyberbank.conta.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformarLimiteDoCartaoUseCase {

    private final ContaRepository contas;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public InformarLimiteDoCartaoUseCase(ContaRepository contas, EventoRepository eventos,
            DiaLocal diaLocal, Clock relogio) {
        this.contas = contas;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Conta executar(Long ambienteId, Long autorId, Long contaId, Long limiteCentavos) {
        if (limiteCentavos == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("limite", "OBRIGATORIO",
                    "Informe o limite do cartão.")));
        }

        Conta cartao = contas.buscarDoAmbiente(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Long antes = cartao.exigirContratoDeCartao().limiteCentavos();
        LocalDate hoje = diaLocal.hoje();

        Conta gravada = contas.salvar(cartao.comLimiteInformado(limiteCentavos, hoje));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.LIMITE_INFORMADO,
                Alvo.conta(gravada.id()),
                Evento.reunir(List.of(
                        Evento.dados("nome", gravada.nome()),
                        Evento.deParaDe("limite", antes, limiteCentavos))),
                hoje, relogio.instant()));

        return gravada;
    }
}
