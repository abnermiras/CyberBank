---
id: 01-arquitetura/padroes-de-codigo
titulo: Padrões de código
dono: nomes de classe por camada, a conversao entre DTO, dominio e JPA, validacao, excecoes e o que e proibido
ler-junto: [01-arquitetura/visao-geral, 04-api/erros, 01-arquitetura/estrutura-de-pastas]
status: ativo
---

# Padrões de código

## A primeira linha, porque ela surpreende

**Não há pasta de topo por camada.** Quem chega de Spring procura `controller/`, `service/` e
`repository/` na raiz e não acha: o corte é **por assunto** (`ADR-0008`), e as camadas vivem
dentro de cada um. É a decisão que faz uma funcionalidade caber numa pasta em vez de em quatro.

## Nomes

**Substantivo do domínio em português, sufixo técnico em inglês.** A divisão não é gosto: o
substantivo é o vocabulário que o `04-api/convencoes.md` proíbe traduzir (`Fatura`,
`Lancamento`, `PROVISIONADO`), e o sufixo é vocabulário do **framework** — traduzir `Repository`
para `Repositorio` só faz a documentação do Spring parar de casar com o código.

| Camada | Padrão | Exemplo |
|---|---|---|
| `dominio` | O substantivo, puro | `Fatura`, `Lancamento`, `Situacao` |
| `dominio` (porta) | `<Assunto>Repository`, `<Assunto>Gateway` | `FaturaRepository` |
| `aplicacao` | **Verbo no infinitivo** + `UseCase` | `PagarFaturaUseCase`, `RolarFaturaUseCase` |
| `api` | `<Assunto>Controller` | `FaturaController` |
| `api` (DTO) | `<Acao>Request` / `<Assunto>Response` | `PagamentoRequest`, `FaturaResponse` |
| `persistencia` | `<Assunto>Entity`, `<Porta>Jpa` | `FaturaEntity`, `FaturaRepositoryJpa` |

**Caso de uso é um verbo**, e um só. `FaturaService` com oito métodos é a classe onde a regra
some — `PagarFaturaUseCase` diz o que faz, e quando ele crescer demais é porque virou dois.

## Três classes para a mesma coisa, e é de propósito

A regra 2 do `CLAUDE.md`: **entidade de domínio ≠ entidade JPA ≠ DTO**. `Fatura`,
`FaturaEntity` e `FaturaResponse` são três classes, e a conversão é **explícita**.

Parece desperdício, e o preço é real — três classes e dois mapeamentos por assunto. O que ele
compra:

- **A regra fica livre do banco e do HTTP.** `Fatura` não tem `@Entity`, não tem anotação de
  JSON, e por isso o teste dela roda sem Spring e sem Postgres.
- **Mudança de schema não vaza para o cliente.** Renomear coluna é migration mais mapeamento —
  o `FaturaResponse` nem sabe.
- **Nada vaza por acidente.** Entidade JPA serializada leva junto o que tem dentro: campo
  interno, coleção que ninguém queria carregar, e o `LazyInitializationException` clássico.

**Quem converte:** a `api` converte DTO ↔ domínio; a `persistencia` converte JPA ↔ domínio. O
`dominio` não conhece nenhuma das duas, e o mapeamento é código escrito à mão — sem biblioteca
de mapeamento, que troca o erro de compilação por um campo nulo em produção.

## Validação: duas, em camadas diferentes

Não é duplicação — são perguntas diferentes.

| Onde | Pergunta | Resposta ao erro |
|---|---|---|
| **`api`, no DTO** | *Isto é preenchível?* Campo obrigatório, formato de data, valor positivo | `422 VALIDACAO`, com **todos** os campos de uma vez (`docs/04-api/erros.md`) |
| **`dominio`** | *Isto pode acontecer?* Fatura recebe pagamento, categoria é escolhível, conta está ativa | `409` com o código da regra |

A da borda protege o domínio de lixo; a do domínio é a regra, e ela **existe mesmo que nenhuma
API chame** — é ela que o bot, a rotina do ciclo e o teste também atravessam.

## Exceções

**Exceção de domínio carrega o código do catálogo**, e nada mais:
`throw new RegraDeDominioException(FATURA_NAO_RECEBE_PAGAMENTO, ...)`.

Um **tratador único**, em `comum/`, traduz toda exceção de domínio em `problem+json`. O
controlador **não trata exceção** — `try/catch` em controlador é onde o mapeamento
erro→resposta começa a divergir entre endpoints.

Código que não está em `docs/04-api/erros.md` não existe: o código entra no catálogo **antes**
de existir no Java (`docs/08-fluxos/novo-endpoint.md`).

## Transação

**`@Transactional` na `aplicacao`**, no caso de uso. Nunca no controlador, nunca no
repositório, nunca no domínio.

Não é só arrumação: é a transação que faz o `SET LOCAL` do ambiente que a política de RLS lê
(`ADR-0002`). Transação aberta no lugar errado é **RLS lendo o ambiente errado** — ou nenhum.

## Sem Lombok

Java 21 tem `record`, que cobre DTO e objeto de valor com uma linha. O resto se escreve.

É a regra 3 do `CLAUDE.md` (dependência nova exige ADR) valendo para o caso mais fácil de
aceitar sem pensar: Lombok gera código que não está no arquivo, e a economia que ele traz é de
digitação — que não é o custo deste projeto.

## Proibido

- Regra de negócio em controlador, repositório, DTO ou tela.
- `@Transactional` fora da `aplicacao`.
- Entidade JPA em resposta de API, ou em parâmetro de caso de uso.
- `try/catch` de exceção de domínio no controlador.
- Import entre pacotes de domínio, e campo de domínio tipado como objeto de outro domínio
  (`ADR-0010`).
- Concatenar valor em SQL, mesmo em query nativa (`docs/01-arquitetura/seguranca.md`).
- `ddl-auto: update` (`docs/03-dados/migrations.md`).
- Valor monetário em `double` ou `BigDecimal` com casas — é inteiro em centavos, sempre.
- Código de erro que não está no catálogo.
- Pasta de topo por camada.

## Invariantes

- Nome de classe segue a tabela acima; caso de uso é um verbo.
- Domínio, JPA e DTO são classes distintas, com conversão escrita à mão.
- `dominio` não tem anotação de Spring, de JPA nem de JSON.
- Toda exceção de domínio carrega um código do catálogo, e um tratador único a converte.
- A transação abre na `aplicacao` e carrega o ambiente para o RLS.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| As camadas e o caminho de um caso de uso | `01-arquitetura/visao-geral` |
| Onde cada arquivo é criado | `01-arquitetura/estrutura-de-pastas` |
| O catálogo de códigos e o corpo do erro | `04-api/erros` |
| Formatos, paginação e compatibilidade | `04-api/convencoes` |
| Por que domínio referencia domínio por id | `ADR-0010` |
| Tipos de coluna, RLS e migration | `03-dados/modelo-de-dados`, `03-dados/migrations` |
