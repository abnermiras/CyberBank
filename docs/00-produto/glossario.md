---
id: 00-produto/glossario
titulo: Glossário de domínio
dono: o significado canônico de cada termo do domínio e onde vivem as regras de cada um
ler-junto: [00-produto/visao, 02-dominio/ambiente-financeiro]
status: ativo
---

# Glossário de domínio

Linguagem ubíqua do Cyberbank. **Este doc manda:** o termo usado aqui é o termo usado
no código, no banco, na API e nas mensagens do bot. Se você precisou de um sinônimo,
ou o termo está errado, ou falta um termo.

O glossário define **o que a palavra significa**. Quem define **as regras** é a coluna
"Regras em" — e um termo sem doc de regras é lacuna conhecida, não descuido.

## Acesso e estrutura

| Termo | Definição | Regras em |
|---|---|---|
| **Ambiente financeiro** | Espaço de dados criado por um usuário, com suas próprias contas, lançamentos, categorias, orçamentos, aplicações e metas. Pode ser compartilhado. **É o dono de todo dado financeiro.** | `02-dominio/ambiente-financeiro` |
| **Usuário** | Quem se cadastra e faz login. Não possui dado financeiro diretamente — possui *acesso* a ambientes. | `01-arquitetura/seguranca` |
| **Acesso** | O vínculo entre um usuário e um ambiente, carregando um papel. Um usuário tem no máximo um acesso por ambiente. | `02-dominio/ambiente-financeiro` |
| **Papel** | O que um usuário pode fazer dentro de um ambiente. São três: **dono**, **editor** e **leitor**. | `02-dominio/ambiente-financeiro` |
| **Convite** | O pedido de acesso a um ambiente enviado pelo dono a um e-mail, com o papel já definido. Enquanto não aceito, não é acesso e não revela dado. | `02-dominio/ambiente-financeiro` |

## O dinheiro

| Termo | Definição | Regras em |
|---|---|---|
| **Lançamento** | Um evento financeiro individual que altera o saldo de uma conta. Unidade central do sistema. | `02-dominio/lancamento` |
| **Receita** | Lançamento de entrada de dinheiro. Existe como conceito próprio, **não** como despesa com sinal negativo. | `02-dominio/lancamento` |
| **Conta** | Onde o dinheiro está ou de onde ele sai: conta corrente, carteira, poupança. | `02-dominio/conta` |
| **Saldo** | Resultado dos lançamentos de uma conta até uma data. | `02-dominio/conta` |
| **Meio de pagamento** | *Como* a compra foi paga (débito, crédito, Pix, VR). Distinto de conta. | `02-dominio/meio-de-pagamento` |
| **Categoria** | Classificação hierárquica do propósito do gasto (ex.: transporte → gasolina). | `02-dominio/categoria` |
| **Fatura** | Agrupamento de lançamentos de crédito em um ciclo, com fechamento e vencimento. | `02-dominio/fatura-cartao` |
| **Parcelamento** | Compra única dividida em N lançamentos futuros. Distinto de recorrência. | `02-dominio/recorrencia` |
| **Recorrência** | Série de lançamentos previstos que se repetem por regra de tempo. Distinto de parcelamento. | `02-dominio/recorrencia` |
| **Orçamento** | Limite de gasto planejado para uma categoria em um período. | `02-dominio/orcamento` |

## Patrimônio

| Termo | Definição | Regras em |
|---|---|---|
| **Aplicação** | Dinheiro guardado ou investido. **Não é gasto:** sai do fluxo de caixa e entra no patrimônio. | `02-dominio/aplicacao-patrimonio` |
| **Aporte** | Lançamento que move dinheiro de uma conta para uma aplicação. | `02-dominio/aplicacao-patrimonio` |
| **Resgate** | Lançamento que move dinheiro de uma aplicação de volta para uma conta. | `02-dominio/aplicacao-patrimonio` |
| **Patrimônio** | Quanto a pessoa tem, somando contas e aplicações. Distinto de fluxo de caixa. | `02-dominio/aplicacao-patrimonio` |
| **Meta** | Objetivo financeiro com valor-alvo, prazo e progresso (ex.: R$ 20 mil de reserva até dezembro). | ⚠ sem doc — Fase 3 |

## Entrada automática

| Termo | Definição | Regras em |
|---|---|---|
| **Captura** | O ato de o sistema obter um lançamento automaticamente, sem digitação (notificação push, OFX, voz). | `05-integracoes/captura-notificacao` |
| **Notificação de compra** | O texto bruto recebido do banco ou do cartão, antes de virar lançamento. É a matéria-prima da captura, não um lançamento. | `05-integracoes/captura-notificacao` |
| **Pendência** | Lançamento capturado que ainda não tem categoria confirmada pelo usuário. | `02-dominio/lancamento` |
| **Categorização** | Atribuir categoria a um lançamento. Única etapa que o sistema aceita exigir do usuário. | `02-dominio/regras-categorizacao` |
| **Regra de categorização** | O mapeamento que faz um estabelecimento virar categoria automaticamente. | `02-dominio/regras-categorizacao` |
| **Estabelecimento** | Contraparte da compra, como veio da fonte externa: **texto bruto, antes de normalizar**. | `02-dominio/regras-categorizacao` |
| **Conciliação** | Casar um lançamento capturado com o registro oficial do banco, sem duplicar. | `02-dominio/importacao-conciliacao` |

## Sobre "ambiente"

O termo tem dois sentidos no projeto e **os dois ficam**:

- **Ambiente financeiro** — o conceito de domínio acima. No código é o não-marcado:
  `Ambiente`, `ambiente_id`.
- **Ambiente de execução** — dev, homologação, produção. Escrito **sempre por extenso**
  (`01-arquitetura/ambientes-de-execucao`).

Decidido em `ADR-0001`. Escrever "ambiente" sozinho para dev/homologação/produção passou
a ser proibido, em doc, commit, log ou nome de variável.

## Sobre "aplicação"

Mesma armadilha do "ambiente", e ela já estava nos docs: **aplicação** é dinheiro
investido, nunca o software. Para o software, diga **o sistema**.

Sobra um terceiro uso legítimo: a *camada de aplicação* da arquitetura
(`01-arquitetura/visao-geral`). Ele só aparece falando de camada, sempre com a palavra
"camada" junto — se começar a aparecer sozinho, troque o nome da camada para "caso de uso".

## Termos que decidimos não criar

- **Agrupamento de contas por instituição** ("banco", "instituição"). Não existe: a conta
  já carrega de onde ela é. Criar o agrupamento antes de alguém sentir falta é modelo a
  mais para manter.

## Termos proibidos

Palavra ambígua vira modelo ambíguo. Não use:

| Não use | Use |
|---|---|
| "transação" | **lançamento** (transação é termo de banco de dados) |
| "gasto" / "despesa" / "compra" como sinônimos soltos | **lançamento** (o sinal e o tipo são atributos dele) |
| "cartão" para se referir ao meio | **meio de pagamento** |
| "tag" | **categoria** |
| "importar" para captura automática | **capturar** (importar é só arquivo/OFX) |
| "ambiente" para dev/homologação/produção | **ambiente de execução** |
| "investimento" como sinônimo solto | **aplicação** (investir é o ato, aplicação é a coisa) |
| "aplicação" para se referir ao software | **o sistema** (aplicação é dinheiro investido) |
