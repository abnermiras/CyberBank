# Controle Financeiro

## 1. Movimentação Financeira

A movimentação financeira representa o registro de uma entrada ou saída de dinheiro.

Uma movimentação normal deve possuir:

- tipo: `Entrada` ou `Saída`;
- valor;
- descrição;
- data de lançamento;
- data de efetivação;
- conta bancária;
- forma de pagamento ou recebimento;
- categoria;
- subcategoria, opcional;
- usuário responsável pela criação.

O usuário informa o valor sempre como número positivo. O tipo determina o efeito de débito ou crédito.

São aceitos valores como `1.521,10` e `1521,10`, ambos representando o mesmo valor.

### 1.1 Entrada

Uma entrada representa dinheiro recebido pelo usuário.

Para uma entrada, o usuário seleciona a conta e uma forma de recebimento previamente habilitada naquela conta.

### 1.2 Saída

Uma saída representa dinheiro utilizado ou pago pelo usuário.

Para uma saída, o usuário seleciona a conta e uma forma de pagamento previamente habilitada naquela conta.

### 1.3 Categoria e Subcategoria

A categoria é obrigatória e deve ser compatível com o tipo da movimentação:

- categoria `Entrada` para entradas;
- categoria `Saída` para saídas.

A subcategoria é opcional, mesmo quando a categoria possui subcategorias cadastradas.

### 1.4 Ciclo de Vida

Uma movimentação pode ser criada, editada ou excluída.

Excluir e cancelar representam a mesma operação.

Toda exclusão é lógica. O registro permanece armazenado para histórico e auditoria, mas deixa de produzir efeitos financeiros e de participar dos cálculos ativos.

### 1.4.1 Permissões

Qualquer usuário com permissão `Controle Total` no ambiente pode criar, editar ou excluir movimentações.

O usuário que criou a movimentação não precisa ser o mesmo usuário que posteriormente a altera ou exclui.

### 1.4.2 Responsável pela Criação

O CyberBank registra automaticamente o usuário que criou a movimentação.

O responsável pela criação não pode ser alterado posteriormente.

Essa informação existe para histórico e auditoria.

### 1.4.3 Alteração

Uma movimentação pode ser alterada mesmo depois de efetivada.

A alteração de uma movimentação efetivada deve recalcular os saldos, patrimônio e demais valores afetados.

Se a conta for alterada, o efeito financeiro deve ser removido da conta anterior e aplicado à nova conta.

A autoria original permanece preservada.

### 1.4.4 Exclusão

A exclusão de uma movimentação efetivada remove seu efeito financeiro e recalcula os valores afetados.

A exclusão de uma movimentação prevista faz com que ela deixe de participar das projeções.

### 1.4.5 Estorno

Estorno é exclusivo de movimentações relacionadas a cartões de crédito. As regras específicas ficam em `CREDIT-CARDS.md`.

## 1.5 Forma de Pagamento e Recebimento

O usuário seleciona a conta que será utilizada e, em seguida, uma forma previamente habilitada naquela conta.

Operações sistêmicas não exigem seleção manual da forma. O sistema determina automaticamente a natureza da operação.

Nas operações sistêmicas que possuem saída e entrada, a saída utiliza `Débito` e a entrada utiliza `Crédito`.

## 1.6 Lançamentos Futuros

O CyberBank permite registrar movimentações com datas futuras.

Uma movimentação futura permanece `Prevista` até sua data de efetivação e pode participar de projeções, planejamentos e dashboards.

## 1.7 Lançamentos Recorrentes

O CyberBank permite recorrências com:

- quantidade determinada de ocorrências;
- data de início e data de término;
- duração indefinida.

As periodicidades disponíveis seguem o conceito de calendário, incluindo:

- diária;
- semanal;
- mensal;
- anual;
- a cada X dias;
- a cada X semanas;
- a cada X meses.

Não existe tratamento especial para dia útil, feriado ou fim de semana.

### 1.7.1 Ocorrências

Cada ocorrência representa uma movimentação individual e permanece relacionada à série de recorrência.

Recorrências com quantidade determinada ou data de término têm suas ocorrências previstas criadas no momento da criação da série.

