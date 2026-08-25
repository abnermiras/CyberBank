# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o cartão de crédito como instrumento de utilização de um contrato de crédito: sua identidade, titularidade, relacionamento com o contrato, limite, formas de utilização, autorização e ciclo de vida operacional.

As regras específicas de **faturas, ciclos de faturamento, fechamento, pagamentos, quitação e créditos de fatura** serão definidas em documento próprio.

---

## 2. Conceito

Um cartão de crédito é um instrumento de pagamento vinculado a um contrato de crédito.

O contrato de crédito é uma representação abstrata da relação de crédito estabelecida pelo usuário dentro de seu ambiente financeiro. Ele não representa necessariamente um contrato físico, um documento ou um número de contrato fornecido pela instituição financeira.

O contrato é criado obrigatoriamente junto com o primeiro cartão de crédito, que sempre é um cartão principal físico.

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

O titular do contrato possui **autoridade administrativa sobre todos os cartões vinculados ao contrato**.

Quando todos os cartões do contrato estiverem desativados, o contrato permanece preservado para fins históricos e financeiros, mas fica inerte para novas utilizações.

---

## 4. Entidade Mantenedora

Todo contrato de cartão de crédito possui uma instituição mantenedora.

A instituição mantenedora representa a entidade responsável pela manutenção do crédito e pode ser um banco cadastrado no ambiente financeiro ou uma instituição financeira declarada pelo usuário.

Quando a instituição mantenedora for um banco já cadastrado no ambiente financeiro, o contrato mantém uma referência a esse cadastro.

Quando a instituição mantenedora não estiver cadastrada como banco no ambiente financeiro, o usuário pode declará-la para representar a instituição responsável pelo cartão.

A ausência de um cadastro bancário não impede a existência ou utilização do cartão.

Um contrato possui uma única instituição mantenedora e todos os cartões vinculados a esse contrato pertencem a essa mesma instituição.

Se o usuário quiser representar uma mudança de instituição mantenedora ou de relação de crédito, deve criar um novo contrato. Um cartão existente não pode ser transferido para outro contrato ou instituição.

---

## 5. Cadastro e Identidade do Cartão

Um cartão é criado dentro de um contrato de crédito existente ou durante a criação do primeiro cartão, quando o contrato ainda não existe.

O CyberBank não armazena o número completo do cartão.

O cartão possui um **identificador técnico interno imutável**, utilizado pelo sistema para identificar univocamente a entidade durante toda a sua existência. Esse identificador não depende do estado do cartão, de seu titular, de seu compartilhamento ou de seus dados de apresentação.

Além do identificador técnico, o cartão possui uma identificação de negócio destinada à apresentação e reconhecimento pelo usuário. Ela considera, conforme aplicável:

- instituição financeira mantenedora;
- nome do contrato/produto;
- últimos quatro dígitos do cartão (`last4`);
- usuário titular do cartão.

Essa identificação de negócio não substitui o identificador técnico interno e não deve ser tratada como chave técnica da entidade.

Os quatro últimos dígitos são utilizados como componente de identificação e não devem ser tratados isoladamente como identificador único.

O cartão não possui data de validade ou data de emissão como parte de seu modelo de negócio.

O cartão não pode ser transferido de um contrato para outro. Sua associação com o contrato e com a instituição mantenedora permanece durante toda a existência do cartão.

O meio de existência do cartão é uma característica destinada à identificação do instrumento pelo usuário:

- `Físico`;
- `Virtual`.

O meio de existência não representa um tipo independente de contrato ou limite.

---

## 6. Tipos e Características de Cartão

Um contrato pode possuir cartões para diferentes finalidades.

Os conceitos de cartão são:

- cartão principal;
- cartão virtual;
- cartão adicional;
- cartão compartilhado.

`Virtual/Físico` é uma característica do cartão, e não um tipo independente.

`Compartilhado` também não constitui um tipo independente de cartão. É uma condição de autorização de uso que pode existir sobre cartões elegíveis ao compartilhamento.

Todos os cartões permanecem vinculados ao contrato original e utilizam seu limite global.

### 6.1 Cartão Principal

Todo contrato obrigatoriamente nasce com um cartão principal físico.

O cartão principal:

- é criado junto com o contrato;
- é sempre `Físico`;
- pertence ao titular do contrato;
- utiliza o limite global do contrato;
- representa a função de **cartão físico principal do titular do contrato**.

O conceito de `Principal` representa uma função do cartão físico atualmente ativo do titular do contrato. Não é uma identidade permanente que permaneça associada a todos os cartões históricos do titular.

