---
id: 03-dados/catalogo-tabelas-do-cartao
titulo: Catalogo de tabelas do cartao
dono: definicao coluna a coluna de fatura, parcelamento e recorrencia, com constraints, indices e politicas
ler-junto: [03-dados/catalogo-tabelas-do-ambiente, 02-dominio/fatura-cartao]
status: ativo
---

# Catálogo de tabelas do cartão

`fatura`, `parcelamento` e `recorrencia`. As três são **da família do ambiente** — têm `ambiente_id` e política
de RLS por ambiente, como as de `docs/03-dados/catalogo-tabelas-do-ambiente.md` —, e saíram para
cá quando aquele doc passou das 300 linhas do `CONVENTIONS`.

**O corte é o assunto, e é de propósito:** é o mesmo eixo do `ADR-0008`, que dá endereço ao
código pelo nome do doc. Quem mexe no ciclo do cartão abre este; quem mexe em conta, meio ou
lançamento abre o de lá, e nenhum dos dois paga o outro.

As convenções de tipo e o padrão de RLS continuam sendo de
`docs/03-dados/catalogo-tabelas.md` e `docs/03-dados/modelo-de-dados.md`.

## `fatura` — família do ambiente (`V009`)

O recorte de um período da conta `CARTAO`. Guarda **só o que não se deriva**: em que ponto do
ciclo ela está e as datas desse ciclo (`docs/02-dominio/fatura-cartao.md`).

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE` |
| `conta_id` | `bigint` | não | — | A conta `CARTAO`. **Com** chave composta, ao contrário de `lancamento`: a fatura é sempre do mesmo ambiente do contrato |
| `competencia` | `date` | não | — | O **primeiro dia** do mês da fatura. É o eixo de ordem do ciclo, e o que torna *"a parcela k na k-ésima fatura"* uma conta em vez de uma busca |
| `data_fechamento` | `date` | não | — | Calculada no nascimento e **nunca recalculada** |
| `data_vencimento` | `date` | não | — | Idem. Mudar o ciclo vale da próxima fatura a nascer |
| `status` | `varchar(10)` | não | — | `FUTURA`, `ABERTA`, `FECHADA`. **Não existe `REABERTA`** |
| `criada_em` | `timestamptz` | não | `now()` | |

**Nenhuma coluna de total, pago, rolado ou a pagar.** Os três números são soma de lançamento,
calculada na leitura — a mesma regra do saldo de conta. Também **não há coluna de "encerrada"**,
e a ausência é regra: *"não existe encerrada como estado que impeça isso; existe o número"*
(`docs/02-dominio/fatura-pagamento.md`).

| Constraint | Protege |
|---|---|
| `uq_fatura_aberta_por_conta` | Único parcial `WHERE status = 'ABERTA'`, em `(conta_id)`. **É a invariante mais cara deste arquivo:** *"exatamente uma `ABERTA` por conta `CARTAO`"* é o que faz *"a fatura vem do status, nunca da data"* devolver **uma** resposta. Escrita como índice porque confiança na aplicação não é invariante |
| `uq_fatura_conta_competencia` | `(conta_id, competencia)`. Duas faturas do mesmo mês dariam dois destinos à mesma parcela |
| `fk_fatura_conta` | `(conta_id, ambiente_id) → (id, ambiente_id)`, `ON DELETE CASCADE`. Excluir a conta leva as faturas dela — e a conta com lançamento já foi recusada antes disso |
| `ck_fatura_status` | Os três estados |
| `ck_fatura_competencia_e_o_primeiro_dia` | `extract(day from competencia) = 1` |
| `ck_fatura_fecha_antes_de_vencer` | `data_fechamento < data_vencimento` |
| `ix_fatura_ciclo` | `(ambiente_id, status, data_vencimento)` — a varredura da rotina: fechar o que chegou na data, encerrar o que venceu |

`ENABLE` + `FORCE ROW LEVEL SECURITY`, com uma política só:

| Política | Comando | Regra |
|---|---|---|
| `fatura_do_ambiente` | `ALL` | `ambiente_id = app_ambiente_id()`, no `USING` e no `WITH CHECK` |

**Sem o `OR` do `ADR-0004`, e isso é decisão, não esquecimento** — é a mesma escolha que o
`evento` fez na `V006`. *"Quem fecha e abre a fatura de um cartão compartilhado"* está em aberto,
e *"partes da fatura"* é Fase 2. O preço está nomeado: o destino de um cartão compartilhado vê o
**lançamento** dele e não vê a fatura em que ele caiu. O `OR` entra junto com o
compartilhamento, que é quando a regra dele existir.

## `parcelamento` — família do ambiente (`V011`)

**Uma compra só, dividida em N.** A `recorrencia` — N eventos independentes — é outra tabela,
logo abaixo: quando **todas** as regras mudam por tipo, não é um tipo, são duas coisas
(`docs/02-dominio/recorrencia.md`).

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE` |
| `conta_id` · `meio_id` · `categoria_id` | `bigint` | não / não / sim | — | A conta `CARTAO`, o cartão e a categoria que as parcelas **herdam ao nascer** |
| `valor_da_compra_centavos` | `bigint` | não | — | **Guardado**, e é a única coisa aqui que se poderia derivar das parcelas — ver abaixo |
| `parcelas` | `smallint` | não | — | O N. Fechado desde o nascimento: mudar o N seria outra compra |
| `data_da_compra` | `date` | não | — | Vira a `dataEvento` de **todas** as parcelas (`ADR-0006`) |
| `descricao` | `varchar(200)` | não | — | |
| `criado_em` | `timestamptz` | não | `now()` | |

