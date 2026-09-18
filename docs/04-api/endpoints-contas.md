---
id: 04-api/endpoints-contas
titulo: Endpoints de contas
dono: contrato dos endpoints de conta e saldo
ler-junto: [02-dominio/conta, 04-api/convencoes]
status: ativo
---

# Endpoints de contas

Família do ambiente: tudo aqui vive sob `/api/v1/ambientes/{ambienteId}/`, porque conta tem
`ambiente_id` (`docs/04-api/convencoes.md`). A regra é de `docs/02-dominio/conta.md`.

**Nenhum saldo é campo guardado.** Todo número desta página é soma de lançamento, calculada na
leitura (`docs/02-dominio/conta.md`). É por isso que não existe `PUT` de saldo: corrigir o
saldo de abertura é editar o **lançamento** de abertura.

## `GET /api/v1/ambientes/{ambienteId}/contas`

As contas do ambiente, ordenadas por `nome`, com o **saldo realizado de hoje** de cada uma e
os dois agregados que a Home mostra.

| Parâmetro | Valor | Para quê |
|---|---|---|
| `inativas` | `true`, `false` (padrão: `false`) | Dado inativo fica escondido por padrão, e a tela diz que escondeu (`docs/06-interface/navegacao.md`) |

```
GET /api/v1/ambientes/1/contas

200 OK
{
  "itens": [
    { "id": 1, "nome": "Nubank", "tipo": "CORRENTE",
      "entraNoFluxoDeCaixa": true, "entraEmCaixa": true,
      "inativa": false, "saldoRealizadoCentavos": 1093600,
      "previstoAteOFimDoMesCentavos": -19900, "saldoProjetadoCentavos": 1073700,
      "tiposDeMeioDisponiveis": ["DEBITO","PIX","TED","DESCONTO_EM_FOLHA","BOLETO"] },
    { "id": 3, "nome": "Poupança", "tipo": "APLICACAO",
      "entraNoFluxoDeCaixa": false, "entraEmCaixa": false,
      "inativa": false, "saldoRealizadoCentavos": 1342400,
      "previstoAteOFimDoMesCentavos": 0, "saldoProjetadoCentavos": 1342400,
      "tiposDeMeioDisponiveis": [] }
  ],
  "emCaixaCentavos": 1093600,
  "patrimonioCentavos": 2524000,
  "previstoAte": "2026-09-30",
  "tiposDisponiveis": [
    { "tipo": "CORRENTE", "entraNoFluxoDeCaixa": true, "entraEmCaixa": true,
      "aceitaSaldoInicial": true,
      "meios": ["DEBITO","PIX","TED","DESCONTO_EM_FOLHA","BOLETO"] },
    { "tipo": "CARTEIRA", "entraNoFluxoDeCaixa": true, "entraEmCaixa": true,
      "aceitaSaldoInicial": true, "meios": ["DINHEIRO"] },
    { "tipo": "APLICACAO", "entraNoFluxoDeCaixa": false, "entraEmCaixa": false,
      "aceitaSaldoInicial": true, "meios": [] },
    { "tipo": "BENEFICIO", "entraNoFluxoDeCaixa": true, "entraEmCaixa": false,
      "aceitaSaldoInicial": true, "meios": ["BENEFICIO"] },
    { "tipo": "CARTAO", "entraNoFluxoDeCaixa": true, "entraEmCaixa": false,
      "aceitaSaldoInicial": false, "meios": ["CREDITO"] }
  ]
}
```

**`tiposDisponiveis` é o catálogo, e vem aqui de propósito:** é o que a tela de cadastro usa
para montar o seletor de tipo e as caixas de meio, **sem manter uma segunda cópia da tabela de
`docs/02-dominio/meio-de-pagamento.md` dentro do JavaScript**. Um fato, um dono, e a borda
serve o dono.

Uma conta `CARTAO` traz também **`diaVencimento`, `diasAntesFechamento`, `limiteCentavos`,
`limiteInformadoEm` e `contaPagadoraPadraoId`**; nas outras os cinco campos **não vêm**. O ciclo
e o limite são do contrato, nunca do cartão (`docs/02-dominio/meio-de-pagamento.md`), e a fatura
em si está em `docs/04-api/endpoints-faturas.md`.

