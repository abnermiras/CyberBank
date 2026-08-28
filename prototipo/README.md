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
diferença, e o ciclo de fatura com reabertura.

## O que dá para testar

| Ação | O que ela valida |
|---|---|
| Trocar de ambiente no header | Isolamento: nada de um ambiente aparece no outro |
| `+1D` / `+7D` / `+30D` | Previsto virando realizado; fatura fechando sozinha |
| Lançar pelo quick-add sem categoria | A fila de pendências |
| Formulário completo → CRÉDITO, 3 parcelas | Parcelas nascendo previstas, atravessando faturas |
| Formulário completo → TRANSFERÊNCIA para a Reserva | Aporte que não é gasto e não muda o patrimônio |
| Fatura → REABRIR, editar, FECHAR | O ajuste da diferença numa fatura já paga |
| Reserva → informar valor atual | Rendimento como lançamento, não como sobrescrita |
| Séries → alterar o **parcelamento** | Altera todas as parcelas e reabre a fatura fechada sozinho |
| Séries → alterar a **recorrência** | A pergunta "só as futuras ou o passado também?", com o impacto na tela antes de confirmar |
| Séries → cancelar | Previstos somem, passado fica |

O seed começa em **27/08/2026** com uma fatura fechada vencendo no dia seguinte, outra
aberta, um parcelamento atravessando as duas, um boleto previsto, um aporte e uma
pendência — tudo o que o modelo sabe fazer visível numa tela só.

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

Achado novo trabalhando aqui? Ele vale mais no doc dono da regra do que neste README.

## Limites

Sem persistência: recarregar volta ao seed (é o botão `RESET`). Não dá para **criar**
recorrência pela tela ainda — o seed traz uma; a tela só edita e cancela. Sem backend, sem
autenticação real, sem validação séria de formulário. Cadastro de conta, categoria e
meio ainda não existe — o seed faz esse papel.

## Direção visual

`docs/06-interface/direcao-visual.md`. Navegação e ações globais:
`docs/06-interface/navegacao.md`.
