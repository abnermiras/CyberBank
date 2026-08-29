---
id: 02-dominio/lancamento
titulo: Lançamento
dono: campos, as duas datas, situacao, transferencia, correcao versus estorno e invariantes do lancamento
ler-junto: [02-dominio/conta, 02-dominio/meio-de-pagamento, 02-dominio/fatura-cartao, 02-dominio/recorrencia]
status: rascunho
---

# Lançamento

A unidade central do sistema: **um evento financeiro que altera o saldo de uma conta.**
Tudo que o Cyberbank sabe sobre dinheiro é soma de lançamento.

Um lançamento pertence a um ambiente e a **uma** conta. O ambiente é o de **quem lançou**,
e a conta pode ser de outro ambiente, se compartilhada
(`docs/02-dominio/compartilhamento.md`). Movimento entre duas contas é um par de
lançamentos, não um lançamento com dois lados — ver Transferência.

## Campos

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `ambiente` | sim | O ambiente de **quem lançou**, nunca o da conta. Nunca muda. `docs/02-dominio/ambiente-financeiro.md` |
| `conta` | sim | A conta cujo saldo este lançamento move |
| `sentido` | sim | `ENTRADA` ou `SAIDA` |
| `valor` | sim | Inteiro em centavos, **sempre positivo**. O sinal vem do `sentido`, nunca do valor |
| `dataEvento` | sim | Quando aconteceu na vida real |
| `dataEfeito` | sim | Quando mexe no saldo. Ver As duas datas |
| `descricao` | sim | O que o usuário lê no extrato |
| `situacao` | sim | `PREVISTO`, `PROVISIONADO` ou `REALIZADO`. Ver Dois eixos independentes |
| `categoria` | não | Obrigatória para o lançamento deixar de ser pendência |
| `meioDePagamento` | não | Obrigatório em gasto e receita reais. Ausente em transferência, aporte, resgate, rendimento e lançamento de abertura |
| `fatura` | não | Preenchido quando o meio é crédito. **Nasce** pelo status da fatura, nunca pela data; editável para qualquer fatura, aberta ou não. Mover **não muda data nenhuma** — no crédito `dataEfeito = dataEvento`, sempre. `docs/02-dominio/fatura-cartao.md` |
| `transferenciaId` | não | Amarra os dois lançamentos de uma transferência |
| `origemParcelamento` | não | A compra que gerou esta parcela. `docs/02-dominio/recorrencia.md` |
| `rolagemDeFatura` | não | Amarra o par que move o saldo não pago de uma fatura para a seguinte. `ADR-0005` |
| `estabelecimento` | não | Texto bruto da captura, antes de normalizar |
| `autor` | sim | Qual usuário criou. Em ambiente compartilhado, "quem lançou isso?" é a primeira pergunta. Lançamento que o **ciclo** cria sozinho é assinado pelo dono do ambiente da conta — ver Lançamento que o sistema cria |

`valor` positivo com `sentido` separado não é preciosismo: valor com sinal transforma todo
relatório em `SUM(CASE WHEN ...)` e faz um sinal trocado passar despercebido.

## As duas datas

Registrar um boleto que vence dia 10 e pagá-lo dia 14 são **duas datas**, e tratar como uma
é o bug que faz o saldo mentir o mês inteiro.

- **`dataEvento`** — quando a compra aconteceu. É por ela que o usuário procura e é ela
  que o relatório de gasto por categoria usa: o mercado de terça foi gasto de terça.
- **`dataEfeito`** — quando o dinheiro sai da conta. É por ela que o saldo se calcula.

Em quase todo meio as duas são iguais, **crédito incluído**: comprar no cartão cria dívida
na hora, na conta `CARTAO` (`ADR-0003`). O boleto é a exceção que faz os dois campos
existirem — e o `PREVISTO` é a outra. **A regra que calcula `dataEfeito` por tipo de meio vive em
`docs/02-dominio/meio-de-pagamento.md`** — aqui só fica o fato de que os dois campos existem.

## Dois eixos independentes

Situação e categorização são coisas separadas e não se misturam num estado só. Um
lançamento capturado hoje pode estar realizado e pendente ao mesmo tempo; uma parcela de
dezembro pode estar prevista e já categorizada.

**`situacao`** — em que ponto entre o **fato** e a **liquidação** este lançamento está?
São três valores, e o do meio existe porque as duas coisas não acontecem juntas no crédito
(`ADR-0006`).

| Valor | Significa | Entra no saldo realizado | No projetado |
|---|---|:--:|:--:|
| `PREVISTO` | Vai acontecer, ainda não aconteceu | não | sim |
| `PROVISIONADO` | **Aconteceu, falta liquidar** | **sim** | sim |
| `REALIZADO` | Aconteceu e foi liquidado | sim | sim |

