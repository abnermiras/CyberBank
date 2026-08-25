# Cartões de Crédito

## 1. Conceito

Um cartão de crédito representa um contrato de crédito criado pelo usuário
dentro de um ambiente financeiro.

O contrato de cartão de crédito pertence ao usuário que realizou sua criação
e ao ambiente financeiro no qual foi criado.

O cartão de crédito pode estar associado a uma instituição financeira
cadastrada no ambiente ou pode existir sem associação a uma instituição
financeira.

O cartão possui um limite global de crédito, que representa o limite
disponível para utilização dentro do contrato.

---

## 2. Cadastro do Cartão de Crédito

O usuário pode criar um cartão de crédito dentro de seu ambiente financeiro.

No momento da criação, devem ser informados:

* nome do cartão;
* últimos quatro dígitos do cartão;
* limite global;
* empresa mantenedora do contrato;
* banco associado, quando aplicável.

O limite global é obrigatório e pode ser alterado posteriormente pelo
usuário.

Os últimos quatro dígitos são utilizados apenas para identificação do
cartão e não representam o número completo do cartão.

O CyberBank não armazena o número completo do cartão.

---

## 3. Empresa Mantenedora

Todo cartão de crédito possui uma empresa mantenedora do contrato.

A empresa mantenedora representa a instituição responsável pelo contrato
de crédito.

A empresa mantenedora pode ser:

* um banco;
* uma instituição financeira;
* uma empresa não bancária;
* outra entidade que ofereça um contrato de cartão de crédito.

Exemplos:

* Nubank;
* Santander;
* Itaú;
* Banco Inter;
* iFood.

A empresa mantenedora deve ser informada no momento da criação do cartão.

---

## 4. Associação com Banco

O cartão de crédito pode possuir uma associação com um banco cadastrado no
ambiente financeiro.

Quando a empresa mantenedora for um banco e esse banco já estiver
cadastrado no ambiente do usuário, o cartão de crédito pode ser associado
diretamente ao banco existente.

Nesse caso, o cartão utiliza o banco já cadastrado no ambiente como sua
instituição associada.

A empresa mantenedora do contrato continua sendo identificada pelo banco
associado.

### 4.1 Banco não cadastrado

Quando a empresa mantenedora for um banco que ainda não estiver cadastrado
no ambiente do usuário, o cartão de crédito pode ser criado sem associação
a uma conta bancária existente.

Nesse caso, o CyberBank deve manter a informação da empresa mantenedora
informada no cadastro do cartão.

O cadastro do cartão não cria automaticamente uma conta bancária.

Posteriormente, o usuário poderá cadastrar o banco e associar o cartão,
conforme as regras de associação definidas pelo sistema.

---

## 5. Empresa Mantenedora não Bancária

Uma empresa não bancária também pode ser responsável por um contrato de
cartão de crédito.

Exemplo:

Um usuário cria um cartão de crédito `iFood`.

No momento da criação:

* empresa mantenedora: `iFood`;
* banco associado: nenhum;
* nome do cartão: definido pelo usuário;
* últimos quatro dígitos: informados pelo usuário;
* limite global: definido pelo usuário.

O cartão permanece válido mesmo sem possuir um banco associado.

A empresa mantenedora continua registrada como responsável pelo contrato.

---

## 6. Cartão sem Banco Associado

Um cartão de crédito não precisa obrigatoriamente estar associado a um
banco cadastrado no ambiente.

São situações válidas:

* cartão mantido por um banco cadastrado no ambiente;
* cartão mantido por um banco ainda não cadastrado no ambiente;
* cartão mantido por uma empresa não bancária;
* cartão sem banco associado.

A ausência de banco associado não impede a utilização do cartão nem o
controle de seu contrato de crédito.

---

## 7. Informações do Cartão

O CyberBank deve manter as informações necessárias para identificar o
cartão de crédito.

As informações principais são:

* empresa mantenedora;
* banco associado, quando houver;
* nome do cartão;
* últimos quatro dígitos;
* limite global.

O número completo do cartão não é armazenado pelo CyberBank.

---

## 8. Limite Global

Todo contrato de cartão de crédito possui um limite global.

O limite global é definido pelo usuário no momento da criação do cartão.

O usuário pode alterar posteriormente o limite global do cartão.

O limite global representa o valor máximo de crédito disponível para o
contrato, conforme as regras de consumo e comprometimento de limite
definidas pelo CyberBank.

As regras de consumo, comprometimento, liberação e cálculo do limite serão
definidas nas seções específicas de utilização do cartão e faturamento.

---

## 9. Cartões Virtuais

O usuário pode criar cartões virtuais vinculados ao seu contrato de cartão
de crédito.

O cartão virtual não constitui um novo contrato de crédito.

O cartão virtual utiliza o limite global do contrato ao qual está
vinculado.

Cada cartão virtual possui sua própria identificação.

As regras específicas de utilização, limite, faturamento e encerramento de
cartões virtuais serão definidas nas seções correspondentes.

---

## 10. Cartões Adicionais

O titular do contrato de cartão de crédito pode criar cartões adicionais
para outros usuários do CyberBank.

O cartão adicional não constitui um novo contrato de cartão de crédito.

