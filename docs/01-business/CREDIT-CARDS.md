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

Um contrato pode possuir quantos cartões físicos, adicionais e virtuais forem necessários, respeitando as regras específicas de cada tipo.

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

O usuário que cria o contrato é seu **responsável e dono**.

O responsável/dono do contrato:

- permanece responsável pelo crédito;
- permanece responsável pelo limite global;
- possui autoridade plena sobre o contrato e todos os cartões vinculados a ele;
- é o único usuário autorizado a criar novos cartões, exceto o primeiro cartão físico, que é criado automaticamente pelo sistema junto com o contrato;
- pode administrar livremente os cartões, seus usuários de utilização, compartilhamentos e estados, respeitadas as regras estruturais do domínio;
- pode realizar qualquer ação de administração permitida pelo sistema sobre o contrato e seus cartões.

As ações explicitamente descritas neste documento não constituem uma lista exaustiva das prerrogativas do responsável/dono. A autoridade decorre de sua condição de dono do contrato.

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
Titular de utilização: Usuário A
```

---

## 5. Tipos de Cartão

Um contrato pode possuir três tipos de cartão:

1. `Físico`;
2. `Adicional`;
3. `Virtual`.

O cartão `Adicional` é um tipo próprio do domínio, embora seja fisicamente um cartão físico. Ele possui regras específicas de titularidade de utilização e aceitação.

O compartilhamento não é um tipo de cartão. É uma autorização de utilização que pode existir sobre cartões físicos e virtuais, exceto cartões adicionais.

Todos os cartões permanecem vinculados ao contrato original e utilizam seu limite global.

---

## 6. Cartão Físico

Ao criar um contrato, o sistema cria automaticamente um primeiro cartão físico para o responsável/dono do contrato.

O responsável/dono do contrato pode criar quantos cartões físicos quiser.

Cada cartão físico possui:

- identificação técnica própria;
- apelido definido pelo responsável;
- últimos quatro dígitos informados pelo responsável;
- tipo de emissão `Físico`;
- vínculo com o contrato;
- titular de utilização correspondente ao responsável pelo contrato.

Exemplo:

```text
Banco/Instituição: Nubank
Contrato: Ultravioleta
Apelido: Pessoal
Tipo de emissão: Físico
Últimos 4 dígitos: 1234
Titular de utilização: Usuário A
```

Outro exemplo:

```text
Banco/Instituição: Lojas Crediário
Contrato: Cartão Loja
Apelido: Meu cartão
Tipo de emissão: Físico
Últimos 4 dígitos: 4321
Titular de utilização: Usuário A
```

A existência de vários cartões físicos não cria novos contratos nem novos limites.

---

## 7. Cartão Adicional

O cartão adicional é um cartão físico criado pelo responsável/dono do contrato e concedido para utilização de outro usuário.

O cartão adicional:

- é sempre físico;
- pertence ao contrato original;
- permanece sob responsabilidade do responsável pelo contrato;
- possui identificação técnica própria;
- possui apelido próprio;
- possui seus próprios últimos quatro dígitos;
- não pode ser compartilhado;
- não pode gerar cartão virtual;
- pode ser concedido a qualquer usuário do sistema escolhido pelo responsável pelo contrato;
- possui um único titular de utilização, definido para aquela entidade de cartão.

O responsável pelo contrato pode criar quantos cartões adicionais quiser.

Não existe limite global de quantidade de cartões adicionais no contrato.

### 7.1 Titular de Utilização do Cartão Adicional

O usuário que recebe o cartão adicional passa a ser o **titular de utilização** daquele cartão, mas não se torna responsável pelo contrato nem proprietário do crédito.

A responsabilidade pelo contrato e pelo limite global permanece com o responsável pelo contrato.

A titularidade de utilização não altera a propriedade do cartão dentro do contrato.

O titular de utilização de um cartão adicional **não pode ser trocado**. A indicação de um usuário para um cartão adicional representa uma nova concessão que depende da aceitação do usuário.

Quando outro usuário deve receber um cartão adicional, o responsável pode realizar uma nova concessão. O novo usuário deverá passar pelo processo de aceitação correspondente.

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

A indicação do usuário pelo responsável representa somente o envio de uma solicitação. **Indicar um usuário não significa pré-aprovar a titularidade.**

Para aceitar, o usuário deve concluir a aceitação e escolher em qual de seus ambientes financeiros o cartão será disponibilizado como meio de pagamento.

Após a conclusão da aceitação, o cartão adicional passa para `Ativo`.

Se o usuário recusar a solicitação, o cartão adicional passa automaticamente para `Desativado`.

O responsável/dono pode administrar a concessão enquanto ela estiver pendente, inclusive cancelá-la ou realizar nova indicação, pois é o dono do contrato e do cartão. Uma alteração da concessão não equivale à aceitação do novo usuário.

Se o responsável indicar outro usuário para um cartão adicional desativado, o cartão volta para `Pendente de Aceitação` e o novo usuário deverá aceitar explicitamente a solicitação para que o cartão volte a `Ativo`.

### 7.3 Administração do Cartão Adicional

O responsável/dono do contrato pode:

- ativar;
- desativar;
- bloquear;
- desbloquear;
- criar novos cartões adicionais;
- administrar as concessões e solicitações de aceitação;
- cancelar uma solicitação de aceitação pendente;
- realizar qualquer outra operação de administração permitida pelo sistema.

O titular de utilização de um cartão adicional pode:

- utilizar o cartão;
- bloquear o próprio cartão;
- desbloquear o próprio cartão.

O titular de utilização não possui autoridade administrativa sobre o contrato ou sobre outros cartões.

A titularidade de utilização do cartão adicional é imutável após a aceitação. Para conceder um cartão adicional a outro usuário, o responsável deve realizar uma nova concessão, utilizando outra entidade de cartão adicional ou uma nova concessão sobre um cartão adicional desativado, conforme as regras deste documento.

### 7.4 Revogação e Substituição

Um cartão adicional desativado permanece preservado para fins históricos e não pode ser convertido em outro tipo de cartão.

A criação de um novo cartão adicional é uma nova entidade de cartão e não transfere automaticamente o histórico do cartão anterior.

A troca de titular por cartão desativado só acontece se ninguem nunca aceitou esse cartão adicional 

Se o responsável indicar outro usuário para um cartão adicional, que nunca recebeu nenhum resposável, que esteja `Desativado`, o estado deve ser alterado para `Pendente de Aceitação`. O novo usuário precisará aceitar explicitamente a solicitação.

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
Titular de utilização: Usuário A
```

