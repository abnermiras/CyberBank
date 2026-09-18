---
id: 04-api/endpoints-faturas
titulo: Endpoints de faturas
dono: contrato dos endpoints de fatura de cartao
ler-junto: [02-dominio/fatura-cartao, 02-dominio/fatura-pagamento, 04-api/convencoes]
status: ativo
---

# Endpoints de faturas

Família do ambiente: tudo aqui vive sob `/api/v1/ambientes/{ambienteId}/`
(`docs/04-api/convencoes.md`). As regras são de `docs/02-dominio/fatura-cartao.md` (ciclo) e
`docs/02-dominio/fatura-pagamento.md` (dinheiro).

**A fatura é recurso de topo, não sub-recurso da conta.** Ela é o que se fecha, se abre e se
paga, e as ações dela viram sub-recursos dela — `POST .../faturas/42/pagamentos` —, não da
conta. O cartão entra como **filtro**, porque a pergunta é sempre *"as faturas de qual
contrato?"*.

**Nenhum dos três números é campo guardado.** `total`, `pago` e `rolado` são soma de lançamento,
calculada na leitura — a mesma regra do saldo de conta. A fatura guarda só o que não se deriva:
o ponto do ciclo e as duas datas.

## `GET /api/v1/ambientes/{ambienteId}/faturas?contaId={contaId}`

As faturas de um contrato de cartão, **da mais recente para a mais antiga**, com o cabeçalho do
cartão. É o que a tela Fatura desenha inteira, numa requisição só.

| Parâmetro | Obrigatório | Valor |
|---|:--:|---|
| `contaId` | sim | A conta `CARTAO`. Sem ele não há pergunta: somar faturas de contratos diferentes não responde nada |

```
GET /api/v1/ambientes/1/faturas?contaId=9

200 OK
{
  "cartao": {
    "id": 9, "nome": "UltraVioleta",
    "diaVencimento": 5, "diasAntesFechamento": 8,
    "limiteCentavos": 1500000, "limiteInformadoEm": "2026-09-18",
    "contaPagadoraPadraoId": 1,
    "dividaCentavos": 356080,
    "limiteDisponivelCentavos": 1143920,
    "limitePodeEstarDesatualizado": false
  },
  "itens": [
    { "id": 42, "competencia": "2026-10",
      "dataFechamento": "2026-09-27", "dataVencimento": "2026-10-05",
      "status": "ABERTA",
      "totalCentavos": 356080, "pagoCentavos": 0, "roladoCentavos": 0,
      "agendadoCentavos": 0, "aPagarCentavos": 356080,
      "encerrada": false, "rolada": false, "recebePagamento": false }
  ]
}
```

| Campo do cartão | Nota |
|---|---|
| `dividaCentavos` | **É o saldo da conta `CARTAO`**, com o sinal virado para leitura. Sem cálculo próprio (`ADR-0003`), e já inclui a parcela de daqui a oito meses, porque ela é provisionada desde a compra |
| `limiteCentavos` · `limiteInformadoEm` | **Ausentes** quando o limite nunca foi informado. Valor informado carrega a data (regra 7 do `CLAUDE.md`) |
| `limiteDisponivelCentavos` | `limite − dívida`. **Ausente** sem limite informado — número que não descreve nada não é devolvido |
| `limitePodeEstarDesatualizado` | O disponível ficou **negativo**: a dívida passou do limite informado. O sinal de que ele envelheceu não é um prazo, é este fato. **O limite nunca trava um lançamento** — ele orienta |

| Campo da fatura | Nota |
|---|---|
| `competencia` | `"AAAA-MM"`. O mês da fatura, que é o eixo de ordem do ciclo |
| `status` | `FUTURA`, `ABERTA` ou `FECHADA`. **Não existe `REABERTA`** |
| `totalCentavos` | Soma dos lançamentos que apontam para ela, **menos o lado crédito de uma rolagem**. É por isso que o total histórico não cai quando a fatura rola (`ADR-0005`) |
| `pagoCentavos` | Soma dos pagamentos **`REALIZADO`** que apontam para ela. O pagamento agendado ainda não pagou |
| `roladoCentavos` | O crédito que o total já não conta |
| `agendadoCentavos` | Quanto já tem pagamento **marcado e ainda não realizado**. **Não entra no `a pagar`** — serve para a tela não pedir de novo o que você já resolveu, e é ele que impede a projeção de contar a mesma dívida duas vezes (`docs/02-dominio/conta.md`) |
| `aPagarCentavos` | `total − pago − rolado`. **Negativo é crédito** no cartão, e isso existe na vida real |
| `encerrada` | `aPagar <= 0`. **É leitura, não estado salvo** — não existe "encerrada" como carimbo; existe o número (`docs/02-dominio/fatura-pagamento.md`) |
| `rolada` | Venceu sem ser quitada e o que faltava rolou |
| `recebePagamento` | **A janela**: `FECHADA` com `a pagar` maior que zero. Um número, duas operações — é o mesmo campo que diz se a fatura pode ser **aberta**, e é por isso que ele vem pronto em vez de a tela recompor a condição |

