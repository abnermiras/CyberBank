# Cartões de Crédito

## 1. Conceito

Um cartão de crédito representa um contrato de crédito criado pelo usuário dentro de um ambiente financeiro.

O contrato pertence ao usuário que realizou sua criação e ao ambiente financeiro no qual foi criado.

O cartão pode estar associado a um banco ou ser mantido por uma empresa ou outra entidade não bancária.

O contrato possui um limite global de crédito. O limite pertence ao contrato e é compartilhado por todos os cartões vinculados a ele.

---

## 2. Cadastro do Cartão de Crédito

O usuário pode criar um cartão de crédito dentro de seu ambiente financeiro.

No momento da criação devem ser informados:

* nome do cartão;
* últimos quatro dígitos;
* limite global;
* empresa mantenedora;
* banco associado, quando aplicável;
* dia de vencimento da fatura;
* quantidade de dias anteriores ao vencimento utilizada para fechamento da fatura.

O limite global pode ser alterado posteriormente pelo titular do contrato.

Os últimos quatro dígitos são utilizados somente para identificação. O CyberBank não armazena o número completo do cartão.

---

## 3. Empresa Mantenedora e Associação com Banco

Todo contrato de cartão de crédito possui uma empresa mantenedora.

A empresa mantenedora representa a instituição responsável pelo contrato de crédito e pode ser um banco, instituição financeira, empresa não bancária ou outra entidade responsável pelo contrato.

A associação com um banco cadastrado no ambiente é opcional.

### 3.1 Mantenedora Bancária já Cadastrada

Quando a empresa mantenedora for um banco e esse banco já estiver cadastrado no ambiente do usuário, o cartão deve ser associado ao banco existente.

Exemplo:

```text
Banco: Nubank
Cartão: Ultravioleta ****-1234
Limite: R$ 50.000,00
```

Durante o lançamento de uma saída, o usuário seleciona o banco e o cartão aparece como forma de pagamento.

### 3.2 Mantenedora Bancária não Cadastrada

Quando a empresa mantenedora for um banco que não estiver cadastrado no ambiente, o cartão pode ser criado sem associação a um banco cadastrado.

A informação da empresa mantenedora permanece armazenada no contrato. O cadastro do cartão não cria automaticamente uma conta bancária.

### 3.3 Mantenedora não Bancária

Um cartão pode ser mantido por uma empresa que não seja um banco.

Exemplo:

```text
Empresa mantenedora: Lojas Marisa
Banco associado: nenhum
Cartão: Cartão Marisa ****-1234
```

Durante o lançamento, a empresa mantenedora pode aparecer como entidade de pagamento e o cartão como forma de pagamento.

---

## 4. Cartão como Forma de Pagamento

O cartão de crédito é uma forma de pagamento utilizada em lançamentos de saída.

Durante a criação de uma saída, o usuário seleciona a entidade de pagamento e a forma de pagamento disponível para essa entidade.

Quando a entidade possuir cartões de crédito disponíveis no ambiente, os cartões correspondentes aparecem como formas de pagamento.

A identificação do cartão deve apresentar:

* nome do cartão;
* últimos quatro dígitos.

Exemplo:

```text
Ultravioleta ****-1234
```

O número completo do cartão não é armazenado ou exibido.

---

## 5. Limite Global

Todo contrato de cartão de crédito possui um limite global.

O limite é definido pelo titular no momento da criação do contrato e pode ser alterado posteriormente pelo titular.

Todos os cartões vinculados ao contrato compartilham o mesmo limite global, incluindo cartões principais, virtuais, adicionais e compartilhados.

Não existe limite independente por cartão.

### 5.1 Limite como Informação Financeira

O limite é uma informação financeira e não uma trava operacional.

O CyberBank não impede uma nova compra quando o limite disponível for insuficiente.

O limite disponível pode ficar negativo.

Exemplo:

```text
Limite global:       R$ 50.000,00
Limite comprometido: R$ 30.000,00
Limite disponível:   R$ 20.000,00
```

Se o titular alterar o limite para R$ 20.000,00:

```text
Limite global:       R$ 20.000,00
Limite comprometido: R$ 30.000,00
Limite disponível:  -R$ 10.000,00
```

O sistema continua permitindo novos lançamentos.

---

## 6. Consumo do Limite

Um lançamento realizado com cartão de crédito compromete o limite global do contrato imediatamente, no momento em que é registrado.

O consumo independe da situação `Prevista` ou `Realizada` do lançamento.

O valor comprometido permanece associado ao contrato até ser liberado pelas regras de quitação, exclusão ou estorno.

### 6.1 Compra à Vista

Uma compra à vista compromete imediatamente o valor total da compra.

Exemplo:

```text
Limite global:       R$ 50.000
Compra:              R$ 1.000
Limite comprometido: R$ 1.000
Limite disponível:   R$ 49.000
```

### 6.2 Compra Parcelada

Uma compra parcelada compromete imediatamente o valor total da compra.

Exemplo:

```text
Compra: R$ 1.200
Parcelamento: 12 x R$ 100

Comprometimento inicial do limite:
R$ 1.200
```

O valor comprometido de uma parcela somente é liberado quando a fatura que contém aquela parcela for integralmente quitada.

Nesse exemplo, a quitação da fatura contendo uma parcela de R$ 100 libera R$ 100 do limite comprometido.

Pagamento parcial da fatura não libera limite.

---

## 7. Cartões Virtuais

O titular pode criar cartões virtuais vinculados ao contrato.

O cartão virtual não constitui um novo contrato e utiliza o limite global do contrato.

Cada cartão virtual possui sua própria identificação e sua própria fatura.

Os lançamentos realizados no cartão virtual seguem as mesmas regras de lançamento, limite e faturamento dos demais cartões do contrato.

---

## 8. Cartões Adicionais

O titular do contrato pode criar cartões adicionais para outros usuários do CyberBank.

O cartão adicional não constitui um novo contrato, permanece vinculado ao contrato do titular e utiliza o limite global do contrato.

Não existe limite individual para cartão adicional.

### 8.1 Recebimento

Após a criação, o cartão adicional é disponibilizado ao usuário destinatário.

O usuário destinatário deve aceitar o recebimento e escolher em qual de seus ambientes financeiros o cartão será disponibilizado.

O cartão passa a ser uma forma de pagamento disponível naquele ambiente.

### 8.2 Propriedade

O usuário destinatário não se torna proprietário do contrato. O titular continua sendo o proprietário do contrato e responsável por sua estrutura de limite.

O usuário destinatário recebe apenas o direito de utilização do cartão adicional.

### 8.3 Lançamentos e Fatura

Os lançamentos realizados pelo usuário do cartão adicional:

* consomem o limite global do contrato;
* pertencem ao contrato do titular;
* são realizados pelo usuário destinatário;
* pertencem ao ambiente escolhido pelo destinatário;
* aparecem na fatura específica do cartão adicional.

---

## 9. Cartões Compartilhados

O titular pode compartilhar um cartão existente com outro usuário do CyberBank.

O compartilhamento não cria um novo contrato. O cartão continua pertencendo ao contrato original e utiliza seu limite global.

O usuário que recebe o compartilhamento deve aceitar o recebimento e escolher em qual de seus ambientes o cartão será disponibilizado.

O compartilhamento representa a concessão de utilização do cartão a outro usuário, sem transferência de propriedade.

O cartão compartilhado possui sua própria fatura. Os lançamentos realizados pelo usuário compartilhado aparecem na fatura desse mesmo cartão.

---

## 10. Faturas

Cada cartão possui suas próprias faturas.

A fatura é vinculada a um cartão específico. Cartões diferentes do mesmo contrato possuem faturas diferentes.

As faturas dos cartões pertencentes ao mesmo contrato ficam reunidas sob o mesmo contrato para fins de responsabilidade e visualização do titular.

Exemplo:

```text
Contrato
│
├── Cartão principal
│     └── Faturas
│
├── Cartão virtual
│     └── Faturas
│
├── Cartão adicional
│     └── Faturas
│
└── Cartão compartilhado
      └── Faturas
```

