---
id: 04-api/convencoes
titulo: Convenções de API
dono: forma da URL, versionamento, nomes, formatos no JSON, paginacao e compatibilidade
ler-junto: [04-api/erros, 01-arquitetura/seguranca, 02-dominio/ambiente-financeiro]
status: ativo
---

# Convenções de API

Este doc é o dono da **forma**. O que cada endpoint faz mora em `04-api/endpoints-<agregado>`,
e a regra que ele expõe mora no domínio. Aqui só o que vale para **todos**.

O critério que decidiu quase tudo abaixo: **a borda não inventa vocabulário.** Se o domínio já
disse como uma coisa se chama e em que unidade ela vive, a API repete — traduzir na borda cria
um segundo glossário para divergir do primeiro.

## Duas famílias de rota

| Família | Forma | O que vive nela |
|---|---|---|
| **Do usuário** | `/api/v1/...` | Login, logout, o próprio perfil, a lista de ambientes a que ele tem acesso, convites recebidos |
| **Do ambiente** | `/api/v1/ambientes/{ambienteId}/...` | **Todo o resto.** Contas, meios, categorias, lançamentos, faturas, relatórios |

**O ambiente vem no caminho, e nunca do corpo nem de query** — é a exigência do `ADR-0002`
finalmente escrita. Um filtro resolve o `{ambienteId}`, valida o acesso do usuário autenticado
e só então põe o ambiente no contexto (`docs/01-arquitetura/seguranca.md`).

Se o dado tem `ambiente_id`, o endpoint dele está na segunda família. Sem exceção: um endpoint
"de conveniência" fora dela seria um caminho sem a validação, que é exatamente a falha que o
`ADR-0002` existe para impedir.

> **Exemplo literal.**
> `GET  /api/v1/ambientes`
> `GET  /api/v1/ambientes/7/contas`
> `POST /api/v1/ambientes/7/lancamentos`
> `POST /api/v1/ambientes/7/faturas/42/pagamentos`

## Versionamento

`v1` no caminho, desde o primeiro endpoint. Versão nova é para **quebra**, e quebra é rara por
construção (ver *Compatibilidade*). Não há negociação por header: um caminho, uma versão, e a
URL diz qual sem ninguém precisar inspecionar a requisição.

## Nomes

- **Recurso é substantivo no plural, em português:** `contas`, `lancamentos`, `faturas`,
  `categorias`, `meios-de-pagamento`. Em caminho, palavra composta usa hífen.
- **Campo em `camelCase`, em português:** `dataEvento`, `entraEmCaixa`, `pagamentoDeFatura`.
  São os nomes do domínio, sem tradução.
- **Enum em `MAIUSCULA`**, com o valor que o domínio usa: `PROVISIONADO`, `CARTAO`, `SAIDA`.
- **Verbo em caminho é proibido.** Ação que não é CRUD vira **sub-recurso**: fechar uma fatura
  é `POST .../faturas/42/fechamento`, não `.../fecharFatura`. Quando a ação já é um substantivo
  do domínio — fechamento, abertura, pagamento, rolagem, estorno —, o sub-recurso sai pronto.

## Formatos no JSON

| O que | Como vai | Por quê |
|---|---|---|
| **Dinheiro** | **Inteiro, em centavos.** `161060` é R$ 1.610,60 | Regra 5 do `CLAUDE.md`. Nunca decimal (arredondamento binário), nunca string formatada (a borda não decide idioma) |
| **Data de domínio** | `"2026-09-07"` — dia, sem hora e **sem fuso** | `dataEvento`, `dataEfeito`, vencimento e fechamento de fatura são **dia local**, e um fuso no JSON reintroduz a ambiguidade que a regra 5 tirou |
| **Instante** | ISO-8601 em **UTC**: `"2026-09-07T02:29:04Z"` | Carimbo de auditoria e hora em que a rotina rodou. É o único lugar onde há hora |
| **Ausente × nulo** | Campo que não se aplica **não vem**; campo que se aplica e está vazio vem `null` | `meioDePagamento` numa transferência não existe; `categoria` num lançamento pendente existe e está vazia — e é `categoria IS NULL` que define a pendência |
| **Identificador** | Número, como no banco | Não é segredo: o que protege é o `ADR-0002`, não a dificuldade de adivinhar |

## Autenticação e método

Autenticação é o **cookie de sessão** do `ADR-0009`. Nenhum header extra, nenhum token no
corpo, nada de `Authorization`.

