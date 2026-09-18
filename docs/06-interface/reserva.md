---
id: 06-interface/reserva
titulo: Reserva
dono: a tela do patrimonio: as aplicacoes, a idade do valor informado e a distincao entre fluxo de caixa e patrimonio
ler-junto: [02-dominio/aplicacao-patrimonio, 02-dominio/conta, 04-api/endpoints-contas]
status: ativo
---

# Reserva

A Reserva responde **"quanto eu tenho"** — a outra metade da aposta do produto, ao lado do
*"para onde o dinheiro foi"* que o Extrato responde. É a tela do que está guardado, e a única
em que o usuário **informa** um número em vez de registrar um fato.

## Três números, e eles não se somam

| Número | Cor | O que é |
|---|---|---|
| **Guardado** | lima | Saldo das contas fora do fluxo de caixa |
| **Em caixa** | ciano | Saldo das contas que pagam qualquer coisa |
| **Patrimônio** | tinta | Todas as contas, sem exceção |

São os mesmos três da Home, e é de propósito: **não há uma quarta leitura de patrimônio**.
Aqui eles voltam porque esta é a tela em que se mexe no que os produz, e conferir o efeito
numa tela e o número em outra é como se perde a confiança no número.

O **Patrimônio** troca o próprio subtítulo quando alguma aplicação está desatualizada: em vez
de *"todas as contas"*, ele diz **"contém valor desatualizado"**. Um total que soma um número
velho sem avisar é o total em que se confia demais.

## A lista de aplicações

Cada linha traz o nome, o saldo, **quando o valor foi informado** e **há quantos dias**. Passados
30 dias, a linha ganha a marca `DESATUALIZADA` (`docs/02-dominio/aplicacao-patrimonio.md`).

**A idade é o que torna o número velho utilizável.** Sem ela a tela afirmaria que a poupança
vale hoje o que valia em julho; com ela, a mesma tela diz a verdade — *valia isso, há 59 dias* —
e continua servindo para decidir.

Aplicação que nunca teve valor informado diz **`SEM VALOR INFORMADO`** e **não** é marcada de
desatualizada: chamar de velho o que nunca existiu é ruído, não aviso.

## Informar o valor é criar um lançamento, e a tela diz isso

O formulário pede **quanto vale hoje**, já preenchido com o valor atual — porque o gesto mais
comum é ajustar, não digitar do zero. Abaixo do campo, a tela nomeia o que vai acontecer: *a
diferença vira um lançamento de rendimento nesta conta, com a data de hoje; nada é sobrescrito,
e ele aparece no Extrato.*

Isso não é detalhe de implementação exposto por preguiça. É o que explica por que o histórico
existe, por que o número tem data e por que o sistema não pode "corrigir" a aplicação sozinho:
**não há campo de valor para ele escrever** (`docs/02-dominio/aplicacao-patrimonio.md`).

Informar o valor que a aplicação **já vale** não grava nada e não é erro — não houve fato.

## Aportar e resgatar na própria linha

As duas operações são **transferências**, e sempre foram (`docs/02-dominio/lancamento.md`) — o
atalho não cria capacidade, encurta caminho. `APORTAR` e `RESGATAR` abrem na linha um formulário
de três campos: a **conta de caixa** da outra ponta, quanto, e a descrição — já sugerida como
*Aporte em `<nome>`* ou *Resgate de `<nome>`*, que é o que se lê nos **dois** lados no Extrato.

**A outra ponta é sempre uma conta de caixa**, e quem decide isso é o servidor
(`docs/04-api/endpoints-contas.md`): a aplicação não financia outra aplicação, e o vale-benefício
não entra porque o saldo dele não é fungível (`docs/02-dominio/conta.md`). Sem nenhuma conta de
caixa, os dois botões ficam desabilitados e a linha **diz por quê** em vez de só apagar.

**O formulário afirma a consequência antes do clique:** *o patrimônio não muda — o dinheiro só
troca de bolso*. É a invariante do `aplicacao-patrimonio.md` escrita onde ela é contrariada pela
intuição: guardar dinheiro **parece** que deveria mudar quanto se tem, e não muda.

A data é **hoje**, e não há campo para ela. Aporte com outra data é lançamento comum, e o
caminho é a aba `TRANSFERÊNCIA` do formulário completo — dois campos a menos aqui valem mais que
uma data que quase ninguém troca.

## O rodapé que a tela precisa ter

Duas regras ficam escritas na própria tela, porque as duas contrariam a intuição:

- **O sistema nunca extrapola.** Não há rendimento estimado nem curva projetada. Ele fica
  parado no último número informado, e mostra a idade dele.
- **Fluxo de caixa não é patrimônio.** Um aporte muda o mês e **não** muda o patrimônio: o
  dinheiro trocou de bolso. É o que explica a linha *guardado* separada do gasto na Home — e
  por que o mês deixa de fechar na soma simples.

## O que a Reserva ainda não faz

**Aportar com outra data**, e **desfazer** um aporte por aqui: o par se exclui pelo Extrato,
abrindo qualquer um dos dois lados (`docs/06-interface/extrato.md`).

**Rentabilidade, cotação e meta.** Estão fora da Fase 1 por decisão de produto, não por falta de
tempo (`docs/02-dominio/aplicacao-patrimonio.md`).