Outro exemplo:

```text
Banco/Instituição: Lojas Crediário
Contrato: Cartão Loja
Apelido: Compras
Tipo de emissão: Virtual
Últimos 4 dígitos: 8765
Titular de utilização: Usuário A
```

O cartão virtual:

- não cria novo contrato;
- não possui limite próprio;
- utiliza o limite global do contrato;
- possui identidade técnica própria;
- pode ser compartilhado com um ou mais usuários;
- não pode ser criado diretamente em nome de outro usuário.

O usuário que receber utilização de um cartão virtual por compartilhamento não se torna responsável pelo contrato nem proprietário do cartão.

O responsável/dono do contrato possui autoridade plena sobre o cartão virtual, incluindo suas operações de administração e ciclo de vida.

---

## 9. Compartilhamento de Cartões

Cartões físicos e virtuais podem ser compartilhados com um ou mais outros usuários do sistema.

Cartões adicionais não podem ser compartilhados.

O compartilhamento não cria novo cartão e não cria novo contrato.

O cartão continua pertencendo ao contrato e permanece sob responsabilidade do responsável pelo contrato.

Um mesmo cartão pode possuir simultaneamente autorizações de compartilhamento para vários usuários. Cada usuário compartilhado possui sua própria autorização de utilização sobre o mesmo cartão.

### 9.1 Administração do Compartilhamento

Somente o responsável/dono do contrato pode administrar o compartilhamento dos cartões, incluindo:

- compartilhar um cartão elegível;
- descompartilhar um cartão;
- conceder autorização de uso a outro usuário;
- remover autorização de uso;
- administrar qualquer outro aspecto do compartilhamento permitido pelo sistema.

O usuário que recebe o compartilhamento não pode bloquear nem desbloquear o cartão.

O compartilhamento concede autorização de utilização, mas não transfere:

- titularidade do contrato;
- responsabilidade pelo limite;
- propriedade do cartão;
- autoridade administrativa sobre o cartão.

Quando um cartão compartilhado é desativado, **todas as autorizações de compartilhamento existentes sobre o cartão são perdidas**. A reativação do cartão não restaura automaticamente nenhum compartilhamento.

Para compartilhar novamente um cartão que foi reativado, o responsável pelo contrato deve realizar novos compartilhamentos para os usuários desejados.

### 9.2 Utilização por Usuário Compartilhado

O usuário que recebe um cartão compartilhado pode utilizar o cartão em movimentos financeiros enquanto possuir a autorização de compartilhamento e o cartão estiver disponível para utilização.

Cada usuário compartilhado pode visualizar, no extrato da fatura do cartão compartilhado, os lançamentos inerentes aos gastos realizados por ele.

