---
id: 02-dominio/fatura-pagamento
titulo: Pagamento de fatura
dono: como a fatura e paga, a rolagem do que venceu sem ser pago e a correcao de fatura ja paga
ler-junto: [02-dominio/fatura-cartao, 02-dominio/lancamento, 02-dominio/compartilhamento]
status: ativo
---

# Pagamento de fatura

Separado de `docs/02-dominio/fatura-cartao.md` quando aquele doc passou das 300 linhas do
`CONVENTIONS`. Lá fica o **ciclo** da fatura; aqui fica o **dinheiro**: como se paga, o que
acontece com o que não foi pago, e o que muda ao corrigir uma fatura já paga.

O ponto de partida está no `ADR-0003`: o contrato de cartão é uma conta, então pagar a
fatura é mover dinheiro de uma conta para outra — e nada além disso.

## Como se paga

**Pagar a fatura é uma transferência** da conta pagadora para a conta `CARTAO`
(`docs/02-dominio/lancamento.md`): `SAIDA` na conta que paga, `ENTRADA` na do cartão, mesmo
`transferenciaId`. A dívida cai porque o saldo da conta `CARTAO` é a soma dos lançamentos
dela.

Não há duplo cômputo, e não é preciso nenhuma regra dizendo que não há: **transferência não
tem categoria e não entra no relatório de gasto**, igual a aporte e resgate. O gasto foi
contado uma vez, na compra.

**Cada fatura é paga na tela dela.** O pagamento aponta para uma fatura, não para o cartão:
é assim que se sabe qual ciclo foi quitado e qual não. O campo é `pagamentoDeFatura`, distinto
do `fatura` do lançamento de crédito: o pagamento **quita** a fatura, não **entra** nela — e
por isso não conta no total dela (`docs/02-dominio/lancamento.md`).

**E o pagamento é sempre seu.** O sistema não cria nenhum — nem no fechamento, nem previsto.
Você abre a fatura e diz o que vai fazer: qual conta paga, em que dia, e quanto. Marcando um
dia à frente, o lançamento nasce `PREVISTO` e **realiza pela data**, como um boleto
registrado; marcando hoje, nasce `REALIZADO`. É declaração sua, não palpite do sistema — e
por isso não precisa de confirmação depois.

> **Exemplo literal.** Fatura de R$ 1.000 fecha em 10/10 e vence em 20/10. O sistema não faz
> nada. Você agenda R$ 900 pelo Nubank em 19/10: nasce `PREVISTO`, aparece no extrato do
> Nubank e entra em "quanto sobra até o fim do mês". Dia 19 ele realiza. Dia 21 a rotina acha
> R$ 100 a pagar e rola para a fatura seguinte.

**Quem liquida é o encerramento da fatura**, e ela encerra de dois jeitos: **quitada** — a
soma dos pagamentos cobre o total — ou **vencida sem ser quitada**, e aí o que faltou rola.
Nos dois casos os lançamentos dela saem de `PROVISIONADO` e viram `REALIZADO` (`ADR-0006`).

**Pagamento parcial, sozinho, não liquida nada.** Enquanto a fatura está viva, o que foi
comprado continua `PROVISIONADO` — que é exatamente o que "aconteceu, falta liquidar" quer
dizer. Chamar de liquidado o que ainda se deve é a confusão que o `ADR-0006` desfez.

| Caso | O que acontece |
|---|---|
| Pagar tudo | Uma transferência no valor do `a pagar`. Se você tinha agendado, é o agendamento que realiza |
| **Pagar menos** | O valor do pagamento é o que foi pago. A fatura fica **parcial** e **nada é liquidado**; se ela vencer assim, o que sobrou rola e ela encerra — ver Encerramento |
| **Pagar de outra conta** | Troca-se a conta de origem do pagamento. Qualquer conta que o ambiente acesse serve |
| **Pagar em dois ou mais pedaços** | Duas transferências para a mesma fatura. A soma quita (`docs/02-dominio/compartilhamento.md`) |
| Pagar mais que a fatura | A conta `CARTAO` fica com saldo a favor. É crédito no cartão, e existe na vida real |

### Qual fatura recebe pagamento

**Só a `FECHADA` que ainda tem `a pagar`.** É a mesma janela em que se pode **abrir** a fatura
(`docs/02-dominio/fatura-cartao.md`) — uma janela, duas operações, e o código pergunta uma
coisa só. As três exclusões saem todas da mesma razão, e nenhuma é proibição inventada:

