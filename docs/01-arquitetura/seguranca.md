---
id: 01-arquitetura/seguranca
titulo: Seguranca
dono: autenticacao, autorizacao, gestao de segredos, superficie exposta
ler-junto: [05-integracoes/vault-segredos, 07-operacao/deploy]
status: stub
---

# Seguranca

> **STUB** — conteudo ainda nao escrito. Ao preencher, siga `docs/CONVENTIONS.md`,
> apague este bloco e troque `status: stub` por `status: ativo`.

## Perguntas que este documento precisa responder

- [x] **Como o isolamento por ambiente e imposto** — decidido em `ADR-0002`: filtro no
      repositorio + Row Level Security no Postgres. As regras de acesso e papeis ficam em
      `docs/02-dominio/ambiente-financeiro.md`
- [ ] Como um usuario se autentica e como a sessao/token e mantida
- [ ] O que fica exposto na rede local e o que nunca sai do host
- [ ] Onde ficam os segredos e como o sistema os le
- [ ] Regras de dados sensiveis (o que nao pode ir para log, o que e mascarado)
- [ ] Como o bot do Telegram prova que a mensagem veio de quem diz ter vindo

## Conteudo

_(vazio)_
