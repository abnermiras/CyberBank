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
      "aPagarCentavos": 356080, "encerrada": false, "rolada": false }
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
| `aPagarCentavos` | `total − pago − rolado`. **Negativo é crédito** no cartão, e isso existe na vida real |
| `encerrada` | `aPagar <= 0`. **É leitura, não estado salvo** — não existe "encerrada" como carimbo; existe o número (`docs/02-dominio/fatura-pagamento.md`) |
| `rolada` | Venceu sem ser quitada e o que faltava rolou |

**`aPagarCentavos` maior que zero numa `FECHADA` é a janela** em que a fatura pode ser **paga**
e pode ser **aberta**: um número, duas operações (`docs/02-dominio/fatura-cartao.md`).

| Erro | Quando |
|---|---|
| `CONTA_NAO_E_CARTAO` (409) | A conta existe e não é `CARTAO`. As outras não têm ciclo nenhum para recortar |
| `NAO_ENCONTRADO` (404) | Conta inexistente ou de outro ambiente |

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

## O que ainda não existe

- **`POST .../faturas/{id}/pagamentos`** — pagar é uma transferência da conta pagadora para a
  conta `CARTAO`, apontando para a fatura. Só a `FECHADA` que ainda tem `a pagar` recebe.
- **`POST .../faturas/{id}/fechamento` e `POST .../faturas/{id}/abertura`** — fechar e abrir à
  mão, que são **contingência** e não fluxo normal: o banco fechou em dia diferente, a rotina
  não rodou quando devia. Abrir vale só para a última fechada que ainda deve.
- **Parcelamento** — `POST /lancamentos` ainda não divide uma compra em N.
- **Papel**: nenhum endpoint desta página verifica se o usuário é dono, editor ou leitor. A
  verificação entra com o convite (`docs/02-dominio/ambiente-financeiro.md`).
