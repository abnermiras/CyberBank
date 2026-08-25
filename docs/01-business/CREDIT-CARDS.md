# Cartões de Crédito

## 1. Conceito

Um cartão de crédito representa um contrato de crédito criado pelo usuário
dentro de um ambiente financeiro.

O contrato de cartão de crédito pertence ao usuário que realizou sua criação
e ao ambiente financeiro no qual foi criado.

O cartão pode estar associado a um banco ou a uma empresa mantenedora que
não seja um banco.

O contrato possui um limite global de crédito.

O limite global pertence ao contrato e é compartilhado por todos os cartões
vinculados ao contrato.

---

## 2. Cadastro do Cartão de Crédito

O usuário pode criar um cartão de crédito dentro de seu ambiente
financeiro.

No momento da criação devem ser informados:

* nome do cartão;
* últimos quatro dígitos;
* limite global;
* empresa mantenedora;
* banco associado, quando aplicável;
* dia de vencimento da fatura;
* quantidade de dias anteriores ao vencimento utilizada para fechamento
  da fatura.

O limite global pode ser alterado posteriormente pelo titular do contrato.

Os últimos quatro dígitos são utilizados somente para identificação.

O CyberBank não armazena o número completo do cartão.

---

## 3. Empresa Mantenedora

Todo contrato de cartão de crédito possui uma empresa mantenedora.

A empresa mantenedora representa a instituição responsável pelo contrato de
crédito.

A empresa mantenedora pode ser:

* banco;
* instituição financeira;
* empresa não bancária;
* outra entidade responsável pelo contrato.

Exemplos:

* Nubank;
* Santander;
* Itaú;
* Banco Inter;
* Lojas Marisa;
* iFood.

A empresa mantenedora deve ser informada no cadastro do cartão.

---

## 4. Associação com Banco

Um cartão de crédito pode estar associado a um banco cadastrado no ambiente
financeiro.

A associação com banco é opcional.

### 4.1 Cartão Associado a Banco

Quando a empresa mantenedora for um banco e esse banco estiver cadastrado
no ambiente do usuário, o cartão pode ser associado ao banco existente.

Exemplo:

```text
Banco: Nubank
Cartão: Ultravioleta ****-1234
Limite: R$ 50.000,00
```

Durante o lançamento de uma saída, o usuário seleciona:

```text
Entidade de pagamento
└── Nubank

Forma de pagamento
└── Ultravioleta ****-1234
```

### 4.2 Banco não Cadastrado

Quando a empresa mantenedora for um banco que não estiver cadastrado no
ambiente, o usuário pode criar o cartão sem associá-lo a um banco
cadastrado.

A informação da empresa mantenedora permanece armazenada no contrato.

O cadastro do cartão não cria automaticamente uma conta bancária.

### 4.3 Empresa não Bancária

Um cartão pode ser mantido por uma empresa que não seja um banco.

Exemplo:

```text
Empresa mantenedora: Lojas Marisa
Banco associado: nenhum
Cartão: Cartão Marisa ****-1234
```

Durante o lançamento:

```text
Entidade de pagamento
└── Lojas Marisa

Forma de pagamento
└── Cartão Marisa ****-1234
```

A empresa mantenedora pode aparecer como entidade de pagamento mesmo não
sendo uma conta bancária.

---

## 5. Cartão como Forma de Pagamento

O cartão de crédito é uma forma de pagamento utilizada em lançamentos de
saída.

Durante a criação de uma saída, o usuário seleciona:

1. a entidade de pagamento;
2. a forma de pagamento disponível para a entidade.

Quando a entidade possuir cartões de crédito disponíveis no ambiente, os
cartões correspondentes aparecem como formas de pagamento.

A identificação do cartão deve apresentar:

* nome do cartão;
* últimos quatro dígitos.

Exemplo:

```text
Ultravioleta ****-1234
```

O número completo do cartão não é armazenado ou exibido.

---

## 6. Limite Global

Todo contrato de cartão de crédito possui um limite global.

