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
| **Compartilhamento** | Dar **uso** de uma conta ou de um cartão a outro ambiente. O que atravessa é o uso, nunca a posse — e **todo lançamento pertence ao ambiente em que foi feito**. Distinto de acesso ao ambiente e de cartão adicional. | `02-dominio/compartilhamento`, `ADR-0004` |
| **Vínculo** | O elo que liga um objeto compartilhável a um ambiente de destino. No máximo um por destino. Revogar não apaga, não move e não recategoriza lançamento. | `02-dominio/compartilhamento` |
| **Categoria mascarada** | Como a categoria de um lançamento de outro ambiente aparece numa conta ou fatura compartilhada: o lançamento aparece inteiro, a etiqueta não. Não é privacidade, é higiene — as árvores são de cada ambiente. | `02-dominio/compartilhamento` |
| **Parte da fatura** | Quanto de uma fatura pertence a cada ambiente e a cada cartão adicional. **A parte orienta, não trava:** qualquer um paga qualquer valor, de qualquer conta que acesse. | `02-dominio/compartilhamento` |

## O dinheiro

| Termo | Definição | Regras em |
|---|---|---|
| **Lançamento** | Um evento financeiro individual que altera o saldo de uma conta. Unidade central do sistema. | `02-dominio/lancamento` |
| **Receita** | Lançamento de entrada de dinheiro. Existe como conceito próprio, **não** como despesa com sinal negativo. | `02-dominio/lancamento` |
| **Conta** | Onde o dinheiro está. Corrente, carteira, aplicação, benefício — e o **contrato de cartão de crédito**, cujo saldo é a dívida (`ADR-0003`). **Se tem saldo próprio que o sistema acompanha, é conta.** Poupança é uma aplicação, não um tipo à parte. | `02-dominio/conta` |
| **Saldo** | Soma dos lançamentos de uma conta até uma data. Nunca armazenado. Tem duas leituras: **realizado** (só o que aconteceu) e **projetado** (mais os previstos). | `02-dominio/conta` |
| **Em caixa** | Quanto há de dinheiro que serve para pagar **qualquer coisa**: soma do saldo realizado das contas com `entraEmCaixa = true`. Fica de fora o que não é fungível — aplicação, benefício e a dívida do cartão. Distinto de fluxo de caixa e de patrimônio. | `02-dominio/conta` |
| **Meio de pagamento** | *Como* a compra foi paga (débito, crédito, Pix, dinheiro, benefício, boleto). Distinto de conta, e sempre apontando para uma. Só o **boleto** separa as duas datas; em todo o resto, crédito incluído, `dataEfeito = dataEvento`. | `02-dominio/meio-de-pagamento` |
| **Débito automático** | **Não é meio de pagamento.** O meio é débito; "automático" é fato da recorrência, que se paga sem o usuário agir. | `02-dominio/recorrencia` |
| **Transferência** | Movimento de dinheiro entre duas contas acessíveis ao ambiente do lançamento — próprias, ou compartilhadas com ele. São **dois lançamentos** ligados pelo mesmo id, sentidos opostos. Carrega **categoria de sistema** e não é gasto. | `02-dominio/lancamento` |
| **Correção** | Arrumar um registro errado (valor digitado errado, categoria errada). **Edita** o lançamento e guarda o histórico. | `02-dominio/lancamento` |
| **Estorno** | O dinheiro voltou de verdade: compra cancelada, devolução, chargeback. É um **lançamento novo** de sentido oposto, não uma edição. | `02-dominio/lancamento` |
| **Situação** | Em que ponto entre o **fato** e a **liquidação** o lançamento está. Três valores: `PREVISTO` (vai acontecer), `PROVISIONADO` (aconteceu, falta liquidar) e `REALIZADO` (aconteceu e liquidou). Só anda para frente. O teste de "entra no saldo" é `situacao !== PREVISTO`, **nunca** `=== REALIZADO`. | `02-dominio/lancamento`, `ADR-0006` |
| **Abrir fatura** | Devolver a **última fatura fechada** ao estado `ABERTA`, porque o ciclo ainda estava correndo. Não existe estado "reaberta", e corrigir o passado **não** exige abrir fatura: fatura fechada não congela nada. | `02-dominio/fatura-cartao` |
| **Encerrar fatura** | O fim do ciclo de cobrança: a fatura é **quitada**, ou **vence sem ser quitada** e o que faltou rola. É o encerramento que liquida os lançamentos dela — nem o fechamento, nem um pagamento parcial. | `02-dominio/fatura-pagamento` |
| **Rolagem** | O que uma fatura vencida não cobriu vira um **par de lançamentos dentro da própria conta `CARTAO`** — crédito na vencida, débito na aberta — que **soma zero**. Move dívida de período, não cria dívida. | `ADR-0005`, `02-dominio/fatura-pagamento` |
| **Categoria** | Para que serviu o dinheiro. Árvore de **exatamente dois níveis** (transporte → gasolina) e com um `sentido`: categoria de entrada não recebe lançamento de saída. | `02-dominio/categoria` |
| **Categoria de sistema** | Categoria de uso **exclusivo do sistema**, para ele conseguir lançar o que o ciclo produz: transferência, aporte, resgate, pagamento de fatura, rolagem, rendimento e saldo de abertura. Nasce com o ambiente, nunca aparece no seletor do usuário, não se renomeia nem se exclui, e nenhum relatório por categoria a inclui. Sete operações, cada uma em `ENTRADA` e `SAIDA`. | `02-dominio/categoria` |
| **Categoria do usuário** | Todas as outras. O sistema **não cria nenhuma** — "Moradia", "Transporte", "Estudos" nascem com o nome que o usuário escolher. Não existe conjunto inicial. | `02-dominio/categoria` |
| **Categoria inativa** | O *excluir* de quem tem histórico: some da escolha, e o que já foi lançado continua exibindo e somando. Lógico, nunca físico. Raiz inativa **esconde** a árvore sem mexer no campo dos filhos; raiz cujos filhos estão todos inativos **volta a ser escolhível**. Inativar é sempre ato do usuário — nada é inativado nem excluído por prazo ou desuso. | `02-dominio/categoria` |
| **Sentido** | `ENTRADA` ou `SAIDA`. Atributo do lançamento e da categoria — é ele que dá o sinal, nunca o valor. | `02-dominio/lancamento` |
| **Fatura** | **Recorte de um período da conta `CARTAO`**, com fechamento e vencimento. Tem estado salvo (`FUTURA`, `ABERTA`, `FECHADA`); o valor dela é sempre derivado, nunca armazenado. | `02-dominio/fatura-cartao` |
| **Parcelamento** | **Uma compra só**, dividida em N (R$ 5.000 em 10x). Editar altera **todas** as parcelas, sem perguntar: se elas divergem, o dado está errado. Entidade própria, que guarda o valor da compra. | `02-dominio/recorrencia` |
| **Recorrência** | **N eventos independentes** que se repetem por regra de tempo, normalmente sem fim (a Netflix). Editar **pergunta**: só as futuras, ou o passado também? Entidade própria, e **não** um tipo de parcelamento: as duas não compartilham tabela. | `02-dominio/recorrencia` |
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
| **Pendência** | Lançamento **sem categoria**. Não é estado próprio, é a consulta `categoria IS NULL` — e não tem exceção nenhuma, porque tudo que o ciclo cria já nasce com **categoria de sistema**. | `02-dominio/lancamento`, `02-dominio/categoria` |
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
| "fatura reaberta", estado `REABERTA` | **abrir a fatura** (é ação, não estado — e não existe estado de reabertura) |
| `REALIZADO` como sinônimo de "entra no saldo" | **`!== PREVISTO`** (`PROVISIONADO` também entra) |
| valor com sinal negativo | **valor positivo + `sentido`** (ENTRADA ou SAIDA) |
| "contas de fluxo menos as de dívida" | **`entraEmCaixa`** (virou campo quando apareceu o segundo caso: o benefício) |
| "categoria protegida" / "categoria sistêmica" | **categoria de sistema** |
| "conjunto inicial de categorias" | não existe: o sistema só cria as **de sistema** |
