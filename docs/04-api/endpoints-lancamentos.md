---
id: 04-api/endpoints-lancamentos
titulo: Endpoints de lançamentos
dono: contrato dos endpoints de lancamento, extrato, transferencia e estorno
ler-junto: [02-dominio/lancamento, 04-api/convencoes]
status: ativo
---

# Endpoints de lançamentos

Família do ambiente, sob `/api/v1/ambientes/{ambienteId}/lancamentos`. A regra é de
`docs/02-dominio/lancamento.md`.

**O lançamento pertence ao ambiente de quem lançou**, nunca ao da conta — a conta pode ser de
outro ambiente, emprestada por vínculo (`ADR-0004`). O `ambienteId` do caminho é quem decide, e
o corpo nunca o carrega.

## `GET /api/v1/ambientes/{ambienteId}/lancamentos`

O Extrato. **Pagina por cursor, não por página numerada.**

| Parâmetro | Valor | Para quê |
|---|---|---|
| `contaId` | id | Filtra por conta. Ausente: todo o ambiente |
| `pendentes` | `true`, `false` (padrão: `false`) | A fila de pendências, que é exatamente `categoria IS NULL` |
| `limite` | 1 a 200 (padrão: 50) | Quantos por página |
| `apos` | cursor opaco | Continua a listagem. Veio no `proximo` da página anterior |

Ordem: `dataEvento` **decrescente**, e `id` decrescente para desempatar. A chave de ordenação
é única, que é o que sustenta o cursor.

```
GET /api/v1/ambientes/1/lancamentos?limite=2

200 OK
{
  "itens": [
    { "id": 7, "contaId": 1, "meioId": 1, "categoriaId": 15, "autorId": 1,
      "sentido": "SAIDA", "valor": 30000,
      "dataEvento": "2026-09-14", "dataEfeito": "2026-09-14",
      "descricao": "Feira", "situacao": "REALIZADO", "doCiclo": false },
    { "id": 4, "contaId": 1, "categoriaId": 2, "autorId": 1,
      "sentido": "ENTRADA", "valor": 1123600,
      "dataEvento": "2026-09-16", "dataEfeito": "2026-09-16",
      "descricao": "Saldo de abertura", "situacao": "REALIZADO", "doCiclo": true }
  ],
  "proximo": "MjAyNi0wOS0xNjo0"
}
```

**`proximo` ausente significa que acabou.** O cursor é opaco de propósito: dentro dele vai a
chave de ordenação, e isso é detalhe do servidor.

| Campo | Nota |
|---|---|
| `valor` | Inteiro em centavos e **sempre positivo**. O sinal vem do `sentido` |
| `categoriaId` | Sempre presente; `null` **é** a pendência. Campo que se aplica e está vazio vem `null` |
| `meioId` | **Não vem** em transferência nem no lançamento de abertura: ali ninguém pagou nada. Campo que não se aplica não vem |
| `transferenciaId`, `estornoDeId`, `estabelecimento` | Só vêm quando existem |
| `doCiclo` | `true` no que o sistema criou sozinho. É o que a tela usa para não oferecer o botão de excluir |
| `dataEvento`, `dataEfeito` | Data de domínio: `"AAAA-MM-DD"`, dia local, **sem fuso** |

## `GET /api/v1/ambientes/{ambienteId}/lancamentos/{lancamentoId}`

O detalhe de um lançamento só — **com os nomes resolvidos**, ao contrário do extrato, que
devolve id cru. A razão é que esta rota é a que a tela de detalhe abre **por endereço**, vinda
do Diário, sem ter carregado conta nem categoria nenhuma
(`docs/06-interface/extrato.md`).

O `POST` já devolvia `Location` apontando para cá desde a fatia 3. Agora a URL responde.

```
GET /api/v1/ambientes/1/lancamentos/88

200 OK
{
  "id": 88, "sentido": "SAIDA", "valor": 15450, "descricao": "Gasolina",
  "dataEvento": "2026-09-16", "dataEfeito": "2026-09-16",
  "situacao": "REALIZADO", "doCiclo": false,
  "criadoEm": "2026-09-16T17:32:08Z",
  "estabelecimento": "POSTO IPIRANGA",
  "conta":     { "id": 3, "nome": "Nubank", "tipo": "CORRENTE" },
  "meio":      { "id": 7, "tipo": "PIX", "nome": null },
  "categoria": { "id": 22, "nome": "Gasolina",
                 "raiz": { "id": 19, "nome": "Transporte", "cor": "ARDOSIA" } },
  "autor":     { "id": 1, "nome": "Abner" },
  "estornadoPorId": 92
}
```