O limite é definido pelo titular no momento da criação do contrato.

O titular pode alterar o limite posteriormente.

Todos os cartões vinculados ao contrato compartilham o mesmo limite global.

Isso inclui:

* cartão principal;
* cartões virtuais;
* cartões adicionais;
* cartões compartilhados.

Não existe limite independente por cartão.

### 6.1 Limite como Informação Financeira

O limite não é uma trava para novos lançamentos.

O CyberBank não impede uma nova compra quando o limite disponível for
insuficiente.

O sistema deve permitir que o limite disponível fique negativo.

O limite disponível é calculado considerando o limite global e o valor
comprometido pelo contrato.

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

## 7. Consumo do Limite

Um lançamento realizado com cartão de crédito consome o limite global do
contrato imediatamente.

O consumo ocorre no momento em que o lançamento é registrado.

O consumo independe da situação `Prevista` ou `Realizada` do lançamento.

O valor comprometido permanece associado ao contrato até que seja liberado
pelas regras de pagamento da fatura ou por exclusão ou estorno da compra.

---

## 8. Compra à Vista

Uma compra à vista compromete imediatamente o valor total da compra no
limite global do contrato.

Exemplo:

```text
Limite global:       R$ 50.000
Compra:              R$ 1.000
Limite comprometido: R$ 1.000
Limite disponível:   R$ 49.000
```

---

## 9. Compra Parcelada

Uma compra parcelada compromete imediatamente o valor total da compra no
limite global do contrato.

Exemplo:

```text
Compra: R$ 1.200
Parcelamento: 12 x R$ 100

Comprometimento inicial do limite:
R$ 1.200
```

O limite é liberado progressivamente conforme as parcelas forem quitadas.

Nesse exemplo, a cada parcela integralmente paga, R$ 100 do limite
comprometido é liberado.

O parcelamento não cria um limite separado por cartão.

---

## 10. Cartões Virtuais

O titular pode criar cartões virtuais vinculados ao contrato.

O cartão virtual não constitui um novo contrato.

O cartão virtual utiliza o limite global do contrato.

Cada cartão virtual possui sua própria identificação e sua própria fatura.

Os lançamentos realizados no cartão virtual seguem as mesmas regras de
lançamento, limite e faturamento dos demais cartões do contrato.

---

## 11. Cartões Adicionais

O titular do contrato pode criar cartões adicionais para outros usuários
do CyberBank.

O cartão adicional não constitui um novo contrato.

O cartão adicional permanece vinculado ao contrato do titular.

O cartão adicional utiliza o limite global do contrato.

Não existe limite individual para cartão adicional.

### 11.1 Recebimento

Após a criação, o cartão adicional é disponibilizado ao usuário
destinatário.

O usuário destinatário deve aceitar o recebimento.

Ao aceitar, o usuário escolhe em qual de seus ambientes financeiros o
cartão adicional será disponibilizado.

O cartão passa a ser uma forma de pagamento disponível naquele ambiente.

### 11.2 Propriedade

O usuário destinatário não se torna proprietário do contrato.

O titular continua sendo o proprietário do contrato e responsável por sua
estrutura de limite.

O usuário destinatário recebe apenas o direito de utilização do cartão
adicional.

### 11.3 Lançamentos

Os lançamentos realizados pelo usuário do cartão adicional:

* consomem o limite global do contrato;
* pertencem ao contrato do titular;
* são realizados pelo usuário destinatário;
* pertencem ao ambiente escolhido pelo destinatário;
* aparecem na fatura específica do cartão adicional.

---

## 12. Cartões Compartilhados

O titular pode compartilhar um cartão existente com outro usuário do
CyberBank.

O compartilhamento não cria um novo contrato.

O cartão continua pertencendo ao contrato original.

O usuário que recebe o compartilhamento deve aceitar o recebimento e
escolher em qual de seus ambientes o cartão será disponibilizado.

O cartão compartilhado passa a estar disponível como forma de pagamento
nesse ambiente.

