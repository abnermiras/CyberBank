package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarConviteUseCase {

    private final ConviteRepository convites;
    private final AcessoRepository acessos;
    private final Clock relogio;

    public CancelarConviteUseCase(ConviteRepository convites, AcessoRepository acessos,
            Clock relogio) {
        this.convites = convites;
        this.acessos = acessos;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, Long ambienteId, Long conviteId) {
        Acesso quemCancela = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        Convite convite = convites.buscar(conviteId)
                .filter(encontrado -> encontrado.ambienteId().equals(ambienteId))
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        convites.salvar(convite.canceladoPor(quemCancela, relogio.instant()));
    }
}
