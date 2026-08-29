---
id: 01-arquitetura/decisoes/ADR-0005-rolagem-entre-faturas
titulo: "ADR-0005: o que vence sem ser pago rola para a fatura seguinte"
dono: como o saldo nao pago de uma fatura chega na fatura seguinte
ler-junto: [01-arquitetura/decisoes/ADR-0003-cartao-de-credito-e-conta, 02-dominio/fatura-cartao]
status: ativo
---

# ADR-0005: o que vence sem ser pago rola para a fatura seguinte

- **Status:** aceita. **Substitui uma consequência do `ADR-0003`** — a que dizia "o que não
  foi pago fica como saldo da conta `CARTAO`, não há par de rolagem". O resto do `ADR-0003`
  continua valendo por inteiro
- **Data:** 2026-08-28
- **Afeta:** `02-dominio/fatura-cartao`, `02-dominio/lancamento`, protótipo

## Contexto

O `ADR-0003` deixou a dívida viver no saldo da conta `CARTAO`, e concluiu que o pagamento
parcial não precisava de nada: o que não foi pago simplesmente ficava no saldo.

O protótipo mostrou o furo. Pagando R$ 800 de uma fatura de R$ 1.610,60 e avançando o
relógio, os R$ 810,60 restantes ficavam presos **numa fatura já vencida**, com um pagamento
previsto datado no passado. A fatura seguinte exibia só o próprio período — o usuário lia
R$ 1.100,10 numa fatura em que o banco ia cobrar R$ 1.910,70.

O erro de fundo era tratar o pagamento como sendo *da fatura* enquanto a dívida era *da
conta*. Cada fatura é paga na tela dela; então cada fatura precisa conter tudo o que ela
cobra.

## Decisão

**Fatura que vence sem ser quitada rola o que sobrou para a fatura aberta**, como um **par
de lançamentos dentro da própria conta `CARTAO`**:

| Lado | Onde | Sentido | Descrição |
|---|---|---|---|
| Crédito | na fatura que venceu | `ENTRADA` | "Rolado para a fatura seguinte" |
| Débito | na fatura aberta | `SAIDA` | "Saldo da fatura anterior" |

Os dois têm o mesmo `rolagemDeFatura`, a data do vencimento que passou, e **somam zero**:
a dívida do cartão não muda. A rolagem move dívida de período, não cria dívida.

**A rolagem é o encerramento da fatura**, e faz mais do que o par: o débito nasce
`PROVISIONADO`, os lançamentos da fatura vencida viram `REALIZADO` e o pagamento previsto
dela que não foi pago é descartado. Débito `PREVISTO` foi cogitado e derrubado por
aritmética — ele não entraria no saldo, o crédito entraria, e o par que existe justamente
para somar zero apagaria a dívida.

É a mesma forma da transferência — par ligado, sem categoria, fora do relatório de gasto —
aplicada **entre faturas** em vez de entre contas.

O **total histórico da fatura não cai**: agosto continua tendo sido R$ 1.610,60. O crédito
de rolagem fica fora do total dela e o débito entra no total da seguinte, que é exatamente
o "saldo anterior" da fatura de papel.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Deixar no saldo, como o `ADR-0003` dizia | O saldo fica certo e a fatura fica errada: quem paga por fatura não tem como pagar uma dívida que não está em fatura nenhuma |
| "Saldo anterior" como linha só de exibição, derivada | Não custa lançamento, mas quebra "cada fatura é paga na tela dela": o número existiria na tela e não no dado |
| Um pagamento previsto por cartão, valendo a dívida inteira | Resolve a projeção e não resolve a alocação — continua sem dizer qual fatura foi quitada |

## Consequências

- **Ganhamos:** cada fatura contém tudo o que cobra, e o total dela bate com o do banco.
  Pagar parcial passa a ter um destino visível em vez de sumir no saldo.
- **Perdemos:** dois lançamentos que não são evento financeiro — o dinheiro não se moveu.
  É o mesmo preço que a transferência já cobra, e pela mesma razão: manter "saldo é a soma
  dos lançamentos" verdadeiro ao pé da letra.
- **Passa a ser proibido:** rolagem com um lado só; rolagem que altere a dívida total;
  débito de rolagem `PREVISTO`; pagamento previsto que sobreviva ao vencimento da fatura
  dele; contar linha de rolagem em relatório de gasto ou na fila de pendências.
- **Revisitar se:** aparecer um segundo par que existe só para mover valor entre recortes
  do mesmo agregado. Aí a "transferência interna" vira conceito de primeira classe em vez
  de dois casos parecidos.
