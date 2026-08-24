# Regras de Negócio

## 1. Controle Financeiro

O CyberBank possui um controle financeiro responsável por armazenar,
controlar e evidenciar as movimentações financeiras dos usuários.

As informações financeiras são organizadas dentro de ambientes financeiros.

---

## 2. Usuário

Um usuário pode possuir ou participar de um ou mais ambientes financeiros.

Um usuário pode possuir diferentes níveis de acesso dentro de um ambiente
financeiro.

As permissões de acesso podem determinar se o usuário pode apenas
visualizar informações ou também alterá-las.

---

## 3. Ambiente Financeiro

O ambiente financeiro é o contexto no qual as informações financeiras são
organizadas, controladas e compartilhadas.

Um usuário pode possuir mais de um ambiente financeiro.

Um ambiente financeiro pode ser compartilhado com outros usuários.

O proprietário do ambiente define quais usuários podem acessar o ambiente
e quais permissões cada usuário possui.

### 3.1 Permissões

Um usuário pode possuir permissão de:

- `read` — visualizar informações;
- `update` — alterar informações.

Usuários com permissão `update` podem criar e alterar informações
permitidas dentro do ambiente.

Por exemplo, usuários com permissão `update` podem criar categorias e
subcategorias.

---

## 4. Contas Bancárias

Um usuário pode cadastrar uma ou mais contas bancárias.

Uma conta bancária pertence a um ambiente financeiro.

Uma conta bancária pode ser compartilhada com outro ambiente financeiro.

### 4.1 Compartilhamento de Conta entre Ambientes

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

### 4.2 Acesso à Conta

Todos os usuários que possuem acesso à conta podem realizar operações
sobre ela de acordo com suas permissões.

Um usuário pode possuir acesso somente para visualização ou acesso para
alteração, conforme a permissão concedida pelo proprietário.

---

## 5. Categorias

O usuário pode criar categorias para classificar suas movimentações
financeiras.

As categorias podem representar:

- entradas de dinheiro;
- saídas de dinheiro.

Uma categoria deve estar associada a um tipo de movimentação.

Em um ambiente compartilhado, usuários com permissão `update` podem
criar e alterar categorias.

---

## 6. Subcategorias

O usuário pode criar subcategorias para detalhar uma categoria em um
segundo nível.

Uma subcategoria pertence a uma categoria.

As subcategorias permitem maior detalhamento das receitas e despesas.

Em um ambiente compartilhado, usuários com permissão `update` podem
criar e alterar subcategorias.

---

## 7. Formas de Pagamento e Recebimento

O usuário pode cadastrar formas de pagamento e recebimento.

As formas de pagamento e recebimento podem ser associadas às contas
bancárias.

---

## 8. Cartões de Crédito

O usuário pode cadastrar cartões de crédito.

Um cartão de crédito pode estar associado a uma conta bancária, mas essa
associação não é obrigatória.

O cartão de crédito pertence ao usuário titular do contrato.

O titular é responsável pelo contrato, limite global e faturas
relacionadas ao cartão.

### 8.1 Limite de Crédito

Um contrato de cartão de crédito possui um limite global.

Todos os cartões vinculados ao mesmo contrato compartilham esse limite.

O consumo realizado por qualquer cartão vinculado ao contrato reduz o
limite disponível globalmente.

---

## 9. Cartões Virtuais

O titular de um cartão de crédito pode criar cartões virtuais vinculados
ao mesmo contrato.

Cada cartão virtual possui sua própria identificação.

As compras realizadas por cartões virtuais consomem o limite global do
contrato.

Os cartões virtuais podem gerar faturas separadas.

---

## 10. Cartões Adicionais

O titular de um cartão de crédito pode solicitar cartões adicionais para
outros usuários.

O cartão adicional não constitui um novo contrato de cartão de crédito.

O cartão adicional pertence ao contrato do titular.

O usuário adicional recebe o direito de utilizar o cartão, mas o titular
continua sendo o responsável pelo contrato.

As compras realizadas pelo cartão adicional:

- consomem o limite global do contrato;
- pertencem ao contrato do titular;
- são apresentadas ao titular nas informações de faturamento;
- podem possuir identificação própria para permitir o controle do
  usuário que realizou as despesas.

---

## 11. Compartilhamento de Cartão de Crédito

O titular de um cartão pode compartilhar o uso do cartão com outro
usuário.

O compartilhamento não transfere a propriedade do cartão ou do contrato.

O titular pode revogar o compartilhamento a qualquer momento.

---

## 12. Movimentações Financeiras

As movimentações financeiras são registradas dentro de um ambiente
financeiro.

Uma movimentação pode utilizar elementos financeiros cadastrados ou
compartilhados no ambiente, incluindo:

- conta bancária;
- categoria;
- subcategoria;
- forma de pagamento;
- forma de recebimento;
- cartão de crédito.

Quando uma movimentação utiliza uma conta bancária compartilhada entre
ambientes, ela pertence à mesma conta independentemente do ambiente a
partir do qual foi registrada.

---

## 13. Compartilhamento

O compartilhamento permite disponibilizar informações ou recursos
financeiros para outros usuários ou ambientes financeiros.

O compartilhamento não implica duplicação do recurso financeiro.

O recurso original continua existindo como uma única entidade financeira,
mesmo quando disponibilizado em múltiplos ambientes.

O proprietário do recurso pode revogar o compartilhamento quando a regra
do recurso permitir.

As permissões determinam quais operações cada usuário pode realizar sobre
os recursos compartilhados.
