package br.com.cyberbank.ambiente.aplicacao;

import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O ambiente vem do CAMINHO da URL e passa por aqui antes de virar contexto: sem a validacao
 * do acesso, aceitar o id que o cliente mandou e a falha classica do ADR-0002.
 *
 * <p>Ambiente que existe e nao e seu responde IGUAL a ambiente que nao existe. Diferenciar 403
 * de 404 conta ao curioso que aquele ambiente existe — e o identificador e sequencial.
 */
@Service
public class ResolverAmbienteAtivoUseCase {

    private final AcessoRepository acessos;

    public ResolverAmbienteAtivoUseCase(AcessoRepository acessos) {
        this.acessos = acessos;
    }

    @Transactional(readOnly = true)
    public Papel executar(Long usuarioId, Long ambienteId) {
        return acessos.buscar(usuarioId, ambienteId)
                .map(acesso -> acesso.papel())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
    }
}