| Campo | Nota |
|---|---|
| `saldoRealizadoCentavos` | Soma dos lançamentos com `situacao != PREVISTO` e `dataEfeito <= hoje`. **O teste é `!= PREVISTO`, nunca `== REALIZADO`** (`docs/02-dominio/lancamento.md`) |
| `previstoAteOFimDoMesCentavos` | Soma **com sinal** dos `PREVISTO` desta conta entre hoje e `previstoAte`. Negativo é o que falta pagar. Zero quando não há nada previsto |
| `saldoProjetadoCentavos` | `saldoRealizadoCentavos + previstoAteOFimDoMesCentavos`. Vem pronto porque **saldo é do dono da conta**, não da tela — e porque duas telas somando por conta própria é como dois números do mesmo fato passam a divergir |
| `previstoAte` | O fim do horizonte, **no envelope e não em cada conta**: é um só para a resposta inteira. A tela o usa como rótulo em vez de recalcular o fim do mês, que é conta de fuso e é do servidor (`docs/02-dominio/conta.md`) |
| `entraNoFluxoDeCaixa` | O movimento é gasto da vida? É **este campo**, e não o tipo, que o relatório de gasto consulta |
| `entraEmCaixa` | O saldo paga **qualquer coisa**? Os dois são campo e não derivação do tipo, de propósito: tipo novo no futuro só precisa responder a estas duas perguntas |
| `tiposDeMeioDisponiveis` | Quais meios cabem nesta conta. `CARTEIRA` devolve só `DINHEIRO` — **é isso que torna o dinheiro exclusivo**, sem precisar de regra própria; `APLICACAO` devolve vazio |
| `emCaixaCentavos` | Soma das contas com `entraEmCaixa = true`. O vale-refeição fica **fora**: aquele saldo só compra uma coisa |
| `patrimonioCentavos` | Soma do saldo realizado de **todas** as contas, sem exceção nenhuma |

Contas inativas entram nos dois agregados: histórico e saldo continuam existindo.

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `NAO_ENCONTRADO` (404) | Ambiente inexistente **ou sem acesso** — a mesma resposta para os dois |

## `GET /api/v1/ambientes/{ambienteId}/contas/reserva`

A tela do patrimônio (`docs/06-interface/reserva.md`): as aplicações com a **idade do valor
informado**, e os três totais que a Home já mostra.

```
GET /api/v1/ambientes/1/contas/reserva

200 OK
{
  "aplicacoes": [
    { "id": 3, "nome": "Poupança", "inativa": false,
      "saldoRealizadoCentavos": 1000000, "informadoEm": "2026-07-20",
      "diasDeIdade": 59, "desatualizada": true }
  ],
  "contasDeCaixa": [ { "id": 1, "nome": "Nubank", "tipo": "CORRENTE" } ],
  "guardadoCentavos": 1000000,
  "emCaixaCentavos": 500000,
  "patrimonioCentavos": 1500000,
  "algumaDesatualizada": true
}
```

`contasDeCaixa` são as contas **ativas** com `entraEmCaixa = true`: a outra ponta possível de um
aporte ou de um resgate. Vem daqui, e não da tela, porque *de onde o dinheiro sai para uma
aplicação* é regra de domínio — a aplicação não financia outra aplicação, e o vale-benefício não
entra porque o saldo dele não é fungível (`docs/02-dominio/conta.md`). O aporte em si é uma
transferência comum: `POST /lancamentos/transferencias`.

`informadoEm` é a `dataEvento` do último lançamento de **rendimento** da conta, ou a da
**abertura**, se nunca houve rendimento. **Ausente** quando não há nem uma nem outra — e aí
`desatualizada` é `false`: o que nunca teve valor não tem idade
(`docs/02-dominio/aplicacao-patrimonio.md`).

## `PUT /api/v1/ambientes/{ambienteId}/contas/{contaId}/valor-atual`

