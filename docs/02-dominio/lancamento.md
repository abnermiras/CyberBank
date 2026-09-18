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
| `dataEvento` | sim | Quando aconteceu na vida real. **Data pura**, sem hora e sem fuso |
| `dataEfeito` | sim | Quando mexe no saldo. **Data pura**, sem hora e sem fuso. Ver As duas datas |
| `descricao` | sim | O que o usuário lê no extrato |
| `situacao` | sim | `PREVISTO`, `PROVISIONADO` ou `REALIZADO`. Ver Dois eixos independentes |
| `categoria` | não | Vazia significa **pendência**, e nada além disso: o lançamento que o ciclo cria nasce com a **categoria de sistema** da operação dele (`docs/02-dominio/categoria.md`) |
| `meioDePagamento` | não | Obrigatório em gasto e receita reais. Ausente em transferência, aporte, resgate, rendimento e lançamento de abertura |
| `fatura` | não | Em qual fatura o lançamento **entra**. Preenchido quando o meio é crédito. **Nasce** pelo status da fatura, nunca pela data; editável para qualquer fatura, aberta ou não. Mover **não muda data nenhuma** — no crédito `dataEfeito = dataEvento`, sempre. `docs/02-dominio/fatura-cartao.md` |
| `transferenciaId` | não | Amarra os dois lançamentos de uma transferência |
| `parcelamento` | não | O `Parcelamento` de que esta parcela faz parte. `docs/02-dominio/recorrencia.md` |
| `recorrencia` | não | A `Recorrencia` que gerou esta ocorrência. Nunca junto com `parcelamento` |
| `estornoDe` | não | O lançamento original que este estorna. Ver Correção não é estorno |
| `pagamentoDeFatura` | não | A fatura que este pagamento **quita** — distinto de `fatura`, que é onde o lançamento **entra**. O pagamento não é gasto da fatura e não conta no total dela (`docs/02-dominio/fatura-pagamento.md`) |
| `rolagemDeFatura` | não | Amarra o par que move o saldo não pago de uma fatura para a seguinte. `ADR-0005` |
| `estabelecimento` | não | Texto bruto da captura, antes de normalizar |
| `autor` | sim | Qual usuário criou. Em ambiente compartilhado, "quem lançou isso?" é a primeira pergunta. Lançamento que o **ciclo** cria sozinho é assinado pelo dono do ambiente da conta — ver Lançamento que o sistema cria |

`valor` positivo com `sentido` separado não é preciosismo: valor com sinal transforma todo
relatório em `SUM(CASE WHEN ...)` e faz um sinal trocado passar despercebido.

## As duas datas

Registrar um boleto que vence dia 10 e pagá-lo dia 14 são **duas datas**, e tratar como uma
é o bug que faz o saldo mentir o mês inteiro.

- **`dataEvento`** — quando a compra aconteceu. É por ela que o usuário procura: o mercado
  de terça foi gasto de terça. **Não é ela que o relatório de gasto usa por padrão** — ver
  *Em que mês o gasto conta*.
- **`dataEfeito`** — quando o dinheiro sai da conta. É por ela que o saldo se calcula.

**As duas são data pura: dia, sem hora e sem fuso.** A compra das 22h de 25 de fevereiro em
São Paulo é do dia 25 — e não do dia 26, que é onde ela cairia se a data viesse de um instante
convertido para UTC, junto com a fatura errada. O UTC da regra 5 do `CLAUDE.md` continua
valendo para **instante**: criado em, alterado em, hora em que a rotina rodou. Data de domínio
não é instante.

Em quase todo meio as duas são iguais, **crédito incluído**: comprar no cartão cria dívida
na hora, na conta `CARTAO` (`ADR-0003`). O boleto é a exceção que faz os dois campos
existirem — e o `PREVISTO` é a outra. **A regra que calcula `dataEfeito` por tipo de meio vive em
`docs/02-dominio/meio-de-pagamento.md`** — aqui só fica o fato de que os dois campos existem.

## Em que mês o gasto conta

O relatório de gasto por categoria tem **dois eixos de competência**, e é o parcelamento que
os separa. R$ 5.000 em 10x tem `dataEvento` da compra em **todas** as dez parcelas
(`ADR-0006`), então:

| Eixo | Em que mês a parcela conta | O que responde |
|---|---|---|
| **Por fatura** (padrão) | No mês de vencimento da fatura em que ela entrou | "Quanto saiu da minha vida neste mês" |
| **Por compra** | No mês da `dataEvento` — os R$ 5.000 inteiros de uma vez | "Quanto eu me comprometi neste mês" |

