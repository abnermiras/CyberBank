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

## 4. Associação do Cartão

Um cartão de crédito pode estar associado a um banco ou a uma empresa
mantenedora que não seja um banco.

A associação determina qual entidade será apresentada ao usuário como
origem do meio de pagamento durante a criação de uma movimentação.

### 4.1 Cartão Associado a Banco

Quando o cartão de crédito estiver associado a um banco cadastrado no
ambiente financeiro, o banco será utilizado como entidade de seleção no
lançamento.

Exemplo:

O usuário cria o cartão:

* banco: `Nubank`;
* cartão: `Ultravioleta`;
* últimos quatro dígitos: `1234`;
* limite: `R$ 50.000,00`.

Ao criar uma saída financeira, o usuário seleciona:

1. `Banco Nubank` como conta ou entidade de pagamento;
2. `Ultravioleta ****-1234` como forma de pagamento.

O cartão de crédito será apresentado como uma forma de pagamento
disponível para a entidade selecionada.

### 4.2 Cartão Associado a Empresa não Bancária

Um cartão de crédito também pode ser mantido por uma empresa que não seja
um banco.

Nesse caso, a empresa mantenedora será utilizada como entidade de seleção
durante a criação da movimentação.

Exemplo:

O usuário cria o cartão:

* empresa mantenedora: `Lojas Marisa`;
* banco associado: nenhum;
* cartão: `Cartão Marisa`;
* últimos quatro dígitos: `1234`;
* limite: definido pelo usuário.

Ao criar uma saída financeira, o usuário seleciona:

1. `Lojas Marisa` como entidade de pagamento;
2. `Cartão Marisa ****-1234` como forma de pagamento.

A empresa mantenedora passa a aparecer na seleção mesmo não sendo uma conta
bancária.

### 4.3 Entidade de Pagamento

Para fins de criação de uma movimentação, o CyberBank considera como
entidade de pagamento a instituição associada ao cartão de crédito.

Essa entidade pode ser:

* um banco;
* uma empresa mantenedora não bancária.

A entidade apresentada ao usuário depende da associação definida no
cadastro do cartão.

O cartão de crédito é apresentado como uma forma de pagamento vinculada à
entidade.

---

## 5. Cartão como Forma de Pagamento

O cartão de crédito é uma forma de pagamento disponível durante a criação
de uma movimentação de saída.

O usuário não seleciona diretamente o cartão como primeira opção de
pagamento.

O fluxo de seleção ocorre em duas etapas:

1. seleção da entidade de pagamento;
2. seleção da forma de pagamento disponível para a entidade.

Quando a entidade selecionada possuir cartões de crédito disponíveis para o
ambiente, esses cartões devem aparecer como formas de pagamento.

### 5.1 Exemplo — Cartão associado ao Nubank

O usuário possui:

* banco: `Nubank`;
* cartão: `Ultravioleta ****-1234`;
* limite: `R$ 50.000,00`.

Durante o lançamento de uma saída:

```text
Entidade de pagamento
└── Nubank

Forma de pagamento
└── Ultravioleta ****-1234
```

O lançamento utiliza o cartão `Ultravioleta ****-1234`.

### 5.2 Exemplo — Cartão associado à Lojas Marisa

O usuário possui:

* empresa mantenedora: `Lojas Marisa`;
* cartão: `Cartão Marisa ****-1234`;
* limite: definido no cadastro.

Durante o lançamento de uma saída:

```text
Entidade de pagamento
└── Lojas Marisa

Forma de pagamento
└── Cartão Marisa ****-1234
```

A `Lojas Marisa` aparece na seleção mesmo não sendo uma conta bancária.

---

## 6. Configuração do Faturamento

O cartão de crédito deve possuir informações necessárias para determinar o
ciclo de faturamento.

No momento da criação do cartão, o usuário deve informar:

* data de vencimento da fatura;
* regra de fechamento da fatura.

A data de vencimento representa o dia do mês no qual a fatura deve ser
paga.

O fechamento pode ser definido como uma quantidade de dias anterior ao
vencimento.

### 6.1 Exemplo

O usuário cria o cartão:

* vencimento: dia `18`;
* fechamento: `5 dias antes do vencimento`.

O CyberBank deverá determinar o fechamento da fatura de acordo com essa
configuração.

A configuração pertence ao contrato de cartão de crédito.

As regras completas de geração, fechamento, vencimento e pagamento das
faturas serão definidas nas seções específicas de faturamento.

---

## 7. Identificação no Lançamento

Quando um cartão de crédito estiver disponível como forma de pagamento,
o sistema deve apresentar informações suficientes para que o usuário
identifique qual cartão está utilizando.

A identificação deve conter:

* nome do cartão;
* últimos quatro dígitos.

Exemplo:

```text
Ultravioleta ****-1234
```

O CyberBank não deve exibir ou armazenar o número completo do cartão.

---

## 8. Disponibilidade da Forma de Pagamento

Um cartão de crédito somente deve aparecer como forma de pagamento quando
estiver disponível para utilização no ambiente financeiro do usuário.

A disponibilidade deve considerar:

* vínculo do cartão com o ambiente;
* situação do cartão;
* permissões do usuário;
* existência de limite disponível, conforme as regras de limite;
* demais restrições de utilização definidas pelo contrato.

Um cartão que não esteja disponível para utilização não deve ser
apresentado como forma de pagamento válida no lançamento.


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