O titular do contrato consegue visualizar todas as faturas de todos os cartões vinculados ao contrato.

---

## 11. Ciclo de Faturamento

O usuário define no cadastro do cartão:

* dia de vencimento;
* quantidade de dias anteriores ao vencimento para fechamento.

O CyberBank calcula automaticamente a data de fechamento da fatura.

As compras respeitam o ciclo de faturamento.

Um lançamento registrado antes do fechamento pertence à fatura atual. Um lançamento registrado depois do fechamento pertence à próxima fatura.

### 11.1 Fechamento Automático

Quando o sistema identificar que chegou a data configurada para fechamento, a fatura atual será fechada automaticamente e uma nova fatura será aberta para o próximo ciclo.

Os lançamentos realizados posteriormente passam a pertencer à nova fatura.

### 11.2 Fechamento Manual

O titular do contrato pode fechar manualmente a fatura a qualquer momento.

Os lançamentos registrados antes do fechamento permanecem na fatura que está sendo fechada. Os lançamentos registrados depois do fechamento passam para a próxima fatura.

O usuário pode fechar novamente a fatura após realizar ajustes. O sistema não deve impor restrições artificiais que impeçam a correção operacional.

### 11.3 Reabertura

O titular do contrato pode reabrir uma fatura fechada.

Uma fatura reaberta pode receber novos lançamentos e pode ser fechada novamente pelo titular.

Pagamentos já registrados permanecem registrados. A situação financeira da fatura deve continuar sendo determinada pelos valores devidos, créditos e pagamentos existentes.

---

## 12. Lançamentos, Datas e Situação

Quando uma saída é criada utilizando um cartão de crédito:

1. o lançamento aparece no extrato de lançamentos;
2. o mesmo lançamento aparece na fatura do cartão utilizado;
3. o valor compromete o limite global do contrato.

O extrato e a fatura representam visões diferentes do mesmo lançamento. O lançamento não é duplicado.

### 12.2 Data de Efetivação

A data de efetivação representa a data em que o lançamento foi considerado
financeiramente realizado.

Para lançamentos de cartão de crédito, existem duas situações:

#### 12.2.1 Quitação por Pagamento

Quando a fatura é quitada por pagamento, a data de efetivação dos lançamentos
é a data do pagamento que completou a quitação integral da fatura.

Exemplo:

```text
Data da compra:          05/08
Vencimento da fatura:    18/08
Pagamento parcial:       18/08
Pagamento final:         25/08

Data de lançamento:      05/08
Data de efetivação:      25/08
```
---

## 13. Pagamento da Fatura

Uma fatura fechada pode ser paga utilizando qualquer banco ou conta disponível no ambiente que realizará o pagamento.

O usuário seleciona a conta ou banco de origem e a forma de pagamento permitida.

O pagamento de fatura não pode utilizar cartão de crédito.

O pagamento gera os efeitos financeiros correspondentes no extrato e no saldo da conta utilizada.

O titular ou usuário autorizado pelo cartão pode realizar pagamentos conforme suas permissões.

### 13.1 Pagamento Parcial

O pagamento parcial é permitido.

Quando ocorre um pagamento parcial:

* o valor pago é registrado;
* o pagamento permanece associado à fatura;
* a fatura continua não quitada;
* todos os lançamentos da fatura permanecem `Previstos`;
* todo o limite comprometido pelos lançamentos permanece comprometido.

O CyberBank não distribui um pagamento parcial entre lançamentos individuais.

Exemplo:

```text
Fatura: R$ 100,00

10 lançamentos de R$ 10,00

Pagamento parcial: R$ 50,00
```

Resultado:

```text
Fatura:              Pendente
Total da fatura:     R$ 100,00
Total pago:          R$ 50,00
Saldo pendente:      R$ 50,00

Todos os lançamentos: Previsto
Limite:                continua comprometido em R$ 100,00
```

### 13.2 Quitação da Fatura

A fatura é quitada quando o total de pagamentos e créditos aplicáveis atingir ou superar o valor devido da fatura.