Um contrato possui somente um cartão físico exercendo a função de principal por vez.

O titular do contrato pode criar um novo cartão físico principal. Ao criar o novo cartão:

1. o cartão físico anteriormente ativo do titular deixa de exercer a função de principal e é desativado;
2. o novo cartão físico é criado;
3. o novo cartão passa a ser o cartão físico ativo e principal do titular.

### 6.2 Cartão Virtual

Um cartão virtual é um cartão criado dentro de um contrato de crédito para utilização em meios digitais ou outras finalidades definidas pelo seu titular.

O contrato não possui limite de quantidade de cartões virtuais.

O cartão virtual:

- possui característica `Virtual`;
- não cria um novo contrato;
- não possui limite global próprio;
- compartilha o limite do contrato;
- possui identidade própria;
- pertence sempre ao usuário que o criou;
- pode ser compartilhado com outros usuários;
- pode ser desativado ou bloqueado conforme as regras deste documento.

Um cartão virtual não pode ser criado diretamente em nome de outro usuário. Para conceder utilização a outro usuário, deve ser utilizado o mecanismo de compartilhamento ou, quando aplicável, um cartão adicional.

Um cartão virtual não substitui o cartão físico principal e não transforma o contrato em outro tipo de contrato.

### 6.3 Cartão Adicional

Um cartão adicional é um novo cartão físico criado pelo titular do contrato e concedido para uso de outro usuário.

O cartão adicional:

- é sempre `Físico`;
- continua vinculado ao contrato do titular;
- utiliza o limite global do contrato;
- não cria um novo contrato;
- possui identidade técnica própria;
- possui um usuário titular próprio;
- não pode ser compartilhado com terceiros;
- pode ser utilizado pelo seu titular e pelo titular do contrato.

Cada usuário pode possuir no máximo um cartão adicional dentro de um mesmo contrato.

Um contrato pode possuir cartões adicionais para diferentes usuários.

Quando o titular do contrato cria um novo cartão adicional para determinado usuário, o cartão adicional físico anteriormente ativo daquele mesmo usuário é desativado e o novo cartão passa a ser o cartão físico ativo daquele usuário.

Um cartão adicional desativado permanece associado ao seu titular original. Ele **não pode ser convertido, transferido ou reatribuído** para outro usuário. Para conceder um cartão adicional a outro usuário, deve ser criado um novo cartão adicional.

#### 6.3.1 Recebimento

O usuário destinatário deve receber um convite e aceitá-lo antes de utilizar o cartão adicional.

Ao aceitar o convite, o usuário escolhe em qual de seus ambientes financeiros o cartão será disponibilizado.

O usuário que recebe um cartão adicional pode visualizar e realizar lançamentos utilizando esse cartão, mas não recebe acesso aos demais cartões do contrato aos quais não tenha autorização própria.

O cartão adicional não é um empréstimo de um cartão existente. Ele possui titular próprio e identidade própria.

#### 6.3.2 Titularidade e Responsabilidade

O usuário que recebe o cartão adicional é o titular do cartão adicional, mas não se torna titular do contrato de crédito.

O titular do contrato continua responsável pelo contrato e pelo limite global.

O titular do contrato possui autoridade administrativa sobre todos os cartões vinculados ao contrato.

O titular do cartão adicional pode utilizar e bloquear ou desativar o próprio cartão adicional, conforme as regras de estado deste documento.

O titular do cartão adicional não pode desbloquear ou reativar o cartão. Essas operações são exclusivas do titular do contrato.

#### 6.3.3 Revogação do Cartão Adicional

O titular do contrato pode revogar o cartão adicional concedido a outro usuário.

A revogação remove a autorização para novos lançamentos pelo usuário destinatário e desativa o cartão adicional.

O cartão adicional revogado permanece vinculado ao seu titular original para fins históricos e **não pode ser reatribuído a outro usuário**.

---

### 6.4 Cartão Compartilhado

Um cartão elegível pode ter seu uso concedido a outro ou outros usuários por meio de compartilhamento.

O compartilhamento não cria um novo cartão e não cria um novo contrato.

Um mesmo cartão pode ser compartilhado com múltiplos usuários simultaneamente, sem limite de quantidade definido pelo domínio.

O cartão compartilhado:

- mantém sua identidade técnica original;
- mantém seu titular original;
- permanece vinculado ao contrato original;
- utiliza o limite global do contrato;
- pode ser físico ou virtual;
- pode ser utilizado pelos usuários autorizados pelo compartilhamento.

