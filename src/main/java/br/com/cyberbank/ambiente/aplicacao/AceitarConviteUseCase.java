package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoAoAmbiente;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.comum.contexto.ContextoDoBanco;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AceitarConviteUseCase {

    private final ConviteRepository convites;
    private final AcessoRepository acessos;
    private final AmbienteRepository ambientes;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final ContextoDoBanco contextoDoBanco;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public AceitarConviteUseCase(ConviteRepository convites, AcessoRepository acessos,
            AmbienteRepository ambientes, UsuarioRepository usuarios, EventoRepository eventos,
            ContextoDoBanco contextoDoBanco, DiaLocal diaLocal, Clock relogio) {
        this.convites = convites;
        this.acessos = acessos;
        this.ambientes = ambientes;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.contextoDoBanco = contextoDoBanco;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public AcessoAoAmbiente executar(Long usuarioId, Long conviteId) {
        Instant agora = relogio.instant();
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));
        Convite convite = convites.buscar(conviteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        boolean jaTemAcesso = acessos.buscar(usuarioId, convite.ambienteId()).isPresent();
        Convite aceito = convite.aceitoPor(usuario.email(), jaTemAcesso, agora);

        Acesso acesso = acessos.salvar(aceito.acessoDe(usuarioId, agora));
        convites.salvar(aceito);

        contextoDoBanco.definirAmbiente(acesso.ambienteId());
        eventos.registrar(Evento.doUsuario(acesso.ambienteId(), usuarioId,
                TipoDeEvento.ACESSO_CONCEDIDO, null,
                Evento.dados("usuarioId", usuarioId, "pessoa", usuario.nome(),
                        "papel", acesso.papel().name()),
                diaLocal.hoje(), agora));

        Ambiente ambiente = ambientes.buscar(acesso.ambienteId())
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        return new AcessoAoAmbiente(ambiente, acesso.papel());
    }
}
