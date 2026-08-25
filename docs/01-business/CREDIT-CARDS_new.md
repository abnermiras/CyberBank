# Cartões de Crédito

## 1. Objetivo

Este documento define as regras de negócio do domínio de **Cartões de Crédito** do CyberBank.

O foco deste documento é o cartão de crédito como instrumento de utilização de um contrato de crédito: sua identidade, titularidade, relacionamento com o contrato, limite, formas de utilização e ciclo de vida operacional.

As regras específicas de **faturas, ciclos de faturamento, fechamento, pagamentos, quitação e créditos de fatura** serão definidas em documento próprio.

---

## 2. Conceito

Um cartão de crédito é um instrumento de pagamento vinculado a um contrato de crédito.

O contrato define a capacidade de crédito disponível e pode possuir um ou mais cartões vinculados.

O cartão não possui limite de crédito independente do contrato. Todos os cartões vinculados ao mesmo contrato compartilham o limite global definido para esse contrato.

---

## 3. Contrato de Crédito

O contrato de crédito é a unidade responsável pela disponibilização do limite de crédito.

O contrato possui:

- um titular;
- um ambiente financeiro de origem;
- uma entidade mantenedora;
- um limite global;
- um ou mais cartões vinculados.

O contrato pode existir independentemente de um banco estar cadastrado como conta no ambiente financeiro do titular.

### 3.1 Titularidade

O titular é o usuário responsável pelo contrato de crédito.

A titularidade do contrato não é transferida pela criação de cartões adicionais ou pelo compartilhamento de cartões com outros usuários.

---

## 4. Entidade Mantenedora

Todo contrato de cartão de crédito possui uma entidade mantenedora.

A entidade mantenedora representa a instituição responsável pelo contrato de crédito e pode ser:

- um banco;
- uma instituição financeira;
- uma empresa não bancária;
- outra entidade responsável pela emissão ou manutenção do crédito.

A entidade mantenedora não precisa necessariamente estar cadastrada como banco ou conta no ambiente financeiro do usuário.

### 4.1 Associação com Banco

Quando a entidade mantenedora também estiver cadastrada como banco no ambiente financeiro, o contrato poderá possuir uma associação com esse cadastro.

Essa associação representa uma referência entre o contrato de crédito e uma entidade bancária já conhecida pelo ambiente.

A ausência dessa associação não impede a existência ou utilização do cartão.

---

## 5. Cadastro do Cartão

Um cartão é criado dentro de um contrato de crédito existente ou durante a criação desse contrato, conforme as regras de cadastro definidas pelo sistema.

O cadastro do cartão deve permitir identificar o instrumento de pagamento sem armazenar o número completo do cartão.

Informações mínimas de identificação:

- nome ou descrição do cartão;
- últimos quatro dígitos;
- tipo do cartão;
- vínculo com o contrato de crédito.

O CyberBank não armazena o número completo do cartão.

---

## 6. Tipos de Cartão

Um contrato pode possuir diferentes cartões para finalidades distintas.

Os principais tipos considerados pelo domínio são:

- cartão principal;
- cartão virtual;
- cartão adicional;
- cartão compartilhado.

Esses tipos não representam contratos de crédito independentes.

Todos permanecem vinculados ao contrato original e utilizam seu limite global.

---

## 7. Cartão Principal

O cartão principal é o instrumento originalmente associado ao contrato de crédito.

O titular pode utilizar o cartão principal para realizar compras e demais operações permitidas pelo sistema.

O cartão principal utiliza o limite global do contrato.

---

## 8. Cartão Virtual

Um cartão virtual é um cartão adicionalmente criado dentro de um contrato de crédito para utilização em meios digitais ou outras finalidades definidas pelo titular.

O cartão virtual:

- não cria um novo contrato;
- não possui limite global próprio;
- compartilha o limite do contrato;
- possui identidade própria;
- pode ser ativado ou desativado independentemente de outros cartões do mesmo contrato.