O cartão adicional não pode ser compartilhado.

O usuário autorizado pelo compartilhamento pode:

- visualizar os lançamentos do cartão compartilhado;
- visualizar as informações de fatura às quais recebeu autorização;
- realizar lançamentos utilizando o cartão compartilhado;
- realizar o pagamento da fatura relacionada ao cartão compartilhado, inclusive pagamento parcial ou total, conforme as regras do domínio de faturas;
- sair do compartilhamento.

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
Ultravioleta ****-4567
```

O cartão continua pertencendo ao contrato e ao ambiente de origem. O ambiente do usuário receptor possui apenas uma referência autorizada para utilização do cartão.

#### 6.4.2 Saída do Compartilhamento

O usuário que recebeu acesso a um cartão compartilhado pode sair do compartilhamento a qualquer momento.

Sair do compartilhamento remove somente a autorização de uso daquele usuário.

A saída do compartilhamento:

- não desativa o cartão;
- não altera o estado do cartão;
- não afeta o titular original;
- não afeta outros usuários autorizados;
- impede novos lançamentos realizados pelo usuário que saiu.

O usuário que saiu do compartilhamento pode **visualizar o próprio histórico de utilização daquele cartão**, conforme as regras de histórico e acesso do domínio.

A saída do compartilhamento não apaga lançamentos realizados anteriormente pelo usuário e não remove esses registros do histórico financeiro do contrato.

O titular do contrato pode remover o compartilhamento de qualquer usuário.

A remoção do compartilhamento pelo titular produz o mesmo efeito sobre o usuário removido: ele deixa de poder realizar novos lançamentos, sem alterar o estado do cartão para os demais usuários autorizados.

---

## 7. Criação e Substituição de Cartões Físicos

Cada usuário pode possuir no máximo um cartão físico ativo dentro de um determinado contrato.

Essa regra se aplica ao titular do contrato e aos titulares de cartões adicionais.

Para o titular do contrato, o cartão físico ativo exerce a função de cartão principal.

Quando um novo cartão físico é criado para um usuário que já possui um cartão físico ativo naquele contrato:

1. o cartão físico anterior é desativado;
2. o novo cartão físico é criado;
3. o novo cartão passa a ficar ativo;
4. se o usuário for o titular do contrato, o novo cartão passa a exercer a função de principal;
5. os cartões físicos de outros usuários do mesmo contrato não são afetados.

Exemplo:

```text
Contrato: Ultravioleta

Usuário A — titular do contrato
  ****-1234 — Físico — Desativado
  ****-9999 — Físico — Ativo — Principal

Usuário B — adicional
  ****-5678 — Físico — Ativo

Usuário C — adicional
  ****-7777 — Físico — Ativo