**O padrão é por fatura**, porque é o número que bate com o dinheiro que sai e é o único que
sobrevive à comparação mês a mês: uma geladeira em 10x não pode fazer julho parecer o mês em
que a pessoa perdeu o controle. A leitura **por compra** continua existindo porque é a única
que responde ao compromisso assumido, e essa pergunta é real.

Duas coisas que este eixo **não** é:

- **Não é campo, e não muda o modelo.** `dataEvento` continua sendo a data da compra em toda
  parcela. Quem espalha é o relatório, olhando a **fatura** de cada parcela — nunca a data
  delas. Trocar a data das parcelas mentiria sobre quando a compra aconteceu, e ainda quebraria
  a busca do usuário.
- **Não vale só para parcelamento.** Lançamento sem fatura — débito, Pix, dinheiro, boleto —
  conta pela `dataEvento` nos dois eixos, porque não há fatura para discordar dela. O eixo só
  separa alguma coisa no crédito.

Rolagem, pagamento de fatura, transferência, aporte, resgate e rendimento carregam **categoria
de sistema**, que nenhum relatório por categoria inclui — então não entram em nenhum dos dois.

**O relatório é líquido, e quem manda é o sentido da categoria, não o do lançamento.** O
estorno herda a categoria do original e vem com o sentido invertido: ele **abate** o mês em que
aconteceu. Sem isso, quem comprou e devolveu continuaria vendo o gasto cheio para sempre — o
saldo se compensava e o relatório não. *(Achado do protótipo: a compra entrava com R$ 300 e o
estorno não tirava nada.)* A consequência aceita: **uma categoria pode ficar negativa num mês**,
quando o estorno é de uma compra de outro mês. É fiel ao fato, e a tela precisa saber mostrar
número negativo.

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

E a consulta é exatamente **`categoria IS NULL`**, sem nenhuma exceção. Transferência, aporte,
resgate, rendimento, pagamento de fatura, rolagem e abertura não ficam de fora por uma lista:
eles nascem com a **categoria de sistema** deles (`docs/02-dominio/categoria.md`), então nunca
estiveram dentro.

*(Já foi diferente, e a diferença custou um bug. A consulta era "sem categoria **e que espera
uma**", com a lista de exceções escrita à mão, e o protótipo mostrou a abertura de conta na fila
de pendências. A categoria de sistema não corrigiu a lista: apagou a necessidade dela.)*

**Fora do cartão a transição é automática: o lançamento vira `REALIZADO` quando a
`dataEfeito` chega.** Sem confirmação e sem fila. No cartão, quem liquida é o **encerramento
da fatura** — quitada, ou vencida e rolada (`docs/02-dominio/fatura-pagamento.md`).

O preço dessa automação tem nome, e ele é o boleto: o sistema afirma o fato pela **data**, não
por ter observado o dinheiro sair. Boleto esquecido, débito que não passou por falta de saldo,
cobrança que o banco atrasou — nos três o saldo realizado desconta dinheiro que ainda está na
conta. Foi escolhido assim porque uma fila de confirmação cobra atrito em **todo** lançamento
previsto para acertar a minoria que dá errado, e porque a Fase 2 resolve pela raiz: com o
extrato na mão, a conciliação confirma pelo fato (`docs/02-dominio/importacao-conciliacao.md`).

Toda transição automática **grava um evento** (`docs/02-dominio/evento.md`): é onde o usuário
vai ler que o boleto foi realizado pela data, e no dia em que isso aconteceu. Sem esse
registro, o preço acima não tem como ser percebido — e preço invisível é preço que ninguém
aceitou de verdade.

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
- Transferência carrega a **categoria de sistema** "Transferência" e não aparece no relatório
  de gasto: o dinheiro não saiu da vida do usuário, mudou de bolso.

## Lançamento que o sistema cria

Nem todo lançamento sai de alguém digitando. Três nascem do ciclo: os dois lados da
**rolagem**, o **lançamento de abertura** de uma conta e — quando a recorrência entrar — a
**ocorrência do ciclo**.

**Pagamento de fatura não está nessa lista, e é de propósito**: o sistema não sabe de qual
conta nem em que dia você vai pagar, então não inventa o lançamento
(`docs/02-dominio/fatura-pagamento.md`).

| Campo | Valor |
|---|---|
| `autor` | O **dono do ambiente** ao qual a conta pertence, no instante em que o lançamento nasce |
| `ambiente` | O do **dono do objeto**: a conta `CARTAO` na rolagem, a conta no lançamento de abertura. A regra "o ambiente é o de quem lançou" não se aplica quando ninguém lançou |

