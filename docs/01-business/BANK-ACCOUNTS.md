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

## 4. Desativação da Conta

Uma conta bancária pode ser desativada.

A desativação impede novos lançamentos e operações financeiras na conta.

A desativação não remove o histórico financeiro da conta.

O histórico de movimentações, extratos e demais registros realizados
antes da desativação permanecem disponíveis.

### 4.1 Lançamentos Futuros

Ao desativar uma conta, todos os lançamentos futuros associados à conta
são excluídos.

Esses lançamentos não fazem mais parte dos cálculos ou projeções do
ambiente.

Caso a conta seja reativada, os lançamentos futuros anteriormente
excluídos não são restaurados e devem ser cadastrados novamente.

### 4.2 Patrimônio

Uma conta desativada deixa de compor o patrimônio do ambiente.

O saldo que a conta possuía no momento da desativação deixa de ser
considerado no patrimônio atual.

O histórico financeiro da conta permanece disponível mesmo após sua
desativação.

### 4.3 Reativação

Uma conta desativada pode ser reativada.

Ao reativar uma conta, o usuário deve informar novamente o saldo atual.

O saldo informado na reativação representa a situação financeira atual
da conta.

O CyberBank não assume que o saldo anterior à desativação permanece
válido, pois podem ter ocorrido movimentações fora do sistema durante o
período em que a conta esteve desativada.

A reativação não restaura os lançamentos futuros que foram excluídos
durante a desativação.

---

## 5. Compartilhamento entre Ambientes

Uma conta bancária pode ser compartilhada entre ambientes financeiros.

O compartilhamento pode ocorrer:

- entre ambientes pertencentes ao mesmo usuário;
- entre ambientes pertencentes a usuários diferentes.

### 5.1 Compartilhamento entre Ambientes do Mesmo Usuário

O usuário pode compartilhar uma conta bancária de um de seus ambientes
com outro ambiente de sua propriedade.

A conta permanece única e passa a estar disponível nos dois ambientes.

### 5.2 Compartilhamento com Outro Usuário

Um usuário pode compartilhar uma conta bancária com outro usuário.

O usuário que recebe o compartilhamento recebe um convite.

O destinatário deve escolher em qual de seus ambientes financeiros a conta
compartilhada será disponibilizada.

A conta somente passa a estar disponível no ambiente escolhido após a
aceitação do convite.

### 5.3 Permissões do Compartilhamento

O proprietário da conta define a permissão concedida ao usuário ou
ambiente que receberá o compartilhamento.

As permissões disponíveis são:

- `Leitura` — permite visualizar o extrato da conta;
- `Controle Total` — permite visualizar o extrato e realizar novos
  lançamentos na conta.

A permissão `Leitura` não permite criar, alterar ou excluir lançamentos.

A permissão `Controle Total` permite realizar lançamentos de gastos e
recebimentos na conta, utilizando as categorias e subcategorias
existentes no ambiente do usuário que realizou o lançamento.

### 5.4 Informações de Movimentações Compartilhadas

O extrato da conta compartilhada é único e pode ser visualizado pelos
usuários que possuem acesso à conta.

As categorias e subcategorias pertencentes ao ambiente de origem de uma
movimentação não são compartilhadas automaticamente.

Quando um usuário visualizar uma movimentação realizada por outro
usuário, as informações de categoria e subcategoria podem ser omitidas.

O usuário ainda poderá visualizar informações gerais da movimentação,
como:

- valor;
- data;
- forma de pagamento ou recebimento;
- usuário responsável pelo lançamento.

### 5.5 Lançamentos em Conta Compartilhada

Um usuário com permissão `Controle Total` pode realizar lançamentos na
conta compartilhada.

O lançamento é associado ao ambiente no qual foi realizado.

O lançamento permanece visível no extrato da conta para todos os usuários
que possuem acesso à conta.

Cada usuário visualiza as informações de classificação do lançamento de
acordo com as regras de acesso ao ambiente de origem.

### 5.6 Patrimônio em Contas Compartilhadas

Uma conta compartilhada não compõe o patrimônio do usuário ou ambiente
que recebeu o compartilhamento.

O saldo da conta compartilhada continua compondo somente o patrimônio do
proprietário da conta e dos ambientes do proprietário aos quais a conta
está vinculada.

O acesso compartilhado permite visualizar e, quando autorizado, realizar
lançamentos na conta, mas não concede propriedade sobre o saldo da conta.

### 5.7 Revogação do Compartilhamento

O proprietário da conta pode revogar o compartilhamento.

A revogação remove o acesso do usuário ou ambiente ao recurso.

A revogação não remove o histórico de movimentações realizadas enquanto
o compartilhamento estava ativo.

---

## 6. Acesso

O proprietário da conta pode definir a permissão concedida ao usuário ou
ambiente que recebeu o compartilhamento.

As permissões disponíveis são:

- `Leitura`;
- `Controle Total`.

### 6.1 Leitura

A permissão `Leitura` permite visualizar o extrato da conta.

O usuário com `Leitura` não pode:

- criar movimentações;
- editar movimentações;
- excluir movimentações;
- alterar configurações da conta.

As categorias e subcategorias utilizadas nas movimentações pertencentes
a outros ambientes não são disponibilizadas.

O usuário pode visualizar informações básicas da movimentação, incluindo:

- valor;
- data;
- forma de pagamento ou recebimento;
- responsável pelo lançamento.

### 6.2 Controle Total

A permissão `Controle Total` permite realizar todas as operações
financeiras permitidas sobre a conta.

O usuário pode:

- visualizar o extrato;
- criar movimentações;
- editar movimentações;
- excluir movimentações;
- realizar operações permitidas pela conta.

O usuário pode utilizar suas próprias categorias e subcategorias nos
lançamentos realizados por ele.

Um usuário com `Controle Total` pode editar ou excluir movimentações
realizadas por qualquer outro usuário que tenha acesso à conta.

O proprietário da conta também pode editar ou excluir movimentações
realizadas por usuários com `Controle Total`.

### 6.3 Privacidade das Classificações

As categorias e subcategorias utilizadas em um lançamento pertencem ao
contexto do ambiente que realizou o lançamento.

No extrato compartilhado, o lançamento pode ser visualizado por todos os
usuários com acesso à conta, porém sua categoria e subcategoria somente
são apresentadas quando o usuário possui acesso ao contexto que realizou
a classificação.
