---
id: 08-fluxos/novo-caso-de-uso
titulo: "Fluxo: funcionalidade nova (caso de uso)"
dono: roteiro de escrever uma funcionalidade nova ponta a ponta
ler-junto: []
status: ativo
---

# Fluxo: funcionalidade nova (caso de uso)

Este é o fluxo mais usado, e existe para uma coisa: **você não precisa procurar nada.** O
endereço do código sai do nome do doc (`ADR-0008`).

## 1. Contexto a carregar

1. `docs/02-dominio/<assunto>.md` — **a regra.** É o único doc que decide alguma coisa aqui
2. `docs/01-arquitetura/padroes-de-codigo.md` — nomes, camadas, o que é proibido

**O código é `src/main/java/br/com/cyberbank/<assunto>/`.** Não varra `src/`: o assunto tem o
nome do doc, e as camadas dentro dele são sempre `dominio`, `aplicacao`, `api`,
`persistencia`.

**Condicionais, e só se o passo aparecer:** `docs/04-api/convencoes.md` e
`docs/04-api/erros.md` se expõe endpoint (ou entre pelo `novo-endpoint.md`) ·
`docs/03-dados/migrations.md` se precisa de coluna nova · `docs/01-arquitetura/visao-geral.md`
se é o primeiro caso de uso de um assunto que ainda não existe.

**Não abra:** integrações, operação, interface, outros assuntos de domínio.

## 2. Regras

- **A regra vive no `dominio`.** Se ela acabou no caso de uso, ela vai vazar para o próximo
  que precisar dela.
- **Se a funcionalidade precisa de dois assuntos, quem junta é a `aplicacao`** — nunca um
  domínio importando o outro (`ADR-0010`). Domínio referencia domínio por `id`.
- **Se a regra precisa de dois pacotes de domínio, é erro de modelagem** (`ADR-0008`). Pare e
  pergunte, não contorne.
- **Se o doc de domínio não responde, ele não decidiu.** Pergunte — não deduza, e não escreva a
  regra no código para "resolver depois". É o que a regra de stub do `CLAUDE.md` manda.
- Erro novo entra em `docs/04-api/erros.md` **antes** de existir no Java.
- Rotina automática é caso de uso como outro qualquer: idempotente, recupera atraso e grava
  evento (`docs/02-dominio/evento.md`).

## 3. Ordem de trabalho

1. **Regra no `dominio`, com teste** — sem Spring, sem banco. Se precisou dos dois, o desenho
   está errado.
2. **Caso de uso na `aplicacao`**, com `@Transactional`, chamando as portas.
3. **Persistência**: porta implementada, e migration antes se houver coluna nova.
4. **API**, se houver: pelo `docs/08-fluxos/novo-endpoint.md`.
5. **Atualizar o doc dono** se alguma regra mudou — a tabela do `CLAUDE.md` diz qual é.

## 4. Pronto quando

- [ ] A regra tem teste que roda sem Spring e sem banco
- [ ] Nenhuma regra ficou no caso de uso, no controlador ou no repositório
- [ ] O teste de fronteira (`ADR-0010`) passa: nenhum import entre pacotes de domínio
- [ ] Todo erro previsível tem código no catálogo
- [ ] Se o passo mexe em dinheiro sozinho, ele grava evento
- [ ] O doc dono está de acordo com o código — ou foi atualizado no mesmo commit
