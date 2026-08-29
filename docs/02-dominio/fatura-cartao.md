---
id: 02-dominio/fatura-cartao
titulo: Fatura de cartão
dono: ciclo e datas da fatura, estados, a que fatura um lancamento pertence, fechamento e abertura
ler-junto: [02-dominio/fatura-pagamento, 02-dominio/conta, 02-dominio/meio-de-pagamento]
status: ativo
---

# Fatura de cartão

**A fatura é o recorte de um período da conta `CARTAO`.** O contrato de cartão é uma conta
cujo saldo é a dívida (`ADR-0003`); a fatura é o que foi gasto entre um fechamento e o
seguinte, com uma data para pagar.

Isso é o que faz a fatura ser barata: ela não move dinheiro, não guarda total e não muda
lançamento de lugar. Quem move dinheiro é a compra (que aumenta a dívida) e o pagamento
(que a diminui).

**Pagamento, rolagem e correção do passado vivem em
`docs/02-dominio/fatura-pagamento.md`.** Aqui fica o ciclo: datas, estados, a que fatura um
lançamento pertence, fechamento e abertura.

## Por que a fatura é entidade e o valor dela não

Fatura tem duas coisas que **não dá para derivar**: em que ponto do ciclo ela está e as
datas desse ciclo. Isso é estado, e estado se guarda.

O **valor** não é uma delas: é a soma dos lançamentos que apontam para ela, calculada
sempre, nunca armazenada — a mesma regra do saldo de conta (`docs/02-dominio/conta.md`).

## O ciclo

O usuário não informa duas datas soltas. Ele informa o que sabe de cabeça, na conta
`CARTAO`:

| Campo (na conta `CARTAO`) | Exemplo |
|---|---|
| `diaVencimento` | 5 |
| `diasAntesFechamento` | 8 |
| `contaPagadoraPadrao` | A `Nubank` |

Cada fatura nasce com as duas datas calculadas:

- `dataVencimento` = `diaVencimento` do mês da fatura.
- `dataFechamento` = `dataVencimento` − `diasAntesFechamento` (dias corridos), o que a joga
  naturalmente para o mês anterior.

> **Exemplo literal.** Cartão vence dia 5 e fecha 8 dias antes.
> Fatura de **março/2027**: fechamento em **25/02/2027**, vencimento em **05/03/2027**.

Bordas:

| Caso | Regra |
|---|---|
| `diaVencimento` maior que o mês (31 em fevereiro) | Cai no **último dia do mês** |
| Vencimento em fim de semana ou feriado | A data **não muda**. O pagamento real tem a data que tiver, e é ela que vale — o sistema não carrega calendário de feriados |
| Usuário muda o ciclo | Vale da próxima fatura a nascer. Fatura já criada mantém suas datas |

## Estados: dois eixos independentes

### Eixo 1 — ciclo (salvo)

| Estado | Recebe compra nova | Significa |
|---|:--:|---|
| `FUTURA` | não | Existe só para segurar parcela de mês que ainda não chegou |
| `ABERTA` | **sim** | O ciclo corrente. **Exatamente uma por conta `CARTAO`** |
| `FECHADA` | não | O ciclo acabou. **Não congela nada** — ver Corrigir o passado |

| De | Para | Gatilho |
|---|---|---|
| `FUTURA` | `ABERTA` | A fatura anterior fechou |
| `ABERTA` | `FECHADA` | Chegou a `dataFechamento` — ou o usuário fechou à mão |
| `FECHADA` | `ABERTA` | O usuário **abriu** a última fatura fechada. A seguinte volta a `FUTURA` |

**Não existe estado de "reaberta".** Ele foi cogitado e derrubado: era estado inventado para
proteger uma edição que já tem proteção melhor no lançamento. Abrir fatura serve para
**continuar lançando nela**; corrigir o passado é outra coisa, e não precisa de fatura
aberta.

### Eixo 2 — pagamento (derivado)

Não é estado salvo: é comparação entre o que foi pago e o total da fatura.

| Leitura | Condição |
|---|---|
| **Em aberto** | Nenhum pagamento apontando para ela |
| **Parcial** | Pago > 0 e menor que o total |
| **Quitada** | Pago ≥ total |
| **Rolada** | Venceu sem ser quitada: o que faltava rolou para a `ABERTA`, e não há mais o que pagar nela |

**Quitada** e **rolada** são as duas formas de a fatura **encerrar** — e é o encerramento,
nunca o fechamento nem um pagamento parcial, que liquida os lançamentos dela
(`docs/02-dominio/fatura-pagamento.md`).

## A situação do lançamento de crédito

