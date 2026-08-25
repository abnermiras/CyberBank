# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o contrato de cartão de crédito, seus cartões, titularidade, autorização de uso, compartilhamento, ciclo de vida, movimentos financeiros associados e limite de crédito.

As regras detalhadas de **faturas, ciclos de faturamento, fechamento, vencimento, pagamentos, quitação e demais operações próprias da fatura** serão definidas em documento próprio:

`CREDIT-CARDS-INVOICE.md`

Este documento mantém somente as regras básicas necessárias para estabelecer a relação entre cartão, contrato, movimento financeiro e fatura.

---

## 2. Conceito

Um cartão de crédito é um instrumento de pagamento vinculado a um **contrato de cartão de crédito**.

O contrato representa a relação de crédito estabelecida pelo usuário dentro de um ambiente financeiro.

O cartão de crédito não possui limite independente. Todos os cartões vinculados ao mesmo contrato utilizam o **limite global do contrato**.

O contrato é criado pelo usuário responsável e, no momento de sua criação, recebe automaticamente um primeiro cartão físico.

Um contrato pode possuir cartões físicos, cartões adicionais e cartões virtuais.

---

## 3. Contrato de Cartão de Crédito

O contrato de cartão de crédito pertence ao ambiente financeiro no qual foi criado e possui um único usuário responsável.

O contrato possui conceitualmente:

- usuário responsável;
- ambiente financeiro de origem;
- banco cadastrado ou instituição financeira declarada;
- nome do contrato;
- data de vencimento da fatura;
- quantidade de dias anteriores ao vencimento utilizada para definir o fechamento da fatura;
- limite global de crédito;
- cartões vinculados.

O contrato não precisa corresponder a um contrato físico ou a um número de contrato existente na instituição financeira.

### 3.1 Instituição Financeira

A instituição financeira do contrato pode ser:

- um banco previamente cadastrado no ambiente financeiro do usuário; ou
- uma instituição financeira declarada pelo próprio usuário quando não houver cadastro bancário correspondente no ambiente financeiro.

Um contrato possui uma única instituição financeira.

Todos os cartões vinculados ao contrato pertencem à mesma instituição financeira.

Um cartão não pode ser transferido de um contrato para outro.

Se o usuário quiser representar outra relação de crédito ou outra instituição financeira, deve criar um novo contrato.

### 3.2 Nome do Contrato

O usuário responsável define um nome para o contrato para facilitar sua identificação no ambiente financeiro.

Exemplo:

```text
Banco: Nubank
Contrato: Ultravioleta
```

ou:

```text
Instituição: Lojas Crediário
Contrato: Cartão Loja
```

O nome do contrato não é o identificador técnico da entidade.

### 3.3 Responsável pelo Contrato

O usuário que cria o contrato é seu **responsável**.

O responsável pelo contrato:

- permanece responsável pelo crédito;
- permanece responsável pelo limite global;
- possui autoridade administrativa sobre todos os cartões do contrato;
- é o único usuário autorizado a criar novos cartões;
- pode administrar o ciclo de vida dos cartões conforme as regras deste documento.

A responsabilidade pelo contrato não é transferida pela criação de cartões adicionais ou pelo compartilhamento de cartões.

---

## 4. Cadastro e Identidade do Cartão

Cada cartão possui um **identificador técnico interno imutável**, utilizado pelo sistema para identificar univocamente a entidade durante toda sua existência.

O identificador técnico não depende:

- do estado do cartão;
- do usuário que o utiliza;
- do compartilhamento;
- do apelido;
- dos últimos quatro dígitos.

O CyberBank não armazena o número completo do cartão.

Para apresentação ao usuário, o cartão possui uma identificação de negócio composta, conforme aplicável, por:

- banco ou instituição financeira;
- nome do contrato;
- apelido do cartão;
- tipo de emissão;
- últimos quatro dígitos (`last4`);
- usuário ao qual o cartão foi concedido para utilização, quando aplicável.

Os últimos quatro dígitos não são identificador único do cartão.

O cartão não possui data de validade nem data de emissão como parte do modelo de negócio do CyberBank.

### 4.1 Exemplo de Identificação

