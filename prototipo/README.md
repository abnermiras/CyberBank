# CyberBank — protótipo

Protótipo navegável do CyberBank. **Não é o app**: é um instrumento para fechar regra de
negócio e decisão de UX antes de escrever código de produção, quando mudar ainda é barato.

Fica fora de `docs/` de propósito — `docs/_tools/docs.py` varre tudo lá dentro esperando
front-matter, e um README de protótipo quebraria o `check`.

## Como abrir

Abra `index.html` no navegador. Não precisa de servidor, build nem dependência —
são arquivos estáticos e scripts clássicos, funcionam por `file://`.

```
index.html   →  landing
login.html   →  acesso (qualquer identidade 3+ e chave 4+ entra)
app.html     →  terminal
```

## Estrutura

```
prototipo/
├── index.html              landing
├── login.html              acesso e provisionamento de identidade
├── app.html                shell do terminal (home, extrato, fatura, reserva)
└── assets/
    ├── css/cyber.css       linguagem visual: tokens, HUD, atmosfera
    ├── css/app.css         layout do terminal
    ├── js/dominio.js       O MOTOR — implementa as regras de docs/02-dominio/
    ├── js/mock.js          dados fictícios (nomes inventados)
    └── js/app.js           UI e interação
```

`dominio.js` é a parte que importa. Ele implementa de verdade as decisões tomadas:
valor sempre positivo com `sentido` à parte, as duas datas, `PREVISTO`/`REALIZADO`,
saldo como soma dos lançamentos, transferência como par ligado, rendimento como
diferença — e, desde 28/08, o **cartão como conta** (`ADR-0003`): a compra debita a
conta `CARTAO`, pagar a fatura é transferência, e o que não foi pago fica como saldo.

## O que dá para testar

| Ação | O que ela valida |
|---|---|
| Trocar de ambiente no header | Isolamento: nada de um ambiente aparece no outro |
| `+1D` / `+7D` / `+30D` | Fatura fechando sozinha, abrindo a seguinte, lançando a recorrência e criando o **pagamento previsto** |
| Lançar pelo quick-add sem categoria | A fila de pendências |
| Formulário completo → CRÉDITO, 3 parcelas | A compra cai na fatura **ABERTA** pelo status, não pela data; as 3 parcelas nascem juntas e **todas `PROVISIONADAS`**, porque a compra aconteceu uma vez |
| Formulário completo → TRANSFERÊNCIA para a Reserva | Aporte que não é gasto e não muda o patrimônio |
| Fatura → **PAGAR** | O pagamento é uma **transferência** da conta pagadora para o cartão. Compare o extrato da corrente com o do cartão |
| Fatura → **PAGAR PARTE**, depois `+30D` | O que não foi pago vira a linha **"Saldo da fatura anterior"** no topo da fatura seguinte, quando esta vencer. É um par dentro da própria conta do cartão: soma zero, a dívida não muda |
| Fatura → **ABRIR** | Só aparece na última fechada; a seguinte volta a `FUTURA` mantendo o que tinha |
| Editar lançamento de fatura fechada | **Nada congela**: edita direto. Em fatura paga, `AJUSTAR PAGAMENTO` é a resposta "o banco cobrou o valor novo" |
| Reserva → informar valor atual | Rendimento como lançamento, não como sobrescrita |
| Séries → alterar o **parcelamento** | Altera todas as parcelas e mostra quais faturas fechadas mudam de valor |
| Séries → alterar a **recorrência** | A pergunta "só as futuras ou o passado também?", com o impacto na tela antes de confirmar |
| Séries → cancelar | Previstos somem, passado fica |

O seed começa em **27/08/2026** com uma fatura fechada vencendo no dia seguinte, outra
aberta, uma futura, um parcelamento atravessando as três, um boleto previsto, um aporte e
uma pendência — tudo o que o modelo sabe fazer visível numa tela só.

**Confira estes números ao abrir** (ambiente PESSOAL): em caixa `R$ 11.236,00` · dívida do
cartão `R$ 3.560,80` · patrimônio `R$ 21.160,00` · limite disponível `R$ 11.439,20`.
Pagar a fatura **não pode mudar o patrimônio** — se mudar, é bug.

