package br.com.cyberbank.usuario.api;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.contexto.ContextoDaRequisicao;
import br.com.cyberbank.usuario.aplicacao.AutenticarUsuarioUseCase;
import br.com.cyberbank.usuario.aplicacao.EncerrarSessaoUseCase;
import br.com.cyberbank.usuario.aplicacao.ListarSessoesUseCase;
import br.com.cyberbank.usuario.aplicacao.ListarSessoesUseCase.SessaoListada;
import br.com.cyberbank.usuario.aplicacao.RevogarSessaoUseCase;
import br.com.cyberbank.usuario.aplicacao.RevogarTodasAsSessoesUseCase;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessoes")
public class SessaoController {

    public record LoginRequest(String email, String senha) {
    }

    public record SessaoResponse(
            Long id,
            Instant criadaEm,
            Instant ultimoUsoEm,
            String origem,
            String navegador,
            boolean atual) {
    }

    public record ListaResponse(List<SessaoResponse> itens) {
    }

    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final EncerrarSessaoUseCase encerrarSessao;
    private final ListarSessoesUseCase listarSessoes;
    private final RevogarSessaoUseCase revogarSessao;
    private final RevogarTodasAsSessoesUseCase revogarTodas;

    public SessaoController(
            AutenticarUsuarioUseCase autenticarUsuario,
            EncerrarSessaoUseCase encerrarSessao,
            ListarSessoesUseCase listarSessoes,
            RevogarSessaoUseCase revogarSessao,
            RevogarTodasAsSessoesUseCase revogarTodas) {
        this.autenticarUsuario = autenticarUsuario;
        this.encerrarSessao = encerrarSessao;
        this.listarSessoes = listarSessoes;
        this.revogarSessao = revogarSessao;
        this.revogarTodas = revogarTodas;
    }

    @PostMapping
    public ResponseEntity<Void> entrar(@RequestBody LoginRequest requisicao, HttpServletRequest http) {
        var sessao = autenticarUsuario.executar(requisicao.email(), requisicao.senha(),
                http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieDeSessao.novo(sessao.identificador()).toString())
                .build();
    }

    @GetMapping
    public ListaResponse listar(HttpServletRequest http) {
        return new ListaResponse(listarSessoes
                .executar(ContextoDaRequisicao.usuarioId(), CookieDeSessao.ler(http)).stream()
                .map(SessaoController::paraResposta)
                .toList());
    }

    @DeleteMapping("/atual")
    public ResponseEntity<Void> sair(HttpServletRequest http) {
        encerrarSessao.executar(CookieDeSessao.ler(http));
        return semSessao();
    }

    @DeleteMapping("/{sessaoId}")
    public ResponseEntity<Void> revogar(@PathVariable Long sessaoId, HttpServletRequest http) {
        boolean eraAAtual = revogarSessao.executar(
                ContextoDaRequisicao.usuarioId(), sessaoId, CookieDeSessao.ler(http));
        return eraAAtual ? semSessao() : ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> revogarTodas() {
        revogarTodas.executar(ContextoDaRequisicao.usuarioId());
        return semSessao();
    }

    private static ResponseEntity<Void> semSessao() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieDeSessao.expirado().toString())
                .build();
    }

    private static SessaoResponse paraResposta(SessaoListada listada) {
        var sessao = listada.sessao();
        return new SessaoResponse(sessao.id(), sessao.criadaEm(), sessao.ultimoUsoEm(),
                sessao.origem(), sessao.navegador(), listada.atual());
    }
}
