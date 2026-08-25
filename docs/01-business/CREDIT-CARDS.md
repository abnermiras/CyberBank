# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o cartão de crédito como instrumento de utilização de um contrato de crédito: sua identidade, titularidade, relacionamento com o contrato, limite, formas de utilização e ciclo de vida operacional.

As regras específicas de **faturas, ciclos de faturamento, fechamento, pagamentos, quitação e créditos de fatura** serão definidas em documento próprio.

---

## 2. Conceito

Um cartão de crédito é um instrumento de pagamento vinculado a um contrato de crédito.

O contrato de crédito é uma representação abstrata da relação de crédito estabelecida pelo usuário dentro de seu ambiente financeiro. Ele não representa necessariamente um contrato físico, um documento ou um número de contrato fornecido pela instituição financeira.

O contrato define a capacidade de crédito disponível e pode possuir um ou mais cartões vinculados.

O cartão não possui limite de crédito independente do contrato. Todos os cartões vinculados ao mesmo contrato compartilham o limite global definido para esse contrato.

---

## 3. Contrato de Crédito

O contrato de crédito representa a relação de crédito criada pelo usuário dentro de seu ambiente financeiro.

O contrato possui conceitualmente:

- um usuário titular;
- um ambiente financeiro de origem;
- uma instituição mantenedora;
- um limite global;
- um ou mais cartões vinculados.

O contrato não precisa corresponder a um cadastro físico ou a um número de contrato existente na instituição mantenedora.

Um contrato é criado quando o usuário cadastra um cartão de crédito e define a instituição que mantém esse crédito.

A instituição mantenedora pode ser:

- um banco já cadastrado no ambiente financeiro do usuário; ou
- uma instituição financeira declarada pelo próprio usuário.

Todos os cartões vinculados ao mesmo contrato pertencem à mesma instituição mantenedora.

Cartões vinculados a instituições mantenedoras diferentes pertencem a contratos diferentes e não podem ser misturados entre esses contratos.

### 3.1 Titularidade

O titular é o usuário responsável pelo contrato de crédito.

A titularidade do contrato não é transferida pela criação de cartões adicionais ou pelo compartilhamento de cartões com outros usuários.

O titular do contrato possui controle sobre os cartões vinculados ao contrato, respeitando as regras de autorização do sistema.

---

## 4. Entidade Mantenedora

Todo contrato de cartão de crédito possui uma instituição mantenedora.

A instituição mantenedora representa a entidade responsável pela manutenção do crédito e pode ser um banco cadastrado no ambiente financeiro ou uma instituição financeira declarada pelo usuário.

Quando a instituição mantenedora for um banco já cadastrado no ambiente financeiro, o contrato mantém uma referência a esse cadastro.

Quando a instituição mantenedora não estiver cadastrada como banco no ambiente financeiro, o usuário pode declará-la para representar a instituição responsável pelo cartão.

A ausência de um cadastro bancário não impede a existência ou utilização do cartão.

Um contrato possui uma única instituição mantenedora e todos os cartões vinculados a esse contrato pertencem a essa mesma instituição.

---

## 5. Cadastro do Cartão

Um cartão é criado dentro de um contrato de crédito existente ou durante a criação desse contrato, conforme as regras de cadastro definidas pelo sistema.

O cadastro do cartão deve permitir identificar o instrumento de pagamento sem armazenar o número completo do cartão.

Informações mínimas de identificação:

- nome ou descrição do cartão;
- últimos quatro dígitos;
- tipo do cartão;
- meio de existência do cartão;
- vínculo com o contrato de crédito.

O CyberBank não armazena o número completo do cartão.

O cartão não pode ser transferido de um contrato para outro. Sua associação com o contrato e com a instituição mantenedora é permanente durante sua existência.

---

## 6. Tipos de Cartão

Um contrato pode possuir diferentes cartões para finalidades distintas.

Os principais tipos considerados pelo domínio são:

- cartão principal;
- cartão virtual;
- cartão adicional;
- cartão compartilhado.

