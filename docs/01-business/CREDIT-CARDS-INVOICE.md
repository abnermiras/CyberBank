# Faturas de Cartão de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Faturas de Cartão de Crédito** do CyberBank.

O documento trata da fatura como unidade de consolidação e liquidação financeira de um contrato de crédito.

As regras do cartão de crédito propriamente dito são definidas em:

`CREDIT-CARDS.md`

---

## 2. Conceito de Fatura

A fatura é uma representação financeira de um ciclo mensal de um **contrato de crédito**.

A fatura não pertence a um cartão individual.

Todos os cartões vinculados ao mesmo contrato participam da mesma fatura do contrato.

A fatura consolida os lançamentos realizados pelos diferentes cartões vinculados ao contrato durante o respectivo ciclo de faturamento.

---

## 3. Relação com o Contrato de Crédito

A relação fundamental é:

```text
Contrato de Crédito
        │
        └── Fatura mensal
             │
             ├── Cartão principal
             │   └── Lançamentos
             │
             ├── Cartões virtuais
             │   └── Lançamentos
             │
             ├── Cartões adicionais
             │   └── Lançamentos
             │
             └── Cartões compartilhados
                 └── Lançamentos
```

A instituição mantenedora do contrato não determina a fatura. A fatura é determinada pelo contrato de crédito.

Contratos diferentes possuem faturas independentes, mesmo que pertençam à mesma instituição mantenedora.

---

## 4. Periodicidade

A fatura é mensal.

Cada contrato possui uma fatura correspondente a cada ciclo mensal de faturamento, conforme as regras de ciclo, fechamento e vencimento definidas neste domínio.

A periodicidade da fatura não depende da quantidade de cartões vinculados ao contrato.

Um contrato com um cartão e um contrato com vários cartões continuam possuindo uma fatura por ciclo.

---

## 5. Composição da Fatura

Uma fatura reúne todos os lançamentos pertencentes ao contrato e ao respectivo ciclo de faturamento.

Podem compor uma mesma fatura lançamentos realizados por:

- cartão principal;
- cartões virtuais;
- cartões adicionais;
- cartões compartilhados.

A fatura deve manter a identificação do cartão utilizado em cada lançamento.

Quando aplicável, a fatura também deve identificar o usuário titular do cartão e o usuário responsável pela realização do lançamento.

Essas informações são independentes.

Exemplo:

```text
Cartão: ****-0202
Titular do cartão: Usuário A
Responsável pela compra: Usuário B
```

---

## 6. Valor da Fatura

O valor da fatura corresponde à soma dos lançamentos que compõem o ciclo daquele contrato.

Conceitualmente:

```text
Valor da Fatura
    = soma dos lançamentos do contrato no ciclo
```

O total não é calculado individualmente por cartão para formar faturas separadas. Os valores dos diferentes cartões são consolidados na mesma fatura do contrato.

Exemplo:

```text
Cartão físico ****-1234        R$ 80,00
Cartão adicional ****-5678     R$ 10,00
Cartão virtual ****-0101       R$ 100,00
Cartão compartilhado ****-0202 R$ 20,00
                              ---------
Total da Fatura               R$ 210,00
```

A fatura pode apresentar subtotais por cartão para facilitar a consulta, mas o total devido pertence à fatura do contrato.

---

## 7. Exemplo Completo

Contrato do Usuário A no ambiente `CLT`.

Instituição mantenedora: `Nubank`.

### Fatura Nubank — 08/2026

```text
Total da Fatura:       R$ 210,00
Data de fechamento:    20/08/2026
Data de vencimento:    31/08/2026
```

### Cartão físico ****-1234

```text
Titular: Usuário A

Compra 1                         R$ 10,00
Compra 2                         R$ 20,00
Compra 3 — parcela 2/3           R$ 50,00
                                  ---------
Total do cartão                  R$ 80,00
```

### Cartão adicional ****-5678

```text
Titular: Usuário B

Compra 1                         R$ 10,00
                                  ---------
Total do cartão                  R$ 10,00
```

O cartão continua pertencendo ao contrato do Usuário A, mesmo tendo Usuário B como titular operacional do cartão adicional.

### Cartão virtual ****-0101

```text
Titular: Usuário A

Compra 1                         R$ 100,00
                                  ---------
Total do cartão                  R$ 100,00
```

### Cartão virtual ****-0202 — Compartilhado

```text
Titular: Usuário A
Acesso cedido para: Usuário B

Compra 1                         R$ 10,00
Responsável pela compra: Usuário A

Compra 2                         R$ 10,00
Responsável pela compra: Usuário B
                                  ---------
Total do cartão                  R$ 20,00
```

### Total da Fatura

```text
R$ 80,00
+ R$ 10,00
+ R$ 100,00
+ R$ 20,00
---------
R$ 210,00
```

---

## 8. Identificação dos Lançamentos

Cada lançamento apresentado na fatura deve permitir identificar, quando aplicável:

- o cartão utilizado;
- o titular do cartão;
- o usuário responsável pela compra;
- a descrição da operação;
- o valor da operação;
- informações de parcelamento, quando aplicável.

A identificação do cartão é necessária mesmo quando vários cartões pertencem ao mesmo contrato.

Isso permite ao titular compreender como o valor total da fatura foi formado.

---

## 9. Cartões Adicionais na Fatura

Um cartão adicional não possui uma fatura própria.

Os lançamentos realizados pelo cartão adicional são incluídos na fatura do contrato ao qual o cartão pertence.

Exemplo:

```text
Contrato do Usuário A
│
├── Cartão principal ****-1234
│   └── R$ 80,00
│
└── Cartão adicional ****-5678
    └── R$ 10,00

Fatura do contrato: R$ 90,00
```