`PROVISIONADO` é a situação da compra no crédito, do dia da compra até a fatura ser paga.
O boleto registrado e não pago **não** é provisionado: ele é lançamento da conta corrente, e
nela nada se moveu ainda.

**O teste para "entra no saldo" é `situacao !== 'PREVISTO'`**, nunca
`situacao === 'REALIZADO'`.

**`categoria` preenchida ou não** — um lançamento sem categoria **é** a pendência do
glossário. Não existe estado `PENDENTE` separado: pendência é uma **consulta**, e um
estado a menos é um estado que não dessincroniza.

A consulta não é "sem categoria" — é **"sem categoria e que espera uma"**. Ficam de fora
transferência, aporte, resgate, rendimento, pagamento de fatura, rolagem e lançamento de abertura:
esses não têm categoria por natureza, e não são trabalho pendente para ninguém.
*(A definição larga foi corrigida depois que o protótipo mostrou a abertura de conta
aparecendo na fila de pendências.)*

**Fora do cartão a transição é automática: o lançamento vira `REALIZADO` quando a
`dataEfeito` chega.** Sem confirmação e sem fila. No cartão, quem liquida é o **encerramento
da fatura** — quitada, ou vencida e rolada (`docs/02-dominio/fatura-pagamento.md`).

O preço dessa automação tem nome, e ele é o boleto: o sistema afirma o fato pela **data**, não
por ter observado o dinheiro sair. Boleto esquecido, débito que não passou por falta de saldo,
cobrança que o banco atrasou — nos três o saldo realizado desconta dinheiro que ainda está na
conta. Foi escolhido assim porque uma fila de confirmação cobra atrito em **todo** lançamento
previsto para acertar a minoria que dá errado, e porque a Fase 2 resolve pela raiz: com o
extrato na mão, a conciliação confirma pelo fato (`docs/02-dominio/importacao-conciliacao.md`).

**A automação só anda para frente** — `PREVISTO → PROVISIONADO → REALIZADO`, com qualquer
salto válido, e nunca ao contrário. **A correção do usuário anda nos dois sentidos**, porque
ela não descreve o dinheiro: descreve o registro. Devolver a `PREVISTO` o boleto que o sistema
realizou pela data é correção comum, com histórico — ver Correção não é estorno. Estornar
seria pior: inventaria um dinheiro que voltou.

## Transferência

Mover dinheiro entre duas contas — inclusive **aporte, resgate**
(`docs/02-dominio/aplicacao-patrimonio.md`) e o **pagamento de fatura de cartão**
(`docs/02-dominio/fatura-cartao.md`) — cria **dois lançamentos** com o mesmo
`transferenciaId`: uma `SAIDA` na conta de origem e uma `ENTRADA` na de destino.

Por que dois e não um com origem e destino: o saldo de uma conta continua sendo a soma
simples dos lançamentos dela. Um lançamento de dois lados obrigaria todo cálculo de saldo
a perguntar "sou a origem ou o destino?" — em toda consulta, para sempre.

Regras do par:

- Os dois lançamentos existem juntos ou não existem. Não há metade de transferência.
- Mesmo valor, sentidos opostos, contas **diferentes** — ambas acessíveis ao ambiente do
  lançamento, próprias ou compartilhadas (`docs/02-dominio/compartilhamento.md`).
- Editar ou apagar um lado age no par inteiro.
- Transferência **não tem categoria** e não aparece no relatório de gasto: o dinheiro não
  saiu da vida do usuário, mudou de bolso.

## Lançamento que o sistema cria

Nem todo lançamento sai de alguém digitando. Quatro nascem do ciclo: os dois lados da
**rolagem**, o **pagamento previsto** que o fechamento gera, o **lançamento de abertura** de
uma conta e — quando a recorrência entrar — a **ocorrência do ciclo**.

| Campo | Valor |
|---|---|
| `autor` | O **dono do ambiente** ao qual a conta pertence, no instante em que o lançamento nasce |
| `ambiente` | O do **dono do objeto**: a conta `CARTAO` na rolagem e no pagamento previsto, a conta no lançamento de abertura. A regra "o ambiente é o de quem lançou" não se aplica quando ninguém lançou |

O `autor` continua **obrigatório** de propósito. Um campo que aceita vazio obriga toda
consulta e toda tela a tratar o vazio, e um bug que esquecesse de preenchê-lo passaria
despercebido como "foi o sistema".