O `autor` continua **obrigatório** de propósito. Um campo que aceita vazio obriga toda
consulta e toda tela a tratar o vazio, e um bug que esquecesse de preenchê-lo passaria
despercebido como "foi o sistema".

**O preço, escrito para não ser esquecido:** o histórico afirma que o dono lançou algo que
ele não lançou — e responder "quem lançou isso?" é a razão de o campo existir. Na prática a
descrição desfaz a confusão: ninguém lê "Saldo da fatura anterior" e pensa numa pessoa.
**Revisitar se** alguém, num ambiente compartilhado, discutir a autoria de um lançamento que
o ciclo criou. Aí o vazio — ou um campo `origem` ao lado — passa a valer o custo.

## Correção, estorno e exclusão

**São três, e a mesma palavra esconde as três:**

| Situação | O que houve | O que o sistema faz |
|---|---|---|
| **Correção** | O registro está errado, mas descreve algo que aconteceu: valor digitado errado, categoria errada, conta errada | **Edita o lançamento**, e o que mudou vira evento |
| **Estorno** | O dinheiro voltou de verdade: compra cancelada, devolução, chargeback | **Cria um lançamento novo** de sentido oposto, ligado ao original por `estornoDe` |
| **Exclusão** | O lançamento **nunca correspondeu a nada**: a duplicata, o valor inventado, a linha lançada por engano | **Remove o lançamento**, e a remoção vira evento |

A exclusão existe porque o lançamento é feito por gente. Estornar uma duplicata inventaria
**dois** fatos falsos no lugar de um, e deixaria os dois no extrato para sempre.

Isso não contradiz *o sistema não reescreve o passado*, que é o princípio que matou o mover
(`docs/02-dominio/categoria.md`). A diferença cabe numa frase:

> **O sistema não reescreve o passado; o usuário corrige o que ele mesmo escreveu errado.**

O mover reescrevia março **sem que nada tivesse mudado** — o gasto aconteceu e continuou
acontecendo, e só a resposta do relatório mudava. A exclusão reescreve março porque **março
estava errado**. E ela nunca é silenciosa: é sempre ato do usuário, mostra o impacto numérico
antes de confirmar (`docs/06-interface/navegacao.md`) e grava `LANCAMENTO_EXCLUIDO`
(`docs/02-dominio/evento.md`).

### O que se exclui: o que o usuário lançou

A fronteira é uma só, e é o espelho da regra já escrita para conta e categoria inativas —
*recusa o do usuário, o ciclo continua*:

> **O que o usuário lançou, o usuário exclui. O que o ciclo criou não é dele para excluir.**

| Lançamento | Exclui? |
|---|---|
| Gasto, receita, transferência, aporte, resgate, estorno | **Sim.** Qualquer data, qualquer fatura, aberta ou não |
| Um lado de uma transferência | Sim, mas age no **par inteiro**. Não existe metade de transferência |
| **Parcela isolada** de um parcelamento | **Não.** Quebraria a soma das parcelas, e o usuário já não edita parcela sozinha. Quem se arrepende exclui o parcelamento (`docs/02-dominio/recorrencia.md`) |
| Lançamento que **tem estorno** apontando para ele | **Não**, enquanto o estorno existir: ele ficaria órfão. Exclui-se o estorno primeiro, se ele também for engano |
| **Par de rolagem** e **lançamento de abertura** | **Não.** São do ciclo. O saldo de abertura se corrige editando o valor |

**Pagamento de fatura é do usuário**, inclusive o agendado: ele o cria, o edita e o exclui
como qualquer outro lançamento (`docs/02-dominio/fatura-pagamento.md`). O sistema não cria e
não apaga nenhum.

**Nada é recalculado**, porque nada é armazenado: saldo, total de fatura, dívida e patrimônio
são todos soma de lançamento (`docs/02-dominio/conta.md`), e tirar a linha já refaz tudo que
deriva dela. **E fatura nenhuma trava a exclusão** — nem `FECHADA`, nem paga, nem de cinco
meses atrás. O que muda conforme a fatura é o destino da **diferença**, e a regra é uma só
para excluir e editar (`docs/02-dominio/fatura-pagamento.md`, *Corrigir o passado*), porque
**excluir é o caso extremo de editar**.

