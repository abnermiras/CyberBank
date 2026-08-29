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
- Em **que mês** cada lançamento conta é regra de `docs/02-dominio/lancamento.md` (seção
  *Em que mês o gasto conta*): o padrão é a fatura, não a `dataEvento`.

## Sentido

Toda categoria tem um `sentido`: `ENTRADA` ou `SAIDA`. Na hora de lançar, só aparecem as
categorias compatíveis com o sentido do lançamento.

O motivo é bobo e real: sem isso, nada impede categorizar o salário como "mercado", e o
relatório some com a diferença sem avisar.

- Subcategoria **herda** o sentido da raiz e não pode divergir dele.
- Transferência, aporte, resgate e rendimento **não têm categoria** e por isso não
  entram nessa conversa (`docs/02-dominio/lancamento.md`).

## Sistêmica e do usuário

Toda categoria tem um campo `sistemica`, e ele responde de onde ela veio.

| | Quem cria | Quando |
|---|---|---|
| **Sistêmica** | O sistema | No instante em que o **ambiente financeiro** é criado |
| **Do usuário** | O usuário | Quando ele quiser — é assim que a árvore dele nasce de verdade |

O ambiente nasce com o conjunto sistêmico porque tela vazia na primeira vez é o jeito mais
rápido de fazer alguém desistir de categorizar. O que o sistema **não** faz é adivinhar a vida
de ninguém: as categorias que importam, o usuário cria.

**A categoria sistêmica não pode ser excluída.** É a única proteção que ela tem, e existe para
que todo ambiente sempre tenha para onde categorizar. Inativar já resolve o incômodo de uma
categoria que não serve — ela some da lista de escolha do mesmo jeito. Fora isso, ela é do
usuário como qualquer outra: renomear e mover são livres, e mudar o `sentido` segue valendo
enquanto ela não tiver lançamento.

`sistemica` é **origem, não privilégio**. Nenhuma regra do sistema procura categoria por nome
— transferência, aporte, resgate, rendimento e rolagem simplesmente não têm categoria. A
proteção é contra tela vazia, não contra regra quebrada.

**O preço, escrito para não ser esquecido:** um ambiente PJ nasce com categorias de vida
pessoal que ele nunca vai usar e não pode apagar, só inativar. **Revisitar se** isso incomodar
— a saída seria conjunto sistêmico por perfil de ambiente, que é modelo novo para um problema
que talvez não exista.

## O conjunto sistêmico

Onze raízes, deliberadamente poucas. Subcategoria só onde ela responde uma pergunta que a raiz
não responde.

**Saída**

| Raiz | Subcategorias |
|---|---|
| Moradia | Aluguel ou financiamento · Condomínio · Energia · Água · Internet · Gás |
| Alimentação | Mercado · Restaurante · Delivery |
| Transporte | Combustível · Aplicativo · Transporte público · Estacionamento · Manutenção |
| Saúde | Plano · Farmácia · Consultas e exames |
| Educação | — |
| Lazer | — |
| Compras | Vestuário · Casa · Eletrônicos |
| Tarifas e encargos | Tarifa bancária · Juros · Anuidade · Impostos |

**Entrada**

| Raiz | Subcategorias |
|---|---|
| Salário | — |
| Renda extra | — |
| Reembolso | — |

Três ausências que são decisão, não esquecimento:

- **Não existe "Assinaturas".** Categoria responde *para que serviu o dinheiro*, e a Netflix
  serviu para lazer. "Assinatura" descreve a **forma** de pagar, e essa forma já tem dono: a
  `Recorrencia` (`docs/02-dominio/recorrencia.md`).
- **Não existe "Outros".** Quem não acha a categoria na hora deixa o lançamento **sem
  categoria**, e ele vira **pendência** — a fila de quem volta depois. Uma raiz "Outros"
  lavaria essa fila: o lançamento sairia dela sem ninguém ter decidido nada.
- **Nada de "Investimentos" ou "Poupança".** Aporte e resgate são transferência e não têm
  categoria (`docs/02-dominio/aplicacao-patrimonio.md`).

**"Tarifas e encargos" não é enfeite de lista.** O `ADR-0003` decidiu que o sistema **nunca**
calcula juros nem pagamento mínimo, e que encargos entram como lançamento comum — lançamento
comum precisa de categoria. É a única raiz do conjunto que existe porque uma decisão escrita
a exige.

## Renomear, mover, excluir

O lançamento referencia a categoria por **identidade, não por nome** — então renomear é
livre e o histórico acompanha sem reescrever nada.

| Ação | Regra |
|---|---|
| Renomear | Livre. O histórico passa a exibir o nome novo |
| Mover subcategoria para outra raiz | Permitido, e **o relatório do passado muda junto** — aquele gasto passa a contar na raiz nova |
| Mudar o sentido | Só enquanto a categoria não tiver nenhum lançamento |
| Inativar | Some da lista de escolha; o histórico continua exibindo e somando normalmente |
| Excluir | Só se nunca teve lançamento **e** não for sistêmica. Nos dois casos o caminho é inativar |

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
- Categoria sistêmica nunca é excluída, tenha lançamento ou não.
- Todo ambiente nasce com o conjunto sistêmico, no mesmo ato que cria o ambiente.
- Lançamento e categoria são sempre do mesmo ambiente.
