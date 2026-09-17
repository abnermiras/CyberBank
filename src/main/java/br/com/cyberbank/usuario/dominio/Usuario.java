package br.com.cyberbank.usuario.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Usuario(Long id, String email, String nome, String senhaHash, Avatar avatar,
                      Long telegramChatId, Instant criadoEm) {

    public static final int TAMANHO_MINIMO_DA_SENHA = 8;

    private static final int TAMANHO_MAXIMO_DO_EMAIL = 255;
    private static final int TAMANHO_MAXIMO_DO_NOME = 120;

    public static Usuario cadastrar(
            String nome, String email, String senhaHash, Avatar avatar, Instant agora) {
        return new Usuario(null, normalizarEmail(email), nome == null ? null : nome.trim(),
                senhaHash, avatar, null, agora);
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public Usuario comPerfil(String nome, Avatar avatar) {
        return new Usuario(id, email, nome.trim(), senhaHash, avatar, telegramChatId, criadoEm);
    }

    public Usuario comTelegramChatId(Long telegramChatId) {
        return new Usuario(id, email, nome, senhaHash, avatar, telegramChatId, criadoEm);
    }

    public Usuario comSenhaHash(String senhaHash) {
        return new Usuario(id, email, nome, senhaHash, avatar, telegramChatId, criadoEm);
    }

    public static void validarCadastro(String nome, String email, String senha) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(nome, erros);

        String normalizado = normalizarEmail(email);
        if (vazio(normalizado)) {
            erros.add(new ErroDeValidacao("email", "OBRIGATORIO", "Informe o e-mail."));
        } else if (!ehEmail(normalizado)) {
            erros.add(new ErroDeValidacao("email", "FORMATO", "E-mail inválido."));
        } else if (normalizado.length() > TAMANHO_MAXIMO_DO_EMAIL) {
            erros.add(new ErroDeValidacao("email", "TAMANHO",
                    "O e-mail tem no máximo " + TAMANHO_MAXIMO_DO_EMAIL + " caracteres."));
        }

        validarSenha(senha, erros);

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static void validarAlteracaoDePerfil(
            String nome, String avatar, String emailInformado) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarNome(nome, erros);

        if (vazio(avatar)) {
            erros.add(new ErroDeValidacao("avatar", "OBRIGATORIO", "Escolha um avatar."));
        } else if (!Avatar.existe(avatar)) {
            erros.add(new ErroDeValidacao("avatar", "FORMATO", "Este avatar não existe."));
        }

        if (emailInformado != null) {
            erros.add(new ErroDeValidacao("email", "IMUTAVEL",
                    "O e-mail é o seu login e a chave dos convites: ele não muda."));
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static void validarTelegramChatId(Long chatId) {
        if (chatId == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao(
                    "chatId", "OBRIGATORIO", "Informe o id do chat.")));
        }
        if (chatId == 0L) {
            throw new ValidacaoException(List.of(new ErroDeValidacao(
                    "chatId", "FORMATO", "Zero não é chat de ninguém.")));
        }
    }

    public static void validarNovaSenha(String novaSenha) {
        List<ErroDeValidacao> erros = new ArrayList<>();
        validarSenha(novaSenha, erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(renomeadosParaNovaSenha(erros));
        }
    }

    public static ValidacaoException senhaRepetida() {
        return new ValidacaoException(List.of(new ErroDeValidacao("novaSenha", "IGUAL_A_ATUAL",
                "A nova senha é igual à atual.")));
    }

    private static void validarNome(String nome, List<ErroDeValidacao> erros) {
        if (vazio(nome)) {
            erros.add(new ErroDeValidacao("nome", "OBRIGATORIO", "Informe o nome."));
        } else if (nome.trim().length() > TAMANHO_MAXIMO_DO_NOME) {
            erros.add(new ErroDeValidacao("nome", "TAMANHO",
                    "O nome tem no máximo " + TAMANHO_MAXIMO_DO_NOME + " caracteres."));
        }
    }

    private static void validarSenha(String senha, List<ErroDeValidacao> erros) {
        if (senha == null || senha.isEmpty()) {
            erros.add(new ErroDeValidacao("senha", "OBRIGATORIO", "Informe a senha."));
        } else if (senha.length() < TAMANHO_MINIMO_DA_SENHA) {
            erros.add(new ErroDeValidacao("senha", "TAMANHO",
                    "A senha tem no mínimo " + TAMANHO_MINIMO_DA_SENHA + " caracteres."));
        }
    }

    private static List<ErroDeValidacao> renomeadosParaNovaSenha(List<ErroDeValidacao> erros) {
        return erros.stream()
                .map(erro -> new ErroDeValidacao("novaSenha", erro.codigo(), erro.mensagem()))
                .toList();
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
