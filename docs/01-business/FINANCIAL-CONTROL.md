# Controle Financeiro

## 1. Movimentação Financeira

A movimentação financeira representa o registro de uma entrada ou saída
de dinheiro.

Toda movimentação deve identificar:

- tipo da movimentação: entrada ou saída;
- valor;
- descrição;
- data de lançamento;
- data de efetivação;
- conta bancária;
- forma de pagamento ou recebimento;
- categoria;
- subcategoria, quando aplicável;
- responsável pelo lançamento.

### 1.1 Entrada

Uma entrada representa dinheiro recebido pelo usuário.

A entrada deve identificar:

- conta na qual o dinheiro será recebido;
- forma de recebimento habilitada na conta;
- valor recebido;
- descrição;
- data de lançamento;
- data de efetivação;
- categoria;
- subcategoria, opcional;
- responsável pelo lançamento.

### 1.2 Saída

Uma saída representa dinheiro utilizado ou pago pelo usuário.

A saída deve identificar:

- conta na qual o dinheiro será utilizado;
- forma de pagamento habilitada na conta;
- valor;
- descrição;
- data de lançamento;
- data de efetivação;
- categoria;
- subcategoria, opcional;
- responsável pelo lançamento.

### 1.3 Regras do Valor

O usuário informa o valor da movimentação sempre como um valor positivo.

O tipo da movimentação determina se o valor representa entrada ou saída.

O sistema aplica internamente o efeito de crédito ou débito conforme o
tipo da movimentação.

O valor pode ser informado com ou sem separador de milhar, utilizando
vírgula como separador decimal.

Exemplos válidos:

- `1.521,10`;
- `1521,10`.

### 1.4 Categoria e Subcategoria

A categoria é obrigatória em movimentações normais.

A categoria deve ser compatível com o tipo da movimentação:

- categoria `Entrada` para movimentação de entrada;
- categoria `Saída` para movimentação de saída.

A subcategoria é opcional, mesmo quando a categoria possui subcategorias
cadastradas.

### 1.5 Forma de Pagamento e Recebimento

A conta bancária deve ser selecionada para determinar onde a movimentação
será efetivada.

Para uma saída, o usuário deve selecionar uma forma de pagamento
previamente habilitada na conta selecionada.

Para uma entrada, o usuário deve selecionar uma forma de recebimento
previamente habilitada na conta selecionada.

O sistema não permite utilizar uma forma que não esteja habilitada na
conta.

Operações sistêmicas possuem regras próprias e não exigem seleção manual
da forma de pagamento ou recebimento. Essas operações incluem:

- Transferência;
- Saque;
- Compra de Moeda.

Nessas operações, o sistema determina automaticamente a forma utilizada.

Para operações sistêmicas que movimentam recursos entre contas ou moedas,
a movimentação de saída utiliza `Débito` e a movimentação de entrada
utiliza `Crédito`.

### 1.6 Ciclo de Vida da Movimentação

Uma movimentação financeira pode ser:

- criada;
- editada;
- excluída.

A exclusão de uma movimentação é considerada o cancelamento da
movimentação.

A exclusão é lógica. O registro histórico da operação permanece
armazenado, mas a movimentação deixa de produzir efeitos financeiros e de
compor os cálculos ativos.

### 1.6.1 Permissões

A criação, edição e exclusão de uma movimentação podem ser realizadas por
qualquer usuário que possua permissão `Controle Total` no ambiente.

Usuários com permissão `Leitura` não podem criar, editar ou excluir
movimentações.

### 1.6.2 Estorno

O estorno é uma operação exclusiva das movimentações relacionadas a
cartões de crédito.

As regras específicas de estorno serão definidas no documento
`CREDIT-CARDS.md`.

## 1.7 Lançamentos Futuros

O CyberBank permite o registro de movimentações com data futura.

Uma movimentação futura pode ser registrada antes da data em que o
evento financeiro ocorrerá.

A movimentação futura permanece registrada e pode ser utilizada em
projeções financeiras, planejamentos e dashboards.

## 1.8 Lançamentos Recorrentes

O CyberBank permite a criação de lançamentos que se repetem ao longo do
tempo.

Um lançamento recorrente pode possuir:

- quantidade determinada de ocorrências;
- data de início e data de término;
- duração indefinida.

A recorrência deve definir a periodicidade de repetição do lançamento.

Cada ocorrência representa um lançamento financeiro individual.

