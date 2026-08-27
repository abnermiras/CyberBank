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

**Invariante:** todo ambiente tem **exatamente um** dono, sempre. Não existe ambiente
sem dono nem com dois.

## Convite e saída

| Situação | Regra |
|---|---|
| Quem convida | Só o dono |
| Como | Por e-mail do convidado, com o papel já definido no convite |
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

> ☐ **A definir com a arquitetura:** onde o filtro é aplicado de forma que não dê para
> esquecer — filtro global no repositório, `Row Level Security` no Postgres, ou os dois.
> Decidir em `docs/01-arquitetura/seguranca.md` antes do primeiro repositório existir.

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
- Conta, categoria e meio de pagamento de ambientes diferentes nunca se referenciam.

## O que este doc exige dos outros

- `docs/02-dominio/lancamento.md`: o lançamento guarda **quem** o criou. Em ambiente
  compartilhado, "quem lançou isso?" é a primeira pergunta que aparece — e o autor é
  informação de auditoria, não o dono do dado.
- `docs/03-dados/modelo-de-dados.md`: `ambiente_id` em toda tabela de dado financeiro,
  com índice, e a tabela de acesso (usuário, ambiente, papel) com unicidade no par.
- `docs/01-arquitetura/seguranca.md`: como o ambiente entra no contexto da requisição e
  onde o filtro é imposto.
