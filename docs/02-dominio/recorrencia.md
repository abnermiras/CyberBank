---
id: 02-dominio/recorrencia
titulo: Recorrência e parcelamento
dono: as duas series de lancamentos: como nascem, como sao editadas e como sao canceladas
ler-junto: [02-dominio/lancamento, 02-dominio/fatura-cartao, 02-dominio/meio-de-pagamento]
status: rascunho
---

# Recorrência e parcelamento

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

## Parcelamento

Nasce de uma compra: valor total e número de parcelas. As **N parcelas nascem na hora**,
como `PREVISTO`, cada uma na fatura do seu mês (`docs/02-dominio/fatura-cartao.md`).

### Editar

**Altera todas as parcelas, sempre. O sistema não pergunta.** R$ 5.000 em 10x continua
sendo R$ 5.000: se as parcelas divergirem, o dado está errado, não flexível.

O procedimento, parcela a parcela:

1. Parcela em fatura **aberta** → altera direto.
2. Parcela em fatura **fechada** → reabre a fatura, altera, recalcula o total, fecha.
3. Parcela em fatura **paga** → reabre, altera, recalcula, **corrige o valor do
   pagamento** e fecha de novo.

O passo 3 não custa nada além do óbvio: como saldo é sempre a soma dos lançamentos e
nunca um total armazenado (`docs/02-dominio/conta.md`), reescrever o valor **já refaz
todos os saldos derivados**. Não existe recálculo a disparar.

### Cancelar

Cancelar um parcelamento não é apagar a compra — a compra aconteceu. As parcelas
**já realizadas ficam**; as `PREVISTO` somem.

> ☐ **A definir:** o que fazer quando o parcelamento é cancelado porque a compra foi
> estornada pela loja. Provavelmente é estorno, não cancelamento — ver a distinção em
> `docs/02-dominio/lancamento.md`.

## Recorrência

Nasce de uma regra: valor, periodicidade e dia. Normalmente **sem data de fim**.

### Geração: 12 meses rolantes

O sistema mantém sempre **~12 meses de lançamentos previstos à frente** e estende sozinho
conforme o tempo passa. Assim "quanto vou gastar de assinatura até o meio do ano que vem"
é consulta, não conta.

Doze e não infinito porque previsto é lançamento que ainda não aconteceu, e base cheia de
lançamento fantasma envelhece mal. Doze e não dois porque a projeção de saldo é uma das
perguntas que o produto promete responder.

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

- Fatura **fechada** congela seus lançamentos (`docs/02-dominio/lancamento.md`). Editar
  qualquer coisa lá dentro exige **reabrir** — inclusive quando o pedido veio de uma
  edição de série.
- Editar uma série pode, portanto, **reabrir várias faturas de uma vez**. O sistema faz
  isso sozinho, mas mostra ao usuário quais faturas serão reabertas antes de confirmar:
  reabrir fatura paga de dois meses atrás não pode ser efeito colateral silencioso.
- Corrigir uma fatura já paga **reescreve o passado**: o lançamento e o pagamento passam a
  valer o valor novo, na data original. O extrato fica igual ao documento do banco, e a
  memória do que mudou vive no histórico de alteração do lançamento, não numa linha de
  ajuste no extrato.

## Invariantes

- Um lançamento pertence a **no máximo uma** série (recorrência ou parcelamento), nunca às duas.
- Todas as parcelas de um parcelamento têm o **mesmo valor total de compra**: se a soma
  delas não bate com o valor da compra, é bug.
- Uma recorrência **não tem valor total** — perguntar "quanto custa a Netflix" só faz
  sentido por ocorrência.
- Uma recorrência sem data de fim sempre tem previstos gerados à frente; nunca fica com zero.
- Cancelar série nunca apaga ocorrência `REALIZADO`.
- Editar série nunca altera lançamento de fatura fechada **sem reabrir** a fatura.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Fechamento, pagamento e reabertura de fatura | `02-dominio/fatura-cartao` |
| Estados do lançamento e histórico de edição | `02-dominio/lancamento` |
| Débito automático (é atributo daqui, não meio) | este doc |
| Como uma ocorrência gerada se casa com o lançamento capturado | `02-dominio/importacao-conciliacao` |

> ☐ **A definir:** débito automático é atributo da recorrência (já decidido), mas falta
> escrever o que ele muda no comportamento — se nada muda além de rótulo, ele é rótulo.