No crédito, o estorno de uma compra parcelada credita o **valor total** de uma vez, e as
parcelas restantes seguem correndo — os dois se compensam **no saldo e no relatório**, este
último porque o gasto por categoria é líquido (ver *Em que mês o gasto conta*). O parcelamento não é editado
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

**Quem guarda é o evento** (`docs/02-dominio/evento.md`), e quem mostra é o **detalhe do
lançamento** (`docs/06-interface/extrato.md`): o histórico não é uma segunda estrutura, é a
mesma lida por alvo.

**Quem manda na conta é o meio**, na correção como no nascimento. O meio já aponta para uma
conta, e trocar de meio leva o lançamento junto: o gasto que foi pago pelo Pix do Itaú é gasto
do Itaú, e não há como ser das duas coisas. Conta e meio divergentes são **recusados** em vez
de gravados — um lançamento na conta A pago por um meio da conta B não descreve nada que possa
ter acontecido. É por isso que "trocar de banco" e "trocar de meio" são **um campo só** na
tela.

Transferência não tem meio, e nela a conta de cada lado não se corrige: **editar um lado age no
par inteiro**, e mudar a conta de um lado sozinho quebraria a soma zero. Quem errou a conta de
uma transferência exclui o par e transfere de novo.

**A `dataEfeito` não é campo livre: ela é consequência do meio**
(`docs/02-dominio/meio-de-pagamento.md`), e a correção a recalcula. Em meio à vista as duas
datas são a mesma, então corrigir a `dataEvento` move as duas — e trocar um boleto por um meio
à vista junta as duas no dia do evento. Guardar a data de vencimento de um boleto que virou Pix
seria guardar a memória de uma regra que não vale mais. Na transferência vale o mesmo por outro
motivo: mover dinheiro entre contas não tem vencimento, e a `dataEvento` move as duas datas dos
dois lados.

**Do lançamento que o ciclo criou, só o `valor` é do usuário.** É o que torna verdadeira a
frase que a tabela de exclusão já dizia — *o saldo de abertura se corrige editando o valor* —,
e a recusa vale para todo o resto: conta, meio, categoria, sentido, as duas datas, descrição e
situação. Eles descrevem a abertura da conta, não uma escolha de ninguém. **Corrigir o valor é
permitido; excluir continua não sendo**, e as duas coisas não se contradizem: a linha descreve
um fato que aconteceu, e é só o número que estava errado.

**A situação é do usuário e o sistema não a re-deriva.** Ele decide se o boleto adiado volta a
`PREVISTO` ou continua `REALIZADO` — mexer nisso por conta própria seria extrapolar valor
informado, que é o que a regra 7 do `CLAUDE.md` proíbe. Quem move a situação sozinho é só a
rotina, e só para frente.

**Nenhum estado de fatura trava a edição.** Lançamento de fatura fechada se edita como
qualquer outro, e o campo `fatura` aponta para qualquer fatura do cartão, aberta ou não.
Fatura fechada não congela nada — o sistema não tem a palavra final sobre o dinheiro do
usuário; o que ele deve é mostrar a consequência antes e guardar quem mudou o quê.

O que muda conforme a fatura é o **aviso**: corrigir lançamento de fatura já paga faz o
sistema nomear o pagamento e a diferença antes de aplicar. **O pagamento não é tocado** — a
diferença vira lançamento, e as regras estão em `docs/02-dominio/fatura-pagamento.md`. Aqui
vale o princípio: **ação retroativa mostra o impacto antes de confirmar e vai para o
evento**, sem lançamento de ajuste no extrato.
Nada precisa ser recalculado: como saldo é sempre a soma dos lançamentos
(`docs/02-dominio/conta.md`), reescrever o valor já refaz tudo que deriva dele.

## Invariantes

- `valor` é sempre positivo. Zero não é lançamento.
- Estorno aponta para o lançamento que estorna (`estornoDe`) e nunca o apaga.
- `parcelamento` e `recorrencia` nunca aparecem preenchidos no mesmo lançamento.
- Lançamento de abertura de conta e lançamento de rendimento nascem `REALIZADO`.
- Do lançamento que o ciclo criou, a correção aceita **só o `valor`**; todo outro campo é
  recusado, e ele continua não se excluindo.
- Todo lançamento tem exatamente um ambiente e uma conta.
- Todo lançamento tem `autor`, inclusive os que o ciclo cria sozinho — nesses, o dono do
  ambiente da conta.
- Um lançamento **nunca muda de ambiente** — nem por edição, nem por correção. O certo é
  **excluir** em um e criar no outro: ninguém devolveu dinheiro, o registro é que nasceu no
  lugar errado. Estornar inventaria um fato.
