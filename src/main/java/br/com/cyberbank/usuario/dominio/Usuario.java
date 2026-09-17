package br.com.cyberbank.usuario.dominio;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Usuario(Long id, String email, String nome, String senhaHash, Instant criadoEm) {

    public static final int TAMANHO_MINIMO_DA_SENHA = 8;

    private static final int TAMANHO_MAXIMO_DO_EMAIL = 255;
    private static final int TAMANHO_MAXIMO_DO_NOME = 120;

    public static Usuario cadastrar(String nome, String email, String senhaHash, Instant agora) {
        return new Usuario(null, normalizarEmail(email), nome == null ? null : nome.trim(),
                senhaHash, agora);
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static void validarCadastro(String nome, String email, String senha) {
        List<ErroDeValidacao> erros = new java.util.ArrayList<>();

        if (vazio(nome)) {
            erros.add(new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome."));
        } else if (nome.trim().length() > TAMANHO_MAXIMO_DO_NOME) {
            erros.add(new ErroDeValidacao("nome", "TAMANHO",
                    "O nome tem no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres."));
        }

        String normalizado = normalizarEmail(email);
        if (vazio(normalizado)) {
            erros.add(new ErroDeValidacao("email", "OBRIGATORIO", "Informe o e-mail."));
        } else if (!ehEmail(normalizado)) {
            erros.add(new ErroDeValidacao("email", "FORMATO", "E-mail inválido."));
        } else if (normalizado.length() > TAMANHO_MAXIMO_DO_EMAIL) {
            erros.add(new ErroDeValidacao("email", "TAMANHO",
                    "O e-mail tem no máximo " + TAMANHO_MAXIMO_DO_EMAIL + " caracteres."));
        }

        if (senha == null || senha.isEmpty()) {
            erros.add(new ErroDeValidacao("senha", "OBRIGATORIO", "Informe a senha."));
        } else if (senha.length() < TAMANHO_MINIMO_DA_SENHA) {
            erros.add(new ErroDeValidacao("senha", "TAMANHO",
                    "A senha tem no mínimo " + TAMANHO_MINIMO_DA_SENHA + " caracteres."));
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static boolean ehEmail(String email) {
        int arroba = email.indexOf('@');
        int ponto = email.lastIndexOf('.');
        return arroba > 0
                && ponto > arroba + 1
                && ponto < email.length() - 1
                && !email.contains(" ");
    }
}