As regras detalhadas de visualização da fatura e do extrato serão definidas em `CREDIT-CARDS-INVOICE.md`.

O usuário compartilhado não possui autoridade administrativa sobre o cartão. A administração permanece com o responsável/dono do contrato.

---

## 10. Titularidade, Responsabilidade e Autoridade

Todos os cartões criados dentro de um contrato pertencem ao **responsável/dono do contrato**.

A existência de um titular de utilização em um cartão adicional ou de um usuário autorizado por compartilhamento não transfere a propriedade do cartão, a responsabilidade pelo crédito ou o limite global.

O cartão adicional possui um **titular de utilização** específico, que é definido para aquela entidade e, após a aceitação, não pode ser trocado.

O usuário compartilhado possui somente uma autorização de utilização concedida pelo responsável/dono do contrato.

### 10.1 Princípio de Autoridade do Dono

O responsável/dono do contrato é a autoridade máxima sobre o contrato e seus cartões.

Sua autoridade não é limitada à lista de ações explicitamente enumeradas neste documento. As ações descritas servem para documentar os principais comportamentos operacionais, mas não constituem uma lista exaustiva de poderes.

O responsável/dono pode realizar qualquer operação de administração permitida pelo sistema sobre:

- o contrato;
- os cartões vinculados;
- as concessões de utilização dos cartões adicionais;
- os compartilhamentos;
- os estados dos cartões;
- as configurações do contrato.

Nenhum usuário que não seja o responsável/dono adquire autoridade administrativa sobre o contrato por receber um cartão adicional ou por receber um compartilhamento.

---

## 11. Ciclo de Vida do Cartão

Os estados operacionais do cartão são:

- `Pendente de Aceitação` — aplicável ao cartão adicional aguardando decisão do usuário destinatário;
- `Ativo`;
- `Bloqueado`;
- `Desativado`.

O estado `Bloqueado` possui finalidade exclusivamente operacional: impedir que o cartão seja utilizado como meio de pagamento. O bloqueio não representa encerramento, revogação ou perda de propriedade do cartão.

### 11.1 Pendente de Aceitação

O estado `Pendente de Aceitação` existe para cartões adicionais criados ou novamente concedidos a um usuário e ainda não aceitos pelo destinatário.

Enquanto estiver pendente:

- o usuário destinatário ainda não pode utilizar o cartão;
- o cartão não deve ser considerado meio de pagamento ativo para o destinatário;
- o usuário destinatário pode aceitar ou recusar a solicitação;
- o responsável/dono continua possuindo autoridade sobre o cartão e sobre a concessão de sua utilização;
- o responsável pode cancelar a solicitação ou realizar nova concessão (somente se ninguém nunca aceitou antes);
- indicar o usuário não equivale à aceitação ou pré-aprovação da titularidade.

Aceitação concluída:

`Pendente de Aceitação → Ativo`

Recusa:

`Pendente de Aceitação → Desativado`

Se o responsável indicar outro usuário para um cartão adicional que esteja `Desativado`, o cartão volta para `Pendente de Aceitação` e aguarda a aceitação explícita do novo usuário. esta ação só pode acontecer se ninguem nunca aceitou esse cartão antes.

### 11.2 Ativo

Um cartão ativo pode receber novos movimentos financeiros quando o usuário que realiza a operação possuir autorização de utilização.

### 11.3 Bloqueado

Um cartão bloqueado continua existindo no contrato, porém não pode ser utilizado para novos movimentos financeiros.

O bloqueio não:

- exclui o cartão;
- remove o cartão do contrato;
- altera sua identidade;
- transfere sua responsabilidade;
- elimina histórico financeiro;
- encerra o cartão.

O responsável/dono do contrato pode bloquear e desbloquear qualquer cartão.

O titular de utilização de um cartão adicional pode bloquear e desbloquear somente o próprio cartão adicional.

Usuários que utilizam um cartão por compartilhamento não podem bloquear nem desbloquear o cartão.

O responsável/dono pode desativar diretamente um cartão que esteja `Bloqueado`, sem necessidade de desbloqueá-lo previamente.

### 11.4 Desativado

Um cartão desativado não pode receber novos movimentos financeiros.

A desativação não:

- exclui o cartão;
- remove seu histórico;
- altera movimentos financeiros já registrados;
- transfere o cartão para outro usuário;
- encerra obrigações financeiras já existentes.

O cartão desativado permanece preservado para fins históricos.

Qualquer autorização de compartilhamento existente sobre o cartão é perdida no momento da desativação. A reativação não restaura automaticamente essa autorização.

O responsável/dono do contrato pode ativar novamente um cartão desativado.

