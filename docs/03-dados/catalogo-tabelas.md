---
id: 03-dados/catalogo-tabelas
titulo: Catalogo de tabelas
dono: definicao coluna a coluna de cada tabela, com constraints, indices e politicas
ler-junto: [03-dados/modelo-de-dados, 03-dados/migrations]
status: ativo
---

# Catálogo de tabelas

Coluna a coluna, como o schema **está** — não como se pretende que fique. Quem decide o
*porquê* de cada padrão é `docs/03-dados/modelo-de-dados.md`; aqui é a forma.

Convenções que valem para todas: chave `id bigint GENERATED ALWAYS AS IDENTITY`; dinheiro
`bigint` em centavos; data de domínio `date`; instante `timestamptz` em UTC; enum é `varchar`
com `CHECK`.

**A família decide a proteção** (`modelo-de-dados.md`), e ela está dita em cada seção.

| Tabela | Família | Migration |
|---|---|---|
| `usuario` | do usuário | `V001` |
| `sessao` | do usuário | `V001` |
| `ambiente` | de ligação | `V001` |
| `acesso` | de ligação | `V001` |
| `categoria` | **do ambiente** | `V002` |

## Funções de contexto

Postas pelo filtro autenticado com `SET LOCAL`, lidas pelas políticas.

| Função | Devolve |
|---|---|
| `app_usuario_id()` | `app.usuario_id`, ou **`NULL`** se a variável não foi posta |
| `app_ambiente_id()` | `app.ambiente_id`, ou **`NULL`** se a variável não foi posta |

`NULL` em vez de erro é escolha: sem contexto, toda comparação da política é falsa e **nada é
visível**. É o lado certo para errar.

## `usuario` — família do usuário

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `email` | `varchar(255)` | não | — | Identificador de login, único no sistema inteiro |
| `nome` | `varchar(120)` | não | — | Exibição; é o `autor` que o lançamento mostra |
| `senha_hash` | `varchar(255)` | não | — | Argon2id (`docs/01-arquitetura/seguranca.md`) |
| `criado_em` | `timestamptz` | não | `now()` | |

| Constraint | Protege |
|---|---|
| `uq_usuario_email` | Um e-mail, um usuário |
| `ck_usuario_email_minusculo` | `email = lower(email)` — sem isso a unicidade seria por caixa, e `Ana@x` e `ana@x` seriam duas contas |
| `ck_usuario_nome_nao_vazio` | Nome em branco |

**Sem RLS, de propósito:** o login procura o usuário **pelo e-mail antes de existir sessão**.
A proteção é a sessão — a consulta é sempre pelo usuário autenticado.

## `sessao` — família do usuário (`ADR-0009`)

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `usuario_id` | `bigint` | não | — | FK → `usuario`, `ON DELETE CASCADE` |
| `identificador_hash` | `varchar(64)` | não | — | **Hash** do identificador opaco do cookie, nunca ele |
| `criado_em` | `timestamptz` | não | `now()` | Base da expiração **absoluta** |
| `ultimo_uso_em` | `timestamptz` | não | `now()` | Base da expiração por **inatividade** |
| `expira_em` | `timestamptz` | não | — | Expiração absoluta, já calculada |
| `origem` | `varchar(200)` | sim | — | Por onde entrou. Inspecionar sessão é metade do valor do `ADR-0009` |

| Índice / constraint | Para quê |
|---|---|
| `uq_sessao_identificador` | O cookie aponta para **uma** linha |
| `ix_sessao_usuario` | "Todas as sessões deste usuário" — a consulta da troca de senha |
| `ix_sessao_expira_em` | A limpeza das expiradas |

**Guarda-se o hash, não o identificador.** Cópia do banco — backup, dump, suporte — não vira
sessão viva de ninguém. Sem RLS, pela mesma razão de `usuario`: a sessão é achada pelo cookie
**antes** de se saber quem é.

## `ambiente` — família de ligação

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `nome` | `varchar(80)` | não | — | `"Ambiente Pessoal"` no cadastro; renomeável |
| `criado_por` | `bigint` | não | — | FK → `usuario`. **Auditoria, não propriedade** — a propriedade se transfere e esta coluna não muda |
| `criado_em` | `timestamptz` | não | `now()` | |

`ENABLE` + `FORCE ROW LEVEL SECURITY`.

| Política | Comando | Regra |
|---|---|---|
| `ambiente_visivel_por_acesso` | `SELECT` | Tem acesso a ele — **ou** o criou e ele ainda não tem acesso nenhum |
| `ambiente_criado_pelo_usuario` | `INSERT` | `criado_por` é o usuário do contexto |
| `ambiente_alterado_por_acesso` | `UPDATE` | Tem acesso a ele |
| `ambiente_excluido_pelo_dono` | `DELETE` | Tem acesso com papel `DONO` |

**A segunda metade do `SELECT` é mecânica, e a janela é fechada:** `INSERT ... RETURNING`
aplica a política de `SELECT` à linha recém-inserida, e no instante em que o ambiente nasce o
acesso dele ainda não existe — a chave estrangeira exige essa ordem. Então a exceção é
exatamente *ambiente órfão, e só para quem o criou*; ela se fecha no comando seguinte da mesma
transação, e ambiente órfão fora de transação já viola a invariante *todo ambiente tem
exatamente um dono*.

## `acesso` — família de ligação

O par (usuário, ambiente) mais o papel. É a tabela que **define quem vê o quê**.

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `usuario_id` | `bigint` | não | — | FK → `usuario`, `ON DELETE CASCADE` |
| `ambiente_id` | `bigint` | não | — | FK → `ambiente`, `ON DELETE CASCADE`. **Não é a coluna da família do ambiente** — aqui ela é o alvo do acesso, não o dono do dado |
| `papel` | `varchar(20)` | não | — | `DONO`, `EDITOR`, `LEITOR` |
| `criado_em` | `timestamptz` | não | `now()` | |

| Índice / constraint | Protege |
|---|---|
| `ck_acesso_papel` | Os três papéis, e só eles |
| `uq_acesso_usuario_ambiente` | Um usuário tem **um** papel por ambiente, não vários |
| `uq_acesso_um_dono_por_ambiente` | Único parcial `WHERE papel = 'DONO'`: **no máximo um dono**. O *pelo menos um* é do domínio — não há forma declarativa de exigir linha que ainda não nasceu |
| `ix_acesso_ambiente` | "Quem tem acesso a este ambiente?", que é a subconsulta da política de `ambiente` |

`ENABLE` + `FORCE ROW LEVEL SECURITY`.

| Política | Comando | Regra |
|---|---|---|
| `acesso_do_usuario` | `SELECT` | Os próprios acessos |
| `acesso_criado_pelo_usuario` | `INSERT` | Só para si mesmo — que é o que o cadastro faz |
| `acesso_removido_pelo_usuario` | `DELETE` | Sair de um ambiente é apagar o próprio acesso |

**Não há política de `UPDATE`, e falta a de convite.** Criar acesso para *outro* usuário e
trocar papel exigem a condição *"sou dono deste ambiente"*, que lê a própria tabela `acesso` —
e política que lê a tabela que ela protege recursiona. A saída é uma função
`SECURITY DEFINER`, e ela entra **com o convite**. Até lá, o banco nega os dois.

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