| Campo | Nota |
|---|---|
| `criadoEm` | **Instante**, em UTC — a hora em que o lançamento foi cadastrado. É o único campo com hora, e o extrato não o devolve |
| `conta`, `meio`, `categoria`, `autor` | Resolvidos aqui, e **só aqui**. O extrato continua com id cru: ele pagina cinquenta linhas, e cinquenta junções para mostrar o que a tela já tem em memória seria caro à toa |
| `meio.nome` | `null` fora do crédito — **só o cartão tem nome** (`docs/02-dominio/meio-de-pagamento.md`). O rótulo `Nubank · Pix` é montado pela tela, com a conta que já veio ao lado |
| `categoria.raiz` | Sempre presente quando há categoria. Numa raiz, `raiz` é ela mesma — assim a tela não precisa de dois caminhos |
| `transferencia` | `{ "id": ..., "outroLadoId": ... }`. **Só em transferência**, e nela `meio` não vem: ninguém pagou nada |
| `estornoDeId` | Este lançamento **é** o estorno de outro |
| `estornadoPorId` | Este lançamento **foi** estornado. Não existe coluna para isso: é a consulta inversa, e é o que evita o usuário achar que o estorno sumiu |

Campo que não se aplica **não vem** (`04-api/convencoes.md`).

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `NAO_ENCONTRADO` (404) | Lançamento inexistente **ou de outro ambiente** — a mesma resposta |

**O histórico não vem aqui.** *O que já aconteceu com este lançamento* é pergunta de evento, e
o dono dela é `docs/04-api/endpoints-eventos.md` — repetir o contrato de evento dentro do
lançamento seria a segunda cópia que o `CONVENTIONS` proíbe.

## `POST /api/v1/ambientes/{ambienteId}/lancamentos`

Gasto ou receita real. **A conta não vem no corpo**: ela sai do meio.

```
POST /api/v1/ambientes/1/lancamentos
{ "meioId": 1, "categoriaId": 15, "sentido": "SAIDA", "valor": 30000,
  "dataEvento": "2026-09-14", "descricao": "Feira" }

201 Created
Location: /api/v1/ambientes/1/lancamentos/7
{ "id": 7, "contaId": 1, "meioId": 1, "categoriaId": 15, ... }
```

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `meioId` | sim | Gasto e receita reais têm meio. Ativo, e de uma conta ativa |
| `categoriaId` | não | Ausente ou `null` deixa o lançamento **pendente**, o que é um estado legítimo. Precisa ser **escolhível** e **do mesmo sentido** do lançamento |
| `sentido` | sim | `ENTRADA` ou `SAIDA` |
| `valor` | sim | Centavos, positivo. Zero não é lançamento |
| `dataEvento` | sim | Quando aconteceu na vida |
| `dataEfeito` | só no boleto | **Nos meios à vista as duas datas são a mesma**, e mandar uma diferente é recusado, não corrigido em silêncio (regra 7 do `CLAUDE.md`) |
| `descricao` | sim | Até 200 caracteres |
| `estabelecimento` | não | Texto bruto da captura, antes de normalizar |

**A `situacao` não vem na requisição, ela é calculada**: `dataEfeito` no futuro nasce
`PREVISTO`; hoje ou antes nasce `REALIZADO`. É o que faz o boleto em aberto entrar no saldo
projetado sem entrar no realizado.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Valor não positivo, `dataEfeito` anterior à `dataEvento`, descrição vazia, meio ausente, ou data de efeito num meio à vista |
| `CONTA_INATIVA` (409) | A conta do meio está inativa |
| `MEIO_INATIVO` (409) | O meio está inativo |
| `CATEGORIA_NAO_ESCOLHIVEL` (409) | Categoria inativa, raiz com filha ativa, ou de sistema |
| `CATEGORIA_DE_OUTRO_SENTIDO` (409) | Categoria de `ENTRADA` num lançamento de `SAIDA`, ou o contrário |
| `NAO_ENCONTRADO` (404) | Meio de outro ambiente ou inexistente |

