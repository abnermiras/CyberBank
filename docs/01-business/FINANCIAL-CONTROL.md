## 1. Movimentações Financeiras

As movimentações financeiras são registradas dentro de um ambiente
financeiro.

Uma movimentação pode utilizar elementos financeiros cadastrados ou
compartilhados no ambiente, incluindo:

- conta bancária;
- categoria;
- subcategoria;
- forma de pagamento;
- forma de recebimento;
- cartão de crédito.

Quando uma movimentação utiliza uma conta bancária compartilhada entre
ambientes, ela pertence à mesma conta independentemente do ambiente a
partir do qual foi registrada.

---

## 2. Movimentações Sistêmicas

O CyberBank pode gerar movimentações sistêmicas para representar
operações realizadas pelo próprio sistema.

Movimentações sistêmicas podem utilizar categorias de uso exclusivo do
sistema.

### 2.1 Transferência entre Contas

Uma transferência entre contas gera duas movimentações:

- uma movimentação de débito na conta de origem;
- uma movimentação de crédito na conta de destino.

As duas movimentações representam uma única transferência.

As movimentações utilizam uma categoria sistêmica do tipo `Transferência`.