**`GET` nunca muda estado** — e isso aqui é regra de segurança, não estilo. O `SameSite=Lax`
do cookie barra CSRF em `POST`, `PUT`, `PATCH` e `DELETE`, mas **deixa o `GET` passar**. Um
`GET` que apaga alguma coisa é uma porta que o navegador abre para qualquer site.

| Método | Uso |
|---|---|
| `GET` | Ler. Nunca muda nada |
| `POST` | Criar, e executar ação de domínio (o sub-recurso) |
| `PUT` | Substituir o recurso inteiro |
| `PATCH` | Alterar parte — é o que a **edição de lançamento** usa |
| `DELETE` | Excluir, no sentido restrito do domínio: **nunca correspondeu a nada** (`docs/02-dominio/lancamento.md`). Inativar **não** é `DELETE`; é estado, e vai por `PATCH` |

## Paginação: cursor, não página

Lista devolve um cursor opaco, e o cliente pede "os N seguintes a este ponto":

```
GET /api/v1/ambientes/7/lancamentos?limite=50
GET /api/v1/ambientes/7/lancamentos?limite=50&apos=<cursor>

{ "itens": [ ... ], "proximo": "<cursor>" }
```

`proximo` ausente significa **acabou**. O cursor é opaco de propósito: dentro dele vai a chave
de ordenação, e ela pode mudar sem quebrar cliente nenhum.

**Por que não `page`/`size`.** O Extrato é a única lista sem teto, e ela **cresce por cima**:
o lançamento novo entra no começo. Com página numerada, quem está na página 2 vê um item
**repetir ou sumir** quando chega um lançamento enquanto ele lê — e o item que some é um
lançamento de dinheiro que o usuário nunca soube que existia. O cursor não tem esse estado:
ele aponta para uma linha, não para uma contagem. De quebra, no Postgres ele fica **mais
rápido conforme a lista cresce**, enquanto `OFFSET` fica mais lento.

O que se perde, dito por inteiro: **não existe "pular para a página 7"**, e o total de itens é
consulta separada — só é pedido onde a tela mostra o total. Nenhuma tela do protótipo numera
página, e a decisão de errar aqui custaria mexer em todo endpoint de lista depois.

**A chave de ordenação é sempre única.** No extrato é `(dataEvento, id)` — só `dataEvento`
empata, e cursor sobre chave que empata pula linha.

## Ordenação e filtro

- **Cada lista tem uma ordem padrão escrita no doc do agregado**, e o cliente não a escolhe
  livremente: ordem é o que sustenta o cursor.
- **Filtro é query param com o nome do campo:** `?conta=3&de=2026-09-01&ate=2026-09-30`.
  Período usa sempre `de` e `ate`, inclusivos nos dois lados.
- **Filtro desconhecido é erro**, nunca ignorado em silêncio: filtro ignorado devolve mais
  dado do que o cliente pediu, e num app de dinheiro isso é um número errado na tela.

## Compatibilidade

**Mudança aditiva por padrão** (`docs/08-fluxos/novo-endpoint.md`). Podem entrar sem aviso:
campo novo na resposta, filtro novo opcional, endpoint novo.

São **quebra**, e exigem decisão registrada: remover ou renomear campo, mudar o tipo de um
campo, tornar obrigatório um campo que era opcional, mudar o significado de um valor de enum, e
apertar validação que antes passava.

**Cliente ignora campo que não conhece.** É o que torna o aditivo barato — e vale também para o
`erros.md`: o cliente lê o **código** do erro, nunca a mensagem.

## Invariantes

- Todo dado com `ambiente_id` é exposto **só** sob `/api/v1/ambientes/{ambienteId}/...`.
- Nenhum endpoint recebe `ambienteId` por corpo ou por query.
- Nenhum caminho contém verbo.
- Valor monetário é sempre inteiro em centavos; data de domínio nunca carrega hora ou fuso;
  instante é sempre UTC.
- `GET` não muda estado, em endpoint nenhum.
- Lista paginada devolve `itens` e `proximo`, e ordena por chave **única**.
- Filtro não reconhecido é erro, nunca silêncio.
- Erro previsível tem código no catálogo **antes** de existir no código (`04-api/erros.md`).

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Corpo de erro e catálogo de códigos | `04-api/erros` |
| O que cada endpoint faz | `04-api/endpoints-<agregado>` |
| Como a sessão viaja e por que | `ADR-0009`, `01-arquitetura/seguranca` |
| Por que o ambiente vem do contexto | `ADR-0002` |
| Roteiro de criar ou mudar endpoint | `08-fluxos/novo-endpoint` |
