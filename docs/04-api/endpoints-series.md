---
id: 04-api/endpoints-series
titulo: Endpoints de series
dono: contrato dos endpoints de parcelamento
ler-junto: [02-dominio/recorrencia, 02-dominio/fatura-cartao, 04-api/convencoes]
status: ativo
---

# Endpoints de séries

Família do ambiente, sob `/api/v1/ambientes/{ambienteId}/` (`docs/04-api/convencoes.md`). A
regra é de `docs/02-dominio/recorrencia.md`.

**São duas coisas, e por ora só uma existe.** *Parcelamento* é **uma compra dividida em N**;
*recorrência* são **N eventos independentes** que se repetem. Todas as regras mudam entre as
duas — como nascem, como se editam, o que é cancelar, se existe total —, e por isso são duas
entidades e serão dois recursos. A recorrência é Fase 2.

## `POST /api/v1/ambientes/{ambienteId}/parcelamentos`

Compra parcelada no cartão. **É recurso próprio e não `POST /lancamentos` com um campo a
mais**: o que nasce aqui é **uma compra**, e as N parcelas são consequência dela — inverter isso
faria a tela criar dez lançamentos e depois procurar o que os amarra.

```
POST /api/v1/ambientes/1/parcelamentos
{ "meioId": 7, "categoriaId": 22, "valor": 500000, "parcelas": 3,
  "dataEvento": "2026-09-18", "descricao": "Aulas de espanhol" }

201 Created
Location: /api/v1/ambientes/1/parcelamentos/4
{ "id": 4, "contaId": 9, "meioId": 7, "categoriaId": 22,
  "valorDaCompraCentavos": 500000, "parcelas": 3,
  "dataDaCompra": "2026-09-18", "descricao": "Aulas de espanhol",
  "itens": [
    { "id": 51, "numero": 1, "valorCentavos": 166668, "faturaId": 42, "situacao": "PROVISIONADO" },
    { "id": 52, "numero": 2, "valorCentavos": 166666, "faturaId": 43, "situacao": "PROVISIONADO" },
    { "id": 53, "numero": 3, "valorCentavos": 166666, "faturaId": 44, "situacao": "PROVISIONADO" }
  ] }
```

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `meioId` | sim | Um meio **`CREDITO`**. Só o cartão tem fatura e só ele parcela |
| `valor` | sim | O **valor da compra**, inteiro em centavos. Não é o valor da parcela |
| `parcelas` | sim | De 2 a 99. **Uma parcela é uma compra à vista**, e para isso existe `POST /lancamentos` |
| `dataEvento` | sim | A data da compra. Vira a `dataEvento` de **todas** as parcelas |
| `categoriaId` | não | Ausente deixa as parcelas pendentes, como qualquer lançamento |

**As N parcelas nascem todas juntas e todas `PROVISIONADO`**, porque a compra aconteceu **uma
vez** (`ADR-0006`): quem parcelou R$ 5.000 em 10x deve R$ 5.000 hoje, e o limite já se comporta
assim. O que espalha a cobrança pelos meses é a **fatura** de cada parcela, nunca a data delas.

**A 1ª cai na `ABERTA`; a parcela *k*, na *k*-ésima fatura a partir dela — criada como `FUTURA`
na hora se ainda não existir** (`docs/02-dominio/fatura-cartao.md`).

**O centavo que sobra vai na primeira parcela**, que é o que a maioria dos emissores faz: assim
o valor do app bate com o da fatura sem ninguém corrigir nada. A soma das parcelas é **sempre**
igual ao valor da compra — se não bate, é bug.

| Erro | Quando |
|---|---|
| `MEIO_NAO_PARCELA` (409) | O meio não é `CREDITO` |
| `MEIO_INATIVO` (409) · `CONTA_INATIVA` (409) | Cartão ou contrato inativos |
| `CATEGORIA_NAO_ESCOLHIVEL` (409) · `CATEGORIA_DE_OUTRO_SENTIDO` (409) | A categoria não é destino de gasto |
| `VALIDACAO` (422) | `valor` não positivo, `parcelas` fora de 2–99, descrição vazia |

## `PATCH /api/v1/ambientes/{ambienteId}/parcelamentos/{parcelamentoId}`

**Altera todas as parcelas, sempre. O sistema não pergunta** — R$ 5.000 em 10x continua sendo
R$ 5.000, e se as parcelas divergirem o dado está errado, não flexível. *(A pergunta "só as
futuras ou o passado também?" é da **recorrência**, e só dela: fazê-la aqui seria oferecer ao
usuário a opção de deixar o próprio dado inconsistente.)*

```
PATCH /api/v1/ambientes/1/parcelamentos/4
{ "valor": 600000 }

200 OK
{ "id": 4, "valorDaCompraCentavos": 600000, "parcelas": 3, "itens": [ ... ] }
```

Campos aceitos: `valor`, `categoriaId`, `descricao`. **`parcelas` não está aqui**: mudar o N
seria outra compra.

**Mexer numa fatura já paga é permitido, e o pagamento não é tocado.** Se o total subiu, o que a
fatura voltou a dever **rola** na passagem seguinte da rotina; se caiu, sobra crédito na conta
`CARTAO` (`docs/02-dominio/fatura-pagamento.md`). Nada precisa ser recalculado: saldo é sempre
soma de lançamento.

## `DELETE /api/v1/ambientes/{ambienteId}/parcelamentos/{parcelamentoId}`

Exclui o parcelamento **e as N parcelas**, no sentido restrito do domínio: *nunca correspondeu a
nada*. `204 No Content`.

**É o único caminho**, e é por isso que ele existe: `DELETE /lancamentos/{id}` numa parcela
responde `PARCELA_ISOLADA` (409). Tirar uma parcela quebraria a soma das parcelas, e meia compra
não descreve nada que possa ter acontecido.

**Não confundir com o estorno da loja.** Quando o emissor devolve o dinheiro, ele credita o
**valor total da compra de uma vez** e as parcelas restantes seguem correndo: isso é um
`POST /lancamentos/{id}/estorno`, e o parcelamento **não é tocado** — a compra aconteceu, e é
verdade histórica (`docs/02-dominio/recorrencia.md`).

## O que ainda não existe

- **A tela de Séries**, que lista os parcelamentos vivos. Hoje a série aparece no **detalhe de
  cada parcela** (`docs/06-interface/extrato.md`): *parcela 3 de 10, da compra de R$ 5.000*.
- **`GET /parcelamentos`** — ninguém precisou dele antes da tela.
- **Recorrência**, e com ela o passo do fechamento que lança a ocorrência do ciclo (Fase 2).
- **Antecipar parcelas** — Fase 2 (`docs/00-produto/roadmap.md`).
