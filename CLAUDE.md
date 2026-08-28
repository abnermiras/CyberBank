# Cyberbank

App de finanças pessoais auto-hospedado e **multiusuário**. Evolução do RaspyBank —
**não é refatoração**: modelo de domínio e arquitetura novos. Aposta central: enxergar a
vida financeira inteira — entrada, gasto, investido, guardado. O **ambiente financeiro**
é o dono de todo dado; usuário só tem acesso a ambientes.

**Stack:** Java 21 + Spring Boot · PostgreSQL · Docker · Raspberry Pi (self-hosted, custo externo zero).

---

## Regra de contexto (leia antes de qualquer coisa)

**Não faça varredura no repositório.** Nada de `grep`/`glob` amplo, nada de ler `docs/`
inteiro, nada de abrir arquivos "para entender o projeto". Este arquivo é o roteador:
localize a tarefa na tabela abaixo, leia **apenas** os docs listados e vá.

- Se a tarefa não estiver na tabela → leia `docs/INDEX.md` (só ele) e escolha de lá.
- Se um doc listado referenciar outro em `ler-junto:`, leia **só se precisar de fato**.
- Se um doc estiver com `status: stub`, o conteúdo não existe: **pergunte, não invente**.
- Terminou uma tarefa? Atualize o doc dono do fato que mudou (a tabela diz qual é).
- Antes de mudar de tarefa, rode `/clear`. Contexto de tarefa anterior é desperdício.

## Roteamento: tarefa → documentos

| Tarefa | Leia exatamente |
|---|---|
| Novo meio de pagamento | `docs/08-fluxos/novo-meio-de-pagamento.md` |
| Nova integração externa (banco, extrato, canal) | `docs/08-fluxos/nova-integracao-externa.md` |
| Novo endpoint / mudança de contrato de API | `docs/08-fluxos/novo-endpoint.md` |
| Mudança de schema / nova migration | `docs/08-fluxos/nova-migration.md` |
| Papel, convite ou permissão de ambiente | `docs/02-dominio/ambiente-financeiro.md` |
| Compartilhar conta ou cartão entre ambientes | `docs/02-dominio/compartilhamento.md` |
| Nova regra de negócio ou mudança de invariante | `docs/08-fluxos/nova-regra-de-dominio.md` |
| Corrigir bug | `docs/08-fluxos/correcao-de-bug.md` |
| Mexer no bot do Telegram | `docs/05-integracoes/telegram-bot.md` + `docs/06-interface/bot-conversas.md` |
| Mexer na captura de notificação de compra | `docs/05-integracoes/captura-notificacao.md` + `docs/02-dominio/lancamento.md` |
| Categorização automática | `docs/02-dominio/regras-categorizacao.md` + `docs/02-dominio/categoria.md` |
| Fatura, parcelamento, recorrência | `docs/02-dominio/fatura-cartao.md` + `docs/02-dominio/recorrencia.md` |
| Pagar fatura, rolagem, corrigir fatura paga | `docs/02-dominio/fatura-pagamento.md` |
| Dashboard / relatórios | `docs/06-interface/dashboard.md` + `docs/04-api/endpoints-relatorios.md` |
| Deploy, ambiente, incidente | `docs/07-operacao/deploy.md` ou `docs/07-operacao/runbook.md` |
| Escrever/ajustar testes | `docs/07-operacao/testes.md` |
| Decisão arquitetural nova | `docs/01-arquitetura/decisoes/README.md` |
| Não sei / é outra coisa | `docs/INDEX.md` |

Os fluxos em `docs/08-fluxos/` já trazem, no topo, a lista fechada de docs a abrir —
é a rota mais barata. Prefira sempre entrar por um fluxo.

## Regras duras do código

1. Regra de negócio vive no domínio. Nunca em controller, nunca em repository.
2. Entidade de domínio ≠ entidade JPA ≠ DTO de API. A conversão é explícita.
3. Nada de dependência nova sem ADR. Nada de serviço pago. Nada que não rode no Pi.
4. Toda mudança de schema é migration versionada — nunca `ddl-auto: update`.
5. Todo valor monetário é inteiro em centavos. Toda data/hora é armazenada em UTC.
6. Um fato mora em um doc só. Se você precisou repetir, é sinal de que está no doc errado.

## Comandos

Build/run/test: `docs/07-operacao/build-e-run.md`. Não os reproduza aqui.
