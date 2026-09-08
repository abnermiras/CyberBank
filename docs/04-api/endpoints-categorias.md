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

## O que ainda não existe

Criar, renomear, inativar e excluir categoria; e a categorização automática
(`docs/02-dominio/regras-categorizacao.md`). As regras estão escritas no doc de domínio; os
endpoints entram com a tela de cadastro de categoria.
