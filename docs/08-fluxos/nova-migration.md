---
id: 08-fluxos/nova-migration
titulo: "Fluxo: mudança de schema"
dono: roteiro de alteração de banco de dados
ler-junto: []
status: ativo
---

# Fluxo: mudança de schema

## 1. Contexto a carregar

1. `docs/03-dados/migrations.md` — ferramenta, numeração, regras
2. `docs/03-dados/catalogo-tabelas.md` — **só a seção das tabelas afetadas**
3. `docs/02-dominio/<agregado>.md` — a regra que motiva a mudança

**Condicional:** `docs/03-dados/modelo-de-dados.md` se a mudança cria entidade ou
relacionamento novo (não abra para adicionar coluna).

**Não abra:** API, integrações, operação.

## 2. Regras

- **Migration aplicada nunca é editada.** Corrige-se com uma nova.
- Nada de `ddl-auto: update`. O schema é o que as migrations dizem, e só isso.
- Dinheiro: inteiro em centavos. Data/hora: UTC. Sem exceção.
- Mudança destrutiva (drop, rename, tipo incompatível) vai em **expand/contract**:
  1. adiciona o novo, 2. escreve nos dois, 3. migra os dados, 4. passa a ler do novo,
  5. remove o antigo em migration posterior. Nunca em um passo só.
- Coluna nova em tabela com dados: `NULL` permitido **ou** default explícito. Nunca
  `NOT NULL` sem default numa tabela populada.
- Dado de referência (seed) entra por migration, não por script solto.

## 3. Ordem de trabalho

1. Escrever a migration.
2. Aplicar em base **limpa** e em cópia da base **real** — as duas.
3. Atualizar `catalogo-tabelas.md` no mesmo commit.
4. Só então ajustar as entidades JPA.

## 4. Pronto quando

- [ ] Aplica em base limpa e em cópia da base real, sem erro
- [ ] `catalogo-tabelas.md` atualizado (colunas, tipos, constraints, índices)
- [ ] Índice criado para toda coluna nova usada em filtro
- [ ] Nenhuma migration anterior foi tocada
- [ ] Se destrutiva: os passos de expand/contract estão previstos e escritos
