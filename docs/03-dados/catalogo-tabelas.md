---
id: 03-dados/catalogo-tabelas
titulo: Catalogo de tabelas
dono: indice das tabelas e definicao coluna a coluna das familias do usuario e de ligacao
ler-junto: [03-dados/catalogo-tabelas-do-ambiente, 03-dados/catalogo-tabelas-do-cartao, 03-dados/modelo-de-dados, 03-dados/migrations]
status: ativo
---

# Catálogo de tabelas

Coluna a coluna, como o schema **está** — não como se pretende que fique. Quem decide o
*porquê* de cada padrão é `docs/03-dados/modelo-de-dados.md`; aqui é a forma.

Convenções que valem para todas: chave `id bigint GENERATED ALWAYS AS IDENTITY`; dinheiro
`bigint` em centavos; data de domínio `date`; instante `timestamptz` em UTC; enum é `varchar`
com `CHECK`.

**A família decide a proteção** (`modelo-de-dados.md`), e ela está dita em cada seção.

As tabelas **do ambiente** — as que têm `ambiente_id` — estão em
`docs/03-dados/catalogo-tabelas-do-ambiente.md`, e as do **cartão** (`fatura` e `parcelamento`)
em `docs/03-dados/catalogo-tabelas-do-cartao.md`, que saiu daquele quando ele passou das 300
linhas. Aqui ficam as do **usuário** e as de **ligação**, mais as funções de contexto que todas
as políticas leem.

| Tabela | Família | Migration |
|---|---|---|
| `usuario` | do usuário | `V001` · `V007` |
| `sessao` | do usuário | `V001` |
| `ambiente` | de ligação | `V001` |
| `acesso` | de ligação | `V001` |
| `categoria` | **do ambiente** | `V002` · `V003` |
| `conta` | **do ambiente** | `V004` · `V009` |
| `meio` | **do ambiente** | `V004` · `V005` |
| `lancamento` | **do ambiente** | `V004` · `V009` · `V011` |
| `vinculo` | de ligação | `V004` |
| `evento` | **do ambiente** | `V006` · `V008` · `V009` · `V010` · `V011` |
| `fatura` | **do ambiente** (cartão) | `V009` |
| `parcelamento` | **do ambiente** (cartão) | `V011` |

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
| `avatar` | `varchar(20)` | não | — | O **nome** de um dos dez, sorteado no cadastro (`docs/02-dominio/usuario.md`) |
| `telegram_chat_id` | `bigint` | sim | — | Declarado pela pessoa e **não verificado**. `NULL` é ausência de vínculo |
| `criado_em` | `timestamptz` | não | `now()` | |

| Constraint | Protege |
|---|---|
| `uq_usuario_email` | Um e-mail, um usuário |
| `ck_usuario_email_minusculo` | `email = lower(email)` — sem isso a unicidade seria por caixa, e `Ana@x` e `ana@x` seriam duas contas |
| `ck_usuario_nome_nao_vazio` | Nome em branco |
| `ck_usuario_avatar` | A lista fechada dos dez nomes. Enum é `varchar` com `CHECK`, nunca o tipo nativo |
| `uq_usuario_telegram_chat_id` | Um `chat id`, um usuário. Vários `NULL` **não colidem** no Postgres — é o que permite a coluna ser opcional e única ao mesmo tempo |
| `ck_usuario_telegram_chat_id_nao_zero` | Zero não é chat de ninguém |

**A `V007` acrescentou as duas colunas, e o avatar entrou em três passos:** coluna nula,
`UPDATE` sorteando um dos dez para quem já existia, e só então `SET NOT NULL`. **Nenhum
`DEFAULT` ficou na coluna** — quem sorteia é o domínio (`docs/02-dominio/usuario.md`), e um
`DEFAULT` no banco seria um segundo lugar decidindo a mesma coisa, calado.

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
| `navegador` | `varchar(200)` | sim | — | O `User-Agent` da abertura, cortado. Nulo nas sessões anteriores à `V014`. É o que torna a lista de sessões reconhecível |

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

## `vinculo` — família de ligação (`V004`)

**Nasce vazia.** Ela existe agora porque a política de `conta` e a de `meio` precisam do `OR`
do `ADR-0004` desde a primeira migration. Criar e revogar vínculo é a **funcionalidade** do
compartilhamento, liberada com a Fase 1 concluída.

| Coluna | Tipo | Nulo | Default | Nota |
|---|---|---|---|---|
| `id` | `bigint` identity | não | — | PK |
| `objeto` | `varchar(10)` | não | — | `CONTA` ou `MEIO` |
| `conta_id` | `bigint` | sim | — | Preenchido quando `objeto = 'CONTA'` |
| `meio_id` | `bigint` | sim | — | Preenchido quando `objeto = 'MEIO'` |
| `ambiente_origem_id` | `bigint` | não | — | O dono do objeto. **Não é denormalização solta**: as chaves compostas o amarram |
| `ambiente_destino_id` | `bigint` | não | — | Quem ganha o **uso** |
| `criado_por` | `bigint` | não | — | FK → `usuario` |
| `criado_em` | `timestamptz` | não | `now()` | |

| Constraint | Protege |
|---|---|
| `fk_vinculo_conta`, `fk_vinculo_meio` | `(objeto_id, ambiente_origem_id) → (id, ambiente_id)`. A origem é mesmo o ambiente do objeto |
| `ck_vinculo_objeto` | Exatamente uma das duas colunas preenchida, e casando com `objeto` |
| `ck_vinculo_ambientes_diferentes` | Emprestar para si mesmo não é compartilhar |
| `uq_vinculo_conta_destino`, `uq_vinculo_meio_destino` | Únicos parciais: *"um objeto tem no máximo um vínculo por ambiente de destino"* |
| `ix_vinculo_destino` | A subconsulta do `OR`, que roda em toda leitura de conta, meio e lançamento |
