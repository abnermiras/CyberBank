package br.com.cyberbank.ambiente.aplicacao;

import java.util.List;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarConvitesDoAmbienteUseCase {

    private final ConviteRepository convites;
    private final AcessoRepository acessos;

    public ListarConvitesDoAmbienteUseCase(ConviteRepository convites, AcessoRepository acessos) {
        this.convites = convites;
        this.acessos = acessos;
    }

    @Transactional(readOnly = true)
    public List<Convite> executar(Long usuarioId, Long ambienteId) {
        Acesso acesso = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        if (!acesso.papel().podeConvidar()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
        return convites.listarPendentesDoAmbiente(ambienteId);
    }
}
