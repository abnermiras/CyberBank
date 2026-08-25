# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o cartão de crédito como instrumento de utilização de um contrato de crédito: sua identidade, titularidade, relacionamento com o contrato, limite, formas de utilização, autorização e ciclo de vida operacional.

As regras específicas de **faturas, ciclos de faturamento, fechamento, pagamentos, quitação e créditos de fatura** serão definidas em documento próprio.

---

## 2. Conceito

Um cartão de crédito é um instrumento de pagamento vinculado a um contrato de crédito.

O contrato de crédito é uma representação abstrata da relação de crédito estabelecida pelo usuário dentro de seu ambiente financeiro. Ele não representa necessariamente um contrato físico, um documento ou um número de contrato fornecido pela instituição financeira.

O contrato é criado junto com o primeiro cartão de crédito e representa a relação de crédito daquele conjunto de cartões dentro do ambiente financeiro do usuário.

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

O contrato nasce quando o usuário cria o primeiro cartão de crédito e define a instituição que mantém esse crédito. Não existe, para o usuário, a criação de um contrato de crédito vazio e independente de cartão.

A instituição mantenedora pode ser:

- um banco já cadastrado no ambiente financeiro do usuário; ou
- uma instituição financeira declarada pelo próprio usuário.

Todos os cartões vinculados ao mesmo contrato pertencem à mesma instituição mantenedora.

Cartões vinculados a instituições mantenedoras diferentes pertencem a contratos diferentes e não podem ser misturados entre esses contratos.

### 3.1 Titularidade

O titular do contrato é o usuário que criou o primeiro cartão e estabeleceu a relação de crédito no seu ambiente financeiro.

A titularidade do contrato não é transferida pela criação de cartões adicionais ou pelo compartilhamento de cartões com outros usuários.

O titular do contrato possui controle sobre todos os cartões vinculados ao contrato, respeitando as regras específicas de autorização de cada tipo de cartão.

Quando todos os cartões do contrato estiverem desativados, o contrato permanece preservado para fins históricos e financeiros, mas fica inerte para novas utilizações do usuário.

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

Um cartão é criado dentro de um contrato de crédito existente ou durante a criação do primeiro cartão, quando o contrato ainda não existe.

O cadastro do cartão deve permitir identificar o instrumento de pagamento sem armazenar o número completo do cartão.

Informações estruturais de identificação incluem:

- referência ao contrato;
- tipo do cartão;
- meio de existência do cartão;
- últimos quatro dígitos, quando aplicável;
- titular do cartão;
- usuários autorizados, quando aplicável.

O nome do cartão não é um atributo independente obrigatório. A identificação apresentada ao usuário pode ser composta a partir do nome/produto associado ao contrato e das características do cartão.

Exemplos de identificação:

```text
Ultravioleta ****-1234 — Físico
Ultravioleta ****-0101 — Virtual
Ultravioleta Adicional ****-5678 — Físico
```

Os últimos quatro dígitos são apenas informação de identificação visual e não constituem identificador único do cartão.

O CyberBank não armazena o número completo do cartão.

Um cartão não possui data de validade ou data de emissão como parte de seu modelo de negócio.

O cartão não pode ser transferido de um contrato para outro. Sua associação com o contrato e com a instituição mantenedora permanece durante toda a existência do cartão.

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

O cartão principal pertence ao titular do contrato e possui meio de existência `Físico`.

O titular pode utilizar o cartão principal para realizar operações permitidas pelo sistema.

O cartão principal utiliza o limite global do contrato.

### 6.2 Cartão Virtual

Um cartão virtual é um cartão criado dentro de um contrato de crédito para utilização em meios digitais ou outras finalidades definidas pelo titular.

O contrato não possui limite de quantidade de cartões virtuais.

O cartão virtual:

- não cria um novo contrato;
- não possui limite global próprio;
- compartilha o limite do contrato;
- possui identidade própria;
- possui meio de existência `Virtual`;
- pode ser compartilhado;
- pode ser ativado, desativado ou bloqueado conforme as regras de autorização aplicáveis.

### 6.3 Cartão Adicional

Um cartão adicional é um novo cartão físico criado dentro do contrato do titular e concedido para uso de outro usuário.

O cartão adicional:

- continua vinculado ao contrato do titular;
- utiliza o limite global do contrato;
- não cria um novo contrato;
- possui identidade própria;
- possui meio de existência `Físico`;
- possui um usuário titular próprio;
- é destinado ao usuário que recebeu o cartão;
- não concede ao usuário acesso aos demais cartões do contrato.

Cartão adicional não pode ser virtual.

O cartão adicional é identificado visualmente pela indicação `Adicional`, pelos quatro últimos dígitos e pelo meio de existência `Físico`, além da identificação do produto/contrato quando apresentada ao usuário.

#### 6.3.1 Recebimento

O usuário destinatário deve receber um convite e aceitá-lo antes de utilizar o cartão adicional.

Ao aceitar o convite, o usuário escolhe em qual de seus ambientes financeiros o cartão será disponibilizado.

Após o aceite, o cartão passa a ser apresentado no ambiente escolhido pelo usuário como um cartão compartilhado de outro usuário, mantendo sua relação original com o contrato.

O usuário que recebe um cartão adicional pode visualizar e realizar lançamentos utilizando esse cartão, mas não pode visualizar ou utilizar os demais cartões do contrato aos quais não tenha autorização própria.

#### 6.3.2 Titularidade e Responsabilidade

O usuário que recebe o cartão adicional torna-se o titular operacional daquele cartão, mas não se torna titular do contrato de crédito.

O titular do contrato continua responsável pelo contrato e pelo limite global.

O titular do contrato pode visualizar e utilizar tanto seus próprios cartões quanto os cartões adicionais vinculados ao seu contrato.

O titular do cartão adicional pode desativar, reativar ou bloquear o próprio cartão, respeitando as regras de estado definidas neste documento.

#### 6.3.3 Revogação do Cartão Adicional

O titular do contrato pode revogar o cartão adicional concedido a outro usuário.

A revogação remove a autorização para novos lançamentos pelo usuário destinatário, mas não elimina imediatamente sua visualização do cartão quando ainda existirem obrigações ou lançamentos pendentes relacionados a ele.

Após o encerramento das obrigações pendentes, o cartão deixa de ser apresentado no ambiente do usuário que recebeu o adicional.

O cartão adicional revogado deve ser desativado.

Um cartão adicional revogado não pode ser reatribuído a outro usuário. Para outro usuário, deve ser criado um novo cartão adicional.

### 6.4 Cartão Compartilhado

Um cartão compartilhado é um cartão já existente do titular cujo uso é concedido a outro ou outros usuários.

O compartilhamento não cria um novo cartão e não cria um novo contrato.

Um mesmo cartão pode ser compartilhado com múltiplos usuários simultaneamente.

O cartão compartilhado:

- permanece vinculado ao contrato original;
- mantém sua identidade original;
- mantém seu titular original;
- utiliza o limite global do contrato;
- pode ser físico ou virtual;
- pode ser utilizado pelos usuários autorizados pelo compartilhamento.

O usuário autorizado pelo compartilhamento pode:

- visualizar os lançamentos do cartão;
- realizar lançamentos utilizando o cartão;
- participar das operações de pagamento relacionadas ao cartão, conforme as regras específicas de fatura.

O compartilhamento não transfere a titularidade do cartão nem a titularidade do contrato.

#### 6.4.1 Recebimento

O usuário destinatário deve receber um convite e aceitá-lo antes de utilizar o cartão compartilhado.

Ao aceitar o convite, o usuário escolhe em qual de seus ambientes financeiros o cartão será disponibilizado.