## `POST /api/v1/ambientes/{ambienteId}/lancamentos/transferencias`

Cria **o par**: uma `SAIDA` na origem e uma `ENTRADA` no destino, com o mesmo
`transferenciaId`. Aporte, resgate e saque são casos disto.

```
POST /api/v1/ambientes/1/lancamentos/transferencias
{ "contaDeOrigemId": 1, "contaDeDestinoId": 3, "valor": 200000,
  "dataEvento": "2026-09-15", "descricao": "Aporte de setembro" }

201 Created
{ "itens": [ { "id": 8, "contaId": 1, "sentido": "SAIDA", ... },
             { "id": 9, "contaId": 3, "sentido": "ENTRADA", ... } ] }
```

Sem meio de pagamento: o dinheiro não foi pago, mudou de lugar. A categoria é **de sistema** —
`Aporte` quando o destino é `APLICACAO`, `Resgate` quando a origem é, `Transferência` no resto
—, e por isso a transferência não entra em relatório de gasto nem na fila de pendências.

| Erro | Quando |
|---|---|
| `TRANSFERENCIA_MESMA_CONTA` (409) | Origem e destino iguais |
| `BENEFICIO_NAO_TRANSFERE` (409) | Uma das pontas é `BENEFICIO`: aquele saldo não é fungível |
| `CONTA_INATIVA` (409) | Qualquer uma das duas está inativa |
| `VALIDACAO` (422) | Valor não positivo, ou descrição vazia |

## `PATCH /api/v1/ambientes/{ambienteId}/lancamentos/{lancamentoId}`

Correção: **o registro está errado, mas descreve algo que aconteceu.** O que não veio não muda.

```
PATCH /api/v1/ambientes/1/lancamentos/7
{ "valor": 35000, "situacao": "PREVISTO" }

200 OK
{ "itens": [ { "id": 7, "valor": 35000, "situacao": "PREVISTO", ... } ] }
```

```
PATCH /api/v1/ambientes/1/lancamentos/7
{ "meioId": 9, "dataEvento": "2026-09-14" }

200 OK
{ "itens": [ { "id": 7, "meioId": 9, "contaId": 4, "dataEvento": "2026-09-14",
              "dataEfeito": "2026-09-14", ... } ] }
```

A resposta é uma **lista** porque um lado de uma transferência corrige o par inteiro — não
existe metade de transferência. Nesse caso `contaId`, `meioId`, `categoriaId`, `sentido` e
`dataEfeito` são ignorados: mudar um lado do par sozinho quebraria a soma zero, e a
transferência não tem vencimento — **`dataEvento` move as duas datas dos dois lados**.

Campos aceitos: `contaId`, `meioId`, `categoriaId`, `sentido`, `valor`, `dataEvento`,
`dataEfeito`, `descricao`, `situacao`.

**Quem manda na conta é o `meioId`, exatamente como no `POST`.** O meio já aponta para uma
conta, e é dela que o lançamento passa a ser — mandar `contaId` junto só serve para confirmar
o que o meio já diz. `contaId` divergente do meio é recusado em vez de separar os dois: um
lançamento na conta A pago por um meio da conta B não descreve nada que possa ter acontecido.

**A `dataEfeito` é recalculada pelo meio resultante** (`docs/02-dominio/meio-de-pagamento.md`).
Em meio à vista as duas datas são a mesma, então corrigir só a `dataEvento` move as duas; e
trocar um boleto por um meio à vista junta as duas no dia do evento. Só meio que separa as
duas datas aceita `dataEfeito` própria.

**Meio e conta só precisam estar ativos quando o `meioId` muda.** Corrigir a descrição de um
lançamento antigo não ressuscita a discussão sobre a conta que foi inativada desde então.

**`situacao` anda nos dois sentidos aqui, e é de propósito.** A automação só anda para frente;
a correção do usuário volta, porque ela descreve o registro, não o dinheiro — devolver a
`PREVISTO` o boleto que a data realizou e ninguém pagou é correção comum, e estornar seria pior:
inventaria um dinheiro que voltou.

