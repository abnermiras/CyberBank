## 1. Movimentações Financeiras

As movimentações financeiras são registradas dentro de um ambiente
financeiro.

Uma movimentação pode utilizar elementos financeiros cadastrados ou
compartilhados no ambiente, incluindo:a# Controle Financeiro

## 1. Movimentação Financeira

A movimentação financeira representa o registro de uma entrada ou saída
de dinheiro.

Toda movimentação deve identificar:

- tipo da movimentação: entrada ou saída;
- valor;
- data;
- origem ou destino do dinheiro;
- categoria;
- responsável pelo lançamento.

### 1.1 Entrada

Uma entrada representa dinheiro recebido pelo usuário.

A entrada deve identificar:

- por onde o dinheiro foi recebido;
- quando ocorreu;
- valor recebido;
- categoria;
- responsável pelo lançamento.

### 1.2 Saída

Uma saída representa dinheiro utilizado ou pago pelo usuário.

A saída deve identificar:

- por onde o dinheiro foi utilizado;
- quando ocorreu;
- valor;
- categoria;
- responsável pelo lançamento.

---

## 2. Movimentações Sistêmicas

O sistema pode criar movimentações utilizando categorias sistêmicas.

Movimentações sistêmicas representam operações que precisam ser
registradas para manter a consistência financeira do sistema.

As categorias sistêmicas são controladas pelo sistema e não dependem de
categorias criadas pelo usuário.

---

## 3. Transferência entre Contas

Uma transferência entre contas representa a movimentação de dinheiro de
uma conta bancária para outra.

Uma transferência gera duas movimentações financeiras relacionadas:

- uma saída na conta de origem;
- uma entrada na conta de destino.

As duas movimentações utilizam a categoria sistêmica `Transferência`.

A transferência representa uma única operação financeira, mesmo sendo
registrada através de duas movimentações.

### 3.1 Atualização de Saldos

A transferência deve atualizar:

- o saldo da conta de origem;
- o saldo da conta de destino;
- o saldo global do patrimônio.

O valor transferido não representa aumento ou redução do patrimônio
global, pois apenas altera a distribuição do dinheiro entre contas.

---

## 4. Extratos

As movimentações financeiras devem estar disponíveis no extrato da conta
bancária relacionada.

Uma transferência deve aparecer:

- como saída no extrato da conta de origem;
- como entrada no extrato da conta de destino.

---

## 5. Extrato de Lançamentos

O CyberBank disponibiliza um extrato consolidado de lançamentos.

O extrato de lançamentos permite visualizar em um único local as
movimentações financeiras registradas nos diferentes recursos financeiros
do ambiente.

As movimentações realizadas em contas bancárias compartilhadas também
devem ser apresentadas conforme as regras de acesso ao recurso.

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
