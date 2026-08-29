---
id: 02-dominio/ambiente-financeiro
titulo: Ambiente financeiro
dono: o ambiente como agregado dono do dado: papeis, acesso, convite, ciclo de vida e a regra de isolamento
ler-junto: [00-produto/glossario, 02-dominio/lancamento, 01-arquitetura/seguranca]
status: rascunho
---

# Ambiente financeiro

O conceito estruturante do Cyberbank. A definição canônica está em
`docs/00-produto/glossario.md`; aqui ficam as **regras**.

Uma frase que resume tudo: **o ambiente é o dono do dado; o usuário só tem acesso a
ambientes.** Nenhuma conta, lançamento, categoria, orçamento, aplicação ou meta pertence
diretamente a um usuário.

Isso continua verdade depois do compartilhamento de conta e cartão: lá o que atravessa é o
**uso** de um objeto, nunca a posse (`docs/02-dominio/compartilhamento.md`, `ADR-0004`).

> **Ambiente financeiro ≠ ambiente de execução.** Dev, homologação e produção são
> *ambientes de execução* e vivem em `docs/01-arquitetura/ambientes-de-execucao.md`.
> A convivência dos dois nomes foi decidida — ver `ADR-0001`.

## Papéis

Um acesso é o par (usuário, ambiente) mais um papel. Três papéis, e só três:

| Papel | Lê o dado | Cria e edita lançamento | Gerencia conta, categoria, meio | Convida e remove pessoas | Exclui o ambiente |
|---|:--:|:--:|:--:|:--:|:--:|
| **Dono** | sim | sim | sim | sim | sim |
| **Editor** | sim | sim | sim | não | não |
| **Leitor** | sim | não | não | não | não |

Por que três: o **editor** é o casal que divide as contas de casa; o **leitor** é quem
acompanha sem mexer (um contador, um filho, você olhando o ambiente de outra pessoa).
Sem o leitor, todo convite vira permissão de escrita — e aí compartilhar assusta.

Na tela isso aparece como duas opções, não três: **autorização completa** e **somente
leitura**. Autorização completa é o **editor** — faz tudo com o dinheiro e não convida
ninguém nem exclui o ambiente. Convidar e excluir ficam só com o dono, que é o que impede o
ambiente de ficar órfão.

**Invariante:** todo ambiente tem **exatamente um** dono, sempre. Não existe ambiente
sem dono nem com dois.

## Convite e saída

| Situação | Regra |
|---|---|
| Quem convida | Só o dono |
| Como | O dono digita o **e-mail** do convidado — que é o identificador de login — e o papel |
| Por onde chega | **Dentro do sistema**, na área de perfil da pessoa: aceitar ou recusar |
| O sistema manda e-mail? | **Não.** Não há serviço de e-mail no Pi, e custo externo zero é restrição. O e-mail identifica a pessoa; o convite vive no app |
| Convidado sem cadastro | O convite fica pendente; ao se cadastrar com aquele e-mail, ele aparece |
| Antes do aceite | O convidado **não vê nada** do ambiente. Convite pendente não é acesso |
| Trocar o papel de alguém | Só o dono, a qualquer momento, com efeito imediato |
| Editor ou leitor sai | Remove o acesso. O dado que ele criou **permanece** no ambiente |
| Dono quer sair | Tem que **transferir a propriedade** para outro membro antes. Não há saída que deixe o ambiente órfão |
| Dono exclui o ambiente | Só se for o único com acesso. Com outras pessoas dentro, primeiro remove ou transfere |

**Remover acesso nunca apaga lançamento.** O dado é do ambiente, não de quem digitou —
apagar o histórico de quem saiu quebraria o saldo de todo mundo que ficou.

## Isolamento

Esta é a regra de segurança do sistema inteiro:

1. Toda consulta a dado financeiro é filtrada por ambiente. **Sem exceção.**
2. O filtro não é responsabilidade de quem chama. Consulta sem ambiente não deve
   compilar, ou deve falhar — nunca retornar tudo.
3. O ambiente vem do **contexto da requisição autenticada**, jamais de parâmetro que o
   cliente escolhe sem checagem. Aceitar `ambienteId` do corpo sem validar o acesso é a
   falha clássica, e é falha crítica.
4. Vazamento entre ambientes é incidente de segurança, não bug de lógica.

Como o filtro é imposto — repositório-base e Row Level Security — está no `ADR-0002`. O que
o compartilhamento mudou nele está no `ADR-0004`: o critério deixou de ser
`ambiente_id = corrente` e passou a ser *"o ambiente corrente, ou um objeto compartilhado com
ele"*.

## Ciclo de vida

| Momento | O que acontece |
|---|---|
| Cadastro de usuário | Ganha um ambiente próprio já criado, do qual é dono |
| Criar mais ambientes | Livre. "Pessoal", "Casa", "Empresa" |
| Renomear | Dono e editor |
| Exclusão | Ver tabela de convite e saída. Apaga todo o dado do ambiente |

> ☐ **A definir:** exclusão é imediata ou tem janela de arrependimento? Amarrar com a
> política de backup em `docs/07-operacao/` na Fase 4.

## Invariantes

- Todo dado financeiro referencia exatamente um ambiente. Nunca zero, nunca dois.
- Todo ambiente tem exatamente um dono.
- Um usuário tem no máximo um acesso por ambiente (um papel, não vários).
- Um lançamento **não muda de ambiente**. Nem por edição, nem por correção — o certo é
  estornar em um e criar no outro, para o saldo dos dois continuar verdadeiro.
- A **categoria** de um lançamento é sempre do mesmo ambiente dele. Sem exceção.
- **Conta e meio** podem ser de outro ambiente, e só num caso: existe um vínculo de
  compartilhamento com o ambiente do lançamento (`ADR-0004`,
  `docs/02-dominio/compartilhamento.md`). Fora dele, nada de ambientes diferentes se
  referencia.

## O que este doc exige dos outros

- `docs/02-dominio/lancamento.md`: o lançamento guarda **quem** o criou. Em ambiente
  compartilhado, "quem lançou isso?" é a primeira pergunta que aparece — e o autor é
  informação de auditoria, não o dono do dado.
- `docs/03-dados/modelo-de-dados.md`: `ambiente_id` em toda tabela de dado financeiro,
  com índice, e a tabela de acesso (usuário, ambiente, papel) com unicidade no par.
- `docs/01-arquitetura/seguranca.md`: como o ambiente entra no contexto da requisição e
  onde o filtro é imposto.
