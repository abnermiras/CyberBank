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

## 9. Lançamento com Cartão de Crédito

Quando uma movimentação de saída utiliza um cartão de crédito como forma de
pagamento, a movimentação passa a possuir uma relação com o cartão
utilizado.

O lançamento permanece registrado no extrato de lançamentos do ambiente
financeiro.

Além do extrato de lançamentos, o lançamento também é apresentado na
fatura do cartão utilizado.

O mesmo lançamento financeiro não é duplicado. O extrato e a fatura são
visões diferentes do mesmo lançamento.

---

## 10. Faturas

Cada cartão de crédito possui suas próprias faturas.

A fatura é vinculada a um cartão específico.

Cartões diferentes do mesmo contrato possuem faturas diferentes.

Exemplo:

```text
Contrato de Cartão
│
├── Ultravioleta ****-1234
│     ├── Fatura Janeiro
│     ├── Fatura Fevereiro
│     └── Fatura Março
│
├── Ultravioleta Virtual ****-5678
│     ├── Fatura Janeiro
│     ├── Fatura Fevereiro
│     └── Fatura Março
│
└── Cartão Adicional ****-9012
      ├── Fatura Janeiro
      ├── Fatura Fevereiro
      └── Fatura Março
```

As faturas dos cartões pertencentes ao mesmo contrato são consolidadas
para fins de responsabilidade financeira do contrato.

O titular do contrato consegue visualizar todas as faturas de todos os
cartões vinculados ao contrato.

---

## 11. Limite de Crédito

O limite de crédito pertence ao contrato de cartão de crédito e não a uma
fatura individual.

Todo lançamento realizado utilizando qualquer cartão vinculado ao contrato
consome o limite global do contrato.

Isso inclui:

* cartão principal;
* cartão virtual;
* cartão adicional;
* cartão compartilhado.

O limite é consumido independentemente de o lançamento ser:

* à vista;
* parcelado;
* realizado pelo titular;
* realizado por um usuário adicional;
* realizado por um usuário com cartão compartilhado.

### 11.1 Compra à Vista

Uma compra à vista consome o valor total da compra no limite global do
contrato.

### 11.2 Compra Parcelada

Uma compra parcelada também compromete o limite global conforme as regras
de limite do contrato.

O parcelamento não cria um limite independente para cada cartão.

As regras específicas de comprometimento e liberação do limite das parcelas
serão definidas na seção de parcelamento.

---

## 12. Datas do Lançamento com Cartão

Um lançamento realizado com cartão de crédito possui duas datas distintas:

* **Data de lançamento:** data em que o lançamento foi registrado no
  CyberBank;
* **Data de efetivação:** data de vencimento da fatura à qual o lançamento
  está associado.

A data de lançamento representa o momento em que a operação foi registrada
no sistema.

A data de efetivação representa o momento em que o valor financeiro deverá
ser considerado efetivado em função do pagamento da fatura.

---

## 13. Fechamento da Fatura

Cada cartão possui uma configuração de vencimento da fatura.

O usuário define:

* dia de vencimento;
* quantidade de dias anteriores ao vencimento para fechamento.

O CyberBank calcula automaticamente a data de fechamento da fatura com
base nessa configuração.

### 13.1 Fechamento Automático

Quando a data calculada de fechamento for atingida, a fatura deverá ser
fechada conforme as regras do sistema.

Somente o titular do contrato pode realizar o fechamento da fatura.

### 13.2 Fechamento Manual

O titular do contrato pode fechar manualmente uma fatura antes da data de
fechamento calculada.

O fechamento manual determina que os lançamentos incluídos até aquele
momento façam parte da fatura fechada.

Após o fechamento, novos lançamentos deverão ser direcionados para a
próxima fatura daquele cartão.

---

## 14. Reabertura da Fatura

O titular do contrato pode reabrir uma fatura fechada.

A reabertura permite que a fatura volte ao estado anterior ao fechamento,
conforme as regras de edição e inclusão de lançamentos.

Somente o titular do contrato pode reabrir uma fatura.

