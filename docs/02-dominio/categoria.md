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
- **Categoria de sistema também tem `sentido`**, e é por isso que cada operação existe duas
  vezes, em `ENTRADA` e em `SAIDA` — ver *As categorias de sistema*. A invariante não abre
  exceção para ninguém.

## De sistema e do usuário

Toda categoria tem um campo `sistema`, e ele separa duas coisas que não se misturam.

| | Para que serve | Quem cria | Quando |
|---|---|---|---|
| **De sistema** | Para o **sistema** conseguir lançar. O usuário nunca escolhe uma | O sistema | No ato em que o **ambiente financeiro** é criado |
| **Do usuário** | Para o usuário responder "onde meu dinheiro foi" | O usuário | Quando ele quiser |

**O sistema não cria nenhuma categoria de uso do usuário.** Nada de conjunto inicial, nada de
lista sugerida: "Moradia", "Transporte" e "Estudos" nascem com o nome que o usuário escolher, e
a nomenclatura é dele. O preço é a **tela vazia no primeiro lançamento** — aceito de propósito,
porque adivinhar a vida de alguém custa mais caro e para sempre: categoria que ninguém usa é
ruído em toda tela de lançamento, todo dia.

### O que a categoria de sistema resolve

Ela existe por uma razão só, e é estrutural. Antes dela, a pendência era *"lançamento sem
categoria **que espera uma**"* — e o "que espera uma" era uma lista de exceções (transferência,
aporte, resgate, rendimento, pagamento de fatura, rolagem, abertura) que alguém tinha que manter
em dia. Foi ela que produziu o primeiro achado do protótipo: a abertura de conta apareceu na
fila de pendências.

Com a categoria de sistema, **`categoria` vazia passa a significar uma coisa só: pendência.**
A exceção não foi corrigida — deixou de existir. É o mesmo movimento do `PROVISIONADO`
(`ADR-0006`), e o mesmo argumento que manteve o `autor` obrigatório: campo que aceita vazio
obriga toda tela a tratar o vazio.

A troca está nomeada: **sai uma lista de exceções, entra um filtro único.** Todo relatório por
categoria passa a excluir `sistema = true` — um predicado, no mesmo lugar, sempre igual.

### As regras delas

- **Nunca aparecem no seletor** de um lançamento do usuário. Ninguém categoriza o mercado como
  "Transferência".
- **Nunca entram no relatório de gasto nem no de receita por categoria.** Transferência, aporte
  e pagamento de fatura não são gasto da vida — só mudam o dinheiro de bolso.
- **Não se renomeia, não se move, não se inativa e não se exclui.** Aqui a proteção tem motivo
  nomeado: o sistema depende delas por identidade, e sem elas o ciclo não consegue lançar.
- **São sempre raiz e nunca têm filhos.** O usuário não pendura subcategoria em uma delas.
- Como qualquer categoria, **pertencem a um ambiente** e nunca mudam de ambiente. Cada ambiente
  tem o seu jogo completo.

## As categorias de sistema

São **sete operações**, e cada uma existe em **`ENTRADA` e `SAIDA`** — todas movimentam duas
contas, ou podem cair para qualquer lado. Quatorze registros por ambiente, gerados em laço.

| Operação | Como o sistema reconhece o lançamento |
|---|---|
| **Saldo de abertura** | É o lançamento de abertura da conta (`docs/02-dominio/conta.md`) |
| **Transferência** | Tem `transferenciaId` e não é nenhum dos casos abaixo |
| **Aporte** | Transferência cujo **destino** é uma conta `APLICACAO` |
| **Resgate** | Transferência cuja **origem** é uma conta `APLICACAO` |
| **Pagamento de fatura** | Tem `pagamentoDeFatura` preenchido |
| **Rolagem de fatura** | Tem `rolagemDeFatura` preenchido |
| **Rendimento** | É o lançamento de rendimento de uma aplicação |

Nenhuma foi inventada: **cada uma é uma operação que o modelo já distinguia**, por campo próprio
ou por tipo de conta. É o critério do projeto aplicado à lista — categoria de sistema nova só
existe se o sistema souber, sozinho e sem perguntar, quando usá-la.

**"Saque" ficou de fora** por esse mesmo critério. Sacar é uma transferência de uma `CORRENTE`
para uma `CARTEIRA`, e o modelo não tem nada que a distinga de outra transferência qualquer —
a categoria exigiria inventar uma regra só para ela existir. Se um dia o saque ganhar
comportamento próprio, ele entra.

O par `ENTRADA`/`SAIDA` mantém o **mesmo nome** nos dois lados: no extrato da conta corrente
lê-se "Transferência −R$ 500" e no da carteira "Transferência +R$ 500", como no extrato do
banco. São dois registros porque a invariante do `sentido` vale para toda categoria, sem
exceção nenhuma.

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

**Nada nesta tabela vale para categoria de sistema:** ela não se renomeia, não se move, não se
inativa e não se exclui. O sistema depende dela por identidade.

O caso do "mover" é o único que muda o passado. Foi escolhido assim porque a alternativa —
congelar a árvore depois do primeiro lançamento — deixaria o usuário preso a uma
organização que ele montou antes de entender os próprios gastos.

> ☐ **Revisitar quando o orçamento existir** (Fase 3): mover uma subcategoria muda o
> consumo de orçamento de meses já fechados. Pode ser que aí o "mover" precise valer só
> daqui pra frente (`docs/02-dominio/orcamento.md`).

## Lançamento sem categoria

É a **pendência** do glossário: não é um estado próprio, é a consulta `categoria IS NULL`,
sem exceção nenhuma. Um lançamento pode nascer, existir e entrar no saldo sem categoria — o
que ele não faz é sumir da fila de pendências até alguém resolver.

Só o lançamento **do usuário** chega aqui. O que o ciclo cria já nasce com a categoria de
sistema da operação dele, e por isso nunca foi trabalho pendente para ninguém.

Como a categoria é atribuída automaticamente é assunto de
`docs/02-dominio/regras-categorizacao.md`. Este doc só garante que o destino existe.

## Invariantes

- Toda categoria pertence a exatamente um ambiente e nunca muda de ambiente.
- A árvore tem no máximo dois níveis: subcategoria não tem filhos.
- Subcategoria tem o mesmo `sentido` da raiz.
- Categoria raiz com subcategorias não é escolhível num lançamento.
- Categoria com qualquer lançamento não pode ser excluída nem ter o sentido alterado.
- Categoria inativa não aparece para escolha, mas continua somando no histórico.
- Categoria de sistema não se renomeia, não se move, não se inativa e não se exclui.
- Categoria de sistema é sempre raiz e nunca tem subcategoria.
- Todo ambiente nasce com o jogo completo de categorias de sistema, no mesmo ato que o cria —
  e com **nenhuma** categoria de usuário.
- Nenhuma categoria de sistema aparece no seletor de um lançamento do usuário.
- Nenhum relatório por categoria inclui categoria de sistema.
- Lançamento e categoria são sempre do mesmo ambiente.
