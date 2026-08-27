---
id: 02-dominio/aplicacao-patrimonio
titulo: Aplicação e patrimônio
dono: aplicacao como entidade, aporte e resgate, e como o patrimonio e calculado
ler-junto: [02-dominio/conta, 02-dominio/lancamento, 00-produto/roadmap]
status: stub
---

# Aplicação e patrimônio

> **STUB** — conteúdo ainda não escrito. Ao preencher, siga `docs/CONVENTIONS.md`,
> apague este bloco e troque `status: stub` por `status: ativo`.

**Este doc é Fase 1**, não "algum dia": o roadmap decidiu que aplicação e patrimônio
entram no corte mínimo, justamente porque a separação entre fluxo de caixa e patrimônio
contamina saldo, dashboard e schema. Ver `docs/00-produto/roadmap.md`.

Escopo já decidido pelo roadmap, para não reabrir: valor atual da aplicação é atualizado
**à mão**; sem rentabilidade calculada, sem cotação, sem consulta a serviço externo.

## Perguntas que este documento precisa responder

- [ ] Aplicação é um **tipo de conta** ou uma entidade própria? (Ela tem saldo, recebe e
      devolve dinheiro — parece conta; mas não é meio de pagamento e não entra no fluxo
      de caixa. A escolha decide se `conta.tipo` cresce ou se nasce uma tabela.)
- [ ] Aporte e resgate são **transferências entre duas contas** ou lançamentos de tipo
      próprio? Se forem transferência, o modelo de lançamento precisa de origem e destino.
- [ ] Aporte aparece no dashboard de gasto por categoria? (Se aparecer, "guardar dinheiro"
      vira despesa e o relatório mente. Se não aparecer, o mês não fecha na soma simples.)
- [ ] Como o patrimônio é calculado e em que momento — soma ao vivo ou foto por data?
- [ ] Que tipos de aplicação existem na Fase 1 (poupança, CDB, reserva) e se o tipo muda
      alguma regra ou é só rótulo.
- [ ] O que acontece com o valor atual quando o usuário nunca o atualiza — o patrimônio
      envelhece em silêncio ou o sistema avisa?

## Conteúdo

_(vazio)_