O compartilhamento pode ser entendido como a concessão de utilização do
cartão a outro usuário, sem transferência de propriedade.

O cartão continua possuindo sua própria fatura.

Os lançamentos realizados pelo usuário compartilhado aparecem na fatura do
mesmo cartão.

---

## 13. Faturas

Cada cartão possui suas próprias faturas.

A fatura é vinculada a um cartão específico.

Cartões diferentes do mesmo contrato possuem faturas diferentes.

As faturas dos cartões pertencentes ao mesmo contrato ficam reunidas sob o
mesmo contrato para fins de responsabilidade e visualização do titular.

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

O titular do contrato consegue visualizar todas as faturas de todos os
cartões vinculados ao contrato.

---

## 14. Ciclo de Faturamento

O usuário define no cadastro do cartão:

* dia de vencimento;
* quantidade de dias anteriores ao vencimento para fechamento.

O CyberBank calcula automaticamente a data de fechamento da fatura.

As compras respeitam o ciclo de faturamento.

Um lançamento realizado antes do fechamento pertence à fatura atual.

Um lançamento realizado depois do fechamento pertence à próxima fatura.

---

## 15. Fechamento Automático

Quando o sistema identificar que chegou a data configurada para fechamento,
a fatura atual será fechada automaticamente.

Após o fechamento, uma nova fatura é aberta para o próximo ciclo.

Os lançamentos realizados posteriormente passam a pertencer à nova
fatura.

---

## 16. Fechamento Manual

O titular do contrato pode fechar manualmente a fatura a qualquer momento.

O fechamento manual produz o mesmo efeito do fechamento automático.

Os lançamentos registrados antes do fechamento permanecem na fatura que
está sendo fechada.

Os lançamentos registrados depois do fechamento passam para a próxima
fatura.

O sistema não deve impedir o usuário de realizar novos fechamentos ou
ajustes necessários ao controle da fatura.

---

## 17. Reabertura da Fatura

O titular do contrato pode reabrir uma fatura fechada.

Uma fatura reaberta pode receber novos lançamentos.

O titular pode novamente fechar a fatura após realizar os ajustes
necessários.

O sistema deve permitir a correção operacional da fatura sem criar
restrições artificiais que impeçam o usuário de ajustar seu controle
financeiro.

O pagamento parcial ou integral já realizado deve continuar registrado.

A situação da fatura será determinada pela relação entre o valor devido e
os pagamentos realizados.

---

## 18. Lançamentos na Fatura

Quando uma saída é criada utilizando um cartão de crédito:

1. o lançamento aparece no extrato de lançamentos;
2. o lançamento aparece na fatura do cartão utilizado;
3. o valor compromete o limite global do contrato.

O extrato e a fatura representam visões diferentes do mesmo lançamento.

O lançamento não é duplicado.

---

## 19. Datas do Lançamento

Uma movimentação realizada com cartão possui:

* **Data de lançamento:** data em que o item foi registrado no CyberBank;
* **Data de efetivação:** data de vencimento da fatura à qual o lançamento
  pertence.

A data de lançamento representa o registro da operação.

A data de efetivação representa o vencimento da fatura.

---

## 20. Situação dos Lançamentos

Os lançamentos de cartão de crédito permanecem `Previstos` até que a
fatura correspondente seja integralmente paga.

O fechamento da fatura não altera a situação do lançamento.

O pagamento parcial da fatura não altera a situação dos lançamentos.

Somente o pagamento integral da fatura permite que os lançamentos
correspondentes passem para `Realizados`.

---

## 21. Pagamento da Fatura

Uma fatura fechada pode ser paga utilizando qualquer banco cadastrado no
ambiente financeiro.

O usuário seleciona:

1. o banco ou conta que realizará o pagamento;
2. a forma de pagamento disponível para essa conta.

O pagamento da fatura não pode utilizar outro cartão de crédito.

O pagamento gera os efeitos financeiros correspondentes nos extratos e
saldos da conta utilizada.

O pagamento também atualiza o valor comprometido do contrato e o limite
disponível.

