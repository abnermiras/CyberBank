# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o contrato de cartão de crédito, seus cartões, titularidade, autorização de uso, compartilhamento, ciclo de vida, movimentos financeiros associados e limite de crédito.

As regras de **faturas, ciclos de faturamento, fechamento, vencimento, pagamentos, quitação e demais operações próprias da fatura** são definidas em documento próprio:

`CREDIT-CARDS-INVOICE.md`

Este documento mantém somente as regras necessárias para estabelecer a relação entre cartão, contrato, movimento financeiro e fatura. Sempre que uma regra pertencer ao domínio de faturas, este documento remete ao `CREDIT-CARDS-INVOICE.md` e não a redefine.

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
- pode administrar livremente os cartões, seus titulares de utilização, compartilhamentos e estados, respeitadas as regras estruturais do domínio;
- pode realizar qualquer ação de administração permitida pelo sistema sobre o contrato e seus cartões.

As ações explicitamente descritas neste documento não constituem uma lista exaustiva das prerrogativas do responsável/dono. A autoridade decorre de sua condição de dono do contrato (ver §10.1).

A responsabilidade pelo contrato não é transferida pela criação de cartões adicionais nem pelo compartilhamento de cartões.

### 3.4 Encerramento e Preservação do Contrato

O contrato é considerado **encerrado** quando todos os seus cartões estão no estado `Desativado`. O encerramento decorre automaticamente da desativação do último cartão vinculado e não é uma exclusão.

O CyberBank não exclui fisicamente contratos nem cartões. Um contrato encerrado, junto de todos os seus cartões desativados, permanece preservado e apenas deixa de aparecer entre os contratos ativos, mantendo integralmente seu histórico financeiro.

O contrato volta a ficar ativo quando qualquer um de seus cartões é reativado, conforme as regras de reativação do §11.4. Como o contrato sempre possui ao menos o cartão físico do responsável — que pode sempre ser reativado —, o contrato sempre pode ser retomado pelo responsável/dono.

---

## 4. Cadastro e Identidade do Cartão

Cada cartão possui um **identificador técnico interno imutável**, utilizado pelo sistema para identificar univocamente a entidade durante toda a sua existência.

O identificador técnico não depende:

- do estado do cartão;
- do titular de utilização;
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
- titular de utilização do cartão, quando aplicável.

Os últimos quatro dígitos não são identificador único do cartão. O mesmo contrato pode possuir mais de um cartão com os mesmos quatro dígitos finais; a distinção entre eles é feita pelo identificador técnico interno, não pelo `last4`.

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

O cartão `Adicional` é um tipo próprio do domínio, embora seja fisicamente um cartão físico. Ele possui regras específicas de titularidade de utilização e aceitação (ver §7).

O compartilhamento não é um tipo de cartão. É uma autorização de utilização que pode existir sobre cartões físicos e virtuais, exceto cartões adicionais (ver §9).

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
- possui um único titular de utilização (ver §7.1).

O responsável pelo contrato pode criar quantos cartões adicionais quiser. Não existe limite de quantidade de cartões adicionais no contrato.

### 7.1 Titularidade de Utilização do Cartão Adicional

Esta seção é a **definição única** da titularidade de utilização do cartão adicional. As demais seções que mencionam concessão, aceitação, reativação ou troca de titular remetem a esta seção e não redefinem estas regras.

**Titular de utilização.** O cartão adicional possui um único **titular de utilização**: o usuário autorizado a utilizá-lo. O titular de utilização não se torna responsável pelo contrato nem proprietário do crédito. A responsabilidade pelo contrato e pelo limite global permanece com o responsável/dono do contrato.

**A aceitação estabelece a titularidade.** Um cartão adicional é criado por meio de uma **concessão** a um usuário e nasce no estado `Pendente de Aceitação`. O cartão só passa a ter titular de utilização quando o usuário destinatário **aceita** a concessão. Indicar um usuário representa apenas o envio de uma solicitação e não pré-aprova nem estabelece a titularidade.

**A titularidade aceita é imutável.** Uma vez que um cartão adicional foi aceito por um titular de utilização, esse cartão **não pode ser transferido para outro usuário em nenhuma hipótese** — nem enquanto ativo, nem após ser desativado. Para conceder um cartão adicional a um usuário diferente, o responsável/dono deve criar uma **nova entidade de cartão adicional**.

