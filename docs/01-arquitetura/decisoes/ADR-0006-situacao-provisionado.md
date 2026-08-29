---
id: 01-arquitetura/decisoes/ADR-0006-situacao-provisionado
titulo: "ADR-0006: PROVISIONADO, a situacao entre o fato e a liquidacao"
dono: por que a situacao do lancamento tem tres valores e nao dois
ler-junto: [02-dominio/lancamento, 02-dominio/fatura-cartao, 02-dominio/conta]
status: ativo
---

# ADR-0006: `PROVISIONADO`, a situação entre o fato e a liquidação

- **Status:** aceita
- **Data:** 2026-08-28
- **Afeta:** `02-dominio/lancamento`, `02-dominio/conta`, `02-dominio/fatura-cartao`,
  `02-dominio/aplicacao-patrimonio`, `03-dados/modelo-de-dados`

## Contexto

`situacao` tinha dois valores, e eles carregavam **duas perguntas ao mesmo tempo**: *já
aconteceu?* e *entra no saldo realizado?*. Enquanto as duas andaram juntas, ninguém notou.

A conta `CARTAO` separou as duas. Uma compra no crédito **aconteceu** — o comerciante
recebeu, o limite caiu, a dívida subiu — mas **não foi liquidada**: o dinheiro só sai no
pagamento da fatura. Nenhum dos dois valores dizia isso, e o protótipo mostrou o preço:
numa mesma fatura aberta, uma compra à vista aparecia `REALIZADO` e uma parcela aparecia
`PREVISTO`, lado a lado, esperando o mesmo pagamento no mesmo dia.

## Decisão

`situacao` passa a ter **três** valores:

| Valor | Significa | Conta no saldo do que já aconteceu | Conta no projetado |
|---|---|:--:|:--:|
| `PREVISTO` | Vai acontecer, ainda não aconteceu | não | sim |
| `PROVISIONADO` | **Aconteceu, falta liquidar** | **sim** | sim |
| `REALIZADO` | Aconteceu e foi liquidado | sim | sim |

A transição só anda para frente: `PREVISTO → PROVISIONADO → REALIZADO`, e qualquer um dos
saltos é válido. Nunca volta.

**Onde `PROVISIONADO` aparece:** compra no crédito, de qualquer valor e qualquer parcela,
desde o dia da compra até a fatura dela **encerrar** — quitada, ou vencida e rolada
(`ADR-0005`). O **débito de rolagem** também nasce `PROVISIONADO`: é dívida que aconteceu e
ainda espera liquidação. Pagamento parcial não liquida nada. Todas as N parcelas nascem provisionadas
juntas — a compra aconteceu **uma vez**, e o que espalha a cobrança pelos meses é a fatura
de cada parcela, não a situação delas.

**Onde não aparece:** o boleto registrado e não pago continua `PREVISTO`. Ele é lançamento
da conta corrente, e nela nada se moveu ainda — provisionar faria "em caixa" descontar
contas que o usuário nem pagou.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Manter dois valores, compra `REALIZADO` desde o dia dela | Era o desenho até aqui. Deixa "realizado" significar coisas diferentes na mesma fatura e obriga a explicar por que uma parcela é diferente de uma compra à vista |
| Manter dois valores, compra `PREVISTO` até a fatura ser paga | Faz o saldo da conta que existe **para mostrar dívida** ficar zerado justamente enquanto se deve. E já custou um box de aviso no `recorrencia.md`, avisando que ali `PREVISTO` não queria dizer "não aconteceu" |
| Um campo novo, separado de `situacao` | Dois campos para uma pergunta só. A pergunta é uma: em que ponto entre o fato e a liquidação este lançamento está |

## Consequências

- **Ganhamos:** duas exceções somem. A **dívida do cartão** deixa de ser cálculo próprio e
  volta a ser o saldo da conta — o pagamento previsto é `PREVISTO`, então já não entra. E o
  **patrimônio** volta a ser o saldo realizado de todas as contas, sem tratar a `CARTAO`
  como caso especial. A frase "dívida futura já é sua; receita futura ainda não" deixa de
  ser regra e vira consequência.
- **Perdemos:** um valor a mais no enum, em todo lugar que lê `situacao` — telas, filtros,
  relatórios e a migration.
- **Passa a ser proibido:** tratar `situacao === 'REALIZADO'` como sinônimo de "entra no
  saldo". O teste é `situacao !== 'PREVISTO'`.
- **Revisitar se:** aparecer um caso de fato ocorrido e não liquidado **fora** do cartão que
  precise entrar no saldo. Aí `PROVISIONADO` deixa de ser regra do crédito e vira regra
  geral do domínio.
