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

O titular do contrato possui controle sobre todos os cartões vinculados ao contrato.

Quando todos os cartões do contrato estiverem desativados, o contrato permanece preservado para fins históricos e financeiros, mas fica inerte para novas utilizações.

---

## 4. Entidade Mantenedora

Todo contrato de cartão de crédito possui uma instituição mantenedora.

A instituição mantenedora representa a entidade responsável pela manutenção do crédito e pode ser um banco cadastrado no ambiente financeiro ou uma instituição financeira declarada pelo usuário.

Quando a instituição mantenedora for um banco já cadastrado no ambiente financeiro, o contrato mantém uma referência a esse cadastro.

Quando a instituição mantenedora não estiver cadastrada como banco no ambiente financeiro, o usuário pode declará-la para representar a instituição responsável pelo cartão.

A ausência de um cadastro bancário não impede a existência ou utilização do cartão.

Um contrato possui uma única instituição mantenedora e todos os cartões vinculados a esse contrato pertencem a essa mesma instituição.

A mudança de instituição mantenedora ou a representação de uma nova relação de crédito com outra instituição exige a criação de um novo contrato. Um contrato existente não é migrado de uma instituição para outra.

---

## 5. Identidade e Cadastro do Cartão

Um cartão é criado dentro de um contrato de crédito existente ou durante a criação do primeiro cartão, quando o contrato ainda não existe.

A identificação única de um cartão no domínio é composta pela combinação de:

- instituição financeira declarada ou banco associado ao contrato;
- nome do contrato/produto, por exemplo `Ultravioleta`;
- últimos quatro dígitos do cartão (`last4`);
- usuário titular do cartão.

Essa composição garante a diferenciação entre cartões mesmo quando usuários diferentes possuam instituição, nome de contrato e `last4` iguais.

Os últimos quatro dígitos são apenas uma parte da identidade composta e não devem ser tratados isoladamente como identificador único.

O CyberBank não armazena o número completo do cartão.

O cadastro do cartão deve registrar, conforme aplicável:

- referência ao contrato;
- instituição mantenedora;
- nome do contrato/produto;
- `last4`;
- usuário titular do cartão;
- característica física ou virtual;
- situação de compartilhamento;
- usuários autorizados;
- histórico de estados.

O nome apresentado para identificação do cartão pode ser derivado do nome do contrato/produto e de uma descrição definida pelo usuário para cartões virtuais.

Exemplos:

```text
Ultravioleta ****-1234 — Físico
Ultravioleta / Assinaturas ****-4567 — Virtual
Ultravioleta / Compras ****-7890 — Virtual
Ultravioleta / Adicional ****-0101 — Físico
```

Um cartão não possui data de validade ou data de emissão como parte de seu modelo de negócio.

Um cartão não pode ser transferido de um contrato para outro. Sua associação com o contrato e com a instituição mantenedora permanece durante toda a existência do cartão.

---

## 6. Tipos e Características do Cartão

O domínio não trata `Físico` e `Virtual` como tipos de cartão. Eles são **características de identificação do meio de existência do cartão**, utilizadas principalmente para auxiliar o usuário na identificação do instrumento.

Os papéis de negócio considerados pelo domínio são:

- cartão principal;
- cartão adicional.

O estado de compartilhamento é uma característica independente do papel do cartão. **Qualquer cartão elegível pode ser compartilhado**, exceto cartões adicionais, que possuem uso exclusivo entre o titular do contrato e o usuário titular do adicional.

Todos os cartões permanecem vinculados ao contrato original e utilizam seu limite global.

### 6.1 Cartão Principal

O contrato obrigatoriamente nasce com um cartão principal físico.

O primeiro cartão físico criado para o contrato é o cartão principal inicial.

O cartão principal:

- pertence ao titular do contrato;
- possui característica `Físico`;
- utiliza o limite global do contrato;
- pode ser utilizado pelo titular do contrato;
- pode ser compartilhado com outros usuários;
- pode ser substituído pela criação de um novo cartão físico principal.

Um contrato possui somente um cartão principal ativo por vez.

Quando o titular do contrato cria um novo cartão físico principal, o cartão físico principal anteriormente ativo é desativado e o novo cartão passa a ser o cartão principal ativo.