| Erro | Quando |
|---|---|
| `LANCAMENTO_DO_CICLO` (409) | É lançamento que o sistema criou e a correção mandou **algo além do `valor`**. Só `{ "valor": ... }` passa: é assim que o saldo de abertura se corrige. O `DELETE` continua recusando o lançamento inteiro |
| `VALIDACAO` (422) | O resultado da correção violaria valor positivo, datas ou descrição |
| `CATEGORIA_NAO_ESCOLHIVEL` (409) | A categoria nova não é destino de lançamento |
| `CATEGORIA_DE_OUTRO_SENTIDO` (409) | A categoria nova é do outro sentido. Vale contra o sentido **resultante** da correção, não o antigo |
| `MEIO_INCOMPATIVEL_COM_CONTA` (409) | O `contaId` enviado não é a conta do meio resultante |
| `MEIO_INATIVO` (409) | O `meioId` **novo** está inativo |
| `CONTA_INATIVA` (409) | A conta do `meioId` **novo** está inativa |
| `NAO_ENCONTRADO` (404) | Lançamento inexistente ou de outro ambiente |

## `POST /api/v1/ambientes/{ambienteId}/lancamentos/{lancamentoId}/estorno`

**O dinheiro voltou de verdade**: compra cancelada, devolução, chargeback. Cria um lançamento
novo de sentido oposto, ligado ao original por `estornoDe`, e **nunca apaga o original** — o
extrato do banco mostra a compra e a devolução, e o nosso também.

```
POST /api/v1/ambientes/1/lancamentos/7/estorno
{ "dataEvento": "2026-09-18" }

201 Created
{ "id": 10, "contaId": 1, "categoriaId": 15, "sentido": "ENTRADA", "valor": 30000,
  "dataEvento": "2026-09-18", "descricao": "Estorno de Feira",
  "situacao": "REALIZADO", "estornoDeId": 7, "doCiclo": false }
```

`dataEvento` é opcional; ausente, é hoje. O estorno **herda a categoria do original**: é isso
que faz o relatório de gasto ser líquido e o estorno abater o mês em que aconteceu.

**O estorno não passa pela regra do sentido, e é a única coisa que não passa.** Ele herda a
categoria e inverte o sentido, então o dado diverge de propósito: uma categoria de `SAIDA`
carregando um lançamento de `ENTRADA`. A regra do sentido governa **o que o usuário escolhe**,
não o que fica guardado (`docs/02-dominio/categoria.md`).

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | `dataEvento` anterior à do lançamento estornado |
| `NAO_ENCONTRADO` (404) | Lançamento inexistente ou de outro ambiente |

## `DELETE /api/v1/ambientes/{ambienteId}/lancamentos/{lancamentoId}`

Exclusão no sentido restrito: **o lançamento nunca correspondeu a nada** — a duplicata, o valor
inventado, a linha lançada por engano. Excluir um lado de uma transferência **exclui o par**.

```
DELETE /api/v1/ambientes/1/lancamentos/7

204 No Content
```

Nada é recalculado, porque nada é armazenado: saldo e patrimônio são soma de lançamento, e
tirar a linha já refaz tudo que deriva dela.

| Erro | Quando |
|---|---|
| `LANCAMENTO_DO_CICLO` (409) | O que o ciclo criou não é do usuário para excluir |
| `LANCAMENTO_COM_ESTORNO` (409) | Há estorno apontando para ele, e ele ficaria órfão. Exclui-se o estorno primeiro |
| `NAO_ENCONTRADO` (404) | Lançamento inexistente ou de outro ambiente |

## O que ainda não existe

- **`fatura`, `parcelamento` e `recorrencia`** nos campos e no corpo — dependem do cartão.
- **A transição automática `PREVISTO → REALIZADO` pela data.** Hoje a situação é decidida no
  nascimento e só a correção do usuário a move; enquanto a rotina não existir, um boleto
  previsto continua previsto depois do vencimento.
- **O histórico de alteração** que `docs/02-dominio/lancamento.md` entrega pelo evento. Depende
  de `docs/02-dominio/evento.md`, que é Fase 1 e ainda não tem código.
- **Contagem total** do extrato: com cursor ela é consulta separada, e ninguém precisou dela.
