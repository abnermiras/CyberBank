---
id: 06-interface/navegacao
titulo: Navegação e ações globais
dono: estrutura de navegacao, onde o ambiente vive na tela e como se lanca
ler-junto: [02-dominio/ambiente-financeiro, 06-interface/dashboard, 06-interface/direcao-visual]
status: rascunho
---

# Navegação e ações globais

Decisões tomadas no protótipo (`prototipo/`), validadas navegando. Elas são caras de
mudar depois porque moldam todas as telas.

## O ambiente vive no header

O seletor de **ambiente financeiro** fica fixo no topo, sempre visível, com o nome e a
cor do ambiente ativo. Trocar é um clique e a tela inteira recarrega no novo contexto.

Não é enfeite: toda consulta do sistema é filtrada por ambiente
(`docs/02-dominio/ambiente-financeiro.md`). Se a tela deixar dúvida sobre em qual
ambiente o usuário está, ele vai lançar no lugar errado — e o erro só aparece no
fechamento do mês, quando já tem uma semana de dado torto.

| Regra | Motivo |
|---|---|
| O ambiente ativo aparece em **toda** tela, sem exceção | Contexto invisível é contexto esquecido |
| Cada ambiente tem uma **cor** própria, aplicada ao chip | Reconhecimento antes da leitura |
| Trocar de ambiente **limpa filtros e seleções** da tela anterior | Filtro herdado de outro contexto é resultado errado com cara de certo |
| Nunca existe tela que some dados de dois ambientes | O isolamento é do modelo, não só do banco |

**Como a troca funciona.** O chip `AMBIENTE ATIVO` é o botão: abre a lista dos ambientes da
pessoa (relida a cada abertura, para o recém-criado aparecer), com o atual marcado e um rodapé
que leva ao Perfil, onde se cria e se renomeia (`docs/06-interface/perfil.md`). Escolher outro
**recarrega a página**, e é isso que cumpre o *"limpa filtros e seleções"* sem que cada tela
precise saber limpar o próprio estado. A tela em que a pessoa estava se mantém; o **argumento**
dela não — `#/extrato/88` vira `#/extrato`, porque o lançamento 88 é do ambiente anterior.

**A escolha fica no navegador**, e só nele: o servidor não guarda "último ambiente usado", e
cada rota já carrega o `{ambienteId}` no caminho. Guardado que não está mais na lista da pessoa
(outro usuário no mesmo navegador, acesso removido) cai no primeiro da lista, o *Ambiente
Pessoal*. Preço aceito: em outro aparelho, a pessoa começa no Pessoal.

> ☐ **A definir:** a **cor própria** de cada ambiente. O modelo não tem esse campo; até ele
> existir, todo chip usa o ciano padrão. Pede coluna em `ambiente` e escolher de qual paleta
> ela sai (`docs/06-interface/direcao-visual.md`).

## Lançar: dois caminhos, um deles é o padrão

**Quick-add** é o caminho principal. Um botão presente em qualquer tela (e a tecla `N`)
abre um painel curto: valor, o que foi, meio, categoria opcional. É desenhado para o caso
que acontece 30 vezes por mês — um café, um mercado — e some da frente em dois segundos.

Categoria é **opcional** no quick-add. Sem ela o lançamento entra e vai para a fila de
pendências. Isso é a regra do glossário virando tela: o sistema aceita exigir a
categorização, nunca a digitação.

**Formulário completo** é a saída do quick-add, não um caminho paralelo — o botão
"abrir formulário completo" leva de um para o outro. Ele existe para o que o painel curto
não comporta: parcelamento, transferência, aporte, boleto com vencimento, e as duas datas.

| Caso | Caminho |
|---|---|
| Gasto ou receita à vista | Quick-add |
| Compra no crédito parcelada | Completo |
| Transferência, aporte, resgate | Completo |
| Boleto com vencimento futuro | Completo |
| Corrigir categoria de uma pendência | Direto na lista de pendências, sem abrir formulário |

## O formulário completo explica o que vai fazer

Antes de registrar, o formulário mostra uma linha dizendo **o que o modelo fará**: em qual
fatura a compra vai cair, qual será a data de efeito, que uma transferência cria dois
lançamentos e não entra no relatório de gasto.

Parece detalhe e não é. As regras do Cyberbank — duas datas, previsto e realizado, aporte
que não é gasto — são invisíveis num formulário comum, e o usuário só descobre que
entendeu errado quando o saldo não bate. A linha de explicação é o que transforma a regra
em algo aprendível.

### As abas do formulário completo

**GASTO · RECEITA · TRANSFERÊNCIA · CRÉDITO**. A de crédito **só existe quando funciona**: ela
fica desabilitada enquanto não houver uma conta `CARTAO` com pelo menos um cartão, e a linha ao
lado diz o que falta. É a regra do *botão que só existe quando funciona* aplicada a uma aba.

