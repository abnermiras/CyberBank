---
id: 01-arquitetura/decisoes/ADR-0004-compartilhamento-entre-ambientes
titulo: "ADR-0004: conta e cartão podem ser compartilhados entre ambientes"
dono: o que o compartilhamento faz com a regra de isolamento do ADR-0002
ler-junto: [01-arquitetura/decisoes/ADR-0002-isolamento-por-ambiente, 02-dominio/compartilhamento]
status: ativo
---

# ADR-0004: conta e cartão podem ser compartilhados entre ambientes

- **Status:** aceita, estende o `ADR-0002`
- **Data:** 2026-08-28
- **Afeta:** isolamento, todo repositório, políticas de RLS, `02-dominio/ambiente-financeiro`

## Contexto

O `ADR-0002` impõe isolamento total por ambiente: toda consulta filtra por `ambiente_id`, na
aplicação e no RLS. Isso assume que o ambiente é uma **partição** — nada atravessa.

Conta conjunta e cartão compartilhado quebram essa premissa, e não por capricho: são a vida
real. Uma conta conjunta é uma conta só que dois ambientes usam; um cartão compartilhado é
um plástico só que duas pessoas usam. Modelar isso duplicando a conta em cada ambiente faria
o saldo mentir nos dois.

## Decisão

O ambiente continua sendo o dono do dado. **O que atravessa é o uso, não a posse.**

1. Um **objeto compartilhável** — conta ou cartão (meio `CREDITO`) — continua pertencendo ao
   ambiente que o criou, para sempre.
2. Um **vínculo de compartilhamento** liga esse objeto a outro ambiente, do mesmo usuário ou
   de outro. O vínculo dá **uso**, não posse.
3. **Todo lançamento pertence ao ambiente em que foi feito**, mesmo quando a conta é de
   outro. Essa regra não tem exceção, e é ela que mantém categoria, orçamento e relatório
   de cada ambiente separados.
4. A visibilidade deixa de ser "meu ambiente" e passa a ser **"meu ambiente, mais o que foi
   compartilhado comigo"** — com a categoria dos lançamentos de fora **mascarada**.

O critério de acesso, tanto no filtro da aplicação quanto no RLS, passa de

> `ambiente_id = <ambiente corrente>`

para

> `ambiente_id = <ambiente corrente>` **ou** o dado pertence a um objeto compartilhado com o
> ambiente corrente.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Não compartilhar: quem quer conta conjunta cria um ambiente para ela | Simples e já era a resposta. Mas separa em dois ambientes o que na vida é uma conta só, e obriga a olhar dois lugares para saber quanto tem |
| Copiar a conta em cada ambiente e sincronizar | Dois saldos para manter iguais. É a definição de dado que diverge |
| Compartilhar o ambiente inteiro em vez do objeto | Já existe, e é grosso demais: para dividir um cartão de gasolina você entregaria a vida financeira inteira |

## Consequências

- **Ganhamos:** conta conjunta, cartão de gasolina dividido e cartão adicional de outra
  pessoa passam a existir sem duplicar dado nem mentir no saldo.
- **Perdemos:** o isolamento deixa de ser uma linha. A política de RLS ganha um `OR` com
  subconsulta, e cada consulta nova precisa responder "estou lendo pelo ambiente ou pelo
  objeto?". É o preço, e ele é pago em toda tabela ligada a conta.
- **Passa a ser proibido:** lançamento sem ambiente próprio; herdar o ambiente da conta em
  vez de gravar o ambiente de quem lançou; e exibir categoria de lançamento de outro
  ambiente — categoria de fora é sempre mascarada.
- **Quando:** o **modelo** entra na Fase 1 — ambiente de quem lançou, posse separada do uso e
  a política de RLS já com o `OR`, mesmo sem nenhum vínculo existir. A **funcionalidade** é
  liberada com a Fase 1 concluída (`docs/00-produto/roadmap.md`).
- **Revisitar se:** o `OR` da política aparecer em medição real como custo no Raspberry Pi.
  A saída seria uma tabela materializada de visibilidade, nunca desligar o RLS.
