---
id: 02-dominio/conta
titulo: Conta
dono: tipos de conta, saldo, a separacao entre fluxo de caixa e patrimonio, e o ciclo de vida
ler-junto: [02-dominio/lancamento, 02-dominio/meio-de-pagamento, 02-dominio/aplicacao-patrimonio]
status: rascunho
---

# Conta

**Conta é onde o dinheiro está.** Se uma coisa tem saldo próprio e o sistema precisa
acompanhar esse saldo, ela é uma conta — não um tipo novo de entidade.

Essa é a regra que resolve quatro perguntas que apareceram separadas: aplicação é conta,
vale-benefício é conta, carteira de dinheiro vivo é conta — e **o contrato de cartão de
crédito também é conta**, porque a dívida dele é um saldo que o sistema acompanha
(`ADR-0003`). Cada uma tem saldo, recebe e devolve dinheiro. O que muda entre elas é
**comportamento**, e comportamento é `tipo`.

Toda conta pertence a um **ambiente financeiro** e nunca muda de ambiente
(`docs/02-dominio/ambiente-financeiro.md`). Ela pode ser **usada** por outros ambientes, se
for compartilhada: o que atravessa é o uso, nunca a posse
(`docs/02-dominio/compartilhamento.md`).

## Os três eixos

Uma conta tem três classificações separadas. Confundi-las é o erro que faz o
dashboard mentir.

**Eixo 1 — `tipo`:** o que a conta é.

| Tipo | O que é | Meio de pagamento próprio |
|---|---|---|
| `CORRENTE` | Conta bancária do dia a dia | débito, Pix, boleto |
| `CARTEIRA` | Dinheiro vivo | dinheiro |
| `APLICACAO` | Dinheiro guardado ou investido — ver `docs/02-dominio/aplicacao-patrimonio.md` | nenhum: não se paga com ela |
| `BENEFICIO` | Vale-refeição e afins, com saldo separado do banco | o cartão de benefício |
| `CARTAO` | O **contrato** de cartão de crédito. O saldo é a dívida — `ADR-0003` | os cartões do contrato: físico, virtual, adicional |

**Não existe tipo `POUPANCA`.** Poupança é uma aplicação: fica fora do fluxo de caixa, não
tem meio de pagamento e rende — comportamento idêntico ao de `APLICACAO`. Uma poupança é
uma conta `APLICACAO` chamada "Poupança do Itaú". Dois tipos com o mesmo comportamento é
exatamente o que a regra desta seção manda não criar.

**`BENEFICIO` está confirmado — e não pelo motivo que parecia.** "Saldo separado do banco"
não muda regra nenhuma: uma `CORRENTE` chamada "Vale-refeição" faria o mesmo, e é assim que
`POUPANCA` morreu. O que justifica o tipo é o saldo ser **não fungível**: dele não sai
transferência, e ele não é caixa. São duas regras do sistema que mudam por causa dele — que
é exatamente o critério desta seção. **Revisitar se** aparecer benefício com saque em
dinheiro: aí ele volta a ser fungível e o tipo perde a razão de existir.

**Eixo 2 — `entraNoFluxoDeCaixa`:** se o movimento nessa conta é gasto/receita da vida,
ou apenas dinheiro trocando de lugar dentro do próprio patrimônio.

| Valor | Contas | Consequência |
|---|---|---|
| `true` | `CORRENTE`, `CARTEIRA`, `BENEFICIO`, `CARTAO` | Saída daqui é gasto; entrada é receita |
| `false` | `APLICACAO` | Mover dinheiro para cá **não é gasto** — é guardar |

É esse campo, e não o tipo, que o dashboard de gasto por categoria consulta. Tipo novo no
futuro só precisa responder a esta pergunta para o relatório continuar certo.

`CARTAO` entra como `true` porque **comprar no cartão é gasto da vida**. Pagar a fatura não
conta de novo: pagamento é transferência, e transferência nunca entra no relatório de gasto
(`docs/02-dominio/fatura-cartao.md`).

**Eixo 3 — `entraEmCaixa`:** se o saldo dessa conta é dinheiro que serve para pagar
**qualquer coisa**.

| Valor | Contas | Consequência |
|---|---|---|
| `true` | `CORRENTE`, `CARTEIRA` | Entra em "em caixa" e em "quanto sobra até o fim do mês" |
| `false` | `APLICACAO`, `BENEFICIO`, `CARTAO` | Fica fora das duas leituras. Continua no patrimônio |

