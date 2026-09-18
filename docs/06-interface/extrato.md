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
| 6 | **Série e fatura** | A fatura da compra no crédito — mês, estado e as duas datas. Série ainda reservada |
| 7 | **O que já aconteceu com ele** | O histórico |
| 8 | **Ações** | Editar, estornar e excluir |

### As duas datas só se explicam quando divergem

Quando `dataEvento` e `dataEfeito` são iguais — que é quase sempre —, o bloco mostra as duas e
cala. **Quando diferem, ele diz por quê**: o boleto foi lançado num dia e vence noutro, e é
essa divergência que faz os dois campos existirem (`docs/02-dominio/lancamento.md`). Explicar
sempre seria ruído; explicar só quando a coisa acontece é onde a regra vira aprendizado.

### O estabelecimento é rotulado como texto bruto

Ele vem da captura, antes de qualquer normalização, e por isso aparece marcado como tal — e
não como se fosse um nome que o sistema escolheu. `MEDTECH 24H` é o que a máquina do cartão
mandou, e a tela não finge o contrário.

### Série e fatura

**No crédito o bloco é real:** diz de qual fatura o lançamento é — *outubro de 2026*, com o
estado dela —, quando ela fecha e quando vence, e que **é no mês do vencimento que o gasto
conta** (`docs/02-dominio/lancamento.md`). É aqui que o eixo de competência da fatura para de
ser prosa de doc e vira coisa que a pessoa lê na linha dela.

**Fora do crédito ele diz que não se aplica**, em vez de sumir: só compra no crédito entra em
fatura, e a tela que esconde o que não faz ensina errado.

**A série continua reservada.** Parcelamento não existe (`docs/02-dominio/recorrencia.md`), e o
bloco segue **dizendo o que vai dizer** — *"parcela 3 de 10, da compra de R$ 5.000"*. É o mesmo
tratamento do bloco de fatura na Home e dos convites no Perfil: espaço reservado diz que o lugar
é aqui; espaço ausente ensina que o app não faz.

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

## Editar é o detalhe virando formulário

`EDITAR` não abre outra tela nem outro modal: os três primeiros blocos — **o dinheiro**, **as
duas datas** e **a classificação** — viram campos no lugar em que estavam. A pessoa corrige
olhando o mesmo lançamento que estava lendo, e o cabeçalho, o histórico e as ligações continuam
à vista.

**Meio e conta são um campo só**, porque é o meio que carrega a conta
(`docs/02-dominio/lancamento.md`). O rótulo diz `Nubank · Pix`, e é assim que se troca de
banco. O **vencimento** só aparece quando o meio escolhido separa as duas datas — trocar boleto
por Pix faz o campo sumir, e é a regra aparecendo em vez de ser explicada.

**O impacto é contínuo, não um aviso no fim.** Enquanto os campos mudam, a linha embaixo do
formulário diz em quanto o saldo de cada conta envolvida fica — duas linhas quando o meio leva
o lançamento para outra conta, e o aviso de que o outro lado acompanha quando é transferência.
É a exigência do `lancamento.md` — *ação retroativa mostra o impacto antes de confirmar* — em
vez de uma confirmação que a pessoa aprende a clicar sem ler.

`APLICAR` grava e volta para a leitura, com o de/para já no histórico logo abaixo.
`DESISTIR` não pergunta nada: nada foi enviado.

**Numa transferência o formulário é menor**, e o que falta nele é a regra: valor, data,
descrição e situação valem para os dois lados; conta, meio e categoria não se corrigem de um
lado só. O formulário diz isso em vez de esconder os campos calado.

## O filtro de conta responde com números, não só com linhas

Escolher uma conta no filtro passou a mostrar **três números** acima da lista: `SALDO AGORA`,
`PREVISTO ATÉ dd/mm` e `PROJETADO`. Sem eles, a única forma de saber quanto havia no Nubank era
filtrar e **somar as linhas à mão** — numa tela cujo cabeçalho diz *"saldo é sempre a soma
disto"*.

**Os três juntos são a conta inteira, não só o resultado**: o do meio é o que separa os outros
dois, e carrega a data no próprio rótulo. É a regra do `dashboard.md` — *número de projeção sem
a conta à vista é número que ninguém confere* — e é também o que explica por que um `PREVISTO`
visível na lista pode não estar no número: ele vence depois do fim do mês, e o rótulo diz até
onde a projeção foi.

**Com `TODAS AS CONTAS` não há números, e a ausência é a resposta.** Somar corrente com
aplicação daria um total que não responde pergunta nenhuma, e as três leituras que respondem —
em caixa, guardado e patrimônio — já existem na Home, cada uma com seu recorte
(`docs/02-dominio/conta.md`). A faixa diz isso e aponta para lá, em vez de inventar uma quarta
leitura que contradiria as três.

## As ações moram no detalhe

`ESTORNAR` e `EXCLUIR` **saíram da linha**. A linha ficou com uma função só — abrir —, e as
ações passaram para onde a pessoa já está olhando o que vai mexer.

O impacto continua sendo mostrado **antes** de confirmar: a exclusão diz em quanto o saldo da
conta fica, e nomeia o outro lado quando é transferência. É a regra de `navegacao.md` — *ação
destrutiva mostra o impacto numérico antes de confirmar* — no lugar em que ela finalmente tem
espaço para caber.

**Lançamento do ciclo tem uma ação só**, e é `CORRIGIR O VALOR`: o formulário abre com esse
campo e nenhum outro, e o texto diz por quê antes de a pessoa procurar o que falta. Excluir não
aparece, porque a abertura não é do usuário para apagar — as duas coisas juntas são a regra do
`lancamento.md` inteira na tela, não metade dela.

## O endereço abre o lançamento

`#/extrato/88` abre o Extrato **com o detalhe daquele lançamento aberto**, mesmo que ele não
esteja na primeira página e mesmo com a página recarregada do zero — é para isso que o detalhe
tem endpoint próprio (`docs/04-api/endpoints-lancamentos.md`).

Foi isso que fechou o buraco que o `docs/06-interface/navegacao.md` registrava: **o link do
Diário agora leva ao objeto, não à tela do objeto.** Fechar o modal volta para `#/extrato`, e o
botão "voltar" do navegador funciona como a pessoa espera.

## O que o detalhe ainda não faz

**Tirar a categoria de um lançamento já categorizado.** O seletor só oferece *sem categoria*
enquanto o lançamento está pendente, porque o `PATCH` lê campo ausente e campo nulo como a mesma
coisa (`docs/02-dominio/lancamento.md`). Oferecer a opção e não fazer nada seria pior que não
oferecer.

**Nomear o pagamento e a diferença** quando o lançamento corrigido é de fatura já paga. O aviso
de impacto de hoje fala de **saldo de conta**, que é tudo que existe; a fatura ainda não existe
no código, e a regra dela mora em `docs/02-dominio/fatura-pagamento.md`. Quando a fatura
chegar, é este mesmo bloco que ganha a segunda frase.
