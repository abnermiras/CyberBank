# Regras de Negócio

## 1. Controle Financeiro

O CyberBank possui um controle financeiro responsável por armazenar,
controlar e evidenciar as movimentações financeiras dos usuários.

As informações financeiras são organizadas dentro de ambientes financeiros.

---

## 2. Usuários

Um usuário pode possuir ou participar de um ou mais ambientes financeiros.

Um usuário pode possuir diferentes níveis de acesso dentro de um ambiente
financeiro.

---

## 3. Ambientes Financeiros

O ambiente financeiro é o contexto no qual as informações financeiras são
organizadas, controladas e compartilhadas.

Um usuário pode possuir mais de um ambiente financeiro.

Um ambiente financeiro pode ser compartilhado com outros usuários.

O proprietário do ambiente define quais usuários podem acessar o ambiente
e quais permissões cada usuário possui.

### 3.1 Permissões

Um usuário pode possuir as seguintes permissões:

- `Leitura` — permite visualizar informações;
- `Controle Total` — permite visualizar, criar e alterar informações
  permitidas dentro do ambiente.

---

## 4. Compartilhamento

O compartilhamento permite disponibilizar informações ou recursos
financeiros para outros usuários ou ambientes financeiros.

O compartilhamento não implica duplicação do recurso financeiro.

O recurso original continua existindo como uma única entidade financeira,
mesmo quando disponibilizado em múltiplos ambientes.

O proprietário do recurso pode revogar o compartilhamento quando a regra
do recurso permitir.

As permissões determinam quais operações cada usuário pode realizar sobre
os recursos compartilhados.
