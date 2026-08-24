# Controle Financeiro

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

## 1.3 Ciclo de Vida da Movimentação

Uma movimentação financeira pode ser:

- criada;
- editada;
- excluída.

A exclusão de uma movimentação é considerada o cancelamento da
movimentação.

Uma movimentação excluída deixa de compor os saldos, extratos e demais
cálculos financeiros do ambiente.

### 1.3.1 Permissões

A criação, edição e exclusão de uma movimentação podem ser realizadas por
qualquer usuário que possua permissão `Controle Total` no ambiente.

Usuários com permissão `Leitura` não podem criar, editar ou excluir
movimentações.

### 1.3.2 Estorno

O estorno é uma operação exclusiva das movimentações relacionadas a
cartões de crédito.

As regras específicas de estorno serão definidas no documento
`CREDIT-CARDS.md`.

## 1.4 Lançamentos Futuros

O CyberBank permite o registro de movimentações com data futura.

Uma movimentação futura pode ser registrada antes da data em que o
evento financeiro ocorrerá.

A movimentação futura deve ser identificada como um lançamento ainda não
realizado.

Quando a data da movimentação for atingida, o lançamento passa a ser
considerado realizado conforme as regras definidas pelo sistema.

## 1.5 Lançamentos Recorrentes

O CyberBank permite a criação de lançamentos que se repetem ao longo do
tempo.

Um lançamento recorrente pode possuir:

- quantidade determinada de ocorrências;
- data de início e data de término;
- duração indefinida.

A recorrência deve definir a periodicidade de repetição do lançamento.

Cada ocorrência representa um lançamento financeiro individual.

As ocorrências pertencentes a uma mesma recorrência devem permanecer
relacionadas entre si.

### 1.5.1 Alteração de Lançamentos Recorrentes

Uma ocorrência de um lançamento recorrente pode ser alterada
individualmente sem alterar as demais ocorrências da série.

Também é possível alterar a série de lançamentos recorrentes.

Quando uma alteração na série afetar uma ocorrência que já tenha sido
realizada ou paga, o sistema deve alertar o usuário antes de confirmar a
alteração.

O usuário deve ser informado de que a alteração afetará uma ocorrência
financeira que já foi realizada.

### 1.5.2 Exclusão de Lançamentos Recorrentes

Uma ocorrência de um lançamento recorrente pode ser excluída
individualmente.

A série de lançamentos recorrentes pode ser encerrada ou excluída,
conforme a operação realizada pelo usuário.

A exclusão ou alteração de ocorrências já realizadas deve seguir as
mesmas regras de permissão aplicáveis às demais movimentações.

## 1.6 Datas da Movimentação

Uma movimentação financeira possui duas datas distintas:

- **Data de lançamento:** data em que a movimentação foi registrada no CyberBank;
- **Data de efetivação:** data em que a movimentação produz efeito financeiro.

A data de lançamento representa o momento do registro e não determina
alteração de saldo.

A data de efetivação determina quando a movimentação passa a produzir
efeito financeiro.

### 1.6.1 Situação da Movimentação

A situação da movimentação é determinada automaticamente pela comparação
entre a data atual do sistema e a data de efetivação.

Uma movimentação pode possuir as seguintes situações:

- `Previsto` — quando a data de efetivação ainda não foi atingida;
- `Efetivado` — quando a data de efetivação foi atingida.

O usuário não define manualmente a situação da movimentação.

### 1.6.2 Movimentação Prevista

Uma movimentação `Prevista`:

- não altera o saldo atual da conta;
- não altera o patrimônio atual;
- pode ser considerada em projeções financeiras;
- pode ser utilizada em planejamentos e dashboards.

### 1.6.3 Efetivação

Quando a data de efetivação é atingida, a movimentação passa
automaticamente de `Previsto` para `Efetivado`.

Nesse momento:

- a movimentação passa a produzir efeito financeiro;
- o saldo da conta relacionada é atualizado;
- os cálculos patrimoniais passam a considerar a movimentação.

