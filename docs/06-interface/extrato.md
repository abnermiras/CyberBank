---
id: 06-interface/extrato
titulo: Extrato e o detalhe do lancamento
dono: a lista de movimento, a linha resumida e o modal que abre um lancamento inteiro
ler-junto: [04-api/endpoints-lancamentos, 04-api/endpoints-eventos, 02-dominio/lancamento]
status: ativo
---

# Extrato e o detalhe do lançamento

O Extrato responde **"o que aconteceu com o meu dinheiro"** — todo movimento do ambiente, do
mais novo para o mais antigo. A Home diz como as coisas estão; o Diário diz o que aconteceu
num dia; **aqui é a linha do tempo do dinheiro**, e é a tela em que se procura uma coisa
específica.

## A linha é curta de propósito

`16/09 · Gasolina · Nubank · REALIZADO · Transporte › Gasolina · −R$ 154,50`

A lista existe para **varrer**, não para ler: cinquenta linhas na tela, e o olho procurando
uma. Tudo que não ajuda a reconhecer a linha fica fora dela — e é por isso que o autor, a hora
do cadastro, o estabelecimento e o histórico **não estão** ali, mesmo já existindo no servidor.

A linha mostra: as datas (e a de efeito **só quando difere** da de evento), a descrição, a
conta, a situação, a categoria — ou a marca de pendência —, e as marcas de transferência,
estorno e do sistema.

**A linha inteira é clicável**, e abrir é a única coisa que ela faz.

## O detalhe abre tudo

Um modal, e nele o lançamento inteiro. A ordem é a de quem chegou perguntando *"o que é
isso?"*: primeiro o que se reconhece, por último o que é raro.

| # | Bloco | O que responde |
|---|---|---|
| 0 | Cabeçalho | Descrição, valor, `#id` e as marcas de situação e natureza |
| 1 | **O dinheiro** | De qual conta saiu, por qual meio, em que sentido |
| 2 | **As duas datas** | Quando **aconteceu** e quando **mexe no saldo** |
| 3 | **Classificação** | `Raiz › Subcategoria`, na cor da raiz — ou o seletor, se está pendente |
| 4 | **Quem e quando** | Quem lançou, a que horas foi cadastrado, e o texto bruto do estabelecimento |
| 5 | **Ligações** | O outro lado da transferência, o estorno, o original |
| 6 | **Série e fatura** | *Reservado* |
| 7 | **O que já aconteceu com ele** | O histórico |
| 8 | **Ações** | Estornar e excluir |

### As duas datas só se explicam quando divergem

Quando `dataEvento` e `dataEfeito` são iguais — que é quase sempre —, o bloco mostra as duas e
cala. **Quando diferem, ele diz por quê**: o boleto foi lançado num dia e vence noutro, e é
essa divergência que faz os dois campos existirem (`docs/02-dominio/lancamento.md`). Explicar
sempre seria ruído; explicar só quando a coisa acontece é onde a regra vira aprendizado.

### O estabelecimento é rotulado como texto bruto

Ele vem da captura, antes de qualquer normalização, e por isso aparece marcado como tal — e
não como se fosse um nome que o sistema escolheu. `MEDTECH 24H` é o que a máquina do cartão
mandou, e a tela não finge o contrário.

### Série e fatura — bloco reservado

Parcelamento, recorrência e fatura ainda não existem no modelo
(`docs/02-dominio/recorrencia.md`, `docs/02-dominio/fatura-cartao.md`). O bloco fica na tela
**dizendo o que vai dizer** — *"parcela 3 de 10, da compra de R$ 5.000"*, a fatura e o mês de
competência. É o mesmo tratamento do bloco de fatura na Home e dos convites no Perfil: espaço
reservado diz que o lugar é aqui; espaço ausente ensina que o app não faz.

### O histórico é a promessa sendo cumprida

*"Toda alteração fica registrada: quem, quando, o campo, e o valor antes e depois"* está no
`docs/02-dominio/lancamento.md` desde o começo, e o `docs/02-dominio/evento.md` diz que é o
evento quem entrega. **Este bloco é o primeiro lugar do app onde isso aparece.**

- Ordem **cronológica**, do nascimento para cá — ao contrário do Diário.
- Cada linha diz **quem** e **a que horas**, e o evento do sistema vem marcado como tal: foi a
  rotina que virou o previsto em realizado, não uma pessoa.
- A correção mostra o **de → para**, com o nome do campo em português.
- As frases são as mesmas do Diário, montadas na tela — nunca guardadas
  (`docs/04-api/endpoints-eventos.md`).

## As ações moram no detalhe

`ESTORNAR` e `EXCLUIR` **saíram da linha**. A linha ficou com uma função só — abrir —, e as
ações passaram para onde a pessoa já está olhando o que vai mexer.

O impacto continua sendo mostrado **antes** de confirmar: a exclusão diz em quanto o saldo da
conta fica, e nomeia o outro lado quando é transferência. É a regra de `navegacao.md` — *ação
destrutiva mostra o impacto numérico antes de confirmar* — no lugar em que ela finalmente tem
espaço para caber.

**Lançamento do ciclo não tem ação**, e o detalhe diz por quê em vez de só esconder o botão.

## O endereço abre o lançamento

`#/extrato/88` abre o Extrato **com o detalhe daquele lançamento aberto**, mesmo que ele não
esteja na primeira página e mesmo com a página recarregada do zero — é para isso que o detalhe
tem endpoint próprio (`docs/04-api/endpoints-lancamentos.md`).

Foi isso que fechou o buraco que o `docs/06-interface/navegacao.md` registrava: **o link do
Diário agora leva ao objeto, não à tela do objeto.** Fechar o modal volta para `#/extrato`, e o
botão "voltar" do navegador funciona como a pessoa espera.

## O que o detalhe ainda não faz

**Editar os campos.** O contrato existe (`PATCH`), e a única edição que a tela faz hoje é
resolver a pendência escolhendo a categoria. Corrigir valor, data ou conta entra aqui — e entra
junto com o aviso que o `lancamento.md` exige: **ação retroativa nomeia o impacto antes de
aplicar**, e no lançamento de fatura já paga isso significa nomear o pagamento e a diferença.
Meia edição, sem o aviso, é pior que nenhuma.