### 21.1 Pagamento Parcial

O pagamento parcial é permitido.

Quando ocorre um pagamento parcial:

* o valor pago é registrado;
* o valor devido da fatura é reduzido;
* a fatura permanece não quitada;
* os lançamentos permanecem `Previstos`;
* o limite correspondente permanece comprometido.

Pagamento parcial não libera limite.

### 21.2 Pagamento Integral

Quando o total pago atingir o valor devido da fatura:

* a fatura é quitada;
* os lançamentos da fatura passam para `Realizados`;
* o limite correspondente é liberado;
* os saldos das contas são atualizados conforme o pagamento realizado.

  ## 21.3 Quitação da Fatura

A quitação financeira ocorre no nível da fatura.

Um pagamento parcial não determina quais lançamentos individuais foram
pagos.

Enquanto a fatura não estiver integralmente quitada:

* a fatura permanece pendente;
* todos os lançamentos da fatura permanecem `Previstos`;
* todo o valor comprometido pelos lançamentos permanece comprometendo o
  limite global do contrato;
* os pagamentos parciais são apenas acumulados no histórico financeiro da
  fatura.

O CyberBank não distribui pagamentos parciais entre os lançamentos
individuais da fatura.

### 21.3.1 Exemplo

Uma fatura possui dez lançamentos de R$ 10,00:

```text
Total da fatura: R$ 100,00
```

O usuário realiza um pagamento parcial de R$ 50,00.

O resultado é:

```text
Valor da fatura:       R$ 100,00
Valor pago:             R$ 50,00
Valor pendente:         R$ 50,00
Situação da fatura:     Pendente
```

Todos os dez lançamentos permanecem `Previstos`.

Nenhum lançamento individual é considerado quitado.

Os R$ 100,00 continuam comprometendo o limite global do contrato.

Quando o usuário realizar um novo pagamento de R$ 50,00:

```text
Valor da fatura:       R$ 100,00
Valor pago acumulado:  R$ 100,00
Valor pendente:          R$ 0,00
Situação da fatura:     Quitada
```

Nesse momento, todos os lançamentos da fatura são efetivados.

### 21.3.2 Data de Efetivação

Quando uma fatura é quitada, todos os lançamentos pertencentes à fatura
passam para `Realizados` na data do pagamento que completou o valor total
da fatura.

A data de efetivação dos lançamentos corresponde, portanto, à data em que
a fatura foi integralmente quitada.

Exemplo:

```text
Data da compra:          05/08
Vencimento da fatura:    18/08
Pagamento parcial:       18/08
Pagamento final:         25/08
```

O lançamento permanecerá:

```text
Data de lançamento:      05/08
Situação:                 Prevista
```

até que o pagamento final seja realizado.

Após o pagamento de 25/08:

```text
Data de lançamento:      05/08
Data de efetivação:      25/08
Situação:                 Realizada
```

A data original de vencimento da fatura não determina a data de efetivação
quando a fatura não foi quitada naquele vencimento.

### 21.3.3 Liberação do Limite

O limite comprometido pelos lançamentos de uma fatura somente é liberado
quando a fatura estiver integralmente quitada.

Pagamentos parciais não liberam limite.

Quando a fatura for quitada, o valor correspondente aos lançamentos
quitados deixa de comprometer o limite global do contrato.

Em compras parceladas, somente o valor das parcelas efetivamente quitadas
na fatura é liberado conforme as regras de parcelamento.


---

## 22. Pagamento por Usuário de Cartão Adicional

O usuário que recebeu um cartão adicional pode visualizar a fatura
correspondente ao cartão.

Esse usuário pode realizar o pagamento da fatura.

O pagamento pode ser parcial ou integral.

O usuário seleciona qualquer banco ou conta disponível em seu ambiente e
uma forma de pagamento permitida.

Quando o pagamento atingir o valor total devido da fatura:

* a fatura é quitada;
* os lançamentos correspondentes passam para `Realizados`;
* o limite correspondente é liberado.

---