Esses tipos não representam contratos de crédito independentes.

Todos permanecem vinculados ao contrato original e utilizam seu limite global.

### 6.1 Cartão Principal

O cartão principal é o primeiro cartão físico criado para o contrato de crédito.

O cartão principal pertence ao titular do contrato e possui como identificação o nome do cartão, os quatro últimos dígitos e o meio de existência `Físico`.

O titular pode utilizar o cartão principal para realizar compras e demais operações permitidas pelo sistema.

O cartão principal utiliza o limite global do contrato.

### 6.2 Cartão Virtual

Um cartão virtual é um cartão criado dentro de um contrato de crédito para utilização em meios digitais ou outras finalidades definidas pelo titular.

O cartão virtual:

- não cria um novo contrato;
- não possui limite global próprio;
- compartilha o limite do contrato;
- possui identidade própria;
- possui meio de existência `Virtual`;
- pode ser ativado ou desativado independentemente de outros cartões do mesmo contrato.

As regras de faturamento dos lançamentos realizados pelo cartão virtual pertencem ao domínio de faturas.

### 6.3 Cartão Adicional

Um cartão adicional é um novo cartão físico criado dentro do contrato do titular e concedido para uso de outro usuário.

O cartão adicional:

- continua vinculado ao contrato do titular;
- utiliza o limite global do contrato;
- não cria um novo contrato;
- possui identidade própria;
- possui meio de existência `Físico`;
- é destinado ao usuário que recebeu o cartão;
- não concede ao usuário acesso aos demais cartões do contrato.

O cartão adicional é identificado pelo nome do cartão, pela indicação `Adicional`, pelos quatro últimos dígitos e pelo meio de existência `Físico`.

#### 6.3.1 Recebimento

O usuário destinatário deve aceitar o cartão adicional antes de utilizá-lo.

Após o aceite, o cartão fica disponível para o usuário destinatário conforme as regras de autorização do sistema.

O usuário que recebe um cartão adicional pode visualizar e realizar lançamentos utilizando esse cartão, mas não pode visualizar ou utilizar os demais cartões do contrato aos quais não tenha autorização própria.

#### 6.3.2 Responsabilidade

O usuário que recebe o cartão adicional possui autorização para utilização do cartão, mas não se torna titular do contrato de crédito.

O titular do contrato continua responsável pelo contrato e pelo limite global.

O titular do contrato pode visualizar e utilizar tanto seus próprios cartões quanto os cartões adicionais vinculados ao seu contrato.

### 6.4 Cartão Compartilhado

Um cartão compartilhado é um cartão já existente do titular cujo uso é concedido a outro usuário.

O compartilhamento não cria um novo cartão e não cria um novo contrato.

O cartão compartilhado:

- permanece vinculado ao contrato original;
- mantém sua identidade original;
- mantém seu titular original;
- utiliza o limite global do contrato;
- pode ser utilizado pelos usuários autorizados pelo compartilhamento.

O usuário autorizado pelo compartilhamento pode, conforme a configuração concedida:

- visualizar os lançamentos do cartão;
- realizar lançamentos utilizando o cartão;
- participar das operações de pagamento relacionadas ao cartão.

O compartilhamento não transfere a titularidade do cartão nem a titularidade do contrato.

---

## 7. Limite Global

O limite global pertence ao contrato de crédito e não a um cartão individual.

Todos os cartões vinculados ao contrato compartilham o mesmo limite.

Exemplo:

```text
Contrato
Limite global: R$ 50.000,00

Cartão principal:       utiliza o limite do contrato
Cartão virtual:         utiliza o limite do contrato
Cartão adicional:       utiliza o limite do contrato
Cartão compartilhado:   utiliza o limite do contrato
```

Não existe limite independente por cartão neste modelo.

### 7.1 Alteração do Limite

O titular do contrato pode alterar o limite global conforme as permissões do sistema.

A alteração do limite não altera retroativamente os lançamentos já realizados.