No ambiente escolhido, a instituição mantenedora é apresentada como uma instituição compartilhada, identificando o usuário titular que concedeu o acesso.

Exemplo:

```text
Instituição:
Nubank compartilhado de Usuário A

Forma de pagamento:
Ultravioleta ****-1234
```

ou:

```text
Instituição:
Lojas Crediários compartilhado de Usuário A

Forma de pagamento:
LojaCerta ****-4567
```

O cartão continua pertencendo ao contrato e ao ambiente de origem. O ambiente do usuário receptor possui apenas uma referência autorizada para utilização do cartão.

#### 6.4.2 Revogação do Compartilhamento

O titular do cartão pode remover o acesso concedido a um usuário compartilhado.

A revogação impede novos lançamentos pelo usuário removido.

O usuário removido continua podendo visualizar o cartão enquanto existirem lançamentos ou obrigações pendentes relacionadas à sua utilização.

Quando não existirem mais obrigações ou lançamentos pendentes relacionados ao acesso removido, o cartão deixa de ser apresentado no ambiente desse usuário.

A revogação do compartilhamento não desativa o cartão para os demais usuários autorizados ou para o titular.

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

O número completo do cartão não deve ser exibido no sistema.

Ao selecionar uma instituição no lançamento financeiro, o sistema deve apresentar as formas de pagamento disponíveis para aquela instituição. Os cartões de crédito exibidos devem corresponder aos cartões vinculados a contratos daquela instituição e aos quais o usuário possui autorização de utilização.

Para cartões recebidos por adicional ou compartilhamento, a instituição pode ser apresentada como uma instituição compartilhada do usuário que concedeu o acesso.

---

## 10. Ciclo de Vida do Cartão

O cartão possui três estados operacionais:

- `Ativo`;
- `Desativado`;
- `Bloqueado`.

### 10.1 Cartão Ativo

Um cartão ativo pode ser utilizado para novos lançamentos, desde que o usuário possua permissão para utilizá-lo.

O cartão ativo aparece normalmente na lista de meios de pagamento do usuário.

### 10.2 Cartão Desativado

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação não deve:

- excluir o cartão;
- excluir lançamentos históricos;
- excluir faturas existentes;
- cancelar automaticamente obrigações financeiras existentes;
- remover o cartão do contrato;
- alterar retroativamente o histórico financeiro.

Um cartão desativado pode continuar sendo visualizado enquanto existirem obrigações ou lançamentos pendentes relacionados ao cartão.

Após o encerramento dessas obrigações, o cartão permanece preservado para fins históricos, mas fica inerte para novas utilizações.

### 10.3 Cartão Bloqueado

O bloqueio é um mecanismo de organização da interface e da lista de meios de pagamento.

Um cartão bloqueado continua existindo e não tem seu histórico ou suas obrigações alterados.

O bloqueio impede que o cartão seja apresentado normalmente na lista de meios de pagamento do usuário, evitando poluição visual.

O bloqueio não representa desativação financeira e não encerra obrigações existentes.

O cartão pode ser desbloqueado e voltar a aparecer na lista de meios de pagamento.

### 10.4 Desativação e Reativação

O titular do contrato pode desativar ou reativar qualquer cartão do contrato a qualquer momento, respeitando as regras de integridade do sistema.

O titular de um cartão adicional pode desativar ou reativar o próprio cartão adicional.

Em um cartão compartilhado, somente o titular original do cartão pode desativá-lo ou reativá-lo.

A reativação:

- não cria um novo cartão;
- não cria um novo contrato;
- não altera o limite global;
- não elimina o histórico de estados;
- não altera retroativamente lançamentos ou faturas existentes.

---

## 11. Permissões de Utilização

A posse do cartão e a autorização para utilização são conceitos distintos.

O titular do contrato possui controle sobre todos os cartões vinculados ao contrato.

O titular de um cartão adicional possui autorização de utilização exclusivamente sobre o cartão adicional que recebeu.

