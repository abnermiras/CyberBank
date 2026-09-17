---
id: 03-dados/catalogo-tabelas-do-ambiente
titulo: Catalogo de tabelas do ambiente
dono: definicao coluna a coluna das tabelas com ambiente_id, com constraints, indices e politicas
ler-junto: [03-dados/catalogo-tabelas, 03-dados/modelo-de-dados]
status: ativo
---

# Catálogo de tabelas do ambiente

As tabelas que têm `ambiente_id` e política de RLS por ambiente. As famílias **do usuário** e
**de ligação** ficam em `docs/03-dados/catalogo-tabelas.md`, que também é o índice de todas.

As convenções de tipo e o padrão de RLS são de lá e de
`docs/03-dados/modelo-de-dados.md`; aqui é a forma de cada uma.

## `categoria` — família do ambiente (`V002`)

A primeira tabela de dado do ambiente. **As outras oito copiam a forma daqui.**

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE`. É a coluna da família |
| `pai_id` | `bigint` | sim | — | `NULL` é raiz |
| `nome` | `varchar(80)` | não | — | Renomear é livre; o lançamento referencia por identidade |
| `sentido` | `varchar(10)` | não | — | `ENTRADA` ou `SAIDA` |
| `sistema` | `boolean` | não | `false` | Categoria de sistema |
| `operacao` | `varchar(30)` | sim | — | Qual das sete operações. **É por aqui que o ciclo acha a dele** — nome não é identidade |
| `inativa` | `boolean` | não | `false` | Sempre ato do usuário |
| `criada_em` | `timestamptz` | não | `now()` | |
| `nivel` | `smallint` gerado | não | — | `1` se raiz, `2` se filha. Derivado de `pai_id` |
| `pai_nivel` | `smallint` gerado | não | — | Constante `1`. Existe só para a chave estrangeira abaixo |

| Constraint | Protege |
|---|---|
| `fk_categoria_pai` | `(pai_id, ambiente_id, pai_nivel) → (id, ambiente_id, nivel)`. **Uma constraint, duas invariantes:** o pai é raiz (a árvore tem dois níveis, e subcategoria não tem filho) **e** o pai é do mesmo ambiente. `MATCH SIMPLE`: com `pai_id` nulo não é cobrada, que é o caso da raiz |
| `ck_categoria_sentido` | `ENTRADA` / `SAIDA` |
| `ck_categoria_operacao` | As sete operações, e só elas |
| `ck_categoria_sistema_tem_operacao` | `sistema = (operacao IS NOT NULL)` — uma é a outra |
| `ck_categoria_sistema_e_raiz` | Categoria de sistema nunca tem pai |
| `ck_categoria_sistema_nunca_inativa` | O único pedaço de *"não se renomeia, não se move, não se inativa"* que cabe no banco; o resto é do domínio |
| `uq_categoria_sistema_por_ambiente` | Único parcial `WHERE sistema`, em `(ambiente_id, operacao, sentido)`: as **quatorze**, uma de cada |
| `ix_categoria_ambiente_pai` | A lista de categorias do ambiente, que é a consulta de toda tela de lançamento |

**Não há unicidade de nome.** Dois nomes iguais em raízes diferentes são duas categorias de
propósito (`docs/02-dominio/categoria.md`), e inativar-e-recriar depende disso.

`ENABLE` + `FORCE ROW LEVEL SECURITY`, com uma política só:

| Política | Comando | Regra |
|---|---|---|
| `categoria_do_ambiente` | `ALL` | `ambiente_id = app_ambiente_id()`, no `USING` e no `WITH CHECK` |

**Sem o `OR` do vínculo, e isso não é esquecimento.** O `ADR-0004` paga o `OR` "em toda tabela
ligada a conta", e o que o vínculo empresta é o uso de **conta** e de **meio**. Categoria não
atravessa ambiente em hipótese nenhuma — `ambiente-financeiro.md` escreve *"conta e meio podem
ser de outro ambiente… categoria, nunca"*, e o próprio `ADR-0004` manda **mascarar** a categoria
de fora. Mascarar é o oposto de enxergar: um `OR` aqui daria acesso exatamente ao que a decisão
proíbe. Ele entra nas tabelas ligadas a conta, quando elas nascerem.

## `conta` — família do ambiente (`V004`)

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE` |
| `nome` | `varchar(80)` | não | — | Renomear é livre |
| `tipo` | `varchar(20)` | não | — | `CORRENTE`, `CARTEIRA`, `APLICACAO`, `BENEFICIO`, `CARTAO` |
| `entra_no_fluxo_de_caixa` | `boolean` | não | — | Eixo 2. **Coluna, não derivação do tipo** |
| `entra_em_caixa` | `boolean` | não | — | Eixo 3 |
| `inativa` | `boolean` | não | `false` | Sempre ato do usuário |
| `criada_em` | `timestamptz` | não | `now()` | |

**Nenhuma coluna de saldo, dívida ou limite disponível**: nada derivado é coluna.

| Constraint | Protege |
|---|---|
| `ck_conta_tipo` | Os cinco tipos. `CARTAO` já está no `CHECK` embora o caso de uso ainda o recuse — assim a fatia da fatura não precisa de migration em cima de dado real |
| `ck_conta_caixa_implica_fluxo` | `entra_em_caixa → entra_no_fluxo_de_caixa`. O contrário não vale |
| `uq_conta_id_ambiente` | `(id, ambiente_id)`. Existe para `meio` e `vinculo` exigirem o mesmo ambiente por chave composta |
| `ix_conta_ambiente` | A lista de contas do ambiente |

