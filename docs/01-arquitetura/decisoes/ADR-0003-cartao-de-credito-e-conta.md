---
id: 01-arquitetura/decisoes/ADR-0003-cartao-de-credito-e-conta
titulo: "ADR-0003: o contrato de cartão de crédito é uma conta"
dono: como a dívida de cartão é representada e o que isso faz com o pagamento de fatura
ler-junto: [02-dominio/conta, 02-dominio/fatura-cartao, 02-dominio/meio-de-pagamento]
status: ativo
---

# ADR-0003: o contrato de cartão de crédito é uma conta

- **Status:** aceita, substitui a regra "pagar fatura não cria lançamento"
- **Data:** 2026-08-28
- **Afeta:** `02-dominio/conta`, `02-dominio/fatura-cartao`, `02-dominio/meio-de-pagamento`,
  `02-dominio/lancamento`, `02-dominio/aplicacao-patrimonio`, `03-dados/modelo-de-dados`

## Contexto

O desenho anterior fazia a compra no crédito **debitar a conta corrente** com `dataEfeito`
no vencimento, e por isso pagar a fatura não podia criar lançamento — criaria, o gasto
contaria duas vezes.

Três cenários de compartilhamento quebraram isso de uma vez
(`docs/02-dominio/compartilhamento.md`): uma fatura paga em dois pedaços, de contas
diferentes e de ambientes diferentes; um cartão adicional cuja fatura é paga por **outra
pessoa**; e um cartão compartilhado em que cada ambiente paga a parte que lhe cabe. Em
todos, os lançamentos de um ambiente debitariam a conta corrente de outro.

## Decisão

**O contrato de cartão de crédito é uma conta**, de tipo `CARTAO`, cujo saldo é a dívida.
Ele passa no critério que o projeto já usa em `docs/02-dominio/conta.md`: *se tem saldo
próprio que o sistema acompanha, é conta*.

Disso decorre, sem regra nova:

| O que | Como fica |
|---|---|
| Compra no cartão | `SAIDA` na conta `CARTAO`. A dívida sobe. Não toca a conta corrente |
| Os cartões | Físico, virtual e adicional são **meios de pagamento** `CREDITO` apontando para a mesma conta `CARTAO`. Não existe nível "contrato" separado |
| Pagar a fatura | **Transferência** da conta pagadora para a conta `CARTAO` — e transferência já não entra em gasto por categoria, como aporte e resgate |
| Pagamento parcial | O que não foi pago **fica** como saldo da conta `CARTAO`. Não há par de rolagem |
| Dívida de cartão | É o **saldo** da conta. O patrimônio já somava saldo |
| Limite disponível | `limite − |saldo projetado|` da conta `CARTAO` — o projetado já inclui as parcelas futuras |
| Dois pagadores | Duas transferências para a mesma conta `CARTAO`. Zero regra especial |

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Manter a compra debitando a conta corrente | Funciona para um usuário com um cartão. Morre no cartão adicional de outra pessoa: os gastos dela debitariam a conta de quem tem o contrato |
| Pagamento multi-ambiente como reembolso entre contas | Salva a regra antiga com uma exceção que o usuário não pensa assim — ninguém "reembolsa" a si mesmo por ter pago a própria fatura |
| Cartão como entidade própria, fora de Conta | Teria que reaprender saldo, extrato, transferência e projeção — tudo que Conta já faz. É a mesma razão que fez `APLICACAO` ser conta |

## Consequências

- **Ganhamos:** o pagamento parcial some como regra especial; a dívida vira saldo; e o
  extrato da conta corrente passa a mostrar **um** pagamento de fatura, igual ao extrato do
  banco — a conciliação, que ia ter que casar 1 débito com N compras, deixa de ter esse
  problema (`docs/02-dominio/importacao-conciliacao.md`).
- **Perdemos:** duas regras já escritas e commitadas caem — "pagar fatura não cria
  lançamento" e o par de rolagem do pagamento parcial. O custo é de reescrita, não de
  modelo.
- **Passa a ser proibido:** lançamento de compra no crédito que debite conta que não seja a
  `CARTAO`; qualquer cálculo de dívida de cartão que não seja o saldo dessa conta.
- **Revisitar se:** aparecer um cartão cuja fatura não tem conta de destino — pré-pago ou
  algo que não gere dívida. Aí não é `CARTAO`, é outro tipo.
