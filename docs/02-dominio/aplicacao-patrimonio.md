---
id: 02-dominio/aplicacao-patrimonio
titulo: Aplicação e patrimônio
dono: aplicacao como conta, aporte e resgate, atualizacao do valor atual e o calculo do patrimonio
ler-junto: [02-dominio/conta, 02-dominio/lancamento, 00-produto/roadmap]
status: rascunho
---

# Aplicação e patrimônio

É a fatia da aposta que entra na Fase 1 (`docs/00-produto/roadmap.md`). O que ela precisa
entregar é uma frase: **quanto eu tenho**, e não só para onde o dinheiro foi.

Escopo fechado pelo roadmap: valor atual informado **à mão**. Sem rentabilidade calculada,
sem cotação, sem consulta a serviço externo — custo externo zero é restrição.

## Aplicação é uma conta

Aplicação tem saldo, recebe dinheiro e devolve dinheiro. Isso é uma conta
(`docs/02-dominio/conta.md`): tipo `APLICACAO`, com `entraNoFluxoDeCaixa = false`.

Não é entidade própria porque entidade própria teria que reaprender saldo, extrato e
transferência — tudo que Conta já sabe fazer.

O que a distingue das outras contas:

- Nenhum meio de pagamento aponta para ela: **não se compra com uma aplicação**. Para
  gastar, resgata-se primeiro.
- Mover dinheiro para ela **não é gasto**.

> ☐ **A definir:** se `APLICACAO` precisa de subtipo (poupança, CDB, ação, reserva) ou se
> isso é rótulo livre. Só vale virar campo se alguma **regra** mudar por subtipo — se for
> só para o usuário se organizar, é nome da conta e pronto.

## Aporte e resgate

São transferências, e nada além disso (`docs/02-dominio/lancamento.md`):

| Operação | O par de lançamentos |
|---|---|
| **Aporte** | `SAIDA` na conta corrente + `ENTRADA` na aplicação, mesmo `transferenciaId` |
| **Resgate** | `SAIDA` na aplicação + `ENTRADA` na conta corrente |

Como toda transferência, não têm categoria e **não aparecem no relatório de gasto por
categoria**. Guardar dinheiro não é despesa; se aparecesse, o relatório de gasto mentiria
e comparar mês a mês ficaria distorcido por quem aportou muito.

**Consequência aceita, e ela precisa aparecer na tela:** o mês deixa de fechar na soma
simples. Quem recebeu 5 mil, gastou 3 e guardou 2 vê 3 de gasto e some com 2. O dashboard
mostra **uma linha "guardado" separada do gasto** — fora do relatório por categoria, mas
visível no fechamento do mês, senão o usuário procura o dinheiro que sumiu.
Regras da tela: `docs/06-interface/dashboard.md`.

## O valor atual, e por que ele também é lançamento

Uma aplicação rende. O saldo dos aportes diz quanto foi colocado, não quanto vale hoje.

Quando o usuário informa o valor atual, o sistema **não sobrescreve nada**: ele cria um
lançamento de **rendimento** na conta de aplicação, com a diferença entre o valor
informado e o saldo atual — `ENTRADA` se rendeu, `SAIDA` se perdeu.

Isso mantém de pé a regra que vale para todas as contas: *saldo é a soma dos lançamentos,
sempre, sem campo guardando total*. E dá de graça três coisas: o rendimento tem data,
aparece no extrato, e o histórico de quanto a aplicação valeu ao longo do tempo existe sem
tabela nova.

O lançamento de rendimento não tem categoria e não entra no fluxo de caixa — ele não é
receita da vida, é a aplicação valendo mais.

> ☐ **A definir:** o que acontece quando o usuário passa meses sem atualizar. O patrimônio
> envelhece calado, e um número velho apresentado como atual é pior que número nenhum.
> Mínimo: a tela mostra a data da última atualização de cada aplicação.

## Patrimônio

**Patrimônio = soma do saldo realizado de todas as contas do ambiente**, aplicações
inclusive. É consulta, não entidade: nada é armazenado.

A distinção que dá sentido a tudo:

| Leitura | O que responde |
|---|---|
| **Fluxo de caixa** | Só contas com `entraNoFluxoDeCaixa = true`. "Quanto entrou e saiu da minha vida este mês" |
| **Patrimônio** | Todas as contas. "Quanto eu tenho" |

Um aporte muda o fluxo de caixa do mês e **não muda o patrimônio** — o dinheiro só trocou
de bolso. Se um aporte alterar o patrimônio, é bug.

> ☐ **A definir:** patrimônio desconta dívida? Fatura de cartão em aberto é dinheiro que
> já não é seu. Descontar dá o número honesto; não descontar dá o número que o usuário
> espera ver. Decidir junto com `docs/02-dominio/fatura-cartao.md`.

## Invariantes

- Conta `APLICACAO` tem `entraNoFluxoDeCaixa = false`.
- Nenhum meio de pagamento aponta para uma conta `APLICACAO`.
- Aporte e resgate são sempre um par de lançamentos com o mesmo `transferenciaId`.
- Aporte, resgate e rendimento não têm categoria e nunca entram no relatório de gasto.
- Patrimônio nunca é armazenado; é sempre derivado dos saldos.
- Aporte e resgate não alteram o patrimônio total. Só rendimento altera.

## Fora da Fase 1

Rentabilidade calculada, cotação automática, preço de ativo e meta com valor-alvo. A meta
ainda não tem doc dono — quando a Fase 3 chegar, ela ganha o dela.