| Fatura | Por que não |
|---|---|
| `FUTURA` | Existe só para segurar parcela de mês que ainda não chegou. O emissor nem a emitiu: não há o que quitar |
| `ABERTA` | O ciclo ainda está correndo e o valor ainda vai mudar. Pagar antes do fechamento é **antecipar**, outra mecânica, com desconto do emissor — Fase 2 |
| Encerrada | O `a pagar` dela já é zero, por construção — ver abaixo |

O preço de não escrever isso seria alto e silencioso: a quitação **encerra a fatura**, então
pagar uma `FUTURA` faria os lançamentos dela virarem `REALIZADO` meses antes de o ciclo
existir, e quitar a `ABERTA` a deixaria encerrada enquanto ainda recebe compra — duas
situações diferentes convivendo numa fatura que, por regra, só tem uma
(`docs/02-dominio/fatura-cartao.md`).

**Fatura encerrada não recebe pagamento.** Depois que ela rolou, o `a pagar` dela é zero: a
dívida inteira está na fatura seguinte, e é lá que se paga. Não é proibição inventada, é
consequência da fórmula (`docs/02-dominio/fatura-cartao.md`) — pagar a fatura velha
descontaria a mesma dívida duas vezes.

## Encerramento: o que venceu sem ser pago

**Fatura que vence sem ser quitada rola o que sobrou para a fatura `ABERTA`** (`ADR-0005`).
São **dois lançamentos dentro da própria conta `CARTAO`**, com o mesmo `rolagemDeFatura`:

| Lado | Onde | Sentido | O que o usuário lê |
|---|---|---|---|
| Crédito | na fatura que venceu | `ENTRADA` | "Rolado para a fatura seguinte" |
| Débito | na fatura aberta | `SAIDA` | "Saldo da fatura anterior" — **a primeira linha dela** |

Os dois **somam zero**: a dívida do cartão não muda. A rolagem move dívida de período, não
cria dívida. É a mesma forma da transferência — par ligado, com categoria
de sistema, fora do relatório de gasto — aplicada entre faturas em vez de entre contas.

### O que o encerramento faz, em ordem

1. **Rola o que faltava:** crédito na fatura que venceu, débito na `ABERTA`.
2. **O débito nasce `PROVISIONADO`.** Aquela dívida aconteceu — são compras que não foram
   pagas — e é ela que ainda espera liquidação. `PREVISTO` foi cogitado e cai na aritmética:
   o crédito entraria no saldo e o débito não, o par que existe para somar zero **apagaria a
   dívida**, e o limite voltaria inteiro sem ninguém ter pago nada.
3. **Todos os lançamentos da fatura vencida viram `REALIZADO`**, o crédito de rolagem
   inclusive. Ela acabou: parte paga, parte rolada, nada mais a cobrar nela.
4. **O que você tiver agendado não é tocado.** O sistema não apaga lançamento do usuário. Um
   pagamento marcado para depois do vencimento realiza no dia que você escolheu e entra na
   conta `CARTAO` do mesmo jeito — a dívida já está na fatura seguinte, e o dinheiro a abate
   lá. O `a pagar` da fatura encerrada fica negativo, que é crédito, e isso já é caso
   conhecido (ver *Corrigir o passado*).

Fatura quitada no prazo não passa por nada disso: o pagamento cobriu o total, os lançamentos
dela viram `REALIZADO` na hora e não há o que rolar.

### Quando o encerramento roda

**No dia seguinte ao vencimento.** O usuário tem o dia inteiro do vencimento para pagar, e a
rotina só encontra a fatura em aberto no dia seguinte. Os dois lançamentos do par ficam
datados **no vencimento**, não no dia em que a rotina rodou — a fatura nova mostra o saldo
anterior com a data que o banco usaria.

**É idempotente e recupera atraso, como o fechamento** — e recupera **dia a dia, em ordem
cronológica**, nunca "fecha tudo e depois rola tudo". A ordem cronológica também resolve o dia
em que os dois passos caem juntos: **encerrar vem antes de fechar**, porque o vencimento que
disparou o encerramento é anterior à `dataFechamento` que disparou o fechamento. Rolar depois
mandaria a dívida para a fatura errada — a que acabou de abrir, e não a que estava aberta
quando aquela fatura venceu. Se o Raspberry Pi ficou dois ciclos
desligado, janeiro rola para a fatura que estava aberta em janeiro; só então fevereiro fecha,
vence e rola. Rolar duas vezes a mesma fatura não faz nada.