Comprar no cartão **cria dívida na hora e não liquida nada** — e é exatamente isso que a
situação `PROVISIONADO` diz (`ADR-0006`):

| Lançamento | `situacao` | `dataEfeito` |
|---|---|---|
| Compra no crédito, **à vista ou parcelada** | `PROVISIONADO` | = `dataEvento`. Comprou, deve |
| **Todas as N parcelas**, inclusive as de meses à frente | `PROVISIONADO`, desde a compra | = a data da compra |
| Ocorrência de recorrência que ainda não foi cobrada | `PREVISTO` até a data dela chegar | a data dela |
| Qualquer um deles, depois que a fatura dele **encerra** — quitada, ou vencida e rolada | `REALIZADO` | não muda |

**As parcelas nascem todas juntas e todas provisionadas** porque a compra aconteceu **uma
vez**: quem parcelou R$ 5.000 em 10x deve R$ 5.000 hoje, e o limite já se comporta assim. O
que espalha a cobrança pelos meses é a **fatura** de cada parcela, não a situação delas.

Numa mesma fatura, portanto, tudo tem a mesma situação. Uma compra à vista e uma parcela
5/10 esperam o mesmo pagamento no mesmo dia — não havia o que justificasse a diferença.

E a dívida sai de graça: **é o saldo da conta `CARTAO`**. O que já aconteceu conta; o
pagamento previsto, que é `PREVISTO`, não conta. Sem cálculo especial
(`docs/02-dominio/conta.md`).

## A que fatura um lançamento pertence

**Ao status da fatura, nunca à data do lançamento.**

| Situação | Fatura |
|---|---|
| Compra nova no cartão, qualquer `dataEvento` | A fatura **`ABERTA`** da conta `CARTAO` |
| Compra parcelada em N | 1ª parcela na `ABERTA`; as N−1 seguintes nas `FUTURA` seguintes, **criadas na hora se não existirem** |
| Ocorrência de recorrência do cartão | A fatura que **acabou de abrir** — o gatilho é o fechamento da anterior (`docs/02-dominio/recorrencia.md`) |
| Estorno de uma compra de fatura já fechada | A `ABERTA`. Não mexe na fechada: é fato novo, com data própria |

A regra da data foi descartada de propósito. A `dataFechamento` do sistema é uma **estimativa
do que o emissor faz** — o dia exato varia entre emissores, e uma compra do dia do fechamento
pode cair dos dois lados. Estado do sistema é fato; data de corte é palpite.

**Quando o palpite erra, quem corrige é o usuário**, movendo o lançamento de fatura pela
edição normal — **para qualquer fatura, aberta ou não**. `fatura` é campo editável do
lançamento (`docs/02-dominio/lancamento.md`), não resultado de cálculo imutável.

A automação decide só onde o lançamento **nasce**. Depois disso, quem manda é o usuário.

## Fechamento

Automático, e é o gatilho de mais coisa do que parece:

1. A fatura `ABERTA` cuja `dataFechamento` chegou vira `FECHADA`. **Nenhuma situação muda
   aqui:** fechar é recortar o período, não liquidar. Quem liquida é o **encerramento**
   (`docs/02-dominio/fatura-pagamento.md`).
2. A `FUTURA` seguinte vira `ABERTA` — criada na hora se não existir.
3. O sistema varre as **recorrências ativas** do cartão e lança a ocorrência do ciclo na
   fatura recém-aberta (`docs/02-dominio/recorrencia.md`), **sem repetir** o que já lançou
   naquele ciclo.
4. Nasce o **pagamento previsto**: uma transferência `PREVISTO` da `contaPagadoraPadrao`
   para a conta `CARTAO`, no valor da fatura, com `dataEfeito` no vencimento.

O passo 4 é o que mantém a promessa do produto. Como a compra no crédito não toca mais a
conta corrente, é o pagamento previsto que faz "quanto sobra até o fim do mês" continuar
contando a fatura que vai vencer.

**O fechamento é idempotente e recupera atraso.** Se a rotina não rodou — o Raspberry Pi
estava desligado, o container caiu — ela roda depois e fecha **todos** os ciclos vencidos,
um a um, na ordem. Fechar duas vezes a mesma fatura não faz nada.

**Fechar e abrir à mão existem como contingência**, não como fluxo normal: o banco fechou em
dia diferente, a rotina não rodou quando devia.

**Fatura sem nenhum lançamento fecha do mesmo jeito**, com total zero, e não gera pagamento
previsto — não há o que pagar.

## Abrir a fatura

