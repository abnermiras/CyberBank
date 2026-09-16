---
id: 01-arquitetura/estrutura-de-pastas
titulo: Estrutura de pastas
dono: a arvore do projeto, a convencao de pacotes, onde criar cada arquivo novo e o que nao se versiona
ler-junto: [01-arquitetura/visao-geral, 01-arquitetura/padroes-de-codigo]
status: ativo
---

# Estrutura de pastas

**A pasta é o endereço que o roteador entrega** (`ADR-0008`). Se você sabe o doc, sabe o
pacote — e não precisa procurar.

## A árvore

```
CyberBank/
├── CLAUDE.md              o roteador: tarefa → docs. Lido sempre
├── claude/                estado do projeto e sessões. Fora do roteador
├── docs/                  a documentação, e a ferramenta em docs/_tools
├── prototipo/             banco de provas navegável, sem build
├── docker/postgres-init/  o script que cria os dois papéis do banco local
├── compose.yml            só o Postgres, na versão de produção
├── .env.exemplo           os nomes das variáveis, sem valor nenhum. Versionado
├── mvnw, .mvn/            o wrapper do Maven: a versão vem do repositório
├── pom.xml                módulo único (ADR-0010)
└── src/
    ├── main/
    │   ├── java/br/com/cyberbank/
    │   │   ├── CyberbankApplication.java
    │   │   ├── comum/          o que é de todos: tipos base, tratador de erro, contexto
    │   │   ├── ambiente/       usuário, acesso, papel, convite, sessão
    │   │   ├── conta/
    │   │   ├── categoria/
    │   │   ├── meio/
    │   │   ├── lancamento/
    │   │   ├── fatura/
    │   │   ├── patrimonio/
    │   │   └── evento/
    │   └── resources/
    │       ├── application.yml
    │       ├── db/migration/   V001__... (Flyway, SQL puro)
    │       └── static/         o FRONT: html, css e js, sem build (ADR-0012)
    └── test/java/br/com/cyberbank/
        ├── arquitetura/        o teste de fronteira do ADR-0010
        └── <assunto>/          espelha main, sempre
```

**Um pacote por assunto**, e o nome sai do doc dono de `docs/02-dominio/`
(`docs/01-arquitetura/modulos.md`). Nomes curtos onde o doc é composto: `meio-de-pagamento` →
`meio`, `aplicacao-patrimonio` → `patrimonio`, `ambiente-financeiro` → `ambiente`.

**O front mora em `resources/static/`** e o próprio Spring Boot o serve, na mesma porta da
API (`ADR-0012`). Ele **não** tem pasta por assunto: `assets/js/api.js` é a conversa com a API,
e cada tela tem o seu arquivo (`login.js`, `cadastro.js`). O corte por assunto é do Java — no
front, o corte é por tela.

**`patrimonio`, e não `aplicacao`**, por uma razão boba e cara: `aplicacao` já é o nome de uma
camada. Duas coisas com o mesmo nome em níveis diferentes da árvore é confusão garantida em
toda conversa e em todo import.

## Dentro de um assunto

```
fatura/
├── dominio/         Fatura, as regras, e as portas (FaturaRepository)
├── aplicacao/       PagarFaturaUseCase, FecharFaturaUseCase
├── api/             FaturaController, os DTOs de requisição e resposta
└── persistencia/    FaturaEntity, FaturaRepositoryJpa, o mapeamento
```

Sempre as quatro, sempre com esses nomes. Assunto que não expõe HTTP simplesmente não tem
`api` — nunca uma quinta pasta com outro nome.

**Pacote é `br.com.cyberbank.<assunto>.<camada>`**, e a regra de importação é a do
`visao-geral.md`: a seta aponta para dentro, e domínio não importa domínio.

## Onde criar cada coisa

| O que | Onde |
|---|---|
| **Caso de uso novo** | `<assunto>/aplicacao/`, com o nome da ação: `RolarFaturaUseCase` |
| **Regra nova** | `<assunto>/dominio/`. Se ela precisa de dois assuntos, **é erro de modelagem** (`ADR-0008`) |
| **Endpoint novo** | `<assunto>/api/`, depois de escrever o bloco em `docs/04-api/endpoints-<agregado>.md` (`docs/08-fluxos/novo-endpoint.md`) |
| **Adapter (bot, OFX, e-mail)** | `<assunto>/persistencia/` se implementa uma porta daquele assunto; em `comum/` se é infraestrutura de todos |
| **Migration** | `src/main/resources/db/migration/`, e a tabela nasce inteira (`docs/03-dados/migrations.md`) |
| **Teste** | `src/test/java/...` **espelhando o caminho de `main`**, sempre |
| **Tipo usado por todo mundo** | `comum/`. E pense duas vezes: `comum` que cresce é assunto que ninguém quis nomear |

## `comum/` é o único lugar sem dono, e por isso tem regra

Cabe ali só o que **não é de assunto nenhum**: o tratador de exceção que produz
`problem+json`, o contexto de ambiente da requisição, o tipo de dinheiro em centavos, a
configuração do Spring.

Não cabe: nada que tenha regra de negócio dentro. `comum/` é a pasta que, sem regra, vira o
depósito de tudo que ninguém quis classificar — e aí o roteador perde o endereço, porque
`comum` não é assunto de doc nenhum.

## O que não se versiona

O `.gitignore` é parte da defesa (`docs/01-arquitetura/seguranca.md`): **nenhum segredo é
versionado**.

Ficam de fora: `target/`, artefatos de build, `.env` e qualquer arquivo de segredo,
configuração local de IDE (`.idea/`, `.vscode/`, `*.iml`), log e dump de banco.

Fica **dentro**: `application.yml` sem segredo — valor sensível entra por variável de
ambiente, e o arquivo só nomeia a variável.

## Invariantes

- Um pacote por assunto, com o nome derivado do doc dono (`ADR-0008`).
- As camadas dentro do assunto são exatamente `dominio`, `aplicacao`, `api`, `persistencia`.
- Não há pasta de topo por camada.
- O teste espelha o caminho de `main`.
- `comum/` não contém regra de negócio.
- Nenhum segredo é versionado, em nenhuma forma.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| As camadas e o que cada uma conhece | `01-arquitetura/visao-geral` |
| Quais assuntos existem, e o grafo entre eles | `01-arquitetura/modulos` |
| Nomes de classe e o que é proibido escrever | `01-arquitetura/padroes-de-codigo` |
| Nome e conteúdo de uma migration | `03-dados/migrations` |
| Por que o pacote espelha o doc | `ADR-0008` |
