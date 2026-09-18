package br.com.cyberbank.comum.erro;

public enum CodigoDeErro {

    CORPO_INVALIDO(400, "Requisição malformada"),
    NAO_AUTENTICADO(401, "Não autenticado"),
    CREDENCIAIS_INVALIDAS(401, "E-mail ou senha inválidos"),
    MUITAS_TENTATIVAS(429, "Tentativas demais. Tente de novo mais tarde"),
    SEM_PERMISSAO(403, "Seu papel neste ambiente não permite esta operação"),
    NAO_ENCONTRADO(404, "Não encontrado"),
    EMAIL_JA_CADASTRADO(409, "Este e-mail já está cadastrado"),
    VALIDACAO(422, "Requisição inválida"),
    SENHA_ATUAL_INVALIDA(422, "A senha atual não confere"),
    TELEGRAM_JA_VINCULADO(409, "Este chat do Telegram já está vinculado a outro usuário"),
    CATEGORIA_DE_SISTEMA_PROTEGIDA(409, "Esta categoria é do sistema"),
    CATEGORIA_PAI_INVALIDO(409, "A árvore de categorias tem só dois níveis"),
    CATEGORIA_COM_SUBCATEGORIA(409, "Esta categoria ainda tem subcategorias"),
    CATEGORIA_COM_LANCAMENTO(409, "Esta categoria já tem lançamento"),
    CATEGORIA_SEM_COR_PROPRIA(409, "Subcategoria herda a cor da raiz"),
    CATEGORIA_NAO_ESCOLHIVEL(409, "Esta categoria não pode ser escolhida"),
    CATEGORIA_DE_OUTRO_SENTIDO(409, "Esta categoria é do outro sentido"),
    CONTA_INATIVA(409, "Esta conta está inativa"),
    CONTA_COM_LANCAMENTO(409, "Esta conta já tem lançamento"),
    CARTAO_SEM_SALDO_INICIAL(422, "Cartão de crédito não tem saldo inicial"),
    CONTA_NAO_E_APLICACAO(409, "Só uma aplicação tem valor atual informado"),
    CONTA_NAO_E_CARTAO(409, "Esta conta não é um contrato de cartão de crédito"),
    FATURA_FORA_DO_CICLO(409, "Esta fatura não está no ponto do ciclo que a operação exige"),
    FATURA_NAO_RECEBE_PAGAMENTO(409, "Esta fatura não recebe pagamento"),
    FATURA_NAO_ABRE(409, "Esta fatura não pode ser aberta"),
    MEIO_INCOMPATIVEL_COM_CONTA(409, "Este meio não serve para esta conta"),
    MEIO_COM_LANCAMENTO(409, "Este meio já tem lançamento"),
    MEIO_DUPLICADO_NA_CONTA(409, "Esta conta já tem um meio desse tipo"),
    MEIO_INATIVO(409, "Este meio está inativo"),
    MEIO_NAO_PARCELA(409, "Só o cartão de crédito parcela"),
    LANCAMENTO_DO_CICLO(409, "Este lançamento foi criado pelo sistema"),
    LANCAMENTO_COM_ESTORNO(409, "Este lançamento tem um estorno apontando para ele"),
    PARCELA_ISOLADA(409, "Parcela não se exclui sozinha: quem se arrepende exclui o parcelamento"),
    TRANSFERENCIA_MESMA_CONTA(409, "Origem e destino são a mesma conta"),
    BENEFICIO_NAO_TRANSFERE(409, "Conta de benefício não entra em transferência"),
    AMBIENTE_INVALIDO(409, "Este dado é de outro ambiente");

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