As ocorrências pertencentes a uma mesma recorrência devem permanecer
relacionadas entre si.

### 1.8.1 Alteração de Lançamentos Recorrentes

Uma ocorrência de um lançamento recorrente pode ser alterada
individualmente sem alterar as demais ocorrências da série.

Também é possível alterar a série de lançamentos recorrentes.

Quando uma alteração na série afetar uma ocorrência que já tenha sido
realizada ou paga, o sistema deve alertar o usuário antes de confirmar a
alteração.

O usuário deve ser informado de que a alteração afetará uma ocorrência
financeira que já foi realizada.

### 1.8.2 Exclusão de Lançamentos Recorrentes

Uma ocorrência de um lançamento recorrente pode ser excluída
individualmente.

A série de lançamentos recorrentes pode ser encerrada ou excluída,
conforme a operação realizada pelo usuário.

A exclusão ou alteração de ocorrências já realizadas deve seguir as
mesmas regras de permissão aplicáveis às demais movimentações.

## 1.9 Datas da Movimentação

Uma movimentação financeira possui duas datas distintas:

- **Data de lançamento:** data em que a movimentação foi registrada no CyberBank;
- **Data de efetivação:** data em que o movimento financeiro realmente ocorreu.

A data de lançamento é preenchida automaticamente com a data atual, mas
o usuário pode alterá-la para informar a data em que o lançamento foi
registrado.

A data de efetivação representa o momento em que o dinheiro realmente
entra ou sai do recurso financeiro.

### 1.9.1 Data de Efetivação por Forma de Pagamento

Para formas de pagamento com efeito financeiro direto, como `PIX`, `TED`,
`Débito`, `Saque` e `Desconto em Folha de Pagamento`, a data de efetivação
é inicialmente preenchida com a data atual.

O usuário pode alterar a data de efetivação para representar a data real
em que o movimento financeiro ocorrerá.

Exemplo: uma movimentação registrada no sábado pode possuir a segunda-feira
como data de efetivação quando o débito ocorrer somente nesse dia.

Para movimentações realizadas por cartão de crédito, a data de efetivação
é inicialmente definida como a data de vencimento da fatura correspondente.

O usuário pode alterar a data de efetivação quando necessário.

### 1.9.2 Situação da Movimentação

A situação da movimentação é determinada automaticamente pela comparação
entre a data atual do sistema e a data de efetivação.

Uma movimentação pode possuir as seguintes situações:

- `Previsto` — quando a data de efetivação ainda não foi atingida;
- `Efetivado` — quando a data de efetivação foi atingida.

O usuário não define manualmente a situação da movimentação.

### 1.9.3 Movimentação Prevista

Uma movimentação `Prevista`:

- não altera o saldo atual da conta bancária;
- não altera o patrimônio atual;
- pode ser considerada em projeções financeiras;
- pode ser utilizada em planejamentos e dashboards.

Uma movimentação de cartão de crédito constitui uma exceção quanto ao
limite do cartão: mesmo enquanto `Prevista`, o lançamento consome o limite
do cartão a partir da data de lançamento.

### 1.9.4 Efetivação

Quando a data de efetivação é atingida, a movimentação passa
automaticamente de `Previsto` para `Efetivado`.

Nesse momento:

- a movimentação passa a produzir efeito financeiro;
- o saldo da conta relacionada é atualizado;
- os cálculos patrimoniais passam a considerar a movimentação.

No cartão de crédito, a efetivação corresponde ao pagamento da fatura.
O pagamento reduz o saldo da conta bancária utilizada e libera o limite
que estava comprometido pelo lançamento.

## 1.10 Projeção Financeira

O CyberBank deve permitir a análise da situação financeira considerando
movimentações previstas e efetivadas.

As movimentações previstas podem ser utilizadas para calcular projeções
financeiras futuras.

As projeções podem ser utilizadas por:

- dashboards;
- planejamentos financeiros;
- metas;
- projetos financeiros;
- análises de fluxo de caixa.

Uma projeção não altera os saldos financeiros efetivos.

## 1.11 Saldos e Patrimônio

O CyberBank trabalha com diferentes visões de valor financeiro.

### 1.11.1 Saldo da Conta

O saldo da conta representa o valor financeiro efetivamente disponível
em uma conta.

O saldo considera somente movimentações `Efetivadas`.

Movimentações `Previstas` não alteram o saldo atual da conta.

