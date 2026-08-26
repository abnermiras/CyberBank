---
id: 08-fluxos/novo-endpoint
titulo: "Fluxo: novo endpoint ou mudança de contrato"
dono: roteiro de criação/alteração de endpoint REST
ler-junto: []
status: ativo
---

# Fluxo: novo endpoint ou mudança de contrato

## 1. Contexto a carregar

1. `docs/04-api/convencoes.md` — estilo, paginação, formatos, versionamento
2. `docs/04-api/erros.md` — corpo de erro e catálogo de códigos
3. `docs/04-api/endpoints-<agregado>.md` — o doc do agregado afetado
4. `docs/02-dominio/<agregado>.md` — a regra que o endpoint expõe

**Condicionais:** `docs/01-arquitetura/padroes-de-codigo.md` se for o primeiro endpoint
de um módulo novo · `docs/06-interface/dashboard.md` se o consumidor é o painel.

**Não abra:** integrações, operação, dados (a menos que o endpoint exija campo novo —
aí é `nova-migration.md` primeiro).

## 2. Regras

- O endpoint **não decide nada**: ele traduz HTTP ↔ caso de uso. Zero regra no controller.
- Request e response são **DTOs próprios**, nunca entidade de domínio ou JPA serializada.
- Mudança em endpoint existente é **aditiva** por padrão. Remover ou renomear campo,
  mudar tipo, ou tornar campo obrigatório são **quebras** — exigem decisão explícita
  registrada, não passam de improviso.
- Todo erro previsível tem código no catálogo. Se o erro é novo, ele entra em `erros.md`
  **antes** de existir no código.

## 3. Ordem de trabalho

1. Escrever o bloco do endpoint no doc do agregado — rota, request, response, erros,
   **exemplo literal de payload**.
2. Caso de uso na camada de aplicação, com teste.
3. Controller fino + teste de contrato batendo com o exemplo do doc.

## 4. Pronto quando

- [ ] Bloco no `endpoints-<agregado>.md` com exemplo real de request e response
- [ ] Erros mapeados no catálogo de `erros.md`
- [ ] Teste de contrato passando e conferindo com o exemplo documentado
- [ ] Nenhuma regra de negócio no controller
- [ ] Se houve quebra de compatibilidade: registrada e o consumidor foi ajustado