### 6.2 Cartão Virtual

Um cartão virtual é um cartão criado dentro de um contrato de crédito para utilização em meios digitais ou outras finalidades definidas pelo titular.

A quantidade de cartões virtuais de um contrato é ilimitada.

Um cartão virtual:

- não cria um novo contrato;
- não possui limite global próprio;
- compartilha o limite do contrato;
- possui identidade própria;
- possui característica `Virtual`;
- pode possuir uma descrição definida pelo usuário, como `Assinaturas` ou `Compras`;
- pode ser compartilhado;
- pode ser ativado, desativado ou bloqueado conforme as regras de estado.

Cartões virtuais são derivados do contrato e não substituem o cartão principal físico.

### 6.3 Cartão Adicional

Um cartão adicional é um novo cartão físico criado pelo titular do contrato para uso exclusivo de outro usuário.

O cartão adicional:

- continua vinculado ao contrato do titular;
- utiliza o limite global do contrato;
- não cria um novo contrato;
- possui identidade própria;
- possui característica `Físico`;
- possui um usuário titular próprio;
- é destinado exclusivamente ao usuário escolhido pelo titular do contrato.

A quantidade de cartões adicionais é limitada a **um cartão adicional por usuário destinatário dentro do mesmo contrato**.

Exemplo:

```text
Contrato: Ultravioleta
Titular do contrato: Usuário A

Cartão adicional — Usuário B — ****-0101
Cartão adicional — Usuário C — ****-0202
```

Um usuário não pode possuir dois cartões adicionais simultaneamente no mesmo contrato.

Cartão adicional não pode ser compartilhado com terceiros. Seu uso é exclusivo entre o titular do contrato e o usuário titular do cartão adicional.

#### 6.3.1 Recebimento

O usuário destinatário deve receber um convite e aceitá-lo antes de utilizar o cartão adicional.

Ao aceitar o convite, o usuário escolhe em qual de seus ambientes financeiros o cartão será disponibilizado.

O usuário que recebe um cartão adicional pode visualizar as faturas do contrato e todos os lançamentos realizados no cartão adicional ao qual possui titularidade operacional.

O usuário de um cartão adicional não recebe automaticamente autorização para consultar ou utilizar os demais cartões do contrato.

#### 6.3.2 Titularidade e Responsabilidade

O usuário que recebe o cartão adicional é o titular operacional daquele cartão, mas não se torna titular do contrato de crédito.

O titular do contrato continua responsável pelo contrato e pelo limite global.

O titular do contrato pode visualizar todas as faturas e realizar lançamentos em todos os cartões do contrato.

O titular do cartão adicional pode utilizar o próprio cartão e pode desativá-lo ou reativá-lo conforme as regras de estado definidas neste documento.

#### 6.3.3 Revogação do Cartão Adicional

O titular do contrato pode revogar o cartão adicional concedido a outro usuário.

A revogação impede novos lançamentos pelo usuário destinatário e desativa o cartão.

A revogação não elimina o histórico financeiro do cartão nem os lançamentos já realizados.

Após a revogação, o cartão permanece preservado para fins históricos e financeiros.

Um cartão adicional revogado não pode ser reatribuído a outro usuário. Para outro usuário, deve ser criado um novo cartão adicional.

### 6.4 Cartão Compartilhado

O compartilhamento não é um tipo de cartão. É uma situação de uso que pode ser aplicada a cartões permitidos pelo domínio.

Um cartão compartilhado é o mesmo cartão original, cujo uso é concedido pelo titular do contrato a outro ou outros usuários.

O compartilhamento:

- não cria um novo cartão;
- não cria um novo contrato;
- não transfere a titularidade do cartão;
- não transfere a titularidade do contrato;
- pode ser aplicado a cartões físicos e virtuais;
- pode atingir múltiplos usuários simultaneamente;
- possui quantidade ilimitada de usuários por cartão, conforme decisão do titular do contrato.

Cartões adicionais não podem ser compartilhados.

O usuário autorizado por compartilhamento pode:

- visualizar os lançamentos do cartão compartilhado;
- visualizar o extrato/fatura relacionada ao cartão compartilhado;
- realizar lançamentos utilizando o cartão compartilhado;
- participar das operações de pagamento relacionadas ao cartão, conforme as regras específicas de fatura.

