---
id: 02-dominio/categoria
titulo: Categoria
dono: a arvore de categorias, o sentido, e o que acontece ao renomear, mover ou excluir
ler-junto: [02-dominio/lancamento, 02-dominio/regras-categorizacao, 02-dominio/orcamento]
status: rascunho
---

# Categoria

Categoria responde **para que serviu o dinheiro**. É o que transforma uma lista de
lançamentos em resposta à pergunta "onde meu dinheiro foi".

Toda categoria pertence a um ambiente financeiro e nunca muda de ambiente
(`docs/02-dominio/ambiente-financeiro.md`).

## Dois níveis, e só dois

A árvore tem exatamente **dois níveis**: categoria raiz e subcategoria.

```
Transporte          <- raiz
  Gasolina          <- subcategoria
  Uber
  Estacionamento
```

Profundidade livre foi descartada: o relatório vira consulta recursiva em todo lugar, e a
árvore cresce até ninguém achar a categoria certa na hora de lançar — o momento em que o
usuário está com menos paciência.

Regras da árvore:

- Subcategoria **não tem filhos**. Se algo pede um terceiro nível, ele vira duas
  subcategorias irmãs.
- O lançamento aponta para **uma** categoria, raiz ou subcategoria. Raiz só é escolhível
  se não tiver subcategorias — se tem filhos, escolher o pai é escolher "não sei qual".
- Todo relatório **agrupa pela raiz**. "Quanto gastei com transporte" é a soma das
  subcategorias mais o que estiver na própria raiz.

## Sentido

Toda categoria tem um `sentido`: `ENTRADA` ou `SAIDA`. Na hora de lançar, só aparecem as
categorias compatíveis com o sentido do lançamento.

O motivo é bobo e real: sem isso, nada impede categorizar o salário como "mercado", e o
relatório some com a diferença sem avisar.

- Subcategoria **herda** o sentido da raiz e não pode divergir dele.
- Transferência, aporte, resgate e rendimento **não têm categoria** e por isso não
  entram nessa conversa (`docs/02-dominio/lancamento.md`).

## De onde vêm as categorias

Ao criar um ambiente, ele nasce com um **conjunto inicial** de categorias — tela vazia na
primeira vez é o jeito mais rápido de fazer alguém desistir de categorizar.

Não existe categoria protegida: tudo que vem no conjunto inicial pode ser renomeado,
movido, inativado ou excluído. O sistema não depende de nenhuma categoria existir por nome.

> ☐ **A definir:** qual é o conjunto inicial. Sai das jornadas reais do RaspyBank
> (`docs/00-produto/jornadas.md`), não de uma lista genérica de app de finanças —
> categoria que ninguém usa é ruído na hora de lançar.

## Renomear, mover, excluir

O lançamento referencia a categoria por **identidade, não por nome** — então renomear é
livre e o histórico acompanha sem reescrever nada.

| Ação | Regra |
|---|---|
| Renomear | Livre. O histórico passa a exibir o nome novo |
| Mover subcategoria para outra raiz | Permitido, e **o relatório do passado muda junto** — aquele gasto passa a contar na raiz nova |
| Mudar o sentido | Só enquanto a categoria não tiver nenhum lançamento |
| Inativar | Some da lista de escolha; o histórico continua exibindo e somando normalmente |
| Excluir | Só se nunca teve lançamento. Com histórico, o caminho é inativar |

O caso do "mover" é o único que muda o passado. Foi escolhido assim porque a alternativa —
congelar a árvore depois do primeiro lançamento — deixaria o usuário preso a uma
organização que ele montou antes de entender os próprios gastos.

> ☐ **Revisitar quando o orçamento existir** (Fase 3): mover uma subcategoria muda o
> consumo de orçamento de meses já fechados. Pode ser que aí o "mover" precise valer só
> daqui pra frente (`docs/02-dominio/orcamento.md`).

## Lançamento sem categoria

É a **pendência** do glossário: não é um estado próprio, é a consulta por lançamento sem
categoria. Um lançamento pode nascer, existir e entrar no saldo sem categoria — o que ele
não faz é sumir da fila de pendências até alguém resolver.

Como a categoria é atribuída automaticamente é assunto de
`docs/02-dominio/regras-categorizacao.md`. Este doc só garante que o destino existe.

## Invariantes

- Toda categoria pertence a exatamente um ambiente e nunca muda de ambiente.
- A árvore tem no máximo dois níveis: subcategoria não tem filhos.
- Subcategoria tem o mesmo `sentido` da raiz.
- Categoria raiz com subcategorias não é escolhível num lançamento.
- Categoria com qualquer lançamento não pode ser excluída nem ter o sentido alterado.
- Categoria inativa não aparece para escolha, mas continua somando no histórico.
- Lançamento e categoria são sempre do mesmo ambiente.