### 1.11.2 Patrimônio

O patrimônio representa o conjunto de recursos financeiros e ativos
pertencentes ao usuário.

O patrimônio pode incluir:

- contas bancárias;
- dinheiro físico;
- investimentos;
- criptomoedas;
- outros ativos financeiros.

Transferências entre recursos pertencentes ao mesmo patrimônio não
alteram o valor patrimonial total.

### 1.11.3 Saldo Projetado

O saldo projetado representa uma estimativa futura dos recursos
financeiros disponíveis.

O cálculo pode considerar movimentações `Previstas` juntamente com os
saldos atuais.

O saldo projetado não altera os saldos efetivos nem o patrimônio atual.

### 1.11.4 Patrimônio por Ambiente

O patrimônio é calculado individualmente para cada ambiente financeiro.

O patrimônio de um ambiente considera os recursos financeiros aos quais
o ambiente possui acesso.

Uma conta compartilhada entre ambientes continua representando um único
recurso financeiro, mas pode compor o patrimônio de cada ambiente que
possui acesso à conta.

A mesma conta compartilhada não deve ser contabilizada mais de uma vez
dentro do patrimônio de um mesmo ambiente.

#### Exemplo

O usuário possui o ambiente `CLT` com:

- Banco1: R$ 100;
- Banco2: R$ 200.

O patrimônio do ambiente `CLT` é de R$ 300.

O Banco2 é compartilhado com o ambiente `PJ`.

O patrimônio do ambiente `PJ` é de R$ 200.

A conta Banco2 continua sendo uma única conta, com saldo de R$ 200,
mesmo estando disponível nos dois ambientes.

### 1.11.5 Patrimônio por Moeda

O patrimônio de um ambiente financeiro é apresentado separadamente por
moeda.

O CyberBank não realiza conversão automática entre moedas para composição
do patrimônio.

Cada moeda possui seu próprio valor patrimonial.

Por exemplo:

- BRL: R$ 500,00;
- USD: US$ 100,00;
- EUR: € 50,00.

Os valores permanecem separados e não são convertidos para uma moeda
única.

## 1.12 Ambiente de Origem da Movimentação

Toda movimentação financeira possui um ambiente financeiro de origem.

O ambiente de origem é o ambiente no qual o usuário realizou o
lançamento.

Uma movimentação registrada em uma conta compartilhada permanece
associada ao ambiente no qual foi criada.

---

## 1.13 Extrato e Mapa de Lançamentos

O CyberBank possui duas visões distintas das movimentações:

- Extrato;
- Mapa de Lançamentos.

### 1.13.1 Extrato

O extrato pertence ao recurso financeiro, como uma conta bancária.

Quando uma conta é compartilhada entre ambientes, todos os ambientes
que possuem acesso à conta podem visualizar seu extrato.

O extrato apresenta todas as movimentações realizadas na conta,
independentemente do ambiente em que foram lançadas.

Quando uma movimentação foi realizada em outro ambiente, as informações
específicas daquele lançamento podem não estar disponíveis no ambiente
que está consultando o extrato.

Nesse caso, a categoria e demais informações específicas do lançamento
podem ser apresentadas em branco ou conforme as regras de
compartilhamento.

### 1.13.2 Mapa de Lançamentos

O mapa de lançamentos pertence ao ambiente financeiro.

Ele apresenta somente as movimentações cuja origem é o próprio ambiente.

Uma movimentação realizada em outro ambiente, mesmo utilizando uma conta
compartilhada, não aparece no mapa de lançamentos do ambiente atual.

### 1.13.3 Exemplo

O Banco1 está compartilhado entre os ambientes `CLT` e `PJ`.

No ambiente `CLT` é registrada uma movimentação:

- R$ 10,00;
- Pizza;
- Categoria: Pessoal / Alimentação.

No ambiente `PJ` é registrada outra movimentação:

- R$ 150,00;
- DIRPF;
- Categoria: Empresa / Impostos.

O extrato do Banco1 apresenta as duas movimentações para ambos os
ambientes.

O mapa de lançamentos do ambiente `CLT` apresenta somente a movimentação
de R$ 10,00.

O mapa de lançamentos do ambiente `PJ` apresenta somente a movimentação
de R$ 150,00.

No extrato do ambiente `CLT`, a movimentação de R$ 150,00 continua
visível, porém suas informações específicas de classificação podem não
ser apresentadas.