No caso de cartão adicional desativado, uma nova concessão pelo responsável coloca o cartão em `Pendente de Aceitação`; o novo usuário precisa aceitar explicitamente para que o cartão volte a `Ativo`.

### 11.5 Transições

As transições de estado permitidas são:

```text
PENDENTE DE ACEITAÇÃO
        │
        ├── aceitar ───────► ATIVO
        │
        ├── recusar ───────► DESATIVADO
        │
        └── cancelar pelo responsável ──► DESATIVADO

ATIVO ─────────► BLOQUEADO
  │                 │
  │                 ├── desbloquear ──► ATIVO
  │                 │
  │                 └── desativar ────► DESATIVADO
  │
  └───────────────► DESATIVADO

DESATIVADO ──────► ATIVO
      │
      └── nova concessão de adicional [se nunca aceito] ──► PENDENTE DE ACEITAÇÃO
```

O responsável/dono possui autoridade para administrar essas transições conforme as regras específicas de cada tipo de cartão.

O bloqueio não é uma etapa obrigatória para desativação. Um cartão `Bloqueado` pode ser diretamente `Desativado` pelo responsável/dono.

---

## 12. Movimentos Financeiros

Um cartão de crédito recebe um movimento financeiro quando um usuário cria um movimento financeiro e indica, no campo de meio de pagamento, um cartão ao qual possui acesso.

O movimento financeiro continua sendo uma movimentação financeira do CyberBank e passa a compor o extrato da fatura do cartão utilizado.

No momento do lançamento, o movimento é registrado como **previsto** no extrato da fatura.

A movimentação lançada consome o limite disponível conforme as regras de limite do contrato.

O movimento financeiro será considerado **realizado** no momento do pagamento total da fatura à qual pertence.

É possível lançar um movimento financeiro de crédito em uma fatura para representar um valor que entrou no extrato.

O crédito não aumenta o limite global do contrato nem pode fazer o limite disponível superar o limite global. O crédito é utilizado para abater o valor da fatura, conforme as regras detalhadas em `CREDIT-CARDS-INVOICE.md`.

O pagamento parcial é permitido. O pagamento parcial não encerra a fatura: a fatura permanece em estado parcial e os lançamentos continuam como `Previstos` até a quitação total.

As regras detalhadas sobre composição da fatura, pagamento, estados da fatura, efetivação e demais operações financeiras pertencem ao domínio de faturas.

---

## 13. Limite de Crédito

O contrato recebe, no momento de sua criação, um **limite global de crédito**.

Nenhum cartão possui limite independente.

Todos os cartões vinculados ao contrato compartilham o limite global.

Cada movimentação financeira de débito consome parte do limite disponível assim que o movimento é lançado.

Movimentos financeiros de crédito não aumentam o limite global nem permitem que o limite disponível supere o limite global. Seu efeito financeiro detalhado é definido no domínio de faturas.

O limite disponível pode ficar **negativo**. O lançamento de um movimento financeiro não é automaticamente impedido pelo fato de o limite disponível ser insuficiente ou já estar negativo.

Pagamentos realizados sobre a fatura também aumentam o limite disponível conforme as regras financeiras aplicáveis. O detalhamento dos pagamentos e de seus efeitos pertence ao domínio de faturas.

A regra básica do limite disponível é:

```text
Limite disponível =
    Limite global
    - lançamentos financeiros de débito
    + lançamentos financeiros de crédito
    + pagamentos realizados
```

A fórmula representa a composição financeira básica do limite e está subordinada às regras detalhadas do domínio de faturas. Em nenhuma situação o limite disponível deve superar o limite global em decorrência de um movimento de crédito.

O limite pertence ao contrato, e não individualmente aos cartões.

Bloquear, desativar, compartilhar ou conceder um cartão adicional não cria novo limite nem altera o limite global do contrato.

As regras detalhadas de como pagamentos, créditos de fatura, liquidação e demais eventos financeiros afetam o limite disponível pertencem ao domínio de faturas e devem ser definidas em `CREDIT-CARDS-INVOICE.md`.

---

## 14. Relação com Faturas

A fatura é a composição dos extratos de todos os cartões vinculados ao contrato.

Cada cartão contribui com seus movimentos para o extrato da fatura do contrato.

O responsável/dono do contrato possui responsabilidade sobre a fatura do contrato.

O responsável/dono pode pagar a fatura inteira ou parcialmente.

Usuários que utilizam cartões adicionais ou cartões compartilhados podem realizar pagamentos totais ou parciais do extrato de fatura correspondente ao cartão que utilizam, conforme as regras do domínio de faturas.