Se o usuário criar 10 ocorrências e excluir uma delas, ela não será recriada para compensar a exclusão.

### 1.7.2 Recorrência Semanal

Uma recorrência semanal pode utilizar mais de um dia da semana, por exemplo, segunda, quarta e sexta-feira.

### 1.7.3 Recorrência Mensal

A recorrência mensal pode utilizar um dia específico do mês.

Quando o dia escolhido não existir em determinado mês, o sistema pergunta como tratar a ocorrência:

- último dia disponível do mês;
- primeiro dia do mês seguinte;
- não gerar a ocorrência.

A escolha passa a fazer parte da configuração da recorrência.

### 1.7.4 Recorrência Anual

A recorrência anual pode utilizar uma data específica do calendário.

Quando a data não existir em determinado ano, aplica-se a mesma regra de tratamento de datas inexistentes:

- último dia disponível;
- primeiro dia do mês seguinte;
- não gerar a ocorrência.

### 1.7.5 Recorrência Indefinida

Uma recorrência indefinida não gera infinitos registros persistentes.

O CyberBank mantém a regra da recorrência e o registro persistente correspondente ao período atual.

Os períodos futuros são apresentados visualmente pelo extrato e pelo mapa de lançamentos conforme o período consultado.

Quando um período futuro se tornar o período atual, sua ocorrência passa a ser persistida.

### 1.7.6 Alteração de Recorrência

Ao alterar uma ocorrência de uma série, o sistema pergunta o escopo:

- apenas esta ocorrência;
- todas as ocorrências;
- apenas ocorrências futuras.

Uma alteração somente na ocorrência cria uma exceção à regra da série. As demais ocorrências continuam seguindo a regra original.

Se a alteração afetar ocorrências já efetivadas, o sistema alerta o usuário antes da confirmação.

Após a confirmação, a alteração pode atingir ocorrências já efetivadas e os efeitos financeiros devem ser recalculados.

### 1.7.7 Exclusão de Recorrência

Ao excluir uma ocorrência de uma série, o sistema pergunta o escopo:

- apenas esta ocorrência;
- todas as ocorrências;
- apenas ocorrências futuras.

As ocorrências excluídas são logicamente excluídas.

Se ocorrências efetivadas forem excluídas, seus efeitos financeiros são removidos e os valores afetados são recalculados.

Quando todas as ocorrências forem excluídas, a regra de recorrência também é encerrada e nenhuma nova ocorrência será gerada.

### 1.7.8 Alteração da Data de Término

Se a data de término for reduzida, ocorrências futuras além da nova data são excluídas logicamente.

Se posteriormente a data de término for ampliada, as ocorrências correspondentes ao novo período são criadas novamente.

Ocorrências já efetivadas não devem ser removidas silenciosamente. O sistema deve alertar o usuário antes de qualquer alteração que as afete.

## 1.8 Datas da Movimentação

Uma movimentação possui duas datas distintas:

- **Data de lançamento:** quando o movimento foi registrado no CyberBank;
- **Data de efetivação:** quando o movimento financeiro realmente ocorre.

A data de lançamento é preenchida automaticamente com a data atual, mas o usuário pode alterá-la.

Para formas de pagamento com efeito direto, como `PIX`, `TED`, `Débito`, `Saque` e `Desconto em Folha de Pagamento`, a data de efetivação é inicialmente a data atual, podendo ser alterada.

Para cartão de crédito, a data de efetivação é inicialmente a data de vencimento da fatura, podendo ser alterada pelo usuário.

### 1.8.1 Situação

A situação é determinada automaticamente pela data atual do sistema e pela data de efetivação:

- `Prevista` — data de efetivação ainda não atingida;
- `Efetivada` — data de efetivação atingida.

O usuário não define manualmente essa situação.

### 1.8.2 Prevista

Uma movimentação `Prevista` não altera o saldo atual da conta nem o patrimônio atual.

Pode ser utilizada em projeções, planejamentos e dashboards.

### 1.8.3 Efetivada

Quando a data de efetivação é atingida, a movimentação passa automaticamente para `Efetivada` e passa a produzir efeito financeiro.

O saldo da conta e os cálculos patrimoniais são atualizados.

### 1.8.4 Cartão de Crédito

