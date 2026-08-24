# Contas Bancárias

## 1. Cadastro da Conta

O usuário pode cadastrar uma conta bancária informando:

- nome da conta;
- descrição;
- saldo inicial;
- moeda;
- formas de pagamento;
- formas de recebimento.

Todos os campos são obrigatórios.

O nome da conta é definido livremente pelo usuário e pode representar a
instituição ou a forma como o usuário identifica o recurso, por exemplo:

- Nubank;
- C6;
- Santander;
- Carteira;
- Bolsa;
- Cofre.

A descrição permite identificar a finalidade da conta, por exemplo:

- Salário;
- Família;
- Financiamento.

O CyberBank não exige o cadastro de dados bancários formais, como número
da agência, número da conta ou dígito.

### 1.1 Formas de Pagamento

O usuário deve selecionar as formas de pagamento aceitas pela conta.

As formas disponíveis são:

- Débito;
- Débito Automático;
- PIX;
- TED;
- Desconto em Folha de Pagamento;
- Dinheiro.

### 1.2 Formas de Recebimento

O usuário deve selecionar as formas de recebimento aceitas pela conta.

As formas disponíveis são:

- Crédito em Conta;
- PIX;
- Dinheiro.

### 1.3 Conta de Dinheiro

Quando a forma `Dinheiro` for selecionada para uma conta, a conta passa a
representar dinheiro físico.

Uma conta que representa dinheiro físico não pode utilizar outras formas
de pagamento ou recebimento.

Nesse caso, a conta aceita exclusivamente:

- Pagamento: `Dinheiro`;
- Recebimento: `Dinheiro`.

Uma conta de dinheiro pode receber qualquer nome definido pelo usuário,
como:

- Carteira;
- Bolsa;
- Bolso;
- Cofre;
- Colchão.
---

## 2. Compartilhamento entre Ambientes

Quando uma conta bancária é compartilhada com outro ambiente financeiro,
o ambiente de destino passa a ter acesso às informações financeiras da
mesma conta.

O compartilhamento inclui:

- saldo;
- extrato;
- movimentações;
- formas de pagamento e recebimento associadas à conta.

A conta continua sendo uma única conta bancária.

O compartilhamento não cria uma nova conta nem duplica seus dados
financeiros.

Movimentações realizadas a partir de qualquer ambiente que tenha acesso
à conta são contabilizadas na mesma conta bancária.

Alterações no saldo e no histórico da conta são refletidas em todos os
ambientes que possuem acesso àquela conta.

---

## 3. Acesso

Todos os usuários que possuem acesso à conta podem realizar operações
sobre ela de acordo com suas permissões.

Um usuário pode possuir acesso somente para visualização ou acesso para
alteração, conforme a permissão concedida.
