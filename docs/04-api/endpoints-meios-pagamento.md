---
id: 04-api/endpoints-meios-pagamento
titulo: Endpoints de meios de pagamento
dono: contrato dos endpoints de meio de pagamento
ler-junto: [02-dominio/meio-de-pagamento, 04-api/convencoes]
status: ativo
---

# Endpoints de meios de pagamento

Família do ambiente, sob `/api/v1/ambientes/{ambienteId}/meios-de-pagamento`. A regra é de
`docs/02-dominio/meio-de-pagamento.md`.

**Meio não é conta.** A conta é *de onde* o dinheiro sai; o meio é *como* ele saiu. Por isso o
lançamento manda o `meioId` e **não** manda a conta: a conta sai do meio, e mandar as duas
abriria a porta para elas discordarem.

**O caminho normal é `POST /contas` com a lista `meios`** — os meios nascem junto com a conta
(`docs/04-api/endpoints-contas.md`). Os endpoints desta página existem para **depois**:
acrescentar um meio que faltou, inativar o que saiu de uso, excluir o que nunca foi usado.

## `GET /api/v1/ambientes/{ambienteId}/meios-de-pagamento`

Os meios do ambiente, ordenados por `nome`.

| Parâmetro | Valor | Para quê |
|---|---|---|
| `inativos` | `true`, `false` (padrão: `false`) | Dado inativo fica escondido por padrão |

```
GET /api/v1/ambientes/1/meios-de-pagamento

200 OK
{
  "itens": [
    { "id": 1, "tipo": "PIX", "contaId": 1,
      "separaAsDuasDatas": false, "inativo": false },
    { "id": 2, "tipo": "BOLETO", "contaId": 1,
      "separaAsDuasDatas": true, "inativo": false }
  ]
}
```

**`nome` não vem, e não é omissão:** campo que não se aplica não aparece
(`docs/04-api/convencoes.md`). Só o `CREDITO` tem nome, porque ali ele é a identidade do
cartão — físico, virtual e adicional dividem o mesmo contrato. No resto, o par
`(conta, tipo)` já identifica, e a tela lê **"Nubank · Pix"**.

| Campo | Nota |
|---|---|
| `contaId` | A conta que este meio move. Sempre do **mesmo ambiente** do meio: o que atravessa ambiente é o uso, pelo vínculo, nunca a posse (`ADR-0004`) |
| `nome` | **Só no `CREDITO`**, e ali é obrigatório |
| `separaAsDuasDatas` | Derivado do tipo, e existe para a **tela**: só onde ele é `true` faz sentido pedir a data de efeito. Hoje só o `BOLETO` |

## `POST /api/v1/ambientes/{ambienteId}/meios-de-pagamento`

Acrescenta um meio a uma conta que já existe.

```
POST /api/v1/ambientes/1/meios-de-pagamento
{ "tipo": "PIX", "contaId": 1 }

201 Created
Location: /api/v1/ambientes/1/meios-de-pagamento/1
{ "id": 1, "tipo": "PIX", "contaId": 1,
  "separaAsDuasDatas": false, "inativo": false }
```

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `tipo` | sim | `DEBITO`, `PIX`, `TED`, `DESCONTO_EM_FOLHA`, `BOLETO`, `DINHEIRO`, `BENEFICIO`. **`CREDITO` ainda não** — ver *O que ainda não existe* |
| `contaId` | sim | Do mesmo ambiente, e do tipo que o meio exige |
| `nome` | **só no `CREDITO`** | Nos outros tipos, mandar nome é `422` — o sistema não guarda calado um valor que não vai usar |

**Cada tipo de meio move exatamente um tipo de conta**, e o servidor recusa o resto:

| Tipo de meio | Tipo de conta |
|---|---|
| `DEBITO`, `PIX`, `TED`, `DESCONTO_EM_FOLHA`, `BOLETO` | `CORRENTE` |
| `DINHEIRO` | `CARTEIRA` |
| `BENEFICIO` | `BENEFICIO` |
| `CREDITO` | `CARTAO` |

**Nenhum meio aponta para `APLICACAO`**: não se paga com ela, resgata-se antes
(`docs/02-dominio/aplicacao-patrimonio.md`). A lista de cada conta vem pronta em
`tiposDeMeioDisponiveis` (`docs/04-api/endpoints-contas.md`).

**Uma conta tem no máximo um meio de cada tipo**, e só `CREDITO` se repete — um contrato tem
vários cartões.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | `tipo` ou `contaId` ausentes; `nome` num tipo que não é `CREDITO`; `tipo: "CREDITO"` enquanto a fatura não existir |
| `MEIO_DUPLICADO_NA_CONTA` (409) | A conta já tem um meio desse tipo |
| `MEIO_INCOMPATIVEL_COM_CONTA` (409) | O tipo do meio não casa com o tipo da conta |
| `NAO_ENCONTRADO` (404) | `contaId` inexistente ou de outro ambiente |

## `PATCH /api/v1/ambientes/{ambienteId}/meios-de-pagamento/{meioId}`

Renomeia e inativa; os dois podem vir juntos, e o que não veio não muda.

```
PATCH /api/v1/ambientes/1/meios-de-pagamento/1
{ "inativo": true }

200 OK
{ "id": 1, "tipo": "PIX", "contaId": 1,
  "separaAsDuasDatas": false, "inativo": true }
```

`nome` só é aceito num meio `CREDITO`; nos outros a resposta é `422`.

**Meio inativo não recebe lançamento novo**, nem por captura — cartão cancelado e conta
encerrada somem da escolha, e o histórico fica.

**`tipo` e `contaId` não estão aqui.** Trocar o tipo de um meio com lançamento mudaria o
significado do histórico, e trocar a conta mudaria de qual saldo o passado saiu. O código
`TIPO_DE_MEIO_IMUTAVEL` está no catálogo esperando o endpoint que precisar dele.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Nenhum campo veio, ou `nome` inválido |
| `NAO_ENCONTRADO` (404) | Meio inexistente ou de outro ambiente |

## `DELETE /api/v1/ambientes/{ambienteId}/meios-de-pagamento/{meioId}`

Só o meio que nunca teve lançamento. Com histórico, o caminho é inativar.

```
DELETE /api/v1/ambientes/1/meios-de-pagamento/3

204 No Content
```

| Erro | Quando |
|---|---|
| `MEIO_COM_LANCAMENTO` (409) | O meio já tem lançamento |
| `NAO_ENCONTRADO` (404) | Meio inexistente ou de outro ambiente |

## O que ainda não existe

- **Meio `CREDITO`**, e com ele os cartões de um contrato: físico, virtual e adicional. Depende
  da conta `CARTAO` e da fatura. O `CHECK` da coluna `tipo` já aceita `CREDITO`.
- **Limite** — é da conta `CARTAO`, nunca do cartão, e por isso não aparece nesta página.
- **Compartilhar um cartão** com outro ambiente: a tabela `vinculo` existe e nasce vazia; a
  funcionalidade é liberada com a Fase 1 concluída (`ADR-0004`).