O limite global pode ser reduzido para valor inferior ao crédito atualmente comprometido.

Nesse caso, o sistema deve preservar o estado financeiro existente e permitir que a diferença seja representada como limite disponível negativo.

Exemplo:

```text
Limite global:        R$ 20.000,00
Crédito comprometido: R$ 25.000,00
Limite disponível:    -R$ 5.000,00
```

---

## 8. Consumo do Limite

Um lançamento realizado com cartão de crédito compromete o limite global do contrato no momento em que o lançamento é registrado.

O comprometimento do limite não depende de o lançamento estar marcado como `Previsto` ou `Realizado`.

O limite comprometido representa o valor de crédito atualmente utilizado pelo contrato e será liberado pelas regras financeiras aplicáveis ao lançamento.

As regras detalhadas de liberação relacionadas à quitação de faturas serão definidas no documento de faturas.

### 8.1 Compra à Vista

Uma compra à vista compromete o valor total da operação.

### 8.2 Compra Parcelada

Uma compra parcelada compromete inicialmente o valor total da operação, e não apenas o valor da primeira parcela.

Exemplo:

```text
Compra:        R$ 1.200,00
Parcelamento:  12 x R$ 100,00

Comprometimento inicial do limite:
R$ 1.200,00
```

A forma como parcelas futuras são apresentadas em faturas e a forma como o limite é liberado durante o ciclo financeiro pertencem ao domínio de faturas.

---

## 9. Cartão como Forma de Pagamento

O cartão de crédito pode ser utilizado como forma de pagamento em um lançamento de saída.

O cartão selecionado identifica qual instrumento de crédito foi utilizado na operação.

O lançamento financeiro permanece sendo uma única operação. O uso do cartão não cria uma segunda movimentação apenas por aparecer também na fatura.

O cartão deve ser apresentado ao usuário por informações de identificação seguras, como:

```text
Ultravioleta ****-1234
```

O número completo do cartão não deve ser exibido no sistema.

Ao selecionar uma instituição no lançamento financeiro, o sistema deve apresentar as formas de pagamento disponíveis para aquela instituição. Os cartões de crédito exibidos devem corresponder aos cartões vinculados a contratos daquela instituição e aos quais o usuário possui autorização de utilização.

Exemplo:

```text
Instituição: Nubank
Formas de pagamento:
- PIX
- Débito
- Ultravioleta ****-1234

Instituição: Lojas Crediários
Formas de pagamento:
- LojaCerta ****-4567
```

---

## 10. Ciclo de Vida do Cartão

O cartão possui um estado operacional que determina se ele pode ser utilizado para novas operações.

Estados mínimos do domínio:

- `Ativo`;
- `Desativado`.

### 10.1 Cartão Ativo

Um cartão ativo pode ser utilizado para novos lançamentos, desde que o usuário possua permissão para utilizá-lo.

### 10.2 Desativação

O titular do contrato pode desativar um cartão conforme as permissões do sistema.

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação não deve:

- excluir o cartão;
- excluir lançamentos históricos;
- excluir faturas existentes;
- cancelar automaticamente obrigações financeiras existentes;
- remover o cartão do contrato;
- alterar retroativamente o histórico financeiro.

### 10.3 Cartão Desativado com Obrigações Existentes

A desativação de um cartão não encerra automaticamente as obrigações financeiras já criadas.

Parcelamentos existentes e demais compromissos financeiros continuam válidos conforme as regras do domínio de faturas.

### 10.4 Reativação

O titular pode reativar um cartão anteriormente desativado, quando permitido pelas regras do sistema.

A reativação:

- não cria um novo cartão;
- não cria um novo contrato;
- não altera o limite global;
- não elimina o histórico de desativação;
- não altera retroativamente lançamentos ou faturas existentes.

Após a reativação, o cartão volta a poder ser utilizado para novos lançamentos.

---

## 11. Permissões de Utilização

A posse do cartão e a autorização para utilização são conceitos distintos.

