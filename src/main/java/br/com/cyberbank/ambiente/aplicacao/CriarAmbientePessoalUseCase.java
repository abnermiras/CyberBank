package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarAmbientePessoalUseCase {

    private final AmbienteRepository ambientes;
    private final AcessoRepository acessos;
    private final Clock relogio;

    public CriarAmbientePessoalUseCase(
            AmbienteRepository ambientes, AcessoRepository acessos, Clock relogio) {
        this.ambientes = ambientes;
        this.acessos = acessos;
        this.relogio = relogio;
    }

    @Transactional
    public Long executar(Long usuarioId) {
        Instant agora = relogio.instant();
        Ambiente ambiente = ambientes.salvar(Ambiente.pessoalDe(usuarioId, agora));
        acessos.salvar(Acesso.donoDe(usuarioId, ambiente.id(), agora));
        return ambiente.id();
    }
}
