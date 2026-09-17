package br.com.cyberbank.usuario.api;

import br.com.cyberbank.usuario.aplicacao.CadastrarUsuarioUseCase;
import br.com.cyberbank.usuario.dominio.Usuario;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    public record CadastroRequest(String nome, String email, String senha) {
    }

    public record UsuarioResponse(Long id, String nome, String email) {
    }

    private final CadastrarUsuarioUseCase cadastrarUsuario;

    public UsuarioController(CadastrarUsuarioUseCase cadastrarUsuario) {
        this.cadastrarUsuario = cadastrarUsuario;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroRequest requisicao) {
        Usuario usuario = cadastrarUsuario.executar(
                requisicao.nome(), requisicao.email(), requisicao.senha());
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email());
    }
}