## 23. Pagamento por Usuário de Cartão Compartilhado

O usuário que recebeu um cartão compartilhado pode visualizar a fatura
daquele cartão.

O usuário pode realizar pagamentos parciais ou integrais.

O pagamento pode ser realizado utilizando uma conta disponível em seu
ambiente.

O pagamento integral quita a fatura e libera o limite correspondente.

---

## 24. Visibilidade das Faturas

### 24.1 Titular do Contrato

O titular consegue visualizar:

* todas as faturas do contrato;
* todas as faturas de todos os cartões;
* todos os lançamentos das faturas;
* lançamentos realizados pelo próprio titular;
* lançamentos realizados por usuários adicionais;
* lançamentos realizados por usuários com cartões compartilhados.

### 24.2 Usuário de Cartão Adicional

O usuário que recebeu um cartão adicional pode visualizar a fatura
específica daquele cartão.

O usuário visualiza somente os lançamentos realizados por ele próprio.

O usuário não visualiza os lançamentos realizados pelo titular ou por
outros usuários.

O usuário não possui acesso às demais faturas do contrato.

### 24.3 Usuário de Cartão Compartilhado

O usuário que recebeu acesso compartilhado a um cartão pode visualizar a
fatura daquele cartão.

O usuário visualiza somente os lançamentos realizados por ele próprio.

O usuário não visualiza os lançamentos realizados pelo titular ou por
outros usuários.

O usuário não possui acesso às demais faturas do contrato.

---

## 25. Fechamento e Reabertura — Permissões

Somente o titular do contrato pode:

* fechar uma fatura;
* reabrir uma fatura.

Usuários que receberam cartões adicionais ou compartilhados não podem
fechar ou reabrir faturas.

---

## 26. Estorno

O estorno de uma compra de cartão deve representar o comportamento de um
cartão de crédito real.

O estorno é registrado como um valor positivo na fatura.

O estorno reduz o valor devido da fatura.

O estorno libera o limite correspondente ao valor estornado.

O estorno não representa uma nova compra.

### 26.1 Estorno em Fatura Aberta

Quando uma compra for estornada enquanto a fatura estiver aberta, o valor
do estorno é registrado na própria fatura e reduz seu valor devido.

O limite correspondente é liberado.

### 26.2 Estorno em Fatura Fechada

Uma compra pertencente a uma fatura fechada também pode ser estornada.

O valor positivo do estorno é registrado na fatura correspondente.

O valor devido da fatura é recalculado.

O limite correspondente é liberado.

As regras de estorno após o pagamento integral da fatura seguem o mesmo
princípio financeiro: o valor devolvido deve ser reconhecido e o limite
correspondente deve ser ajustado.

---

## 27. Exclusão de Compra Parcelada

Uma compra parcelada representa uma única operação financeira composta por
múltiplas ocorrências de parcelas.

A exclusão da compra parcelada deve considerar o estado das parcelas que
compõem o parcelamento.

### 27.1 Parcelamento sem Parcelas Quitadas

Quando nenhuma parcela do parcelamento estiver em uma fatura quitada, o
usuário pode excluir a compra parcelada.

A exclusão:

* remove o parcelamento do controle financeiro ativo;
* exclui logicamente todas as parcelas ainda existentes;
* remove as parcelas das faturas correspondentes;
* libera o limite global comprometido pelo parcelamento;
* recalcula os valores das faturas afetadas;
* remove o gasto do Mapa de Lançamentos;
* preserva o histórico da operação para fins de auditoria.

Exemplo:

```text id="f6wx8j"
Compra: R$ 1.200
Parcelamento: 12x de R$ 100

Parcelas quitadas: 0
Parcelas pendentes: 12
```

O usuário pode excluir o parcelamento.

Após a exclusão:

```text id="v2p2h9"
Parcelas futuras: removidas
Limite comprometido: -R$ 1.200
Faturas futuras: recalculadas
Mapa de Lançamentos: compra removida
```

### 27.2 Parcelamento com Parcelas Quitadas

