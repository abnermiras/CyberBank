---
id: 02-dominio/recorrencia
titulo: Recorrência e parcelamento
dono: as duas series de lancamentos: como nascem, como sao editadas e como sao canceladas
ler-junto: [02-dominio/lancamento, 02-dominio/fatura-cartao, 02-dominio/meio-de-pagamento]
status: rascunho
---

# Recorrência e parcelamento

> **Quando.** O **parcelamento** é Fase 1. A **recorrência** não é
> (`docs/00-produto/roadmap.md`) — ela está escrita aqui porque a regra foi decidida junto
> com a fatura, e porque o passo do fechamento que a lança depende dela.

São **duas coisas diferentes** que geram várias linhas no extrato. Tratar as duas com a
mesma regra é o erro que este doc existe para impedir.

| | **Recorrência** | **Parcelamento** |
|---|---|---|
| Exemplo | Netflix, R$ 50 por mês | Aulas de espanhol, R$ 5.000 em 10x |
| O que é | **N eventos independentes** que se repetem por regra de tempo | **Uma compra só**, dividida em N |
| Fim | Normalmente **sem data limite** | Fechado: exatamente N |
| Valor total | Não existe. Cada mês é um valor | R$ 5.000, e a soma das parcelas tem que dar isso |
| Netflix de junho a R$ 50 e de julho a R$ 55 | Dois fatos verdadeiros | Impossível: seria a mesma compra valendo dois valores |

Dessa diferença sai tudo o que vem abaixo. **A recorrência pode perguntar; o parcelamento
não pode.**

## Duas entidades, não uma com `tipo`

São **duas tabelas**, não uma `Série` com um campo `tipo`. O critério do projeto — *tipo novo
só existe se alguma regra do sistema mudar por causa dele* — aqui aponta para o contrário do
costume: **todas** as regras mudam por tipo (como nascem, como se editam, o que é cancelar, se
existe total). Quando todas mudam, não é um tipo: são duas coisas. Um campo `tipo` viraria
`if (tipo == RECORRENCIA)` espalhado, que `docs/02-dominio/meio-de-pagamento.md` chama de bug
de modelagem.

**`Parcelamento`**

| Campo | Nota |
|---|---|
| `ambiente` | O de quem comprou |
| `valorDaCompra` | Inteiro em centavos. **Guardado** — é ele que torna verificável a invariante da soma |
| `parcelas` | O N. Fechado desde o nascimento |
| `dataDaCompra` | Vira a `dataEvento` de todas as parcelas (`ADR-0006`) |
| `descricao`, `categoria`, `cartao` | O que as parcelas herdam ao nascer |

Não tem `ativa` e não tem estado: um parcelamento não é ligado nem desligado. Ele existe, e
acaba quando a última parcela é paga.

**`Recorrencia`**

| Campo | Nota |
|---|---|
| `ambiente` | O de quem criou |
| `valor` | O da ocorrência. **Não existe valor total** |
| `periodicidade` e `dia` | A regra de tempo |
| `inicio` | A partir de quando vale |
| `ativa` | Cancelar desliga; o passado fica |
| `conta` ou `cartao`, `meio`, `descricao`, `categoria` | O que a ocorrência herda ao nascer |

Ela é entidade porque é **regra**, e regra não se deriva de lançamento nenhum — o mesmo
motivo pelo qual a fatura é entidade e o valor dela não
(`docs/02-dominio/fatura-cartao.md`).

**No lançamento** ficam dois campos anuláveis, `parcelamento` e `recorrencia`, e no máximo um
dos dois é preenchido (`docs/02-dominio/lancamento.md`).

## Parcelamento

Nasce de uma compra: valor total e número de parcelas. As **N parcelas nascem na hora**,
todas `PROVISIONADAS` na data da compra (`ADR-0006`), cada uma na fatura do seu mês
(`docs/02-dominio/fatura-cartao.md`).

**O centavo que sobra vai na primeira parcela.** R$ 5.000 em 3x é 1.666,68 + 1.666,66 +
1.666,66 — é o que a maioria dos emissores faz, então o valor do app bate com o da fatura sem
ninguém corrigir nada. A soma das parcelas é **sempre** igual ao valor da compra, e o
arredondamento é do sistema: o usuário não edita uma parcela sozinha, porque isso quebraria a
soma. Editar o parcelamento muda o valor da compra, e o sistema redistribui as N.

