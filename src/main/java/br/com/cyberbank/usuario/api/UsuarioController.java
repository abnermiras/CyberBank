package br.com.cyberbank.usuario.api;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.usuario.aplicacao.AlterarPerfilUseCase;
import br.com.cyberbank.usuario.aplicacao.CadastrarUsuarioUseCase;
import br.com.cyberbank.usuario.aplicacao.DesvincularTelegramUseCase;
import br.com.cyberbank.usuario.aplicacao.TrocarSenhaUseCase;
import br.com.cyberbank.usuario.aplicacao.VerPerfilUseCase;
import br.com.cyberbank.usuario.aplicacao.VincularTelegramUseCase;
import br.com.cyberbank.usuario.dominio.Avatar;
import br.com.cyberbank.usuario.dominio.Usuario;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    public record CadastroRequest(String nome, String email, String senha) {
    }

    public record PerfilRequest(String nome, String avatar, String email) {
    }

    public record TelegramRequest(Long chatId) {
    }

    public record SenhaRequest(String senhaAtual, String novaSenha) {
    }

    public record UsuarioResponse(Long id, String nome, String email, String avatar) {
    }

    public record PerfilResponse(Long id, String nome, String email, String avatar,
                                 Long telegramChatId, Instant criadoEm,
                                 List<String> avataresDisponiveis) {
    }

    private final CadastrarUsuarioUseCase cadastrarUsuario;
    private final VerPerfilUseCase verPerfil;
    private final AlterarPerfilUseCase alterarPerfil;
    private final TrocarSenhaUseCase trocarSenha;
    private final VincularTelegramUseCase vincularTelegram;
    private final DesvincularTelegramUseCase desvincularTelegram;

    public UsuarioController(
            CadastrarUsuarioUseCase cadastrarUsuario,
            VerPerfilUseCase verPerfil,
            AlterarPerfilUseCase alterarPerfil,
            TrocarSenhaUseCase trocarSenha,
            VincularTelegramUseCase vincularTelegram,
            DesvincularTelegramUseCase desvincularTelegram) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.verPerfil = verPerfil;
        this.alterarPerfil = alterarPerfil;
        this.trocarSenha = trocarSenha;
        this.vincularTelegram = vincularTelegram;
        this.desvincularTelegram = desvincularTelegram;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroRequest requisicao) {
        Usuario usuario = cadastrarUsuario.executar(
                requisicao.nome(), requisicao.email(), requisicao.senha());
        return new UsuarioResponse(
                usuario.id(), usuario.nome(), usuario.email(), usuario.avatar().name());
    }

    @GetMapping("/atual")
    public PerfilResponse ver() {
        return perfil(verPerfil.executar(ContextoDaRequisicao.usuarioId()));
    }

    @PatchMapping("/atual")
    public PerfilResponse alterar(@RequestBody PerfilRequest requisicao) {
        return perfil(alterarPerfil.executar(
                ContextoDaRequisicao.usuarioId(),
                requisicao.nome(),
                requisicao.avatar(),
                requisicao.email()));
    }

    @PutMapping("/atual/telegram")
    public PerfilResponse vincularTelegram(@RequestBody TelegramRequest requisicao) {
        return perfil(vincularTelegram.executar(
                ContextoDaRequisicao.usuarioId(), requisicao.chatId()));
    }

    @DeleteMapping("/atual/telegram")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincularTelegram() {
        desvincularTelegram.executar(ContextoDaRequisicao.usuarioId());
    }

    @PutMapping("/atual/senha")
    public ResponseEntity<Void> trocarSenha(@RequestBody SenhaRequest requisicao) {
        trocarSenha.executar(ContextoDaRequisicao.usuarioId(),
                requisicao.senhaAtual(), requisicao.novaSenha());

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieDeSessao.expirado().toString())
                .build();
    }

    private static PerfilResponse perfil(Usuario usuario) {
        return new PerfilResponse(usuario.id(), usuario.nome(), usuario.email(),
                usuario.avatar().name(), usuario.telegramChatId(), usuario.criadoEm(),
                Avatar.nomes());
    }
}