- `dataEfeito` nunca é anterior à `dataEvento`.
- `transferenciaId`, quando existe, aparece em exatamente dois lançamentos.
- `rolagemDeFatura`, quando existe, aparece em exatamente dois — na mesma conta, somando zero.
- Nenhum estado de fatura impede a edição de um lançamento (`docs/02-dominio/fatura-cartao.md`).
- A situação, **quando a automação a move**, só anda para frente:
  `PREVISTO → PROVISIONADO → REALIZADO`, nunca ao contrário. Só a **correção** do usuário
  volta, e sempre com histórico.
- Lançamento de conta inativa não é criado (`docs/02-dominio/conta.md`).
- Categoria inativa não aparece no seletor, mas continua recebendo o que o ciclo cria —
  a parcela que ainda vai nascer e a ocorrência de recorrência nascem na categoria
  original (`docs/02-dominio/categoria.md`). E o seletor de um lançamento que **já
  aponta** para uma categoria inativa continua exibindo essa categoria, selecionada.
- A **categoria** é sempre do mesmo ambiente do lançamento.
- Todo lançamento que o ciclo cria nasce com a **categoria de sistema** da operação dele — e
  por isso nenhum deles aparece na fila de pendências (`docs/02-dominio/categoria.md`).
- Nenhum lançamento do usuário aponta para uma categoria de sistema.
- **Lançamento do usuário se exclui; lançamento que o ciclo criou, não.** O que o ciclo
  criou e perdeu o motivo é **descartado pelo próprio ciclo**.
- Excluir um lado de uma transferência exclui o **par inteiro**.
- Parcela isolada não se exclui: exclui-se o parcelamento.
- Lançamento com `estornoDe` apontando para ele não se exclui enquanto o estorno existir.
- Toda exclusão é ato do usuário e grava `LANCAMENTO_EXCLUIDO`
  (`docs/02-dominio/evento.md`). O sistema nunca exclui lançamento por prazo ou critério
  próprio.
- A **conta** é do mesmo ambiente, ou de um que a compartilhou com ele.

## O que ainda não existe

| O que falta | Consequência hoje |
|---|---|
| **A transição automática pela data** | A `situacao` é decidida no nascimento (`dataEfeito` futura nasce `PREVISTO`) e **só a correção do usuário a move**. Um boleto previsto continua previsto depois do vencimento, e o saldo realizado não desconta o que já saiu |
| **`evento`** | Nada é gravado: nem `LANCAMENTO_EXCLUIDO`, nem o de/para de uma correção. O *histórico de alteração* que este doc entrega pelo evento não existe, e o preço da automação acima fica invisível quando ela chegar |
| **`recorrencia`** | O campo não existe nem na tabela, e é Fase 2. **`fatura` e `parcelamento` existem desde o cartão**, e com eles o `PROVISIONADO` e o eixo *por compra* do relatório |
| **O eixo *por compra* na tela** | O modelo já o sustenta — a `dataEvento` de toda parcela é a da compra —, mas nenhum relatório oferece a troca de eixo ainda (`docs/06-interface/dashboard.md`) |
| **Estorno de lançamento do ciclo** | Não é recusado explicitamente; não há caso que o produza enquanto a rolagem não existir |
| **A rolagem dentro da regra do ciclo** | *Só o `valor` é corrigível* vale hoje para o único lançamento do ciclo que existe, a **abertura**. A rolagem é um **par que soma zero**, e corrigir o valor de um lado sozinho o quebraria — quando ela nascer, entra com guarda própria, como a transferência já tem |
| **Tirar a categoria de um lançamento já categorizado** | A correção lê campo ausente e campo nulo como a mesma coisa — *não mude* —, então categoria preenchida não volta a vazia. A tela não oferece a opção, em vez de oferecer e não fazer nada. Um lançamento categorizado por engano se corrige **para outra categoria** |

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Como `dataEfeito` é calculada por tipo de meio | `02-dominio/meio-de-pagamento` |
| Como as parcelas nascem e o que acontece ao editar a compra | `02-dominio/recorrencia` |
| Ciclo da fatura: fechamento, abertura e a que fatura o lançamento vai | `02-dominio/fatura-cartao` |
| Pagamento de fatura, rolagem e correção do passado | `02-dominio/fatura-pagamento` |
| Como um lançamento capturado vira realizado sem duplicar | `02-dominio/importacao-conciliacao` |
| Como a categoria é atribuída automaticamente | `02-dominio/regras-categorizacao` |