**`aPagarCentavos` maior que zero numa `FECHADA` é a janela** em que a fatura pode ser **paga**
e pode ser **aberta**: um número, duas operações (`docs/02-dominio/fatura-cartao.md`).

| Erro | Quando |
|---|---|
| `CONTA_NAO_E_CARTAO` (409) | A conta existe e não é `CARTAO`. As outras não têm ciclo nenhum para recortar |
| `NAO_ENCONTRADO` (404) | Conta inexistente ou de outro ambiente |

## `POST /api/v1/ambientes/{ambienteId}/faturas/{faturaId}/pagamentos`

Paga a fatura. **É uma transferência** da conta pagadora para a conta `CARTAO`, e nada além
disso (`docs/02-dominio/fatura-pagamento.md`).

```
POST /api/v1/ambientes/1/faturas/42/pagamentos
{ "contaPagadoraId": 1, "valor": 90000, "dataEvento": "2026-10-19" }

200 OK
{ "cartao": { ... }, "itens": [ ... ] }
```

| Campo | Obrigatório | Nota |
|---|:--:|---|
| `contaPagadoraId` | sim | **Qualquer conta que o ambiente acesse.** A `contaPagadoraPadrao` do cartão só preenche o formulário; nada nasce dela sozinho |
| `valor` | sim | Inteiro em centavos. **Pagar menos é permitido** — a fatura fica parcial e nada é liquidado. Pagar mais também: sobra crédito na conta `CARTAO`, e isso existe na vida real |
| `dataEvento` | não | Padrão hoje. **Dia à frente nasce `PREVISTO` e realiza pela data**, como um boleto registrado; hoje nasce `REALIZADO`. É declaração sua, e por isso não precisa de confirmação depois |

A resposta é o **cartão inteiro**, não o lançamento criado: quem chamou está olhando a tela da
fatura, e devolver um lançamento solto obrigaria a uma segunda requisição só para redesenhar.

**Quitar encerra a fatura na hora** — os lançamentos dela saem de `PROVISIONADO`. Quando o
pagamento é agendado, quem encerra é a **rotina**, no dia em que ela o realiza
(`docs/02-dominio/fatura-pagamento.md`).

| Erro | Quando |
|---|---|
| `FATURA_NAO_RECEBE_PAGAMENTO` (409) | Não é `FECHADA` com `a pagar` maior que zero. `FUTURA` o emissor nem emitiu; na `ABERTA` o valor ainda vai mudar (pagar antes é **antecipar**, Fase 2); encerrada já tem `a pagar` zero, e pagar de novo descontaria a mesma dívida duas vezes |
| `BENEFICIO_NAO_TRANSFERE` (409) | A pagadora é uma conta `BENEFICIO`: aquele saldo não é fungível |
| `CONTA_INATIVA` (409) | A conta pagadora está inativa |
| `NAO_ENCONTRADO` (404) | Fatura ou conta inexistente, ou de outro ambiente |

## `POST .../faturas/{faturaId}/fechamento` e `POST .../faturas/{faturaId}/abertura`

**Contingência, não fluxo normal:** o banco fechou em dia diferente, a rotina não rodou quando
devia (`docs/02-dominio/fatura-cartao.md`). Os dois são **sub-recursos**, porque verbo em
caminho é proibido (`docs/04-api/convencoes.md`), e os dois devolvem o cartão inteiro.

**Fechar** faz as mesmas duas coisas do ciclo — a `ABERTA` vira `FECHADA` e a seguinte abre,
criada na hora se não existir — e grava `FATURA_FECHADA_PELO_USUARIO`, que é o par de
`FATURA_FECHADA` com o autor certo (`docs/02-dominio/evento.md`).

**Abrir** serve para uma coisa só: *o ciclo ainda está correndo e o sistema achou que tinha
acabado*. Vale para a **última fechada**, e só enquanto o `a pagar` dela for maior que zero. A
seguinte volta a `FUTURA` na hora — deixa de receber compra nova, e nada mais: **o que já estava
dentro dela fica**, parcelas e pagamentos agendados inclusive.

| Erro | Quando |
|---|---|
| `FATURA_FORA_DO_CICLO` (409) | Fechar o que não é `ABERTA` |
| `FATURA_NAO_ABRE` (409) | Não é a última fechada, ou já encerrou. **Fatura encerrada não abre** — é o que impede a rolagem de rolar para si mesma, para sempre |
| `NAO_ENCONTRADO` (404) | Fatura inexistente ou de outro ambiente |

## `GET /api/v1/ambientes/{ambienteId}/faturas/{faturaId}/lancamentos`

