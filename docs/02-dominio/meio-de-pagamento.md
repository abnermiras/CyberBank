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

Um meio tem um **tipo** e uma **conta**, e **o par `(conta, tipo)` identifica o meio** —
menos no crédito. Adicionar meio é dado; adicionar tipo é código, ver
`docs/08-fluxos/novo-meio-de-pagamento.md`.

**Todo meio aponta para uma conta.** Dinheiro aponta para uma `CARTEIRA`, vale-refeição
aponta para uma `BENEFICIO`, cartão de crédito aponta para a `CARTAO` do seu contrato. Não
existe meio órfão.

### O nome é do cartão, e só dele

**Só o meio `CREDITO` tem nome, e ali o nome é a identidade.** Um contrato tem vários cartões
— físico `****1234`, virtual `FREELANCE ****0987`, o adicional de outra pessoa —, todos
apontando para a mesma conta `CARTAO`, e o compartilhamento empresta **um cartão**, não a
conta (`docs/02-dominio/compartilhamento.md`). Sem nome não dá para distinguir qual.

**Em todo o resto o nome não existe**, porque não há o que distinguir: uma conta tem no
máximo um Pix, um débito, um boleto. O par `(conta, tipo)` já responde *"como o dinheiro
saiu"*, e a tela lê **"Nubank · PIX"** a partir dele. Nome ali produzia `PIX da Nubank` ao
lado de `PIX` — dois rótulos para a mesma coisa, e o segundo digitado à mão.

É a invariante `uq_meio_conta_tipo`: único por `(conta, tipo)`, **exceto `CREDITO`**.

### Meio se escolhe ao abrir a conta

Não há cadastro de meio à parte: **os meios são escolhidos no cadastro da conta**, e depois
acrescentados ou tirados no cartão dela (`docs/04-api/endpoints-contas.md`). Quem decide o que
está na lista é o tipo da conta, pela tabela abaixo — e é isso que faz o dinheiro ser
exclusivo sem precisar de regra própria: **`CARTEIRA` só aceita `DINHEIRO`, e nenhum outro
tipo de conta o aceita.**

## Tipos

| Tipo | Conta que ele move | `dataEfeito` | Fatura | Parcela | Captura |
|---|---|---|:--:|:--:|---|
| `DEBITO` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `CREDITO` | **`CARTAO`** | = `dataEvento` | **sim** | **sim** | notificação push |
| `PIX` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `TED` | `CORRENTE` | = `dataEvento` | não | não | notificação push |
| `DESCONTO_EM_FOLHA` | `CORRENTE` | = `dataEvento` | não | não | só manual |
| `DINHEIRO` | `CARTEIRA` | = `dataEvento` | não | não | só manual |
| `BENEFICIO` | `BENEFICIO` | = `dataEvento` | não | não | push, se o app do benefício notificar |
| `BOLETO` | `CORRENTE` | data do pagamento | não | não | OFX ou manual |

Conta `APLICACAO` não aparece nesta tabela: não se paga com ela, resgata-se antes
(`docs/02-dominio/aplicacao-patrimonio.md`).

### Três famílias de tipo, e só duas justificam regra

O critério do projeto — *tipo novo só existe se alguma **regra do sistema** mudar por causa
dele* — **não governa esta tabela**, e fingir que governa seria falso. Ela tem três famílias:

| Família | Quem | O que o tipo faz |
|---|---|---|
| **Muda regra** | `BOLETO`, `CREDITO` | Boleto separa as duas datas; crédito tem fatura e parcela |
| **Escolhe a conta** | `DINHEIRO`, `BENEFICIO` | São o que torna `CARTEIRA` e `BENEFICIO` exclusivas |
| **Vocabulário do extrato** | `DEBITO`, `PIX`, `TED`, `DESCONTO_EM_FOLHA` | Nenhuma regra muda. Existem para o extrato responder *como o dinheiro saiu* |

A terceira família **já existia**: `DEBITO` e `PIX` são idênticos nas cinco colunas desde o
primeiro dia, e ninguém tinha nomeado isso. `TED` e `DESCONTO_EM_FOLHA` entram nela.

O que a família muda no custo: tipo de vocabulário **não** ganha regra, teste de regra nem
ramo em código — se aparecer um `if` por causa de um deles fora deste módulo, é bug de
modelagem. Ele é uma linha no `CHECK` e uma opção no seletor.

