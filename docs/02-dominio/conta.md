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

A fatura entra no projetado por **consulta**, não por lançamento: o sistema não cria pagamento
previsto de fatura, porque não sabe de qual conta nem em que dia você vai pagar
(`docs/02-dominio/fatura-pagamento.md`).

**O desconto é do agregado, nunca do saldo projetado de uma conta.** *"Quanto sobra até o fim do
mês"* e o em-caixa projetado descontam o `a pagar` das faturas que vencem na janela; o
`saldoProjetado` de **cada conta** não muda, porque o sistema não sabe de qual conta o dinheiro
vai sair — afirmá-lo seria o mesmo palpite que derrubou o pagamento previsto automático.

**E o que já foi agendado não é contado duas vezes.** O pagamento agendado é um `PREVISTO`
daquela conta e já está na projeção; o que a fatura acrescenta é só o que **ainda não tem
pagamento marcado** — `a pagar` menos o agendado, nunca abaixo de zero. O `a pagar` em si **não
cai** com o agendamento, e isso é de propósito: *pago* é a soma dos pagamentos `REALIZADO`
(`docs/02-dominio/fatura-cartao.md`), e contar o previsto ali faria a fatura ler como quitada e
**encerrar antes de o dinheiro sair**.

| Leitura | Como se calcula | Para que serve |
|---|---|---|
| **Saldo realizado** | Tudo que **já aconteceu** até hoje: `REALIZADO` e `PROVISIONADO` (`ADR-0006`) | Quanto tem na conta agora — e, na `CARTAO`, quanto se deve |
| **Saldo projetado** | Realizado mais o `PREVISTO` até uma data futura, **menos o `a pagar` das faturas que vencem até lá** | Quanto sobra até o fim do mês |

**O horizonte padrão é o fim do mês corrente, e a janela começa hoje** — não no dia 1º. O que
estava previsto e cuja data já passou não é projeção: ou a rotina já o realizou, ou ele é uma
pendência de conciliação, e nos dois casos somá-lo ao futuro contaria duas vezes. A janela é
uma só no sistema, e é ela que a Home e o Extrato dividem: **dois lugares mostrando o mesmo
número nunca podem calculá-lo duas vezes.**

**O projetado nunca aparece sem o realizado ao lado, e nunca sem a data no rótulo.** É a
mesma regra que a Home já pratica em *"em caixa · agora"* e *"sobra até `dd/mm`"*
(`docs/06-interface/dashboard.md`): são perguntas diferentes, e a data é o que as separa.

**Saldo inicial é um lançamento**, não um campo: ao criar a conta com saldo existente, nasce
um lançamento de abertura naquele valor, `REALIZADO` — o dinheiro já está lá. Assim a frase
"saldo é a soma dos lançamentos" continua verdadeira literalmente, sem um "mais o saldo
inicial" grudado em cada cálculo.

**Conta `CARTAO` não tem saldo inicial, e é a única exceção.** O saldo dela não é dinheiro
parado: é dívida, e **dívida de cartão não é um número, é um conjunto de faturas** — com
vencimentos, com parcelas em curso, com uma parte já fechada no emissor e outra ainda
correndo. Achatar isso num lançamento só poria na primeira fatura do app um valor que não
pertence a ciclo nenhum, e `Saldo de abertura` seria a categoria de um fato que não aconteceu
ali. A `CARTAO` nasce zerada, e a fatura que nasce com ela nasce vazia.

O preço está nomeado e é temporário: **patrimônio e limite disponível ficam otimistas** até as
faturas do Cyberbank alcançarem as do emissor, um ou dois ciclos depois. A dívida anterior
você paga no banco, fora do app — como já era antes de o app existir.

## Valores

Todo valor é **inteiro em centavos**, em real. Sem `double`, sem multi-moeda — regra 5
do `CLAUDE.md` e não-objetivo do roadmap.

## Ciclo de vida

| Momento | Regra |
|---|---|
| Criação | Nome, tipo e saldo inicial (que vira lançamento de abertura) — **menos na `CARTAO`** |
| Criação de uma `CARTAO` | **Sem saldo inicial.** Limite, dia do vencimento, quantos dias antes fecha e conta pagadora padrão. Nasce zerada e já com a fatura `ABERTA` do ciclo corrente, vazia (`docs/02-dominio/fatura-cartao.md`) |
| Edição | Nome livre. **Tipo não muda** depois de existir lançamento — mudaria o significado do histórico |
| Inativação | Não aceita lançamento novo **do usuário**; histórico e saldo continuam existindo e visíveis. **Os `PREVISTO` dela são descartados** — não vão acontecer, a conta saiu da sua vida. Numa `CARTAO`, o ciclo da fatura continua correndo: cancelar cartão não perdoa dívida |
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
  que o ciclo da fatura produz sozinho (a rolagem) continua nascendo numa `CARTAO` inativada,
  até a dívida acabar (`docs/02-dominio/fatura-cartao.md`).
- Uma conta com qualquer lançamento **realizado** não pode ser excluída: os realizados são
  histórico e o saldo depende deles. O caminho é inativar, e inativar **descarta os
  `PREVISTO`** da conta.
- Conta com `entraNoFluxoDeCaixa = false` não é origem de compra: nenhum meio de
  pagamento aponta para ela.
- Só meio `CREDITO` aponta para conta `CARTAO`, e todo `CREDITO` aponta para uma.
- Conta `CARTAO` nunca é compartilhada inteira — só os cartões dela
  (`docs/02-dominio/compartilhamento.md`).
- **Conta `CARTAO` nunca tem lançamento de abertura.** Ela nasce zerada, e a fatura `ABERTA`
  que nasce junto nasce vazia.
- Saldo nunca é armazenado como total; é sempre derivado dos lançamentos.

## O que ainda não existe

Escrito aqui, e **não** implementado — a Fase 1 entrou por fatias, e esta é a lista honesta do
que o código ainda não faz (`docs/04-api/endpoints-contas.md` tem o contrato do que faz).

| O que falta | Por quê |
|---|---|
| **Trocar o `tipo`** | O endpoint não expõe o campo. A regra — *não muda depois de existir lançamento* — está escrita e o código `TIPO_DE_CONTA_IMUTAVEL` está no catálogo de erros, esperando a tela que precisar dele |
| **O `a pagar` da fatura dentro do projetado** | O projetado existe e soma o `PREVISTO` até o fim do mês. Falta a segunda metade — descontar o `a pagar` das faturas que vencem até lá. **O desconto é do agregado, nunca do `saldoProjetado` de uma conta:** o sistema não sabe de qual conta você vai pagar, e afirmá-lo seria o mesmo palpite que derrubou o pagamento previsto automático. Se você já agendou, o pagamento é um `PREVISTO` daquela conta e o `a pagar` da fatura já caiu no mesmo valor — nada é contado duas vezes. Entra com o pagamento de fatura |
| **Dono e editor × leitor** | Nenhum caso de uso verifica papel. Hoje todo ambiente tem exatamente um acesso, o do dono, porque convite e compartilhamento não existem — não há leitor no sistema para barrar. A verificação entra com o convite (`docs/02-dominio/ambiente-financeiro.md`) |

## Fronteiras com outros docs

- **Conta ≠ meio de pagamento.** A conta é *de onde* o dinheiro sai; o meio é *como* ele
  saiu. As regras do meio ficam em `docs/02-dominio/meio-de-pagamento.md`.
- Como o dinheiro anda **entre** contas (transferência, aporte, resgate):
  `docs/02-dominio/lancamento.md`.
- Como o patrimônio soma as contas: `docs/02-dominio/aplicacao-patrimonio.md`.