**Exceção — cartão adicional nunca aceito.** Um cartão adicional que se encontra em `Desativado` e que **nunca foi aceito por nenhum titular de utilização** pode receber uma nova concessão pelo responsável/dono. Um cartão adicional está nessa condição quando chegou a `Desativado` por uma destas vias, antes de qualquer aceitação:

- o destinatário recusou a concessão; ou
- o responsável/dono cancelou a concessão enquanto ela ainda estava pendente.

Nesse caso, a nova concessão retorna o cartão a `Pendente de Aceitação` e o novo destinatário deverá aceitá-la explicitamente para que o cartão passe a `Ativo`.

**Reativação depende de o cartão já ter tido titular.** As duas saídas do estado `Desativado` para um cartão adicional dependem de ele já ter sido aceito alguma vez:

- Um cartão adicional que **já foi aceito** e posteriormente desativado pode ser **reativado** pelo responsável/dono, retornando diretamente a `Ativo`, **mantendo o titular de utilização original** e **sem nova aceitação**. Ele não pode ser indicado a outro usuário.
- Um cartão adicional que **nunca foi aceito** não possui titular de utilização e, por isso, **não pode ser reativado diretamente para `Ativo`**. Sua única saída de `Desativado` é a nova concessão, que o leva a `Pendente de Aceitação`.

Por isso o sistema deve distinguir, ao longo de toda a vida do cartão, um cartão adicional desativado que **já foi aceito** de um que **nunca foi aceito** — os dois compartilham o estado `Desativado`, mas admitem operações diferentes.

### 7.2 Fluxo de Criação e Aceitação

Ao criar um cartão adicional, o responsável pelo contrato informa:

- banco ou instituição financeira;
- nome do contrato;
- apelido do cartão;
- tipo de emissão;
- últimos quatro dígitos;
- usuário ao qual a utilização será concedida.

O cartão adicional nasce no estado `Pendente de Aceitação`. O usuário destinatário recebe a solicitação e pode:

- **aceitar** — o usuário conclui a aceitação e escolhe em qual de seus ambientes financeiros o cartão será disponibilizado como meio de pagamento; o cartão passa a `Ativo` e o destinatário torna-se seu titular de utilização;
- **recusar** — o cartão passa a `Desativado` sem ter tido titular de utilização.

Enquanto a concessão estiver pendente, o responsável/dono pode cancelá-la ou administrá-la, pois é o dono do contrato e do cartão. Cancelar a concessão pendente leva o cartão a `Desativado` sem que ele tenha tido titular de utilização. Uma nova indicação enquanto a concessão está pendente não equivale à aceitação do novo usuário.

As condições sob as quais um cartão desativado pode ou não receber uma nova concessão estão definidas na §7.1.

### 7.3 Administração do Cartão Adicional

O responsável/dono do contrato pode:

- ativar (respeitadas as regras de reativação da §7.1);
- desativar;
- bloquear;
- desbloquear;
- criar novos cartões adicionais;
- administrar as concessões e solicitações de aceitação;
- cancelar uma solicitação de aceitação pendente;
- conceder um cartão adicional nunca aceito a outro usuário, conforme a §7.1;
- realizar qualquer outra operação de administração permitida pelo sistema.

O titular de utilização de um cartão adicional pode:

- utilizar o cartão;
- bloquear o próprio cartão;
- desbloquear o próprio cartão.

O titular de utilização não possui autoridade administrativa sobre o contrato ou sobre outros cartões.

Um cartão adicional desativado permanece preservado para fins históricos e não pode ser convertido em outro tipo de cartão. A criação de um novo cartão adicional é uma nova entidade de cartão e não transfere automaticamente o histórico do cartão anterior.

**Titular de utilização removido do sistema.** Se o titular de utilização de um cartão adicional deixar de existir no sistema (por exemplo, remoção da conta ou do acesso), o cartão adicional passa a `Desativado` e deixa de estar disponível para uso. Como a titularidade aceita é imutável (§7.1), esse cartão não é transferido para outro usuário: ele permanece preservado para fins históricos, mantendo todo o seu histórico de movimentos, de modo a evidenciar o que ocorreu.