O usuário que recebe um cartão compartilhado acessa somente o cartão compartilhado e os dados financeiros relacionados a ele. O compartilhamento não concede acesso aos demais cartões do contrato.

#### 6.4.1 Recebimento

O usuário destinatário deve receber um convite e aceitá-lo antes de utilizar o cartão compartilhado.

Ao aceitar o convite, o usuário escolhe em qual de seus ambientes financeiros o cartão será disponibilizado.

No ambiente escolhido, a instituição mantenedora pode ser apresentada como uma instituição compartilhada, identificando o usuário titular que concedeu o acesso.

Exemplo:

```text
Instituição:
Nubank compartilhado de Usuário A

Forma de pagamento:
Ultravioleta ****-1234
```

O cartão continua pertencendo ao contrato e ao ambiente de origem. O ambiente do usuário receptor possui apenas uma referência autorizada para utilização do cartão.

O mesmo usuário não pode receber o mesmo cartão por compartilhamento mais de uma vez simultaneamente.

#### 6.4.2 Revogação do Compartilhamento

Somente o titular do contrato pode conceder ou remover compartilhamentos.

A revogação impede novos lançamentos pelo usuário removido.

A revogação não desativa o cartão para o titular do contrato ou para outros usuários autorizados.

O histórico das operações realizadas pelo usuário removido deve ser preservado.

O lançamento realizado pelo usuário removido continua visível no histórico do próprio usuário mesmo após a revogação do acesso e após a quitação da obrigação correspondente.

O usuário removido deixa de possuir acesso operacional ao cartão para novos lançamentos.

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

Somente o titular do contrato pode alterar o limite global.

O limite global deve ser sempre maior ou igual a zero:

```text
Limite global >= 0
```

A alteração do limite não altera retroativamente os lançamentos já realizados.

O limite global pode ser reduzido para valor inferior ao crédito atualmente comprometido.

Nesse caso, o sistema deve preservar o estado financeiro existente e permitir que o limite disponível seja representado como negativo.

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

**Crédito comprometido** é a soma das operações de crédito ainda não liberadas.

Devem ser excluídas do crédito comprometido as operações:

- canceladas;
- estornadas;
- cujo limite já foi liberado.

O limite comprometido representa o valor de crédito atualmente utilizado pelo contrato e será liberado conforme as regras financeiras aplicáveis ao lançamento e à fatura.

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

### 8.3 Estorno e Cancelamento

As regras de estorno, cancelamento parcial, cancelamento total e seus respectivos efeitos sobre o limite comprometido permanecem **pendentes de definição**.

Este tema será definido em refinamento específico antes da consolidação final do domínio de cartões e faturas.

---

## 9. Cartão como Forma de Pagamento

O cartão de crédito pode ser utilizado como forma de pagamento em um lançamento de saída.

O cartão selecionado identifica qual instrumento de crédito foi utilizado na operação.

Todo lançamento com cartão deve registrar obrigatoriamente o usuário que realizou a operação.

O lançamento financeiro permanece sendo uma única operação. O uso do cartão não cria uma segunda movimentação apenas por aparecer também na fatura.

O número completo do cartão não deve ser exibido no sistema.

Ao selecionar uma instituição no lançamento financeiro, o sistema deve apresentar as formas de pagamento disponíveis para aquela instituição. Os cartões de crédito exibidos devem corresponder aos cartões vinculados a contratos daquela instituição e aos quais o usuário possui autorização de utilização.

Para cartões recebidos por adicional ou compartilhamento, a instituição pode ser apresentada como uma instituição compartilhada do usuário que concedeu o acesso.

---

## 10. Ciclo de Vida do Cartão

O cartão pode apresentar os estados operacionais:

- `Ativo`;
- `Desativado`;
- `Bloqueado`.

`Bloqueado` não substitui `Ativo` ou `Desativado` como estado de existência. Ele representa um **impeditivo temporário de lançamento sobre um cartão que permanece ativo**.

### 10.1 Cartão Ativo

Um cartão ativo permite novos lançamentos, desde que o usuário possua autorização para utilizá-lo e o cartão não esteja bloqueado.

