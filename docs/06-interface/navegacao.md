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
| **Fatura** | O ciclo do cartão e as ações de fechar, pagar e reabrir |
| **Reserva** | Contas, aplicações e a diferença entre fluxo de caixa e patrimônio |

Ainda não existem: cadastro de contas, categorias e meios; convite de pessoas para um
ambiente; perfil e sessões.

## Regras de leitura de número

- **Saldo realizado e projetado nunca aparecem juntos sem rótulo.** São perguntas
  diferentes ("quanto tenho" × "quanto sobra") e confundi-las é o pior erro possível
  numa tela de dinheiro.
- Todo lançamento previsto é marcado como tal na lista. Sem marca, o usuário lê uma
  parcela de dezembro como gasto de hoje.
- Aporte não aparece no relatório de gasto, mas aparece como **linha "guardado"** no
  fechamento do mês — senão o usuário procura o dinheiro que sumiu
  (`docs/06-interface/dashboard.md`).