---

## 8. Cartão Virtual

O cartão virtual é um cartão criado dentro do contrato para utilização em meios digitais ou outras finalidades definidas pelo responsável pelo contrato.

**Somente o responsável pelo contrato pode criar cartões virtuais.** O responsável pode criar quantos cartões virtuais quiser; não existe limite de quantidade por contrato.

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

O cartão virtual:

- não cria novo contrato;
- não possui limite próprio;
- utiliza o limite global do contrato;
- possui identidade técnica própria;
- pode ser compartilhado com um ou mais usuários (ver §9);
- não pode ser criado diretamente em nome de outro usuário.

O usuário que receber utilização de um cartão virtual por compartilhamento não se torna responsável pelo contrato nem proprietário do cartão.

O responsável/dono do contrato possui autoridade plena sobre o cartão virtual, incluindo suas operações de administração e ciclo de vida.

---

## 9. Compartilhamento de Cartões

Cartões físicos e virtuais podem ser compartilhados com um ou mais outros usuários do sistema. Cartões adicionais não podem ser compartilhados.

O compartilhamento não cria novo cartão e não cria novo contrato. O cartão continua pertencendo ao contrato e permanece sob responsabilidade do responsável pelo contrato.

Um mesmo cartão pode possuir simultaneamente autorizações de compartilhamento para vários usuários. Cada usuário compartilhado possui sua própria autorização de utilização sobre o mesmo cartão.

### 9.1 Administração do Compartilhamento

Somente o responsável/dono do contrato pode administrar o compartilhamento dos cartões, incluindo:

- compartilhar um cartão elegível;
- descompartilhar um cartão;
- conceder autorização de uso a outro usuário;
- remover autorização de uso;
- administrar qualquer outro aspecto do compartilhamento permitido pelo sistema.

O compartilhamento concede autorização de utilização, mas não transfere:

- titularidade do contrato;
- responsabilidade pelo limite;
- propriedade do cartão;
- autoridade administrativa sobre o cartão.

O usuário que recebe o compartilhamento não pode bloquear nem desbloquear o cartão.

Quando um cartão compartilhado é desativado, **todas as autorizações de compartilhamento existentes sobre o cartão são perdidas**. A reativação do cartão não restaura automaticamente nenhum compartilhamento. Para compartilhar novamente um cartão reativado, o responsável pelo contrato deve realizar novos compartilhamentos para os usuários desejados.

### 9.2 Utilização por Usuário Compartilhado

O usuário que recebe um cartão compartilhado pode utilizá-lo em movimentos financeiros enquanto possuir a autorização de compartilhamento e o cartão estiver disponível para utilização.

Cada usuário compartilhado pode visualizar, no extrato da fatura do cartão compartilhado, os lançamentos inerentes aos gastos realizados por ele. As regras detalhadas de visualização da fatura e do extrato são definidas em `CREDIT-CARDS-INVOICE.md`.

O usuário compartilhado não possui autoridade administrativa sobre o cartão. A administração permanece com o responsável/dono do contrato.

---

## 10. Titularidade, Responsabilidade e Autoridade

Todos os cartões criados dentro de um contrato pertencem ao **responsável/dono do contrato**.

A existência de um titular de utilização em um cartão adicional, ou de um usuário autorizado por compartilhamento, não transfere a propriedade do cartão, a responsabilidade pelo crédito ou o limite global.

O cartão adicional possui um titular de utilização específico, cujas regras de estabelecimento e imutabilidade estão definidas na §7.1. O usuário compartilhado possui somente uma autorização de utilização concedida pelo responsável/dono do contrato.

### 10.1 Princípio de Autoridade do Dono

O responsável/dono do contrato é a autoridade máxima sobre o contrato e seus cartões.

Sua autoridade não é limitada à lista de ações explicitamente enumeradas neste documento. As ações descritas documentam os principais comportamentos operacionais, mas não constituem uma lista exaustiva de poderes.

O responsável/dono pode realizar qualquer operação de administração permitida pelo sistema sobre o contrato, os cartões vinculados, as concessões de utilização dos cartões adicionais, os compartilhamentos, os estados dos cartões e as configurações do contrato.

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

