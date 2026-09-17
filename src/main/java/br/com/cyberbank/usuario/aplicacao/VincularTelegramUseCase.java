package br.com.cyberbank.usuario.aplicacao;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.usuario.dominio.Usuario;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VincularTelegramUseCase {

    private final UsuarioRepository usuarios;

    public VincularTelegramUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional
    public Usuario executar(Long usuarioId, Long chatId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_AUTENTICADO));

        Usuario.validarTelegramChatId(chatId);

        if (!chatId.equals(usuario.telegramChatId())
                && usuarios.buscarPorTelegramChatId(chatId).isPresent()) {
            throw new RegraDeDominioException(CodigoDeErro.TELEGRAM_JA_VINCULADO);
        }

        return usuarios.salvar(usuario.comTelegramChatId(chatId));
    }
}
