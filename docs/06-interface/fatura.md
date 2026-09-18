---
id: 06-interface/fatura
titulo: Fatura
dono: a tela do ciclo do cartao: os tres numeros de cada fatura, a janela que importa e as acoes de pagar, fechar e abrir
ler-junto: [02-dominio/fatura-cartao, 02-dominio/fatura-pagamento, 04-api/endpoints-faturas]
status: ativo
---

# Fatura

A tela do **ciclo do cartão**. O Extrato responde *"para onde o dinheiro foi"*; a Reserva,
*"quanto eu tenho"*; a Fatura responde a terceira pergunta que nenhuma das duas responde:
**"quanto eu devo, e quando"**.

Ela é a tela mais fácil de desenhar errado, porque o que o usuário quer ver não é o saldo do
cartão — é **o recorte que o banco vai cobrar**. Por isso o eixo dela é a fatura, e não a conta.

## O contrato no topo, as faturas embaixo

O seletor do cabeçalho escolhe o **contrato** — a conta `CARTAO`. Ele **some quando há um
cartão só**: seletor de uma opção é uma pergunta sem resposta alternativa.

Físico, virtual e adicional **não aparecem aqui**, e a ausência é a regra: limite, ciclo e
fatura são do contrato, nunca do cartão (`docs/02-dominio/meio-de-pagamento.md`). Quem quer ver
os cartões vai ao Cadastro, que é onde eles se criam e se inativam.

| Número do cabeçalho | O que é |
|---|---|
| **Dívida · agora** | O **saldo da conta `CARTAO`**, e o subtítulo diz isso em voz alta: *sem cálculo próprio*. É a frase do `ADR-0003` aparecendo como legenda, porque é ela que explica por que o número já inclui a parcela de daqui a oito meses |
| **Limite disponível** | `limite − dívida`. **Ausente quando não há limite informado** — um traço, e o subtítulo diz *sem limite informado*. Número que não descreve nada não é exibido |

**O aviso de limite envelhecido não é um prazo, é um fato:** quando a dívida passa do limite, o
disponível fica negativo e a tela diz que ele pode estar desatualizado — e diz junto que **o
limite nunca trava um lançamento**, porque é exatamente aí que o usuário pensa que o app deveria
ter recusado a compra.

**Informar o limite é ato do usuário, e a tela lembra disso** (`docs/02-dominio/conta.md`): o
sistema nunca o corrige sozinho, e o número passa a carregar a data de hoje.

## Cada fatura mostra os três números, e só eles

`Total`, `Pago` e `A pagar` sempre; `Rolado` e `Agendado` **só quando existem**. Campo que não
se aplica não ocupa linha.

O **agendado** é o que mais precisa da legenda, e ela está ao lado dele: *ainda não saiu, e por
isso não entra no pago*. Sem isso o usuário lê "R$ 0,00 pago" numa fatura que ele já resolveu e
paga duas vezes (`docs/02-dominio/fatura-pagamento.md`).

As marcas de leitura — `ROLADA`, `PARCIAL`, `QUITADA`, `CRÉDITO NO CARTÃO` — são **derivadas**,
não estado salvo, e é por isso que **duas podem aparecer juntas**: *parcial · rolada* é
exatamente o que acontece com a fatura que recebeu R$ 800 de R$ 1.610,60 e venceu.

## Botão que só existe quando funciona

A regra do `docs/06-interface/navegacao.md` governa esta tela mais do que qualquer outra, porque
aqui as ações **mudam com o estado**:

| Estado | O que a tela oferece |
|---|---|
| `FUTURA` | Nada. E a linha diz por quê: *existe só para segurar parcela de mês que ainda não chegou; o emissor nem a emitiu* |
| `ABERTA` | **Fechar à mão**, e a linha nomeia que isso é **contingência** — e que pagar antes do fechamento é *antecipar*, outra mecânica |
| `FECHADA` **e ainda devendo** | **Pagar** e **Abrir** — e a linha diz que é **um número, duas operações**. Abrir só na **última** fechada |
| Encerrada | Nada, e a linha explica o que aconteceu: rolou para a seguinte, ou o pagamento cobriu o total. E aponta para o Extrato, porque **fatura fechada não congela lançamento nenhum** |

**A explicação não é um tooltip: é a linha embaixo dos botões**, sempre visível. As regras da
fatura são invisíveis num formulário comum, e o usuário só descobre que entendeu errado quando
o número não bate.

## O formulário de pagamento explica antes de aplicar

Três campos — **pagar com**, **valor** e **dia** —, e a linha embaixo muda a cada tecla:

- **de onde e para onde**: dois lançamentos, categoria de sistema, **fora do relatório de
  gasto** — *o gasto foi contado uma vez, na compra*;
- **o dia**: à frente nasce `PREVISTO` e realiza pela data; hoje nasce `REALIZADO`;
- **o resto**: sobrando, a fatura fica **parcial** e *nada é liquidado*; faltando, vira
  **crédito** na conta do cartão; batendo, **quita** — e é a quitação que liquida.

O valor vem **pré-preenchido com o `a pagar`** e a conta vem com a `contaPagadoraPadrao` do
cartão. As duas são conveniência, não decisão: a conta pagadora **só preenche o formulário**, e
nada nasce dela sozinho (`docs/02-dominio/fatura-cartao.md`).

**Sem conta de caixa ativa o formulário não aparece**, e a linha diz por quê: pagar é uma
transferência, e ela precisa de uma origem. É o mesmo tratamento da Reserva.

## O bloco da Home

A Home mostra **a janela que importa** de cada cartão: a `FECHADA` que ainda deve, ou, na falta
dela, a `ABERTA`. Um cartão, uma linha, com o **a pagar** como número grande — e a linha leva à
tela da Fatura daquele contrato.

É o mesmo recorte que a tela usa para decidir onde ficam os botões, e de propósito: **dois
lugares mostrando o mesmo fato não podem calculá-lo duas vezes**
(`docs/06-interface/dashboard.md`).

## O que ainda não existe

- **Os lançamentos da fatura, listados dentro dela.** Hoje o caminho é o Extrato, filtrando pela
  conta do cartão. Entra quando o Extrato souber filtrar por fatura.
- **Parcelamento**: a fatura ainda não distingue *parcela 3 de 10* de uma compra à vista
  (`docs/02-dominio/recorrencia.md`).
- **A parte de cada um** num cartão compartilhado (`docs/02-dominio/compartilhamento.md`).