Um pagamento parcial não encerra a fatura. A fatura permanece parcial e os lançamentos permanecem `Previstos` até a quitação total.

As seguintes regras não são detalhadas neste documento:

- composição detalhada da fatura;
- fechamento;
- vencimento;
- pagamentos totais e parciais;
- quitação;
- créditos de fatura;
- efetivação dos movimentos;
- comportamento detalhado do limite decorrente de eventos de pagamento.

Essas regras pertencem exclusivamente ao documento:

`CREDIT-CARDS-INVOICE.md`

---

## 15. Regras Consolidadas

1. O contrato de cartão de crédito pertence a um ambiente financeiro.
2. O contrato possui um único responsável/dono.
3. O responsável/dono é a autoridade máxima sobre o contrato e seus cartões.
4. O contrato possui uma única instituição financeira.
5. A instituição pode ser um banco cadastrado ou uma instituição financeira declarada pelo usuário.
6. O contrato possui um nome definido pelo responsável.
7. O contrato possui um limite global de crédito.
8. A criação do contrato gera automaticamente um primeiro cartão físico para o responsável.
9. O responsável pode criar quantos cartões físicos quiser.
10. O responsável pode criar quantos cartões adicionais quiser.
11. O responsável pode criar quantos cartões virtuais quiser.
12. Somente o responsável pode criar novos cartões; o primeiro físico é criado automaticamente pelo sistema na criação do contrato.
13. Cartão adicional é sempre físico.
14. Cartão adicional não pode ser compartilhado.
15. Cartão adicional não pode gerar cartão virtual.
16. O adicional possui um único titular de utilização, permanece sob responsabilidade do responsável pelo contrato e não permite troca de titular após a aceitação.
17. O adicional nasce `Pendente de Aceitação`.
18. O destinatário pode aceitar ou recusar o adicional.
19. Indicar um usuário para um adicional não significa pré-aprovar a titularidade.
20. Ao aceitar, o destinatário escolhe o ambiente financeiro no qual utilizará o cartão.
21. Ao recusar, o adicional passa para `Desativado`.
22. O responsável, como dono do contrato e do cartão, pode administrar ou cancelar uma concessão pendente.
23. Um adicional desativado pode receber uma nova concessão pelo responsável, mas volta para `Pendente de Aceitação` e aguarda nova aceitação. Somente se ninguem nunca aceitou este cartao antes. A troca de titular por cartão desativado só acontece se ninguem nunca aceitou esse cartão adicional
24. Cartões físicos e virtuais podem ser compartilhados.
25. Um mesmo cartão físico ou virtual pode ser compartilhado simultaneamente com vários usuários.
26. O compartilhamento não cria novo cartão nem novo contrato.
27. O usuário compartilhado não recebe autoridade administrativa.
28. O usuário compartilhado não pode bloquear nem desbloquear o cartão.
29. Ao desativar um cartão compartilhado, todas as autorizações de compartilhamento são perdidas.
30. Ao reativar um cartão que perdeu compartilhamentos, é necessário realizar novos compartilhamentos.
31. Todos os cartões permanecem vinculados ao contrato original.
32. Todos os cartões utilizam o limite global do contrato.
33. Cada cartão possui identificador técnico interno imutável.
34. O CyberBank não armazena o número completo do cartão.
35. `last4` não é identificador único.
36. Cartões não possuem validade ou data de emissão como regra de negócio.
37. O bloqueio existe exclusivamente para impedir a utilização do cartão como meio de pagamento.
38. Um cartão bloqueado pode ser diretamente desativado pelo responsável, sem necessidade de desbloqueio prévio.
39. Movimentos realizados com cartão são registrados inicialmente como previstos no extrato da fatura.
40. Movimentos são considerados realizados no momento do pagamento total da fatura.
41. Movimentos de crédito podem compor o extrato e são utilizados para abater a fatura.
42. Créditos não aumentam o limite global nem permitem que o limite disponível supere o limite global.
43. O responsável pode pagar a fatura inteira ou parcialmente.
44. Usuários de cartões adicionais ou compartilhados podem pagar total ou parcialmente o extrato correspondente ao cartão utilizado.
45. Pagamento parcial não encerra a fatura.
46. O limite é do contrato, não do cartão.
47. O limite disponível pode ficar negativo.
48. Um lançamento não é automaticamente impedido apenas porque o limite disponível é insuficiente ou negativo.
49. O responsável/dono possui autoridade plena sobre o contrato e seus cartões, não estando suas prerrogativas limitadas à lista de ações exemplificadas neste documento.
50. Regras detalhadas de faturas e pagamentos pertencem ao `CREDIT-CARDS-INVOICE.md`.
