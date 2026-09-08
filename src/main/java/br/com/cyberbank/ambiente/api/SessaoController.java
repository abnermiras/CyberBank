package br.com.cyberbank.ambiente.api;

import br.com.cyberbank.ambiente.aplicacao.AutenticarUsuarioUseCase;
import br.com.cyberbank.ambiente.aplicacao.EncerrarSessaoUseCase;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrar e CRIAR uma sessao; sair e APAGA-LA. Verbo em caminho e proibido
 * (docs/04-api/convencoes.md), e aqui a acao ja era um substantivo do dominio.
 */
@RestController
@RequestMapping("/api/v1/sessoes")
public class SessaoController {

    public record LoginRequest(String email, String senha) {
    }

    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final EncerrarSessaoUseCase encerrarSessao;

    public SessaoController(AutenticarUsuarioUseCase autenticarUsuario, EncerrarSessaoUseCase encerrarSessao) {
        this.autenticarUsuario = autenticarUsuario;
        this.encerrarSessao = encerrarSessao;
    }

    /** O identificador vai SO no cookie: corpo nenhum o repete (ADR-0009). */
    @PostMapping
    public ResponseEntity<Void> entrar(@RequestBody LoginRequest requisicao, HttpServletRequest http) {
        var sessao = autenticarUsuario.executar(
                requisicao.email(), requisicao.senha(), origemDe(http));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieDeSessao.novo(sessao.identificador()).toString())
                .build();
    }

    @DeleteMapping("/atual")
    public ResponseEntity<Void> sair(HttpServletRequest http) {
        encerrarSessao.executar(CookieDeSessao.ler(http));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, CookieDeSessao.expirado().toString())
                .build();
    }

    /** Por onde a pessoa entrou — metade do valor de inspecionar sessao (ADR-0009). */
    private String origemDe(HttpServletRequest http) {
        return http.getRemoteAddr();
    }
}