Quanto a aplicação **vale hoje**. O sistema não sobrescreve saldo nenhum: ele grava a
**diferença** como um lançamento de rendimento, com a data de hoje e categoria de sistema.

```
PUT /api/v1/ambientes/1/contas/3/valor-atual
{ "valorCentavos": 1012500 }

200 OK
{ "aplicacoes": [ { "id": 3, "saldoRealizadoCentavos": 1012500,
                    "informadoEm": "2026-09-17", "diasDeIdade": 0,
                    "desatualizada": false } ], ... }
```

A resposta é a **reserva inteira**, não o lançamento criado: quem chamou está olhando a tela do
patrimônio, e devolver um lançamento solto obrigaria a tela a uma segunda requisição só para
redesenhar.

**É `PUT` e é idempotente no que importa:** informar de novo o mesmo valor devolve `200` e
**não grava nada** — não houve fato. Repetir a chamada não empilha rendimento zerado.

| Erro | Quando |
|---|---|
| `CONTA_NAO_E_APLICACAO` (409) | A conta existe e não é `APLICACAO`. O saldo das outras é a soma do que se movimentou de verdade |
| `NAO_ENCONTRADO` (404) | Conta inexistente ou de outro ambiente |

## `POST /api/v1/ambientes/{ambienteId}/contas`

Abre a conta. Se vier `saldoInicial`, **nasce junto um lançamento de abertura** — saldo inicial
é um lançamento, não um campo, e é o que mantém *"saldo é a soma dos lançamentos"* verdadeiro
literalmente (`docs/02-dominio/conta.md`).

```
POST /api/v1/ambientes/1/contas
{ "nome": "Nubank", "tipo": "CORRENTE", "saldoInicial": 1123600,
  "meios": ["DEBITO", "PIX", "BOLETO"] }

201 Created
Location: /api/v1/ambientes/1/contas/1
{ "id": 1, "nome": "Nubank", "tipo": "CORRENTE",
  "entraNoFluxoDeCaixa": true, "entraEmCaixa": true, "inativa": false }
```

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `nome` | sim | Até 80 caracteres |
| `tipo` | sim | `CORRENTE`, `CARTEIRA`, `APLICACAO`, `BENEFICIO`, `CARTAO` |
| `saldoInicial` | não | Inteiro em centavos, **com sinal**: negativo abre a conta no vermelho, e o lançamento nasce `SAIDA` com valor positivo. Ausente, `null` ou `0` não criam lançamento nenhum — zero não é lançamento. **Proibido na `CARTAO`** |
| `meios` | não | Os tipos de meio que a conta passa a ter, **no mesmo ato**. Não existe cadastro de meio à parte (`docs/02-dominio/meio-de-pagamento.md`). Repetido na lista conta uma vez só; ausente ou vazio abre a conta sem meio nenhum, e ela ainda não lança nada |
| `cartoes` | não | **Só na `CARTAO`**: os nomes dos cartões do contrato — físico, virtual, adicional —, um meio `CREDITO` para cada. É lista de **nome** e não de tipo porque só o `CREDITO` tem nome, e nele o nome é a identidade |
| `diaVencimento` | **na `CARTAO`** | 1 a 31. Dia maior que o mês cai no último dia dele |
| `diasAntesFechamento` | **na `CARTAO`** | 1 a 28, dias corridos. `dataFechamento = dataVencimento − diasAntesFechamento`, o que a joga naturalmente para o mês anterior |
| `limite` | não | Só na `CARTAO`. Inteiro em centavos, positivo. **Informado pelo usuário e nunca corrigido pelo sistema** — ele passa a carregar a data de hoje como `limiteInformadoEm` (regra 7 do `CLAUDE.md`) |
| `contaPagadoraPadraoId` | não | Só na `CARTAO`, e de uma conta do mesmo ambiente. **É só o que vem preenchido** no formulário de pagamento: nada nasce dela sozinho |

**Abrir uma `CARTAO` cria junto a fatura `ABERTA` do ciclo corrente, vazia** — aquele cujo
fechamento ainda não passou. Sem ela a invariante da `ABERTA` única seria falsa até o primeiro
fechamento, e a primeira compra não teria onde cair (`docs/02-dominio/fatura-cartao.md`).

