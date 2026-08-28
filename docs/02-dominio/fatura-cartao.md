---
id: 02-dominio/fatura-cartao
titulo: Fatura de cartao
dono: ciclo de fatura, fechamento, vencimento e relacao com lancamentos
ler-junto: [02-dominio/meio-de-pagamento, 02-dominio/lancamento]
status: stub
---

# Fatura de cartao

> **STUB** — conteudo ainda nao escrito. Ao preencher, siga `docs/CONVENTIONS.md`,
> apague este bloco e troque `status: stub` por `status: ativo`.

## Perguntas que este documento precisa responder

- [ ] Como o ciclo de fatura e definido (dia de fechamento x vencimento)
- [ ] A que fatura um lancamento pertence, incluindo casos de borda na virada
- [x] **Como o pagamento da fatura e representado sem contar o gasto duas vezes**
      (respondido pelo prototipo): **pagar nao cria lancamento nenhum.** Os proprios
      lancamentos da fatura ja debitam a conta, com dataEfeito no vencimento — pagar so
      os vira de PREVISTO para REALIZADO, com a dataEfeito na data do pagamento. Um
      lancamento de pagamento por cima contaria o gasto duas vezes. Falta escrever a regra
- [ ] Como parcelamento aparece nas faturas seguintes
- [ ] **Reabertura de fatura** (decidido em 27/08, falta escrever a regra): corrigir um
      lancamento ja faturado e reabrir -> editar -> recalcular -> ajustar o pagamento se
      ja houver -> fechar de novo. Enquanto fechada, os lancamentos da fatura estao
      congelados (`docs/02-dominio/lancamento.md`)
- [ ] Fatura em aberto entra no calculo de patrimonio como divida?
      (`docs/02-dominio/aplicacao-patrimonio.md`)

## Conteudo

_(vazio)_