As regras de faturamento dos lançamentos realizados pelo cartão virtual pertencem ao domínio de faturas.

---

## 9. Cartão Adicional

Um cartão adicional é emitido pelo titular do contrato para utilização por outro usuário.

O cartão adicional:

- continua vinculado ao contrato do titular;
- utiliza o limite global do contrato;
- não cria um novo contrato;
- não transfere a titularidade do contrato.

### 9.1 Recebimento

O usuário destinatário deve aceitar o cartão adicional antes de utilizá-lo.

Ao aceitar o cartão, o usuário escolhe em qual ambiente financeiro próprio o cartão ficará disponível, conforme as regras de compartilhamento e permissões do sistema.

### 9.2 Responsabilidade

O usuário que recebe o cartão adicional possui autorização para utilização do cartão, mas não se torna titular do contrato de crédito.

A responsabilidade pelo contrato e pelo limite global permanece com o titular.

---

## 10. Cartão Compartilhado

O compartilhamento permite que outro usuário utilize um cartão já existente sem criar um novo contrato de crédito.

O cartão compartilhado:

- permanece vinculado ao contrato original;
- mantém sua identidade original;
- utiliza o limite global do contrato;
- concede ao usuário autorizado apenas o direito de utilização definido pelo compartilhamento.

O compartilhamento não transfere a propriedade do cartão nem a titularidade do contrato.

---

## 11. Limite Global

O limite global pertence ao contrato de crédito e não a um cartão individual.

Todos os cartões vinculados ao contrato compartilham o mesmo limite.

Exemplo:

```text
Contrato
Limite global: R$ 50.000,00

Cartão principal:       utiliza o limite do contrato
Cartão virtual:         utiliza o limite do contrato
Cartão adicional:       utiliza o limite do contrato
Cartão compartilhado:   utiliza o limite do contrato
```

Não existe limite independente por cartão neste modelo.

### 11.1 Alteração do Limite

O titular do contrato pode alterar o limite global conforme as permissões do sistema.

A alteração do limite não altera retroativamente os lançamentos já realizados.

O limite global pode ser reduzido para valor inferior ao crédito atualmente comprometido.

Nesse caso, o sistema deve preservar o estado financeiro existente e permitir que a diferença seja representada como limite disponível negativo.

Exemplo:

```text
Limite global:       R$ 20.000,00
Crédito comprometido: R$ 25.000,00
Limite disponível:   -R$ 5.000,00
```

---

## 12. Consumo do Limite

Um lançamento realizado com cartão de crédito compromete o limite global do contrato no momento em que o lançamento é registrado.

O comprometimento do limite não depende de o lançamento estar marcado como `Previsto` ou `Realizado`.

O limite comprometido representa o valor de crédito atualmente utilizado pelo contrato e será liberado pelas regras financeiras aplicáveis ao lançamento.

As regras detalhadas de liberação relacionadas à quitação de faturas serão definidas no documento de faturas.

### 12.1 Compra à Vista

Uma compra à vista compromete o valor total da operação.

### 12.2 Compra Parcelada

Uma compra parcelada compromete inicialmente o valor total da operação, e não apenas o valor da primeira parcela.

Exemplo:

```text
Compra:        R$ 1.200,00
Parcelamento:  12 x R$ 100,00

Comprometimento inicial do limite:
R$ 1.200,00
```

A forma como parcelas futuras são apresentadas em faturas e a forma como o limite é liberado durante o ciclo financeiro pertencem ao domínio de faturas.

---

## 13. Cartão como Forma de Pagamento

O cartão de crédito pode ser utilizado como forma de pagamento em um lançamento de saída.

O cartão selecionado identifica qual instrumento de crédito foi utilizado na operação.

O lançamento financeiro permanece sendo uma única operação. O uso do cartão não cria uma segunda movimentação apenas por aparecer também na fatura.

O cartão deve ser apresentado ao usuário por informações de identificação seguras, como:

```text
Ultravioleta ****-1234
```

O número completo do cartão não deve ser exibido no sistema.

