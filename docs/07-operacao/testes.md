---
id: 07-operacao/testes
titulo: Testes
dono: as quatro suites, o que e obrigatorio testar, e como rodar cada uma
ler-junto: [07-operacao/build-e-run, 01-arquitetura/padroes-de-codigo]
status: ativo
---

# Testes

**O critério não é cobertura, é consequência.** Percentual de linha cobre `getter` e deixa
passar a regra que muda dinheiro; e o protótipo já provou onde os erros deste projeto moram —
os vinte e seis achados foram **regras plausíveis, escritas e commitadas**, que quebraram
quando alguém somou os números (`claude/estado-do-projeto.md`).

Então não há meta de percentual. Há uma lista do que é **obrigatório**, e ela está no fim.

## As quatro suítes

| Suíte | O que prova | Precisa de | Custo |
|---|---|---|---|
| **Domínio** | A regra, sozinha. `Fatura`, `Lancamento`, `Categoria` | Nada. Sem Spring, sem banco, sem Docker | Milissegundos |
| **Arquitetura** | A fronteira do `ADR-0010`: nenhum import entre pacotes de domínio, seta apontando para dentro, entidade JPA presa na persistência | Nada | Milissegundos |
| **Integração** | Schema, migrations, RLS e repositório, contra **Postgres real** (`ADR-0011`) | Docker | Segundos |
| **Contrato** | O endpoint responde o que `docs/04-api/endpoints-<agregado>.md` documenta | Spring, e o banco quando o caso de uso o toca | Segundos |

**A separação é o ponto.** A suíte de domínio é a que roda o tempo todo enquanto se escreve, e
ela só é rápida porque o domínio não conhece Spring nem JPA
(`docs/01-arquitetura/visao-geral.md`). No dia em que ela precisar subir contexto, o desenho
quebrou antes do teste.

## Como rodar

```
./mvnw test                 domínio + arquitetura. Sem Docker
./mvnw verify               tudo, inclusive integração e contrato
./mvnw test -Dtest=FaturaTest
node prototipo/verificar.js  as provas do protótipo, que são outra coisa
```

`verify` é o que vale antes de commitar. `test` é o do minuto a minuto.

## Dados de teste

**Cada teste monta o que precisa, e nada mais.** Não há base de teste compartilhada, nem
carga inicial que "todos os testes usam" — é o estado que faz um teste passar por causa do
anterior.

Na integração, cada suíte sobe o contêiner e aplica as migrations **do zero**
(`ADR-0011`). Isso testa a migration de graça: se ela não aplica em base limpa, a suíte não
começa.

**O seed do protótipo é outra coisa e não se mistura.** Ele existe para ter história em vários
dias e marcas plantadas de propósito (`claude/estado-do-projeto.md`); teste automatizado que
dependesse dele passaria a quebrar quando o seed mudasse por razão de tela.

## O que é obrigatório testar

Não é lista de desejo — é a lista do que, quebrado, o usuário só descobre olhando o próprio
dinheiro:

- **Toda invariante escrita num doc de `02-dominio/`.** Se está escrita como invariante, tem
  teste. É o que o `conferir()` do protótipo já faz do lado do modelo.
- **O isolamento entre ambientes**, com dois ambientes reais e a conexão da aplicação — o teste
  do `ADR-0011`, que precisa **falhar de verdade** se a política sumir.
- **Os dois papéis do banco**, e antes de qualquer tabela existir: o papel da aplicação **não é
  superusuário, não tem `BYPASSRLS` e não é dono do `public`** (`PapeisDoBancoIT`). É a
  montagem de que o `ADR-0002` inteiro depende, e é a única parte dele que quebra **em
  silêncio** — um `DB_APP_USER` apontando para o dono deixa tudo funcionando, nenhuma suíte
  vermelha, e o RLS deixa de existir. O contêiner sobe com o **mesmo script** do `compose.yml`,
  senão o teste prova uma montagem que ele mesmo inventou.
- **Todo passo que o sistema dá sozinho:** fechamento, encerramento, rolagem e realização por
  data. E cada um duas vezes: **idempotência** (rodar de novo não faz nada) e **recuperação de
  atraso** (dois ciclos parados voltam em ordem cronológica).
- **A aritmética que já quebrou.** Os três números da fatura, o par de rolagem somando zero, o
  centavo do arredondamento na primeira parcela, o estorno abatendo o gasto no relatório. São
  achados reais (14, 17, 25), não hipóteses.
- **Todo código de erro do catálogo**, com o status que `docs/04-api/erros.md` promete.
- **A fronteira do `ADR-0010`**, que é a suíte de arquitetura inteira.

## O que não se testa

- **Getter, setter e mapeamento trivial.** Teste que só repete o código não pega erro; ele
  atrasa o refactor e infla o número.
- **Framework.** Que o Spring injete, que o Jackson serialize, que o Flyway ordene: são deles.
- **Aparência.** Nenhuma suíte aqui vê layout — o achado 22 é sobre isso, e a resposta dele foi
  o `verificar.js`, não um teste de pixel.

## Quando um bug aparece

**O teste vem antes da correção**, e ele reproduz o bug — vermelho primeiro. Sem isso não há
prova de que a correção corrige, nem de que o bug não volta.

E a pergunta seguinte é a do achado 23: *o que faltava para que isso fosse pego?* Se a resposta
for "um caso que nenhum teste continha", o caso entra — porque **quando o doc prevê um caso, o
teste tem que conter esse caso**, senão a previsão nunca é executada.

## Invariantes

- A suíte de domínio não sobe Spring, não abre banco e não pede Docker.
- Nenhum teste usa banco em memória (`ADR-0011`).
- Teste de integração conecta com o papel **não-dono** das tabelas, e aplica as migrations do
  zero. O contêiner nasce com os dois papéis, pelo script do `compose.yml`.
- Nenhum teste depende de estado deixado por outro, nem do seed do protótipo.
- Toda invariante escrita em `02-dominio/` tem teste.
- Todo passo automático tem teste de idempotência **e** de recuperação de atraso.
- Correção de bug entra com o teste que o reproduz.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Pré-requisitos, comandos e como subir o banco | `07-operacao/build-e-run` |
| Por que Postgres real e não banco em memória | `ADR-0011` |
| A fronteira que a suíte de arquitetura verifica | `ADR-0010`, `01-arquitetura/modulos` |
| O que cada invariante quer dizer | o doc do assunto em `02-dominio/` |
| O catálogo de códigos de erro | `04-api/erros` |
