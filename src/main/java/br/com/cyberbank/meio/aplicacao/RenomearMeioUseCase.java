package br.com.cyberbank.meio.aplicacao;

import java.time.Clock;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenomearMeioUseCase {

    private final MeioRepository meios;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RenomearMeioUseCase(MeioRepository meios, EventoRepository eventos, DiaLocal diaLocal,
            Clock relogio) {
        this.meios = meios;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public Meio executar(Long ambienteId, Long autorId, Long meioId, String novoNome) {
        Meio meio = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Meio renomeado = meios.salvar(meio.renomeado(novoNome));

        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.MEIO_RENOMEADO,
                Alvo.meio(meioId), Evento.deParaDe("nome", meio.nome(), renomeado.nome()),
                diaLocal.hoje(), relogio.instant()));

        return renomeado;
    }
}
