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

## Uma fatura por vez, e o contrato em chips

**A tela mostra uma fatura**, não a pilha delas: com os lançamentos dentro, empilhar dois anos
de cartão daria 24 painéis, e a pergunta nunca é *"como foram todas"*.

Ela **abre na que importa** — a `FECHADA` que ainda deve, ou, na falta dela, a `ABERTA`. É a
mesma escolha que o bloco da Home faz, e de propósito: **dois lugares mostrando o mesmo fato não
podem calculá-lo duas vezes**. Para as outras, a navegação `‹ competência ›` reusa o desenho do
seletor de dia do Diário, e as setas **desabilitam nas pontas** — o cartão tem um número fechado
de faturas, e seta que não leva a lugar nenhum explica sete vezes.

O contrato — a conta `CARTAO` — é escolhido em **chips**, e não num `<select>`: o dropdown
nativo é pintado pelo sistema operacional e não aceita tema
(`docs/06-interface/direcao-visual.md`). Eles **somem quando há um contrato só**: seletor de uma
opção é uma pergunta sem resposta alternativa.

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

## O `a pagar` é o maior número da tela

É a primeira regra da direção visual aplicada aqui: **o número que importa é o maior elemento, e
a telemetria é moldura.** `A pagar` domina; `Total` e `Pago` ficam ao lado, menores, porque são
a conta que o produz. `Rolado` toma o lugar de `Agendado` quando existe — a fatura que rolou não
tem mais o que agendar.

Acima deles, o **contrato**: a dívida e o limite disponível, que são do cartão inteiro e não
desta fatura. A separação é o que impede a pergunta *"devo R$ 1.610 ou R$ 5.000?"* — a primeira
é desta fatura, a segunda é do contrato, e as duas são verdade.

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

## Os lançamentos, e é aqui que a tela deixa de ser informativa

**Um contrato tem vários cartões e a fatura é uma só** — e o que o usuário precisa ver é
**quanto cada cartão gastou**. Físico, virtual e adicional dividem o limite e o ciclo
(`docs/02-dominio/meio-de-pagamento.md`), mas não dividem a pergunta: *"o que foi meu e o que
foi do adicional?"* só tem resposta separando.

Então o painel é **um bloco por cartão, com subtotal no cabeçalho** — e os subtotais somam o
total da fatura. **Com um cartão só o cabeçalho não aparece:** não há o que distinguir, e é a
mesma razão que tirou o nome dos outros meios.

A ordem vem da **fatura de papel**, não da tela:

| Onde | O quê | Por quê |
|---|---|---|
| **Topo, fora dos grupos** | O **saldo da fatura anterior** | É a primeira linha da fatura de papel (`ADR-0005`), e não é de cartão nenhum: é dívida que **mudou de período**, não dívida nova |
| **Meio** | Um bloco por cartão | O pedido: uma fatura, um extrato por cartão |
| **Fim, como carimbo** | O **rolado para a seguinte** | Não é gasto desta fatura — é a saída dela —, e por isso já está fora do total |

**A parcela diz qual de quantas na própria linha** (`PARCELA 1/3`), como o emissor faz. Sem
isso, três linhas de R$ 1.666,68 em três faturas não se reconhecem como uma compra só.

**Cada linha abre o lançamento** (`#/extrato/{id}`), e é lá que se corrige — inclusive para
**mover de fatura**, que é o conserto de quando a `dataFechamento` do app erra o dia do emissor.
Nenhum estado de fatura trava a edição.

**Fatura vazia é resposta:** *nenhum lançamento nesta fatura*, e a lembrança de que **fatura
vazia fecha do mesmo jeito**, com total zero.

## O bloco da Home

A Home mostra **a janela que importa** de cada cartão: a `FECHADA` que ainda deve, ou, na falta
dela, a `ABERTA`. Um cartão, uma linha, com o **a pagar** como número grande — e a linha leva à
tela da Fatura daquele contrato.

É o mesmo recorte que a tela usa para decidir onde ficam os botões, e de propósito: **dois
lugares mostrando o mesmo fato não podem calculá-lo duas vezes**
(`docs/06-interface/dashboard.md`).

## O que ainda não existe

- **A parte de cada um** num cartão compartilhado (`docs/02-dominio/compartilhamento.md`). Os
  grupos de hoje separam por **cartão do contrato**, que é outra pergunta: adicional é um cartão
  deste contrato; compartilhado é o mesmo cartão em dois ambientes.
- **Filtrar o Extrato por fatura.** A lista daqui é a da fatura em foco; quem quer cruzar
  períodos ainda vai ao Extrato e filtra pela conta.
- **Trocar o ciclo** de um contrato depois de criado — a regra existe, o `PATCH` não expõe.
