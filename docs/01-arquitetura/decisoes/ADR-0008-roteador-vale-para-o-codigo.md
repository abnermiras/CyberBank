---
id: 01-arquitetura/decisoes/ADR-0008-roteador-vale-para-o-codigo
titulo: "ADR-0008: o roteador vale para o código, e o pacote espelha o doc"
dono: a regra que mantem uma tarefa barata depois que houver codigo, e o orcamento por rota
ler-junto: [01-arquitetura/modulos, 01-arquitetura/estrutura-de-pastas]
status: ativo
---

# ADR-0008: o roteador vale para o código, e o pacote espelha o doc

- **Status:** aceita
- **Data:** 2026-09-07
- **Afeta:** `CLAUDE.md`, `01-arquitetura/modulos`, `01-arquitetura/estrutura-de-pastas`,
  `01-arquitetura/padroes-de-codigo`, `docs/_tools/docs.py`

## Contexto

A restrição que criou a `docs/` modular está escrita no `CLAUDE.md`: **não faça varredura no
repositório.** Uma tarefa lê a linha dela na tabela e abre só o que ela manda. Isso funciona, e
tem número — `docs.py custo` mede hoje entre **2.011 e 11.740 tokens por rota**, contra
**73.084** para ler tudo.

Só que o roteador endereça `docs/`, e `src/` ainda não existe. No dia em que existir, a tarefa
*"mudar o fechamento da fatura"* recebe os dois docs certos, e então precisa achar o código —
e acha por varredura, que é exatamente o que a regra proíbe. **Proibir sem dar endereço não
funciona: quem não tem endereço procura.** A economia dos docs seria consumida pela busca no
código, e a medição pararia de dizer a verdade.

Decidir isso agora custa uma convenção. Decidir depois custa mover pastas com código real em
cima — o mesmo argumento que puxou `Aplicação` e o compartilhamento para a Fase 1.

## Decisão

**O endereço do código é derivável do nome do doc dono. Nenhuma tabela precisa ser mantida.**

1. **O pacote espelha o doc.** Cada doc dono de `docs/02-dominio/` corresponde a **um** pacote
   de domínio de mesmo nome, e nenhuma regra dele vive fora dali. A correspondência é por
   **assunto**, não por arquivo: `fatura-cartao.md` e `fatura-pagamento.md` são dois docs
   porque um passou de 300 linhas, e apontam para o mesmo pacote `fatura`.
2. **Vale para as outras camadas pela mesma regra.** `04-api/endpoints-contas.md` endereça o
   pacote de API de `conta`; `03-dados/` endereça a migration daquele nome. O nome do doc é o
   endereço, em qualquer camada.
3. **A rota tem orçamento.** `docs.py custo` passa a contar também o código da rota, e entra no
   `docs.py check`: rota que estoura o teto **falha o check**. É o que o `CONVENTIONS` já faz
   por doc, aplicado ao que de fato se paga — a rota inteira.

A fronteira que sustenta as três: **se uma regra de negócio precisa de dois pacotes de domínio,
o erro é de modelagem, não de organização.** É a regra 6 do `CLAUDE.md` (*um fato mora em um
doc só*) e a regra de desenho do `meio-de-pagamento.md` (*`if (tipo == CREDITO)` fora do módulo
é bug de modelagem*) ditas uma terceira vez, agora sobre pastas.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| **Camadas como pastas de topo** (`controller/`, `service/`, `repository/`) | É o default do Spring e o pior para o custo: uma feature se espalha por quatro pastas e **nenhuma rota fecha**. É o layout que torna o RaspyBank caro de mexer hoje |
| **Índice de código gerado** — mapa símbolo → arquivo, atualizado por script | Resolve a busca sem resolver o espalhamento. O índice cresce com o projeto, precisa ser lido a cada tarefa e envelhece calado: troca varredura por manutenção |
| **Coluna de código na tabela do `CLAUDE.md`** | Endereço escrito à mão é endereço que sai do lugar. Se o nome do doc já dá o pacote, a coluna só cria uma segunda fonte de verdade para divergir da primeira |
| **Deixar para quando houver código** | É a decisão que se paga em migração de pastas. E ela não avisa: o custo aparece diluído em toda tarefa, não num dia |

## Consequências

- **Ganhamos:** a rota fecha de ponta a ponta, e continua **medível** depois que houver código.
  Uma tarefa de fatura lê dois docs e um pacote, e nada mais — hoje e com 200 arquivos em
  `src/`.
- **Perdemos:** o layout não é o que um dev de Spring espera encontrar, e o
  `padroes-de-codigo.md` vai precisar dizer isso na primeira linha. Domínios que conversam
  muito — `lancamento` e `fatura` — passam a exigir fronteira explícita em vez de acesso
  direto.
- **Passa a ser proibido:** regra de negócio de um domínio em pacote de outro; pasta de topo
  por camada; rota nova no `CLAUDE.md` sem custo medido.
- **Revisitar se:** uma rota estourar o teto por motivo legítimo. Aí o que está errado é a
  fronteira do domínio, não o teto — e a resposta é quebrar o domínio, nunca subir o número.
