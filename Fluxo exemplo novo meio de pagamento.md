---
id: 08-fluxos/novo-meio-de-pagamento
titulo: "Fluxo: adicionar um meio de pagamento"
dono: roteiro de implementação de um novo meio de pagamento
ler-junto: []
status: ativo
---

# Fluxo: adicionar um meio de pagamento

Use quando entrar um novo tipo de meio de pagamento (ex.: vale-refeição, cartão
virtual, boleto, Pix agendado) ou uma nova instância de um tipo já existente.

## 1. Contexto a carregar

**Leia, nesta ordem, e nada além disto:**

1. `docs/02-dominio/meio-de-pagamento.md` — modelo, tipos existentes, o que cada um deve responder
2. `docs/02-dominio/lancamento.md` — como o lançamento referencia o meio de pagamento
3. `docs/04-api/endpoints-meios-pagamento.md` — contrato exposto
4. `docs/03-dados/catalogo-tabelas.md` — **só a seção da tabela `meio_pagamento`**

**Condicionais** — abra apenas se a resposta for sim:

| Abra também | Quando |
|---|---|
| `docs/02-dominio/fatura-cartao.md` | o novo meio tem ciclo de fatura ou permite parcelamento |
| `docs/05-integracoes/captura-notificacao.md` | as compras nesse meio chegam por notificação push |
| `docs/02-dominio/regras-categorizacao.md` | o meio muda como a categoria é inferida (ex.: VR ⇒ alimentação) |
| `docs/03-dados/migrations.md` | o passo 3 exigir mudança de schema |

**Não abra:** produto, arquitetura geral, dashboard, operação, integrações não citadas.

## 2. Decidir antes de escrever código

Responda as quatro e registre a resposta no doc de domínio:

1. É um **tipo novo** de meio de pagamento ou uma **instância** de tipo existente?
   Instância nova = só dado, sem código. Se for isso, pule para o passo 5.
2. Ele muda o **ciclo de vida do lançamento**? (posterga débito, agrupa em fatura,
   gera parcelas, tem saldo próprio a debitar)
3. Ele tem **limite ou saldo** que o sistema precisa acompanhar?
4. As compras nele são **capturadas automaticamente** ou só entram manualmente?

Se a resposta de 2 ou 3 for "sim" e ainda não houver desenho para isso no doc de
domínio, **pare e pergunte** — não improvise a regra.

## 3. Domínio primeiro

- Estenda o modelo em `meio-de-pagamento` **sem** tocar em `Lancamento`: o lançamento
  referencia o meio, não conhece seus tipos. Se você precisou de `if (tipo == X)` dentro
  de `Lancamento`, o desenho está errado — a variação pertence ao meio de pagamento.
- Cubra a regra nova com teste de domínio **antes** de persistência e API
  (ver `docs/07-operacao/testes.md`).

## 4. Persistência

- Migration versionada. Nunca alterar migration já aplicada (`docs/03-dados/migrations.md`).
- Valor sempre em centavos (inteiro). Nada de `double`.
- Novo tipo em coluna discriminadora ⇒ atualizar a constraint **e** os dados de referência.

## 5. API e bordas

- Endpoint só depois do domínio verde.
- Campo novo no payload é **aditivo**: cliente antigo não pode quebrar
  (`docs/04-api/convencoes.md`).
- Se aparece na conversa do bot, o texto vai em `docs/06-interface/bot-conversas.md` —
  não invente mensagem nova sem registrar lá.

## 6. Atualizar os docs (obrigatório)

| Mudou | Atualize |
|---|---|
| Tipo, regra ou campo do meio | `docs/02-dominio/meio-de-pagamento.md` |
| Schema | `docs/03-dados/catalogo-tabelas.md` |
| Contrato | `docs/04-api/endpoints-meios-pagamento.md` |
| Comportamento de fatura/parcela | `docs/02-dominio/fatura-cartao.md` |
| Escolha estrutural discutível | novo ADR em `docs/01-arquitetura/decisoes/` |

## 7. Pronto quando

- [ ] Teste de domínio cobrindo a regra específica do meio novo, passando
- [ ] Migration aplica e reverte em base limpa
- [ ] Contrato de API atualizado com exemplo literal de payload
- [ ] Docs da tabela do passo 6 atualizados
- [ ] Nenhum `if` por tipo de meio fora do módulo de meio de pagamento