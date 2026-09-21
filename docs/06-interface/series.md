---
id: 06-interface/series
titulo: Séries
dono: a tela das duas series e o lugar onde se diz, ao lancar no credito, que a cobranca e recorrente e nao parcelada
ler-junto: [02-dominio/recorrencia, 04-api/endpoints-series, 06-interface/navegacao]
status: ativo
---

# Séries

A tela do **que se repete**. O Extrato mostra as linhas uma a uma e a Fatura mostra o recorte
do mês; nenhuma das duas responde *"o que eu assinei, e o que ainda estou pagando"* — porque
as duas mostram **ocorrências**, e a série é a **regra** que as produz.

A regra inteira é de `docs/02-dominio/recorrencia.md`. Aqui está só o que é decisão de tela.

## Dois grupos, e nunca uma lista só

As séries aparecem em **duas grades de cartões**, uma por família: recorrências em cima,
parcelamentos embaixo, cada grupo com o seu cabeçalho. Não é arrumação — é a mesma decisão que
fez delas duas tabelas: não há campo que signifique a mesma coisa nos dois lados. *"R$ 5.000 em
10x"* é um total que existe e é fixo; *"R$ 39,90 por mês"* é o valor de uma ocorrência e não
soma com nada. Uma lista única obrigaria a coluna **valor** a significar duas coisas conforme a
linha, e é exatamente aí que o usuário lê errado.

**Cartão, e não linha de tabela.** Uma linha só cabe quando as colunas significam o mesmo em
todas elas; aqui cada família tem números próprios — a recorrência mostra *desde quando* e
*quanto já passou*, o parcelamento mostra *quanto falta* e *quantas pagas*. Cartão também é o
que dá à tela a leitura de painel: o olho varre blocos, não linhas.

**A cor separa as duas antes de qualquer texto.** Recorrência é **ácido** (`--acid`);
parcelamento é **ciano** (`--cyan`) — na borda esquerda do cartão, no valor grande e no
cabeçalho do grupo. É o mesmo par que o resto do app já usa para *o que se repete* e *o que o
cartão vai cobrar*, e é por isso que a etiqueta do canto (`PERGUNTA AO EDITAR` ·
`ALTERA TODAS`) confirma o que a cor já disse.

Os números do topo seguem a mesma separação:

| Número | O que é |
|---|---|
| **Assinaturas por mês** | Soma do valor das recorrências **ativas**. É compromisso mensal, não dívida — e o rótulo diz `NÃO ACABA` |
| **Parcelas a vencer** | Soma do que falta das compras parceladas. É dívida, e o rótulo diz `ACABA` |
| **Comprometido no mês** | Os dois juntos na única leitura em que somar faz sentido: o que cai **neste mês**, assinaturas mais a parcela de cada compra em aberto |

**Somar os dois num total de dívida seria o erro que esta tela existe para impedir** — um
acaba e o outro não. Somá-los *por mês* é outra pergunta, e essa é legítima.

## Cada cartão diz o que a série é, não só quanto custa

O número grande do cartão é o que **define** aquela família, e são números diferentes:

| | Recorrência | Parcelamento |
|---|---|---|
| Número grande | **Valor da ocorrência** | **Valor total da compra** |
| Abaixo dele | `TODO DIA N · SEM DATA DE FIM` | `Nx DE R$ …` |
| O corpo conta | Desde quando, quantas cobranças, **já cobrado** | **k de N pagas**, quantas faltam e quanto |
| Destaque final | `PRÓXIMA COBRANÇA …` e **ainda não cobrada** | `PRÓXIMA PARCELA …` |

**Só o parcelamento tem barra de progresso**, e a ausência dela na recorrência é a regra
aparecendo no desenho: barra precisa de um fim para medir contra, e assinatura não tem. Marcar
1/12 numa Netflix inventaria um contrato de um ano — exatamente o que o domínio recusa ao não
gerar horizonte.

O `já cobrado` da recorrência é **histórico**, o que passou, e nunca aparece como se fosse um
total da assinatura. *Paga*, no parcelamento, é a parcela `REALIZADO` — quem realiza é o
encerramento da fatura (`docs/02-dominio/fatura-pagamento.md`); pagar a fatura sozinho não
liquida nada.

Série encerrada — recorrência cancelada, compra quitada — continua na grade, **esmaecida e com
a borda apagada**, e a etiqueta troca para `CANCELADA` ou `QUITADA`. Ela é verdade histórica, e
sumir da tela é a forma mais rápida de o usuário achar que o sistema perdeu o dado.

## Onde se diz que é recorrente, e não parcelado

**No formulário de lançar, aba `CRÉDITO`, num segmento de três estados** — `À VISTA`,
`PARCELADO`, `RECORRENTE` —, e não numa caixa de marcar ao lado do campo de parcelas.

O segmento troca o campo que aparece embaixo: `PARCELADO` mostra **em quantas vezes**,
`RECORRENTE` mostra **repete todo dia**, `À VISTA` não mostra nenhum dos dois. Eles **nunca
aparecem juntos**, e isso não é economia de espaço: a invariante do domínio diz que um
lançamento tem parcelamento **ou** recorrência, nunca os dois, e uma tela que oferece os dois
campos ao mesmo tempo convida a pedir o que o sistema recusa.

A explicação embaixo do formulário muda junto, porque as duas escolhas prometem coisas
diferentes: o parcelado diz que as N parcelas **nascem todas juntas** e comem o limite agora;
o recorrente diz que nasce **só a ocorrência deste ciclo**, `PREVISTO`, e que a do mês seguinte
só existe quando esta fatura fechar.

**A assinatura se cadastra lançando, não nesta tela.** Um botão *"nova série"* aqui seria um
segundo formulário de lançamento, com os mesmos campos e outro caminho de erro — e o lugar
onde a pessoa pensa em dinheiro saindo é o `+`.

## O que ainda não existe

| O que falta | Por quê |
|---|---|
| **Alterar e cancelar série** | A tela diz, no rodapé, que as duas não se comportam igual — parcelamento não pergunta, recorrência pergunta o escopo. Enquanto o `PATCH` não existe (`docs/04-api/endpoints-series.md`), prometer o botão seria pior que não ter |
| **Mostrar quais faturas mudam antes de confirmar** | É a regra que falta para o editar existir: mexer numa fatura paga é permitido, mas nunca como efeito colateral silencioso (`docs/02-dominio/recorrencia.md`) |
| **Recorrência fora do cartão** | Sem fatura não há gatilho. A aba `CRÉDITO` é o único lugar com o segmento, e nas outras ele não aparece |
| **Filtro por cartão** | Com poucas séries as duas grades inteiras cabem na tela; filtro entra quando alguém tiver o problema |
| **Botão no cartão** | Os cartões não têm ação nenhuma enquanto `PATCH` e `DELETE` não existirem: botão que abre um modal que não salva é pior que a ausência dele |

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| O que é recorrência, o que é parcelamento e como cada uma nasce | `02-dominio/recorrencia` |
| Rota, payload e erros das duas | `04-api/endpoints-series` |
| Onde a série aparece no detalhe de um lançamento | `06-interface/extrato` |
| O rail, os atalhos e o lugar da tela na navegação | `06-interface/navegacao` |
| O ciclo do cartão e as ações de fatura | `06-interface/fatura` |