`entraNoFluxoDeCaixa` **não responde "isso é caixa?"**, e agora são **dois** os casos em que as
duas perguntas divergem: a `CARTAO`, cujo saldo é dívida e não dinheiro (achado do protótipo em
28/08), e a `BENEFICIO`, cujo saldo é dinheiro que só compra uma coisa. Este doc dizia que ao
aparecer o **segundo** caso a exceção por tipo viraria campo próprio — foi o que aconteceu:
onde se lia "contas de fluxo **menos as de dívida**", agora se lê **`entraEmCaixa`**.

O achado saiu de somar os números do seed: R$ 880 de vale entravam nos R$ 12.036,80 de "em
caixa", e o sistema afirmava que dava para pagar um boleto com dinheiro que só compra comida.

## Saldo

**Saldo = soma dos lançamentos da conta até uma data.** Sem exceção e sem campo
denormalizado guardando o total — saldo guardado é saldo que diverge.

Como o lançamento tem situação `PREVISTO`, `PROVISIONADO` ou `REALIZADO`
(`docs/02-dominio/lancamento.md`), o saldo tem duas leituras, e elas nunca se misturam
na mesma tela sem rótulo:

| Leitura | Como se calcula | Para que serve |
|---|---|---|
| **Saldo realizado** | Tudo que **já aconteceu** até hoje: `REALIZADO` e `PROVISIONADO` (`ADR-0006`) | Quanto tem na conta agora — e, na `CARTAO`, quanto se deve |
| **Saldo projetado** | Realizado mais o `PREVISTO` até uma data futura | Quanto sobra até o fim do mês |

**Saldo inicial é um lançamento**, não um campo: ao criar a conta com saldo existente,
nasce um lançamento de abertura naquele valor, `REALIZADO` — o dinheiro já está lá. Assim a frase "saldo é a soma dos
lançamentos" continua verdadeira literalmente, sem um "mais o saldo inicial" grudado em
cada cálculo.

## Valores

Todo valor é **inteiro em centavos**, em real. Sem `double`, sem multi-moeda — regra 5
do `CLAUDE.md` e não-objetivo do roadmap.

## Ciclo de vida

| Momento | Regra |
|---|---|
| Criação | Nome, tipo e saldo inicial (que vira lançamento de abertura) |
| Criação de uma `CARTAO` | Mais limite, dia do vencimento, quantos dias antes fecha e conta pagadora padrão. Nasce já com a fatura `ABERTA` do ciclo corrente (`docs/02-dominio/fatura-cartao.md`) |
| Edição | Nome livre. **Tipo não muda** depois de existir lançamento — mudaria o significado do histórico |
| Inativação | Não aceita lançamento novo **do usuário**; histórico e saldo continuam existindo e visíveis. Numa `CARTAO`, o ciclo da fatura continua correndo — cancelar cartão não perdoa dívida |
| Exclusão | Só se a conta nunca teve lançamento. Com histórico, o caminho é inativar |

Quem pode: dono e editor. Leitor não mexe (`docs/02-dominio/ambiente-financeiro.md`).

## Invariantes

- Toda conta pertence a exatamente um ambiente, e nunca muda de ambiente.
- Toda conta tem `tipo`, `entraNoFluxoDeCaixa` e `entraEmCaixa`, os três obrigatórios.
- `entraEmCaixa = true` implica `entraNoFluxoDeCaixa = true`. O contrário não vale: a
  `BENEFICIO` e a `CARTAO` são de fluxo e não são caixa.
- Conta `BENEFICIO` não é origem nem destino de transferência: o saldo dela não é fungível.
  Entra por receita (o crédito do benefício) e sai por gasto no meio dele.
- Conta inativa não recebe lançamento novo **do usuário** — nem previsto, nem por captura. O
  que o ciclo da fatura produz sozinho (rolagem e pagamento previsto) continua nascendo numa
  `CARTAO` inativada, até a dívida acabar (`docs/02-dominio/fatura-cartao.md`).
- Uma conta com qualquer lançamento não pode ser excluída.
- Conta com `entraNoFluxoDeCaixa = false` não é origem de compra: nenhum meio de
  pagamento aponta para ela.
- Só meio `CREDITO` aponta para conta `CARTAO`, e todo `CREDITO` aponta para uma.
- Conta `CARTAO` nunca é compartilhada inteira — só os cartões dela
  (`docs/02-dominio/compartilhamento.md`).
- Saldo nunca é armazenado como total; é sempre derivado dos lançamentos.

## Fronteiras com outros docs

- **Conta ≠ meio de pagamento.** A conta é *de onde* o dinheiro sai; o meio é *como* ele
  saiu. As regras do meio ficam em `docs/02-dominio/meio-de-pagamento.md`.
- Como o dinheiro anda **entre** contas (transferência, aporte, resgate):
  `docs/02-dominio/lancamento.md`.
- Como o patrimônio soma as contas: `docs/02-dominio/aplicacao-patrimonio.md`.