**Os dois eixos não vêm na requisição**: eles saem do `tipo` no nascimento. Deixar o cliente
escolher seria deixá-lo contradizer o modelo — uma `APLICACAO` "que entra em caixa" mentiria
na Home.

O lançamento de abertura nasce `REALIZADO`, sem meio de pagamento, com a categoria de sistema
**`Saldo de abertura`** do sentido correspondente, e marcado como **do ciclo**: o usuário
corrige o valor dele, mas não o exclui (`docs/02-dominio/lancamento.md`).

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | `nome` vazio ou longo demais; `tipo` ausente; `CARTAO` sem `diaVencimento` ou sem `diasAntesFechamento`; ciclo de fatura em conta que não é `CARTAO`; `limite` sem ser positivo |
| `CARTAO_SEM_SALDO_INICIAL` (422) | `saldoInicial` numa conta `CARTAO` — a única exceção da regra do saldo inicial |
| `CORPO_INVALIDO` (400) | `tipo` fora da lista |

## `PATCH /api/v1/ambientes/{ambienteId}/contas/{contaId}`

Renomeia e inativa. **Os dois podem vir juntos**, e o que não veio não muda.

```
PATCH /api/v1/ambientes/1/contas/1
{ "inativa": true }

200 OK
{ "id": 1, "nome": "Nubank", "tipo": "CORRENTE",
  "entraNoFluxoDeCaixa": true, "entraEmCaixa": true, "inativa": true }
```

| Campo | Nota |
|---|---|
| `nome` | Livre, a qualquer momento |
| `inativa` | **Inativar descarta os lançamentos `PREVISTO` da conta**: eles não vão acontecer, a conta saiu da sua vida. O histórico e o saldo ficam |

**`tipo` não está aqui**, e não é esquecimento: tipo não muda depois de existir lançamento —
mudaria o significado do histórico. Enquanto não houver tela para trocá-lo, a API não o expõe;
o código `TIPO_DE_CONTA_IMUTAVEL` já está no catálogo esperando esse endpoint.

Inativar **não** é `DELETE`: é estado, e vai por `PATCH` (`docs/04-api/convencoes.md`).

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Nenhum campo veio, ou `nome` inválido |
| `NAO_ENCONTRADO` (404) | Conta inexistente **ou de outro ambiente** |

## `DELETE /api/v1/ambientes/{ambienteId}/contas/{contaId}`

Exclui, no sentido restrito do domínio: **só a conta que nunca teve lançamento**. Com
histórico, o caminho é inativar.

Excluir a conta **leva junto os meios de pagamento dela** — meio órfão não existe, e um meio
sem conta não teria para onde apontar. Se algum meio tiver lançamento, a conta também tem, e a
exclusão já foi recusada antes disso.

```
DELETE /api/v1/ambientes/1/contas/4

204 No Content
```

| Erro | Quando |
|---|---|
| `CONTA_COM_LANCAMENTO` (409) | A conta já teve lançamento — inclusive o de abertura |
| `NAO_ENCONTRADO` (404) | Conta inexistente ou de outro ambiente |

## O que ainda não existe

- **Informar o limite depois de abrir a conta.** Ele entra no `POST` e ainda não tem
  sub-recurso próprio para ser atualizado; o evento `LIMITE_INFORMADO` espera esse endpoint.
- **O `a pagar` das faturas dentro do projetado.** O `saldoProjetadoCentavos` de cada conta
  **não** desconta fatura, e não vai descontar: o sistema não sabe de qual conta você vai pagar
  (`docs/02-dominio/conta.md`). O desconto é do agregado da Home, e entra com o pagamento.
- **Papel**: nenhum endpoint desta página verifica se o usuário é dono, editor ou leitor. A
  gestão de papel e o convite não existem, e todo ambiente hoje tem exatamente um acesso, que é
  o do dono. A verificação entra com o convite (`docs/02-dominio/ambiente-financeiro.md`).
