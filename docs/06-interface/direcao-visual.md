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
