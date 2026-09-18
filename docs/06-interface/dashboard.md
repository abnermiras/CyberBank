---
id: 06-interface/dashboard
titulo: Home
dono: o que a Home mostra, em que ordem e por que — o cockpit de decidir
ler-junto: [04-api/endpoints-relatorios, 06-interface/navegacao, 06-interface/direcao-visual]
status: ativo
---

# Home

**É a tela em que o usuário cai**, e ela tem uma pergunta só: *dá para gastar?* Tudo o que
está nela existe para responder isso em cinco segundos, sem clicar em nada.

O critério que corta: **um bloco fica na Home se muda uma decisão de hoje.** Histórico,
comparação entre meses e detalhe de lançamento não mudam — eles moram no Extrato e nos
relatórios. A Home é cockpit, não arquivo.

Todos os números vêm de **uma requisição só** (`docs/04-api/endpoints-relatorios.md`): duas
metades da tela discordando é pior que a tela demorar.

## A ordem, e por que ela é essa

| # | Bloco | Responde |
|---|---|---|
| 1 | **Os quatro números** | Quanto tenho, quanto sobra, quanto guardei, quanto sou |
| 2 | **Gasto por categoria** | Para onde o dinheiro foi **neste mês** |
| 3 | **Pendências** | O que o sistema não sabe classificar — e o usuário resolve **ali** |
| 4 | **Fatura** | A janela que importa de cada cartão |
| 5 | **O que vem por aí** | O que ainda vai cair até o fim do mês |
| 6 | **O mês em números** | Entrou, saiu, guardou — e o ritmo do que sobra |

A leitura desce do **agora** (1) para o **passado do mês** (2 e 3) e termina no **futuro
próximo** (5). Quem olha três segundos lê a linha 1; quem olha trinta decide com a 5.

## 1. Os quatro números

| Número | Cor | O que é |
|---|---|---|
| **Em caixa · agora** | ciano | Saldo **realizado** das contas que pagam qualquer coisa |
| **Sobra até `dd/mm`** | ácido | Saldo **projetado** até o fim do mês |
| **Guardado** | lima | Saldo das contas fora do fluxo de caixa |
| **Patrimônio** | tinta | Todas as contas, sem exceção |

**Realizado e projetado nunca aparecem sem rótulo**, e aqui o rótulo está no próprio número: um
diz *agora*, o outro diz *até `dd/mm`* com a data escrita. São perguntas diferentes — *"quanto
tenho"* e *"quanto sobra"* — e confundi-las é o pior erro possível numa tela de dinheiro.

A **sobra** carrega embaixo a conta que a produziu: `em caixa + a receber − a pagar`. Número de
projeção sem a conta à vista é número que ninguém confere.

## 2. Gasto por categoria

Uma barra por **raiz**, na cor de identidade dela, decrescente. Ao lado: o valor, quantos
lançamentos e a fatia do total.

- **A barra é proporcional à maior**, não ao total: é a comparação entre categorias que o olho
  precisa fazer, e uma barra de 4% seria invisível.
- **"Sem categoria" aparece na lista**, em cinza, e leva às Pendências. O dinheiro saiu:
  escondê-lo faria a soma das barras ser menor que o mês.
- **A linha `GUARDADO NO MÊS` fica embaixo da lista, separada por uma régua**, dizendo *não é
  gasto — por isso não está na lista acima*. Sem ela o usuário procura o dinheiro que sumiu
  (`docs/02-dominio/aplicacao-patrimonio.md`).
- Mês sem gasto é resposta, não erro: a tela diz que o mês ainda não teve gasto.

**Não há seletor de eixo** enquanto não houver fatura, e a razão está em
`docs/04-api/endpoints-relatorios.md`: sem ela, a compra só cai num mês.

## 3. Pendências

A fila do que está sem categoria, com a **contagem inteira** no cabeçalho — não a da página.

**Resolve-se na Home, sem sair dela**: cada linha tem o seletor de categoria, agrupado por
raiz, e escolher já grava (`docs/06-interface/navegacao.md`). É a única escrita que a Home
faz, e ela está aqui porque a alternativa — abrir o Extrato, achar a linha, editar — é o
caminho que ninguém percorre, e a fila só cresce.

Lista até cinco; o resto está no Extrato, com o filtro de pendência ligado.

## 4. Fatura

**A janela que importa**, uma linha por cartão: a fatura `FECHADA` que ainda deve, ou, na falta
dela, a `ABERTA`. O **a pagar** é o número grande, com o vencimento (ou o fechamento, se ela
ainda está aberta) e o mês da competência embaixo. Se há pagamento agendado, ele aparece — é o
que impede o usuário de pagar duas vezes.

**As ações não estão aqui**, e é de propósito: a linha leva à tela da Fatura
(`docs/06-interface/fatura.md`), que é onde o estado decide quais botões existem. A Home é onde
se **olha antes de decidir**; decidir é na tela do objeto.

**Sem cartão o bloco diz isso**, em vez de sumir: tela que esconde o que falta ensina que o app
não faz.

## 5. O que vem por aí

O que ainda **não aconteceu** e cai até o fim do mês: boleto com vencimento, parcela futura,
qualquer `PREVISTO`.

- Dois totais no topo: **a pagar** (rosa) e **a receber** (lima).
- Abaixo, a lista em ordem de vencimento, com o dia em mono e o **T−n** de quantos dias faltam.
  *Hoje* é escrito por extenso, porque é o único que muda o que se faz agora.
- Cada linha leva ao Extrato.
- Vazio é resposta: *nada previsto até o fim do mês*.
- O rodapé nomeia **o que ainda não entra aqui**: recorrência. A fatura passou a entrar — o
  `a pagar` das que vencem até lá é descontado da **sobra**, por consulta e não por lançamento
  inventado, e o que já tem pagamento agendado não é contado duas vezes
  (`docs/02-dominio/conta.md`). Um número que esconde o que não sabe é um número em que se
  confia demais.

## 6. O mês em números

`ENTROU`, `SAIU` e `GUARDOU` do mês, com uma barra comparando entrada e saída, mais a
contagem regressiva de dias até o fim do mês.

É o bloco que responde *"o mês está indo bem?"* sem exigir conta nenhuma — e é onde a
regressiva `T−n dias` mora, porque ela só significa alguma coisa ao lado do que ainda vai
cair.

## O que a Home nunca faz

| Não faz | Por quê |
|---|---|
| Extrapolar o mês a partir da média | O sistema não inventa número (regra 7 do `CLAUDE.md`). Ele mostra o que aconteceu e o que já está previsto |
| Somar contas de dois ambientes | O isolamento é do modelo (`ADR-0002`) |
| Mostrar gráfico de pizza | Comparar ângulo é pior que comparar barra, e a lista já ordena |
| Esconder bloco que ainda não funciona | Bloco reservado diz o que espera; bloco ausente ensina que o app não faz |
| Recalcular no JavaScript | A soma vem do servidor. Duas aritméticas para o mesmo número é uma delas errada, algum dia |