No extrato do ambiente `PJ`, ocorre o mesmo comportamento para a
movimentação de R$ 10,00.

## 1.14 Transferências entre Contas com Compartilhamento

Uma transferência entre contas somente pode ser registrada quando as
contas de origem e destino estiverem acessíveis dentro do mesmo ambiente
financeiro.

Quando as duas contas são acessíveis pelo mesmo ambiente, a operação é
registrada como uma única transferência composta por duas movimentações:

- uma saída na conta de origem;
- uma entrada na conta de destino.

As duas movimentações utilizam a categoria sistêmica `Transferência` e
permanecem relacionadas.

A movimentação de saída utiliza `Débito` e a movimentação de entrada
utiliza `Crédito`.

### 1.14.1 Contas sem Acesso Comum

Quando duas contas não são acessíveis dentro do mesmo ambiente, não é
possível realizar uma transferência entre elas.

Nesse caso, a movimentação deve ser registrada como duas operações
independentes:

- uma saída na conta de origem, com categoria definida pelo usuário;
- uma entrada na conta de destino, com categoria definida pelo usuário.

Essas movimentações não são consideradas uma transferência sistêmica.

### 1.14.2 Visibilidade da Transferência

Uma transferência pode envolver contas compartilhadas entre diferentes
ambientes.

Cada ambiente visualiza a transferência conforme os recursos aos quais
possui acesso.

Quando um ambiente possui acesso às contas de origem e destino, consegue
visualizar os dois lados da transferência.

Quando possui acesso somente a uma das contas, visualiza apenas a
movimentação correspondente ao recurso ao qual possui acesso.

Nesse caso, não é possível identificar pelo ambiente o recurso de destino
ou origem que não está acessível.

### 1.14.3 Exemplo

O usuário possui:

- ambiente `CLT`;
- ambiente `PJ`;
- Banco1 no `CLT`;
- Banco2 no `CLT`, compartilhado com `PJ`;
- Banco3 no `PJ`.

O Banco1 e o Banco3 não são acessíveis pelo mesmo ambiente.

Portanto, não é possível realizar uma transferência direta entre Banco1
e Banco3.

Já o Banco2 pode realizar uma transferência para Banco1 ou Banco3,
pois o Banco2 é acessível pelo ambiente correspondente à operação.

#### Banco2 → Banco1

A transferência de R$ 100,00 gera:

No ambiente `CLT`:

- Banco2: -R$ 100,00;
- Banco1: +R$ 100,00.

O mapa de lançamentos do `CLT` apresenta os dois lançamentos.

No ambiente `PJ`:

- Banco2: -R$ 100,00.

O mapa de lançamentos do `PJ` apresenta somente a saída, pois o Banco1
não está disponível nesse ambiente.

O ambiente `PJ` não consegue identificar o destino da transferência.

## 1.15 Alteração e Exclusão de Transferências

Uma transferência pode ser editada por um usuário com permissão
`Controle Total`.

A alteração do valor da transferência deve atualizar todas as
movimentações relacionadas à operação.

Os saldos das contas envolvidas devem ser recalculados após a alteração.

### 1.15.1 Exclusão de Transferência

Uma transferência pode ser excluída por um usuário com permissão
`Controle Total`.

A exclusão da transferência deve excluir logicamente todas as
movimentações relacionadas à operação.

Os saldos das contas envolvidas devem ser atualizados após a exclusão.

Não deve existir apenas um dos lados de uma transferência após sua
exclusão.

---

## 1.16 Histórico Financeiro

O histórico financeiro representa os acontecimentos que já ocorreram
dentro do CyberBank.

A alteração de uma conta bancária não deve apagar as movimentações
históricas relacionadas àquela conta.

A perda de acesso de um usuário ou ambiente a uma conta compartilhada não
deve apagar ou alterar as movimentações históricas realizadas enquanto o
acesso existia.

O histórico deve permanecer associado ao recurso financeiro no qual a
movimentação ocorreu.

O acesso às informações históricas deve respeitar as permissões e regras
de compartilhamento vigentes.

---

## 2. Movimentações Sistêmicas

O CyberBank pode gerar movimentações sistêmicas para representar
operações realizadas pelo próprio sistema.

Movimentações sistêmicas podem utilizar categorias de uso exclusivo do
sistema.

As categorias sistêmicas são controladas pelo sistema e não dependem de
categorias criadas pelo usuário.

