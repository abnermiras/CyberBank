---
id: 02-dominio/recorrencia
titulo: Recorrencia
dono: lancamentos recorrentes e parcelados: geracao e manutencao
ler-junto: [02-dominio/lancamento, 02-dominio/fatura-cartao]
status: stub
---

# Recorrencia

> **STUB** — conteudo ainda nao escrito. Ao preencher, siga `docs/CONVENTIONS.md`,
> apague este bloco e troque `status: stub` por `status: ativo`.

## Perguntas que este documento precisa responder

- [ ] Diferenca entre recorrente e parcelado no modelo
- [x] Quando as ocorrencias futuras sao geradas: **na criacao**. As N parcelas nascem no
      momento da compra, com `situacao = PREVISTO` (`docs/02-dominio/lancamento.md`)
- [ ] Ate onde gerar uma recorrencia sem fim definido (12 meses? 24?) e quem estende
- [ ] O que acontece ao editar/cancelar uma serie ja iniciada
- [ ] Como uma ocorrencia gerada se casa com o lancamento real capturado

## Conteudo

_(vazio)_
