package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;
import java.time.Instant;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.compartilhamento.aplicacao.RevogarCompartilhamentoUseCase;
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
public class RemoverAcessoUseCase {

    private final AcessoRepository acessos;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final RevogarCompartilhamentoUseCase revogarCompartilhamento;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RemoverAcessoUseCase(AcessoRepository acessos, UsuarioRepository usuarios,
            EventoRepository eventos, RevogarCompartilhamentoUseCase revogarCompartilhamento,
            DiaLocal diaLocal, Clock relogio) {
        this.acessos = acessos;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.revogarCompartilhamento = revogarCompartilhamento;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, Long ambienteId, Long removidoId) {
        Instant agora = relogio.instant();
        Acesso quemRemove = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        Acesso removido = acessos.listarDoAmbiente(ambienteId).stream()
                .filter(acesso -> acesso.usuarioId().equals(removidoId))
                .findFirst()
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        removido.exigirRemovivelPor(quemRemove);
        revogarCompartilhamento.devolverOsEmprestadosPor(removidoId, ambienteId, usuarioId);
        if (!acessos.remover(removido)) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }

        String pessoa = usuarios.buscarPorId(removidoId).map(Usuario::nome).orElse("");
        eventos.registrar(Evento.doUsuario(ambienteId, usuarioId, TipoDeEvento.ACESSO_REVOGADO,
                null,
                Evento.dados("usuarioId", removidoId, "pessoa", pessoa,
                        "papel", removido.papel().name(),
                        "saiu", removido.removidoPorSiMesmo(usuarioId)),
                diaLocal.hoje(), agora));
    }
}
