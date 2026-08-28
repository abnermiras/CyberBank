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
- [ ] **Ao fechar, a fatura seguinte recebe as recorrencias ativas** do cartao: e o
      gatilho de geracao da recorrencia, e o motivo de ela nao ter horizonte
      (`docs/02-dominio/recorrencia.md`)
- [ ] **Reabertura de fatura** (decidido, falta escrever a regra): reabrir -> editar ->
      fechar de novo. Se a fatura ja estava paga, o valor do pagamento e **reescrito** na
      data original — sem lancamento de ajuste no extrato, porque o saldo e derivado dos
      lancamentos e se refaz sozinho. Enquanto fechada, os lancamentos estao congelados
- [ ] Editar uma serie (recorrencia ou parcelamento) pode obrigar a **reabrir varias
      faturas de uma vez**: mostrar quais antes de confirmar (`docs/02-dominio/recorrencia.md`)
- [ ] Fatura em aberto entra no calculo de patrimonio como divida?
      (`docs/02-dominio/aplicacao-patrimonio.md`)

## Conteudo

_(vazio)_