```text
Banco/Instituição: Nubank
Contrato: Ultravioleta
Apelido: Pessoal
Tipo de emissão: Físico
Últimos 4 dígitos: 1234
Responsável pelo contrato: Usuário A
```

---

## 5. Tipos de Cartão

Um contrato pode possuir três tipos de cartão:

1. `Físico`;
2. `Adicional`;
3. `Virtual`.

O compartilhamento não é um tipo de cartão. É uma autorização de utilização que pode existir sobre cartões físicos e virtuais elegíveis.

Todos os cartões permanecem vinculados ao contrato original e utilizam seu limite global.

---

## 6. Cartão Físico

Ao criar um contrato, o sistema cria automaticamente um primeiro cartão físico.

O usuário responsável pelo contrato pode criar quantos cartões físicos quiser.

Cada cartão físico possui:

- identificação técnica própria;
- apelido definido pelo usuário;
- últimos quatro dígitos informados pelo usuário;
- tipo de emissão `Físico`;
- vínculo com o contrato;
- vínculo com o responsável pelo contrato.

Exemplo:

```text
Banco/Instituição: Nubank
Contrato: Ultravioleta
Apelido: Pessoal
Tipo de emissão: Físico
Últimos 4 dígitos: 1234
Responsável pelo contrato: Usuário A
```

Outro exemplo:

```text
Banco/Instituição: Lojas Crediário
Contrato: Cartão Loja
Apelido: Meu cartão
Tipo de emissão: Físico
Últimos 4 dígitos: 4321
Responsável pelo contrato: Usuário A
```

A existência de vários cartões físicos não cria novos contratos nem novos limites.

---

## 7. Cartão Adicional

O cartão adicional é um cartão físico criado pelo responsável pelo contrato e concedido para utilização de outro usuário.

O cartão adicional:

- é sempre físico;
- pertence ao contrato original;
- permanece sob responsabilidade do responsável pelo contrato;
- possui identificação técnica própria;
- possui apelido próprio;
- possui seus próprios últimos quatro dígitos;
- não pode ser compartilhado;
- não pode gerar cartão virtual;
- pode ser concedido a qualquer usuário do sistema escolhido pelo responsável pelo contrato.

O responsável pelo contrato pode criar quantos cartões adicionais quiser.

Não existe limite global de quantidade de cartões adicionais no contrato.

### 7.1 Titular de Utilização do Cartão Adicional

O cartão adicional pode ser concedido para utilização por outro usuário.

O usuário que recebe o cartão adicional passa a ser o **titular de utilização** daquele cartão, mas não se torna responsável pelo contrato nem proprietário do crédito.

A responsabilidade pelo contrato e pelo limite global permanece com o responsável pelo contrato.

A titularidade de utilização não altera a propriedade do cartão dentro do contrato.

### 7.2 Criação e Aceitação

Ao criar um cartão adicional, o responsável pelo contrato informa:

- banco ou instituição financeira;
- nome do contrato;
- apelido do cartão;
- tipo de emissão;
- últimos quatro dígitos;
- usuário ao qual a utilização será concedida.

O cartão adicional nasce com estado:

`Pendente de Aceitação`

O usuário destinatário recebe uma solicitação e pode:

- aceitar;
- recusar.

Para aceitar, o usuário deve escolher em qual de seus ambientes financeiros o cartão será disponibilizado como meio de pagamento.

Após a conclusão da aceitação, o cartão adicional passa para `Ativo`.

Se o usuário recusar a solicitação, o cartão adicional passa automaticamente para `Desativado`.

### 7.3 Administração do Cartão Adicional

O responsável pelo contrato pode:

- ativar o cartão;
- desativar o cartão;
- bloquear o cartão;
- desbloquear o cartão;
- indicar o usuário ao qual o cartão será concedido, conforme as regras de aceitação.

O titular de utilização do cartão adicional pode:

- utilizar o cartão;
- bloquear o próprio cartão.

O titular de utilização do cartão adicional não pode:

- criar outro cartão adicional;
- compartilhar o cartão;
- criar cartão virtual a partir do adicional;
- desbloquear o cartão;
- ativar ou reativar o cartão;
- alterar o responsável pelo contrato.

### 7.4 Revogação e Substituição

A administração do vínculo de utilização do cartão adicional permanece sob responsabilidade do responsável pelo contrato.

