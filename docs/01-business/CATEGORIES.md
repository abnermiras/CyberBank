# Categorias

### 1.1 Tipo da Categoria

Toda categoria deve possuir um único tipo:

- `Entrada`;
- `Saída`.

Uma categoria de `Entrada` somente pode ser utilizada em movimentações
de entrada.

Uma categoria de `Saída` somente pode ser utilizada em movimentações de
saída.

O usuário pode criar categorias de entrada e saída com o mesmo nome.

### 1.2 Subcategorias

A criação de subcategorias é opcional.

Uma categoria pode existir sem nenhuma subcategoria.

Quando uma categoria possuir subcategorias, uma movimentação pode utilizar
uma subcategoria para fornecer maior detalhamento da classificação.

### 1.3 Alteração e Desativação

O tipo de uma categoria não pode ser alterado após sua criação.

Uma categoria `Entrada` não pode ser transformada em `Saída`, e uma
categoria `Saída` não pode ser transformada em `Entrada`.

Para utilizar o mesmo nome com outro tipo, o usuário deve desativar ou
excluir logicamente a categoria existente e criar uma nova categoria com
o tipo desejado.

Uma categoria pode ser desativada mesmo possuindo movimentações
associadas.

A desativação impede que a categoria seja utilizada em novos lançamentos.

As movimentações históricas permanecem associadas à categoria
desativada e continuam apresentando sua classificação no histórico.

### 1.4 Desativação de Categoria com Subcategorias

Quando uma categoria é desativada, todas as suas subcategorias são
desativadas automaticamente.

A desativação impede que a categoria e suas subcategorias sejam utilizadas
em novos lançamentos.

Movimentações históricas permanecem associadas à categoria e às
subcategorias originalmente utilizadas.

### 1.5 Lançamentos Futuros

Quando uma categoria ou subcategoria utilizada em um lançamento futuro
for desativada, o lançamento futuro permanece existente.

A classificação do lançamento futuro é substituída pela categoria
sistêmica `Descategorizada`.

A categoria `Descategorizada` permite identificar que o lançamento futuro
precisa ser revisado e receber uma nova classificação pelo usuário.

O lançamento futuro não é excluído em razão da desativação da categoria.


---

## 2. Categorias Sistêmicas

O CyberBank pode possuir categorias de uso exclusivo do sistema.

Categorias sistêmicas são utilizadas para registrar movimentações
necessárias ao funcionamento do sistema sem depender de categorias
criadas pelo usuário.

Categorias sistêmicas não podem ser criadas, alteradas ou excluídas
pelos usuários.

### 2.1 Categoria Descategorizada

`Descategorizada` é uma categoria sistêmica utilizada pelo CyberBank
quando um lançamento futuro perde sua categoria original.

A categoria `Descategorizada`:

- pode ser visualizada pelo usuário;
- não pode ser alterada;
- não pode ser excluída;
- não pode ser desativada;
- não possui subcategorias.

Todos os lançamentos futuros que perderem sua categoria em razão da
desativação da categoria original serão classificados como
`Descategorizada`.

O usuário deve editar cada lançamento individualmente para selecionar uma
nova categoria e, opcionalmente, uma nova subcategoria.

A alteração da classificação não modifica o histórico de outras
movimentações.
---

## 3. Subcategorias

O usuário pode criar subcategorias para detalhar uma categoria em um
segundo nível.

Uma subcategoria pertence a uma categoria.

As subcategorias permitem maior detalhamento das receitas e despesas.

Em um ambiente compartilhado, usuários com permissão `Controle Total`
podem criar e alterar subcategorias.

### 3.1 Hierarquia

Uma subcategoria pertence obrigatoriamente a uma categoria.

A subcategoria não possui tipo próprio.

O tipo da subcategoria é determinado pela categoria à qual pertence.

Uma subcategoria de uma categoria `Entrada` representa uma classificação
de entrada.

Uma subcategoria de uma categoria `Saída` representa uma classificação
de saída.

### 3.2 Alteração de Categoria

Uma subcategoria não pode ser movida de uma categoria para outra.

Para utilizar a subcategoria em outra categoria, o usuário deve
desativar ou excluir logicamente a subcategoria existente e criar uma
nova subcategoria na categoria desejada.

A alteração da categoria pai não modifica o histórico das movimentações
já realizadas.

### 3.3 Desativação da Subcategoria

Uma subcategoria pode ser desativada mesmo possuindo movimentações
associadas.

A desativação impede que a subcategoria seja utilizada em novos
lançamentos.

As movimentações históricas permanecem associadas à subcategoria
desativada.

A subcategoria desativada pode ser reativada e voltar a ser utilizada em
novos lançamentos.

### 3.4 Desativação de Subcategoria

Uma subcategoria pode ser desativada independentemente da categoria à
qual pertence.

A desativação impede que a subcategoria seja utilizada em novos
lançamentos.

Movimentações históricas permanecem associadas à subcategoria
originalmente utilizada.

Quando uma subcategoria utilizada em um lançamento futuro é desativada,
o lançamento permanece associado à sua categoria, porém fica sem
subcategoria.

O usuário pode posteriormente editar o lançamento e selecionar uma nova
subcategoria, caso deseje.

## 4. Escopo das Categorias

Categorias e subcategorias são exclusivas do ambiente financeiro no qual
foram criadas.

Uma categoria criada em um ambiente não pode ser utilizada em outro
ambiente.

Categorias e subcategorias também pertencem ao usuário que as criou.

Usuários diferentes dentro do mesmo ambiente possuem suas próprias
categorias e subcategorias.

### 4.1 Compartilhamento do Ambiente

Quando um ambiente financeiro é compartilhado integralmente com outro
usuário, o usuário que recebeu acesso passa a visualizar as categorias e
subcategorias existentes no ambiente compartilhado.

O ambiente compartilhado é tratado como o ambiente do proprietário para
fins de utilização dos recursos.

O usuário que recebeu acesso pode utilizar as categorias e subcategorias
do proprietário ao realizar lançamentos, conforme suas permissões.

A movimentação permanece registrada no ambiente compartilhado e identifica
o usuário responsável pela realização do lançamento.

### 4.2 Exemplo

O usuário A possui o ambiente `CLT` e as categorias:

- Alimentação;
- Moradia;
- Transporte.

O usuário B possui o ambiente `CASA`.

O usuário A compartilha o ambiente `CLT` com o usuário B.

O usuário B passa a visualizar:

- `CASA`;
- `CLT` compartilhado.

Ao acessar o ambiente `CLT`, o usuário B pode utilizar os recursos
disponíveis no ambiente, incluindo as categorias e subcategorias do
usuário A, conforme sua permissão.

Se o usuário B realizar um lançamento utilizando:

- Banco do usuário A;
- Categoria do usuário A;
- Subcategoria do usuário A;

o lançamento permanece no ambiente `CLT` e registra o usuário B como
responsável pela operação.

### 4.3 Criação por Usuário com Acesso ao Ambiente

Quando um ambiente é compartilhado com `Controle Total`, o usuário que
recebeu acesso pode criar categorias e subcategorias dentro do ambiente.

A categoria ou subcategoria criada pertence ao proprietário do ambiente.

O usuário que realizou a criação é registrado como responsável pela
operação.

O proprietário do ambiente mantém controle sobre as categorias e
subcategorias criadas dentro de seu ambiente, inclusive podendo alterá-las
ou desativá-las posteriormente.

O compartilhamento não transfere a propriedade do ambiente ou de seus
recursos para o usuário que recebeu acesso.