Uma compra no cartão consome o limite imediatamente na data de lançamento, mesmo estando `Prevista`.

O consumo do limite não reduz o saldo bancário naquele momento.

Quando a fatura é paga, o saldo da conta bancária utilizada para o pagamento é reduzido e o limite comprometido é liberado.

Exemplo:

- limite total: R$ 5.000;
- compra: R$ 1.000;
- limite disponível após a compra: R$ 4.000;
- saldo bancário: permanece inalterado até o pagamento da fatura.

## 1.9 Projeção Financeira

Movimentações previstas e efetivadas podem ser utilizadas para análises financeiras futuras.

As projeções podem alimentar dashboards, planejamentos, metas, projetos financeiros e análises de fluxo de caixa.

Uma projeção não altera saldos efetivos nem patrimônio atual.

## 1.10 Saldos e Patrimônio

### 1.10.1 Saldo da Conta

O saldo da conta considera somente movimentações `Efetivadas`.

### 1.10.2 Patrimônio

O patrimônio pode incluir:

- contas bancárias;
- dinheiro físico;
- investimentos;
- criptomoedas;
- outros ativos financeiros.

Transferências entre recursos pertencentes ao mesmo patrimônio não alteram o patrimônio total.

### 1.10.3 Saldo Projetado

O saldo projetado pode considerar os saldos atuais e movimentações previstas.

Ele não altera os saldos efetivos nem o patrimônio atual.

### 1.10.4 Patrimônio por Ambiente

O patrimônio é calculado individualmente para cada ambiente financeiro.

Uma conta compartilhada pode compor o patrimônio de cada ambiente que possui acesso à conta.

A mesma conta não é contabilizada duas vezes dentro do patrimônio de um mesmo ambiente.

Exemplo: `CLT` possui Banco1 R$ 100 e Banco2 R$ 200, portanto patrimônio de R$ 300. Se Banco2 for compartilhado com `PJ`, o patrimônio de `PJ` é R$ 200.

### 1.10.5 Patrimônio por Moeda

O patrimônio é apresentado separadamente por moeda.

O CyberBank não converte moedas automaticamente para uma moeda única.

## 1.11 Ambiente de Origem

Toda movimentação possui um ambiente financeiro de origem: o ambiente no qual o usuário realizou o lançamento.

Uma movimentação realizada em conta compartilhada permanece associada ao ambiente no qual foi criada.

## 1.12 Extrato e Mapa de Lançamentos

O CyberBank possui duas visões distintas.

### 1.12.1 Extrato

O extrato pertence ao recurso financeiro, como uma conta bancária.

Todos os ambientes com acesso à conta podem visualizar seu extrato, que apresenta as movimentações da conta independentemente do ambiente de origem.

Quando a movimentação foi criada em outro ambiente, informações específicas como categoria podem ficar em branco conforme as regras de compartilhamento.

### 1.12.2 Mapa de Lançamentos

O mapa de lançamentos pertence ao ambiente financeiro e apresenta somente movimentações cuja origem é o próprio ambiente.

Uma movimentação feita em outro ambiente, mesmo utilizando conta compartilhada, não aparece no mapa do ambiente atual.

### 1.12.3 Exemplo

Banco1 possui movimentação de R$ 10,00 `Pizza`, categoria `Pessoal / Alimentação`, criada no `CLT`, e outra de R$ 150,00 `DIRPF`, categoria `Empresa / Impostos`, criada no `PJ`.

O extrato de Banco1 apresenta ambas para os dois ambientes. O mapa do `CLT` apresenta somente a pizza e o mapa do `PJ` somente o imposto. No extrato de cada ambiente, a classificação da movimentação originada no outro ambiente pode ficar em branco.

## 1.13 Transferências entre Contas

Uma transferência sistêmica somente pode ser registrada quando as contas de origem e destino estiverem acessíveis dentro do mesmo ambiente.

A operação gera duas movimentações relacionadas:

- saída na conta de origem;
- entrada na conta de destino.

Ambas utilizam a categoria sistêmica `Transferência`. A saída utiliza `Débito` e a entrada utiliza `Crédito` automaticamente.