O estado `Pendente de Aceitação` existe para cartões adicionais concedidos a um usuário e ainda não aceitos pelo destinatário.

Enquanto estiver pendente:

- o usuário destinatário ainda não pode utilizar o cartão e não deve considerá-lo meio de pagamento ativo;
- o usuário destinatário pode aceitar ou recusar a solicitação;
- o responsável/dono continua possuindo autoridade sobre o cartão e sobre a concessão, podendo cancelá-la;
- indicar o usuário não equivale à aceitação nem pré-aprova a titularidade.

As transições a partir deste estado, e as condições para que um cartão desativado retorne a `Pendente de Aceitação`, estão definidas na §7.1 e resumidas na §11.5.

### 11.2 Ativo

Um cartão ativo pode receber novos movimentos financeiros quando o usuário que realiza a operação possuir autorização de utilização.

### 11.3 Bloqueado

Um cartão bloqueado continua existindo no contrato, porém não pode ser utilizado para novos movimentos financeiros.

O bloqueio não exclui o cartão, não o remove do contrato, não altera sua identidade, não transfere sua responsabilidade, não elimina histórico financeiro e não encerra o cartão.

O responsável/dono do contrato pode bloquear e desbloquear qualquer cartão. O titular de utilização de um cartão adicional pode bloquear e desbloquear somente o próprio cartão adicional. Usuários que utilizam um cartão por compartilhamento não podem bloquear nem desbloquear o cartão.

O responsável/dono pode desativar diretamente um cartão que esteja `Bloqueado`, sem necessidade de desbloqueá-lo previamente.

### 11.4 Desativado

Um cartão desativado não pode receber novos movimentos financeiros.

A desativação não exclui o cartão, não remove seu histórico, não altera movimentos financeiros já registrados, não transfere o cartão para outro usuário e não encerra obrigações financeiras já existentes. O cartão desativado permanece preservado para fins históricos.

Qualquer autorização de compartilhamento existente sobre o cartão é perdida no momento da desativação. A reativação não restaura automaticamente essa autorização (ver §9.1).

As saídas do estado `Desativado` são:

- **cartões físicos e virtuais**, e **cartões adicionais já aceitos**: podem ser reativados pelo responsável/dono, retornando diretamente a `Ativo`. No caso do adicional já aceito, a reativação mantém o titular de utilização original e não exige nova aceitação;
- **cartões adicionais nunca aceitos**: não possuem titular e não podem ir diretamente a `Ativo`; sua única saída é a nova concessão, que os leva a `Pendente de Aceitação`.

As condições completas desses caminhos estão definidas na §7.1.

### 11.5 Transições

O diagrama abaixo resume as transições. A regra normativa das transições do cartão adicional é a §7.1; este diagrama é um resumo visual.

```text
PENDENTE DE ACEITAÇÃO
        │
        ├── aceitar ─────────────────► ATIVO
        │
        ├── recusar ─────────────────► DESATIVADO (nunca aceito)
        │
        └── cancelar pelo responsável ► DESATIVADO (nunca aceito)

ATIVO ───────────► BLOQUEADO
  │                   │
  │                   ├── desbloquear ► ATIVO
  │                   │
  │                   └── desativar ──► DESATIVADO (já aceito, se adicional)
  │
  └─────────────────► DESATIVADO (já aceito, se adicional)

DESATIVADO (físico/virtual, ou adicional já aceito)
        └── reativar ──────────────► ATIVO   (adicional: mantém titular original)

DESATIVADO (adicional nunca aceito)
        └── nova concessão ────────► PENDENTE DE ACEITAÇÃO
```

O responsável/dono possui autoridade para administrar essas transições conforme as regras específicas de cada tipo de cartão.

O bloqueio não é uma etapa obrigatória para desativação: um cartão `Bloqueado` pode ser diretamente `Desativado` pelo responsável/dono.

---

## 12. Movimentos Financeiros

Um cartão de crédito recebe um movimento financeiro quando um usuário cria um movimento e indica, no campo de meio de pagamento, um cartão ao qual possui acesso.

O movimento financeiro continua sendo uma movimentação financeira do CyberBank e passa a compor o extrato da fatura do cartão utilizado.

