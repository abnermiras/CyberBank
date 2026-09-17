---
id: 01-arquitetura/decisoes/ADR-0013-rotina-enxerga-ambientes-por-funcao
titulo: "ADR-0013: a rotina diária enxerga os ambientes por função SECURITY DEFINER"
dono: como uma rotina sem sessao atravessa o RLS sem virar bypass
ler-junto: [01-arquitetura/decisoes/ADR-0002-isolamento-por-ambiente, 02-dominio/evento]
status: ativo
---

# ADR-0013: a rotina diária enxerga os ambientes por função `SECURITY DEFINER`

- **Status:** aceita
- **Data:** 2026-09-17
- **Afeta:** `02-dominio/evento`, `03-dados/catalogo-tabelas-do-ambiente`, `01-arquitetura/seguranca`, toda rotina futura

## Contexto

O `ADR-0002` isola por ambiente em duas camadas, e a de baixo é RLS no Postgres: o papel da
aplicação não é dono, não tem `BYPASSRLS`, e toda política lê `app.usuario_id` ou
`app.ambiente_id` do `SET LOCAL` da transação. Um teste de integração reprova o build se isso
deixar de ser verdade.

A rotina diária que o `evento.md` exige não tem requisição, não tem sessão e não tem ambiente:
ela precisa passar por **todos** os ambientes. Com as variáveis vazias, as políticas não
devolvem linha nenhuma — nem a lista de ambientes por onde começar.

## Decisão

Uma **função `SECURITY DEFINER`**, criada pelo papel dono na migration, devolve apenas
`(ambiente_id, dono_id)` de todos os ambientes. A rotina chama essa função, e depois **abre uma
transação por ambiente** com o contexto daquele ambiente e do dono dele — da transação para
dentro, o RLS vale exatamente como numa requisição.

`SECURITY DEFINER` sozinho não basta, e o motivo é o `FORCE ROW LEVEL SECURITY`: ele sujeita
**o próprio dono** às políticas, e o dono não tem `BYPASSRLS`. Então a função vem acompanhada
de uma política de `SELECT` em `acesso` escrita **para o papel dono** (`TO`), e que só enxerga
as linhas de papel `DONO`. É a diferença que importa: o privilégio não é uma variável que
qualquer código pode setar, é um papel que só a função assume, numa consulta que só devolve
dois números.

A função é a única coisa que enxerga além do ambiente ativo, e ela não devolve dado financeiro:
devolve por onde iterar.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| `OR current_setting('app.rotina')` em todas as políticas | É `BYPASSRLS` com outro nome, e pior: fica escrito em cada política, e qualquer código que setar a variável passa a ver o banco inteiro. Também obrigaria uma migration a reescrever todas as políticas já aplicadas |
| Dar `BYPASSRLS` ao papel da aplicação | Anula o `ADR-0002` inteiro, e o `PapeisDoBancoIT` reprova o build — corretamente |
| Rotina disparada por ambiente, quando alguém abre o app | Zero infraestrutura, mas quebra a promessa do `evento.md`: ambiente sem acesso por uma semana fica com previsto vencido, e o Diário perde o dia em que a transição deveria ter acontecido. Evento não se reconstitui depois |
| Uma segunda conexão, com o papel dono, só para a rotina | O papel dono é o do Flyway, e ele ignora RLS em tudo. Seria uma porta aberta permanente para economizar uma função |

## Consequências

- **Ganhamos:** rotina que atravessa ambientes sem que nenhuma política existente mude, e um
  mecanismo que serve igual para o fechamento de fatura e para a recorrência.
- **Perdemos:** a rotina passa a ter um privilégio que o resto da aplicação não tem, e ele mora
  no banco — quem lê só o Java não o vê. Por isso ele está aqui e no `catalogo-tabelas`.
- **Passa a ser proibido:** função `SECURITY DEFINER` que devolva dado financeiro, e rotina que
  leia ou escreva movimento fora da transação de um ambiente.
- **Revisitar se:** aparecer uma rotina que precise **comparar** ambientes entre si — esta
  decisão assume que todo trabalho é por ambiente, um de cada vez.