### Editar

**Altera todas as parcelas, sempre. O sistema não pergunta.** R$ 5.000 em 10x continua
sendo R$ 5.000: se as parcelas divergirem, o dado está errado, não flexível.

Isso ficou literal quando as parcelas passaram a nascer todas juntas, **todas
`PROVISIONADAS` na data da compra** (`ADR-0006`): elas são uma compra só, dividida em N
cobranças.

O procedimento, parcela a parcela:

1. Parcela em fatura **aberta** → altera direto.
2. Parcela em fatura **fechada** e não paga → altera direto; o total se reapura.
3. Parcela em fatura **paga** → altera, e o sistema pergunta o que fazer com a diferença:
   ajustar o pagamento, ou deixá-la como saldo da conta `CARTAO`
   (`docs/02-dominio/fatura-cartao.md`).

O passo 3 não custa nada além do óbvio: como saldo é sempre a soma dos lançamentos e
nunca um total armazenado (`docs/02-dominio/conta.md`), reescrever o valor **já refaz
todos os saldos derivados**. Não existe recálculo a disparar.

### Cancelar

Cancelar um parcelamento não é apagar a compra — a compra aconteceu. As parcelas
**já realizadas ficam**; as `PREVISTO` somem.

### Quando a loja estorna a compra

**Isso não é cancelar o parcelamento.** É como a operadora de cartão realmente age: ela
credita o **valor total da compra de uma vez** — não o que você já pagou, e não parcelado
— e as parcelas restantes seguem seu curso na fatura. Os dois se compensam ao longo dos
meses seguintes.

Uma compra de R$ 5.000 em 10x com 4 parcelas pagas gera um crédito de **R$ 5.000**, não
de R$ 2.000. As 6 parcelas que faltam continuam caindo, e no fim a compra custou zero.

Por isso o sistema **não toca no parcelamento**: registra um **lançamento de estorno**
(`docs/02-dominio/lancamento.md`) e deixa as parcelas correrem. Como saldo é sempre a soma
dos lançamentos, a compra se anula sozinha — sem regra especial, sem operação de
cancelamento, sem editar uma série que representa um fato que aconteceu.

O parcelamento continua sendo verdade histórica: aquela compra foi feita e foi dividida
em 10. O estorno é outro fato, com data própria. Um não edita o outro.

> ☐ **A definir:** alguns emissores cancelam as parcelas restantes em vez de deixá-las
> correr, e a prática varia entre eles. Como o sistema descobre qual aconteceu? A resposta
> provavelmente é: **não decide, observa** — o que vier na fatura é a verdade
> (`docs/02-dominio/importacao-conciliacao.md`). Confirmar ao escrever a conciliação.

## Recorrência

Nasce de uma regra: valor, periodicidade e dia. Normalmente **sem data de fim**.

### Geração: uma ocorrência por ciclo. **Nunca um horizonte.**

**Recorrência não gera previstos futuros.** Um lançamento previsto não é só um item de
tela: ele **segura limite do cartão** e sugere que a assinatura tem fim. Assinatura não
acaba e não reserva o limite de um mês que não chegou. Gerar doze meses de Netflix
comeria doze mensalidades do limite e faria a assinatura parecer um contrato de um ano.

A geração segue o **ciclo**, uma ocorrência por vez, com dois gatilhos:

| Recorrência | Gatilho | Momento |
|---|---|---|
| **No cartão** | A fatura fecha e a seguinte abre | O sistema varre as recorrências ativas daquele cartão e lança a ocorrência na fatura recém-aberta |
| **Fora do cartão** (débito, boleto) | Virada do mês | Não existe fatura para disparar; o mês é o ciclo |

O resultado é a invariante que importa: **existe no máximo uma ocorrência ainda não
acontecida por recorrência** — a do ciclo que está aberto agora.

Isso não custa a promessa do produto. "Quanto sobra até o fim do mês" continua respondida,
porque a ocorrência do ciclo corrente existe. Projeção mais longa (Fase 3) se calcula a
partir da **regra** da recorrência, não de lançamentos pré-criados.

> **No crédito, `PREVISTO` significa "ainda não aconteceu"**, como em todo lugar. A compra
> de ontem no cartão é `PROVISIONADA` — aconteceu, falta a fatura ser paga (`ADR-0006`). No
> cartão, `PREVISTO` sobra só para a **ocorrência de recorrência que ainda não foi cobrada**;
> parcela de mês à frente já nasce provisionada, porque a compra aconteceu uma vez.