Quando a fatura for quitada:

* a fatura passa para `Quitada`;
* todos os lançamentos da fatura passam para `Realizados`;
* a data de efetivação dos lançamentos é definida pelas regras de efetivação:
  a data do pagamento que completou a quitação, quando houver pagamento, ou a
  data de vencimento da própria fatura, quando a quitação ocorrer por
  compensação de crédito;
* o limite correspondente aos lançamentos quitados é liberado;
* os efeitos financeiros do pagamento são refletidos na conta utilizada.

A unidade de quitação é a fatura. O CyberBank não tenta identificar quais lançamentos individuais foram pagos com cada pagamento parcial.

### 13.3 Pagamento após o Vencimento

Uma fatura pode permanecer pendente após o vencimento.

O vencimento não gera automaticamente juros, multa, encargos, alteração da situação dos lançamentos ou liberação de limite.

Enquanto a fatura não estiver integralmente quitada, os lançamentos permanecem `Previstos` e o limite permanece comprometido.

A fatura pode ser quitada posteriormente. A data de efetivação será a data do pagamento que completar a quitação.

---

## 14. Crédito de Fatura

Uma fatura pode possuir saldo credor quando pagamentos ou créditos forem superiores ao valor devido.

O crédito não é perdido e não é devolvido automaticamente ao usuário. Ele é transportado para a próxima fatura do mesmo cartão.

O crédito pertence ao cartão e não ao contrato de forma independente. Não pode ser transferido para outro cartão do mesmo contrato.

### 14.1 Pagamento Superior ao Valor da Fatura

O CyberBank permite que os pagamentos acumulados superem o valor devido da fatura.

Exemplo:

```text
Valor devido:       R$ 1.000
Pagamento:          R$ 1.200

Fatura:             Quitada
Saldo credor:       R$ 200
```

Os lançamentos da fatura são efetivados e o saldo excedente permanece como crédito do cartão.

### 14.2 Aplicação do Crédito na Próxima Fatura

O crédito é aplicado automaticamente à próxima fatura do mesmo cartão.

Se o crédito for menor que a nova fatura, o crédito é integralmente utilizado
e o valor restante deve ser pago normalmente.

Exemplo:


Crédito anterior:   R$ 200
Nova fatura:        R$ 500

Crédito aplicado:   R$ 200
Valor líquido:      R$ 300
Crédito restante:   R$ 0
### 14.3 Crédito e Limite

Crédito de fatura e limite de crédito são conceitos distintos.

O crédito representa valor já pago ou creditado em favor do usuário. O limite representa a capacidade de crédito disponibilizada pelo contrato.

O crédito não aumenta o limite global configurado.

Novas compras continuam consumindo o limite global normalmente.

### 14.3 Crédito e Limite

Crédito de fatura e limite de crédito são conceitos distintos.

O limite representa a capacidade de crédito disponibilizada pelo contrato.

O crédito representa um valor já pago ou creditado em favor do usuário e
disponível para compensação de futuras faturas.

O crédito de fatura nunca aumenta o limite global configurado para o contrato.

Exemplo:

```text
Limite global:       R$ 5.000
Limite disponível:   R$ 5.000
Crédito de fatura:   R$ 2.000
```

### 14.4 Crédito de Cartão Desativado

O crédito de fatura permanece vinculado ao cartão que originou o crédito,
mesmo quando esse cartão for desativado.

A desativação do cartão não:

* elimina o crédito;
* transfere o crédito para outro cartão;
* transfere o crédito para o contrato de forma genérica;
* libera o crédito para utilização em outro cartão.

O crédito permanece disponível para consulta no histórico do cartão
desativado.

Como o cartão desativado não aceita novos lançamentos, o crédito não será
utilizado em novas compras realizadas nesse cartão.

O crédito permanece vinculado ao cartão para preservação do histórico
financeiro da operação.

---

## 15. Visibilidade e Permissões

### 15.1 Titular do Contrato

O titular consegue visualizar:

* todas as faturas do contrato;
* todas as faturas de todos os cartões;
* todos os lançamentos das faturas;
* lançamentos realizados por qualquer usuário autorizado do contrato.

Somente o titular pode fechar ou reabrir uma fatura.

### 15.2 Usuário de Cartão Adicional

O usuário que recebeu um cartão adicional pode visualizar a fatura específica daquele cartão.

Ele visualiza somente os lançamentos realizados por ele próprio.

Ele pode realizar pagamentos parciais ou integrais da fatura, conforme as permissões do cartão.

Ele não visualiza as demais faturas do contrato e não pode fechar ou reabrir a fatura.

### 15.3 Usuário de Cartão Compartilhado

O usuário que recebeu acesso compartilhado a um cartão pode visualizar a fatura daquele cartão.

Ele visualiza somente os lançamentos realizados por ele próprio.

Ele pode realizar pagamentos parciais ou integrais da fatura, conforme as permissões do cartão.

Ele não visualiza as demais faturas do contrato e não pode fechar ou reabrir a fatura.

---

## 16. Alteração de Compras

O fechamento da fatura não congela os lançamentos.

Uma compra pertencente a uma fatura aberta ou fechada pode ser alterada enquanto a fatura não estiver quitada, respeitando as permissões do usuário.

A alteração deve recalcular:

* o valor do lançamento;
* o valor devido da fatura;
* o limite comprometido pelo contrato;
* os demais valores financeiros afetados.

A alteração não transfere o lançamento para outra fatura.

### 16.1 Redução do Valor

Quando o valor for reduzido, a diferença deixa de comprometer o limite global.

### 16.2 Aumento do Valor

Quando o valor for aumentado, a diferença passa a comprometer o limite global.

### 16.3 Fatura com Pagamento Parcial

Uma fatura que recebeu pagamentos parciais continua não quitada.

Os lançamentos podem ser alterados enquanto a fatura permanecer não quitada.

O valor dos pagamentos já realizados permanece registrado. O CyberBank não atribui esses pagamentos a lançamentos individuais.

Exemplo:

```text
Fatura original:    R$ 1.000
Pagamento parcial:  R$   300

Compra alterada:
R$ 500 -> R$ 400

Nova fatura:        R$ 900
Pago:               R$ 300
Saldo:              R$ 600
```

Todos os lançamentos continuam `Previstos` até a quitação integral.

A alteração deve permanecer registrada no histórico para fins de auditoria.

---

## 17. Exclusão de Compra

A exclusão é utilizada para remover uma compra que ainda não deve permanecer como obrigação financeira ativa.

A exclusão não elimina fisicamente o histórico da operação.

Quando uma compra ainda puder ser corrigida por alteração, o usuário pode alterar o lançamento em vez de realizar um estorno.

### 17.1 Exclusão de Compra Parcelada sem Parcela Quitada

Quando nenhuma parcela do parcelamento estiver em uma fatura quitada, o usuário pode excluir a compra parcelada.

A exclusão:

* remove logicamente o parcelamento ativo;
* remove as parcelas ainda existentes das respectivas faturas;
* libera o limite global comprometido pelo parcelamento;
* recalcula as faturas afetadas;
* remove o gasto do Mapa de Lançamentos;
* preserva o histórico da operação.

### 17.2 Parcelamento com Parcela Quitada

Quando pelo menos uma parcela estiver em uma fatura quitada, a compra parcelada não pode ser excluída.

O CyberBank deve impedir a exclusão e orientar o usuário a utilizar o processo de estorno.

---

## 18. Alteração de Compra Parcelada

Uma compra parcelada representa uma única série de parcelas.

### 18.1 Alteração do Valor

Quando nenhuma parcela estiver em uma fatura quitada, o valor total da compra pode ser alterado.

O CyberBank recalcula toda a série mantendo a quantidade original de parcelas.

Exemplo:

```text
Original: R$ 1.200 em 12x R$ 100
Novo valor: R$ 900

Nova série: 12x R$ 75
```

O limite comprometido passa de R$ 1.200 para R$ 900 e a diferença de R$ 300 é liberada.