O que a fatura cobra, **agrupado por cartão do contrato**. Um contrato tem vários cartões e a
fatura é uma só; a pergunta *"quanto cada cartão gastou"* só tem resposta separando
(`docs/06-interface/fatura.md`).

```
GET /api/v1/ambientes/1/faturas/42/lancamentos

200 OK
{
  "totalCentavos": 327728,
  "cartoes": [
    { "meioId": 7, "nome": "FÍSICO ****1234", "totalCentavos": 287728,
      "itens": [
        { "id": 51, "dataEvento": "2026-09-18", "descricao": "Aulas de espanhol",
          "sentido": "SAIDA", "valorCentavos": 166668, "situacao": "PROVISIONADO",
          "doCiclo": false, "categoria": "Educação", "parcela": 1, "parcelas": 3 }
      ] },
    { "meioId": 9, "nome": "FREELANCE ****0987", "totalCentavos": 40000, "itens": [ ... ] }
  ],
  "saldoAnterior": { "id": 88, "descricao": "Saldo da fatura anterior", ... },
  "roladoParaASeguinte": { "id": 91, "descricao": "Rolado para a fatura seguinte", ... }
}
```

**Quem agrupa é o servidor**, e não a tela: *quais cartões pertencem a este contrato* é fato do
domínio. **Cartão sem lançamento não vira grupo vazio** — grupo sem linha não é grupo.

| Campo | Nota |
|---|---|
| `totalCentavos` | O mesmo total da fatura, e os subtotais dos grupos somam ele **mais** o saldo anterior |
| `saldoAnterior` | O **débito** de rolagem que esta fatura recebeu. **Fora dos grupos**, porque não é de cartão nenhum: é dívida que mudou de período. **Ausente** quando não houve |
| `roladoParaASeguinte` | O **crédito** de rolagem — a saída da dívida desta fatura para a próxima. Já está **fora do total**, e é por isso que o total histórico não cai. **Ausente** quando não houve |
| `categoria` | O nome resolvido, como `GET /lancamentos/{id}` faz. **Ausente** quando o lançamento está pendente |
| `parcela` · `parcelas` | *Qual de quantas*, como o emissor imprime. **Ausentes** fora de um parcelamento |

**Sem cursor**, e é a mesma razão do Diário: a fatura é um recorte **fechado** — a pergunta é
sobre um período, e o período acabou.

| Erro | Quando |
|---|---|
| `NAO_ENCONTRADO` (404) | Fatura inexistente ou de outro ambiente |

## O ciclo roda sozinho, e não tem endpoint

Fechar, abrir a seguinte, encerrar as quitadas e rolar as vencidas é **rotina**, não requisição:
ela roda uma vez por dia, é idempotente e **recupera atraso em ordem cronológica** — se o
Raspberry Pi ficou dois ciclos desligado, janeiro rola para a fatura que estava aberta quando
janeiro venceu, e só então fevereiro fecha, vence e rola
(`docs/02-dominio/fatura-cartao.md`, `docs/02-dominio/fatura-pagamento.md`).

O que a borda mostra disso é o **Diário**: cada passo que de fato acontece grava
`FATURA_FECHADA`, `FATURA_ABERTA_PELO_CICLO`, `FATURA_ROLADA` e `FATURA_ENCERRADA`, com a fatura
como alvo (`docs/04-api/endpoints-eventos.md`). **Rodada que não fecha nada não grava nada.**

## Compra no crédito

**Não há endpoint de compra aqui.** Comprar no cartão é `POST /lancamentos` com um `meioId` de
um meio `CREDITO` (`docs/04-api/endpoints-lancamentos.md`) — o meio é que decide que aquele
lançamento tem fatura, e a fatura de destino sai do **status**, nunca da data.

O lançamento nasce `PROVISIONADO`, com `dataEfeito = dataEvento` e `faturaId` da `ABERTA`
daquele cartão. Mover um lançamento de fatura é `PATCH /lancamentos/{id}` com `faturaId`, para
**qualquer** fatura daquele cartão, aberta ou não: fatura fechada não congela nada.

**Compra parcelada é `POST /parcelamentos`** (`docs/04-api/endpoints-series.md`): a 1ª parcela na
`ABERTA` e a parcela *k* na *k*-ésima fatura a partir dela, criada como `FUTURA` na hora. É o
único caminho pelo qual uma fatura `FUTURA` ganha conteúdo — e a razão de ela existir.

## O que ainda não existe

- **Antecipar** o pagamento de uma fatura `ABERTA`, que é outra mecânica, com desconto do
  emissor — Fase 2 (`docs/00-produto/roadmap.md`).
- **Papel**: nenhum endpoint desta página verifica se o usuário é dono, editor ou leitor. A
  verificação entra com o convite (`docs/02-dominio/ambiente-financeiro.md`).
