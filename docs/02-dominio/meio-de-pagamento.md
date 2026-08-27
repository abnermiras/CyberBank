---
id: 02-dominio/meio-de-pagamento
titulo: Meio de pagamento
dono: tipos de meio de pagamento, atributos de cada tipo e como o lançamento os referencia
ler-junto: [02-dominio/conta, 02-dominio/lancamento, 02-dominio/fatura-cartao]
status: rascunho
---

# Meio de pagamento

**Meio de pagamento ≠ conta.** A *conta* é de onde o dinheiro sai; o *meio* é **como**
ele saiu. Um cartão de crédito é um meio ligado a uma conta que só é debitada no
vencimento da fatura. Confundir os dois é o erro que quebra o cálculo de saldo — se
o gasto no crédito debitar a conta na hora, o saldo mente o mês inteiro.

## Estrutura

Um meio de pagamento tem um **tipo** (que define o comportamento) e uma **instância**
(o cartão específico, a carteira específica). Adicionar uma instância é dado; adicionar
um tipo é código — ver `docs/08-fluxos/novo-meio-de-pagamento.md`.

## Tipos previstos

Preencha a tabela decidindo o comportamento de cada tipo. Os valores abaixo são
**proposta a confirmar**.

| Tipo | Debita a conta | Tem fatura | Parcela | Tem saldo próprio | Captura automática |
|---|---|---|---|---|---|
| Débito | na hora | não | não | não | notificação push |
| Crédito | no vencimento da fatura | sim | sim | não (tem limite) | notificação push |
| Pix | na hora | não | não | não | notificação push |
| Dinheiro | na hora | não | não | não | não — só manual |
| Vale-refeição / benefício | na hora | não | não | **sim** (saldo separado) | a definir |
| Boleto | na data de pagamento | não | não | não | a definir |

## Perguntas em aberto

- [ ] Vale-benefício: proposta em `docs/02-dominio/conta.md` é que seja uma **conta** de
      tipo `BENEFICIO`, com um meio de pagamento apontando para ela — pela regra "tem
      saldo próprio acompanhado, então é conta". Confirmar ou recusar lá, não aqui.
- [ ] Cartão virtual é instância de crédito ou tipo próprio?
- [ ] Um meio pode existir sem conta vinculada? (dinheiro, vale)
- [ ] O que acontece com o histórico quando um cartão é cancelado?
- [ ] Débito automático de conta: é meio de pagamento ou atributo de recorrência?

## Regras (a escrever)

- [ ] Invariantes: o que nunca pode ser verdade sobre um meio de pagamento
- [ ] Ciclo de vida: criação, inativação, o que trava a exclusão
- [ ] Como o lançamento referencia o meio e o que é obrigatório em cada tipo
- [ ] Como o tipo influencia a data de efeito no saldo

## Regra de desenho

O `Lancamento` **não conhece os tipos** de meio de pagamento. Toda variação de
comportamento por tipo mora aqui. Se aparecer `if (tipo == CREDITO)` fora deste módulo,
é bug de modelagem, não detalhe de implementação.