**O gatilho é o `a pagar`, não um carimbo de "já encerrei esta".** A rotina procura fatura
vencida com `a pagar` maior que zero — e é isso que a faz funcionar sozinha no caso difícil:
uma fatura já encerrada cuja correção fez o total **subir** volta a ter `a pagar` positivo, e
a passagem seguinte rola o que ela voltou a dever (ver *Corrigir o passado*). Não existe
"encerrada" como estado que impeça isso; existe o número.

**A primeira rolagem de uma fatura é datada no vencimento dela.** Uma rolagem **posterior,
disparada por correção, é datada no dia em que a rotina rodou**: o vencimento antigo está num
mês que já foi vivido, e pôr um lançamento lá seria o sistema reescrevendo o passado. O fato é
de hoje.

Daí sai a frase curta: **`PROVISIONADO` dura da compra até o fim da fatura dela, sempre.**

**O total histórico da fatura não cai.** Agosto continua tendo sido R$ 1.610,60: o crédito
de rolagem fica fora do total dela, e o débito entra no total da seguinte. É exatamente o
"saldo anterior" da fatura de papel.

> **Exemplo literal.** Fatura de agosto: R$ 1.610,60, paga R$ 800 no vencimento. No dia
> seguinte ao vencimento, R$ 810,60 rolam. Agosto fica *parcial · rolada*, com total
> histórico de R$ 1.610,60 e nada a pagar. Setembro, que tinha R$ 1.100,10 de compras,
> fecha em **R$ 1.910,70**. A dívida do cartão não mudou em nenhum momento.

Juros e IOF do rotativo entram depois, como **lançamentos comuns** da fatura seguinte,
quando o banco cobrar. O app não calcula nem antecipa.

### O sistema não calcula mínimo nem juros

**Não existe percentual mínimo definido por norma.** Os ~15% que todo mundo repete são
prática de mercado, e cada emissor monta a própria fórmula — o Nubank, por exemplo, cobra
15% das compras do mês, 15% do saldo de faturas anteriores, **100%** dos encargos (juros,
IOF, multa, saque) e as parcelas em curso. Calcular isso aqui seria errar em quase todo
cartão real.

Então o Cyberbank faz o que já faz com o estorno parcelado: **não decide, observa.** Juros,
IOF e multa entram como **lançamentos comuns** na fatura seguinte, quando aparecerem, com
categoria própria. O app nunca inventa uma taxa.

Duas regras de mercado que valem como contexto, não como cálculo: o rotativo dura no máximo
até o vencimento da fatura seguinte, quando o saldo tem que ser quitado ou parcelado
(Resolução CMN 4.549/2017); e juros mais encargos não podem passar de 100% da dívida
original (Lei 14.690/2023).

## Corrigir o passado

**O sistema não congela nada.** Fatura fechada não trava seus lançamentos, e nenhuma
correção exige abrir fatura: **edita-se ou exclui-se o lançamento**
(`docs/02-dominio/lancamento.md`), escolhendo inclusive **em que fatura ele fica**, com ela
aberta ou não. O sistema não tem a palavra final sobre o dinheiro do usuário — o que ele deve
é mostrar a consequência antes de aplicar e guardar quem mudou o quê.

E há **uma regra só** para o destino da diferença, valendo igual para edição e exclusão:

> **O sistema nunca reescreve um pagamento. Toda diferença que uma correção produza vira
> lançamento.**

O pagamento é fato do usuário: R$ 1.000 saíram do banco no dia 5. Reescrever esse valor para
fazer a conta fechar faz o extrato do app discordar do extrato do banco — e *"saldo batendo
com o extrato do banco"* é critério de pronto da Fase 1 (`docs/00-produto/roadmap.md`). **O
app não conserta a própria conta mexendo no que o banco fez.**

As três saídas cobrem todo caso:

| O que a correção fez | O que acontece |
|---|---|
| Mexeu em fatura `ABERTA`, ou `FECHADA` não paga | O total é reapurado, e nada mais. Não há pagamento que acompanhe: o que existir ali foi você que criou |
| **Subiu o total** de uma fatura já encerrada | O `a pagar` dela volta a ser positivo, e o **encerramento a pega na passagem seguinte e rola** o que ela voltou a dever. A dívida aparece na fatura aberta, como qualquer saldo anterior |
| **Baixou o total** de uma fatura já encerrada | O `a pagar` fica negativo: é **crédito** na conta `CARTAO` — a mesma coisa de pagar mais que a fatura, que já existe na vida real. Nada rola e nada é reescrito |

