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
| **Ambiente financeiro** | Espaço de dados criado por um usuário, com suas próprias contas, lançamentos, categorias, orçamentos e metas. Pode ser compartilhado com outros usuários. **É o dono de todo dado financeiro.** | ☐ |
| **Usuário** | Quem se cadastra e faz login. Não possui dado financeiro diretamente — possui *acesso* a ambientes. | ☐ |
| **Acesso** | O vínculo entre um usuário e um ambiente, com um papel que define o que ele pode fazer ali. | ☐ |
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
| **Receita** | Lançamento de entrada de dinheiro. Precisa existir como conceito, não como despesa negativa. | ☐ |
| **Aplicação** | Dinheiro guardado ou investido. Não é gasto: sai do fluxo de caixa e entra no patrimônio. | ☐ |
| **Patrimônio** | Quanto a pessoa tem, somando contas e aplicações. Distinto de fluxo de caixa. | ☐ |
| **Meta** | Objetivo financeiro com valor-alvo, prazo e progresso (ex.: R$ 20 mil de reserva até dezembro). | ☐ |
| **Estabelecimento** | Contraparte da compra, como veio da fonte externa (texto bruto, antes de normalizar). | ☐ |

## Termos a definir

Estes precisam de nome **antes** do modelo de dados existir:

- [ ] Os **papéis** dentro de um ambiente — "dono / editor / leitor"? outra coisa?
- [ ] O convite de um usuário para um ambiente — "convite"? "compartilhamento"?
- [ ] O texto bruto recebido do banco antes de virar lançamento — "notificação"? "evento de captura"?
- [ ] A regra que mapeia estabelecimento → categoria — "regra de categorização"? "aprendizado"?
- [ ] O agrupamento de contas de uma mesma instituição, se existir

## ⚠ Colisão de nomes a resolver

**"Ambiente"** está sendo usado com dois sentidos diferentes:

1. **Ambiente financeiro** — o conceito de domínio acima (espaço de dados do usuário)
2. **Ambiente de execução** — dev, homologação, produção (`docs/01-arquitetura/ambientes-de-execucao.md`)

Duas coisas com o mesmo nome geram bug e conversa confusa. Escolha uma saída:

- [ ] Renomear o conceito de domínio (Espaço? Carteira? Workspace? Cofre?)
- [ ] Manter "ambiente financeiro" e **sempre** dizer "ambiente de execução" para o outro
      (já aplicado no nome do doc de arquitetura)

## Termos proibidos

Palavra ambígua vira modelo ambíguo. Não use:

| Não use | Use |
|---|---|
| "transação" | **lançamento** (transação é termo de banco de dados) |
| "gasto" / "despesa" / "compra" como sinônimos soltos | **lançamento** (o sinal e o tipo são atributos dele) |
| "cartão" para se referir ao meio | **meio de pagamento** |
| "tag" | **categoria** |
| "importar" para captura automática | **capturar** (importar é só arquivo/OFX) |