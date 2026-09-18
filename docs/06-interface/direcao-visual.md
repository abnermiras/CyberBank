---
id: 06-interface/direcao-visual
titulo: Direção visual
dono: a linguagem visual do Cyberbank: paleta, tipografia, forma e o limite do efeito
ler-junto: [06-interface/navegacao, 06-interface/dashboard]
status: rascunho
---

# Direção visual

Estética **cyberpunk / retro-futurista**: terminal, HUD, neon sobre quase-preto. A
referência é o gênero — não os assets, logos, personagens ou marcas de nenhuma obra
existente. Nomes de instituição no protótipo são inventados.

## A regra que manda em todas as outras

**O número que importa é o maior elemento da tela. A telemetria é moldura.**

Um HUD dá o clima, mas isto aqui é dinheiro: se o efeito competir com o dado, o efeito
perde. Nenhum scanline, glitch ou glow passa por cima de valor, saldo ou data.

## Paleta

| Token | Valor | Papel |
|---|---|---|
| `--void` / `--bg` | `#04070c` / `#06090f` | Fundo. Quase preto, nunca preto puro |
| `--panel` | `#0a1018` | Superfície de painel |
| `--cyan` | `#00f0ff` | Sinalização, contexto, estrutura. Cor do ambiente padrão |
| `--acid` | `#f7f13c` | **Ação e atenção.** Botão primário, projeção, fatura |
| `--pink` | `#ff1f91` | Alerta, saída de dinheiro, pendência, ação destrutiva |
| `--lime` | `#5cff9d` | Entrada de dinheiro, patrimônio, confirmação |
| `--rust` | `#ff6b35` | Categoria, acento secundário |

Cada cor tem **um** significado. Amarelo é sempre ação ou atenção; rosa é sempre alerta
ou saída; verde é sempre entrada ou patrimônio. Cor decorativa que não significa nada é o
começo do fim da legibilidade.

### O botão é nomeado pelo significado, não pela cor

As variantes de botão levam o nome do que a ação **é**, e a cor sai daí — não o contrário:

| Classe | Cor | Quando |
|---|---|---|
| `primary` | acid, **preenchido** | A ação principal de um formulário. Uma por vez |
| `acao` | acid, contorno | Ação sem consequência de dinheiro: informar, abrir, editar |
| `entra` | lime, contorno | Dinheiro **entrando** onde se está olhando: aportar |
| `sai` | pink, contorno | Dinheiro **saindo** de onde se está olhando: resgatar |
| `danger` | pink, contorno | Ação **destrutiva**: excluir |
| `ghost` | cinza | Recuar, cancelar, desistir. Nunca compete com nada |

**Chip de contexto é ciano, não amarelo.** O grupo `.seg` — abas do formulário completo,
contratos da Fatura — marca o ativo em `--cyan`, porque escolher *sobre o que se está olhando* é
**estrutura**, não ação. O amarelo da tela continua sendo um só, e é o do botão que faz algo.

**`sai` e `danger` são a mesma cor, e isso é a paleta sendo honesta, não um descuido:** rosa
significa *saída de dinheiro* **e** *ação destrutiva*, as duas desde sempre. O que as separa é
o contexto — nunca aparecem na mesma linha —, e é por isso que as classes têm nomes
diferentes apesar de pintarem igual: quem lê o código vê qual das duas leituras vale ali.

**Nenhuma variante é preenchida além de `primary`.** Preenchimento é peso, e peso é hierarquia:
três botões sólidos na mesma linha não têm ordem nenhuma.

## Cor de sinal e cor de identidade

A regra acima — *cada cor tem um significado* — vale para **cor de sinal**, e ela continua
inteira. Mas a categoria precisa de cor por outro motivo: o usuário reconhece `MORADIA` pelo
tom antes de ler a palavra, e isso é **identidade**, não semântica.

Se as duas usassem a mesma paleta, a regra morreria: uma categoria de `ENTRADA` pintada de
rosa afirmaria "alerta" sem alertar nada. Então são **duas paletas**, e o que as separa é
medível:

| | Saturação | Papel |
|---|---|---|
| **Sinal** | **92% a 100%** | Significa uma coisa e só uma. A tabela da *Paleta* acima |
| **Identidade** | **15% a 53%** | Não significa nada — distingue. Só a categoria raiz tem |

**São 39 pontos de saturação de folga, e é isso que faz a regra se sustentar sozinha:** numa
tela com as duas, o sinal grita mais alto sempre, porque é o único saturado ali. Cor de
identidade é fundo de card, borda e nome; cor de sinal é tag, valor e botão — e elas nunca
disputam o mesmo elemento.

### A paleta de identidade

| Token | Valor | Contraste sobre `--panel-2` |
|---|---|---|
| `--cat-violeta` | `#8f7ad6` | 5.22 |
| `--cat-azul` | `#5c90cc` | 5.57 |
| `--cat-teal` | `#4fa39a` | 6.20 |
| `--cat-oliva` | `#93a75f` | 6.99 |
| `--cat-ocre` | `#c9954e` | 6.95 |
| `--cat-terracota` | `#c47a6a` | 5.59 |
| `--cat-ardosia` | `#78899b` | 5.15 |
| `--cat-malva` | `#b578a8` | 5.48 |

Todas passam de `4.5` — o mínimo para texto normal. Nenhuma é escura "para ficar dark", que é
o que a tabela de *Limites* proíbe.