No momento do lançamento, o movimento é registrado como **previsto** no extrato da fatura e consome o limite disponível conforme as regras do §13. O movimento é considerado **realizado** no momento do pagamento total da fatura à qual pertence.

É possível lançar um movimento financeiro de crédito em uma fatura para representar um valor que entrou no extrato. O crédito não aumenta o limite global do contrato nem permite que o limite disponível supere o limite global; ele é utilizado para abater o valor da fatura, conforme as regras de `CREDIT-CARDS-INVOICE.md`.

O pagamento parcial é permitido e não encerra a fatura: a fatura permanece em estado parcial e os lançamentos continuam como `Previstos` até a quitação total.

As regras detalhadas sobre composição da fatura, pagamento, estados da fatura, efetivação e demais operações financeiras pertencem ao domínio de faturas (`CREDIT-CARDS-INVOICE.md`).

### 12.1 Moeda

O CyberBank é um sistema multimoeda. No domínio de cartões tratado neste documento, porém, o movimento financeiro é sempre lançado e registrado em **reais (BRL)**.

Compras realizadas em moeda estrangeira são lançadas pelo usuário **já convertidas para real**, junto com os encargos aplicáveis, como impostos e IOF, lançados normalmente como movimentos financeiros. O usuário não precisa informar o valor em moeda estrangeira para que o sistema registre o movimento.

O tratamento de cartões que operam diretamente em moeda estrangeira será definido em documento próprio e não faz parte deste documento.

---

## 13. Limite de Crédito

O contrato recebe, no momento de sua criação, um **limite global de crédito**. Nenhum cartão possui limite independente; todos os cartões vinculados ao contrato compartilham o limite global.

Cada movimentação financeira de débito consome parte do limite disponível assim que o movimento é lançado. Movimentos financeiros de crédito não aumentam o limite global nem permitem que o limite disponível supere o limite global; seu efeito financeiro detalhado é definido no domínio de faturas.

O limite disponível pode ficar **negativo**. O lançamento de um movimento financeiro não é automaticamente impedido pelo fato de o limite disponível ser insuficiente ou já estar negativo.

Pagamentos realizados sobre a fatura aumentam o limite disponível conforme as regras financeiras aplicáveis. O detalhamento dos pagamentos e de seus efeitos pertence ao domínio de faturas.

A regra básica do limite disponível é:

```text
Limite disponível =
    Limite global
    - lançamentos financeiros de débito
    + lançamentos financeiros de crédito
    + pagamentos realizados
```

A fórmula representa a composição financeira básica do limite e está subordinada às regras detalhadas do domínio de faturas. Em nenhuma situação o limite disponível deve superar o limite global em decorrência de um movimento de crédito.

O limite pertence ao contrato, e não individualmente aos cartões. Bloquear, desativar, compartilhar ou conceder um cartão adicional não cria novo limite nem altera o limite global do contrato.

---

## 14. Relação com Faturas

A fatura é a composição dos extratos de todos os cartões vinculados ao contrato. Cada cartão contribui com seus movimentos para o extrato da fatura do contrato.

O responsável/dono do contrato possui responsabilidade sobre a fatura e pode pagá-la integral ou parcialmente. Usuários que utilizam cartões adicionais ou cartões compartilhados podem realizar pagamentos totais ou parciais do extrato de fatura correspondente ao cartão que utilizam, conforme as regras do domínio de faturas.

Um pagamento parcial não encerra a fatura: a fatura permanece parcial e os lançamentos permanecem `Previstos` até a quitação total.

As seguintes regras não são detalhadas neste documento e pertencem exclusivamente ao `CREDIT-CARDS-INVOICE.md`:

- composição detalhada da fatura;
- fechamento;
- vencimento;
- pagamentos totais e parciais;
- quitação;
- créditos de fatura;
- efetivação dos movimentos;
- comportamento detalhado do limite decorrente de eventos de pagamento.

---

## 15. Regras Consolidadas

> Esta lista é um **índice derivado** das seções anteriores, para consulta rápida. Em caso de divergência, prevalece o texto da seção correspondente. A definição normativa da titularidade do cartão adicional é a §7.1.