### 18.2 Parcela já Efetivada

Quando pelo menos uma parcela já tiver sido efetivada, uma alteração que modifique o valor financeiro da operação não pode ser feita diretamente.

O valor já efetivado deve permanecer preservado. A correção deve utilizar o processo de estorno.

### 18.3 Alteração da Quantidade de Parcelas

Quando nenhuma parcela tiver sido efetivada, o usuário pode alterar a quantidade de parcelas.

O CyberBank recalcula a série usando o valor total da compra, a nova quantidade de parcelas e as regras de distribuição de centavos.

Exemplo:

```text
R$ 1.200 em 12x R$ 100

Alteração para 8x:
8x R$ 150
```

Quando pelo menos uma parcela tiver sido efetivada, a quantidade de parcelas não pode ser alterada diretamente. O usuário deve utilizar o processo de estorno e, se necessário, registrar uma nova compra com a quantidade desejada.

### 18.4 Distribuição de Centavos

A soma das parcelas deve ser exatamente igual ao valor total da compra.

Quando a divisão não resultar em valores exatos de centavos, a diferença de arredondamento deve ser distribuída entre as parcelas sem alterar o total da operação.

Exemplo:

```text
R$ 1.000,00 / 3

Parcela 1: R$ 333,34
Parcela 2: R$ 333,33
Parcela 3: R$ 333,33
-----------------------
Total:     R$ 1.000,00
```

A posição da diferença de centavos é uma regra de cálculo. A regra de negócio é que a soma das parcelas seja exatamente igual ao valor total da compra.

### 18.5 Antecipação de Parcelas

O CyberBank não possui funcionalidade de antecipação de parcelas.

As parcelas seguem normalmente seus respectivos ciclos de faturamento.

O sistema não antecipa parcelas futuras para uma fatura anterior e não calcula ou concede descontos de antecipação.

---

## 19. Estorno

O estorno representa o reconhecimento de um crédito decorrente da devolução ou correção de uma compra de cartão.

O estorno é registrado como valor positivo na fatura ou como crédito do cartão, conforme o estado financeiro da operação.

O estorno não apaga a operação original do histórico.

O Mapa de Lançamentos não deve apresentar uma compra integralmente estornada como gasto ativo.

### 19.1 Compra em Fatura Não Quitada

Quando a fatura ainda não estiver quitada e o usuário estiver apenas corrigindo o valor da compra, deve alterar o próprio lançamento.

O estorno não é necessário para uma simples correção de valor enquanto a operação puder ser editada.

### 19.2 Estorno de Compra em Fatura Quitada

Uma compra pode ser estornada mesmo depois de a fatura que a contém ter sido quitada.

O estorno não desfaz retroativamente o pagamento da fatura.

O estorno gera um crédito positivo no cartão.

Exemplo:

```text
Compra:                 R$ 500
Fatura:                 R$ 500
Pagamento:              R$ 500
Fatura:                 Quitada

Estorno posterior:     +R$ 500
Crédito do cartão:       R$ 500
```

O crédito será aplicado automaticamente às próximas faturas do mesmo cartão.

Se a compra já estiver integralmente quitada e seu limite correspondente já tiver sido liberado, o estorno não gera uma segunda liberação de limite.

### 19.3 Estorno Parcial após Quitação

Quando uma compra já efetivada for parcialmente estornada, somente o valor estornado gera crédito.

O histórico preserva tanto a compra original quanto o estorno.

### 19.4 Estorno de Compra Parcelada

Quando uma compra parcelada possuir parcelas já quitadas, o estorno considera o valor total que ainda falta ser efetivado no parcelamento.

O estorno não altera retroativamente as parcelas já quitadas.

Exemplo:

```text
Compra: R$ 1.200 em 12x R$ 100

Parcelas quitadas: 4
Valor já quitado: R$ 400

Parcelas restantes: 8
Valor restante: R$ 800
```

O estorno gera um crédito de R$ 800 de uma única vez.

As parcelas futuras deixam de existir como obrigações financeiras.

Os R$ 800 liberam o limite ainda comprometido pelo parcelamento.