E quando **o pagamento é que estava errado** — o banco cobrou R$ 1.010 e o registro diz
R$ 1.000 — o caminho é o direto, e ele já é permitido: **o usuário edita o pagamento**, valor
e data, inclusive para trás, em qualquer fatura, aberta ou não
(`docs/02-dominio/lancamento.md`). Não é preciso reabrir nada.

*(Já foi diferente, e a diferença é a razão desta regra existir. O sistema oferecia duas
respostas na fatura paga — "ajustar o pagamento" ou "deixar como saldo" — e a primeira
reescrevia o valor do pagamento na data original. Ela caiu por mentir exatamente no caso mais
comum, a duplicata: a compra some, o pagamento encolhe junto, e o banco continua tendo
debitado o valor cheio. **Reabrir a fatura e refazer os pagamentos** foi cogitado no lugar
dela e caiu por dois motivos mecânicos: quebra a invariante da `ABERTA` única — com quatro
faturas abertas a regra *"a fatura vem do status"* deixa de devolver resposta — e não sobrevive
à rotina, que roda todo dia e rolaria as quatro sozinha.)*

**Editar uma série pode mudar o valor de várias faturas de uma vez** — inclusive pagas. O
sistema **mostra quais** antes de confirmar (`docs/02-dominio/recorrencia.md`): mexer no
passado é permitido, mas nunca silencioso.

Nada disso dispara recálculo: saldo é sempre soma de lançamento, então reescrever ou remover
um valor **já refaz tudo que deriva dele**. A memória do que mudou vive no **evento**
(`docs/02-dominio/evento.md`), não numa linha de ajuste no extrato.

## Invariantes

- Pagamento de fatura é **sempre** uma transferência, nunca um lançamento solto.
- Pagamento aponta **só para fatura `FECHADA` com `a pagar` maior que zero** — nunca para uma
  `FUTURA`, nunca para a `ABERTA`, nunca para uma encerrada. É a mesma janela em que a fatura
  pode ser aberta (`docs/02-dominio/fatura-cartao.md`).
- Todo pagamento aponta para **uma** fatura: é assim que se sabe qual ciclo foi quitado.
- A rolagem tem **sempre dois lados**, na mesma conta `CARTAO`, e a soma deles é zero.
- A fatura que rola e a que recebe são **sempre duas**, nunca a mesma. Quem garante é o
  `docs/02-dominio/fatura-cartao.md`: fatura encerrada não abre, então a que venceu nunca
  chega a ser a `ABERTA`.
- Rolagem nunca entra em relatório de gasto nem na fila de pendências.
- O encerramento roda **no dia seguinte ao vencimento**, é idempotente e recupera atraso em
  ordem cronológica.
- Fatura encerrada **não recebe pagamento**: o `a pagar` dela é zero por construção. Quando
  uma correção o torna positivo de novo, quem o devolve a zero é a **rolagem**, nunca um
  pagamento novo naquela fatura.
- **Nenhum pagamento é reescrito pelo sistema.** Diferença de correção vira lançamento:
  rolagem se a fatura voltou a dever, crédito na conta `CARTAO` se sobrou.
- O total histórico de uma fatura **não cai** quando ela rola.
- O **débito** de rolagem nasce `PROVISIONADO`; o crédito e os demais lançamentos da fatura
  encerrada viram `REALIZADO`.
- **O sistema nunca cria nem apaga pagamento de fatura.** Quem cria, agenda e cancela é o
  usuário — inclusive o previsto, que é declaração dele e realiza pela data.
- Nenhum valor de pagamento informado pelo usuário é reescrito sem ele mandar.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Ciclo, estados, fechamento e abertura da fatura | `02-dominio/fatura-cartao` |
| Por que o contrato de cartão é uma conta | `ADR-0003` |
| Por que a rolagem existe e o que ela substituiu | `ADR-0005` |
| Transferência, histórico de edição e correção × estorno | `02-dominio/lancamento` |
| Partes da fatura num cartão compartilhado | `02-dominio/compartilhamento` |
