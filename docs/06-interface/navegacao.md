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

## Estrutura de telas

| Tela | Responde |
|---|---|
| **Home** | Quanto tenho, quanto sobra, o que está pendente, como está a fatura |
| **Extrato** | Todo movimento do ambiente, filtrável por conta e por pendência |
| **Fatura** | O ciclo do cartão e as ações de fechar, pagar e abrir (só na última fechada) |
| **Reserva** | Contas, aplicações e a diferença entre fluxo de caixa e patrimônio |
| **Séries** | Os parcelamentos e as recorrências vivos, e o que muda ao alterar cada um |
| **Cadastro** | As árvores de categoria do usuário: criar, inativar, reativar e excluir |
| **Diário** (Fase 2) | O que aconteceu num dia: o que o sistema fez sozinho e o que a pessoa fez |

Ainda não existem: cadastro de contas e de meios; convite de pessoas para um ambiente;
perfil e sessões.

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

## Regras de leitura de número

- **Saldo realizado e projetado nunca aparecem juntos sem rótulo.** São perguntas
  diferentes ("quanto tenho" × "quanto sobra") e confundi-las é o pior erro possível
  numa tela de dinheiro.
- Todo lançamento previsto é marcado como tal na lista. Sem marca, o usuário lê uma
  parcela de dezembro como gasto de hoje.
- Aporte não aparece no relatório de gasto, mas aparece como **linha "guardado"** no
  fechamento do mês — senão o usuário procura o dinheiro que sumiu
  (`docs/06-interface/dashboard.md`).
