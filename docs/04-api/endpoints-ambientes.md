---
id: 04-api/endpoints-ambientes
titulo: Endpoints de ambiente
dono: a lista de ambientes do usuario, criar e renomear ambiente, convite, pessoas com acesso, e a regra do {ambienteId} nas outras rotas
ler-junto: [04-api/convencoes, 04-api/erros, 02-dominio/ambiente-financeiro]
status: ativo
---

# Endpoints de ambiente

**Cadastro, login e logout não estão mais aqui** — eles são do usuário, e moram em
`docs/04-api/endpoints-usuario.md` (`ADR-0014`). O que sobra neste doc é o ambiente: a lista
dele, criar e renomear, o convite e as pessoas com acesso, e a regra que governa **todas** as
rotas que carregam um `{ambienteId}`.

| Rota | O que faz |
|---|---|
| `GET /api/v1/ambientes` | Os ambientes a que o usuário tem acesso |
| `POST /api/v1/ambientes` | Cria um ambiente, e quem cria é o dono |
| `PATCH /api/v1/ambientes/{ambienteId}` | Renomeia |
| `POST /api/v1/ambientes/{ambienteId}/convites` | O dono convida um e-mail |
| `GET /api/v1/ambientes/{ambienteId}/convites` | Os convites pendentes do ambiente, para o dono |
| `DELETE /api/v1/ambientes/{ambienteId}/convites/{conviteId}` | O dono cancela um convite pendente |
| `GET /api/v1/convites` | Os convites esperando pelo usuário |
| `POST /api/v1/convites/{conviteId}/aceite` · `/recusa` | O convidado responde |
| `GET /api/v1/ambientes/{ambienteId}/membros` | Quem tem acesso ao ambiente |
| `DELETE /api/v1/ambientes/{ambienteId}/membros/{usuarioId}` | O dono remove alguém, ou a pessoa sai |

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

**O mesmo filtro barra o leitor.** Toda requisição que não é `GET`, `HEAD` ou `OPTIONS` numa
rota da família pede um papel que altere o dado, e o leitor recebe `SEM_PERMISSAO` (403) antes
de chegar ao caso de uso. A rota que o leitor pode chamar mesmo escrevendo é **marcada no
controlador** (`@AbertoAoLeitor`) — hoje, só a de sair do ambiente. A regra é do
`docs/02-dominio/ambiente-financeiro.md`.

## `POST /api/v1/ambientes/{ambienteId}/convites` — convidar

Só o dono. O e-mail é normalizado (sem as bordas, minúsculo) e o papel é `EDITOR` (a
*autorização completa* da tela) ou `LEITOR` (*somente leitura*).

```
POST /api/v1/ambientes/1/convites
{ "email": "  Bia@Exemplo.com ", "papel": "EDITOR" }

201 Created
Location: /api/v1/ambientes/1/convites/12
{ "id": 12, "email": "bia@exemplo.com", "papel": "EDITOR", "criadoEm": "2026-10-03T19:17:02Z" }
```

**A resposta é a mesma com ou sem cadastro daquele e-mail.** O convite espera, e aparece quando
a pessoa se cadastra.

| Erro | Quando |
|---|---|
| `SEM_PERMISSAO` (403) | Quem convida não é o dono |
| `VALIDACAO` (422) | E-mail vazio, sem formato de e-mail ou longo demais; papel ausente ou `DONO` (`FORA_DA_LISTA`) |
| `JA_TEM_ACESSO` (409) | O e-mail já tem acesso ao ambiente — o do dono incluído |
| `CONVITE_JA_PENDENTE` (409) | Já há convite esperando resposta para aquele e-mail ali |

## `GET /api/v1/ambientes/{ambienteId}/convites` — os pendentes

Só o dono (`SEM_PERMISSAO` para os outros). Mesmo item do `POST`, em `itens`, do mais antigo para
o mais novo. Convite respondido não aparece.

## `DELETE /api/v1/ambientes/{ambienteId}/convites/{conviteId}` — cancelar

Só o dono, e só convite pendente. `204`. Convite de outro ambiente é `NAO_ENCONTRADO`; convite
já respondido é `CONVITE_NAO_PENDENTE` (409).

## `GET /api/v1/convites` — os que esperam por mim

Exige sessão, e **não** passa pelo filtro do `{ambienteId}`: o convidado ainda não tem acesso a
ambiente nenhum dali. Lista os pendentes para o e-mail do usuário.

```
GET /api/v1/convites

200 OK
{ "itens": [ { "id": 12, "ambienteId": 1, "ambienteNome": "Casa", "papel": "EDITOR",
              "convidadoPor": "Abner", "criadoEm": "2026-10-03T19:17:02Z" } ] }
```

`ambienteNome` e `convidadoPor` são o que a pessoa precisa para decidir — e **é tudo o que ela
lê do ambiente** antes de aceitar.

## `POST /api/v1/convites/{conviteId}/aceite` · `/recusa` — responder

Sem corpo. O aceite cria o acesso e devolve o ambiente como o `GET /ambientes` o mostraria; a
recusa devolve `204`.

```
POST /api/v1/convites/12/aceite

200 OK
{ "id": 1, "nome": "Casa", "papel": "EDITOR", "criadoEm": "2026-09-07T14:03:11Z" }
```

| Erro | Quando |
|---|---|
| `NAO_ENCONTRADO` (404) | O convite não existe, ou é para **outro** e-mail — os dois respondem igual |
| `CONVITE_NAO_PENDENTE` (409) | Já foi aceito, recusado ou cancelado |
| `JA_TEM_ACESSO` (409) | Aceite de um ambiente em que a pessoa já está |

## `GET /api/v1/ambientes/{ambienteId}/membros` — quem tem acesso

Qualquer papel. Na ordem de entrada, o dono primeiro porque chegou primeiro.

```
GET /api/v1/ambientes/1/membros

200 OK
{ "itens": [
    { "usuarioId": 1, "nome": "Abner", "email": "abner@exemplo.com", "avatar": "OLHO",
      "papel": "DONO", "desde": "2026-09-07T14:03:11Z", "voce": true },
    { "usuarioId": 9, "nome": "Bia", "email": "bia@exemplo.com", "avatar": "GATO",
      "papel": "EDITOR", "desde": "2026-10-03T19:20:44Z", "voce": false } ] }
```

## `DELETE /api/v1/ambientes/{ambienteId}/membros/{usuarioId}` — remover ou sair

Com o **próprio** id, é sair — e é a rota aberta ao leitor. Com o id de outra pessoa, só o dono.
`204`. O que a pessoa lançou fica no ambiente.

| Erro | Quando |
|---|---|
| `NAO_ENCONTRADO` (404) | A pessoa não tem acesso a este ambiente |
| `SEM_PERMISSAO` (403) | Quem remove outra pessoa não é o dono |
| `DONO_NAO_SAI` (409) | O alvo é o dono — ele mesmo saindo, ou alguém tentando tirá-lo |

## O que ainda não existe

Troca de papel, transferência de propriedade, desligar e exclusão. As regras estão escritas em
`docs/02-dominio/ambiente-financeiro.md`, e a troca de papel tem hoje um caminho mais longo:
remover e convidar de novo.