Um cartão adicional desativado permanece preservado para fins históricos e não pode ser convertido em cartão de outro tipo.

A criação de um novo cartão adicional é uma nova entidade de cartão e não transfere automaticamente o histórico do cartão anterior.

---

## 8. Cartão Virtual

O cartão virtual é um cartão criado dentro do contrato para utilização em meios digitais ou outras finalidades definidas pelo responsável pelo contrato.

**Somente o responsável pelo contrato pode criar cartões virtuais.**

O responsável pelo contrato pode criar quantos cartões virtuais quiser.

Não existe limite de quantidade de cartões virtuais por contrato.

Ao criar um cartão virtual, o responsável pelo contrato informa:

- apelido do cartão;
- últimos quatro dígitos.

Exemplo:

```text
Banco/Instituição: Nubank
Contrato: Ultravioleta
Apelido: Assinaturas
Tipo de emissão: Virtual
Últimos 4 dígitos: 5678
Responsável pelo contrato: Usuário A
```

Outro exemplo:

```text
Banco/Instituição: Lojas Crediário
Contrato: Cartão Loja
Apelido: Compras
Tipo de emissão: Virtual
Últimos 4 dígitos: 8765
Responsável pelo contrato: Usuário A
```

O cartão virtual:

- não cria novo contrato;
- não possui limite próprio;
- utiliza o limite global do contrato;
- possui identidade técnica própria;
- pode ser compartilhado;
- não pode ser criado diretamente em nome de outro usuário.

O usuário que receber utilização de um cartão virtual por compartilhamento não se torna responsável pelo contrato nem proprietário do cartão.

---

## 9. Compartilhamento de Cartões

Cartões físicos e virtuais podem ser compartilhados com outros usuários do sistema.

Cartões adicionais não podem ser compartilhados.

O compartilhamento não cria novo cartão e não cria novo contrato.

O cartão continua pertencendo ao contrato e permanece sob responsabilidade do responsável pelo contrato.

### 9.1 Administração do Compartilhamento

Somente o responsável pelo contrato pode:

- compartilhar um cartão elegível;
- descompartilhar um cartão;
- conceder autorização de uso a outro usuário.

O usuário que recebe o compartilhamento não pode bloquear nem desbloquear o cartão.

O compartilhamento concede autorização de utilização, mas não transfere:

- titularidade do contrato;
- responsabilidade pelo limite;
- propriedade do cartão;
- autoridade administrativa sobre o cartão.

### 9.2 Utilização por Usuário Compartilhado

O usuário que recebe um cartão compartilhado pode utilizar o cartão em movimentos financeiros, desde que o cartão esteja disponível para utilização.

O usuário compartilhado pode visualizar, no extrato da fatura do cartão compartilhado, os lançamentos inerentes aos gastos realizados por ele.

As regras detalhadas de visualização da fatura e do extrato serão definidas em `CREDIT-CARDS-INVOICE.md`.

---

## 10. Ciclo de Vida do Cartão

Os estados operacionais do cartão são:

- `Pendente de Aceitação` — aplicável ao cartão adicional aguardando decisão do usuário destinatário;
- `Ativo`;
- `Bloqueado`;
- `Desativado`.

### 10.1 Pendente de Aceitação

O estado `Pendente de Aceitação` existe para cartões adicionais criados e ainda não aceitos pelo usuário destinatário.

Enquanto estiver pendente:

- o usuário destinatário ainda não pode utilizar o cartão;
- o cartão não deve ser considerado meio de pagamento ativo para o destinatário;
- o usuário destinatário pode aceitar ou recusar a solicitação.

Aceitação concluída:

`Pendente de Aceitação → Ativo`

Recusa:

`Pendente de Aceitação → Desativado`

### 10.2 Ativo

Um cartão ativo pode receber novos movimentos financeiros quando o usuário que realiza a operação possuir autorização de utilização.

### 10.3 Bloqueado

Um cartão bloqueado continua existindo no contrato, porém não pode ser utilizado para novos movimentos financeiros.

O bloqueio não:

- exclui o cartão;
- remove o cartão do contrato;
- altera sua identidade;
- transfere sua responsabilidade;
- elimina histórico financeiro.

O cartão bloqueado pode ser desbloqueado por quem possuir autoridade para essa operação.

