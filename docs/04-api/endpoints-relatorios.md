---
id: 04-api/endpoints-relatorios
titulo: Endpoints de relatorios
dono: contrato dos endpoints de agregacao e relatorio
ler-junto: [04-api/convencoes, 06-interface/dashboard, 02-dominio/lancamento]
status: ativo
---

# Endpoints de relatórios

Família do ambiente, sob `/api/v1/ambientes/{ambienteId}/relatorios`. A regra de **o que conta
como gasto** é de `docs/02-dominio/lancamento.md` e `docs/02-dominio/conta.md`; aqui está o
contrato.

| Rota | O que responde |
|---|---|
| `GET .../relatorios/resumo` | Tudo o que a Home mostra de um mês, numa requisição |

**Um endpoint, não seis.** A Home é um cockpit: ela precisa dos números *juntos* e na mesma
foto do banco. Seis requisições dariam seis fotos, e num app de dinheiro duas metades de tela
discordando é pior que a tela demorar.

## `GET /api/v1/ambientes/{ambienteId}/relatorios/resumo`

| Parâmetro | Valor | Para quê |
|---|---|---|
| `mes` | `AAAA-MM` (padrão: o mês corrente) | O mês do relatório. Mês no futuro é aceito: ele mostra o que já está previsto |

```
GET /api/v1/ambientes/1/relatorios/resumo?mes=2026-09

200 OK
{
  "mes": "2026-09",
  "primeiroDia": "2026-09-01",
  "ultimoDia": "2026-09-30",
  "hoje": "2026-09-17",
  "diasAteOFimDoMes": 13,

  "emCaixaCentavos": 1093600,
  "guardadoCentavos": 1342400,
  "patrimonioCentavos": 2524000,
  "sobraAteOFimDoMesCentavos": 830000,
  "aPagarCentavos": 320000,
  "aReceberCentavos": 56400,

  "entrouNoMesCentavos": 1123600,
  "saiuNoMesCentavos": 430000,
  "guardadoNoMesCentavos": 100000,
  "pendencias": 4,

  "gastoPorCategoria": [
    { "categoriaId": 12, "nome": "Moradia", "cor": "AZUL",
      "totalCentavos": 210000, "lancamentos": 3 },
    { "categoriaId": null, "nome": null, "cor": null,
      "totalCentavos": 12000, "lancamentos": 1 }
  ],

  "proximos": [
    { "id": 22, "dataEfeito": "2026-09-20", "descricao": "Aluguel", "sentido": "SAIDA",
      "valorCentavos": 180000, "contaId": 1, "categoriaId": 12 }
  ]
}
```

### Os números

| Campo | Como é |
|---|---|
| `emCaixaCentavos`, `guardadoCentavos`, `patrimonioCentavos` | Os mesmos de `GET /contas`, e pela mesma consulta: eles vêm juntos aqui para a Home não precisar de duas requisições para os quatro números do topo |
| `sobraAteOFimDoMesCentavos` | Saldo **projetado** das contas de caixa até `ultimoDia`: o realizado mais tudo que ainda cai. É `emCaixa + aReceber − aPagar` |
| `aPagarCentavos`, `aReceberCentavos` | Os `PREVISTO` de **hoje até `ultimoDia`**, em conta de caixa. É o que sustenta a aritmética da sobra |
| `entrouNoMesCentavos`, `saiuNoMesCentavos` | O que **aconteceu** no mês (`situacao != PREVISTO`), líquido de estorno, em conta de fluxo de caixa |
| `guardadoNoMesCentavos` | Transferência que **entrou** numa conta fora do fluxo de caixa: o aporte. Não é gasto, e por isso não está em `gastoPorCategoria` |
| `pendencias` | Quantos lançamentos do ambiente estão **sem categoria**. É a contagem inteira, não a da página |
| `gastoPorCategoria` | Uma linha por **raiz**, decrescente pelo total. `categoriaId: null` é o que ainda não tem categoria |
| `proximos` | Até **8** `PREVISTO` de hoje até `ultimoDia`, em ordem de `dataEfeito`. **Sem cursor**: a pergunta é sobre um horizonte, e o horizonte fecha |

**Só conta de caixa, nos quatro primeiros.** O vale-refeição fica fora: aquele saldo só compra
uma coisa (`docs/02-dominio/conta.md`), e somá-lo à sobra faria a tela prometer um dinheiro
que não paga o aluguel.

### As quatro regras do gasto

As mesmas que o protótipo já executava, e que agora valem no servidor:

1. **Quem manda é o sentido da categoria, não o do lançamento.** O estorno herda a categoria do
   original e chega como `ENTRADA` numa categoria de `SAIDA` — e **abate** o mês em que
   aconteceu. Sem isso, quem compra e devolve continua vendo o gasto cheio.
2. **Categoria de sistema não entra.** É um predicado, não uma lista de exceções: saldo de
   abertura, rendimento e pagamento de fatura não são gasto da vida.
3. **Só conta com `entraNoFluxoDeCaixa`.** É o campo que responde *"isso é gasto da vida?"* —
   nunca o tipo da conta (`docs/04-api/endpoints-contas.md`).
4. **Agrupa pela raiz.** Subcategoria soma na mãe; quem quer o detalhe abre o Extrato.

**Lançamento sem categoria entra como `categoriaId: null`** quando é `SAIDA`. O dinheiro saiu:
escondê-lo faria a soma das barras ser menor que o mês, e a tela mentiria por omissão.

**Não há eixo `POR_FATURA` × `POR_COMPRA`**, e a ausência é a regra no lugar certo: o eixo só
existe quando a mesma compra pode cair em dois meses diferentes, e é a fatura que faz isso
acontecer (`docs/02-dominio/fatura-cartao.md`). Enquanto ela não existir, competência é o mês
da `dataEvento`, e um seletor com uma opção só seria enfeite.

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `NAO_ENCONTRADO` (404) | Ambiente inexistente **ou sem acesso** |
| `VALIDACAO` (422) | `mes` fora do formato `AAAA-MM` |

## O que ainda não existe

Relatório por período livre, comparação entre meses, orçamento consumido
(`docs/02-dominio/orcamento.md`, stub), e a parte da fatura — o bloco dela na Home está
reservado, dizendo o que espera (`docs/06-interface/dashboard.md`).