Quando pelo menos uma parcela do parcelamento estiver em uma fatura
quitada, a compra parcelada não pode ser excluída.

O CyberBank deve impedir a exclusão e orientar o usuário a utilizar o
processo de estorno.

A existência de qualquer parcela já quitada significa que parte da
operação financeira já foi efetivada.

Nesse cenário, a operação original deve permanecer preservada no histórico.

---

## 28. Estorno de Compra Parcelada

O estorno de uma compra parcelada deve considerar o valor total que ainda
falta ser efetivado dentro do parcelamento.

O estorno não altera retroativamente as parcelas que já foram quitadas.

O CyberBank deve identificar:

* valor original do parcelamento;
* valor das parcelas já quitadas;
* valor das parcelas ainda não quitadas.

O valor restante do parcelamento representa o valor que deverá ser
estornado.

### 28.1 Exemplo

Uma compra de:

```text id="7k4yqd"
R$ 1.200
12x de R$ 100
```

possui:

```text id="n7ynqf"
Parcelas quitadas:       4
Valor já quitado:        R$ 400

Parcelas restantes:      8
Valor restante:           R$ 800
```

O estorno será de:

```text id="py9c0r"
R$ 800
```

As quatro parcelas já quitadas permanecem no histórico.

As parcelas ainda não quitadas são encerradas pelo processo de estorno.

### 28.2 Lançamento Positivo do Estorno

O estorno gera um lançamento positivo na fatura correspondente.

O valor do lançamento positivo corresponde ao valor restante do
parcelamento.

Esse lançamento reduz o valor devido da fatura e representa o crédito
concedido pelo estorno.

O estorno também libera o limite correspondente ao valor restante do
parcelamento.

### 28.3 Limite

O valor liberado pelo estorno corresponde ao valor que ainda estava
comprometido pelo parcelamento.

No exemplo de R$ 1.200 em 12 parcelas:

```text id="h7ozl2"
Valor original:             R$ 1.200
Valor já quitado:           R$   400
Valor ainda comprometido:   R$   800
```

O estorno libera:

```text id="u9ubc4"
R$ 800
```

Os R$ 400 já quitados não retornam ao limite porque já foram efetivamente
pagos.

### 28.4 Faturas

As parcelas ainda não quitadas deixam de compor as faturas futuras.

O lançamento positivo do estorno é registrado na fatura conforme as regras
de faturamento do cartão.

O valor devido das faturas afetadas é recalculado.

### 28.5 Mapa de Lançamentos

Uma compra parcelada estornada deixa de aparecer no Mapa de Lançamentos.

O objetivo é evitar que uma despesa que não representa mais uma obrigação
financeira ativa continue aparecendo como gasto no mapa.

A retirada do Mapa de Lançamentos não representa exclusão física do
registro.

A compra original permanece preservada no histórico financeiro.

O lançamento positivo do estorno permanece registrado para representar a
operação financeira realizada.


---

## 29. Crédito de Fatura

Uma fatura pode possuir saldo credor.

O saldo credor ocorre quando o valor dos créditos registrados na fatura
for superior ao valor das obrigações financeiras existentes na fatura.

O crédito não é perdido e não é devolvido automaticamente ao usuário.

O saldo credor é transportado para a próxima fatura do mesmo cartão.

### 29.1 Pagamento Superior ao Valor da Fatura

O CyberBank permite que o valor total dos pagamentos realizados seja
superior ao valor devido da fatura.

Exemplo:

```text id="c1ly4n"
Valor devido:       R$ 1.000
Pagamento:          R$ 1.200
```

Resultado:

```text id="5g3x4j"
Fatura:
Valor devido:       R$ 1.000
Total pago:         R$ 1.200
Saldo credor:       R$   200
```

A fatura é considerada quitada.

Os lançamentos pertencentes à fatura passam para `Realizados`.

O valor excedente de R$ 200 permanece como crédito do cartão.

### 29.2 Crédito na Próxima Fatura

O saldo credor de uma fatura é transportado para a próxima fatura do mesmo
cartão.