O cartão adicional permanece vinculado ao contrato de cartão de crédito do
usuário titular.

O usuário que recebe o cartão adicional não se torna proprietário do
contrato nem do limite global.

### 10.1 Criação do Cartão Adicional

Somente o usuário titular do contrato pode criar um cartão adicional para
outro usuário do CyberBank.

Para criar um cartão adicional, o titular deve informar o usuário que
receberá o cartão.

O cartão adicional deve permanecer vinculado ao contrato original.

O titular pode possuir múltiplos cartões adicionais vinculados ao mesmo
contrato.

Todos os cartões adicionais utilizam o limite global do contrato.

### 10.2 Recebimento do Cartão Adicional

Após a criação, o cartão adicional é disponibilizado ao usuário
destinatário.

O cartão adicional permanece com status `Pendente` até que o usuário
destinatário aceite o recebimento.

Enquanto o cartão estiver `Pendente`, ele não pode ser utilizado pelo
usuário destinatário.

O usuário destinatário pode aceitar ou recusar o cartão adicional.

### 10.3 Aceitação

Ao aceitar o cartão adicional, o usuário destinatário deve escolher em qual
de seus ambientes financeiros o cartão será disponibilizado.

O cartão adicional passa a estar disponível somente no ambiente escolhido
pelo usuário destinatário.

A escolha do ambiente determina o contexto no qual o usuário poderá utilizar
o cartão como forma de pagamento.

O recebimento do cartão adicional não cria um novo contrato de cartão de
crédito no ambiente do usuário destinatário.

### 10.4 Forma de Pagamento

Após a aceitação e escolha do ambiente, o cartão adicional passa a ser
disponibilizado nesse ambiente como uma forma de pagamento.

O usuário destinatário poderá utilizar o cartão adicional nas
movimentações financeiras permitidas pelo sistema.

A movimentação realizada utilizando o cartão adicional pertence ao
ambiente escolhido pelo usuário destinatário.

A utilização do cartão adicional não transfere a propriedade do contrato
nem do limite para o usuário destinatário.

### 10.5 Responsabilidade pelo Contrato

O usuário titular permanece responsável pelo contrato de cartão de crédito.

O usuário destinatário possui apenas o direito de utilização concedido
pelo cartão adicional.

As compras realizadas pelo cartão adicional:

* utilizam o limite global do contrato;
* pertencem ao contrato do titular;
* são realizadas pelo usuário destinatário;
* são registradas no ambiente escolhido pelo usuário destinatário;
* podem ser identificadas pelo cartão adicional utilizado.

### 10.6 Visibilidade

O cartão adicional deve aparecer para o usuário destinatário após sua
aceitação.

O usuário destinatário poderá visualizar as informações necessárias para
identificar o cartão adicional.

O acesso ao cartão adicional não concede acesso aos demais cartões
vinculados ao contrato do titular.

O usuário destinatário não recebe automaticamente acesso ao contrato
principal nem às informações privadas do titular.

### 10.7 Ambiente do Cartão Adicional

O cartão adicional recebido pertence operacionalmente ao ambiente escolhido
pelo usuário destinatário.

O usuário destinatário pode utilizar suas categorias e subcategorias
existentes nesse ambiente ao registrar movimentações com o cartão
adicional.

A movimentação permanece associada ao ambiente no qual foi realizada.

O fato de o cartão pertencer ao contrato de outro usuário não altera a
origem da movimentação dentro do CyberBank.

### 10.8 Revogação

O titular do contrato pode revogar um cartão adicional.

A revogação impede novas utilizações do cartão adicional.

A revogação não altera nem remove as movimentações realizadas
anteriormente com o cartão.

O histórico das movimentações permanece armazenado conforme as regras de
histórico financeiro do CyberBank.

O usuário destinatário deixa de possuir o cartão como forma de pagamento
ativa em seu ambiente.

### 10.9 Recusa

O usuário destinatário pode recusar um cartão adicional recebido.

Um cartão adicional recusado não pode ser utilizado pelo usuário
destinatário.

A recusa não cria qualquer vínculo do cartão com um ambiente do usuário
destinatário.

O titular poderá criar posteriormente um novo cartão adicional, caso
deseje disponibilizar novamente um cartão para esse usuário.


## 11. Responsabilidade pelo Contrato

O usuário que cria o cartão de crédito é o titular do contrato dentro do
CyberBank.

A criação do cartão em um ambiente financeiro não transfere a propriedade
do contrato para o banco ou para a empresa mantenedora.

A instituição mantenedora representa a entidade responsável pelo contrato
de crédito, enquanto o usuário permanece como titular do cadastro do
contrato dentro do CyberBank.

---

## 12. Regras Futuras

As seguintes regras ainda devem ser definidas:

* ciclo de faturamento;
* data de fechamento da fatura;
* data de vencimento;
* criação e estados da fatura;
* compras no cartão;
* consumo do limite;
* limite comprometido;
* limite disponível;
* compras parceladas;
* parcelas;
* pagamento da fatura;
* liberação do limite;
* estorno;
* cancelamento de compras;
* cartões adicionais;
* compartilhamento do cartão;
* encerramento do cartão;
* alteração do limite;
* histórico de alterações do limite.
