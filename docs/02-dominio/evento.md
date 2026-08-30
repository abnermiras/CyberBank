---
id: 02-dominio/evento
titulo: Evento
dono: o registro do que aconteceu num dia — o que o sistema fez sozinho e o que a pessoa fez
ler-junto: [02-dominio/lancamento, 02-dominio/fatura-cartao, 02-dominio/ambiente-financeiro]
status: rascunho
---

# Evento

> **Quando.** **Gravar é Fase 1. A tela do Diário é Fase 2**
> (`docs/00-produto/roadmap.md`). Evento não se reconstitui depois: o dia em que a fatura
> fechou sozinha ou passou, ou não foi registrado. Por isso a gravação não pode esperar a
> tela.

Um **evento** é uma linha que diz *o que aconteceu, quando, e por quem* dentro de um
ambiente financeiro. O conjunto dos eventos de um dia é o **Diário** daquele dia.

## Por que isto existe

O Cyberbank mexe no dinheiro do usuário **sozinho** em quatro lugares:

| O que o sistema faz sem ser mandado | Onde está escrito |
|---|---|
| Previsto vira realizado quando a `dataEfeito` chega | `docs/02-dominio/lancamento.md` |
| A fatura fecha, abre a seguinte e cria o pagamento previsto | `docs/02-dominio/fatura-cartao.md` |
| A fatura vencida e não quitada **rola** para a seguinte | `docs/01-arquitetura/decisoes/ADR-0005-rolagem-entre-faturas.md` |
| A recorrência ganha a ocorrência do ciclo | `docs/02-dominio/recorrencia.md` |

O primeiro deles vem com um preço que o `meio-de-pagamento.md` **nomeia e aceita**: o boleto
que vence é realizado pela data, *"o único ponto do modelo em que o sistema afirma um fato que
não observou"*. O evento é a contrapartida desse preço. Sem ele, o usuário abre o app, vê um número diferente do de ontem e **não tem como
perguntar por quê** — e um app de dinheiro que muda de valor sem explicar é um app em que
ninguém confia.

A pergunta que o Diário responde é literalmente essa: **por que meu saldo mudou?**

## Por que é entidade, e não consulta

Este projeto tem um viés forte e certo: pendência é consulta, saldo é derivado, dívida é o
saldo da conta. O critério é *tipo novo só existe se alguma regra do sistema mudar por causa
dele* — e o evento parece violar isso, porque não muda regra nenhuma.

Não viola, por duas razões.

**Primeira: o critério é sobre tipo, e isto não é um tipo.** Ele não classifica nada nem cria
comportamento condicional. É o registro de um fato.

**Segunda, e decisiva: metade do que o Diário precisa mostrar não é derivável.** Saldo é
derivável porque os lançamentos que o compõem estão todos guardados. Já:

- **quando** um `PREVISTO` virou `REALIZADO` — nada guarda. `situacao` é o estado atual;
- **quando** uma fatura fechou, abriu ou encerrou — `status` é o estado atual, sem data;
- **que** um lançamento foi corrigido, de quanto para quanto — o `lancamento.md` já promete
  *"sempre com histórico"*, e esse histórico não existe em lugar nenhum.

Derivar exigiria espalhar um campo de data por cada transição de cada entidade, e um ramo
novo na consulta a cada evento novo. **Fato que o sistema produz, o sistema registra na hora.**
Depois é tarde: dia que passou sem registro é dia perdido para sempre.

De quebra, esta entidade **entrega o "histórico de alteração"** que o `lancamento.md` promete
e nenhum doc escreve — não é uma segunda estrutura, é a mesma.

## Campos

| Campo | Obrigatório | Regra |
|---|---|---|
| `id` | sim | — |
| `ambiente` | sim | O ambiente em que aconteceu. **Todo evento pertence a um ambiente**, e o isolamento vale igual ao do lançamento (`ADR-0002`) |
| `dia` | sim | **Data de domínio**: dia local, sem hora nem fuso. É por ele que o Diário agrupa e o seletor de dia navega |
| `instante` | sim | **UTC.** Carimbo de auditoria, e o critério de ordem dentro do dia. É a distinção da regra 5 do `CLAUDE.md`: `dia` é domínio, `instante` é instante |
| `origem` | sim | `SISTEMA` ou `USUARIO`. Ver *As duas origens* |
| `autor` | sim | Quem fez. No evento de origem `SISTEMA`, é o **dono do ambiente** — a mesma regra do lançamento que o ciclo cria (`docs/02-dominio/lancamento.md`) |
| `tipo` | sim | O que aconteceu. Lista fechada, ver *Os tipos* |
| `alvo` | não | A entidade a que o evento se refere: lançamento, fatura, conta, categoria. É o que faz a linha do Diário virar link |
| `dados` | não | O punhado de valores que a frase precisa (o valor rolado, o de/para de uma correção). **Não é um espelho do objeto** |

**O evento não guarda a frase pronta.** `tipo` + `dados` + `alvo` bastam, e a tela monta o
texto — senão a frase congela na redação do dia em que foi escrita e nunca mais melhora, e
traduzir o app vira reescrever o passado.

## As duas origens

O Diário mostra **as duas**, marcadas e separadas.

`SISTEMA` é a prestação de contas da automação — a razão de o evento existir. `USUARIO` é o
que a pessoa fez: lançou, corrigiu, estornou, pagou a fatura, inativou uma categoria.

Mostrar só o `SISTEMA` foi considerado e descartado. Num ambiente com mais de uma pessoa, o
saldo muda porque **alguém** mexeu, e um Diário que só fala da automação deixa exatamente a
pergunta mais frequente sem resposta: *"quem lançou isso?"*. O `compartilhamento.md` já
garante que o dado é de quem lançou; o evento é onde isso aparece.