**O preço, escrito para não ser esquecido:** o histórico afirma que o dono lançou algo que
ele não lançou — e responder "quem lançou isso?" é a razão de o campo existir. Na prática a
descrição desfaz a confusão: ninguém lê "Saldo da fatura anterior" e pensa numa pessoa.
**Revisitar se** alguém, num ambiente compartilhado, discutir a autoria de um lançamento que
o ciclo criou. Aí o vazio — ou um campo `origem` ao lado — passa a valer o custo.

## Correção não é estorno

Duas coisas diferentes que a mesma palavra costuma esconder:

| Situação | O que houve | O que o sistema faz |
|---|---|---|
| **Correção** | O registro está errado: valor digitado errado, categoria errada, conta errada | **Edita o lançamento** e guarda o que mudou no histórico |
| **Estorno** | O dinheiro voltou de verdade: compra cancelada, devolução, chargeback | **Cria um lançamento novo** de sentido oposto, ligado ao original |

No crédito, o estorno de uma compra parcelada credita o **valor total** de uma vez, e as
parcelas restantes seguem correndo — os dois se compensam. O parcelamento não é editado
nem cancelado: ver `docs/02-dominio/recorrencia.md`.

Estorno é evento financeiro — aconteceu na vida e tem data própria. Apagar o lançamento
original faria o extrato divergir do banco, que mostra a compra e a devolução.

A correção mais comum fora do cartão é justamente **desfazer o que a data realizou**: o
boleto que venceu e não foi pago. Nenhum dinheiro voltou, então não é estorno — é o registro
que ficou errado, e ele volta para `PREVISTO` como qualquer outro campo se corrige.

## Edição e histórico

Lançamento se edita direto, e toda alteração fica registrada: **quem**, **quando**, o
campo, e o valor antes e depois. O histórico é obrigatório para os campos que mexem em
saldo — `valor`, `sentido`, `conta`, `dataEfeito` e `situacao`.

O motivo do histórico é o ambiente compartilhado: sem ele, "esse valor mudou" vira
discussão entre duas pessoas sem resposta.

**Nenhum estado de fatura trava a edição.** Lançamento de fatura fechada se edita como
qualquer outro, e o campo `fatura` aponta para qualquer fatura do cartão, aberta ou não.
Fatura fechada não congela nada — o sistema não tem a palavra final sobre o dinheiro do
usuário; o que ele deve é mostrar a consequência antes e guardar quem mudou o quê.

O que muda conforme a fatura é o **aviso**: corrigir lançamento de fatura já paga faz o
sistema nomear o pagamento e a diferença, e **perguntar** se o pagamento deve ser ajustado
ou se a diferença fica como saldo da conta `CARTAO`. As regras estão em
`docs/02-dominio/fatura-cartao.md`. Aqui vale o princípio: **ação retroativa mostra o
impacto antes de confirmar e vai para o histórico**, sem lançamento de ajuste no extrato.
Nada precisa ser recalculado: como saldo é sempre a soma dos lançamentos
(`docs/02-dominio/conta.md`), reescrever o valor já refaz tudo que deriva dele.

## Invariantes

- `valor` é sempre positivo. Zero não é lançamento.
- Todo lançamento tem exatamente um ambiente e uma conta.
- Todo lançamento tem `autor`, inclusive os que o ciclo cria sozinho — nesses, o dono do
  ambiente da conta.
- Um lançamento **nunca muda de ambiente** — nem por edição, nem por correção. O certo é
  apagar em um e criar no outro, para o saldo dos dois continuar verdadeiro.
- `dataEfeito` nunca é anterior à `dataEvento`.
- `transferenciaId`, quando existe, aparece em exatamente dois lançamentos.
- `rolagemDeFatura`, quando existe, aparece em exatamente dois — na mesma conta, somando zero.
- Nenhum estado de fatura impede a edição de um lançamento (`docs/02-dominio/fatura-cartao.md`).
- A situação, **quando a automação a move**, só anda para frente:
  `PREVISTO → PROVISIONADO → REALIZADO`, nunca ao contrário. Só a **correção** do usuário
  volta, e sempre com histórico.
- Lançamento de conta inativa não é criado (`docs/02-dominio/conta.md`).
- A **categoria** é sempre do mesmo ambiente do lançamento.
- A **conta** é do mesmo ambiente, ou de um que a compartilhou com ele.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Como `dataEfeito` é calculada por tipo de meio | `02-dominio/meio-de-pagamento` |
| Como as parcelas nascem e o que acontece ao editar a compra | `02-dominio/recorrencia` |
| Fechamento, pagamento e reabertura de fatura | `02-dominio/fatura-cartao` |
| Como um lançamento capturado vira realizado sem duplicar | `02-dominio/importacao-conciliacao` |
| Como a categoria é atribuída automaticamente | `02-dominio/regras-categorizacao` |
