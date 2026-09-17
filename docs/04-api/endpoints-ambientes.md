---
id: 04-api/endpoints-ambientes
titulo: Endpoints de ambiente
dono: a lista de ambientes do usuario e a regra do {ambienteId} nas outras rotas
ler-junto: [04-api/convencoes, 04-api/erros, 02-dominio/ambiente-financeiro]
status: ativo
---

# Endpoints de ambiente

**Cadastro, login e logout não estão mais aqui** — eles são do usuário, e moram em
`docs/04-api/endpoints-usuario.md` (`ADR-0014`). O que sobra neste doc é o ambiente: a lista
dele, e a regra que governa **todas** as rotas que carregam um `{ambienteId}`.

| Rota | O que faz |
|---|---|
| `GET /api/v1/ambientes` | Os ambientes a que o usuário tem acesso |

## `GET /api/v1/ambientes` — os ambientes do usuário

Exige sessão. Lista só os ambientes a que o usuário tem acesso, com o papel dele em cada um.

```
GET /api/v1/ambientes

200 OK
{ "itens": [ { "id": 1, "nome": "Ambiente Pessoal", "papel": "DONO" } ] }
```

Ordem: por `id`, crescente — o *Ambiente Pessoal* é sempre o primeiro, porque nasceu primeiro.
Não é paginada: a lista tem o tamanho do número de ambientes de uma pessoa.

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem cookie, cookie inválido, ou sessão expirada |

## O `{ambienteId}` das outras rotas

Toda rota da **família do ambiente** (`/api/v1/ambientes/{ambienteId}/...`) passa por um filtro
que resolve o `{ambienteId}`, valida o acesso do usuário autenticado e põe os dois no contexto
da requisição — e é esse contexto que a política de RLS lê (`ADR-0002`).

**Ambiente que não existe e ambiente que não é seu respondem a mesma coisa: `NAO_ENCONTRADO`
(404).** Um `403` ali contaria ao curioso que aquele ambiente existe, e o identificador é
sequencial (`docs/01-arquitetura/seguranca.md`).

## O que ainda não existe

Renomear ambiente, criar um segundo ambiente, convite, troca de papel, saída e exclusão. As
regras estão escritas em `docs/02-dominio/ambiente-financeiro.md`; os endpoints entram quando
a tela deles entrar — e o lugar delas na tela já está reservado
(`docs/06-interface/perfil.md`).