Quando as contas não são acessíveis pelo mesmo ambiente, não existe transferência sistêmica entre elas. Nesse caso, devem ser registradas uma saída e uma entrada independentes, cada uma com sua categoria de usuário.

### 1.13.1 Visibilidade

Quando um ambiente possui acesso às duas contas, visualiza os dois lados da transferência.

Quando possui acesso somente a uma conta, visualiza somente a movimentação correspondente e não consegue identificar o recurso que não está acessível.

### 1.13.2 Exemplo

`CLT` possui Banco1 e Banco2. Banco2 é compartilhado com `PJ`, que possui Banco3. Banco1 e Banco3 não estão acessíveis pelo mesmo ambiente.

Banco2 → Banco1 pode ser uma transferência sistêmica: no `CLT` aparecem saída de R$ 100 em Banco2 e entrada de R$ 100 em Banco1. No `PJ`, aparece somente a saída de Banco2 e não é possível identificar Banco1.

Banco1 → Banco3 não pode ser uma transferência sistêmica, pois não existe ambiente com acesso comum às duas contas.

### 1.13.3 Alteração e Exclusão

Uma transferência pode ser alterada por qualquer usuário com `Controle Total` do ambiente.

A alteração do valor atualiza todas as movimentações relacionadas e recalcula os saldos afetados.

A exclusão é lógica e exclui os dois lados relacionados da transferência. Os saldos afetados são recalculados.

## 1.14 Histórico Financeiro

Alterações ou desativação de uma conta não removem suas movimentações históricas.

A perda de acesso a uma conta compartilhada não remove nem altera movimentações realizadas enquanto o acesso existia.

O histórico permanece associado ao recurso financeiro e respeita as regras de acesso e compartilhamento.

## 2. Movimentações Sistêmicas

O CyberBank utiliza movimentações sistêmicas para operações controladas pelo próprio sistema.

Categorias sistêmicas não dependem de categorias criadas pelo usuário.

### 2.1 Transferência

Uma transferência gera saída e entrada relacionadas e utiliza a categoria sistêmica `Transferência`.

A operação atualiza os saldos das duas contas, mas não altera o patrimônio total porque apenas desloca recursos.

### 2.2 Saque

O saque transforma dinheiro de uma conta bancária em dinheiro físico.

Gera:

- saída da conta de origem;
- entrada em uma conta `Carteira`.

A conta `Carteira` aceita exclusivamente `Dinheiro` como pagamento e recebimento.

A saída utiliza `Débito` e a entrada utiliza `Crédito` automaticamente.

O saque atualiza os saldos envolvidos, mas não altera o patrimônio total.

### 2.3 Compra de Moeda

A compra de moeda ocorre dentro de uma mesma conta e gera:

- saída na moeda de origem;
- entrada na moeda de destino.

Utiliza a categoria sistêmica `Compra de Moeda`, com `Débito` na saída e `Crédito` na entrada.

O usuário informa o valor utilizado na moeda de origem e o valor recebido na moeda de destino.

O CyberBank não precisa registrar cotação para composição do patrimônio, pois os valores patrimoniais permanecem separados por moeda.

### 2.4 Ajuste de Saldo

O `Ajuste de Saldo` é uma movimentação sistêmica utilizada no cadastro ou atualização do saldo de uma conta.

Ajustes aparecem no extrato, utilizam a categoria sistêmica `Ajuste de Saldo` e afetam o saldo e o patrimônio do ambiente.

## 3. Extratos

Transferências aparecem como saída na conta de origem e entrada na conta de destino.

Saques aparecem como saída na conta de origem e entrada na conta `Carteira`.

Compras de moeda aparecem como saída no extrato da moeda de origem e entrada no extrato da moeda de destino.

## 4. Extrato de Lançamentos

O CyberBank disponibiliza um extrato consolidado de lançamentos do ambiente.

Ele reúne as movimentações registradas nos recursos financeiros do ambiente conforme as regras de acesso e compartilhamento.

## 5. Elementos Financeiros

Uma movimentação pode utilizar:

- conta bancária;
- categoria;
- subcategoria;
- forma de pagamento;
- forma de recebimento;
- cartão de crédito.

Quando utiliza conta compartilhada, a movimentação pertence à mesma conta independentemente do ambiente no qual foi registrada.
