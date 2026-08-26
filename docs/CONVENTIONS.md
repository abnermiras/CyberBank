---
id: CONVENTIONS
titulo: Como esta documentação funciona
dono: regras de escrita, nomeação, front-matter e manutenção dos docs
ler-junto: []
status: ativo
---

# Como esta documentação funciona

Esta doc existe para uma coisa: **fazer o Claude Code trabalhar com precisão lendo o
mínimo possível**. Cada regra abaixo existe porque a alternativa custa tokens.

## As cinco regras

### 1. Um fato tem um dono só
Cada informação é definida em **exatamente um** documento — o `dono:` do front-matter.
Qualquer outro doc que precise dela **referencia por caminho**, não copia.
Fato duplicado é fato que vai divergir, e doc divergente força leitura de mais arquivos
para descobrir qual está certo.

### 2. Documento é folha, não é livro
Alvo: **80 a 300 linhas**. Um doc precisa ser útil sozinho, sem exigir os vizinhos.
Passou de 300 linhas, quebre por subdomínio. Ficou com menos de 40 e sempre é lido
junto com outro, funda os dois.

### 3. O front-matter é o índice de máquina
Todo doc começa com:

```yaml
---
id: 02-dominio/lancamento        # = caminho sem .md, é a identidade do doc
titulo: Lançamento
dono: campos, estados e invariantes do lançamento   # o que SÓ este doc define
ler-junto: [02-dominio/conta, 03-dados/modelo-de-dados]
status: ativo | stub | obsoleto
---
```

`ler-junto` é sugestão, não obrigação — quem lê decide. `dono` é contrato: se a
informação não está no escopo do `dono` de ninguém, ela **não tem lugar** e alguém
precisa criar esse lugar.

### 4. Escreva decisão, não narrativa
Um doc responde perguntas fechadas. Nada de história do projeto, nada de "estávamos
pensando em". Formato preferido, em ordem: **tabela > lista > parágrafo**. Uma tabela
de 10 linhas custa menos que dois parágrafos e é lida sem ambiguidade.
Onde houver contrato (API, schema, payload), coloque o **exemplo literal** — exemplo
concreto evita a rodada de perguntas que custa mais que o exemplo.

### 5. Stub é declaração de ignorância, e isso é útil
`status: stub` significa: **este conteúdo não existe, não deduza**. O Claude tem
instrução no `CLAUDE.md` de perguntar em vez de inventar quando encontra um stub.
Um stub honesto é mais barato que um doc plausível e errado.

## Nomeação

- Pastas numeradas (`00-produto`, `01-arquitetura`, …) para ordem estável e previsível.
- Arquivos em `kebab-case`, singular, sem artigo: `meio-de-pagamento.md`.
- Um endpoint-doc por agregado: `endpoints-<agregado>.md`.
- ADR: `decisoes/ADR-NNNN-titulo-curto.md`, numeração nunca reaproveitada.

## Manutenção

**Ao terminar qualquer tarefa de código, atualize o doc dono do que mudou.**
Não é opcional — doc desatualizado é pior que doc ausente, porque induz erro com
confiança. Se a tarefa não mudou nenhum fato documentado, não escreva nada.

**Doc obsoleto não é apagado nem reescrito em silêncio:** marque `status: obsoleto`,
uma linha dizendo o que o substituiu, e remova na limpeza seguinte. Assim o link
quebrado não vira uma busca no repositório.

**Ao criar um doc novo:** adicione a linha em `INDEX.md`. Se ele é entrada de uma
tarefa recorrente, adicione também na tabela de roteamento do `CLAUDE.md` — mas
pense duas vezes: **o `CLAUDE.md` é lido em toda sessão**, cada linha ali é um imposto
permanente. Mantenha abaixo de 100 linhas.

## Economia de tokens na prática

| Hábito | Por quê |
|---|---|
| `/clear` entre tarefas diferentes | O contexto anterior é recarregado a cada turno |
| Entrar sempre por um `08-fluxos/` | Vem com a lista fechada de docs, sem exploração |
| Dizer o caminho do arquivo ao pedir | Evita `grep`/`glob` de descoberta |
| Pedir diff pequeno, não arquivo inteiro | Reescrita completa custa o arquivo duas vezes |
| Não deixar o Claude "explorar para entender" | É o gasto silencioso mais caro que existe |

## O que NÃO vai para os docs

Log de sessão, changelog, TODOs pessoais, saída de comando colada, tutorial de
tecnologia que já existe na documentação oficial, e qualquer coisa que envelheça
sozinha em uma semana.
