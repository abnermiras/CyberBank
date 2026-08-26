---
id: 08-fluxos/nova-integracao-externa
titulo: "Fluxo: adicionar uma integração externa"
dono: roteiro de implementação de integração com sistema externo
ler-junto: []
status: ativo
---

# Fluxo: adicionar uma integração externa

Use para uma nova fonte de dados (banco, emissor, extrato, canal de entrada) ou um
novo destino (notificação, exportação).

## 1. Contexto a carregar

1. `docs/05-integracoes/visao-geral.md` — a porta comum que toda integração implementa
2. `docs/01-arquitetura/modulos.md` — onde o adapter mora e do que ele pode depender
3. O doc da integração **mais parecida** que já existe (imite, não invente)
4. `docs/02-dominio/lancamento.md` — se a integração produz lançamentos

**Condicionais:** `docs/02-dominio/importacao-conciliacao.md` se traz dados que podem
duplicar com captura existente · `docs/05-integracoes/vault-segredos.md` se tem
credencial · `docs/01-arquitetura/seguranca.md` se abre porta ou recebe requisição externa.

**Não abra:** domínio além do citado, dashboard, produto.

## 2. Regras inegociáveis

- Integração é **adapter na borda**. O domínio não sabe que ela existe e nunca importa
  classe dela.
- Todo dado externo é **desconfiado**: valide e normalize na borda; o domínio só recebe
  tipo próprio já válido.
- **Idempotência é obrigatória** em integração de entrada: a mesma mensagem entregue
  duas vezes não pode gerar dois lançamentos. Defina a chave de deduplicação e registre-a
  no doc.
- Falha externa **nunca** derruba o fluxo do usuário: timeout definido, retry com limite,
  e um lugar onde o que falhou fica visível para reprocessar.
- Custo externo zero. Se exige serviço pago, **pare e pergunte**.

## 3. Ordem de trabalho

1. Escrever o doc `docs/05-integracoes/<nome>.md` **antes** do código — formato bruto
   recebido, mapeamento campo a campo, o que falta, o que fazer com o não reconhecido.
2. Implementar o adapter contra a porta existente.
3. Teste com **amostra real** do payload externo, salva como fixture.
4. Registrar na tabela de `docs/05-integracoes/visao-geral.md`.

## 4. Pronto quando

- [ ] Doc da integração escrito com mapeamento campo a campo e exemplo bruto real
- [ ] Chave de idempotência definida, documentada e testada com entrega duplicada
- [ ] Teste com payload malformado não derruba nem corrompe estado
- [ ] Credenciais fora do código, conforme `vault-segredos.md`
- [ ] Linha adicionada em `visao-geral.md`
