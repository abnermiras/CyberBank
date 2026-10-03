package br.com.cyberbank.ambiente.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Convite;
import br.com.cyberbank.ambiente.dominio.ConviteRepository;
import br.com.cyberbank.ambiente.dominio.Papel;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConvidarParaAmbienteUseCase {

    private final ConviteRepository convites;
    private final AcessoRepository acessos;
    private final UsuarioRepository usuarios;
    private final Clock relogio;

    public ConvidarParaAmbienteUseCase(ConviteRepository convites, AcessoRepository acessos,
            UsuarioRepository usuarios, Clock relogio) {
        this.convites = convites;
        this.acessos = acessos;
        this.usuarios = usuarios;
        this.relogio = relogio;
    }

    @Transactional
    public Convite executar(Long usuarioId, Long ambienteId, String email, Papel papel) {
        Acesso quemConvida = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        Usuario.exigirEmailValido(email);
        String normalizado = Usuario.normalizarEmail(email);

        boolean jaTemAcesso = usuarios.buscarPorEmail(normalizado)
                .map(convidado -> acessos.listarDoAmbiente(ambienteId).stream()
                        .anyMatch(acesso -> acesso.usuarioId().equals(convidado.id())))
                .orElse(false);
        boolean jaConvidado = convites.buscarPendente(ambienteId, normalizado).isPresent();

        return convites.salvar(Convite.novo(quemConvida, normalizado, papel, jaTemAcesso,
                jaConvidado, relogio.instant()));
    }
}
