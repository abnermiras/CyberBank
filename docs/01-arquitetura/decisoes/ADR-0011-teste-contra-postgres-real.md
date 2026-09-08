---
id: 01-arquitetura/decisoes/ADR-0011-teste-contra-postgres-real
titulo: "ADR-0011: teste de integração roda contra Postgres real"
dono: por que nao ha banco em memoria nos testes, e o que isso custa
ler-junto: [07-operacao/testes, 03-dados/modelo-de-dados]
status: ativo
---

# ADR-0011: teste de integração roda contra Postgres real

- **Status:** aceita
- **Data:** 2026-09-07
- **Afeta:** `07-operacao/testes`, `07-operacao/build-e-run`, dependências de teste

## Contexto

Todo teste que toca o banco precisa de um banco. O caminho barato é o H2 em memória: sobe em
milissegundos, não pede Docker, e é o que a maior parte dos projetos Spring usa.

Ele **não serve aqui**, e por um motivo específico: **o H2 não tem Row Level Security.**

O `ADR-0002` decidiu que o isolamento entre ambientes tem duas camadas, e que a segunda — a
política de RLS no Postgres — é *"a rede embaixo"*, a que vale para query nativa e para código
que ainda não existe. Testar isso contra um banco que não tem RLS não é um teste mais fraco: é
um teste que **passa sempre**, inclusive quando a política está errada, ausente ou anulada
pelo papel dono (`docs/03-dados/modelo-de-dados.md`).

E não é só o RLS. `date` sem fuso, `bigint` de centavos, `CHECK` de enum, `GENERATED ALWAYS AS
IDENTITY`, `current_setting`, o `FORCE ROW LEVEL SECURITY`: o modelo inteiro é Postgres. Um
banco que aceita o SQL "quase igual" transforma erro de schema em erro de produção.

## Decisão

**Teste de integração sobe um Postgres real em contêiner** (Testcontainers), com a **mesma
versão** de produção, e as migrations do Flyway aplicadas do zero a cada suíte.

E o contêiner de teste sobe com os **dois papéis**: o dono das tabelas e o papel da aplicação,
que não é dono e não tem `BYPASSRLS`. Sem isso o teste rodaria como dono, o RLS não se
aplicaria, e voltaríamos ao problema do H2 por outro caminho.

**Há um teste cujo trabalho é falhar quando o isolamento quebra:** dois ambientes, dado nos
dois, conexão da aplicação com o ambiente A no contexto — e a consulta não pode enxergar nada
de B, nem por repositório, nem por query nativa.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| **H2 em memória** | Não tem RLS. O teste de isolamento passaria com a política ausente — e um teste que passa sempre é pior que teste nenhum, porque autoriza |
| **H2 no modo de compatibilidade Postgres** | Compatibilidade de sintaxe, não de comportamento. `current_setting` e política de linha continuam não existindo |
| **Postgres instalado na máquina, compartilhado** | Estado entre execuções: um teste passa porque o anterior deixou dado. E a versão vira "a que estava instalada", que difere entre a máquina e o Pi |
| **Testar RLS só à mão, em produção** | O `ADR-0002` chama o RLS de rede de segurança. Rede que ninguém testou é rede que ninguém sabe se está lá |

## Consequências

- **Ganhamos:** o schema é exercitado como ele é — RLS, `CHECK`, tipos e migrations —, e o
  isolamento entre ambientes tem um teste que **falha de verdade** quando quebra.
- **Perdemos:** a suíte de integração exige **Docker rodando** e demora segundos, não
  milissegundos. Por isso ela é separada da suíte de domínio, que continua rodando sem Spring,
  sem Docker e em milissegundos (`docs/07-operacao/testes.md`).
- **Passa a ser proibido:** banco em memória em qualquer teste; teste de integração rodando com
  o papel dono das tabelas; suíte que depende de dado deixado pela anterior.
- **Revisitar se:** aparecer um ambiente onde Docker não roda e onde os testes precisem rodar.
  E aí o que sai é a suíte de integração naquele ambiente — nunca o Postgres real.
