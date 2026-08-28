---
id: 02-dominio/fatura-cartao
titulo: Fatura de cartão
dono: ciclo e datas da fatura, estados, a que fatura um lançamento pertence, fechamento, pagamento, reabertura e dívida de cartão
ler-junto: [02-dominio/meio-de-pagamento, 02-dominio/lancamento, 02-dominio/recorrencia]
status: ativo
---

# Fatura de cartão

A fatura é o que faz o crédito **não mentir no saldo**: a compra acontece num dia e o
dinheiro sai em outro, e é a fatura que amarra os dois. É também o único objeto de domínio
com **estado próprio salvo** — todo o resto do sistema se deriva de lançamento.

## Por que a fatura é entidade e o valor dela não

Fatura tem duas coisas que **não dá para derivar** dos lançamentos: em que ponto do ciclo
ela está (aberta, fechada) e as datas desse ciclo. Isso é estado, e estado se guarda.

O **valor** da fatura não é uma delas: é a soma dos lançamentos que apontam para ela,
calculada sempre, nunca armazenada — a mesma regra do saldo de conta
(`docs/02-dominio/conta.md`). Total armazenado é total que diverge.

## O ciclo

O usuário não informa duas datas soltas. Ele informa o que sabe de cabeça:

| Campo (no meio de pagamento `CREDITO`) | Exemplo |
|---|---|
| `diaVencimento` | 5 |
| `diasAntesFechamento` | 8 |

Cada fatura nasce com as duas datas já calculadas:

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
| Usuário muda `diaVencimento` ou `diasAntesFechamento` | Vale da próxima fatura a nascer em diante. Fatura já criada mantém suas datas |

## Estados: dois eixos independentes

O mesmo padrão de `docs/02-dominio/lancamento.md` — situação e categorização não se misturam
num estado só. Aqui, **ciclo** e **pagamento** também não.

### Eixo 1 — ciclo (salvo)

| Estado | Recebe compra nova | Significa |
|---|:--:|---|
| `FUTURA` | não | Existe só para segurar parcela de mês que ainda não chegou |
| `ABERTA` | **sim** | O ciclo corrente. **Exatamente uma por cartão** |
| `FECHADA` | não | O ciclo acabou. **Não congela nada** — ver Corrigir o passado |

Transições:

| De | Para | Gatilho |
|---|---|---|
| `FUTURA` | `ABERTA` | A fatura anterior fechou |
| `ABERTA` | `FECHADA` | Chegou a `dataFechamento` — ou o usuário fechou à mão |
| `FECHADA` | `ABERTA` | O usuário **abriu** a última fatura fechada. A seguinte volta a `FUTURA` |

**Não existe estado de "reaberta".** Ele foi cogitado e derrubado: era estado inventado para
proteger uma edição que já tem proteção melhor no lançamento. Abrir fatura serve para
**continuar lançando nela**; corrigir o passado é outra coisa, e não precisa de fatura aberta.

### Eixo 2 — pagamento (derivado)

Não é estado salvo: é comparação entre o que foi pago e o total da fatura.

| Leitura | Condição |
|---|---|
| **Em aberto** | Nenhum pagamento registrado |
| **Parcial** | Pago > 0 e menor que o total |
| **Quitada** | Pago ≥ total |

Um estado a menos é um estado a menos para dessincronizar.

## A que fatura um lançamento pertence

**Ao status da fatura, nunca à data do lançamento.**

| Situação | Fatura |
|---|---|
| Compra nova no cartão, qualquer `dataEvento` | A fatura **`ABERTA`** do cartão |
| Compra parcelada em N | 1ª parcela na `ABERTA`; as N−1 seguintes nas `FUTURA` seguintes, **criadas na hora se não existirem** |
| Ocorrência de recorrência do cartão | A fatura que **acabou de abrir** — o gatilho é o fechamento da anterior (`docs/02-dominio/recorrencia.md`) |
| Estorno de uma compra de fatura já fechada | A `ABERTA`. Não mexe na fechada: é fato novo, com data própria |