### Desconto em folha: o salário entra bruto

O desconto **nunca passa pela conta**, e é aí que ele confunde. A regra é:

> **O salário entra bruto, e cada desconto é uma `SAIDA` da mesma conta.** A soma dos
> descontos com o que sobra é o líquido que o banco depositou.

O preço está nomeado e é aceito: **o extrato do app mostra o bruto onde o banco mostra o
líquido.** O **saldo** é idêntico nos dois — é só a linha da receita que difere, e é ela que
torna o desconto visível como gasto. Lançar o líquido e ainda descontar tiraria o dinheiro
duas vezes, e essa é a única leitura errada possível aqui.

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
a conta de luz que ainda vai ser paga.

**Chegando o vencimento, o sistema realiza o boleto pela data**, tenha ele sido pago ou não
(`docs/02-dominio/lancamento.md`). Quando não foi, o conserto é **corrigir o lançamento** —
devolvê-lo a `PREVISTO` com a data nova. É o único ponto do modelo em que o sistema afirma um
fato que não observou, e ele existe até a conciliação da Fase 2. Enquanto existir, o
**evento** é o que torna esse fato visível no dia em que ele acontece
(`docs/02-dominio/evento.md`).

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

**O limite é informado pelo usuário.** Captura é integração externa, e a Fase 1 não tem
nenhuma (`docs/00-produto/roadmap.md`). Ele segue a **regra 7** do `CLAUDE.md`: o sistema nunca
o corrige sozinho, e a conta `CARTAO` guarda **desde quando** aquele limite está valendo.

O sinal de que ele envelheceu não é um prazo, é um fato: **quando a dívida passa do limite
informado**, o disponível fica negativo e a tela diz que o limite pode estar desatualizado —
em vez de exibir um número que já não descreve nada.

**O limite nunca trava um lançamento.** Ele orienta, como a parte da fatura
(`docs/02-dominio/compartilhamento.md`): recusar uma compra que o emissor já aprovou seria o
app discordando do banco sobre um fato do banco.

## Débito automático

**Não é meio de pagamento.** O meio continua sendo `DEBITO`; o que "automático" descreve é
que a série se paga sozinha, sem o usuário agir — e isso é fato da **recorrência**
(`docs/02-dominio/recorrencia.md`), não do meio.

## Ciclo de vida

| Momento | Regra |
|---|---|
| Criação | Tipo e conta, escolhidos **no cadastro da conta**. Nome **só no `CREDITO`**. Tipo não muda depois |
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
- **Uma conta tem no máximo um meio de cada tipo — exceto `CREDITO`**, que tem quantos cartões
  o contrato tiver. É o que faz `(conta, tipo)` identificar o meio.
- **Só o `CREDITO` tem nome**, e nele o nome é obrigatório: é a identidade do cartão. Nos
  outros o nome não existe, e a tela lê `"<conta> · <TIPO>"`.
- Todo meio `CREDITO` aponta para uma conta `CARTAO`, e só `CREDITO` aponta para ela.
- Todo meio `BENEFICIO` aponta para uma conta `BENEFICIO`, e só ele aponta para ela.
- O limite nunca impede a criação de um lançamento.
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

## O que ainda não existe

| O que falta | Por quê |
|---|---|
| **Informar o limite depois** | O limite entra no cadastro da conta `CARTAO` e ainda não tem endpoint próprio para ser atualizado. A **marca de limite envelhecido** já existe: quando a dívida passa do limite informado, o disponível fica negativo e a borda diz que ele pode estar desatualizado |
| **Trocar o `tipo` de um meio** | O endpoint não expõe o campo; `TIPO_DE_MEIO_IMUTAVEL` está no catálogo esperando |
| **Compartilhar um cartão** | A tabela `vinculo` existe e nasce vazia (`ADR-0004`); a funcionalidade é liberada com a Fase 1 concluída |

O casamento **tipo de meio ↔ tipo de conta** da tabela acima é imposto pelo código, não pelo
banco: um `CHECK` teria que ler a outra tabela. A recusa é `MEIO_INCOMPATIVEL_COM_CONTA`.

## Ainda em aberto

- [ ] Cartão adicional exige que a pessoa tenha cadastro no sistema, ou o adicional pode ser
      só um rótulo de quem gasta?