O "em caixa" caiu de `R$ 12.036,80` para `R$ 11.236,00` em 29/08, e a diferença é exatamente
o saldo do vale-refeição: `entraEmCaixa` virou campo e a `BENEFICIO` saiu do caixa
(`docs/02-dominio/conta.md`). Dívida, patrimônio e limite **não podem** ter mudado junto.

## O que o protótipo já mudou nos docs

- **Pendência** era "lançamento sem categoria". Larga demais: pegava a abertura de conta.
  Virou "lançamento que *espera* categoria" (`docs/02-dominio/lancamento.md`).
- **Pagamento de fatura** não cria lançamento nenhum — os lançamentos da fatura já
  debitam a conta. Criar um lançamento de pagamento contaria o gasto duas vezes.
  Respondeu uma pergunta aberta de `docs/02-dominio/fatura-cartao.md`.
- **Recorrência não é parcelamento**, e a diferença é a regra de edição: parcelamento
  altera todas as parcelas sempre; recorrência pergunta. O protótipo tinha mostrado uma
  série de parcelas terminando com valores diferentes — o bug era aplicar a regra de
  recorrência num parcelamento (`docs/02-dominio/recorrencia.md`).
- **A dívida do cartão não é o saldo projetado da conta `CARTAO`.** O projetado abate o
  pagamento *previsto*, que ainda não aconteceu, e responde outra pergunta: "quanto vou
  dever depois de pagar". Com o projetado, a dívida saía R$ 2.030 em vez de R$ 3.600,70 —
  e o patrimônio herdava o erro. Limite e patrimônio usam a **dívida**
  (`docs/02-dominio/fatura-cartao.md`).
- **`entraNoFluxoDeCaixa` não responde "isso é caixa?".** A conta `CARTAO` foi a primeira em
  que as duas perguntas divergem: os movimentos dela são gasto, mas o saldo é dívida. "Em
  caixa" passou a somar as contas de fluxo **menos as de dívida** (`docs/02-dominio/conta.md`).
- **O vale-refeição era o segundo caso, e ninguém tinha reparado.** R$ 800,80 de vale
  entravam no "em caixa" e no "quanto sobra até o fim do mês": o app afirmava que dava para
  pagar um boleto com dinheiro que só compra comida. O `conta.md` já dizia que ao aparecer o
  segundo caso a exceção por tipo viraria campo — virou **`entraEmCaixa`**, e com ele o motor
  parou de perguntar o tipo da conta para saber o que é caixa.
- **Numa mesma fatura, uma compra à vista era `REALIZADO` e uma parcela era `PREVISTO`** —
  lado a lado, esperando o mesmo pagamento no mesmo dia. O erro de fundo era `situacao`
  carregar duas perguntas: *já aconteceu?* e *entra no saldo?*. Nasceu `PROVISIONADO`
  (`ADR-0006`), e com ele sumiram duas exceções: a dívida do cartão voltou a ser o saldo da
  conta, e o patrimônio voltou a ser o realizado de todas as contas.
- **O que não foi pago ficava preso numa fatura vencida.** Pagando R$ 800 de R$ 1.610,60 e
  avançando o relógio, os R$ 810,60 continuavam na dívida (certo) mas não apareciam na
  fatura seguinte — que exibia R$ 1.100,10 quando o banco ia cobrar R$ 1.910,70. O erro era
  tratar o pagamento como sendo *da fatura* enquanto a dívida era *da conta*. Nasceu a
  rolagem (`ADR-0005`).

Achado novo trabalhando aqui? Ele vale mais no doc dono da regra do que neste README.

## Limites

Sem persistência: recarregar volta ao seed (é o botão `RESET`). Não dá para **criar**
recorrência pela tela ainda — o seed traz uma; a tela só edita e cancela. Sem backend, sem
autenticação real, sem validação séria de formulário. Cadastro de conta, categoria e
meio ainda não existe — o seed faz esse papel.

**Não simula ainda** (`ADR-0004`): compartilhamento de conta e de cartão entre ambientes,
categoria mascarada, partes da fatura e pagamento vindo de dois ambientes. É a próxima
rodada — e é onde a regra "todo lançamento pertence ao ambiente de quem lançou" vai apanhar.
Também não tem cartão virtual nem adicional no seed, embora o modelo já os suporte: são
meios `CREDITO` apontando para a mesma conta `CARTAO`.

## Direção visual

`docs/06-interface/direcao-visual.md`. Navegação e ações globais:
`docs/06-interface/navegacao.md`.