**Por que o valor da compra é guardado**, num projeto em que *nada derivado é coluna*: é ele que
torna **verificável** a invariante da soma — *"se a soma das parcelas não bate com o valor da
compra, é bug"*. Derivar o total das parcelas faria a invariante ser verdadeira por construção,
e invariante que não pode ser falsa não prova nada.

**Não há `ativa` e não há estado:** um parcelamento não é ligado nem desligado. Ele existe, e
acaba quando a última parcela é paga.

| Constraint | Protege |
|---|---|
| `fk_parcelamento_conta` · `fk_parcelamento_meio` · `fk_parcelamento_categoria` | Chaves **compostas** com `ambiente_id`: os três são do mesmo ambiente da série |
| `ck_parcelamento_parcelas` | `BETWEEN 2 AND 99`. **Uma parcela é uma compra à vista** |
| `ck_parcelamento_valor_positivo` | `> 0` |
| `ix_parcelamento_ambiente` | A lista do ambiente |

`ENABLE` + `FORCE ROW LEVEL SECURITY`, com uma política `ALL` por `ambiente_id` — **sem o `OR`
do `ADR-0004`**, como `fatura` e `evento`: o que o destino de um cartão compartilhado enxerga da
série alheia é decisão do compartilhamento, e ela está em aberto.

## `recorrencia` — família do ambiente (`V012`)

**N eventos independentes que se repetem por regra de tempo.** Não é a tabela de cima com um
`tipo`: nada aqui tem equivalente lá, a começar pela ausência de total.

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE` |
| `conta_id` · `meio_id` · `categoria_id` | `bigint` | não / não / sim | — | A conta `CARTAO`, o cartão e a categoria que a ocorrência **herda ao nascer** |
| `valor_centavos` | `bigint` | não | — | O valor **da ocorrência**. Não é total de nada |
| `periodicidade` | `varchar(20)` | não | — | Lista fechada, e hoje ela tem **um** valor: `MENSAL` |
| `dia` | `smallint` | não | — | 1 a 31. Mês que não tem o dia cobra no **último** |
| `inicio` | `date` | não | — | A partir de quando a regra vale |
| `ativa` | `boolean` | não | `true` | Cancelar **desliga**; o passado fica |
| `descricao` | `varchar(200)` | não | — | |
| `criado_em` | `timestamptz` | não | `now()` | |

**Não há `valor_total`, e a ausência é a regra:** perguntar *"quanto custa a Netflix"* só faz
sentido por ocorrência. É a diferença que separa esta tabela da de cima, onde o total é
guardado justamente para ser verificável.

**`ativa` existe aqui e não existe no `parcelamento`**, e a assimetria é a mesma diferença:
uma regra se liga e se desliga; uma compra aconteceu.

| Constraint | Protege |
|---|---|
| `fk_recorrencia_conta` · `fk_recorrencia_meio` · `fk_recorrencia_categoria` | Chaves **compostas** com `ambiente_id`: os três são do mesmo ambiente da série |
| `ck_recorrencia_periodicidade` | `IN ('MENSAL')`. Vocabulário do domínio é lista fechada no schema |
| `ck_recorrencia_dia` | `BETWEEN 1 AND 31` |
| `ck_recorrencia_valor_positivo` | `> 0` |
| `ix_recorrencia_ativa_da_conta` | Índice parcial `WHERE ativa`: é a varredura do fechamento. Por **conta**, não por meio — quem fecha é a fatura do contrato, e um contrato tem vários cartões |

Em `lancamento`, a `V012` acrescenta **`recorrencia_id`** e duas travas que são invariantes do
doc virando schema:

| Constraint | Protege |
|---|---|
| `ck_lancamento_uma_serie_so` | `parcelamento_id IS NULL OR recorrencia_id IS NULL`. Um lançamento é **parcela ou ocorrência**, nunca os dois |
| `uq_lancamento_recorrencia_por_fatura` | Índice único parcial `(recorrencia_id, fatura_id)`. É ele que cumpre *o fechamento nunca lança a mesma recorrência duas vezes na mesma fatura* quando duas rodadas da rotina correm juntas — a checagem em Java sozinha perderia a corrida |

`ENABLE` + `FORCE ROW LEVEL SECURITY`, com uma política `ALL` por `ambiente_id` — **sem o `OR`
do `ADR-0004`**, pela mesma razão do `parcelamento`.
