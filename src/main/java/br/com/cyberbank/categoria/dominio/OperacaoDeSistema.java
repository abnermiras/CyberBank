package br.com.cyberbank.categoria.dominio;

/**
 * As sete operacoes que o sistema lanca sozinho (docs/02-dominio/categoria.md).
 *
 * <p>Nenhuma foi inventada: cada uma e uma operacao que o modelo JA DISTINGUIA, por campo
 * proprio ou por tipo de conta. "Saque" ficou de fora por esse mesmo criterio — sacar e uma
 * transferencia como outra qualquer, e a categoria exigiria inventar uma regra so para ela.
 *
 * <p>O nome e o mesmo nos dois sentidos, como no extrato do banco: "Transferencia -R$ 500" na
 * corrente e "Transferencia +R$ 500" na carteira.
 */
public enum OperacaoDeSistema {

    SALDO_DE_ABERTURA("Saldo de abertura"),
    TRANSFERENCIA("Transferência"),
    APORTE("Aporte"),
    RESGATE("Resgate"),
    PAGAMENTO_DE_FATURA("Pagamento de fatura"),
    ROLAGEM_DE_FATURA("Rolagem de fatura"),
    RENDIMENTO("Rendimento");

    private final String nome;

    OperacaoDeSistema(String nome) {
        this.nome = nome;
    }

    public String nome() {
        return nome;
    }
}