### 10.4 Desativado

Um cartão desativado não pode receber novos movimentos financeiros.

A desativação não:

- exclui o cartão;
- remove seu histórico;
- altera movimentos financeiros já registrados;
- transfere o cartão para outro usuário;
- encerra obrigações financeiras já existentes.

O cartão desativado permanece preservado para fins históricos.

### 10.5 Transições

As transições gerais são:

```text
PENDENTE DE ACEITAÇÃO
        │
        ├── aceitar ───────► ATIVO
        │
        └── recusar ───────► DESATIVADO

ATIVO ─────────► BLOQUEADO
  │                 │
  │                 └──────► ATIVO
  │
  └───────────────► DESATIVADO

DESATIVADO ──────► ATIVO
```

As permissões para cada transição são definidas na seção de ações.

---

## 11. Ações e Autorizações

### 11.1 Criar Cartão

Somente o responsável pelo contrato pode criar novos cartões.

Isso inclui:

- cartão físico;
- cartão adicional;
- cartão virtual.

### 11.2 Ativar e Desativar

Somente o responsável pelo contrato pode ativar ou desativar cartões.

No caso de cartão adicional pendente, a aceitação do usuário destinatário é o evento que permite a transição para `Ativo`.

A recusa do destinatário provoca a transição automática para `Desativado`.

### 11.3 Compartilhar e Descompartilhar

Somente o responsável pelo contrato pode compartilhar ou descompartilhar cartões elegíveis.

São elegíveis:

- cartões físicos;
- cartões virtuais.

Cartões adicionais não são elegíveis ao compartilhamento.

### 11.4 Bloquear e Desbloquear

O responsável pelo contrato pode bloquear ou desbloquear qualquer cartão do contrato.

O titular de utilização de um cartão adicional pode bloquear somente o próprio cartão adicional.

O titular de utilização de um cartão adicional não pode desbloquear o cartão.

Um usuário que recebeu um cartão por compartilhamento não pode bloquear nem desbloquear o cartão.

### 11.5 Matriz de Autorizações

| Ação | Responsável pelo contrato | Titular de utilização de adicional | Usuário compartilhado |
|---|---:|---:|---:|
| Criar cartão físico | Sim | Não | Não |
| Criar cartão adicional | Sim | Não | Não |
| Criar cartão virtual | Sim | Não | Não |
| Ativar | Sim | Não | Não |
| Desativar | Sim | Não | Não |
| Bloquear | Qualquer cartão | Próprio adicional | Não |
| Desbloquear | Qualquer cartão | Não | Não |
| Compartilhar | Sim, se elegível | Não | Não |
| Descompartilhar | Sim | Não | Não |
| Utilizar cartão | Conforme autorização | Próprio adicional | Cartão compartilhado |

---

## 12. Movimentos Financeiros com Cartão

Um cartão de crédito participa de um movimento financeiro quando o usuário cria um lançamento de saída e indica, no campo **meio de pagamento**, um cartão ao qual possui autorização de utilização.

O cartão utilizado é identificado pelo seu identificador técnico interno.

O movimento financeiro continua sendo uma única movimentação financeira do sistema. O uso do cartão não cria uma segunda movimentação apenas para representar a fatura.

### 12.1 Registro no Extrato da Fatura

O movimento financeiro realizado com cartão será registrado como **previsto** no extrato da fatura correspondente ao contrato.

O movimento passa a representar uma obrigação financeira relacionada ao cartão utilizado.

O detalhamento de como esse movimento participa da composição, fechamento e liquidação da fatura será definido em `CREDIT-CARDS-INVOICE.md`.

### 12.2 Efetivação

A efetivação financeira do movimento relacionado ao cartão ocorre no momento em que a fatura correspondente é efetivamente paga, conforme as regras de liquidação definidas no domínio de faturas.

O movimento permanece previsto enquanto a obrigação correspondente não tiver sido liquidada conforme as regras da fatura.

### 12.3 Movimentos de Crédito

É possível registrar um movimento financeiro de crédito em uma fatura para representar um valor que entrou no extrato.

Esse movimento de crédito participa da composição financeira da fatura conforme as regras definidas no domínio de faturas.

### 12.4 Pagamento Parcial

O pagamento parcial de uma fatura é permitido.