A regra da data foi descartada de propósito. A `dataFechamento` do sistema é uma **estimativa
do que o emissor faz** — o dia exato varia entre emissores, e uma compra do dia do fechamento
pode cair dos dois lados. Estado do sistema é fato; data de corte é palpite.

**Quando o palpite erra, quem corrige é o usuário**, movendo o lançamento de fatura pela
edição normal — **para qualquer fatura, aberta ou não**. `fatura` é campo editável do
lançamento (`docs/02-dominio/lancamento.md`), não resultado de cálculo imutável. Mover um
lançamento recalcula sua `dataEfeito` para o vencimento da fatura de destino.

A automação decide só onde o lançamento **nasce**. Depois disso, quem manda é o usuário.

## Fechamento

Automático, e é o gatilho de mais coisa do que parece:

1. A fatura `ABERTA` cuja `dataFechamento` chegou vira `FECHADA`.
2. A `FUTURA` seguinte vira `ABERTA` — criada na hora se não existir.
3. O sistema varre as **recorrências ativas** do cartão e lança a ocorrência do ciclo na
   fatura recém-aberta (`docs/02-dominio/recorrencia.md`). É por isso que recorrência não
   precisa de horizonte: o fechamento é o relógio dela.

**O fechamento é idempotente e recupera atraso.** Se a rotina não rodou — o Raspberry Pi
estava desligado, o container caiu — ela roda depois e fecha **todos** os ciclos vencidos,
um a um, na ordem. Fechar duas vezes a mesma fatura não faz nada.

**Fechar e abrir à mão existem como contingência**, não como fluxo normal: o banco fechou
em dia diferente, a rotina não rodou quando devia. O botão faz exatamente o que a rotina
faria.

**Fatura sem nenhum lançamento fecha do mesmo jeito**, com total zero, e já nasce quitada —
não há o que pagar. Ela não aparece em nenhuma fila de pendência.

## Pagamento

### Pagar não cria lançamento

Essa é a regra que impede o gasto de ser contado duas vezes. Os lançamentos da fatura **já
debitam a conta** que paga o cartão, com `dataEfeito` no vencimento
(`docs/02-dominio/meio-de-pagamento.md`). Um lançamento de "pagamento de fatura" por cima
somaria o mesmo dinheiro de novo.

O que o pagamento faz é mover os lançamentos da fatura de `PREVISTO` para `REALIZADO`, com
`dataEfeito` = **data em que foi pago** — não a do vencimento. Pagou dia 8 o que vencia dia
5, o dinheiro saiu dia 8.

**Consequência aceita:** o extrato do Cyberbank não fica igual ao extrato do banco. O banco
mostra um débito único de "PAGAMENTO FATURA"; o Cyberbank mostra as compras. Tem que ser
assim, senão o gasto por categoria não existe. Quem concilia os dois é
`docs/02-dominio/importacao-conciliacao.md`.

### Pagamento parcial

Pagar aceita **um valor**. Se ele for menor que o total, o resto é dívida que rola:

| Passo | O que acontece |
|---|---|
| 1 | Todos os lançamentos da fatura viram `REALIZADO` na data do pagamento, como sempre |
| 2 | Nasce um lançamento de **rolagem**: `ENTRADA` na conta que paga, valor = o que **não** foi pago, `REALIZADO` na mesma data |
| 3 | Nasce o par dele na **próxima fatura**: `SAIDA` de mesmo valor, `PREVISTO`, descrição "Saldo da fatura anterior" |

Os dois carregam `rolagemDeFatura` com o id da fatura de origem, existem juntos e são
apagados juntos — mesma disciplina do par de transferência. Nenhum dos dois tem categoria:
não é gasto, é dinheiro mudando de mês.

O efeito no saldo é o certo sem nenhuma regra especial: hoje a conta foi debitada só do que
saiu de verdade, e o saldo projetado do mês que vem já carrega o que ficou devendo.

### O sistema não calcula mínimo nem juros

