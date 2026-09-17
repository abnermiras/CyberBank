package br.com.cyberbank.meio.aplicacao;

import java.util.List;

import br.com.cyberbank.meio.dominio.Meio;
import br.com.cyberbank.meio.dominio.MeioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarMeiosUseCase {

    private final MeioRepository meios;

    public ListarMeiosUseCase(MeioRepository meios) {
        this.meios = meios;
    }

    @Transactional(readOnly = true)
    public List<Meio> executar(Long ambienteId, boolean inativos) {
        return meios.listarDoAmbiente(ambienteId).stream()
                .filter(meio -> inativos || !meio.inativo())
                .toList();
    }
}