Serve para uma coisa só: **o ciclo ainda está correndo e o sistema achou que tinha acabado.**
O caso real é o emissor não ter fechado no dia que a conta `CARTAO` diz — o sistema fechou
dia 25, a operadora fechou dia 28, e as compras desses três dias pertencem à fatura que
acabou de fechar.

| Regra | Valor |
|---|---|
| Qual fatura abre | **Só a última fechada.** Nas outras o botão nem aparece |
| Por quê | É a única em que "a seguinte volta a `FUTURA`" tem sentido. Abrir a de oito meses atrás rebaixaria oito faturas — e para mexer no passado edita-se o lançamento |
| A fatura seguinte | Volta a `FUTURA` na hora. Deixa de receber compra nova, e nada mais |
| O que já estava dentro dela | **Fica onde está.** Parcelas e ocorrências de recorrência não se mexem |
| Compras que caíram nela por engano | O usuário move, uma a uma, pela edição do lançamento |
| O pagamento previsto que já tinha nascido | Volta a acompanhar o total, enquanto não tiver sido pago |

## Dívida e limite

A dívida do cartão não é cálculo com regra própria: **é o saldo da conta `CARTAO`**
(`ADR-0003` e `ADR-0006`).

| Leitura | Como |
|---|---|
| **Limite disponível** | `limite − dívida`, e dívida é o saldo da conta. Ele já inclui as parcelas futuras, porque elas são provisionadas desde a compra (`docs/02-dominio/meio-de-pagamento.md`) |
| **Patrimônio** | Soma a conta `CARTAO` como qualquer outra, e ela é negativa. Nenhum tratamento especial (`docs/02-dominio/aplicacao-patrimonio.md`) |
| **Quanto devo desta fatura** | O total dela menos o que já foi pago **e menos o que já rolou** |

## Quem pode

Fechar, abrir e registrar pagamento: **dono e editor** do ambiente
(`docs/02-dominio/ambiente-financeiro.md`). Num cartão compartilhado, cada ambiente paga a
partir das contas que ele acessa (`docs/02-dominio/compartilhamento.md`). Leitor vê e não
mexe.

## Invariantes

- Toda fatura pertence a **uma** conta `CARTAO`, e toda conta `CARTAO` ativa tem
  **exatamente uma** fatura `ABERTA`.
- Lançamento de crédito nasce pelo **status**, nunca pela data: a compra à vista e a 1ª
  parcela na `ABERTA`, a parcela *k* na *k*-ésima fatura a partir dela — criada como
  `FUTURA` se ainda não existir. **Nenhum nasce em `FECHADA`.** Depois disso, só a edição do
  usuário move um lançamento de fatura.
- O valor da fatura **nunca é armazenado**: é sempre a soma dos lançamentos dela.
- **Nenhum estado de fatura impede edição de lançamento.** Fatura fechada não congela nada.
- Só a **última fatura fechada** pode ser aberta, e abrir devolve a seguinte para `FUTURA`
  sem mexer no que ela já contém.
- O fechamento nunca lança a mesma recorrência duas vezes na mesma fatura.
- Fatura sem lançamento fecha com total zero e não gera pagamento previsto.
- **Fechar não liquida nada; encerrar sim.** Uma fatura encerra ao ser quitada ou ao vencer
  e rolar — e só então os lançamentos dela deixam de ser `PROVISIONADO`.
- Cartão inativado **mantém a fatura em aberto viva** até fechar e ser paga.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Por que o contrato de cartão é uma conta | `ADR-0003` |
| Tipos de cartão, limite e o que é adicional | `02-dominio/meio-de-pagamento` |
| Partes da fatura e quem paga cada uma | `02-dominio/compartilhamento` |
| Como as parcelas nascem e o que muda ao editar a compra | `02-dominio/recorrencia` |
| Estados do lançamento, transferência e histórico | `02-dominio/lancamento` |
| Como a fatura real do banco se casa com os lançamentos | `02-dominio/importacao-conciliacao` |
| Pagamento, rolagem e correção do passado | `02-dominio/fatura-pagamento` |

## Ainda em aberto

- [ ] **Antecipar parcelas** — pagar hoje o que venceria em três faturas, com desconto que a
      operadora informa. Fase 2
- [ ] **Parcelamento da própria fatura** oferecido pelo emissor: vira um parcelamento novo
      sobre a dívida que ficou na conta `CARTAO`. Fase 2
- [ ] A parcela futura tem `dataEvento` da compra ou do mês dela? Muda o relatório de gasto
      por categoria de quem parcela muito — decidir em `docs/02-dominio/recorrencia.md`
- [ ] Fatura em **moeda estrangeira**: entra já convertida, ou guarda os dois valores?