```

A criação de um novo cartão físico pelo Usuário A não desativa o cartão físico ativo do Usuário B ou do Usuário C.

### 7.1 Substituição de Cartão Físico Compartilhado

Um cartão físico pode estar compartilhado com outros usuários.

Se esse cartão físico for substituído por um novo cartão físico:

1. o cartão físico anterior é desativado;
2. o novo cartão físico passa a ser o cartão físico ativo do seu titular;
3. o cartão físico anterior permanece preservado para fins históricos;
4. os compartilhamentos associados ao cartão anterior deixam de conceder autorização de utilização;
5. os usuários que anteriormente utilizavam o cartão compartilhado não podem utilizar o cartão físico anterior após sua substituição.

A substituição física não transfere os compartilhamentos para o novo cartão. Se o titular quiser compartilhar o novo cartão, deverá conceder novos compartilhamentos.

---

## 8. Limite Global

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

### 8.1 Alteração do Limite

Somente o titular do contrato pode alterar o limite global.

O limite global deve ser maior ou igual a zero.

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

## 9. Consumo do Limite

Um lançamento realizado com cartão de crédito compromete o limite global do contrato no momento em que o lançamento é registrado.

O comprometimento do limite não depende de o lançamento estar marcado como `Previsto` ou `Realizado`.

O **crédito comprometido** corresponde ao valor das operações de crédito que continuam consumindo o limite do contrato e cujo limite ainda não foi liberado.

Não fazem parte do crédito comprometido:

- operações cujo limite já foi liberado;
- operações que deixaram de produzir comprometimento de limite conforme as regras de liquidação ou correção do domínio financeiro.

A desativação ou bloqueio de um cartão não libera automaticamente o limite comprometido por seus lançamentos.

A existência de parcelamentos vinculados a um cartão desativado também não elimina a obrigação financeira nem libera automaticamente todo o valor comprometido. A liberação seguirá as regras do domínio de faturas e liquidação.

Não existe, neste documento, uma operação independente de cancelamento ou estorno de cartão. As regras de correção de lançamentos e de liberação de limite serão tratadas pelos respectivos domínios.

### 9.1 Compra à Vista

Uma compra à vista compromete o valor total da operação.

### 9.2 Compra Parcelada

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

## 10. Cartão como Forma de Pagamento

O cartão de crédito pode ser utilizado como forma de pagamento em um lançamento de saída.

O cartão selecionado identifica qual instrumento de crédito foi utilizado na operação.

O lançamento financeiro permanece sendo uma única operação. O uso do cartão não cria uma segunda movimentação apenas por aparecer também na fatura.

O número completo do cartão não deve ser exibido no sistema.

Ao selecionar uma instituição no lançamento financeiro, o sistema deve apresentar as formas de pagamento disponíveis para aquela instituição. Os cartões de crédito exibidos devem corresponder aos cartões vinculados a contratos daquela instituição e aos quais o usuário possui autorização de utilização.

Para cartões recebidos por adicional ou compartilhamento, a instituição pode ser apresentada como uma instituição compartilhada do usuário que concedeu o acesso.

---

## 11. Ciclo de Vida do Cartão

O cartão possui três estados operacionais:

- `Ativo`;
- `Bloqueado`;
- `Desativado`.

`Bloqueado` é um estado de impedimento de utilização, mas o cartão continua ativo para fins de existência, titularidade, histórico e demais relações do domínio.

### 11.1 Cartão Ativo

Um cartão ativo permite novos lançamentos, desde que o usuário possua autorização para utilizá-lo.

O cartão ativo pode aparecer normalmente na lista de meios de pagamento do usuário.

### 11.2 Cartão Bloqueado

Um cartão bloqueado continua sendo um cartão ativo, porém fica impedido de receber novos lançamentos.

O bloqueio não:

- desativa o cartão;
- altera sua titularidade;
- exclui lançamentos históricos;
- altera faturas existentes;
- encerra obrigações financeiras;
- remove o cartão do contrato.

O bloqueio é um impedimento operacional de utilização.

Um cartão bloqueado pode voltar ao estado `Ativo` mediante desbloqueio por quem possuir autoridade para essa operação.

O bloqueio não cancela automaticamente operações recorrentes. Se o usuário quiser impedir uma recorrência, deverá acessar a própria recorrência e cancelá-la conforme as regras desse domínio.

### 11.3 Cartão Desativado

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação não deve:

- excluir o cartão;
- excluir lançamentos históricos;
- excluir faturas existentes;
- eliminar obrigações financeiras já existentes;
- remover o cartão do histórico do contrato;
- alterar retroativamente o histórico financeiro.

A desativação encerra a utilização futura do cartão e cancela as recorrências vinculadas ao cartão, conforme as regras do domínio de recorrências.

A desativação **não cancela parcelamentos nem elimina obrigações financeiras já existentes**. Parcelamentos e demais obrigações permanecem vinculados ao contrato e ao histórico financeiro até sua liquidação conforme as regras do domínio de faturas.

Um cartão desativado pode continuar sendo visualizado para fins históricos e enquanto existirem obrigações ou lançamentos relacionados a ele.

### 11.4 Transições de Estado

As transições permitidas são:

```text
                 ┌──────────────┐
                 │    ATIVO     │
                 └──────┬───────┘
                        │
             ┌──────────┴──────────┐
             ▼                     ▼
      ┌──────────────┐      ┌──────────────┐
      │  BLOQUEADO   │      │ DESATIVADO   │
      └──────┬───────┘      └──────┬───────┘
             │                     │
             └──────► ATIVO ◄──────┘
