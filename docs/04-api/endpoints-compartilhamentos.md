---
id: 04-api/endpoints-compartilhamentos
titulo: Endpoints de compartilhamento
dono: contrato dos endpoints que compartilham e revogam o uso de uma conta com outro ambiente
ler-junto: [02-dominio/compartilhamento, 04-api/convencoes]
status: ativo
---

# Endpoints de compartilhamento

Família do ambiente: vive sob `/api/v1/ambientes/{ambienteId}/contas/{contaId}/`, e o
`{ambienteId}` é o **de origem** — o ambiente dono da conta —, com uma exceção: o `DELETE` também
aceita o ambiente **de destino**, que é como quem recebeu devolve a conta. A regra é de
`docs/02-dominio/compartilhamento.md`. Como a conta compartilhada aparece no **destino** —
`compartilhadaDe` na lista e o extrato completo por `contaId` — está em
`docs/04-api/endpoints-contas.md` e `docs/04-api/endpoints-lancamentos.md`.

Sub-recurso da conta, sem verbo no caminho (`docs/04-api/convencoes.md`). Só o **dono** do
ambiente de origem cria, e a conta tem de ser **dele**. Revogar é do dono de **qualquer uma das
duas pontas** (`docs/02-dominio/compartilhamento.md`).

| Método e caminho | O que faz |
|---|---|
| `GET .../contas/{contaId}/compartilhamentos` | Com quem a conta está dividida: `{ "itens": [ { "ambienteDestinoId", "ambienteDestinoNome", "criadoEm" } ] }` |
| `POST .../contas/{contaId}/compartilhamentos` | Corpo `{ "ambienteDestinoId": 9 }`. `201`, com o mesmo formato de um item |
| `DELETE .../contas/{contaId}/compartilhamentos/{ambienteDestinoId}` | `204`. **Revogar não apaga, move nem recategoriza lançamento nenhum**. Com `{ambienteId}` igual ao `{ambienteDestinoId}`, é o destino **devolvendo**: `DELETE /ambientes/9/contas/4/compartilhamentos/9` |

| Erro | Quando |
|---|---|
| `SEM_PERMISSAO` (403) | O papel no ambiente do caminho não é `DONO` — na origem para criar ou revogar, no destino para devolver |
| `NAO_ENCONTRADO` (404) | Conta inexistente, **de outro ambiente ou emprestada** (quem recebeu não repassa), ou vínculo inexistente ao revogar |
| `CONTA_CARTAO_NAO_SE_COMPARTILHA` (409) | Conta `CARTAO` |
| `COMPARTILHAMENTO_JA_EXISTE` (409) | Já há vínculo daquela conta para aquele destino |
| `AMBIENTE_DESTINO_INVALIDO` (422) | O destino é o próprio ambiente de origem, ou é um ambiente em que o usuário **não altera o dado** — sem acesso, ou com somente leitura |
| `VALIDACAO` (422) | `ambienteDestinoId` ausente |

**Criar e revogar gravam evento** no ambiente **do caminho** — o de quem agiu (`VINCULO_CRIADO`,
`VINCULO_REVOGADO`). A devolução leva `devolvida` e `origem` em `dados`.
