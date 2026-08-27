---
id: 01-arquitetura/decisoes/ADR-0002-isolamento-por-ambiente
titulo: "ADR-0002: isolamento por ambiente em duas camadas"
dono: como o isolamento entre ambientes financeiros e imposto
ler-junto: [02-dominio/ambiente-financeiro, 01-arquitetura/seguranca]
status: ativo
---

# ADR-0002: isolamento por ambiente em duas camadas

- **Status:** aceita
- **Data:** 2026-08-27
- **Afeta:** persistência, todo repositório, migrations, contexto de requisição

## Contexto

O ambiente financeiro é o dono de todo dado, e um usuário só pode ver os ambientes a que
tem acesso (`docs/02-dominio/ambiente-financeiro.md`). Vazamento entre ambientes é falha
crítica, não bug de lógica — o sistema guarda dinheiro de outras pessoas.

Um filtro que depende de alguém lembrar de escrever `where ambiente_id = ?` é um filtro
que vai ser esquecido: basta uma query nativa, um relatório com pressa ou um `JdbcTemplate`.

## Decisão

Duas camadas, com papéis diferentes:

1. **Filtro na aplicação** — um repositório-base aplica o ambiente automaticamente. O
   código de negócio **não passa `ambienteId`** e não deve conseguir passar. É o
   comportamento normal, e é onde o erro aparece como exceção clara.
2. **Row Level Security no PostgreSQL** — política de RLS em toda tabela com
   `ambiente_id`. É a rede embaixo: vale para query nativa, para script manual no `psql`
   e para código que ainda não existe.

O ambiente vem do **contexto da requisição autenticada**. Nunca de parâmetro escolhido
pelo cliente sem validação de acesso — aceitar `ambienteId` do corpo sem checar é a falha
clássica desse modelo.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Só filtro na aplicação | Mais simples e tudo em Java, mas uma query nativa passa por fora e ninguém percebe até vazar |
| Só RLS | Garantia mais forte, mas o erro vira "sumiu o dado" em vez de exceção — depurar fica caro no dia a dia |
| Filtro agora, RLS na Fase 4 | Ligar RLS com o banco já cheio de dado real custa muito mais do que ligar vazio. A economia é de hoje; a conta é depois |

## Consequências

- **Ganhamos:** nenhuma query escapa do isolamento, e o erro comum aparece cedo e claro.
- **Perdemos:** a conexão precisa carregar o ambiente a cada transação (`SET LOCAL` sobre
  o pool). É chato de acertar, e é chato uma vez só.
- **Passa a ser proibido:** tabela com dado financeiro sem `ambiente_id` e sem política de
  RLS; qualquer consulta que receba `ambienteId` vindo do cliente sem validar o acesso.
  Toda migration que cria tabela de dado financeiro cria a política junto.
- **Revisitar se:** o custo do `SET LOCAL` aparecer em medição real no Raspberry Pi — e aí
  o que sai é o filtro duplicado, nunca o RLS.
