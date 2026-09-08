---
description: Implementar uma funcionalidade nova (caso de uso) ponta a ponta
argument-hint: <funcionalidade>
---

Tarefa: $ARGUMENTS

Siga `docs/08-fluxos/novo-caso-de-uso.md`.

Carregue **apenas** os documentos que a seção "Contexto a carregar" desse fluxo lista
como obrigatórios. Os condicionais, só se a condição descrita se aplicar de fato ao que
foi pedido. **Não** faça busca ampla no repositório e **não** leia nada fora dessa lista.

O endereço do código é derivável do nome do doc (`ADR-0008`): o assunto está em
`src/main/java/br/com/cyberbank/<assunto>/`, com as camadas `dominio`, `aplicacao`, `api` e
`persistencia` dentro dele. **Não varra `src/`.**

Se algum documento necessário estiver com `status: stub`, pare e pergunte — o conteúdo
não existe e não deve ser deduzido. Se a regra parecer precisar de dois pacotes de domínio,
pare e pergunte: pelo `ADR-0008` isso é erro de modelagem, não de organização.

Antes de escrever código, diga em duas linhas: quais docs você abriu e qual o plano.
Ao terminar, cumpra a seção "Pronto quando" do fluxo, inclusive a atualização dos docs.