Se esse crédito for superior às obrigações da fatura atual, o excedente permanece como saldo credor para os próximos ciclos.

Os R$ 400 já quitados não retornam ao limite.

### 19.5 Estorno e Mapa de Lançamentos

Uma compra integralmente estornada deixa de aparecer como gasto ativo no Mapa de Lançamentos.

A retirada do mapa não representa exclusão física do registro. A compra original e o estorno permanecem no histórico.

---

## 20. Encargos por Atraso

O CyberBank não calcula automaticamente juros, multas ou outros encargos relacionados ao atraso de uma fatura.

As regras de encargos podem variar de acordo com a instituição mantenedora e com o contrato de cada usuário. O CyberBank não reproduz essas regras específicas.

Quando houver juros, multa ou outro encargo, o usuário deve registrar o valor manualmente como uma nova movimentação financeira, utilizando suas categorias e subcategorias.

Exemplos:

* `Juros de Cartão`;
* `Multa de Cartão`;
* `Encargos Financeiros`.

Quando o encargo estiver relacionado a uma fatura, o usuário poderá associá-lo àquela fatura conforme as regras gerais de lançamento de cartão.

---

## 21. Histórico e Desativação

Operações relacionadas ao cartão não devem remover permanentemente
informações financeiras que possuam histórico.

Devem permanecer disponíveis para consulta:

* cartões desativados;
* faturas;
* lançamentos;
* parcelas;
* pagamentos;
* estornos;
* créditos de fatura;
* alterações de limite;
* demais registros financeiros relacionados ao contrato.

A exclusão de um lançamento ou a desativação de um cartão não elimina o
histórico financeiro.

### 21.1 Desativação do Cartão

Um cartão pode ser desativado pelo titular do contrato.

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação não:

* cancela faturas existentes;
* cancela parcelas futuras;
* remove lançamentos históricos;
* altera pagamentos já realizados;
* libera automaticamente limite comprometido;
* elimina créditos existentes.

### 21.2 Faturas de Cartão Desativado

As faturas existentes continuam seguindo normalmente seu ciclo financeiro
mesmo após a desativação do cartão.

Uma fatura pendente continua podendo receber pagamentos.

O usuário continua podendo quitar a fatura conforme as regras normais de
pagamento.

Quando a fatura for integralmente quitada, o limite correspondente aos seus
lançamentos será liberado para o limite global do contrato.

Exemplo:

Cartão desativado

Fatura:              R$ 1.000
Pagamento parcial:   R$   400
Saldo pendente:      R$   600

Limite comprometido: R$ 1.000

---

## 22. Regras de Integridade do Domínio

As seguintes invariantes devem ser preservadas pelo CyberBank:

1. O limite global pertence ao contrato e é compartilhado por todos os cartões do contrato.
2. O limite não bloqueia novos lançamentos e pode ficar negativo.
3. Uma compra de cartão compromete o limite imediatamente.
4. Pagamento parcial não libera limite.
5. A quitação integral da fatura é a unidade de efetivação dos lançamentos.
6. A data de efetivação é a data que completa a quitação da fatura.
7. Um pagamento parcial nunca é atribuído a lançamentos individuais.
8. Crédito de fatura pertence ao cartão e é transportado para os próximos ciclos desse cartão.
9. Estorno de compra já quitada gera crédito e não desfaz retroativamente o pagamento.
10. Uma compra parcelada compromete o valor total imediatamente.
11. A soma das parcelas deve ser exatamente igual ao valor total da compra.
12. Nenhuma parcela já efetivada pode ser alterada retroativamente.
13. Uma compra parcelada com parcela efetivada não pode ser excluída; deve utilizar estorno.
14. O CyberBank não antecipa parcelas nem calcula descontos de antecipação.
15. Juros, multas e encargos por atraso não são calculados automaticamente.
16. Fechar uma fatura não congela seus lançamentos.
17. Somente o titular pode fechar ou reabrir uma fatura.
18. Cartão desativado não aceita novos lançamentos, mas mantém histórico e parcelamentos existentes.
