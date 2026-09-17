package br.com.cyberbank.meio.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenomearMeioUseCase {

    private final MeioRepository meios;

    public RenomearMeioUseCase(MeioRepository meios) {
        this.meios = meios;
    }

    @Transactional
    public Meio executar(Long ambienteId, Long meioId, String novoNome) {
        Meio meio = meios.buscarDoAmbiente(meioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return meios.salvar(meio.renomeado(novoNome));
    }
}