---

## 1.7 Projeção Financeira

O CyberBank deve permitir a análise da situação financeira considerando
movimentações previstas e efetivadas.

As movimentações previstas podem ser utilizadas para calcular projeções
financeiras futuras.

As projeções podem ser utilizadas por:

- dashboards;
- planejamentos financeiros;
- metas;
- projetos financeiros;
- análises de fluxo de caixa.

Uma projeção não altera os saldos financeiros efetivos.
---

## 2. Movimentações Sistêmicas

O CyberBank pode gerar movimentações sistêmicas para representar
operações realizadas pelo próprio sistema.

Movimentações sistêmicas podem utilizar categorias de uso exclusivo do
sistema.

As categorias sistêmicas são controladas pelo sistema e não dependem de
categorias criadas pelo usuário.

### 2.1 Transferência entre Contas

Uma transferência entre contas representa a movimentação de dinheiro de
uma conta bancária para outra.

Uma transferência gera duas movimentações financeiras relacionadas:

- uma saída na conta de origem;
- uma entrada na conta de destino.

As duas movimentações representam uma única transferência e utilizam a
categoria sistêmica `Transferência`.

#### 2.1.1 Atualização de Saldos

A transferência deve atualizar:

- o saldo da conta de origem;
- o saldo da conta de destino;
- o saldo global do patrimônio.

O valor transferido não representa aumento ou redução do patrimônio
global, pois apenas altera a distribuição do dinheiro entre contas.

### 2.2 Saque

O saque representa a retirada de dinheiro de uma conta bancária para
transformá-lo em dinheiro físico.

Um saque gera duas movimentações financeiras relacionadas:

- uma saída na conta de origem;
- uma entrada na conta de destino.

A conta de destino deve ser uma conta bancária do tipo `Carteira`.

#### 2.2.1 Conta Carteira

Uma conta `Carteira` representa dinheiro físico sob controle do usuário.

O usuário pode criar uma ou mais contas do tipo `Carteira`.

A conta `Carteira` pode receber qualquer nome definido pelo usuário,
como:

- Carteira;
- Bolsa;
- Colchão;
- Bolso;
- Cofre.

Uma conta `Carteira` aceita exclusivamente a forma de pagamento e
recebimento `Dinheiro`.

Uma conta `Carteira` não pode utilizar formas de pagamento ou recebimento
diferentes de `Dinheiro`.

#### 2.2.2 Registro do Saque

Ao registrar um saque, o usuário deve informar:

- conta bancária de origem;
- conta `Carteira` de destino;
- valor;
- data;
- responsável pelo lançamento.

O saque gera:

- uma saída na conta de origem;
- uma entrada na conta `Carteira`.

As duas movimentações representam uma única operação de saque.

O saque utiliza uma categoria sistêmica do tipo `Saque`.

#### 2.2.3 Atualização de Saldos

O saque deve atualizar:

- o saldo da conta de origem;
- o saldo da conta `Carteira`;
- o saldo global do patrimônio.

O saque não altera o patrimônio global do usuário, pois apenas transforma
a forma como o dinheiro está armazenado.

---

## 3. Extratos

As movimentações financeiras devem estar disponíveis no extrato da conta
bancária relacionada.

Uma transferência deve aparecer:

- como saída no extrato da conta de origem;
- como entrada no extrato da conta de destino.

Um saque deve aparecer:

- como saída no extrato da conta de origem;
- como entrada no extrato da conta `Carteira`.

---

## 4. Extrato de Lançamentos

O CyberBank disponibiliza um extrato consolidado de lançamentos.

O extrato de lançamentos permite visualizar em um único local as
movimentações financeiras registradas nos diferentes recursos financeiros
do ambiente.

As movimentações realizadas em contas bancárias compartilhadas também
devem ser apresentadas conforme as regras de acesso ao recurso.

---

## 5. Elementos Financeiros da Movimentação

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
