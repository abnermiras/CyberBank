---
id: 04-api/endpoints-ambientes
titulo: Endpoints de usuario, sessao e ambiente
dono: cadastro, login, logout e a lista de ambientes do usuario — rota, payload e erros
ler-junto: [04-api/convencoes, 04-api/erros, 02-dominio/ambiente-financeiro]
status: ativo
---

# Endpoints de usuário, sessão e ambiente

A **família do usuário** do `04-api/convencoes.md`: as rotas que existem antes de haver um
ambiente ativo. A forma (formatos, métodos, versionamento) é de lá; a regra é de
`docs/02-dominio/ambiente-financeiro.md` e `docs/01-arquitetura/seguranca.md`.

| Rota | O que faz |
|---|---|
| `POST /api/v1/usuarios` | Cadastro. **Um ato, três efeitos** |
| `POST /api/v1/sessoes` | Login |
| `DELETE /api/v1/sessoes/atual` | Logout |
| `GET /api/v1/ambientes` | Os ambientes a que o usuário tem acesso |

Nenhuma tem verbo no caminho: entrar é **criar uma sessão**, sair é **apagá-la**.

## `POST /api/v1/usuarios` — cadastro

Aberto: não exige sessão. **Um ato com três efeitos** (`ambiente-financeiro.md`): cria o
usuário, cria o *"Ambiente Pessoal"* do qual ele é dono, e cria nele o jogo completo de
categorias de sistema — as quatorze. Os três numa transação só: um usuário sem ambiente, ou um
ambiente sem as categorias, é estado que nenhuma tela sabe mostrar.

**O cadastro não loga.** Ele não devolve cookie: quem entra é o `POST /sessoes`. Sessão que
nasce de um cadastro é uma segunda porta de autenticação, e o `ADR-0009` só tem uma.

```
POST /api/v1/usuarios
{ "nome": "Ana", "email": "ana@exemplo.com", "senha": "uma senha longa" }

201 Created
{ "id": 1, "nome": "Ana", "email": "ana@exemplo.com" }
```

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Campo faltando, e-mail malformado, senha curta demais. **Todos de uma vez** |
| `EMAIL_JA_CADASTRADO` (409) | Já existe usuário com aquele e-mail |

O e-mail é **normalizado para minúsculas** antes de qualquer coisa: ele é o identificador de
login, e `Ana@x` e `ana@x` são a mesma pessoa.

## `POST /api/v1/sessoes` — login

```
POST /api/v1/sessoes
{ "email": "ana@exemplo.com", "senha": "uma senha longa" }

204 No Content
Set-Cookie: cyberbank_sessao=<opaco>; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=...
```

**Não há corpo na resposta, e o identificador não aparece nela** — ele vive só no cookie
`HttpOnly` (`ADR-0009`). Quem quer saber quem está logado usa `GET /api/v1/ambientes`.

| Erro | Quando |
|---|---|
| `CREDENCIAIS_INVALIDAS` (401) | **E-mail inexistente e senha errada, sem distinção** — mesmo código, mesma mensagem e **mesmo tempo** |
| `MUITAS_TENTATIVAS` (429) | Bloqueio temporário, contado por conta **e** por origem |

O tempo igual não é detalhe de implementação: sem ele, a diferença de milissegundos entre
"não existe" e "senha errada" entrega a lista de usuários (`docs/01-arquitetura/seguranca.md`).

## `DELETE /api/v1/sessoes/atual` — logout

Apaga a linha da sessão e expira o cookie. `204 No Content`.

**Idempotente:** sem cookie, com cookie inválido ou com sessão já apagada, a resposta é a
mesma `204`. Sair não é operação que possa falhar — e um `401` aqui contaria ao cliente que
aquele identificador não vale mais, que é informação que ele não precisa.

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
a tela deles entrar.
