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
é assim que se sabe qual ciclo foi quitado e qual não.

**Quem liquida é o encerramento da fatura**, e ela encerra de dois jeitos: **quitada** — a
soma dos pagamentos cobre o total — ou **vencida sem ser quitada**, e aí o que faltou rola.
Nos dois casos os lançamentos dela saem de `PROVISIONADO` e viram `REALIZADO` (`ADR-0006`).

**Pagamento parcial, sozinho, não liquida nada.** Enquanto a fatura está viva, o que foi
comprado continua `PROVISIONADO` — que é exatamente o que "aconteceu, falta liquidar" quer
dizer. Chamar de liquidado o que ainda se deve é a confusão que o `ADR-0006` desfez.

| Caso | O que acontece |
|---|---|
| Pagar tudo | O pagamento previsto vira `REALIZADO`, com a data e a conta reais |
| **Pagar menos** | O valor do pagamento é o que foi pago. A fatura fica **parcial** e **nada é liquidado**; se ela vencer assim, o que sobrou rola e ela encerra — ver Encerramento |
| **Pagar de outra conta** | Troca-se a conta de origem do pagamento. Qualquer conta que o ambiente acesse serve |
| **Pagar em dois ou mais pedaços** | Duas transferências para a mesma fatura. A soma quita (`docs/02-dominio/compartilhamento.md`) |
| Pagar mais que a fatura | A conta `CARTAO` fica com saldo a favor. É crédito no cartão, e existe na vida real |

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
cria dívida. É a mesma forma da transferência — par ligado, sem categoria, fora do relatório
de gasto — aplicada entre faturas em vez de entre contas.

### O que o encerramento faz, em ordem

1. **Rola o que faltava:** crédito na fatura que venceu, débito na `ABERTA`.
2. **O débito nasce `PROVISIONADO`.** Aquela dívida aconteceu — são compras que não foram
   pagas — e é ela que ainda espera liquidação. `PREVISTO` foi cogitado e cai na aritmética:
   o crédito entraria no saldo e o débito não, o par que existe para somar zero **apagaria a
   dívida**, e o limite voltaria inteiro sem ninguém ter pago nada.
3. **Todos os lançamentos da fatura vencida viram `REALIZADO`**, o crédito de rolagem
   inclusive. Ela acabou: parte paga, parte rolada, nada mais a cobrar nela.
4. **O pagamento previsto que ela ainda tinha é descartado.** Um previsto datado num
   vencimento que já passou era metade do furo que o `ADR-0005` veio tapar.

Fatura quitada no prazo não passa por nada disso: o pagamento cobriu o total, os lançamentos
dela viram `REALIZADO` na hora e não há o que rolar.

### Quando o encerramento roda

**No dia seguinte ao vencimento.** O usuário tem o dia inteiro do vencimento para pagar, e a
rotina só encontra a fatura em aberto no dia seguinte. Os dois lançamentos do par ficam
datados **no vencimento**, não no dia em que a rotina rodou — a fatura nova mostra o saldo
anterior com a data que o banco usaria.

**É idempotente e recupera atraso, como o fechamento** — e recupera **dia a dia, em ordem
cronológica**, nunca "fecha tudo e depois rola tudo". Se o Raspberry Pi ficou dois ciclos
desligado, janeiro rola para a fatura que estava aberta em janeiro; só então fevereiro fecha,
vence e rola. Rolar duas vezes a mesma fatura não faz nada.

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
correção exige abrir fatura: edita-se o lançamento, escolhendo inclusive **em que fatura ele
fica**, com ela aberta ou não. O sistema não tem a palavra final sobre o dinheiro do
usuário — o que ele deve é mostrar a consequência antes de aplicar e guardar quem mudou o
quê.

| Fatura do lançamento | O que acontece |
|---|---|
| `ABERTA`, ou `FECHADA` não paga | O total é reapurado, e o pagamento previsto acompanha. Nada além disso |
| **Já paga** | O sistema **avisa antes**, nomeando o pagamento, a data e a diferença, e **pergunta** o que fazer com ela |

A pergunta na fatura paga tem duas respostas legítimas, e é por isso que ela existe — a
mesma razão da pergunta ao editar recorrência:

| Resposta | Quando é a certa |
|---|---|
| **Ajustar o pagamento** | O banco cobrou o valor novo e o registro é que estava errado. O pagamento passa a valer o valor novo, na data original |
| **Deixar como saldo** | O pagamento foi o que foi, e a diferença é dívida (ou crédito) que continua na conta `CARTAO` |

Nada disso dispara recálculo: saldo é sempre soma de lançamento, então reescrever um valor
**já refaz tudo que deriva dele**. A memória do que mudou vive no histórico de alteração
(`docs/02-dominio/lancamento.md`), não numa linha de ajuste no extrato.

**Editar uma série pode mudar o valor de várias faturas de uma vez** — inclusive pagas. O
sistema **mostra quais** antes de confirmar (`docs/02-dominio/recorrencia.md`): mexer no
passado é permitido, mas nunca silencioso.

## Invariantes

- Pagamento de fatura é **sempre** uma transferência, nunca um lançamento solto.
- Todo pagamento aponta para **uma** fatura: é assim que se sabe qual ciclo foi quitado.
- A rolagem tem **sempre dois lados**, na mesma conta `CARTAO`, e a soma deles é zero.
- Rolagem nunca entra em relatório de gasto nem na fila de pendências.
- O encerramento roda **no dia seguinte ao vencimento**, é idempotente e recupera atraso em
  ordem cronológica.
- Fatura encerrada **não recebe pagamento**: o `a pagar` dela é zero por construção.
- O total histórico de uma fatura **não cai** quando ela rola.
- O **débito** de rolagem nasce `PROVISIONADO`; o crédito e os demais lançamentos da fatura
  encerrada viram `REALIZADO`.
- Fatura encerrada **não deixa pagamento previsto** datado no passado.
- Nenhum valor de pagamento informado pelo usuário é reescrito sem ele mandar.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Ciclo, estados, fechamento e abertura da fatura | `02-dominio/fatura-cartao` |
| Por que o contrato de cartão é uma conta | `ADR-0003` |
| Por que a rolagem existe e o que ela substituiu | `ADR-0005` |
| Transferência, histórico de edição e correção × estorno | `02-dominio/lancamento` |
| Partes da fatura num cartão compartilhado | `02-dominio/compartilhamento` |
