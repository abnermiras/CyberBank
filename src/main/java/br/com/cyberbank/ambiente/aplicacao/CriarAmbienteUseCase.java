package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.categoria.aplicacao.CriarCategoriasDeSistemaUseCase;
import br.com.cyberbank.comum.contexto.ContextoDoBanco;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarAmbienteUseCase {

    private final AmbienteRepository ambientes;
    private final AcessoRepository acessos;
    private final CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema;
    private final ContextoDoBanco contextoDoBanco;
    private final Clock relogio;

    public CriarAmbienteUseCase(
            AmbienteRepository ambientes,
            AcessoRepository acessos,
            CriarCategoriasDeSistemaUseCase criarCategoriasDeSistema,
            ContextoDoBanco contextoDoBanco,
            Clock relogio) {
        this.ambientes = ambientes;
        this.acessos = acessos;
        this.criarCategoriasDeSistema = criarCategoriasDeSistema;
        this.contextoDoBanco = contextoDoBanco;
        this.relogio = relogio;
    }

    @Transactional
    public AcessoAoAmbiente executar(Long usuarioId, String nome) {
        Instant agora = relogio.instant();
        Ambiente ambiente = ambientes.salvar(Ambiente.novo(nome, usuarioId, agora));
        Acesso acesso = acessos.salvar(Acesso.donoDe(usuarioId, ambiente.id(), agora));

        contextoDoBanco.definirAmbiente(ambiente.id());
        criarCategoriasDeSistema.executar(ambiente.id());

        return new AcessoAoAmbiente(ambiente, acesso.papel());
    }
}