**GASTO e RECEITA não oferecem cartão de crédito, e CRÉDITO só oferece cartão.** As abas
existem para a pessoa ver o que vai acontecer, e crédito acontece diferente: a compra cai na
**fatura `ABERTA`** — pelo status, nunca pela data —, nasce `PROVISIONADO`, e não tem campo de
vencimento, porque no crédito as duas datas são a mesma
(`docs/02-dominio/fatura-cartao.md`).

**Não há aba de boleto**, e a ausência é a regra aparecendo no lugar certo: boleto é um
**meio**, e é o meio que decide se existem duas datas (`docs/02-dominio/meio-de-pagamento.md`).
O campo de vencimento aparece sozinho quando o meio escolhido separa as duas datas. Uma aba de
boleto duplicaria na tela uma decisão que o meio já carrega — e no dia em que entrar outro meio
com vencimento futuro, ela estaria errada calada.

**A aba desabilitada diz o que falta**, em vez de ficar cinza e muda. É o mesmo tratamento que
as telas em espera recebem: tela — ou aba — que existe sem funcionar precisa dizer o que está
esperando, senão ensina que o app está quebrado.

## Estrutura de telas

| Tela | Responde |
|---|---|
| **Home** | Quanto tenho, quanto sobra, o que está pendente, como está a fatura |
| **Extrato** | Todo movimento do ambiente, filtrável por conta e por pendência. A linha abre o **detalhe** do lançamento (`docs/06-interface/extrato.md`) |
| **Fatura** | O ciclo do cartão e as ações de fechar, pagar e abrir (só na última fechada) — `docs/06-interface/fatura.md` |
| **Reserva** | Contas, aplicações e a diferença entre fluxo de caixa e patrimônio |
| **Séries** | Os parcelamentos e as recorrências vivos, e o que cada série ainda vai cobrar — `docs/06-interface/series.md` |
| **Cadastro** | O que precisa existir antes de lançar, em três abas: **categorias**, **contas e meios** e **cartão de crédito**. O cartão tem aba própria porque é a única conta com ciclo, limite e fatura — junto das outras, o formulário mudava de forma conforme o tipo escolhido |
| **Diário** | O que aconteceu num dia: o que o sistema fez sozinho e o que a pessoa fez |
| **Perfil** | Quem eu sou no sistema: avatar, nome, senha, Telegram e os convites |

**O Perfil não entra no rail, e a porta dele é o avatar no canto superior direito** — ele é a
única tela que não é sobre dinheiro. Como ele se organiza por dentro é de
`docs/06-interface/perfil.md`.

Ainda não existem: o convite de pessoas para um ambiente e a lista de sessões. Os dois têm
**espaço reservado no Perfil**, dizendo o que esperam.

## O Diário responde "por que meu saldo mudou?"

A tela do Diário (`docs/02-dominio/evento.md`) existe porque o sistema mexe no dinheiro
sozinho — a fatura fecha, o previsto vira realizado, o que não foi pago rola. Toda tela do
app mostra **como as coisas estão**; o Diário é a única que mostra **o que aconteceu**.

- **Um dia por vez, e o padrão é hoje.** Não é uma lista infinita: a pergunta é sobre um dia.
- **O seletor de dia anda para trás livremente.** Para frente do dia corrente **não há
  Diário** — o que ainda não aconteceu está no previsto, e são duas perguntas diferentes.
- **Duas seções: o que o sistema fez e o que você fez.** A primeira vem primeiro: é a que o
  usuário não tem como saber sozinho. Num ambiente compartilhado, a segunda diz **quem**.
- **Dia sem evento é resposta, não erro.** A tela diz que o dia não teve movimento, com o
  seletor no lugar — nada de tela em branco que parece quebrada.
- **Cada linha leva ao objeto.** Clicar em "fatura 2026-08 fechada" abre aquela fatura. O
  Diário é uma porta, não um relatório morto.
- **A frase é montada na tela, nunca guardada** (`docs/02-dominio/evento.md`) — senão o texto
  congela na redação do dia em que foi escrito.
- **Transferência não leva sinal no valor.** O par soma zero, e um `+` ali afirmaria que entrou
  dinheiro na vida do usuário — que é exatamente o erro que o `lancamento.md` passa o tempo
  todo evitando. Gasto e receita levam sinal e cor; transferência leva só o número.
- **O evento de correção nomeia o lançamento pelo nome ANTIGO**, e depois mostra o de/para. É o
  nome que a pessoa reconhece: ela está procurando o que mexeu, não o que resultou.

**O link de um lançamento leva ao lançamento**, e não mais à tela dele: `#/extrato/88` abre o
Extrato com o detalhe aberto (`docs/06-interface/extrato.md`). **Conta, meio e categoria
continuam levando à tela do alvo** — nenhum dos três tem detalhe próprio, e inventar um para
fechar a simetria seria tela que ninguém pediu. O evento de exclusão não leva link nenhum: o
alvo não existe mais, e é por isso que ele carrega a linha inteira nos `dados`.

