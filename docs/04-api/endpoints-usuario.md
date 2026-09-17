---
id: 04-api/endpoints-usuario
titulo: Endpoints de usuario e sessao
dono: cadastro, login, logout e o proprio perfil — rota, payload e erros
ler-junto: [04-api/convencoes, 04-api/erros, 02-dominio/usuario]
status: ativo
---

# Endpoints de usuário e sessão

A **família do usuário** do `04-api/convencoes.md`: as rotas que existem sem ambiente ativo. A
forma é de lá; a regra é de `docs/02-dominio/usuario.md` e `docs/01-arquitetura/seguranca.md`.

| Rota | O que faz | Sessão |
|---|---|---|
| `POST /api/v1/usuarios` | Cadastro. **Um ato, três efeitos** | não exige |
| `POST /api/v1/sessoes` | Login | não exige |
| `DELETE /api/v1/sessoes/atual` | Logout | idempotente |
| `GET /api/v1/usuarios/atual` | O próprio perfil | exige |
| `PATCH /api/v1/usuarios/atual` | Nome e avatar | exige |
| `PUT /api/v1/usuarios/atual/telegram` | Vincula o chat do Telegram | exige |
| `DELETE /api/v1/usuarios/atual/telegram` | Desfaz o vínculo | exige |
| `PUT /api/v1/usuarios/atual/senha` | Troca de senha | exige |

Nenhuma tem verbo no caminho: entrar é **criar uma sessão**, sair é **apagá-la**, e trocar a
senha é **substituir** a senha — por isso `PUT`.

**`atual` é o único usuário que a API serve.** Não existe `GET /usuarios/{id}`: ninguém lê o
perfil de ninguém, nem dentro de um ambiente compartilhado. O que o outro mostra ali é o
`autor` do lançamento, que vem no próprio lançamento.

## `POST /api/v1/usuarios` — cadastro

Aberto: não exige sessão. **Um ato com três efeitos** (`docs/02-dominio/usuario.md`): cria o
usuário — com o avatar já sorteado —, cria o *"Ambiente Pessoal"* do qual ele é dono, e cria
nele o jogo completo de categorias de sistema. Os três numa transação só.

**O cadastro não loga.** Ele não devolve cookie: quem entra é o `POST /sessoes`. Sessão que
nasce de um cadastro é uma segunda porta de autenticação, e o `ADR-0009` só tem uma.

```
POST /api/v1/usuarios
{ "nome": "Ana", "email": "ana@exemplo.com", "senha": "uma senha longa" }

201 Created
{ "id": 1, "nome": "Ana", "email": "ana@exemplo.com", "avatar": "PRISMA" }
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
`HttpOnly` (`ADR-0009`).

| Erro | Quando |
|---|---|
| `CREDENCIAIS_INVALIDAS` (401) | **E-mail inexistente e senha errada, sem distinção** — mesmo código, mesma mensagem e **mesmo tempo** |
| `MUITAS_TENTATIVAS` (429) | Bloqueio temporário, contado por conta **e** por origem |

## `DELETE /api/v1/sessoes/atual` — logout

Apaga a linha da sessão e expira o cookie. `204 No Content`.

**Idempotente:** sem cookie, com cookie inválido ou com sessão já apagada, a resposta é a
mesma `204`. Sair não é operação que possa falhar — e um `401` aqui contaria ao cliente que
aquele identificador não vale mais, que é informação que ele não precisa.

## `GET /api/v1/usuarios/atual` — o próprio perfil

```
GET /api/v1/usuarios/atual

