---
id: 04-api/endpoints-categorias
titulo: Endpoints de categorias
dono: contrato dos endpoints de categoria e regras de categorizacao
ler-junto: [02-dominio/categoria, 04-api/convencoes]
status: ativo
---

# Endpoints de categorias

Família do ambiente: tudo aqui vive sob `/api/v1/ambientes/{ambienteId}/`, porque categoria
tem `ambiente_id` (`docs/04-api/convencoes.md`). A regra é de `docs/02-dominio/categoria.md`.

## `GET /api/v1/ambientes/{ambienteId}/categorias`

A árvore do ambiente. Devolve **raízes com as filhas dentro** — a hierarquia mora no lugar, não
num campo (`docs/06-interface/direcao-visual.md`), e a árvore tem no máximo dois níveis.

Ordem: raízes por `nome`, e as filhas por `nome` dentro de cada raiz.

| Parâmetro | Valor | Para quê |
|---|---|---|
| `sistema` | `true`, `false` (padrão: `false`) | As de sistema **não aparecem por padrão**: elas nunca entram num seletor de lançamento nem em relatório por categoria. `true` é para a tela que precisa mostrar o que o ciclo usa |
| `inativas` | `true`, `false` (padrão: `false`) | Dado inativo fica escondido por padrão, e a tela diz que escondeu |

```
GET /api/v1/ambientes/1/categorias?sistema=true

200 OK
{
  "itens": [
    { "id": 15, "nome": "Transporte", "sentido": "SAIDA", "sistema": false,
      "inativa": false, "escolhivel": false,
      "filhas": [
        { "id": 16, "nome": "Gasolina", "sentido": "SAIDA", "sistema": false,
          "inativa": false, "escolhivel": true, "filhas": [] }
      ] },
    { "id": 1, "nome": "Aporte", "sentido": "ENTRADA", "sistema": true,
      "inativa": false, "escolhivel": false, "operacao": "APORTE", "filhas": [] }
  ]
}
```

| Campo | Nota |
|---|---|
| `escolhivel` | **Derivado, nunca coluna**: a categoria está ativa, a raiz dela está ativa, ela não tem filha ativa e não é de sistema (`docs/02-dominio/categoria.md`). É a regra do modelo virando estado na tela |
| `operacao` | Só vem em categoria de sistema — campo que não se aplica **não vem** |
| `filhas` | Sempre presente; `[]` numa subcategoria, que nunca tem filhas |

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `NAO_ENCONTRADO` (404) | Ambiente inexistente **ou sem acesso** — a mesma resposta para os dois |

## `POST /api/v1/ambientes/{ambienteId}/categorias`

Cria categoria **do usuário**. Quem decide se é raiz ou subcategoria é o `paiId`.

```
POST /api/v1/ambientes/1/categorias
{ "nome": "Transporte", "sentido": "SAIDA" }        raiz
{ "nome": "Gasolina",   "paiId": 15 }               subcategoria

201 Created
Location: /api/v1/ambientes/1/categorias/16
{ "id": 16, "paiId": 15, "nome": "Gasolina", "sentido": "SAIDA", "inativa": false }
```

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, no máximo 80 caracteres. Gravado sem o espaço das pontas |
| `sentido` | Obrigatório **na raiz**. Numa subcategoria ele é **herdado** — mandar um valor divergente é `422`, nunca correção silenciosa (regra 7 do `CLAUDE.md`) |
| `paiId` | Ausente é raiz. Presente, tem de apontar para uma raiz **do usuário**, no mesmo ambiente |

**Nome repetido é aceito**, inclusive entre irmãs. O doc de domínio aceita `Academia` sob
`Lazer` e `Academia` sob `Saúde` como duas categorias — são dois períodos da vida do usuário,
e é a resposta certa para "onde meu dinheiro foi".

**A resposta não traz `escolhivel`**, e é de propósito: ele é derivado da árvore inteira, não
da linha. Criar uma subcategoria muda o `escolhivel` **da raiz** — então a tela relê a árvore
depois de escrever, e um `escolhivel` calculado no vazio só a faria confiar num valor errado.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | `nome` vazio ou longo demais, `sentido` ausente na raiz ou divergente do pai |
| `CATEGORIA_PAI_INVALIDO` (409) | O `paiId` aponta para uma subcategoria — a árvore tem dois níveis |
| `CATEGORIA_DE_SISTEMA_PROTEGIDA` (409) | O `paiId` aponta para uma categoria de sistema, que nunca tem filhas |
| `NAO_ENCONTRADO` (404) | O `paiId` não existe **ou é de outro ambiente** |

## `PATCH /api/v1/ambientes/{ambienteId}/categorias/{categoriaId}`

Renomeia, inativa e reativa. Os dois campos são opcionais e **o que não vem não muda**;
mandar o corpo vazio é `422`.

```
PATCH /api/v1/ambientes/1/categorias/16
{ "nome": "Combustível" }      renomeia
{ "inativa": true }            inativa
{ "inativa": false }           reativa

200 OK
{ "id": 16, "paiId": 15, "nome": "Combustível", "sentido": "SAIDA", "inativa": false }
```

**Inativar vai por `PATCH` porque é estado, não exclusão** (`docs/04-api/convencoes.md`). E
ele grava em **uma linha, sempre**: inativar uma raiz esconde as filhas na leitura e **não
toca no `inativa` delas** — cascata na escrita destruiria quais filhas o usuário já tinha
inativado à mão, e reativar a raiz não teria como reconstruir o jogo anterior.

**Mudar o `sentido` não passa por aqui ainda.** A regra existe no domínio ("só enquanto a
categoria não tiver nenhum lançamento") e o endpoint entra quando houver lançamento para
contar.

| Erro | Quando |
|---|---|
| `VALIDACAO` (422) | Corpo sem `nome` e sem `inativa`, ou `nome` inválido |
| `CATEGORIA_DE_SISTEMA_PROTEGIDA` (409) | Categoria de sistema não se renomeia nem se inativa |
| `NAO_ENCONTRADO` (404) | Não existe **ou é de outro ambiente** |

## `DELETE /api/v1/ambientes/{ambienteId}/categorias/{categoriaId}`

Exclui no sentido restrito do domínio: o que **nunca correspondeu a nada**. Responde `204`.

Quem tem histórico não se exclui — o caminho é inativar, e apagar a linha apagaria a resposta
de "onde meu dinheiro foi" em todo mês que a usou.

| Erro | Quando |
|---|---|
| `CATEGORIA_COM_SUBCATEGORIA` (409) | A raiz ainda tem subcategoria, de qualquer estado. Excluir a raiz orfanaria a filha |
| `CATEGORIA_DE_SISTEMA_PROTEGIDA` (409) | Categoria de sistema não se exclui |
| `NAO_ENCONTRADO` (404) | Não existe **ou é de outro ambiente** |

> **`CATEGORIA_COM_LANCAMENTO` ainda não é devolvido por ninguém**, e não é esquecimento: a
> tabela `lancamento` não existe. Hoje nenhuma categoria tem lançamento, então "só se nunca
> teve lançamento" é verdade para todas. A verificação entra no `ExcluirCategoriaUseCase` — na
> aplicação, que é quem junta dois assuntos (`ADR-0010`) — no mesmo commit em que o assunto
> `lancamento` nascer.

## O que ainda não existe

A categorização automática (`docs/02-dominio/regras-categorizacao.md`) e a troca de `sentido`
de uma categoria já criada.