---

## 14. Ciclo de Vida do Cartão

O cartão possui um estado operacional que determina se ele pode ser utilizado para novas operações.

Estados mínimos do domínio:

- `Ativo`;
- `Desativado`.

### 14.1 Cartão Ativo

Um cartão ativo pode ser utilizado para novos lançamentos, desde que o usuário possua permissão para utilizá-lo.

### 14.2 Desativação

O titular do contrato pode desativar um cartão conforme as permissões do sistema.

A desativação impede novos lançamentos utilizando aquele cartão.

A desativação não deve:

- excluir o cartão;
- excluir lançamentos históricos;
- excluir faturas existentes;
- cancelar automaticamente obrigações financeiras existentes;
- remover o cartão do contrato;
- alterar retroativamente o histórico financeiro.

### 14.3 Cartão Desativado com Obrigações Existentes

A desativação de um cartão não encerra automaticamente as obrigações financeiras já criadas.

Parcelamentos existentes e demais compromissos financeiros continuam válidos conforme as regras do domínio de faturas.

### 14.4 Reativação

O titular pode reativar um cartão anteriormente desativado, quando permitido pelas regras do sistema.

A reativação:

- não cria um novo cartão;
- não cria um novo contrato;
- não altera o limite global;
- não elimina o histórico de desativação;
- não altera retroativamente lançamentos ou faturas existentes.

Após a reativação, o cartão volta a poder ser utilizado para novos lançamentos.

---

## 15. Permissões de Utilização

A posse do cartão e a autorização para utilização são conceitos distintos.

O titular do contrato possui controle sobre os cartões vinculados ao contrato, respeitando as regras de autorização do sistema.

Usuários de cartões adicionais e compartilhados possuem autorização de uso, mas não adquirem automaticamente os direitos do titular do contrato.

As permissões específicas para consultar ou operar faturas serão definidas no documento de faturas.

---

## 16. Histórico

O histórico financeiro do cartão deve ser preservado mesmo quando o cartão for desativado.

A desativação não autoriza a remoção física dos registros relacionados ao cartão.

Devem permanecer rastreáveis, quando aplicável:

- identidade do cartão;
- vínculo com o contrato;
- alterações de estado;
- lançamentos realizados;
- operações relacionadas ao cartão;
- referências a faturas.

---

## 17. Relação com Faturas

Cada cartão pode possuir faturas próprias.

A fatura representa o ciclo financeiro dos lançamentos realizados por aquele cartão.

A existência da fatura, sua abertura, fechamento, pagamento, quitação, crédito e demais regras de liquidação não são definidas neste documento.

Essas regras devem ser mantidas em:

`CREDIT-CARD-INVOICES.md`

A separação entre cartão e fatura é uma decisão de modelagem do domínio e deve ser preservada para evitar que regras de ciclo financeiro contaminem as regras estruturais do cartão.

---

## 18. Invariantes do Domínio do Cartão

As seguintes regras devem permanecer verdadeiras:

1. Um cartão pertence a um contrato de crédito.
2. O limite global pertence ao contrato, não ao cartão individual.
3. Cartões vinculados ao mesmo contrato compartilham o limite global.
4. Cartões adicionais e compartilhados não transferem a titularidade do contrato.
5. Cartões virtuais não criam novos contratos.
6. Um lançamento de cartão compromete o limite no momento de seu registro.
7. O sistema não utiliza o limite como validação impeditiva de um novo lançamento, salvo regra futura explicitamente definida para isso.
8. Desativar um cartão impede novos lançamentos, mas não elimina o histórico financeiro existente.
9. Desativar um cartão não encerra automaticamente parcelamentos ou outras obrigações existentes.
10. Reativar um cartão não cria novo contrato nem novo histórico financeiro.
11. O número completo do cartão não é armazenado nem exibido pelo CyberBank.
12. Regras de ciclo, fechamento, pagamento e quitação de faturas pertencem ao domínio de faturas e não devem ser duplicadas neste documento.
