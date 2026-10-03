package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResolverAmbienteAtivoUseCase {

    private final AcessoRepository acessos;

    public ResolverAmbienteAtivoUseCase(AcessoRepository acessos) {
        this.acessos = acessos;
    }

    @Transactional(readOnly = true)
    public Papel executar(Long usuarioId, Long ambienteId, boolean alteraODado) {
        Papel papel = acessos.buscar(usuarioId, ambienteId)
                .map(acesso -> acesso.papel())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        if (alteraODado && !papel.podeAlterarODado()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
        return papel;
    }
}