O pagamento parcial não encerra a fatura.

Enquanto a fatura permanecer parcial:

- a fatura continua aberta conforme as regras do domínio de faturas;
- os lançamentos ainda não liquidados permanecem previstos;
- a liquidação e efetivação dos movimentos seguem as regras de `CREDIT-CARDS-INVOICE.md`.

---

## 13. Fatura e Extrato de Fatura

A fatura é responsabilidade do contrato de cartão de crédito e será detalhada em:

`CREDIT-CARDS-INVOICE.md`

Neste documento ficam somente as regras básicas de relacionamento.

A fatura é a composição dos extratos dos cartões vinculados ao contrato que participam daquele ciclo de faturamento.

Os cartões podem possuir movimentos realizados por diferentes usuários autorizados, porém a fatura pertence ao contrato.

### 13.1 Pagamento pelo Responsável pelo Contrato

O responsável pelo contrato pode pagar a fatura inteira ou parcialmente.

### 13.2 Pagamento por Usuários de Cartões

Usuários que receberam um cartão adicional ou um cartão compartilhado podem pagar total ou parcialmente o extrato de fatura correspondente ao cartão ao qual possuem autorização de utilização.

Os detalhes de como esse pagamento afeta a fatura, sua composição, seu saldo e sua liquidação pertencem a `CREDIT-CARDS-INVOICE.md`.

### 13.3 Visibilidade do Extrato

Um usuário que utiliza um cartão compartilhado pode visualizar, no extrato da fatura do cartão compartilhado, os lançamentos inerentes aos gastos realizados por ele.

As regras completas de visibilidade, composição do extrato, fechamento, vencimento e pagamento serão definidas no documento de faturas.

---

## 14. Limite de Crédito

O limite de crédito pertence ao contrato, e não a um cartão individual.

No momento da criação do contrato, o responsável pelo contrato define o **limite global de crédito**.

Todos os cartões do contrato utilizam esse mesmo limite global.

### 14.1 Consumo do Limite

Cada movimentação financeira realizada com cartão consome parte do limite de crédito **no momento em que o movimento é lançado**.

O consumo de limite ocorre independentemente do cartão ser:

- físico;
- adicional;
- virtual;
- utilizado diretamente pelo responsável pelo contrato;
- utilizado por usuário autorizado por compartilhamento.

### 14.2 Limite Disponível

O valor disponível do contrato é calculado por:

```text
Limite disponível =
    Limite global
    - lançamentos financeiros
    + lançamentos de crédito
```

O limite é compartilhado por todos os cartões do contrato.

Nenhum cartão possui limite independente.

### 14.3 Limite Global

O limite global é uma propriedade do contrato.

Somente o responsável pelo contrato pode alterar o limite global.

O limite global deve ser maior ou igual a zero.

A forma como pagamentos, liquidações e demais eventos da fatura impactam o limite disponível será detalhada em `CREDIT-CARDS-INVOICE.md`.

---

## 15. Histórico e Integridade

Cartões não são excluídos fisicamente do domínio quando deixam de ser utilizáveis.

Devem permanecer rastreáveis, quando aplicável:

- identificador técnico do cartão;
- contrato ao qual pertence;
- responsável pelo contrato;
- tipo do cartão;
- apelido;
- últimos quatro dígitos;
- estado atual;
- histórico de alterações de estado;
- usuário destinatário de cartão adicional;
- autorizações de compartilhamento;
- movimentos financeiros realizados;
- usuário responsável por cada movimento;
- referências às faturas.

A desativação, bloqueio, recusa de cartão adicional ou remoção de compartilhamento não deve apagar movimentos financeiros históricos.

O histórico financeiro não deve ser transferido retroativamente para outro cartão.

---

## 16. Invariantes do Domínio

As seguintes regras devem permanecer verdadeiras:

1. Todo cartão pertence a um contrato de cartão de crédito.
2. Todo contrato pertence a um ambiente financeiro.
3. Todo contrato possui exatamente um usuário responsável.
4. Um contrato possui uma única instituição financeira.
5. A instituição pode ser um banco cadastrado ou uma instituição financeira declarada pelo usuário.
6. O contrato possui um limite global de crédito.
7. O limite global pertence ao contrato e não ao cartão.
8. Todos os cartões do contrato compartilham o limite global.
9. A criação do contrato cria automaticamente um primeiro cartão físico.
10. O responsável pelo contrato pode criar quantos cartões físicos quiser.
11. O responsável pelo contrato pode criar quantos cartões adicionais quiser.
12. O responsável pelo contrato pode criar quantos cartões virtuais quiser.
13. Somente o responsável pelo contrato pode criar cartões.
14. Cartão adicional é sempre físico.
15. Cartão adicional não pode ser compartilhado.
16. Cartão adicional não pode gerar cartão virtual.
17. Cartão virtual pode ser compartilhado.
18. Cartão físico pode ser compartilhado.
19. Compartilhamento não transfere propriedade nem responsabilidade pelo contrato.
20. Todos os cartões permanecem sob responsabilidade do responsável pelo contrato.
21. O usuário que recebe um cartão adicional possui titularidade de utilização, não responsabilidade pelo contrato.
22. O cartão adicional nasce como `Pendente de Aceitação`.
23. Um cartão adicional somente passa para `Ativo` após aceitação do usuário destinatário.
24. A recusa de um cartão adicional leva o cartão para `Desativado`.
25. O usuário destinatário de cartão adicional escolhe em qual ambiente financeiro disponibilizará o cartão após aceitar.
26. Somente o responsável pelo contrato pode ativar ou desativar cartões.
27. O responsável pelo contrato pode bloquear ou desbloquear qualquer cartão.
28. O titular de utilização de um cartão adicional pode bloquear somente o próprio adicional.
29. O titular de utilização de um cartão adicional não pode desbloquear o próprio cartão.
30. Usuário compartilhado não pode bloquear nem desbloquear o cartão compartilhado.
31. Somente o responsável pelo contrato pode compartilhar ou descompartilhar cartões elegíveis.
32. Um movimento financeiro de saída pode indicar um cartão ao qual o usuário possui autorização de utilização.
33. O movimento financeiro com cartão é registrado como previsto no extrato da fatura.
34. A efetivação do movimento financeiro relacionado ao cartão ocorre conforme a liquidação da fatura.
35. É permitido registrar movimento financeiro de crédito relacionado ao extrato da fatura.
36. Pagamento parcial de fatura é permitido, mas não encerra a fatura.
37. A fatura pertence ao contrato e compõe os extratos dos cartões participantes do ciclo.
38. O responsável pelo contrato pode pagar a fatura inteira ou parcialmente.
39. Usuário de cartão adicional ou compartilhado pode pagar total ou parcialmente o extrato correspondente ao cartão ao qual possui autorização.
40. O limite é consumido no momento do lançamento do movimento financeiro.
41. O limite disponível é calculado pelo limite global menos lançamentos financeiros mais lançamentos de crédito.
42. Cartão não possui limite de crédito independente.
43. O número completo do cartão não é armazenado pelo CyberBank.
44. `last4` não é identificador técnico único.
45. Cada cartão possui identificador técnico interno imutável.
46. Cartões desativados permanecem preservados para fins históricos.
47. Bloqueio não exclui nem altera retroativamente movimentos financeiros.
48. Desativação não exclui nem altera retroativamente movimentos financeiros.
49. Compartilhamento não cria novo cartão.
50. Compartilhamento não cria novo contrato.
51. Cartão adicional não pode ser convertido ou reutilizado como cartão de outro tipo.
52. O cartão não possui data de validade ou data de emissão no modelo de negócio.

---

## 17. Limite deste Documento

Este documento define as regras do contrato, cartões, autorização de utilização, compartilhamento, movimentos financeiros relacionados ao cartão e regra básica de limite.

As regras detalhadas de fatura devem permanecer exclusivamente em:

`CREDIT-CARDS-INVOICE.md`

Isso inclui, entre outras:

- criação e ciclos de fatura;
- data de fechamento;
- data de vencimento e regras de calendário;
- composição detalhada da fatura;
- detalhamento dos extratos;
- pagamentos;
- pagamentos parciais;
- quitação;
- créditos de fatura;
- liquidação dos movimentos;
- efetivação decorrente da liquidação;
- impacto detalhado dos pagamentos no limite disponível;
- demais regras específicas do ciclo financeiro da fatura.