### 2.1 Transferência entre Contas

Uma transferência entre contas representa a movimentação de dinheiro de
uma conta bancária para outra.

Uma transferência gera duas movimentações financeiras relacionadas:

- uma saída na conta de origem;
- uma entrada na conta de destino.

As duas movimentações representam uma única transferência e utilizam a
categoria sistêmica `Transferência`.

A saída utiliza `Débito` e a entrada utiliza `Crédito`.

#### 2.1.1 Atualização de Saldos

A transferência deve atualizar:

- o saldo da conta de origem;
- o saldo da conta de destino;
- o saldo global do patrimônio.

O valor transferido não representa aumento ou redução do patrimônio
global, pois apenas altera a distribuição do dinheiro entre contas.

### 2.2 Saque

O saque representa a retirada de dinheiro de uma conta bancária para
transformá-lo em dinheiro físico.

Um saque gera duas movimentações financeiras relacionadas:

- uma saída na conta de origem;
- uma entrada na conta de destino.

A conta de destino deve ser uma conta bancária do tipo `Carteira`.

A saída utiliza `Débito` e a entrada utiliza `Crédito`.

#### 2.2.1 Conta Carteira

Uma conta `Carteira` representa dinheiro físico sob controle do usuário.

O usuário pode criar uma ou mais contas do tipo `Carteira`.

A conta `Carteira` pode receber qualquer nome definido pelo usuário,
como:

- Carteira;
- Bolsa;
- Colchão;
- Bolso;
- Cofre.

Uma conta `Carteira` aceita exclusivamente a forma de pagamento e
recebimento `Dinheiro`.

Uma conta `Carteira` não pode utilizar formas de pagamento ou recebimento
diferentes de `Dinheiro`.

#### 2.2.2 Registro do Saque

Ao registrar um saque, o usuário deve informar:

- conta bancária de origem;
- conta `Carteira` de destino;
- valor;
- data;
- responsável pelo lançamento.

O saque gera:

- uma saída na conta de origem;
- uma entrada na conta `Carteira`.

As duas movimentações representam uma única operação de saque.

O saque utiliza uma categoria sistêmica do tipo `Saque`.

#### 2.2.3 Atualização de Saldos

O saque deve atualizar:

- o saldo da conta de origem;
- o saldo da conta `Carteira`;
- o saldo global do patrimônio.

O saque não altera o patrimônio global do usuário, pois apenas transforma
a forma como o dinheiro está armazenado.

### 2.3 Compra de Moeda

A compra de moeda representa a conversão de um valor de uma moeda de
origem para outra moeda dentro da mesma conta.

A operação gera duas movimentações relacionadas:

- uma saída na moeda de origem;
- uma entrada na moeda de destino.

A saída utiliza `Débito` e a entrada utiliza `Crédito`.

A operação utiliza a categoria sistêmica `Compra de Moeda`.

A compra de moeda deve registrar o valor utilizado na moeda de origem e o
valor recebido na moeda de destino.

A compra de moeda não exige que o sistema determine ou registre uma
cotação para composição do patrimônio.

## 3. Extratos

As movimentações financeiras devem estar disponíveis no extrato da conta
bancária relacionada.

Uma transferência deve aparecer:

- como saída no extrato da conta de origem;
- como entrada no extrato da conta de destino.

Um saque deve aparecer:

- como saída no extrato da conta de origem;
- como entrada no extrato da conta `Carteira`.

Uma compra de moeda deve aparecer no extrato separado de cada moeda:

- como saída na moeda de origem;
- como entrada na moeda de destino.

---

## 4. Extrato de Lançamentos

O CyberBank disponibiliza um extrato consolidado de lançamentos.

O extrato de lançamentos permite visualizar em um único local as
movimentações financeiras registradas nos diferentes recursos financeiros
do ambiente.

As movimentações realizadas em contas bancárias compartilhadas também
devem ser apresentadas conforme as regras de acesso ao recurso.

---

## 5. Elementos Financeiros da Movimentação

Uma movimentação pode utilizar elementos financeiros cadastrados ou
compartilhados no ambiente, incluindo:

- conta bancária;
- categoria;
- subcategoria;
- forma de pagamento;
- forma de recebimento;
- cartão de crédito.

Quando uma movimentação utiliza uma conta bancária compartilhada entre
ambientes, ela pertence à mesma conta independentemente do ambiente a
partir do qual foi registrada.
