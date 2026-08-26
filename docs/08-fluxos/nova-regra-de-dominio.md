---
id: 08-fluxos/nova-regra-de-dominio
titulo: "Fluxo: nova regra de negócio"
dono: roteiro de alteração de regra ou invariante de domínio
ler-junto: []
status: ativo
---

# Fluxo: nova regra de negócio

Use quando a mudança é de **comportamento**, não de encanamento: um novo estado, uma
validação nova, uma invariante que passa a valer.

## 1. Contexto a carregar

1. `docs/00-produto/glossario.md` — use o termo certo; termo errado vira modelo errado
2. `docs/02-dominio/<agregado>.md` — o agregado dono da regra
3. Os agregados que a regra **atravessa**, se houver (a seção `ler-junto` do doc diz quais)

**Condicionais:** `docs/01-arquitetura/modulos.md` se a regra cruza módulos ·
`docs/03-dados/modelo-de-dados.md` se exigir estrutura nova.

**Não abra:** API, integrações, operação, dashboard. Regra de domínio se resolve no domínio.

## 2. Regras

- Se a regra não cabe em nenhum agregado existente, **pare**: ou falta um agregado, ou
  o glossário está incompleto. Pergunte antes de forçar em um agregado que não é dono.
- Regra que precisa de dado de dois agregados vive no **caso de uso**, não dentro de um
  dos dois puxando o outro.
- Invariante se defende no **construtor/factory**, não em validação espalhada.
- Estado novo ⇒ desenhe a **máquina de estados completa** antes: quais transições passam
  a ser válidas e, principalmente, quais deixam de ser.
- Toda regra nasce com teste que **falha antes** dela existir.

## 3. Pronto quando

- [ ] Regra escrita em prosa no doc do agregado, com os casos de borda nomeados
- [ ] Teste de domínio para o caminho feliz **e** para cada violação da invariante
- [ ] Máquina de estados atualizada no doc, se houve estado novo
- [ ] Glossário atualizado se surgiu termo novo
- [ ] Nenhuma parte da regra vazou para controller, repository ou adapter