**Não existe percentual mínimo definido por norma.** Os ~15% que todo mundo repete são
prática de mercado, e cada emissor monta a própria fórmula — o Nubank, por exemplo, cobra
15% das compras do mês, 15% do saldo de faturas anteriores, **100%** dos encargos (juros,
IOF, multa, saque) e as parcelas em curso. Calcular isso aqui seria errar em quase todo
cartão real.

Então o Cyberbank faz o que já faz com o estorno parcelado: **não decide, observa.** Juros,
IOF e multa entram como **lançamentos comuns** na fatura seguinte, quando aparecerem, com
categoria própria. O app nunca inventa uma taxa.

Duas regras de mercado que valem como contexto, não como cálculo: o rotativo dura no máximo
até o vencimento da fatura seguinte, quando o saldo tem que ser quitado ou parcelado
(Resolução CMN 4.549/2017); e juros mais encargos não podem passar de 100% da dívida
original (Lei 14.690/2023). Nenhuma das duas exige código na v1 — a segunda vira alerta
quando o app souber ler encargos.

## Abrir a fatura

Serve para uma coisa só: **o ciclo ainda está correndo e o sistema achou que tinha acabado.**
O caso real é o emissor não ter fechado no dia que o cartão diz — o sistema fechou dia 25, a
operadora fechou dia 28, e as compras desses três dias pertencem à fatura que acabou de
fechar. O usuário abre a fatura e volta a lançar nela normalmente.

| Regra | Valor |
|---|---|
| Qual fatura abre | **Só a última fechada.** Nas outras o botão nem aparece |
| Por quê | É a única em que "a seguinte volta a `FUTURA`" tem sentido. Abrir a de oito meses atrás rebaixaria oito faturas — e ninguém precisa disso: para mexer no passado, edita-se o lançamento |
| O que acontece com a seguinte | Volta a `FUTURA` na hora. Deixa de receber compra nova, e nada mais |
| O que já estava dentro dela | **Fica onde está.** Parcelas e ocorrências de recorrência não se mexem |
| Compras que caíram nela por engano | O usuário move, uma a uma, pela edição do lançamento |
| Se a última fechada já foi paga | O botão continua lá, com o aviso da seção seguinte. O sistema não é a palavra final |

Ao fechar de novo, a geração de recorrência é **idempotente por ciclo**: o fechamento nunca
lança a mesma assinatura duas vezes na mesma fatura (`docs/02-dominio/recorrencia.md`).

## Corrigir o passado

**O sistema não congela nada.** Fatura fechada não trava seus lançamentos, e nenhuma correção
exige abrir fatura: edita-se o lançamento, escolhendo inclusive **em que fatura ele fica**,
com ela aberta ou não. O sistema não tem a palavra final sobre o dinheiro do usuário — o que
ele deve é mostrar a consequência antes de aplicar e guardar quem mudou o quê.

O que a correção mexe depende de a fatura ter sido paga, e de **como**:

| Fatura do lançamento | O que acontece |
|---|---|
| `ABERTA`, ou `FECHADA` não paga | Só o total é reapurado. Não há mais nada a fazer |
| **Paga integralmente** | O sistema **avisa antes**, nomeando o pagamento, a data e a diferença. O valor pago acompanha o valor novo, na data original — **sem lançamento de ajuste** |
| **Paga em parte** | O **valor pago não muda**: foi um débito real no banco, na data em que foi. A diferença vai para a **rolagem**, que sobe ou desce |

A linha que separa os dois últimos casos vale para o sistema inteiro: **valor que o usuário
digitou é fato e não se reescreve; valor que o sistema derivou acompanha.** No pagamento
total ninguém digitou número — disse-se "pagar tudo", e "tudo" mudou. No parcial, R$ 500
foram R$ 500.

Nada disso dispara recálculo: saldo é sempre soma de lançamento (`docs/02-dominio/conta.md`),
então reescrever um valor **já refaz tudo que deriva dele**. A memória do que mudou vive no
histórico de alteração (`docs/02-dominio/lancamento.md`), não numa linha de ajuste que o
usuário não reconheceria no extrato do banco.