Exemplo:

```text id="7wwp3w"
Crédito da fatura anterior: R$ 200
Nova fatura:                R$ 800
```

O valor líquido devido será:

```text id="rb5ljo"
R$ 800 - R$ 200 = R$ 600
```

O crédito pertence ao cartão e não ao contrato de forma independente.

O crédito não pode ser transferido para outro cartão do mesmo contrato.

### 29.3 Crédito Proveniente de Estorno

Um estorno pode gerar crédito superior ao valor devido da fatura atual.

Nesse caso, o valor excedente permanece como saldo credor do cartão e é
transportado para a próxima fatura.

Exemplo:

```text id="l7j4h5"
Fatura atual:
Obrigações:          R$ 300

Estorno:
R$ 800

Crédito resultante:
R$ 500
```

A fatura atual é encerrada com saldo credor de R$ 500.

A próxima fatura do mesmo cartão receberá esse crédito.

### 29.4 Estorno de Compra Parcelada

No estorno de uma compra parcelada, o valor total ainda não efetivado do
parcelamento é creditado de uma única vez.

O valor do estorno não é dividido entre as parcelas futuras.

Exemplo:

```text id="j91q3s"
Compra original:          R$ 1.200
Parcelamento:             12x R$ 100

Parcelas já quitadas:      4
Valor já quitado:          R$ 400

Valor restante:             R$ 800
```

O estorno gera um crédito de:

```text id="3r3d0n"
R$ 800
```

Esse crédito é lançado de uma única vez.

As parcelas futuras deixam de existir como obrigações financeiras.

Se o valor do estorno for superior às obrigações existentes na fatura
atual, o excedente permanece como crédito e será transportado para as
próximas faturas.

### 29.5 Crédito e Limite

O crédito de fatura e o limite de crédito são conceitos distintos.

O crédito de fatura representa um valor já pago ou creditado em favor do
titular.

O limite representa o valor de crédito disponibilizado pelo contrato.

O crédito gerado por pagamento excedente ou estorno não altera o limite
global configurado do contrato.

O crédito deve ser considerado no cálculo do valor líquido devido das
faturas futuras.
## 30. Encargos por Atraso

O CyberBank não calcula automaticamente juros, multas ou outros encargos
relacionados ao atraso no pagamento de uma fatura.

As regras de encargos podem variar de acordo com a instituição mantenedora
do cartão e com o contrato de cada usuário.

O CyberBank não reproduz essas regras específicas.

### 30.1 Registro Manual

Quando houver juros, multa ou outro encargo relacionado ao cartão, o usuário
deve registrar o valor manualmente no CyberBank.

O lançamento deve ser tratado como uma nova movimentação financeira.

O usuário poderá utilizar suas categorias e subcategorias para identificar
a natureza do encargo.

Exemplos:

* `Juros de Cartão`;
* `Multa de Cartão`;
* `Encargos Financeiros`.

### 30.2 Fatura

Quando o encargo estiver relacionado a uma fatura, o usuário poderá
registrá-lo como lançamento associado ao cartão e à fatura correspondente.

O valor registrado passa a fazer parte do controle financeiro da fatura
conforme as regras gerais de lançamentos de cartão de crédito.

O CyberBank não calcula automaticamente o valor do encargo nem determina
quando ele deve ser aplicado.

### 30.3 Responsabilidade pelo Valor

O valor do encargo informado no CyberBank é de responsabilidade do usuário.

O sistema registra e controla o valor informado, mas não valida se o valor
corresponde às regras comerciais da instituição mantenedora do cartão.


## 31. Histórico

Operações relacionadas ao cartão não devem remover permanentemente
informações financeiras que possuam histórico.

Devem permanecer disponíveis para consulta:

* cartões desativados;
* faturas;
* lançamentos;
* parcelas;
* pagamentos;
* estornos;
* alterações de limite;
* demais registros financeiros relacionados ao contrato.

A exclusão de um lançamento ou a desativação de um cartão não elimina o
histórico financeiro.
