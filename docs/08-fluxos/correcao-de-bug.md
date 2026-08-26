---
id: 08-fluxos/correcao-de-bug
titulo: "Fluxo: correção de bug"
dono: roteiro de diagnóstico e correção de defeito
ler-junto: []
status: ativo
---

# Fluxo: correção de bug

O fluxo mais caro em tokens quando feito errado — "investigar" vira leitura do
repositório inteiro. A regra aqui é: **localizar antes de ler**.

## 1. Antes de abrir qualquer arquivo

Responda:

1. Qual **sintoma** exato? (mensagem, valor errado, estado errado — não "não funciona")
2. Em que **camada** ele aparece? bot / API / domínio / banco / integração
3. Qual o **caminho** completo do gasto ou dado até o sintoma?

Sem as três respostas, não comece — pergunte. Investigação sem sintoma preciso é o
gasto mais caro que existe.

## 2. Contexto a carregar

- O doc do agregado ou da integração da camada identificada — **um só**
- `docs/07-operacao/runbook.md` **apenas se** o sintoma é de operação (fora do ar, lento,
  captura parada). Se é lógica errada, não abra.

**Não abra o repositório para "entender".** Se precisa achar o código, peça o caminho
ou faça **uma** busca por símbolo específico — nunca busca ampla por palavra genérica.

## 3. Ordem de trabalho

1. **Teste que reproduz o bug, falhando.** Sempre primeiro. Se não consegue reproduzir,
   ainda não entendeu o bug.
2. Corrigir com o **menor diff possível**. Refatoração oportunista entra em outra tarefa.
3. Perguntar: o doc estava certo e o código errado, ou o doc induziu o erro?
   Se foi o doc, **corrija o doc** — senão o bug volta.

## 4. Pronto quando

- [ ] Teste que reproduzia o bug agora passa
- [ ] Nenhum teste existente quebrou
- [ ] Diff mínimo, sem mudança não relacionada carona
- [ ] Doc corrigido, se o doc participou da causa
- [ ] Se o sintoma pode reaparecer em produção: entrada nova no `runbook.md`
