---
id: 01-arquitetura/visao-geral
titulo: Visão geral da arquitetura
dono: o estilo, as camadas, a regra de importacao e o caminho de um caso de uso ponta a ponta
ler-junto: [01-arquitetura/modulos, 01-arquitetura/padroes-de-codigo, 01-arquitetura/seguranca]
status: ativo
---

# Visão geral da arquitetura

**Monolito modular, cortado por assunto e não por camada.** Um processo, um banco, um artefato
para subir no Raspberry Pi — e, por dentro, fronteiras que o build reprova quando alguém as
atravessa (`ADR-0008`, `ADR-0010`).

O corte por assunto é a decisão que organiza todo o resto: `fatura` é uma pasta, não quatro
arquivos espalhados por `controller/`, `service/`, `repository/` e `dto/`. O corte clássico por
camada faz uma funcionalidade nascer em quatro lugares — e é o que torna o RaspyBank caro de
mexer hoje.

## As quatro camadas

Elas existem **dentro** de cada assunto, não acima deles.

| Camada | O que é | Conhece |
|---|---|---|
| `dominio` | A regra. Objetos, invariantes e as **portas** (interfaces) de que ela precisa | **Nada.** Nem Spring, nem JPA, nem outro assunto |
| `aplicacao` | Os casos de uso. Orquestra, abre transação, junta assuntos | O `dominio` |
| `api` | Controlador e DTO. Traduz HTTP ↔ caso de uso | A `aplicacao` |
| `persistencia` | Entidade JPA, repositório, mapeamento. **Implementa** as portas do domínio | O `dominio` |

**A seta aponta sempre para dentro.** `api` e `persistencia` são as bordas e podem depender do
miolo; o miolo não sabe que elas existem. É o que permite testar a regra de fatura sem subir
Spring e sem banco — e é o teste do `ADR-0010` que garante que continua assim.

**Domínio não importa domínio** (`ADR-0010`). Quem junta dois assuntos é a `aplicacao`.

## Onde a regra de negócio vive

**No `dominio`, e em lugar nenhum além** — é a regra 1 do `CLAUDE.md`. Os três lugares onde ela
tenta morar, e por quê não:

- **No controlador.** Regra ali só é alcançável por HTTP: o bot, uma rotina e um teste passam
  por fora dela. Foi assim que *"o estorno não abate o gasto"* sobreviveu meses no protótipo.
- **No repositório.** Regra escrita em SQL não aparece em teste de domínio e não aparece na
  leitura do modelo. Vira comportamento que só o banco conhece.
- **Na tela.** A tela **mostra** a regra — o botão que só existe quando funciona —, mas quem a
  decide é o domínio. Regra na tela é regra que o segundo cliente não tem.

## Um caso de uso, ponta a ponta

Pagar uma fatura, que atravessa dois assuntos e é o caminho inteiro:

1. **`api`** — `POST /api/v1/ambientes/7/faturas/42/pagamentos` chega no controlador. Ele valida
   **formato** (o DTO), converte para os tipos do domínio e chama o caso de uso. Nada mais.
2. **Filtro de contexto** — antes disso, já resolveu o `{ambienteId}`, validou o acesso do
   usuário autenticado e pôs o ambiente no contexto (`docs/01-arquitetura/seguranca.md`).
3. **`aplicacao`** — `PagarFaturaUseCase` abre a transação, e é ela que faz o `SET LOCAL` que a
   política de RLS lê. Busca a fatura pela porta, busca a conta pagadora pela porta — **dois
   passos, porque domínio não referencia domínio** — e chama a regra.
4. **`dominio`** — decide. *A fatura é `FECHADA` com `a pagar` maior que zero?* Se não, lança a
   exceção de domínio com o código `FATURA_NAO_RECEBE_PAGAMENTO`. Se sim, produz o par de
   lançamentos da transferência.
5. **`persistencia`** — grava, convertendo objeto de domínio em entidade JPA. O RLS confere de
   novo, do lado do banco.
6. **`api`** — converte o resultado em DTO de resposta. Se veio exceção de domínio, um
   tratador único a traduz em `problem+json` (`docs/04-api/erros.md`); o controlador não trata
   exceção.

**Nenhum passo decide duas vezes, e nenhum decide o que não é dele.**

## As bordas são clientes, não caminhos paralelos

O bot do Telegram, a ingestão de OFX, a captura de notificação e o painel **entram pela mesma
API** — nenhum fala com o domínio direto, e nenhum tem atalho.

Não é preferência de desenho: caminho paralelo é regra duplicada, e regra duplicada é onde as
duas divergem. Uma compra lançada pelo bot e uma lançada pela tela passam pelo **mesmo** caso
de uso, pela mesma validação e pelo mesmo evento gravado — senão o Diário conta uma história
diferente conforme por onde a pessoa entrou.

O que muda por borda é só **como se prova quem está falando**
(`docs/01-arquitetura/seguranca.md`).

## O que o sistema faz sozinho

Três rotinas rodam sem ninguém pedir: **fechamento** e **encerramento** da fatura
(`docs/02-dominio/fatura-cartao.md`, `docs/02-dominio/fatura-pagamento.md`) e a **realização
por data** (`docs/02-dominio/lancamento.md`).

Elas são **casos de uso como os outros** — mesma camada, mesma transação, mesma regra de
domínio. O que as diferencia é só quem as dispara: um agendador, e não um controlador. E as
três são **idempotentes e recuperam atraso**, porque o Pi desliga.

Cada passo que de fato acontece grava um **evento** (`docs/02-dominio/evento.md`). É a
contrapartida do preço de o sistema mexer no dinheiro sozinho.

## Invariantes

- A seta aponta para dentro: `api` e `persistencia` conhecem o miolo; `dominio` não conhece
  ninguém.
- `dominio` não importa outro `dominio`, nem Spring, nem JPA.
- Regra de negócio existe só em `dominio` — nunca em controlador, repositório, DTO ou tela.
- Toda borda entra pela API. Não há caminho paralelo até o domínio.
- Transação abre na `aplicacao`, e é ela que carrega o ambiente para o RLS.
- Rotina automática é caso de uso, é idempotente, recupera atraso e grava evento.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Quem impede a violação da fronteira, e por que por teste | `ADR-0010` |
| Por que o pacote espelha o doc | `ADR-0008` |
| O grafo entre assuntos, e como dois conversam | `01-arquitetura/modulos` |
| Onde criar cada arquivo | `01-arquitetura/estrutura-de-pastas` |
| Nomes de classe, DTO × domínio × JPA, exceções | `01-arquitetura/padroes-de-codigo` |
| Autenticação, contexto do ambiente e RLS | `01-arquitetura/seguranca`, `ADR-0002` |