**O domínio guarda o nome, nunca o hexadecimal** (`docs/02-dominio/categoria.md`). `VIOLETA` é
o dado; `#8f7ad6` é uma decisão desta página, e trocá-la é editar uma linha de CSS — não uma
migration.

**A paleta não cresce por pedido.** Oito tons distinguíveis já é o limite do que alguém separa
de relance; o nono torna dois indistinguíveis e a cor deixa de identificar.

## Os dez avatares

Não há upload de foto: o usuário escolhe um de dez desenhos, e o domínio guarda **o nome**
(`docs/02-dominio/usuario.md`). Como eles são desenhados é decisão desta página.

| Regra | Por quê |
|---|---|
| **SVG inline**, traço, sem preenchimento chapado | Mesma linguagem dos painéis: linha fina, peso pela cor. E um SVG escala do grid de escolha ao quadrado de 28px do header sem virar borrão |
| **Monocromáticos.** O que distingue é a **forma**, nunca a cor | A paleta de identidade tem **oito** tons e esta página proíbe o nono; os avatares são **dez**. Cor aqui ou obrigaria a repetir dois tons, ou quebraria a regra de cima |
| **Nenhuma cor de sinal** — nada de `--acid`, `--pink` ou `--lime` | Um avatar rosa afirmaria alerta sem alertar nada. Avatar é identidade, e identidade não sinaliza |
| O escolhido é marcado com **borda de seleção em `--cyan`** | Ciano é estrutura e contexto. O amarelo é da ação — e a ação desta tela é o botão de salvar, que é um só |
| Traço legível em 28px | O grid mostra o desenho no tamanho em que ele vai ser visto. Detalhe que some no header não existe |

A referência é o **gênero**: nada de logo, personagem, arte ou marca de obra existente — a
mesma linha da tabela de *Limites*.

## Tipografia

- **Display** (`Chakra Petch`): títulos, valores, botões. Caixa alta, entrelinha curta.
- **Mono** (`Share Tech Mono`): telemetria, rótulos, datas, códigos de sistema.
- Todo valor monetário é **tabular** (`font-variant-numeric: tabular-nums`) — coluna de
  número que dança é coluna que não se compara.

As duas famílias vêm de CDN com pilha de fallback. Sem rede, a interface cai para
`system-ui` e `Consolas` e continua legível — o layout não depende da fonte.

## Forma

- Painéis **chanfrados** (`clip-path`), nunca cantos arredondados. Arredondado é app de
  banco; chanfrado é equipamento.
- Cantos decorativos em `L` nos painéis quentes.
- Bordas de 1px, finas. O peso vem da cor, não da espessura.
- Grade de perspectiva, scanlines e vinheta ficam numa camada `.atmos` com
  `pointer-events:none` — atmosfera nunca captura clique.

> ⚠ **Armadilha real, achada no protótipo:** `clip-path` **recorta todos os descendentes**
> ao próprio retângulo. Um chip com `clip-path` some com o dropdown que abre embaixo dele.
> Elemento que abre camada flutuante não leva `clip-path` — o chanfro dele vira borda ou
> pseudo-elemento.

### A pintura de um controle pertence ao seletor de elemento

**Regra, não aviso:** `input`, `select` e `textarea` são pintados **por tipo de elemento** —
`input[type="text"]`, `select`, `textarea` —, nunca por container. O `.field` cuida **só de
layout**: margem e rótulo.

Ela já foi aviso, e o aviso não impediu a segunda ocorrência. Em 17/09 a pintura estava presa a
`.field input`, o seletor de dia do Diário reusou o `campo-data` fora de um `.field`, e apareceu
**cinza do sistema operacional** no meio de um app cyberpunk. O conserto daquele dia desamarrou
**só o `campo-data`** — e em 18/09 o seletor de contrato da Fatura, um `<select>` no `telahead`,
caiu exatamente no mesmo buraco. **Armadilha consertada só no caso que a revelou volta pelo
segundo caso.**

> ⚠ **E o `<select>` nativo tem um limite que nenhuma regra de CSS resolve:** o **dropdown
> aberto** é desenhado pelo sistema operacional e não aceita tema. Pintar o controle fechado
> deixa a tela coerente até o clique. Onde a lista importa — e o usuário vai olhar para ela —,
> o caminho é **não usar `<select>`**: chips (`.seg`) quando as opções são poucas e cabem, ou um
> painel próprio, como o `Calendario` que nasceu quando o `input[type=date]` caiu pela mesma
> razão.

## Movimento

Animação é rara e curta. O glitch nos títulos dispara a cada poucos segundos, em passos,
por menos de 200ms. Nada pisca, nada gira, nada se move em loop na área de leitura.
Tudo que anima respeita `prefers-reduced-motion`.

## Limites

| Não faça | Por quê |
|---|---|
| Texto de conteúdo em fonte mono estreita | Telemetria é mono; conteúdo é display |
| Glow ou glitch sobre valor monetário | Dinheiro se lê, não se decora |
| Mais de um amarelo de ação por tela | Se tudo é ação primária, nada é |
| Contraste abaixo do legível para "ficar dark" | Fundo quase preto já dá o clima; texto apagado só cansa |
| Logo, marca, fonte ou arte de obra existente | A referência é o gênero, não o produto de ninguém |
