---
id: 01-arquitetura/decisoes/README
titulo: Registro de decisões arquiteturais
dono: índice das ADRs e regra de quando escrever uma
ler-junto: []
status: ativo
---

# Registro de decisões (ADR)

Uma ADR responde **"por que assim e não de outro jeito"**. É o doc que evita a pergunta
mais cara de todas: reabrir uma decisão já tomada porque ninguém lembra do motivo.

## Quando escrever uma ADR

Escreva quando: entra dependência ou tecnologia nova · muda a estrutura de módulos ·
troca-se um padrão já estabelecido · escolhe-se entre duas opções defensáveis ·
aceita-se um trade-off que vai incomodar depois.

**Não** escreva para: escolha óbvia sem alternativa real, detalhe de implementação,
nome de variável, ou coisa que cabe no doc do módulo.

## Regras

- Numeração sequencial, **nunca reaproveitada**. Arquivo: `ADR-NNNN-titulo-curto.md`.
- Uma página. Se passou disso, a decisão não está madura.
- ADR **não se edita** depois de aceita. Mudou de ideia? Escreva uma nova com
  `Substitui: ADR-NNNN` e marque a antiga como `Substituída por: ADR-MMMM`.
- Registre a alternativa **descartada** e o motivo. É a parte que mais vale depois.

## Índice

| # | Decisão | Status | Data |
|---|---|---|---|
| 0000 | Template | — | — |
| 0001 | [Manter o nome "ambiente financeiro"](ADR-0001-nome-ambiente-financeiro.md) | aceita | 2026-08-27 |
| 0002 | [Isolamento por ambiente em duas camadas](ADR-0002-isolamento-por-ambiente.md) | aceita | 2026-08-27 |
| 0003 | [O contrato de cartão de crédito é uma conta](ADR-0003-cartao-de-credito-e-conta.md) | aceita | 2026-08-28 |
| 0004 | [Conta e cartão podem ser compartilhados entre ambientes](ADR-0004-compartilhamento-entre-ambientes.md) | aceita | 2026-08-28 |
| 0005 | [O que vence sem ser pago rola para a fatura seguinte](ADR-0005-rolagem-entre-faturas.md) | aceita · substitui uma consequência da 0003 | 2026-08-28 |

_(adicione uma linha por ADR)_