## `meio` — família do ambiente (`V004`)

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE` |
| `nome` | `varchar(80)` | não | — | |
| `tipo` | `varchar(20)` | não | — | `DEBITO`, `CREDITO`, `PIX`, `DINHEIRO`, `BENEFICIO`, `BOLETO` |
| `conta_id` | `bigint` | não | — | Não existe meio órfão |
| `inativo` | `boolean` | não | `false` | |
| `criado_em` | `timestamptz` | não | `now()` | |

| Constraint | Protege |
|---|---|
| `fk_meio_conta` | `(conta_id, ambiente_id) → (id, ambiente_id)`. A conta é do **mesmo ambiente**: o que atravessa é o uso, pelo vínculo, nunca a posse |
| `ck_meio_tipo` | Os seis tipos |
| `ix_meio_ambiente`, `ix_meio_conta` | A lista do ambiente e os meios de uma conta |

**O casamento tipo de meio ↔ tipo de conta não está no banco**, e é decisão: ele mora em
`docs/02-dominio/meio-de-pagamento.md`, e um `CHECK` teria que ler a outra tabela.

## `lancamento` — família do ambiente (`V004`)

**Uma tabela só**, sem herança e sem coluna `tipo`.

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | O de **quem lançou**, nunca o da conta |
| `conta_id` | `bigint` | não | — | FK → `conta`. **Sem chave composta**: a conta pode ser de outro ambiente |
| `meio_id` | `bigint` | sim | — | Ausente em transferência, abertura, aporte e resgate |
| `categoria_id` | `bigint` | sim | — | `NULL` **é** a pendência |
| `autor_id` | `bigint` | não | — | FK → `usuario`. Obrigatório inclusive no que o ciclo cria |
| `sentido` | `varchar(10)` | não | — | `ENTRADA` ou `SAIDA` |
| `valor_centavos` | `bigint` | não | — | Sempre positivo; o sinal vem do `sentido` |
| `data_evento` | `date` | não | — | Dia local, sem hora e sem fuso |
| `data_efeito` | `date` | não | — | Idem |
| `descricao` | `varchar(200)` | não | — | |
| `situacao` | `varchar(20)` | não | — | `PREVISTO`, `PROVISIONADO`, `REALIZADO` |
| `transferencia_id` | `bigint` | sim | — | Amarra o par. Vem de `transferencia_id_seq` |
| `estorno_de_id` | `bigint` | sim | — | FK → `lancamento` |
| `do_ciclo` | `boolean` | não | `false` | A fronteira do excluir |
| `estabelecimento` | `varchar(200)` | sim | — | Texto bruto da captura |
| `criado_em` | `timestamptz` | não | `now()` | |

| Constraint | Protege |
|---|---|
| `fk_lancamento_categoria` | `(categoria_id, ambiente_id) → (id, ambiente_id)`. *"A categoria é sempre do mesmo ambiente do lançamento"* — a de fora é mascarada, e mascarar é o oposto de apontar |
| `ck_lancamento_valor_positivo` | `> 0`. Zero não é lançamento |
| `ck_lancamento_efeito_nao_antecede_evento` | `data_efeito >= data_evento` |
| `ck_lancamento_sentido`, `ck_lancamento_situacao` | As listas do domínio |
| `ix_lancamento_extrato` | `(ambiente_id, conta_id, data_evento, id)` — o índice que `modelo-de-dados.md` exige pelo nome |
| `ix_lancamento_pendencia` | Parcial `WHERE categoria_id IS NULL` |
| `ix_lancamento_conta_efeito` | Saldo de uma conta até uma data |
| `ix_lancamento_transferencia`, `ix_lancamento_estorno_de` | Parciais. O par inteiro, e "tem estorno?" |

`transferencia_id_seq` é uma sequência à parte: o par precisa de um identificador de grupo, e
ele é `bigint` como toda chave aqui.

**Colunas que ainda não existem**, e vêm com a fatura: `fatura_id`, `parcelamento_id`,
`recorrencia_id`, `pagamento_de_fatura` e `rolagem_de_fatura`. Coluna com `REFERENCES` para
tabela inexistente não é schema.

## RLS das tabelas da `V004`

As três do ambiente levam `ENABLE` + `FORCE`, com uma política `ALL` cada, **já com o `OR` do
`ADR-0004`** — mesmo com `vinculo` vazia.

| Política | `USING` | `WITH CHECK` |
|---|---|---|
| `conta_do_ambiente` | `ambiente_id = app_ambiente_id()` **ou** existe `vinculo` daquela conta para o ambiente ativo | `ambiente_id = app_ambiente_id()` |
| `meio_do_ambiente` | idem, pelo `meio_id` do vínculo | idem |
| `lancamento_do_ambiente` | `ambiente_id = app_ambiente_id()` **ou** existe vínculo da **conta ou do meio** do lançamento | idem |

**O `WITH CHECK` não leva o `OR`, e é a metade que importa:** o destino de um compartilhamento
**usa** a conta e nunca a altera, e lançamento nenhum nasce fora do ambiente de quem lançou.

`vinculo` é de ligação e pergunta *qual usuário*, como `acesso`:

| Política | Comando | Regra |
|---|---|---|
| `vinculo_das_duas_pontas` | `SELECT` | O usuário tem acesso ao ambiente de origem **ou** ao de destino |

Ela lê **só `acesso`**. Ler `conta` aqui recursionaria, porque a política de `conta` lê
`vinculo` — e é por isso que `ambiente_origem_id` é coluna. **Sem política de escrita**, de
propósito: ela entra com o caso de uso do compartilhamento, junto com a regra de quem pode
compartilhar.
