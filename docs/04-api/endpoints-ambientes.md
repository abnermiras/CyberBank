---
id: 04-api/endpoints-ambientes
titulo: Endpoints de ambiente
dono: a lista de ambientes do usuario, criar e renomear ambiente, e a regra do {ambienteId} nas outras rotas
ler-junto: [04-api/convencoes, 04-api/erros, 02-dominio/ambiente-financeiro]
status: ativo
---

# Endpoints de ambiente

**Cadastro, login e logout não estão mais aqui** — eles são do usuário, e moram em
`docs/04-api/endpoints-usuario.md` (`ADR-0014`). O que sobra neste doc é o ambiente: a lista
dele, criar e renomear, e a regra que governa **todas** as rotas que carregam um `{ambienteId}`.

| Rota | O que faz |
|---|---|
| `GET /api/v1/ambientes` | Os ambientes a que o usuário tem acesso |
| `POST /api/v1/ambientes` | Cria um ambiente, e quem cria é o dono |
| `PATCH /api/v1/ambientes/{ambienteId}` | Renomeia |

## `GET /api/v1/ambientes` — os ambientes do usuário

Exige sessão. Lista só os ambientes a que o usuário tem acesso, com o papel dele em cada um.

```
GET /api/v1/ambientes

200 OK
{ "itens": [ { "id": 1, "nome": "Ambiente Pessoal", "papel": "DONO",
              "criadoEm": "2026-09-07T14:03:11Z" } ] }
```

`criadoEm` é **instante**, em UTC. O dia que a tela mostra é o de Brasília, convertido nela.

Ordem: por `id`, crescente — o *Ambiente Pessoal* é sempre o primeiro, porque nasceu primeiro.
Não é paginada: a lista tem o tamanho do número de ambientes de uma pessoa.

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem cookie, cookie inválido, ou sessão expirada |

## `POST /api/v1/ambientes` — criar

Exige sessão. Não passa pelo filtro do `{ambienteId}`: o ambiente ainda não existe.

```
POST /api/v1/ambientes
{ "nome": "Casa" }

201 Created
Location: /api/v1/ambientes/7
{ "id": 7, "nome": "Casa", "papel": "DONO", "criadoEm": "2026-09-22T16:12:40Z" }
```

No mesmo ato nascem o acesso de dono e as categorias de sistema do ambiente
(`docs/02-dominio/ambiente-financeiro.md`).

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `VALIDACAO` (422) | `nome` vazio, ou com mais de 80 caracteres |

## `PATCH /api/v1/ambientes/{ambienteId}` — renomear

```
PATCH /api/v1/ambientes/7
{ "nome": "Casa da praia" }

200 OK
{ "id": 7, "nome": "Casa da praia", "papel": "DONO", "criadoEm": "2026-09-22T16:12:40Z" }
```

O único campo é `nome`. A rota **passa pelo filtro do `{ambienteId}`** descrito abaixo, como
qualquer outra da família.

| Erro | Quando |
|---|---|
| `NAO_ENCONTRADO` (404) | O ambiente não existe, ou não é seu |
| `SEM_PERMISSAO` (403) | Seu papel é leitor |
| `VALIDACAO` (422) | `nome` vazio, ou com mais de 80 caracteres |

## O `{ambienteId}` das outras rotas

Toda rota da **família do ambiente** (`/api/v1/ambientes/{ambienteId}/...`) passa por um filtro
que resolve o `{ambienteId}`, valida o acesso do usuário autenticado e põe os dois no contexto
da requisição — e é esse contexto que a política de RLS lê (`ADR-0002`).

**Ambiente que não existe e ambiente que não é seu respondem a mesma coisa: `NAO_ENCONTRADO`
(404).** Um `403` ali contaria ao curioso que aquele ambiente existe, e o identificador é
sequencial (`docs/01-arquitetura/seguranca.md`).

## O que ainda não existe

Convite, troca de papel, saída, desligar e exclusão. As regras estão escritas em `docs/02-dominio/ambiente-financeiro.md`; os endpoints entram quando
a tela deles entrar — e o lugar delas na tela já está reservado
(`docs/06-interface/perfil.md`).
