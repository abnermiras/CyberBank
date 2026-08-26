---
id: 00-produto/glossario
titulo: Glossário de domínio
dono: o significado canônico de cada termo do domínio
ler-junto: [00-produto/visao]
status: rascunho
---

# Glossário de domínio

Linguagem ubíqua do Cyberbank. **Este doc manda:** o termo usado aqui é o termo usado
no código, no banco, na API e nas mensagens do bot. Se você precisou de um sinônimo,
ou o termo está errado, ou falta um termo.

As definições abaixo são **propostas** derivadas do RaspyBank — confirme ou reescreva
cada uma. Enquanto `status: rascunho`, trate como sugestão, não como verdade.

| Termo | Definição proposta | ✔ |
|---|---|:--:|
| **Lançamento** | Um evento financeiro individual que altera o saldo de uma conta. Unidade central do sistema. | ☐ |
| **Captura** | O ato de o sistema obter um lançamento automaticamente, sem digitação (notificação push, OFX, voz). | ☐ |
| **Pendência** | Lançamento capturado que ainda não tem categoria confirmada pelo usuário. | ☐ |
| **Categorização** | Atribuir categoria a um lançamento. Única etapa que o sistema aceita exigir do usuário. | ☐ |
| **Conta** | Onde o dinheiro está ou de onde ele sai: conta corrente, carteira, poupança. | ☐ |
| **Meio de pagamento** | *Como* a compra foi paga (débito, crédito, Pix, VR). Distinto de conta — ver `02-dominio/meio-de-pagamento`. | ☐ |
| **Categoria** | Classificação hierárquica do propósito do gasto (ex.: transporte → gasolina). | ☐ |
| **Fatura** | Agrupamento de lançamentos de crédito em um ciclo, com fechamento e vencimento. | ☐ |
| **Conciliação** | Casar um lançamento capturado com o registro oficial do banco, sem duplicar. | ☐ |
| **Recorrência** | Série de lançamentos previstos que se repetem por regra de tempo. | ☐ |
| **Parcelamento** | Compra única dividida em N lançamentos futuros. Distinto de recorrência. | ☐ |
| **Orçamento** | Limite de gasto planejado para uma categoria em um período. | ☐ |
| **Saldo** | Resultado dos lançamentos de uma conta até uma data. | ☐ |
| **Estabelecimento** | Contraparte da compra, como veio da fonte externa (texto bruto, antes de normalizar). | ☐ |

## Termos a definir

Estes precisam de nome **antes** do modelo de dados existir:

- [ ] O texto bruto recebido do banco antes de virar lançamento — "notificação"? "evento de captura"?
- [ ] O usuário do sistema, se houver mais de um (você e a família?) — "titular"? "perfil"?
- [ ] A regra que mapeia estabelecimento → categoria — "regra de categorização"? "aprendizado"?
- [ ] O agrupamento de contas de uma mesma instituição, se existir

## Termos proibidos

Palavra ambígua vira modelo ambíguo. Não use:

| Não use | Use |
|---|---|
| "transação" | **lançamento** (transação é termo de banco de dados) |
| "gasto" / "despesa" / "compra" como sinônimos soltos | **lançamento** (o sinal e o tipo são atributos dele) |
| "cartão" para se referir ao meio | **meio de pagamento** |
| "tag" | **categoria** |
| "importar" para captura automática | **capturar** (importar é só arquivo/OFX) |