### 10.2 Cartão Desativado

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação é lógica e não exclui o cartão.

A desativação não deve:

- excluir o cartão;
- excluir lançamentos históricos;
- excluir faturas existentes;
- remover o cartão do histórico do contrato;
- alterar retroativamente o histórico financeiro.

O cartão desativado permanece preservado para fins históricos e financeiros.

A desativação de um cartão encerra as recorrências de lançamentos associadas a esse cartão.

Após a desativação, o cartão não pode originar novos lançamentos.

### 10.3 Cartão Bloqueado

O bloqueio impede lançamentos no cartão, mas o cartão continua ativo.

O bloqueio não representa desativação financeira e não encerra obrigações existentes.

O bloqueio não cancela recorrências associadas ao cartão.

Para cancelar uma recorrência de um cartão bloqueado, o usuário deve acessar a própria recorrência e efetuar seu cancelamento.

Um cartão bloqueado continua pertencendo ao contrato, mantém seu histórico e pode voltar ao estado normal de utilização por meio da remoção do bloqueio.

### 10.4 Transições

As transições permitidas são:

```text
ATIVO -> DESATIVADO
ATIVO -> BLOQUEADO

BLOQUEADO -> ATIVO
BLOQUEADO -> DESATIVADO

DESATIVADO -> ATIVO
```

Não existe necessidade de transição `DESATIVADO -> BLOQUEADO`, pois um cartão desativado já impede novos lançamentos.

Somente cartões no estado `ATIVO` e sem bloqueio podem originar novos lançamentos.

### 10.5 Desativação por Substituição de Cartão Físico

O titular do contrato pode criar um novo cartão físico para substituir o cartão físico anteriormente ativo.

Quando um novo cartão físico é criado dentro do contrato para substituir o cartão físico atualmente ativo daquele papel/titular, o cartão físico anterior é desativado e o novo cartão é ativado.

Essa substituição preserva integralmente o histórico do cartão anterior.

Para o titular do contrato, existe apenas um cartão físico principal ativo por vez.

Quando um novo cartão físico adicional é criado para um usuário destinatário, o cartão físico adicional anteriormente ativo desse mesmo usuário é desativado e o novo passa a ser o ativo.

---

## 11. Permissões de Utilização

A posse do cartão e a autorização para utilização são conceitos distintos.

O titular do contrato possui controle sobre todos os cartões vinculados ao contrato e é o único usuário autorizado a:

- criar cartões;
- criar cartões virtuais;
- criar cartões adicionais;
- conceder compartilhamentos;
- remover compartilhamentos;
- reativar cartões.

O titular do contrato pode visualizar todas as faturas e realizar lançamentos em todos os cartões do contrato.

O titular de um cartão adicional possui autorização de utilização exclusivamente sobre o cartão adicional que recebeu. Ele não pode criar, compartilhar ou reativar cartões.

O usuário que recebeu acesso por compartilhamento possui autorização exclusivamente sobre o cartão compartilhado conforme o acesso concedido. Ele não pode criar, compartilhar ou reativar cartões.

A autorização para utilização de um cartão não concede automaticamente acesso aos demais cartões do mesmo contrato.

O cartão adicional possui acesso às faturas do contrato e aos lançamentos do próprio cartão adicional.

O usuário com cartão compartilhado possui acesso aos lançamentos e ao extrato/fatura relacionados ao cartão compartilhado.

A remoção do acesso a um cartão adicional ou compartilhado impede novos lançamentos do usuário removido, mas não apaga o histórico das operações já realizadas por ele.

As permissões específicas para consultar ou operar faturas serão detalhadas no documento de faturas.

---

## 12. Histórico

O histórico financeiro do cartão deve ser preservado mesmo quando o cartão for desativado.

A desativação, bloqueio ou remoção de acesso não autoriza a remoção física dos registros relacionados ao cartão.

Devem permanecer rastreáveis, quando aplicável:

- identidade do cartão;
- vínculo com o contrato;
- instituição mantenedora;
- nome do contrato/produto;
- `last4`;
- usuário titular do cartão;
- usuários autorizados;
- alterações de estado;
- concessões e revogações de acesso;
- lançamentos realizados;
- usuário responsável por cada lançamento;
- operações relacionadas ao cartão;
- referências a faturas;
- substituições de cartões físicos.

