---
id: 07-operacao/build-e-run
titulo: Build e run
dono: pre-requisitos, comandos exatos, como subir o banco local e as variaveis de ambiente
ler-junto: [07-operacao/testes, 01-arquitetura/ambientes-de-execucao, 03-dados/migrations]
status: ativo
---

# Build e run

Este doc é o dono dos **comandos**. O `CLAUDE.md` aponta para cá em vez de reproduzi-los, para
não ter dois lugares com a mesma linha divergindo.

## Pré-requisitos

| | |
|---|---|
| **JDK 21** | O projeto usa `record` e o que veio com o 21. Não há plano de subir de versão por enquanto |
| **Docker** | Para o Postgres local e para a suíte de integração (`ADR-0011`) |
| **Maven** | **Não instale.** O wrapper (`./mvnw`) vem no repositório e fixa a versão |

Nada além disso. Não há ferramenta paga, serviço externo nem conta em lugar nenhum — é a
restrição de custo externo zero da `visao.md`, e ela vale para a máquina de desenvolvimento
também.

## Os comandos

```
./mvnw test                    domínio + arquitetura. Rápido, sem Docker
./mvnw verify                  tudo. É o que roda antes de commitar
./mvnw spring-boot:run         sobe a aplicação em desenvolvimento
./mvnw -DskipTests package     gera o jar
```

E os do projeto, que não são Maven:

```
python3 docs/_tools/docs.py check     0 erros e 0 avisos antes de commitar doc
python3 docs/_tools/docs.py index     regenera o INDEX.md ao criar doc ou mexer no dono:
python3 docs/_tools/docs.py custo     o custo de contexto por rota
node prototipo/verificar.js           as provas do protótipo
```

## O banco local

`compose.yml` sobe **um** serviço: o Postgres, na mesma versão de produção. A aplicação roda
fora do contêiner em desenvolvimento — recompilar e reiniciar é mais rápido assim, e não muda
nada que o `ambientes-de-execucao.md` prometa.

```
docker compose up -d      sobe o Postgres
docker compose down       para
docker compose down -v    para e APAGA o volume: a próxima subida recria do zero
```

**Ele publica na porta 5433 do host**, e só no `127.0.0.1` — a 5432 continua com o Postgres do
RaspyBank, e os dois convivem enquanto a migração não termina. Dentro do contêiner é 5432
como sempre; quem sabe da 5433 é o `DB_URL` do `.env`.

A imagem é `postgres:18.4`, e a mesma string está no `<postgres.imagem>` do `pom.xml`, que é
de onde a suíte de integração tira o contêiner dela (`ADR-0011`). Mudou uma, muda a outra.

**O banco nasce com dois papéis, e isso não é detalhe de arrumação.** O script de
inicialização `docker/postgres-init/01-papeis.sh` roda **uma vez**, na criação do volume, e
cria o papel dono das tabelas e o papel da aplicação — que **não é dono e não tem
`BYPASSRLS`**. Sem os dois, a aplicação conecta como dono, o Postgres não aplica política
nenhuma, e o `ADR-0002` inteiro vira decoração (`docs/03-dados/modelo-de-dados.md`).

As migrations rodam **com o papel dono**, na subida da aplicação, pelo Flyway
(`docs/03-dados/migrations.md`). Não há passo manual.

## Variáveis de ambiente

**Segredo entra por variável, nunca por arquivo versionado**
(`docs/01-arquitetura/seguranca.md`). O `application.yml` só **nomeia** a variável; o valor vem
de fora, e localmente de um `.env` que o `.gitignore` mantém fora do git.

| Variável | Para quê |
|---|---|
| `DB_URL`, `DB_APP_USER`, `DB_APP_PASSWORD` | A conexão da aplicação — o papel **não-dono** |
| `DB_OWNER_USER`, `DB_OWNER_PASSWORD` | O papel que roda as migrations |
| `ARGON2_MEMORIA`, `ARGON2_ITERACOES`, `ARGON2_PARALELISMO` | Os parâmetros calibrados por host (`docs/01-arquitetura/seguranca.md`) |
| `SMTP_USUARIO`, `SMTP_SENHA_DE_APP` | A senha de app do Gmail, e só para recuperar senha (`ADR-0007`) |
| `CYBERBANK_URL_BASE` | O que entra no link de recuperação de senha |
| `POSTGRES_SENHA_ADMIN` | O superusuário de bootstrap do contêiner, que cria os dois papéis e some da história. Não é o dono e não é a aplicação |

O `compose.yml` lê essas variáveis do `.env` sozinho. A aplicação **não**: `./mvnw
spring-boot:run` herda o ambiente do terminal, então carregue o arquivo antes —
`set -a; . ./.env; set +a`.

**Falta de variável obrigatória derruba a aplicação na subida**, com o nome da variável na
mensagem. Não há valor padrão para segredo: padrão silencioso é como uma senha de
desenvolvimento chega em produção.

**Calibrar o Argon2id é passo de instalação**, não de código. Os números do Pi não são os do
WSL, e parâmetro copiado de tutorial ou trava o login ou não protege nada.

## Ordem numa máquina limpa

1. `docker compose up -d`
2. Copiar o `.env.exemplo` para `.env` e preencher — **o `.env.exemplo` é versionado; o `.env`,
   nunca.**
3. `./mvnw verify` — se a suíte de integração passa, Docker e banco estão certos.
4. `./mvnw spring-boot:run`

## Invariantes

- Maven é o wrapper do repositório; nenhuma versão instalada na máquina participa.
- O Postgres local é a **mesma versão** de produção.
- A aplicação nunca conecta com o papel dono das tabelas.
- Nenhum segredo é versionado; o `.env.exemplo` tem os nomes e nenhum valor.
- Variável obrigatória ausente derruba a subida, e a mensagem diz qual é.
- Migrations são aplicadas pelo Flyway na subida, com o papel dono. Nunca à mão.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| As suítes de teste e o que é obrigatório testar | `07-operacao/testes` |
| Quais ambientes existem e o que muda entre eles | `01-arquitetura/ambientes-de-execucao` |
| Como uma versão vai para o Pi, e o rollback | `07-operacao/deploy` |
| Nome, conteúdo e regras de uma migration | `03-dados/migrations` |
| Por que dois papéis no banco | `03-dados/modelo-de-dados`, `ADR-0002` |
| Onde os segredos ficam | `01-arquitetura/seguranca`, `05-integracoes/vault-segredos` |