1. O contrato de cartão de crédito pertence a um ambiente financeiro.
2. O contrato possui um único responsável/dono, que é a autoridade máxima sobre o contrato e seus cartões (§10.1).
3. O contrato possui uma única instituição financeira, que pode ser um banco cadastrado ou uma instituição declarada pelo usuário.
4. O contrato possui um nome definido pelo responsável e um limite global de crédito.
5. A criação do contrato gera automaticamente um primeiro cartão físico para o responsável.
6. Somente o responsável pode criar novos cartões (físicos, adicionais e virtuais); o primeiro físico é criado automaticamente na criação do contrato.
7. O responsável pode criar quantos cartões físicos, adicionais e virtuais quiser.
8. Cartão adicional é sempre físico, não pode ser compartilhado e não pode gerar cartão virtual.
9. O cartão adicional possui um único titular de utilização, estabelecido pela aceitação (§7.1).
10. A titularidade de utilização aceita é imutável: um adicional já aceito nunca é transferido a outro usuário; para outro usuário, cria-se uma nova entidade de cartão adicional (§7.1).
11. Um adicional nasce `Pendente de Aceitação`; o destinatário pode aceitar ou recusar; indicar um usuário não pré-aprova a titularidade.
12. Ao aceitar, o destinatário escolhe o ambiente financeiro no qual utilizará o cartão e este passa a `Ativo`; ao recusar, o cartão passa a `Desativado` sem ter tido titular.
13. Um adicional em `Desativado` que **nunca foi aceito** (por recusa ou cancelamento da concessão pendente) pode receber nova concessão e volta a `Pendente de Aceitação` (§7.1).
14. Um adicional em `Desativado` que **já foi aceito** só pode ser reativado para `Ativo` mantendo o titular original; não pode ser cedido a outro usuário (§7.1).
15. Um adicional que **nunca foi aceito** não pode ser reativado diretamente para `Ativo`, pois não possui titular (§7.1).
16. O sistema distingue um adicional desativado já aceito de um nunca aceito, pois admitem operações diferentes (§7.1, §11.4).
17. Cartões físicos e virtuais podem ser compartilhados simultaneamente com vários usuários; o compartilhamento não cria novo cartão nem novo contrato.
18. Somente o responsável administra o compartilhamento; o usuário compartilhado não recebe autoridade administrativa e não pode bloquear nem desbloquear o cartão.
19. Ao desativar um cartão compartilhado, todas as autorizações de compartilhamento são perdidas e não são restauradas automaticamente na reativação.
20. Todos os cartões permanecem vinculados ao contrato original e utilizam seu limite global.
21. Cada cartão possui identificador técnico interno imutável; o CyberBank não armazena o número completo do cartão; `last4` não é identificador único.
22. Cartões não possuem validade nem data de emissão como regra de negócio.
23. O bloqueio serve exclusivamente para impedir a utilização do cartão como meio de pagamento; um cartão bloqueado pode ser desativado diretamente, sem desbloqueio prévio.
24. Movimentos feitos com cartão são registrados como `Previstos` no extrato e considerados `Realizados` no pagamento total da fatura.
25. Movimentos de crédito compõem o extrato e abatem a fatura, mas não aumentam o limite global nem permitem que o limite disponível o supere.
26. O responsável pode pagar a fatura integral ou parcialmente; usuários de cartões adicionais ou compartilhados podem pagar o extrato correspondente ao cartão que utilizam; pagamento parcial não encerra a fatura.
27. O limite é do contrato, não do cartão; o limite disponível pode ficar negativo e um lançamento não é impedido automaticamente por limite insuficiente ou negativo.
28. O CyberBank é multimoeda, mas neste domínio o movimento é sempre lançado em BRL; compras em moeda estrangeira são lançadas já convertidas para real, com impostos e IOF lançados normalmente (§12.1).
29. Se o titular de utilização de um cartão adicional for removido do sistema, o cartão passa a `Desativado`, não é transferido a outro usuário e permanece preservado para histórico (§7.3).
30. O contrato é encerrado automaticamente quando todos os seus cartões estão desativados; contrato e cartões nunca são excluídos fisicamente e o contrato volta a ficar ativo ao reativar qualquer cartão (§3.4).
31. Regras detalhadas de faturas e pagamentos pertencem ao `CREDIT-CARDS-INVOICE.md`.
