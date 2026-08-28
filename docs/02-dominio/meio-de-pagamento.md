---
id: 02-dominio/meio-de-pagamento
titulo: Meio de pagamento
dono: tipos de meio, a regra da dataEfeito, os cartoes de um contrato e o limite
ler-junto: [02-dominio/conta, 02-dominio/lancamento, 02-dominio/fatura-cartao]
status: rascunho
---

# Meio de pagamento

**Meio de pagamento ≠ conta.** A *conta* é de onde o dinheiro sai; o *meio* é **como** ele
saiu. Confundir os dois é o erro que quebra o cálculo de saldo.

No crédito isso fica visível: a conta que a compra move é a conta **`CARTAO`** — a dívida
sobe na hora (`ADR-0003`). A conta corrente só se move no dia do pagamento da fatura, e por
uma transferência.

## Estrutura

Um meio tem um **tipo** (que define o comportamento) e uma **instância** (o cartão
específico, a conta específica). Adicionar instância é dado; adicionar tipo é código —
ver `docs/08-fluxos/novo-meio-de-pagamento.md`.

**Todo meio aponta para uma conta.** Dinheiro aponta para uma `CARTEIRA`, vale-refeição
aponta para uma `BENEFICIO`, cartão de crédito aponta para a `CARTAO` do seu contrato. Não
existe meio órfão.

## Tipos

| Tipo | Conta que ele move | `dataEfeito` | Fatura | Parcela | Captura |
|---|---|---|:--:|:--:|---|
| `DEBITO` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `CREDITO` | **`CARTAO`** | = `dataEvento` | **sim** | **sim** | notificação push |
| `PIX` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `DINHEIRO` | `CARTEIRA` | = `dataEvento` | não | não | só manual |
| `BENEFICIO` | `BENEFICIO` | = `dataEvento` | não | não | ☐ a definir |
| `BOLETO` | `CORRENTE` | data do pagamento | não | não | OFX ou manual |

Conta `APLICACAO` não aparece nesta tabela: não se paga com ela, resgata-se antes
(`docs/02-dominio/aplicacao-patrimonio.md`).

## A regra da `dataEfeito`

`docs/02-dominio/lancamento.md` define que existem duas datas e delega **para cá** o cálculo
de uma delas.

**Em todo meio à vista — crédito incluído — `dataEfeito = dataEvento`.** Comprou, saiu; no
crédito, comprou, deve. O crédito deixou de ser exceção quando a dívida passou a ter conta
própria: não é mais preciso adiar o efeito até o vencimento para o saldo não mentir.

O **boleto** é o único tipo com duas datas de verdade, e é por isso que ele encaixa direto no
`PREVISTO`/`REALIZADO`:

| Momento | `situacao` | `dataEfeito` |
|---|---|---|
| Boleto registrado, ainda não pago | `PREVISTO` | o vencimento |
| Boleto pago | `REALIZADO` | a data em que foi pago |

Assim o boleto em aberto já entra no saldo projetado — "quanto sobra até o fim do mês" conta
a conta de luz que ainda vai ser paga — sem nunca mentir no saldo realizado.

## Os cartões de um contrato

O contrato de cartão de crédito **é uma conta** `CARTAO` (`ADR-0003`). Ela guarda o que é do
contrato: limite, ciclo da fatura e conta pagadora padrão. Os cartões são os **meios**
`CREDITO` que apontam para ela.

> **Exemplo literal.** Conta `CORRENTE` "Nubank" → conta `CARTAO` "UltraVioleta" (limite,
> vence dia 5, fecha 8 dias antes, paga pela Nubank) → meios `CREDITO`: `****-1234` físico e
> `FREELANCE ****-0987` virtual.

| Cartão | O que é | Diferença de comportamento |
|---|---|---|
| **Físico** | O plástico | Nenhuma. É o caso base |
| **Virtual** | Outro número, mesmo contrato | **Nenhuma**: mesma fatura, mesmo limite, mesma `dataEfeito`. O que muda é o número, e número é dado |
| **Adicional** | Cartão do contrato emitido **para outra pessoa** | A pessoa vê a fatura dela e paga a parte dela (`docs/02-dominio/compartilhamento.md`) |

**Adicional e cartão compartilhado não são a mesma coisa**, e a diferença é quem usa:

| | Para quem | O que a pessoa recebe |
|---|---|---|
| **Adicional** | Outra pessoa | Um cartão **dela**, com número próprio e parte própria na fatura |
| **Compartilhado** | Outro ambiente, seu ou de outra pessoa | O **mesmo** cartão, usado pelos dois — o cartão de gasolina da casa |

Não se cria adicional para si mesmo em outro ambiente: adicional é, por definição, para
outra pessoa. Para usar o próprio cartão em outro ambiente seu, o caminho é compartilhar.

## Limite

O limite é do **contrato** — ou seja, da conta `CARTAO` — e não se divide entre os cartões.
Físico, virtual, adicional e compartilhado comem do mesmo bolo.

> **Disponível = limite − dívida da conta `CARTAO`**, e a dívida é simplesmente o **saldo**
> dela (`docs/02-dominio/fatura-cartao.md`).

Ele já inclui a parcela de daqui a oito meses, porque **parcela futura segura limite**, como
na vida real:
R$ 5.000 em 10x come R$ 5.000 do limite na hora e libera R$ 500 a cada fatura paga
(`docs/02-dominio/fatura-cartao.md`).

A parcela pesa desde a compra porque ela é `PROVISIONADA` desde a compra: o fato aconteceu
uma vez (`ADR-0006`).

É exatamente por isso que **recorrência não gera lançamento futuro**
(`docs/02-dominio/recorrencia.md`): se gerasse, seguraria limite de um mês que não chegou.
Uma assinatura pesa no limite um ciclo por vez.

Num contrato compartilhado, **o limite e o consumo são visíveis em qualquer ambiente** que
tenha um cartão dele. Limite é do contrato, e esconder metade dele daria um número que não
serve para decidir nada.

> ☐ **A definir:** o limite é dado informado pelo usuário ou capturado do banco? Enquanto
> for informado à mão, ele envelhece — e limite errado é pior que limite ausente.

## Débito automático

**Não é meio de pagamento.** O meio continua sendo `DEBITO`; o que "automático" descreve é
que a série se paga sozinha, sem o usuário agir — e isso é fato da **recorrência**
(`docs/02-dominio/recorrencia.md`), não do meio.

## Ciclo de vida

| Momento | Regra |
|---|---|
| Criação | Tipo, conta vinculada e nome. Tipo não muda depois |
| Criação de um `CREDITO` | Aponta para uma conta `CARTAO`. Limite, ciclo e conta pagadora são **da conta**, não do cartão — vários cartões dividem tudo isso |
| Inativação | Cartão cancelado, conta encerrada: some da escolha, o histórico fica |
| Cartão de crédito inativado | A **fatura em aberto continua viva** até fechar e ser paga. Cancelar cartão não perdoa dívida |
| Exclusão | Só se nunca teve lançamento. Com histórico, o caminho é inativar |

Quem pode: dono e editor (`docs/02-dominio/ambiente-financeiro.md`).

## Como o lançamento referencia

`meioDePagamento` é obrigatório em lançamento de gasto ou receita real, e **ausente** em
transferência, aporte, resgate, rendimento, pagamento de fatura e lançamento de abertura —
nesses o dinheiro não foi "pago" de jeito nenhum, só mudou de lugar.

## Invariantes

- Todo meio pertence a um ambiente e aponta para uma conta.
- Todo meio `CREDITO` aponta para uma conta `CARTAO`, e só `CREDITO` aponta para ela.
- Nenhum meio aponta para conta `APLICACAO`: não se paga com ela.
- O tipo de um meio não muda depois de existir lançamento.
- Só `CREDITO` tem fatura e só `CREDITO` parcela.
- Limite e ciclo são da conta `CARTAO`, nunca do cartão.
- Meio inativo não recebe lançamento novo, nem por captura.
- Meio com qualquer lançamento não pode ser excluído.
- Um meio de ambiente diferente só é usável se houver compartilhamento
  (`docs/02-dominio/compartilhamento.md`).

## Regra de desenho

O `Lancamento` **não conhece os tipos** de meio de pagamento. Toda variação de comportamento
por tipo mora aqui. Se aparecer `if (tipo == CREDITO)` fora deste módulo, é bug de
modelagem, não detalhe de implementação.

## Ainda em aberto

- [ ] `BENEFICIO` tem captura automática? Depende de o app do benefício notificar
- [ ] Confirmar o tipo de conta `BENEFICIO` proposto em `docs/02-dominio/conta.md`
- [ ] Cartão adicional exige que a pessoa tenha cadastro no sistema, ou o adicional pode ser
      só um rótulo de quem gasta?
