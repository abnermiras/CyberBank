package br.com.cyberbank.evento.dominio;

public enum TipoDeEvento {

    LANCAMENTO_REALIZADO(OrigemDeEvento.SISTEMA),
    FATURA_FECHADA(OrigemDeEvento.SISTEMA),
    FATURA_ABERTA_PELO_CICLO(OrigemDeEvento.SISTEMA),
    FATURA_ROLADA(OrigemDeEvento.SISTEMA),
    FATURA_ENCERRADA(OrigemDeEvento.SISTEMA),
    OCORRENCIA_DE_RECORRENCIA(OrigemDeEvento.SISTEMA),

    FATURA_PAGA(OrigemDeEvento.USUARIO),
    FATURA_FECHADA_PELO_USUARIO(OrigemDeEvento.USUARIO),
    FATURA_ABERTA_PELO_USUARIO(OrigemDeEvento.USUARIO),
    LIMITE_INFORMADO(OrigemDeEvento.USUARIO),
    SERIE_CRIADA(OrigemDeEvento.USUARIO),
    SERIE_ALTERADA(OrigemDeEvento.USUARIO),
    SERIE_CANCELADA(OrigemDeEvento.USUARIO),

    LANCAMENTO_CRIADO(OrigemDeEvento.USUARIO),
    LANCAMENTO_EDITADO(OrigemDeEvento.USUARIO),
    LANCAMENTO_ESTORNADO(OrigemDeEvento.USUARIO),
    LANCAMENTO_EXCLUIDO(OrigemDeEvento.USUARIO),

    CONTA_CRIADA(OrigemDeEvento.USUARIO),
    CONTA_RENOMEADA(OrigemDeEvento.USUARIO),
    CONTA_INATIVADA(OrigemDeEvento.USUARIO),
    CONTA_REATIVADA(OrigemDeEvento.USUARIO),
    CONTA_EXCLUIDA(OrigemDeEvento.USUARIO),
    VALOR_DE_APLICACAO_INFORMADO(OrigemDeEvento.USUARIO),

    MEIO_CRIADO(OrigemDeEvento.USUARIO),
    MEIO_RENOMEADO(OrigemDeEvento.USUARIO),
    MEIO_INATIVADO(OrigemDeEvento.USUARIO),
    MEIO_REATIVADO(OrigemDeEvento.USUARIO),
    MEIO_EXCLUIDO(OrigemDeEvento.USUARIO),

    CATEGORIA_CRIADA(OrigemDeEvento.USUARIO),
    CATEGORIA_RENOMEADA(OrigemDeEvento.USUARIO),
    CATEGORIA_RECOLORIDA(OrigemDeEvento.USUARIO),
    CATEGORIA_INATIVADA(OrigemDeEvento.USUARIO),
    CATEGORIA_REATIVADA(OrigemDeEvento.USUARIO),
    CATEGORIA_EXCLUIDA(OrigemDeEvento.USUARIO);

    private final OrigemDeEvento origem;

    TipoDeEvento(OrigemDeEvento origem) {
        this.origem = origem;
    }

    public OrigemDeEvento origem() {
        return origem;
    }

    public boolean doSistema() {
        return origem == OrigemDeEvento.SISTEMA;
    }
}