### Editar

Quando o valor (ou qualquer atributo) de uma recorrência muda, **o sistema pergunta**:

> Alterar **somente as futuras**, ou **o passado também**?

| Resposta | O que acontece |
|---|---|
| **Somente futuras** | O passado não muda. Os previstos ainda não gerados, e os já gerados à frente, passam a valer o valor novo |
| **O passado também** | Cada ocorrência passada é corrigida: abre a fatura, altera, fecha; se paga, corrige o valor do pagamento |

A pergunta existe porque as duas respostas são legítimas. "A Netflix aumentou para R$ 55"
é o caso *somente futuras* — junho a R$ 50 é verdade histórica. "Sempre foi R$ 55 e eu
cadastrei errado" é o caso *passado também*.

**Essa pergunta é da recorrência e só dela.** Fazer a mesma pergunta num parcelamento
seria oferecer ao usuário a opção de deixar o próprio dado inconsistente.

### Cancelar

Cancelou, acabou: **todos os previstos à frente somem**, junto com a regra. O passado
fica intacto — aquelas cobranças aconteceram.

Se ainda havia um ciclo por cobrar quando você cancelou, ele se lança à mão. É mais raro
do que o caso comum, e o caso comum não deve pagar o preço do raro.

## O cruzamento com a fatura

É onde as duas séries encostam na regra de fatura fechada, e é o motivo deste doc existir.

- Fatura **fechada não congela nada** (`docs/02-dominio/fatura-cartao.md`): a parcela se
  altera onde estiver, sem abrir fatura nenhuma.
- Editar uma série pode, portanto, **mudar o valor de várias faturas de uma vez** —
  inclusive pagas. O sistema mostra **quais** antes de confirmar: mexer numa fatura paga de
  dois meses atrás é permitido, mas nunca pode ser efeito colateral silencioso.
- Corrigir uma fatura já paga **pergunta** o que fazer com a diferença: ajustar o pagamento
  ou deixá-la como saldo da conta `CARTAO`. A memória do que mudou vive no histórico de
  alteração do lançamento, não numa linha de ajuste no extrato.

## Invariantes

- Um lançamento tem `parcelamento` **ou** `recorrencia` preenchido, nunca os dois — e na
  maioria das vezes, nenhum dos dois.
- `Parcelamento` guarda o valor da compra; `Recorrencia` não tem valor total nenhum.
- Todas as parcelas de um parcelamento têm o **mesmo valor total de compra**: se a soma
  delas não bate com o valor da compra, é bug.
- O centavo do arredondamento vai na **primeira** parcela, e o usuário não edita parcela
  isolada.
- Uma recorrência **não tem valor total** — perguntar "quanto custa a Netflix" só faz
  sentido por ocorrência.
- Uma recorrência ativa tem **no máximo uma** ocorrência ainda não acontecida: a do ciclo
  aberto agora. Nunca um horizonte.
- Cancelar série nunca apaga ocorrência `REALIZADO`.
- Editar série nunca muda o valor de uma fatura paga **sem mostrar quais** antes de confirmar.
- O fechamento nunca lança a mesma recorrência duas vezes na mesma fatura.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Fechamento, pagamento e reabertura de fatura | `02-dominio/fatura-cartao` |
| Estados do lançamento e histórico de edição | `02-dominio/lancamento` |
| Débito automático (é atributo daqui, não meio) | este doc |
| Como uma ocorrência gerada se casa com o lançamento capturado | `02-dominio/importacao-conciliacao` |

> ☐ **A definir:** débito automático é atributo da recorrência (já decidido), mas falta
> escrever o que ele muda no comportamento — se nada muda além de rótulo, ele é rótulo.

> ☐ **A definir:** com `ADR-0006`, todas as parcelas têm `dataEvento` **da compra** — e o
> relatório de gasto, que usa `dataEvento`, joga os R$ 5.000 inteiros no mês da compra. É a
> leitura fiel ao fato ("comprei R$ 5.000 em julho"), e não é a que a maioria dos apps mostra
> ("R$ 500 por mês"). Se a segunda for a desejada, quem espalha é o **relatório**, olhando a
> fatura de cada parcela — não a data delas. Decidir com uma jornada real na mão.
