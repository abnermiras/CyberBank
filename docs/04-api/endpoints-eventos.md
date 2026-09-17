---
id: 04-api/endpoints-eventos
titulo: Endpoints de eventos
dono: contrato do Diario — os eventos de um dia de um ambiente
ler-junto: [02-dominio/evento, 04-api/convencoes]
status: ativo
---

# Endpoints de eventos

Família do ambiente: tudo aqui vive sob `/api/v1/ambientes/{ambienteId}/`, porque evento tem
`ambiente_id` (`docs/04-api/convencoes.md`). A regra é de `docs/02-dominio/evento.md`, e a tela
que consome isto é de `docs/06-interface/navegacao.md`.

**O recurso é `eventos`; o Diário é o que a tela chama o conjunto dos eventos de um dia.** A
API não inventa vocabulário na borda: o dia é um **filtro**, com o nome do campo, como manda a
convenção.

## `GET /api/v1/ambientes/{ambienteId}/eventos`

O Diário de um dia. **Mais novos primeiro**, pelo `instante` — que é o critério de ordem dentro
do dia, e a razão de `instante` existir ao lado de `dia`.

| Parâmetro | Valor | Para quê |
|---|---|---|
| `dia` | `AAAA-MM-DD` (padrão: **hoje**) | O dia do Diário. Dia local de Brasília, como toda data de domínio |

**Não é paginado, e é a única lista do sistema que não é.** A pergunta é sobre **um dia**, e um
dia tem dezenas de eventos, não milhares — o cursor existe para lista que cresce por cima
(`docs/04-api/convencoes.md`), e esta não cresce: ela fecha quando o dia acaba.

```
GET /api/v1/ambientes/1/eventos?dia=2026-09-17

200 OK
{
  "dia": "2026-09-17",
  "itens": [
    { "id": 31, "instante": "2026-09-17T12:41:08Z", "origem": "USUARIO",
      "autorId": 1, "autor": "Abner", "tipo": "LANCAMENTO_CRIADO",
      "alvo": { "tipo": "LANCAMENTO", "id": 88 },
      "dados": { "descricao": "Feira", "valor": 30000, "sentido": "SAIDA",
                 "contaId": 3, "situacao": "REALIZADO" } },
    { "id": 30, "instante": "2026-09-17T03:05:11Z", "origem": "SISTEMA",
      "autorId": 1, "autor": "Abner", "tipo": "LANCAMENTO_REALIZADO",
      "alvo": { "tipo": "LANCAMENTO", "id": 87 },
      "dados": { "descricao": "Conta de luz", "valor": 19900, "sentido": "SAIDA",
                 "contaId": 3, "dataEfeito": "2026-09-16" } }
  ]
}
```

| Campo | Nota |
|---|---|
| `dia` | Ecoado na resposta. A tela pediu um dia e precisa saber **qual** veio, porque o padrão é hoje e "hoje" muda |
| `instante` | UTC, o único lugar com hora. É por ele que a lista ordena |
| `origem` | `SISTEMA` ou `USUARIO`. A tela separa as duas em seções, e a do sistema vem primeiro |
| `autorId` · `autor` | Quem fez. **Sempre presentes**, inclusive no evento de sistema — nele o autor é o dono do ambiente. `autor` é o nome, que é o que a tela mostra |
| `alvo` | Ausente quando o evento não aponta para nada. Quando vem, é `{ "tipo": ..., "id": ... }` e é o que faz a linha virar link |
| `dados` | **Ausente quando não há nada a dizer além do tipo.** O conteúdo varia por tipo e é o que a frase da tela consome — em `LANCAMENTO_EXCLUIDO` ele carrega a linha inteira, porque o alvo não existe mais |

**A resposta não traz a frase pronta**, e é a mesma decisão do `evento.md`: frase guardada
congela na redação do dia em que foi escrita, e traduzir o app viraria reescrever o passado.
`tipo` + `dados` + `alvo` bastam, e quem monta o texto é a tela.

**Dia sem evento devolve `200` com `itens` vazio** — não `404`. Dia sem movimento é uma
**resposta**, e a tela diz isso; `404` faria a tela parecer quebrada num dia tranquilo.

| Erro | Quando |
|---|---|
| `NAO_AUTENTICADO` (401) | Sem sessão |
| `NAO_ENCONTRADO` (404) | Ambiente inexistente **ou sem acesso** — a mesma resposta para os dois |
| `VALIDACAO` (422) | `dia` no futuro, ou fora do formato `AAAA-MM-DD` |

**Por que o futuro é `422` e não uma lista vazia:** lista vazia significa *"nada aconteceu
nesse dia"*, e amanhã não é um dia em que nada aconteceu — é um dia que não aconteceu. O que
está por vir é o **previsto**, e ele se pergunta ao Extrato. Responder vazio confundiria as
duas perguntas em silêncio, que é o erro que o `evento.md` nomeia.

## O que ainda não existe

- **Filtro por tipo ou por origem.** A tela mostra o dia inteiro e separa na leitura; filtrar
  antes de existir alguém que precise disso é inventar contrato.
- **Os eventos de um alvo** (*"o histórico deste lançamento"*), que o índice `ix_evento_alvo` já
  sustenta e nenhuma tela ainda pede.
- **Um intervalo de dias.** A pergunta do Diário é sobre um dia; período é relatório, e o dono
  é outro (`docs/04-api/endpoints-relatorios.md`).
