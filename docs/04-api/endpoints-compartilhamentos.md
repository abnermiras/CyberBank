---
id: 04-api/endpoints-compartilhamentos
titulo: Endpoints de compartilhamento
dono: contrato dos endpoints que compartilham e revogam o uso de uma conta com outro ambiente
ler-junto: [02-dominio/compartilhamento, 04-api/convencoes]
status: ativo
---

# Endpoints de compartilhamento

Família do ambiente: vive sob `/api/v1/ambientes/{ambienteId}/contas/{contaId}/`, e o
`{ambienteId}` é o **de origem** — o ambiente dono da conta. A regra é de
`docs/02-dominio/compartilhamento.md`. Como a conta compartilhada aparece no **destino** —
`compartilhadaDe` na lista e o extrato completo por `contaId` — está em
`docs/04-api/endpoints-contas.md` e `docs/04-api/endpoints-lancamentos.md`.

Sub-recurso da conta, sem verbo no caminho (`docs/04-api/convencoes.md`). Só o **dono** do
ambiente de origem cria e revoga, e a conta tem de ser **dele**.

Sub-recurso da conta (`docs/02-dominio/compartilhamento.md`). Só o **dono** do ambiente de origem
cria e revoga, e a conta tem de ser **dele**.

| Método e caminho | O que faz |
|---|---|
| `GET .../contas/{contaId}/compartilhamentos` | Com quem a conta está dividida: `{ "itens": [ { "ambienteDestinoId", "ambienteDestinoNome", "criadoEm" } ] }` |
| `POST .../contas/{contaId}/compartilhamentos` | Corpo `{ "ambienteDestinoId": 9 }`. `201`, com o mesmo formato de um item |
| `DELETE .../contas/{contaId}/compartilhamentos/{ambienteDestinoId}` | `204`. **Revogar não apaga, move nem recategoriza lançamento nenhum** |

| Erro | Quando |
|---|---|
| `SEM_PERMISSAO` (403) | O papel no ambiente de origem não é `DONO` |
| `NAO_ENCONTRADO` (404) | Conta inexistente, **de outro ambiente ou emprestada** (quem recebeu não repassa), ou vínculo inexistente ao revogar |
| `CONTA_CARTAO_NAO_SE_COMPARTILHA` (409) | Conta `CARTAO` |
| `COMPARTILHAMENTO_JA_EXISTE` (409) | Já há vínculo daquela conta para aquele destino |
| `AMBIENTE_DESTINO_INVALIDO` (422) | O destino é o próprio ambiente de origem, ou não é um ambiente do usuário |
| `VALIDACAO` (422) | `ambienteDestinoId` ausente |

**Criar e revogar gravam evento** no ambiente de origem (`VINCULO_CRIADO`, `VINCULO_REVOGADO`).