O usuário do cartão adicional pode ser identificado na composição da fatura, mas isso não cria uma nova fatura.

---

## 10. Cartões Compartilhados na Fatura

Um cartão compartilhado também não possui uma fatura própria.

Os lançamentos realizados por todos os usuários autorizados no mesmo cartão são consolidados na fatura do contrato.

A fatura deve preservar o responsável por cada lançamento quando o cartão for utilizado por mais de um usuário.

Exemplo:

```text
Cartão ****-0202
Titular: Usuário A

Compra 1 — R$ 10,00 — Usuário A
Compra 2 — R$ 10,00 — Usuário B

Total do cartão: R$ 20,00
```

O compartilhamento do cartão não cria uma segunda fatura nem divide automaticamente a responsabilidade financeira em faturas separadas.

---

## 11. Fechamento e Vencimento

A fatura possui, no mínimo, as seguintes referências de calendário:

- período/ciclo de faturamento;
- data de fechamento;
- data de vencimento.

No exemplo definido pelo domínio:

```text
Fatura:              08/2026
Data de fechamento:  20/08/2026
Data de vencimento:  31/08/2026
```

As regras detalhadas para determinar o ciclo, executar o fechamento, permitir ou impedir alterações após o fechamento e tratar a data de vencimento serão evoluídas neste documento.

---

## 12. Pagamento e Quitação

A fatura representa o valor financeiro consolidado que deverá ser liquidado conforme as regras de pagamento do CyberBank.

O pagamento e a quitação da fatura são conceitos distintos da realização dos lançamentos que compõem a fatura.

As regras detalhadas sobre:

- pagamento integral;
- pagamento parcial;
- múltiplos pagamentos;
- pagamento antecipado;
- quitação;
- pagamentos em atraso;
- origem do pagamento;
- data de efetivação do pagamento;

serão definidas neste domínio antes da implementação definitiva.

---

## 13. Relação com o Limite do Contrato

A fatura está vinculada ao mesmo contrato que possui o limite global utilizado pelos cartões.

Os lançamentos de todos os cartões do contrato participam do consumo do limite global conforme as regras definidas no domínio de cartões e neste domínio de faturas.

A forma e o momento em que o limite comprometido é liberado após pagamentos ou quitação pertencem às regras de liquidação da fatura.

Não existe limite independente por fatura ou por cartão.

---

## 14. Parcelamentos

Lançamentos parcelados podem aparecer em diferentes faturas do mesmo contrato ao longo dos ciclos correspondentes.

A fatura deve identificar a parcela apresentada quando essa informação fizer parte do lançamento.

Exemplo:

```text
Compra original: R$ 150,00
Parcelamento: 3 x R$ 50,00

Fatura 1: parcela 1/3 — R$ 50,00
Fatura 2: parcela 2/3 — R$ 50,00
Fatura 3: parcela 3/3 — R$ 50,00
```

O comportamento detalhado do limite para compras parceladas é definido no domínio de cartões, enquanto a apresentação das parcelas nas faturas pertence a este domínio.

---

## 15. Estornos e Ajustes

Estornos, créditos e outros ajustes que afetem o valor de uma fatura devem ser representados de forma rastreável e relacionados à operação que originou o ajuste, quando aplicável.

O tratamento financeiro detalhado de estornos, créditos e ajustes ainda será definido neste documento.

---

## 16. Permissões

As permissões para consultar ou operar uma fatura dependem das permissões que o usuário possui sobre o contrato e os cartões relacionados.

A regra detalhada de autorização para:

- visualizar a fatura completa;
- visualizar apenas determinados cartões;
- realizar pagamentos;
- consultar lançamentos de outros usuários;

ainda será definida neste domínio.

A existência de um cartão adicional ou compartilhado não deve, por si só, ser interpretada como autorização automática para todas as operações sobre a fatura.

---

## 17. Histórico e Auditoria

A fatura deve preservar o histórico necessário para explicar como seu valor foi formado e como foi liquidado.

Devem permanecer rastreáveis, quando aplicável:

- contrato ao qual a fatura pertence;
- ciclo da fatura;
- lançamentos que compõem a fatura;
- cartão utilizado em cada lançamento;
- titular do cartão;
- responsável pelo lançamento;
- valor de cada lançamento;
- fechamento;
- pagamentos;
- quitação;
- estornos e ajustes;
- alterações relevantes do estado da fatura.

A remoção de um cartão não deve ser utilizada para eliminar o histórico de lançamentos que já pertenceu a uma fatura.

---

## 18. Invariantes do Domínio da Fatura

As seguintes regras devem permanecer verdadeiras:

1. Uma fatura pertence a um contrato de crédito.
2. Uma fatura não pertence a um cartão individual.
3. A fatura é mensal.
4. Todos os cartões vinculados ao mesmo contrato podem contribuir com lançamentos para a mesma fatura.
5. Cartões adicionais não possuem faturas próprias.
6. Cartões compartilhados não possuem faturas próprias.
7. O valor da fatura corresponde à consolidação dos lançamentos pertencentes ao contrato e ao ciclo.
8. A fatura deve preservar a identificação do cartão utilizado em cada lançamento.
9. O titular do cartão e o responsável pelo lançamento podem ser usuários diferentes.
10. Um cartão compartilhado pode possuir lançamentos de usuários diferentes na mesma fatura.
11. Contratos diferentes possuem faturas independentes.
12. Não existe limite de crédito independente por fatura.
13. Regras específicas de fechamento, vencimento, pagamento, quitação, estorno e liberação de limite devem ser definidas neste domínio e não duplicadas em `CREDIT-CARDS.md`.
