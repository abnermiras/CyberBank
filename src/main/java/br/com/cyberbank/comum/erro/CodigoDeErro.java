package br.com.cyberbank.comum.erro;

/**
 * O catalogo de docs/04-api/erros.md, e nada alem dele. Codigo que nao esta la nao existe
 * (docs/01-arquitetura/padroes-de-codigo.md) — e o cliente le o CODIGO, nunca a mensagem.
 */
public enum CodigoDeErro {

    NAO_AUTENTICADO(401, "Não autenticado"),
    CREDENCIAIS_INVALIDAS(401, "E-mail ou senha inválidos"),
    MUITAS_TENTATIVAS(429, "Tentativas demais. Tente de novo mais tarde"),
    SEM_PERMISSAO(403, "Seu papel neste ambiente não permite esta operação"),
    NAO_ENCONTRADO(404, "Não encontrado"),
    EMAIL_JA_CADASTRADO(409, "Este e-mail já está cadastrado"),
    VALIDACAO(422, "Requisição inválida");

    private final int status;
    private final String titulo;

    CodigoDeErro(int status, String titulo) {
        this.status = status;
        this.titulo = titulo;
    }

    public int status() {
        return status;
    }

    public String titulo() {
        return titulo;
    }
}
