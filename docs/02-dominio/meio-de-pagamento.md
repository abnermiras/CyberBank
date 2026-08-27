---
id: 02-dominio/meio-de-pagamento
titulo: Meio de pagamento
dono: tipos de meio, a regra que calcula a dataEfeito de cada tipo, e como o lancamento os referencia
ler-junto: [02-dominio/conta, 02-dominio/lancamento, 02-dominio/fatura-cartao]
status: rascunho
---

# Meio de pagamento

**Meio de pagamento ≠ conta.** A *conta* é de onde o dinheiro sai; o *meio* é **como**
ele saiu. Um cartão de crédito é um meio ligado a uma conta que só é debitada no
vencimento da fatura. Confundir os dois é o erro que quebra o cálculo de saldo — se
o gasto no crédito debitar a conta na hora, o saldo mente o mês inteiro.

## Estrutura

Um meio tem um **tipo** (que define o comportamento) e uma **instância** (o cartão
específico, a conta específica). Adicionar instância é dado; adicionar tipo é código —
ver `docs/08-fluxos/novo-meio-de-pagamento.md`.

**Todo meio aponta para uma conta.** Isso deixou de ser pergunta quando `conta.md` fixou
que tudo com saldo próprio acompanhado é conta: dinheiro aponta para uma `CARTEIRA`,
vale-refeição aponta para uma `BENEFICIO`. Não existe meio órfão.

## Tipos

| Tipo | Conta que ele move | `dataEfeito` | Fatura | Parcela | Captura |
|---|---|---|---|:--:|---|
| `DEBITO` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `CREDITO` | `CORRENTE` (a que paga a fatura) | vencimento da fatura em que caiu | **sim** | **sim** | notificação push |
| `PIX` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `DINHEIRO` | `CARTEIRA` | = `dataEvento` | não | não | só manual |
| `BENEFICIO` | `BENEFICIO` | = `dataEvento` | não | não | ☐ a definir |
| `BOLETO` | `CORRENTE` | data do pagamento | não | não | OFX ou manual |

`APLICACAO` e `POUPANCA` não aparecem nesta tabela: não se paga com elas, resgata-se antes
(`docs/02-dominio/aplicacao-patrimonio.md`).

## A regra da `dataEfeito`

`docs/02-dominio/lancamento.md` define que existem duas datas e delega **para cá** o
cálculo de uma delas. É esta a regra, e é o coração deste doc:

- **À vista** (`DEBITO`, `PIX`, `DINHEIRO`, `BENEFICIO`): `dataEfeito = dataEvento`.
  Comprou, saiu.
- **`CREDITO`**: `dataEfeito` = o **vencimento da fatura** em que a compra caiu. Qual
  fatura é ela, incluindo o caso de borda na virada do fechamento, é assunto de
  `docs/02-dominio/fatura-cartao.md`.
- **`BOLETO`**: o boleto tem duas datas próprias, e é por isso que ele encaixa direto no
  `PREVISTO`/`REALIZADO`:

| Momento | `situacao` | `dataEfeito` |
|---|---|---|
| Boleto registrado, ainda não pago | `PREVISTO` | o vencimento |
| Boleto pago | `REALIZADO` | a data em que foi pago |

Assim o boleto em aberto já entra no saldo projetado — "quanto sobra até o fim do mês"
conta a conta de luz que ainda vai ser paga — sem nunca mentir no saldo realizado.

## Cartão virtual

É **instância** de `CREDITO`, não tipo novo. Nada no comportamento muda: mesma fatura,
mesmo parcelamento, mesma `dataEfeito`. O que muda é o número, e número é dado.

## Débito automático

**Não é meio de pagamento.** O meio continua sendo `DEBITO`; o que "automático" descreve é
que a série se paga sozinha, sem o usuário agir — e isso é fato da **recorrência**
(`docs/02-dominio/recorrencia.md`), não do meio.

## Ciclo de vida

| Momento | Regra |
|---|---|
| Criação | Tipo, conta vinculada e nome. Tipo não muda depois |
| Inativação | Cartão cancelado, conta encerrada: some da escolha, o histórico fica |
| Cartão de crédito inativado | A **fatura em aberto continua viva** até fechar e ser paga. Cancelar cartão não perdoa dívida |
| Exclusão | Só se nunca teve lançamento. Com histórico, o caminho é inativar |

Quem pode: dono e editor (`docs/02-dominio/ambiente-financeiro.md`).

## Como o lançamento referencia

`meioDePagamento` é obrigatório em lançamento de gasto ou receita real, e **ausente** em
transferência, aporte, resgate, rendimento e lançamento de abertura — nesses o dinheiro
não foi "pago" de jeito nenhum, só mudou de lugar.

## Invariantes

- Todo meio pertence a um ambiente e aponta para uma conta **do mesmo ambiente**.
- Nenhum meio aponta para conta `APLICACAO` nem `POUPANCA`: não se paga com elas.
- O tipo de um meio não muda depois de existir lançamento.
- Só `CREDITO` tem fatura e só `CREDITO` parcela.
- Meio inativo não recebe lançamento novo, nem por captura.
- Meio com qualquer lançamento não pode ser excluído.

## Regra de desenho

O `Lancamento` **não conhece os tipos** de meio de pagamento. Toda variação de
comportamento por tipo mora aqui. Se aparecer `if (tipo == CREDITO)` fora deste módulo,
é bug de modelagem, não detalhe de implementação.

## Ainda em aberto

- [ ] `BENEFICIO` tem captura automática? Depende de o app do benefício notificar
- [ ] Confirmar o tipo de conta `BENEFICIO` proposto em `docs/02-dominio/conta.md`