**Editar uma série pode mudar o valor de várias faturas de uma vez** — inclusive pagas. O
sistema **mostra quais** antes de confirmar (`docs/02-dominio/recorrencia.md`): mexer no
passado é permitido, mas nunca silencioso.

## Dívida de cartão

Definida aqui, usada em dois lugares que são o mesmo número com o sinal trocado.

> **Dívida de cartão = soma de todos os lançamentos de crédito ainda `PREVISTO`**, em
> qualquer fatura — `ABERTA`, `FECHADA` ou `FUTURA`.

Ou seja: tudo que foi comprado e ainda não foi pago, **inclusive as parcelas de 2027**.

| Onde aparece | Como |
|---|---|
| Limite disponível | `limite − dívida do cartão` (`docs/02-dominio/meio-de-pagamento.md`) |
| **Patrimônio** | O patrimônio **desconta a dívida**. Fatura em aberto é dinheiro que já não é seu (`docs/02-dominio/aplicacao-patrimonio.md`) |

Descontar as parcelas futuras é a decisão desconfortável e certa: quem parcelou R$ 5.000 em
10x deve os R$ 5.000 hoje, não R$ 500. Patrimônio que ignora parcela futura é patrimônio que
mente para cima justo quando importa.

## Quem pode

Fechar, abrir e registrar pagamento: **dono e editor** do ambiente
(`docs/02-dominio/ambiente-financeiro.md`). Leitor vê a fatura e não mexe.

## Invariantes

- Todo cartão de crédito ativo tem **exatamente uma** fatura `ABERTA`.
| Lançamento novo de crédito **nasce sempre** na `ABERTA` — nunca em `FUTURA` ou `FECHADA`.
  Só a edição do usuário move um lançamento para outra fatura.
- O valor da fatura **nunca é armazenado**: é sempre a soma dos lançamentos dela.
- **Nenhum estado de fatura impede edição de lançamento.** Fatura fechada não congela nada.
- Só a **última fatura fechada** de um cartão pode ser aberta.
- Abrir uma fatura devolve a seguinte para `FUTURA` sem mexer no que ela já contém.
- O fechamento nunca lança a mesma recorrência duas vezes na mesma fatura.
- Pagamento **parcial** informado pelo usuário nunca é reescrito por correção de lançamento.
- Pagar fatura **não cria lançamento de pagamento**, em nenhuma hipótese.
- Os dois lançamentos de uma rolagem existem juntos ou não existem.
- Uma fatura pertence a **um** meio de pagamento `CREDITO`, e ele a um só ambiente.
- Fatura sem lançamento fecha com total zero e já nasce quitada.
- Fechar a mesma fatura duas vezes não tem efeito.
- Cartão inativado **mantém a fatura em aberto viva** até fechar e ser paga
  (`docs/02-dominio/meio-de-pagamento.md`).

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Como `dataEfeito` é calculada por tipo de meio | `02-dominio/meio-de-pagamento` |
| Limite disponível e o que segura limite | `02-dominio/meio-de-pagamento` |
| Como as parcelas nascem e o que muda ao editar a compra | `02-dominio/recorrencia` |
| Estados do lançamento, histórico e correção × estorno | `02-dominio/lancamento` |
| Como o débito único do banco se casa com N lançamentos | `02-dominio/importacao-conciliacao` |
| Como a dívida entra no patrimônio | `02-dominio/aplicacao-patrimonio` |

## Ainda em aberto

- [ ] **Antecipar parcelas** — pagar hoje o que venceria em três faturas. É desconto sobre
      valor futuro; a operadora informa o valor. Fase 2
- [ ] **Parcelamento da própria fatura** oferecido pelo emissor: é um parcelamento novo que
      substitui a dívida, e precisa apagar a rolagem que já existia. Fase 2, junto com o
      rotativo real
- [ ] Alerta de **encargo acima do teto de 100%** — depende de o app saber ler encargo, o
      que só vem com a conciliação
- [ ] Fatura de cartão em **moeda estrangeira**: entra como lançamento já convertido, ou o
      sistema guarda os dois valores?