## O seletor de categoria tem duas formas, e a diferença é o espaço

A árvore tem dois níveis, e o seletor precisa mostrar os dois sem mentir sobre qual é o
destino. As duas telas resolvem isso de formas diferentes **de propósito**:

- **No formulário completo, são dois campos**: categoria e subcategoria, e o segundo só
  aparece quando a raiz escolhida deixou de ser destino. O campo separado é o que deixa claro
  que a escolha ainda não terminou.
- **No quick-add, é um campo só**, com `optgroup`: a raiz que **é** o destino aparece como
  opção solta, e a raiz que tem filhas vira **cabeçalho do grupo**, com as filhas dentro. O
  painel curto não tem espaço para um segundo campo que aparece e some, e o cabeçalho diz a
  que família a subcategoria pertence sem repetir o nome dela em cada linha.

**Achatar a árvore em `Raiz › Filha` foi tentado e é pior**: o nome da raiz se repete em cada
filha, e numa raiz com cinco filhas a lista vira cinco linhas que começam iguais — o olho
perde exatamente a parte que distingue uma da outra.

**Quando não há nenhuma categoria do sentido escolhido, a tela diz isso** em vez de mostrar um
seletor com uma opção só. Sem a frase, "— sem categoria —" parece defeito; com ela, o usuário
sabe que pode lançar assim mesmo e resolver na fila de pendências.

## Dado inativo fica escondido, e a tela diz que escondeu

Inativar é como o usuário tira do caminho o que não usa mais — categoria, e um dia conta e
meio. Se a tela continuar mostrando tudo, inativar não resolve o problema que existe para
resolver: a poluição.

A regra tem três partes, e as três importam:

1. **Escondido é o padrão.** Ao abrir a tela, o inativo não aparece.
2. **Existe um interruptor para revelar**, na própria tela, sem entrar em configuração
   nenhuma. Ele afeta só o que se está olhando e não é preferência guardada — inativar é
   raro, e revelar é mais raro ainda.
3. **A contagem sempre inclui o escondido, e diz que ele existe.** Uma tela que mostra
   "3 subcategorias" quando existem 4 está mentindo. Ela mostra "3 de 4" ou "3 · 1 inativa",
   e é essa diferença que faz o usuário lembrar de onde foi parar a categoria que ele
   procura.

O item 3 é o que separa "escondido" de "sumido". Escondido é reversível e visível como
ausência; sumido é o usuário achando que perdeu o dado.

**Nada de esconder pelo tempo.** O sistema não infere desuso e não recolhe nada sozinho
(`docs/02-dominio/categoria.md`): só está escondido o que o usuário inativou.

## Campo de data: digitar e apontar, nos dois

Data é o campo que mais se repete no sistema, e o controle nativo de data faz **uma** das duas
coisas bem. Então o campo é um par:

- **Texto**, para digitar e **colar**. O nativo de data não aceita colar, e colar uma data de
  outro lugar é o caminho mais comum quando se está transcrevendo um extrato.
- **Botão de calendário ao lado**, que abre um grid próprio, desenhado no CSS do projeto,
  para escolher no mouse. O grid nativo do navegador é pintado pelo sistema operacional e
  não aceita tema — num app que é todo cyberpunk, ele abria como uma janela cinza do Windows.
  O grid próprio navega mês a mês, marca o dia de hoje na borda ciano, o dia escolhido em
  ácido, e tem um atalho `HOJE` no rodapé. Fecha no `Esc`, no clique fora e ao rolar a tela.

**O campo é tolerante na entrada e rígido na saída.** Aceita `14/09/2026`, `14-09-2026`,
`14092026`, `140926`, `1409` (ano corrente), `2026-09-14` e os atalhos `hoje`, `ontem`,
`anteontem` e `amanhã`; ao sair do campo, normaliza tudo para `dd/mm/aaaa`. Data impossível
— `31/02` — fica marcada em rosa e o envio é barrado com o motivo, em vez de ser corrigida em
silêncio (regra 7 do `CLAUDE.md`).

**Por que o clique no campo não abre o calendário:** ele abriria por cima do teclado, e quem
digita perderia o caminho mais rápido. O grid fica a um clique, no botão — e quem prefere o
mouse nunca toca no texto.

## Regras de leitura de número

- **Saldo realizado e projetado nunca aparecem juntos sem rótulo.** São perguntas
  diferentes ("quanto tenho" × "quanto sobra") e confundi-las é o pior erro possível
  numa tela de dinheiro.
- Todo lançamento previsto é marcado como tal na lista. Sem marca, o usuário lê uma
  parcela de dezembro como gasto de hoje.
- Aporte não aparece no relatório de gasto, mas aparece como **linha "guardado"** no
  fechamento do mês — senão o usuário procura o dinheiro que sumiu
  (`docs/06-interface/dashboard.md`).
