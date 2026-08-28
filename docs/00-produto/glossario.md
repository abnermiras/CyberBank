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
| **Conta** | Onde o dinheiro está. Corrente, carteira, aplicação, benefício. **Se tem saldo próprio que o sistema acompanha, é conta.** Poupança é uma aplicação, não um tipo à parte. | `02-dominio/conta` |
| **Saldo** | Soma dos lançamentos de uma conta até uma data. Nunca armazenado. Tem duas leituras: **realizado** (só o que aconteceu) e **projetado** (mais os previstos). | `02-dominio/conta` |
| **Meio de pagamento** | *Como* a compra foi paga (débito, crédito, Pix, dinheiro, benefício, boleto). Distinto de conta, e sempre apontando para uma. É ele que decide quando o dinheiro sai. | `02-dominio/meio-de-pagamento` |
| **Débito automático** | **Não é meio de pagamento.** O meio é débito; "automático" é fato da recorrência, que se paga sem o usuário agir. | `02-dominio/recorrencia` |
| **Transferência** | Movimento de dinheiro entre duas contas do mesmo ambiente. São **dois lançamentos** ligados pelo mesmo id, sentidos opostos. Não tem categoria e não é gasto. | `02-dominio/lancamento` |
| **Correção** | Arrumar um registro errado (valor digitado errado, categoria errada). **Edita** o lançamento e guarda o histórico. | `02-dominio/lancamento` |
| **Estorno** | O dinheiro voltou de verdade: compra cancelada, devolução, chargeback. É um **lançamento novo** de sentido oposto, não uma edição. | `02-dominio/lancamento` |
| **Previsto / Realizado** | A situação de um lançamento: já aconteceu, ou vai acontecer (parcela futura, recorrência futura). | `02-dominio/lancamento` |
| **Reabertura de fatura** | Destravar uma fatura já fechada para corrigir lançamento: reabre, edita, recalcula, ajusta o pagamento se houver, fecha de novo. | `02-dominio/fatura-cartao` |
| **Categoria** | Para que serviu o dinheiro. Árvore de **exatamente dois níveis** (transporte → gasolina) e com um `sentido`: categoria de entrada não recebe lançamento de saída. | `02-dominio/categoria` |
| **Sentido** | `ENTRADA` ou `SAIDA`. Atributo do lançamento e da categoria — é ele que dá o sinal, nunca o valor. | `02-dominio/lancamento` |
| **Fatura** | Agrupamento de lançamentos de crédito em um ciclo, com fechamento e vencimento. | `02-dominio/fatura-cartao` |
| **Parcelamento** | Compra única dividida em N lançamentos futuros. Distinto de recorrência. | `02-dominio/recorrencia` |
| **Recorrência** | Série de lançamentos previstos que se repetem por regra de tempo. Distinto de parcelamento. | `02-dominio/recorrencia` |
| **Orçamento** | Limite de gasto planejado para uma categoria em um período. | `02-dominio/orcamento` |

## Patrimônio

| Termo | Definição | Regras em |
|---|---|---|
| **Aplicação** | Dinheiro guardado ou investido. É uma **conta** de tipo `APLICACAO`, fora do fluxo de caixa. Não se paga com ela: para gastar, resgata-se antes. | `02-dominio/aplicacao-patrimonio` |
| **Aporte** | Transferência de uma conta para uma aplicação. Não é gasto. | `02-dominio/aplicacao-patrimonio` |
| **Resgate** | Transferência de uma aplicação de volta para uma conta. | `02-dominio/aplicacao-patrimonio` |
| **Rendimento** | O quanto uma aplicação valorizou. Nasce quando o usuário informa o valor atual: o sistema lança a diferença. Não é receita da vida. | `02-dominio/aplicacao-patrimonio` |
| **Patrimônio** | Quanto a pessoa tem: soma do saldo de todas as contas do ambiente. Distinto de fluxo de caixa — aporte muda o fluxo do mês e **não** muda o patrimônio. | `02-dominio/aplicacao-patrimonio` |
| **Meta** | Objetivo financeiro com valor-alvo, prazo e progresso (ex.: R$ 20 mil de reserva até dezembro). | ⚠ sem doc — Fase 3 |

## Entrada automática

| Termo | Definição | Regras em |
|---|---|---|
| **Captura** | O ato de o sistema obter um lançamento automaticamente, sem digitação (notificação push, OFX, voz). | `05-integracoes/captura-notificacao` |
| **Notificação de compra** | O texto bruto recebido do banco ou do cartão, antes de virar lançamento. É a matéria-prima da captura, não um lançamento. | `05-integracoes/captura-notificacao` |
| **Pendência** | Lançamento **que espera categoria** e ainda não tem. Não é estado próprio, é consulta — e exclui o que nunca terá categoria (transferência, rendimento, ajuste, abertura). | `02-dominio/lancamento` |
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
| "débito automático" como meio de pagamento | **débito** + recorrência automática |
| "importar" para captura automática | **capturar** (importar é só arquivo/OFX) |
| "ambiente" para dev/homologação/produção | **ambiente de execução** |
| "investimento" como sinônimo solto | **aplicação** (investir é o ato, aplicação é a coisa) |
| "aplicação" para se referir ao software | **o sistema** (aplicação é dinheiro investido) |
| "estorno" para corrigir digitação errada | **correção** (estorno é o dinheiro voltando de verdade) |
| valor com sinal negativo | **valor positivo + `sentido`** (ENTRADA ou SAIDA) |
