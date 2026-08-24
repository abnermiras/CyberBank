# 1. Contas Bancárias
## BANK-ACCOUNTS.md

Um usuário pode cadastrar uma ou mais contas bancárias.

Uma conta bancária pertence a um ambiente financeiro.

Uma conta bancária pode ser compartilhada com outro ambiente financeiro.

---

## 2. Formas de Pagamento e Recebimento

O usuário pode cadastrar formas de pagamento e recebimento.

As formas de pagamento e recebimento podem ser associadas às contas
bancárias.

### 2.1 Compartilhamento de Conta entre Ambientes

Quando uma conta bancária é compartilhada com outro ambiente financeiro,
o ambiente de destino passa a ter acesso às informações financeiras da
mesma conta.

O compartilhamento inclui:

- saldo;
- extrato;
- movimentações;
- meios de pagamento e recebimento associados à conta.

A conta continua sendo uma única conta bancária.

O compartilhamento não cria uma nova conta nem duplica seus dados
financeiros.

Movimentações realizadas a partir de qualquer ambiente que tenha acesso
à conta são contabilizadas na mesma conta bancária.

Consequentemente, alterações no saldo e no histórico da conta são
refletidas em todos os ambientes que possuem acesso àquela conta.

### 2.2 Acesso à Conta

Todos os usuários que possuem acesso à conta podem realizar operações
sobre ela de acordo com suas permissões.

Um usuário pode possuir acesso somente para visualização ou acesso para
alteração, conforme a permissão concedida pelo proprietário.

---