O titular do contrato possui controle sobre os cartões vinculados ao contrato, respeitando as regras de autorização do sistema.

O usuário de um cartão adicional possui autorização de utilização exclusivamente sobre o cartão adicional que recebeu.

Um usuário que recebeu acesso por compartilhamento possui autorização sobre o cartão compartilhado conforme a configuração do compartilhamento.

A autorização para utilização de um cartão não concede automaticamente acesso aos demais cartões do mesmo contrato.

As permissões específicas para consultar ou operar faturas serão definidas no documento de faturas.

---

## 12. Histórico

O histórico financeiro do cartão deve ser preservado mesmo quando o cartão for desativado.

A desativação não autoriza a remoção física dos registros relacionados ao cartão.

Devem permanecer rastreáveis, quando aplicável:

- identidade do cartão;
- vínculo com o contrato;
- usuário titular do cartão;
- usuários autorizados;
- alterações de estado;
- lançamentos realizados;
- operações relacionadas ao cartão;
- referências a faturas.

---

## 13. Relação com Faturas

A fatura pertence ao contrato de crédito e não a um cartão individual.

Cada contrato possui suas faturas conforme os ciclos definidos pelo domínio de faturas.

Uma fatura reúne os lançamentos de todos os cartões vinculados ao contrato que pertençam ao respectivo ciclo de faturamento.

A fatura pode conter lançamentos realizados por:

- cartão principal;
- cartões virtuais;
- cartões adicionais;
- cartões compartilhados.

O valor da fatura corresponde à soma dos lançamentos que compõem aquele ciclo para o contrato.

O titular do cartão e o usuário responsável por um lançamento são informações distintas e podem ser identificados individualmente dentro da fatura.

As regras detalhadas de criação, ciclo mensal, fechamento, vencimento, pagamento, quitação, créditos e demais regras de liquidação da fatura serão definidas em:

`CREDIT-CARD-INVOICES.md`

Este documento não deve duplicar as regras operacionais de faturas.

---

## 14. Invariantes do Domínio do Cartão

As seguintes regras devem permanecer verdadeiras:

1. Um cartão pertence a um contrato de crédito.
2. O contrato de crédito é uma representação abstrata da relação de crédito dentro do ambiente financeiro do usuário.
3. Um contrato possui uma única instituição mantenedora.
4. Todos os cartões de um contrato pertencem à mesma instituição mantenedora.
5. Cartões vinculados a contratos de instituições mantenedoras diferentes não podem ser misturados.
6. O limite global pertence ao contrato, não ao cartão individual.
7. Cartões vinculados ao mesmo contrato compartilham o limite global.
8. O primeiro cartão físico criado para o contrato é o cartão principal.
9. Cartão adicional cria um novo cartão dentro do mesmo contrato e é destinado ao usuário que o recebeu.
10. O usuário de um cartão adicional não recebe acesso automático aos demais cartões do contrato.
11. Cartão compartilhado é o mesmo cartão do titular com uso concedido a outro usuário e não cria um novo cartão.
12. Cartões adicionais e compartilhados não transferem a titularidade do contrato.
13. Cartões virtuais não criam novos contratos.
14. Um cartão não pode ser transferido de um contrato para outro.
15. Um lançamento de cartão compromete o limite no momento de seu registro.
16. O sistema não utiliza o limite como validação impeditiva de um novo lançamento, salvo regra futura explicitamente definida para isso.
17. Desativar um cartão impede novos lançamentos, mas não elimina o histórico financeiro existente.
18. Desativar um cartão não encerra automaticamente parcelamentos ou outras obrigações existentes.
19. Reativar um cartão não cria novo contrato nem novo histórico financeiro.
20. O número completo do cartão não é armazenado nem exibido pelo CyberBank.
21. A fatura pertence ao contrato e reúne os lançamentos dos cartões daquele contrato, não existindo uma fatura independente por cartão.
22. Regras de ciclo, fechamento, pagamento e quitação de faturas pertencem ao domínio de faturas e não devem ser duplicadas neste documento.
