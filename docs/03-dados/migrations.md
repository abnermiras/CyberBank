---
id: 03-dados/migrations
titulo: Migrations
dono: ferramenta, numeracao e nomes, o que nunca muda depois de aplicado, e como fazer mudanca destrutiva
ler-junto: [03-dados/modelo-de-dados, 03-dados/catalogo-tabelas, 08-fluxos/nova-migration]
status: ativo
---

# Migrations

**O schema é o que as migrations dizem, e só isso.** `ddl-auto` fica em `validate`, nunca em
`update`: schema gerado por ORM é schema que ninguém revisou e que difere entre a máquina do
dev e o Raspberry Pi.

## Ferramenta: Flyway, com SQL puro

Arquivos em `src/main/resources/db/migration`, e cada um é **SQL do Postgres**, sem camada de
abstração.

**Por que não Liquibase.** O que ele oferece de diferente é ser agnóstico de banco, com o
changelog em XML ou YAML — e isso aqui **não vale nada**: o `ADR-0002` exige política de RLS,
`FORCE ROW LEVEL SECURITY` e `current_setting`, que são SQL do Postgres e nada mais. Escrever
Postgres específico dentro de um formato feito para não ser específico é pagar a abstração e
não usá-la. Trocar de banco não está em fase nenhuma; se um dia estiver, a conversão do SQL é
o menor dos problemas.

## Nome e numeração

```
V<numero>__<verbo>_<alvo>.sql

V001__cria_usuario_e_sessao.sql
V002__cria_ambiente_acesso_e_convite.sql
V014__adiciona_entra_em_caixa_em_conta.sql
```

- Número **sequencial de três dígitos**, nunca reaproveitado — igual às ADRs, e pela mesma
  razão: número reaproveitado quebra a conversa sobre o histórico.
- Descrição em **português**, com verbo na frente, `snake_case`. O nome tem que responder *o
  que mudou* sem abrir o arquivo.
- **Uma migration por mudança coerente.** Criar tabela com as suas constraints, índices e
  política de RLS é **uma** mudança, não quatro.

## O que nunca muda depois de aplicada

**Migration aplicada é imutável.** Nem para corrigir um erro de digitação, nem antes do push.
Corrige-se com uma migration nova, e o erro fica visível no histórico — que é o ponto.

O Flyway guarda o *checksum* de cada arquivo aplicado e recusa subir se ele mudou. **Esse erro
não se contorna** apagando a linha da tabela de controle: se ele apareceu, ou alguém editou
uma migration, ou o banco não é o que se pensava. As duas exigem entender antes de agir.

## Mudança destrutiva: expand/contract

`DROP COLUMN`, `RENAME`, mudança de tipo incompatível e apertar `NOT NULL` **nunca vão num
passo só**:

1. **Adiciona** o novo, anulável.
2. **Escreve nos dois**, na aplicação.
3. **Migra** os dados existentes.
4. **Passa a ler** do novo.
5. **Remove** o antigo, em migration posterior.

Os passos 1 e 5 são migrations diferentes, e entre elas o sistema roda com os dois. **Coluna
nova em tabela com dados** é `NULL` permitido **ou** default explícito — `NOT NULL` sem default
numa tabela populada falha na hora, e falha no Pi, em produção, no meio de um deploy.

## Dado de referência

Entra **por migration**, nunca por script solto.

E o que é dado de referência aqui é pouco, de propósito: as **categorias de sistema** de um
ambiente — sete operações × dois sentidos — nascem com o ambiente, então são criadas pelo
código do cadastro, não por seed. **O sistema não cria categoria de usuário nenhuma**
(`docs/02-dominio/categoria.md`), então não existe conjunto inicial para semear.

## Toda tabela do ambiente nasce inteira

Migration que cria tabela de dado financeiro cria, **no mesmo arquivo**:

1. a coluna `ambiente_id NOT NULL` com a chave estrangeira;
2. `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` **e** `FORCE ROW LEVEL SECURITY`;
3. a política, **já com o `OR` do `ADR-0004`**, mesmo que `vinculo` esteja vazia;
4. os índices que o `modelo-de-dados.md` exige.

Separar isso em migrations diferentes cria uma janela em que a tabela existe sem política — e
janela é o que vaza. **Migration roda com o papel dono; a aplicação nunca.**

## Onde se aplica

Em base **limpa** e em **cópia da base real**, as duas, antes do commit
(`docs/08-fluxos/nova-migration.md`). A base limpa prova que o schema nasce; a cópia da real
prova que ele **migra** — e é só nela que aparecem o `NOT NULL` que não cabe e a constraint que
o dado existente viola.

## Invariantes

- Migration aplicada nunca é editada. Correção é migration nova.
- Número sequencial, nunca reaproveitado.
- `ddl-auto` em `validate`. O schema não é gerado por ORM em host nenhum.
- Tabela de dado financeiro nasce com `ambiente_id`, RLS habilitado e **forçado**, política com
  o `OR` do vínculo, e índices — tudo na mesma migration.
- Mudança destrutiva vai em expand/contract, com o passo de remoção em migration posterior.
- Coluna nova em tabela populada é anulável ou tem default.
- Falha de *checksum* se investiga, nunca se contorna apagando o controle.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Entidades, chaves, tipos e o padrão de RLS | `03-dados/modelo-de-dados` |
| Colunas e constraints de cada tabela | `03-dados/catalogo-tabelas` |
| Roteiro de uma mudança de schema | `08-fluxos/nova-migration` |
| Por que o isolamento tem duas camadas | `ADR-0002` |
