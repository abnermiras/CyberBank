---
description: Criar o esqueleto do projeto Java (uma vez so, antes do primeiro caso de uso)
---

Tarefa única e de uma vez só: criar o esqueleto do projeto. **Ainda não existe uma linha de
Java no repositório.**

Leia **exatamente** estes três documentos, e nada mais:

1. `docs/01-arquitetura/estrutura-de-pastas.md` — a árvore e a convenção de pacotes
2. `docs/07-operacao/build-e-run.md` — pré-requisitos, comandos, banco local e variáveis
3. `docs/03-dados/migrations.md` — onde as migrations ficam e a regra delas

Entregue:

- `pom.xml` — módulo único (`ADR-0010`), Java 21, Spring Boot, Flyway, driver do Postgres,
  Argon2 do Spring Security, e as dependências de **teste**: ArchUnit (`ADR-0010`) e
  Testcontainers (`ADR-0011`). Mais o wrapper (`./mvnw`).
- `compose.yml` — só o Postgres, na versão de produção, com **script de inicialização criando
  os dois papéis**: o dono das tabelas e o da aplicação, que **não é dono e não tem
  `BYPASSRLS`**. Sem isso o RLS do `ADR-0002` não se aplica.
- `.env.exemplo` — os nomes das variáveis de `build-e-run.md`, **sem nenhum valor**. Versionado.
- `src/main/resources/application.yml` — `ddl-auto: validate`, Flyway ligado, e cada valor
  sensível vindo de variável de ambiente. **Variável obrigatória ausente derruba a subida, com
  o nome dela na mensagem.**
- `CyberbankApplication.java` em `br.com.cyberbank`.
- O teste de arquitetura em `src/test/java/br/com/cyberbank/arquitetura/`, com as três regras do
  `ADR-0010`. Ele passa vazio hoje, e é isso mesmo.

**Não** crie entidade, controlador, repositório nem migration — nada de domínio. O primeiro
caso de uso vem depois, pelo `/caso-de-uso`.

Pronto quando `./mvnw verify` passa com o Postgres do compose no ar.