A remoção de acesso de um usuário não remove seu histórico de operações. Um lançamento realizado pelo usuário continua sendo histórico e permanece visível para esse usuário mesmo após a perda de acesso operacional ao cartão, inclusive após a quitação da obrigação correspondente.

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

O titular do cartão e o usuário responsável por um lançamento são informações distintas e devem ser identificados individualmente dentro da fatura.

As regras detalhadas de criação, ciclo mensal, fechamento, vencimento, pagamento, quitação, créditos e demais regras de liquidação da fatura serão definidas em:

`CREDIT-CARDS-INVOICE.md`

Este documento não deve duplicar as regras operacionais de faturas.

---

## 14. Invariantes do Domínio do Cartão

As seguintes regras devem permanecer verdadeiras:

1. Um cartão pertence a um contrato de crédito.
2. O contrato de crédito é uma representação abstrata da relação de crédito dentro do ambiente financeiro do usuário.
3. Todo contrato nasce obrigatoriamente com um cartão principal físico.
4. Um contrato possui uma única instituição mantenedora.
5. Todos os cartões de um contrato pertencem à mesma instituição mantenedora.
6. A mudança de instituição mantenedora exige um novo contrato.
7. O limite global pertence ao contrato, não ao cartão individual.
8. Cartões vinculados ao mesmo contrato compartilham o limite global.
9. Um contrato possui somente um cartão principal físico ativo por vez.
10. A quantidade de cartões virtuais de um contrato é ilimitada.
11. `Físico` e `Virtual` são características de identificação do cartão, não papéis de negócio independentes.
12. Cartão adicional é sempre físico.
13. Existe no máximo um cartão adicional ativo por usuário destinatário dentro de um contrato.
14. Cartão adicional não pode ser compartilhado com terceiros.
15. Qualquer cartão elegível pode ser compartilhado, e um cartão compartilhado pode ser utilizado por múltiplos usuários.
16. O mesmo usuário não pode receber o mesmo cartão compartilhado mais de uma vez simultaneamente.
17. Cartões adicionais e compartilhados não transferem a titularidade do contrato.
18. Um cartão não pode ser transferido de um contrato para outro.
19. O titular do contrato é o único usuário que pode criar cartões.
20. O titular do contrato é o único usuário que pode conceder ou remover compartilhamentos.
21. O titular do contrato é o único usuário que pode reativar cartões.
22. O usuário de um cartão adicional pode utilizar e desativar/reativar o próprio cartão, mas não pode criar ou compartilhar cartões.
23. O usuário de um cartão compartilhado pode utilizar o cartão conforme a autorização recebida, mas não pode criar, compartilhar ou reativar cartões.
24. Um lançamento com cartão deve registrar obrigatoriamente o usuário que realizou a operação.
25. Somente cartões `ATIVO` e não bloqueados podem originar novos lançamentos.
26. `BLOQUEADO` impede novos lançamentos, mas o cartão continua ativo.
27. Um cartão desativado não pode originar novos lançamentos.
28. A desativação é lógica e preserva o histórico do cartão.
29. A desativação de um cartão cancela suas recorrências de lançamentos.
30. O bloqueio não cancela suas recorrências.
31. O limite global deve ser maior ou igual a zero.
32. O limite disponível pode ser negativo.
33. Crédito comprometido é a soma das operações de crédito ainda não liberadas.
34. Operações canceladas, estornadas ou já liberadas não compõem o crédito comprometido.
35. A quantidade de usuários de compartilhamento por cartão não possui limite definido pelo domínio.
36. O histórico das operações permanece preservado após revogação de acesso.
37. O usuário removido de um compartilhamento continua podendo consultar o histórico das operações que realizou, inclusive após a quitação das obrigações correspondentes.
38. A fatura pertence ao contrato e reúne os lançamentos dos cartões daquele contrato, não existindo uma fatura independente por cartão.
39. As regras de estorno, cancelamento parcial, cancelamento total e efeitos desses eventos sobre o limite permanecem pendentes de definição.
40. Regras de ciclo, fechamento, pagamento e quitação de faturas pertencem ao domínio de faturas e não devem ser duplicadas neste documento.
