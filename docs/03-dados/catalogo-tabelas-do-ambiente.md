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

## `evento` — família do ambiente (`V006`)

O registro do que aconteceu. **Só nasce e é lido**: não há `UPDATE` nem `DELETE` em lugar
nenhum, e a ausência das duas políticas é onde essa invariante para de depender de memória.

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE`. Apagar o ambiente é o único caminho pelo qual um evento some |
| `dia` | `date` | não | — | Dia local de Brasília. É por ele que o Diário agrupa |
| `instante` | `timestamptz` | não | `now()` | UTC. Carimbo de auditoria e ordem **dentro** do dia |
| `origem` | `varchar(10)` | não | — | `SISTEMA` ou `USUARIO` |
| `autor_id` | `bigint` | não | — | FK → `usuario`. No evento de sistema é o **dono do ambiente** |
| `tipo` | `varchar(40)` | não | — | Lista fechada, no `CHECK` |
| `alvo_tipo` | `varchar(20)` | sim | — | `LANCAMENTO`, `CONTA`, `MEIO` ou `CATEGORIA` |
| `alvo_id` | `bigint` | sim | — | **Sem FK**, e é de propósito: ver abaixo |
| `dados` | `jsonb` | sim | — | O punhado de valores da frase, não um espelho do objeto |

| Constraint | Protege |
|---|---|
| `ck_evento_origem` | `SISTEMA` ou `USUARIO` |
| `ck_evento_alvo` | Os dois campos do alvo, ou nenhum |
| `ck_evento_alvo_tipo`, `ck_evento_tipo` | As listas do domínio |
| `ix_evento_diario` | `(ambiente_id, dia DESC, instante DESC)` — a consulta do Diário |
| `ix_evento_alvo` | Parcial `WHERE alvo_tipo IS NOT NULL` — o histórico de um lançamento |

**`alvo_id` não tem chave estrangeira**, e é a decisão que faz a tabela funcionar:
`LANCAMENTO_EXCLUIDO` aponta para um id que **não existe mais**, e uma referência impediria de
gravar justamente o evento que mais importa. É por isso que ele é o único cujos `dados`
carregam a linha inteira — sem o alvo, a frase precisa se sustentar sozinha.

O `CHECK` de `tipo` lista **só os tipos que algum código grava hoje**. `FATURA_*`, `SERIE_*`,
`OCORRENCIA_DE_RECORRENCIA`, `ACESSO_*`, `VINCULO_*`, `VALOR_DE_APLICACAO_INFORMADO` e
`LIMITE_INFORMADO` entram por migration junto com a fatia que os produz: um `CHECK` que aceita
o que ninguém escreve não protege nada.

| Política | Comando | Regra |
|---|---|---|
| `evento_do_ambiente` | `SELECT` | `ambiente_id = app_ambiente_id()` |
| `evento_gravado_no_ambiente` | `INSERT` | `ambiente_id = app_ambiente_id()` |

**Esta é a única tabela do ambiente sem o `OR` do `ADR-0004`**, e a exceção tem data para
acabar: *"o que o destino de um compartilhamento vê no Diário do ambiente dele"* é decisão em
aberto, pós-Fase 1. Escrever agora um `OR` que a decisão contradiga depois seria migration em
cima de dado real **e** regra errada rodando no meio.

## A visão da rotina (`V006`) — `ADR-0013`

A rotina diária não tem requisição, não tem sessão e não tem ambiente: ela precisa passar por
todos. Com `app.usuario_id` e `app.ambiente_id` vazios, nenhuma política devolve linha — nem a
lista de ambientes por onde começar.

| Objeto | O que é |
|---|---|
| `ambientes_para_rotina()` | Função `SECURITY DEFINER`, `STABLE`, com `search_path` fixo. Devolve `(ambiente_id, dono_id)` e **nada mais** |
| `acesso_visivel_para_a_rotina` | Política de `SELECT` em `acesso` escrita **para o papel dono** (`TO`), limitada a `papel = 'DONO'` |

São **duas** coisas porque `SECURITY DEFINER` sozinho não bastaria: `acesso` tem
`FORCE ROW LEVEL SECURITY`, que sujeita o próprio dono às políticas, e o dono é `NOBYPASSRLS`.
O `TO` sai de `current_user` num bloco `DO`, porque o nome do papel vem do ambiente
(`docker/postgres-init`) e migration não lê variável de ambiente.

**O privilégio não é uma variável que qualquer código seta — é um papel que só a função
assume, numa consulta que devolve dois números.** Da função para fora, a rotina abre uma
transação por ambiente com o contexto do dono, e o RLS vale como numa requisição.