```

Regras:

- `Ativo → Bloqueado`: permitido;
- `Bloqueado → Ativo`: permitido somente ao titular do contrato;
- `Ativo → Desativado`: permitido;
- `Bloqueado → Desativado`: permitido;
- `Desativado → Ativo`: permitido somente ao titular do contrato;
- `Desativado → Bloqueado`: não permitido nem necessário.

Permissões para alteração de estado:

- o titular do contrato pode bloquear, desbloquear, desativar e reativar qualquer cartão do contrato;
- o titular de um cartão adicional pode bloquear ou desativar o próprio cartão adicional;
- o titular de um cartão adicional não pode desbloquear nem reativar o próprio cartão;
- um usuário que recebeu um cartão compartilhado não pode alterar o estado do cartão;
- sair do compartilhamento apenas remove a autorização daquele usuário.

---

## 12. Permissões de Utilização

A posse do cartão e a autorização para utilização são conceitos distintos.

O titular do contrato possui **autoridade administrativa sobre todos os cartões vinculados ao contrato**.

Isso inclui, conforme aplicável:

- criar cartões;
- criar cartões adicionais;
- criar cartões virtuais;
- compartilhar cartões elegíveis;
- remover compartilhamentos;
- bloquear cartões;
- desbloquear cartões;
- desativar cartões;
- reativar cartões.

O titular de um cartão adicional possui autorização de utilização exclusivamente sobre o cartão adicional que recebeu.

O titular de um cartão adicional pode bloquear ou desativar o próprio cartão, mas não pode desbloqueá-lo nem reativá-lo.

Um usuário que recebeu acesso por compartilhamento possui autorização para utilizar o cartão compartilhado conforme o acesso concedido.

O usuário compartilhado pode realizar lançamentos e, quando autorizado pelo domínio de faturas, realizar pagamentos da fatura relacionada ao cartão compartilhado.

O usuário compartilhado pode sair do compartilhamento, removendo somente sua própria autorização de uso.

A autorização para utilização de um cartão não concede automaticamente acesso aos demais cartões do mesmo contrato.

Todo lançamento realizado com cartão deve registrar obrigatoriamente o usuário responsável pela realização.

As permissões específicas para consultar ou operar faturas serão definidas no documento de faturas, exceto pela regra de que o usuário que possui um cartão compartilhado pode realizar o pagamento da fatura relacionada ao cartão compartilhado.

---

## 13. Histórico

O histórico financeiro do cartão deve ser preservado mesmo quando o cartão for desativado.

A desativação, bloqueio ou remoção de acesso não autoriza a remoção física dos registros relacionados ao cartão.

Devem permanecer rastreáveis, quando aplicável:

- identificador técnico interno imutável do cartão;
- identificação de negócio do cartão;
- vínculo com o contrato;
- usuário titular do cartão;
- usuários autorizados;
- alterações de estado;
- concessões e revogações de acesso;
- saídas de compartilhamento;
- lançamentos realizados;
- usuário responsável por cada lançamento;
- operações relacionadas ao cartão;
- referências a faturas.

Quando um usuário perder o acesso a um cartão compartilhado, os lançamentos históricos realizados por ele continuam preservados no histórico financeiro do contrato e o usuário pode visualizar o próprio histórico de utilização, conforme as regras de acesso do domínio.

A substituição ou desativação de um cartão não altera retroativamente seus lançamentos nem transfere esses lançamentos para outro cartão.

---

## 14. Relação com Faturas

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

Um usuário autorizado por compartilhamento pode realizar o pagamento parcial ou total da fatura relacionada ao cartão compartilhado, quando essa operação estiver disponível no domínio de faturas. Os detalhes operacionais do pagamento permanecem definidos em `CREDIT-CARDS-INVOICE.md`.

As regras detalhadas de criação, ciclo mensal, fechamento, vencimento, pagamento, quitação, créditos e demais regras de liquidação da fatura serão definidas em:

`CREDIT-CARDS-INVOICE.md`

Este documento não deve duplicar as regras operacionais de faturas.

---

## 15. Invariantes do Domínio do Cartão

As seguintes regras devem permanecer verdadeiras:

1. Um cartão pertence a um contrato de crédito.
2. O contrato de crédito é uma representação abstrata da relação de crédito dentro do ambiente financeiro do usuário.
3. Todo contrato nasce obrigatoriamente com um cartão principal físico.
4. Um contrato possui uma única instituição mantenedora.
5. Todos os cartões de um contrato pertencem à mesma instituição mantenedora.
6. Cartões vinculados a instituições mantenedoras diferentes pertencem a contratos diferentes.
7. Um cartão não pode ser transferido de um contrato para outro.
8. Todo cartão possui um identificador técnico interno imutável.
9. A identificação de negócio do cartão não substitui o identificador técnico.
10. `Físico/Virtual` é uma característica do cartão, não um contrato ou limite independente.
11. O limite global pertence ao contrato, não ao cartão individual.
12. Cartões vinculados ao mesmo contrato compartilham o limite global.
13. Um contrato possui somente um cartão físico exercendo a função de principal por vez.
14. A função de principal pertence ao cartão físico ativo do titular do contrato.
15. Cada usuário pode possuir no máximo um cartão físico ativo dentro de um contrato.
16. A criação de novo cartão físico para um usuário desativa o cartão físico anterior daquele usuário.
17. A quantidade de cartões virtuais de um contrato não possui limite definido pelo domínio.
18. Cartão virtual pertence sempre ao usuário que o criou.
19. Cartão virtual não pode ser criado diretamente em nome de outro usuário.
20. Cartão adicional é sempre físico.
21. Cada usuário pode possuir no máximo um cartão adicional dentro de um contrato.
22. Cartão adicional possui titular próprio e não pode ser compartilhado.
23. Cartão adicional desativado ou revogado não pode ser convertido, transferido ou reatribuído a outro usuário.
24. O titular do contrato possui autoridade administrativa sobre todos os cartões do contrato.
25. O titular do cartão adicional pode bloquear ou desativar o próprio cartão, mas não pode desbloquear nem reativar.
26. Cartão compartilhado é o mesmo cartão do titular com autorização de uso concedida a outros usuários.
27. Um cartão compartilhado pode ser utilizado por quantos usuários o domínio permitir e o titular autorizar.
28. O usuário compartilhado pode realizar lançamentos utilizando o cartão compartilhado.
29. O usuário compartilhado pode realizar pagamento parcial ou total da fatura relacionada ao cartão compartilhado, conforme o domínio de faturas.
30. O usuário compartilhado pode sair do compartilhamento sem alterar o estado do cartão.
31. Ao sair do compartilhamento, o usuário perde a autorização para novos lançamentos, mas pode visualizar o próprio histórico de utilização conforme as regras de acesso.
32. Cartões virtuais podem ser compartilhados.
33. Cartões adicionais não podem ser compartilhados.
34. Compartilhamento não transfere a titularidade do cartão nem do contrato.
35. A substituição física de um cartão compartilhado encerra a utilização do cartão físico anterior por todos os usuários compartilhados.
36. A substituição física não transfere automaticamente os compartilhamentos para o novo cartão.
37. Todo lançamento com cartão deve registrar o usuário responsável pela realização.
38. O número completo do cartão não é armazenado nem exibido pelo CyberBank.
39. O cartão não possui data de validade ou data de emissão no modelo de negócio.
40. `Ativo` permite novos lançamentos quando houver autorização de uso.
41. `Bloqueado` mantém o cartão existente, mas impede novos lançamentos.
42. `Desativado` impede novos lançamentos e encerra a utilização futura do cartão.
43. `Ativo → Bloqueado` é permitido.
44. `Bloqueado → Ativo` é permitido somente ao titular do contrato.
45. `Ativo → Desativado` é permitido.
46. `Bloqueado → Desativado` é permitido.
47. `Desativado → Ativo` é permitido somente ao titular do contrato.
48. `Desativado → Bloqueado` não é permitido nem necessário.
49. O titular do contrato pode bloquear, desbloquear, desativar e reativar qualquer cartão do contrato.
50. O usuário que recebeu um cartão compartilhado não pode bloquear, desbloquear, desativar ou reativar o cartão por causa do compartilhamento.
51. Desativação não exclui histórico financeiro.
52. Bloqueio não cancela automaticamente recorrências.
53. Desativação cancela as recorrências vinculadas ao cartão.
54. Desativação não cancela parcelamentos nem elimina obrigações financeiras já existentes.
55. O limite global somente pode ser alterado pelo titular do contrato.
56. O limite global deve ser maior ou igual a zero.
57. O limite disponível pode ser negativo.
58. O crédito comprometido corresponde às operações de crédito que continuam consumindo limite e cujo limite ainda não foi liberado.
59. Bloqueio ou desativação do cartão não libera automaticamente o limite comprometido.
60. Parcelamentos vinculados a cartão desativado continuam sendo obrigações financeiras.
61. O comprometimento do limite independe de o lançamento estar `Previsto` ou `Realizado`.
62. A fatura pertence ao contrato e reúne os lançamentos dos cartões daquele contrato, não existindo uma fatura independente por cartão.
63. Regras de ciclo, fechamento, pagamento, quitação e créditos de fatura pertencem ao domínio de faturas e não devem ser duplicadas neste documento.
64. A substituição ou desativação de um cartão não altera retroativamente os lançamentos já registrados.
65. O identificador técnico interno do cartão permanece imutável durante todo o ciclo de vida da entidade.
