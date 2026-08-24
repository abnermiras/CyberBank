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

---

## 5. Exclusão de Dados

O CyberBank não realiza exclusão física de dados financeiros ou de
recursos que possuam histórico de utilização.

A exclusão de um recurso é realizada de forma lógica.

Um recurso excluído passa para o estado `Desativado` e não pode mais ser
utilizado para novas operações.

Os dados históricos relacionados ao recurso permanecem armazenados.

A desativação não remove:

- movimentações;
- extratos;
- históricos;
- relacionamentos;
- registros financeiros realizados anteriormente.

As informações históricas continuam disponíveis conforme as regras de
acesso e compartilhamento aplicáveis.

## 6. Administração do Ambiente Compartilhado

O proprietário do ambiente mantém a propriedade e o controle sobre o
compartilhamento do ambiente.

Somente o proprietário pode:

- enviar convites para novos usuários;
- revogar o acesso de outros usuários;
- administrar os usuários que possuem acesso ao ambiente.

Um usuário que recebeu acesso ao ambiente, mesmo possuindo permissão
`Controle Total`, não pode compartilhar o ambiente com outros usuários.

Um usuário que recebeu acesso ao ambiente não pode revogar o acesso de
outros usuários.

O usuário que recebeu acesso pode revogar somente o próprio acesso ao
ambiente.

A permissão `Controle Total` concede controle operacional sobre os
recursos do ambiente, mas não transfere a propriedade do ambiente nem o
poder de administrar seus acessos.