A separação é de leitura, não de estrutura: é o mesmo campo `origem`, e a tela agrupa.

## Os tipos

Lista fechada. Tipo novo só entra quando aparece um fato novo que o sistema produz — o mesmo
critério do resto do projeto.

**Origem `SISTEMA`:**

| Tipo | Quando |
|---|---|
| `LANCAMENTO_REALIZADO` | A `dataEfeito` chegou e o previsto virou realizado |
| `FATURA_FECHADA` | O ciclo fechou a fatura no dia do fechamento |
| `FATURA_ABERTA_PELO_CICLO` | A seguinte foi aberta no mesmo passo |
| `PAGAMENTO_PREVISTO_CRIADO` | O fechamento criou o previsto de pagamento |
| `FATURA_ROLADA` | Venceu sem quitar e o que faltou rolou (`ADR-0005`) |
| `FATURA_ENCERRADA` | Quitada, ou vencida e rolada — e só aqui os lançamentos dela saem de `PROVISIONADO` |
| `OCORRENCIA_DE_RECORRENCIA` | O ciclo lançou a ocorrência (entra com a recorrência) |

**Origem `USUARIO`:**

| Tipo | Quando |
|---|---|
| `LANCAMENTO_CRIADO` · `LANCAMENTO_EDITADO` · `LANCAMENTO_ESTORNADO` | O `EDITADO` carrega o de/para em `dados`: é ele que cumpre o *"sempre com histórico"* |
| `FATURA_PAGA` | Pagamento de fatura, parcial ou total |
| `FATURA_ABERTA_PELO_USUARIO` | A última fechada foi reaberta à mão |
| `VALOR_DE_APLICACAO_INFORMADO` | O usuário atualizou o valor atual — e a **diferença lançada** vem em `dados` |
| `LIMITE_INFORMADO` | O usuário informou o limite do cartão |
| `CATEGORIA_CRIADA` · `CATEGORIA_RENOMEADA` · `CATEGORIA_INATIVADA` · `CATEGORIA_REATIVADA` · `CATEGORIA_EXCLUIDA` | Ciclo de vida da categoria (`docs/02-dominio/categoria.md`) |
| `CONTA_CRIADA` · `CONTA_INATIVADA` · `MEIO_CRIADO` · `MEIO_INATIVADO` | Idem para conta e meio |
| `SERIE_ALTERADA` · `SERIE_CANCELADA` | Parcelamento ou recorrência |
| `ACESSO_CONCEDIDO` · `ACESSO_REVOGADO` · `VINCULO_CRIADO` · `VINCULO_REVOGADO` | Quem entrou e quem saiu do ambiente, e o compartilhamento (`ADR-0004`) |

## O que NÃO é evento

Esta seção existe para o Diário não virar um log de aplicação.

- **Leitura não é evento.** Abrir uma tela, filtrar, exportar. O Diário responde *"o que
  mudou"*, e olhar não muda nada.
- **Login e sessão não são evento de domínio.** São segurança, e o dono é
  `docs/01-arquitetura/seguranca.md`.
- **Erro e exceção não são evento.** Isso é log de operação (`docs/07-operacao/`). Evento é o
  que **aconteceu**, não o que falhou ao tentar acontecer.
- **Passo de ciclo que não fez nada não é evento.** O fechamento é idempotente e roda todo
  dia; ele só grava quando de fato fecha alguma coisa. Diário cheio de "nada aconteceu" é
  Diário que ninguém lê.
- **Rendimento de aplicação com diferença zero não é evento**, pela mesma razão.

## O Diário

A tela é Fase 2 e o dono dela é `docs/06-interface/navegacao.md`. Do lado do domínio, só o
seguinte é regra:

- O Diário de um dia são os eventos daquele `dia`, **do ambiente ativo**, mais novos primeiro
  pelo `instante`.
- **Dia sem evento é uma resposta válida**, e a tela diz isso em vez de parecer quebrada.
- O seletor de dia navega livremente para trás. **Para frente do dia corrente não há Diário** —
  o que ainda não aconteceu está no previsto, não aqui, e essas são duas perguntas diferentes.

## Retenção

**Evento não expira e não é apagado.** Não há recolhimento por prazo — a mesma regra que vale
para categoria inativa (`docs/02-dominio/categoria.md`) e pelo mesmo motivo: o sistema não
apaga o que o usuário não mandou apagar.

Excluir o ambiente apaga os eventos dele, junto com todo o resto
(`docs/02-dominio/ambiente-financeiro.md`). É o único caminho.

> ☐ **Revisitar se o volume incomodar** no Pi. A conta a fazer antes de inventar arquivamento:
> um ambiente ativo produz da ordem de algumas dezenas de eventos por mês.

## Invariantes

- Todo evento pertence a exatamente um ambiente e nunca muda de ambiente.
- `dia` é data pura; `instante` é UTC. Nunca se deriva um do outro para exibir.
- Todo evento tem `autor`, inclusive o de origem `SISTEMA` — nele o autor é o **dono do
  ambiente**, pela mesma regra do lançamento que o ciclo cria.
- Evento é **imutável**: não se edita e não se exclui. Registro errado se corrige com um
  evento novo, nunca reescrevendo o antigo.
- Passo de ciclo que não mudou nada não grava evento.
- Evento não guarda texto pronto para exibição.
- Todo lançamento que o ciclo cria tem um evento correspondente no mesmo `dia`.
- Nenhum evento tem `dia` maior que o dia corrente.