A reabertura não transfere a responsabilidade financeira da fatura nem
altera a propriedade do contrato.

---

## 15. Situação dos Lançamentos de Cartão

Um lançamento realizado com cartão de crédito permanece `Previsto` até que
a fatura à qual ele pertence seja integralmente paga.

O fechamento da fatura não altera a situação do lançamento para
`Realizado`.

O pagamento parcial da fatura também não altera a situação dos lançamentos
para `Realizado`.

Somente o pagamento integral do valor da fatura permite que os lançamentos
correspondentes passem para `Realizado`.

### 15.1 Pagamento Parcial

O CyberBank permite o pagamento parcial de uma fatura.

Quando ocorrer um pagamento parcial:

* o pagamento é registrado;
* o valor pago é abatido do saldo da fatura;
* a fatura permanece pendente de quitação;
* os lançamentos relacionados permanecem `Previstos`.

O pagamento parcial não efetiva os lançamentos da fatura.

### 15.2 Pagamento Integral

Quando o valor total devido da fatura for pago:

* a fatura é considerada quitada;
* os lançamentos relacionados passam de `Previstos` para `Realizados`;
* os efeitos financeiros correspondentes passam a ser considerados
  efetivados conforme as regras financeiras do CyberBank.

---

## 16. Pagamento da Fatura

Uma fatura fechada pode ser paga por usuários que possuam permissão para
realizar o pagamento daquele cartão.

O pagamento pode ser:

* parcial;
* integral.

O pagamento integral quita a fatura.

O pagamento parcial não quita a fatura.

### 16.1 Pagamento pelo Titular

O usuário titular do contrato pode visualizar e pagar as faturas de todos
os cartões vinculados ao contrato.

O titular pode realizar pagamentos parciais ou integrais.

### 16.2 Pagamento por Usuário de Cartão Adicional ou Compartilhado

Um usuário que recebeu um cartão adicional ou compartilhado pode visualizar
a fatura correspondente ao cartão recebido.

Esse usuário pode realizar o pagamento da fatura.

O usuário pode realizar pagamento parcial ou integral.

Quando o pagamento realizado pelo usuário for equivalente ao valor total
devido da fatura daquele cartão, a fatura será quitada e os lançamentos
correspondentes passarão para `Realizados`.

---

## 17. Visibilidade das Faturas

A visibilidade das faturas depende da relação do usuário com o contrato e
com o cartão.

### 17.1 Titular do Contrato

O titular do contrato pode visualizar:

* todas as faturas do contrato;
* todas as faturas de todos os cartões;
* todos os lançamentos de todas as faturas;
* lançamentos realizados pelo próprio titular;
* lançamentos realizados por usuários de cartões adicionais;
* lançamentos realizados por usuários com cartões compartilhados.

### 17.2 Usuário de Cartão Adicional

O usuário que recebeu um cartão adicional pode visualizar a fatura
específica daquele cartão.

O usuário não possui acesso às faturas dos demais cartões do contrato.

Na fatura, o usuário visualiza somente os lançamentos realizados por ele
próprio.

Lançamentos realizados pelo titular ou por outros usuários não são
apresentados ao usuário adicional.

### 17.3 Usuário de Cartão Compartilhado

O usuário que recebeu acesso compartilhado a um cartão possui as mesmas
restrições de visualização dos lançamentos da fatura daquele cartão.

Ele pode visualizar a fatura do cartão compartilhado, porém somente os
lançamentos realizados pelo próprio usuário são apresentados.

O compartilhamento não concede acesso às demais faturas do contrato.

---

## 18. Responsabilidade pelo Fechamento

O fechamento da fatura é uma operação exclusiva do titular do contrato.

Somente o usuário proprietário do contrato pode:

* fechar uma fatura;
* reabrir uma fatura.

Usuários que receberam cartões adicionais ou compartilhados não podem
fechar ou reabrir faturas.

O fechamento da fatura pelo titular determina que novos lançamentos sejam
direcionados para o próximo ciclo de faturamento daquele cartão.