200 OK
{
  "id": 1,
  "nome": "Ana",
  "email": "ana@exemplo.com",
  "avatar": "GATO",
  "telegramChatId": 184712993,
  "criadoEm": "2026-09-07T02:29:04Z",
  "avataresDisponiveis": ["VISOR","OLHO","GATO","CAVEIRA","DRONE","CIRCUITO","ONDA","TORRE","PRISMA","ROBO"]
}
```

**A lista de avatares vem daqui, e a tela não mantém uma segunda cópia dela** — é a mesma regra
do catálogo de tipos em `GET /contas`. A tela é dona do **desenho** de cada nome
(`docs/06-interface/direcao-visual.md`), nunca de quais nomes existem.

`telegramChatId` vem `null` quando não há vínculo — o campo se aplica e está vazio
(`04-api/convencoes.md`).

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem cookie, cookie inválido, ou sessão expirada |

## `PATCH /api/v1/usuarios/atual` — nome e avatar

```
PATCH /api/v1/usuarios/atual
{ "nome": "Ana Miras", "avatar": "DRONE" }

200 OK
{ "id": 1, "nome": "Ana Miras", "email": "ana@exemplo.com", "avatar": "DRONE", "telegramChatId": null, ... }
```

**Campo ausente não muda nada**, e `null` em `nome` ou `avatar` é `VALIDACAO`.

**O e-mail não está aqui, e mandá-lo é `VALIDACAO`** em vez de ser ignorado em silêncio: campo
ignorado faz o cliente acreditar que trocou o e-mail (`docs/02-dominio/usuario.md`).

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Nome vazio ou longo demais, `avatar` fora da lista, ou `email` no corpo |

## O Telegram é sub-recurso, e não campo do `PATCH`

```
PUT    /api/v1/usuarios/atual/telegram     { "chatId": 184712993 }   → 200 OK, o perfil
DELETE /api/v1/usuarios/atual/telegram                               → 204 No Content
```

**Por que não é um campo do `PATCH`:** o vínculo tem três estados — *não mexa*, *passa a ser
este* e *tira* —, e um `PATCH` só distingue os três se o cliente e o servidor concordarem que
`null` explícito é diferente de campo ausente. **Em JSON eles não concordam sozinhos**: a
ausência e o `null` chegam iguais do outro lado, e o que se paga por essa ambiguidade é o
vínculo do Telegram sumindo quando alguém troca só o nome. O sub-recurso não tem o problema:
`PUT` põe, `DELETE` tira, e quem não fala de Telegram não mexe nele.

`DELETE` é **idempotente**: desvincular o que não está vinculado responde `204` igual.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | `chatId` ausente, zero, ou não inteiro |
| `TELEGRAM_JA_VINCULADO` (409) | Outro usuário já declarou aquele `chat id` |

## `PUT /api/v1/usuarios/atual/senha` — troca de senha

```
PUT /api/v1/usuarios/atual/senha
{ "senhaAtual": "a de antes", "novaSenha": "uma senha longa nova" }

204 No Content
Set-Cookie: cyberbank_sessao=; Max-Age=0; ...
```

**A resposta expira o cookie**, porque a troca derruba **todas** as sessões do usuário,
inclusive a que pediu a troca (`docs/01-arquitetura/seguranca.md`). A próxima requisição é
`401`, e isso é o comportamento certo: o cliente vai para o login. A tela avisa **antes** de
enviar.

| Erro | Quando |
|---|---|
| `SENHA_ATUAL_INVALIDA` (422) | A senha atual não confere. **Conta para o mesmo bloqueio do login** |
| `VALIDACAO` (422) | `novaSenha` curta demais, vazia, ou **igual à atual** |
| `MUITAS_TENTATIVAS` (429) | Erros demais na senha atual, contados por conta |
| `NAO_AUTENTICADO` (401) | Sem sessão |

**`422` e não `401`.** Um `401` aqui diria ao cliente que a *sessão* não vale mais, e ele
mandaria a pessoa para o login no meio de uma troca de senha que nem começou. O que está
errado é um valor do corpo, e é isso que o `422` diz (`04-api/erros.md`).

## O que ainda não existe

Recuperação de senha (`ADR-0007` decidiu, e ninguém construiu), troca de e-mail, exclusão de
conta, upload de foto, listar e encerrar sessões, e os convites recebidos — estes dois últimos
têm espaço reservado na tela (`docs/06-interface/perfil.md`) e entram com o convite.
