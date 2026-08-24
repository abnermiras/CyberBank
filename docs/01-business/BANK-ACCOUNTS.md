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

## 2. Saldo Inicial

Ao cadastrar uma conta, o usuário deve informar o saldo inicial.

O saldo inicial gera automaticamente uma movimentação sistêmica de
`Ajuste de Saldo`.

A movimentação possui o valor informado no cadastro da conta.

O `Ajuste de Saldo`:

- aparece no extrato da conta;
- utiliza a categoria sistêmica `Ajuste de Saldo`;
- compõe o patrimônio do ambiente;
- representa o saldo existente na conta no momento de seu cadastro.

O saldo inicial pode ser alterado posteriormente pelo usuário.

A alteração do saldo inicial deve atualizar a movimentação de `Ajuste de
Saldo` correspondente e recalcular os saldos e o patrimônio afetados.

---

## 3. Moedas da Conta

Uma conta bancária possui uma moeda principal, sendo `Real (BRL)` a
moeda padrão.

O usuário pode adicionar outras moedas à mesma conta.

Cada moeda possui seu próprio saldo dentro da conta.

Uma moeda adicional é identificada como uma sub-moeda da conta.

### 3.1 Compra de Moeda

O CyberBank permite realizar operações de compra de moeda dentro de uma
conta.

A compra de moeda utiliza a categoria sistêmica `Compra de Moeda`.

A operação deve informar:

- moeda de origem;
- moeda de destino;
- valor utilizado na moeda de origem;
- valor recebido na moeda de destino.

A compra de moeda gera movimentações relacionadas nos saldos das duas
moedas.

A movimentação na moeda de origem representa uma saída do valor utilizado.

A movimentação na moeda de destino representa uma entrada do valor
recebido.

### 3.2 Exemplo

Uma conta `Nubank` possui:

- BRL: R$ 1.000,00;
- USD: US$ 0,00.

O usuário realiza uma compra de US$ 100,00 utilizando R$ 500,00.

O resultado será:

**Extrato BRL**

- `Compra de Moeda USD`: -R$ 500,00.

**Extrato USD**

- `Compra de Moeda`: +US$ 100,00.

Após a operação, os saldos serão:

- BRL: R$ 500,00;
- USD: US$ 100,00.

As duas movimentações representam uma única operação de compra de moeda.

---

## 4. Compartilhamento entre Ambientes

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

## 5. Acesso

Todos os usuários que possuem acesso à conta podem realizar operações
sobre ela de acordo com suas permissões.

Um usuário pode possuir acesso somente para visualização ou acesso para
alteração, conforme a permissão concedida.