Um usuário que recebeu acesso por compartilhamento possui autorização sobre o cartão compartilhado conforme o acesso concedido.

A autorização para utilização de um cartão não concede automaticamente acesso aos demais cartões do mesmo contrato.

A remoção do acesso a um cartão adicional ou compartilhado impede novos lançamentos pelo usuário removido, mas a visualização pode permanecer enquanto existirem obrigações ou lançamentos pendentes relacionados ao seu acesso.

As permissões específicas para consultar ou operar faturas serão definidas no documento de faturas.

---

## 12. Histórico

O histórico financeiro do cartão deve ser preservado mesmo quando o cartão for desativado.

A desativação, bloqueio ou remoção de acesso não autoriza a remoção física dos registros relacionados ao cartão.

Devem permanecer rastreáveis, quando aplicável:

- identidade do cartão;
- vínculo com o contrato;
- usuário titular do cartão;
- usuários autorizados;
- alterações de estado;
- concessões e revogações de acesso;
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

`CREDIT-CARDS-INVOICE.md`

Este documento não deve duplicar as regras operacionais de faturas.

---

## 14. Invariantes do Domínio do Cartão

As seguintes regras devem permanecer verdadeiras:

1. Um cartão pertence a um contrato de crédito.
2. O contrato de crédito é uma representação abstrata da relação de crédito dentro do ambiente financeiro do usuário.
3. O contrato nasce com a criação do primeiro cartão.
4. Um contrato possui uma única instituição mantenedora.
5. Todos os cartões de um contrato pertencem à mesma instituição mantenedora.
6. Cartões vinculados a contratos de instituições mantenedoras diferentes não podem ser misturados.
7. O limite global pertence ao contrato, não ao cartão individual.
8. Cartões vinculados ao mesmo contrato compartilham o limite global.
9. O primeiro cartão físico criado para o contrato é o cartão principal.
10. A quantidade de cartões virtuais de um contrato não possui limite definido pelo domínio.
11. Cartão adicional é sempre físico.
12. Cartão adicional cria um novo cartão dentro do mesmo contrato e possui um usuário titular próprio.
13. O usuário de um cartão adicional não recebe acesso automático aos demais cartões do contrato.
14. Cartão compartilhado é o mesmo cartão do titular com uso concedido a outro ou outros usuários e não cria um novo cartão.
15. Cartões virtuais podem ser compartilhados.
16. Cartões adicionais e compartilhados não transferem a titularidade do contrato.
17. Um cartão não pode ser transferido de um contrato para outro.
18. Cartão adicional revogado não pode ser reatribuído a outro usuário.
19. O titular do contrato pode desativar ou reativar qualquer cartão do contrato.
20. O titular de um cartão adicional pode desativar ou reativar o próprio cartão.
21. Somente o titular original pode desativar ou reativar um cartão compartilhado.
22. `Bloqueado` é um estado de apresentação/seleção e não equivale a desativação financeira.
23. Um cartão bloqueado continua existindo e preserva seu histórico e suas obrigações.
24. Desativar um cartão impede novos lançamentos, mas não elimina o histórico financeiro existente.
25. Remover o acesso de um usuário a um cartão adicional ou compartilhado impede novos lançamentos desse usuário, mas preserva sua visualização enquanto existirem obrigações ou lançamentos pendentes relacionados ao acesso.
26. A remoção do acesso não desativa o cartão para o titular ou para outros usuários autorizados.
27. O número completo do cartão não é armazenado nem exibido pelo CyberBank.
28. O cartão não possui data de validade ou data de emissão no modelo de negócio.
29. A fatura pertence ao contrato e reúne os lançamentos dos cartões daquele contrato, não existindo uma fatura independente por cartão.
30. Regras de ciclo, fechamento, pagamento e quitação de faturas pertencem ao domínio de faturas e não devem ser duplicadas neste documento.
