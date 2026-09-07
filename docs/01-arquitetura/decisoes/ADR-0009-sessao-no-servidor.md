---
id: 01-arquitetura/decisoes/ADR-0009-sessao-no-servidor
titulo: "ADR-0009: sessão no servidor, não JWT"
dono: como a sessao autenticada viaja e por que ela tem estado
ler-junto: [01-arquitetura/seguranca]
status: ativo
---

# ADR-0009: sessão no servidor, não JWT

- **Status:** aceita
- **Data:** 2026-09-07
- **Afeta:** autenticação, `01-arquitetura/seguranca`, `03-dados/modelo-de-dados`, `04-api`

## Contexto

A autenticação é própria, sem provedor externo, e faltava decidir **como a sessão viaja entre
uma requisição e a seguinte**. As duas respostas usuais são um cookie apontando para estado no
servidor, ou um JWT que se valida sozinho.

A premissa que decide não é o tamanho de hoje: **o sistema é para crescer**, de três pessoas
para dezenas, e do Raspberry Pi para a nuvem se o Pi não aguentar. Uma decisão que só funciona
com três usuários está errada agora, não depois.

## Decisão

**Sessão com estado no servidor.** O cookie carrega um identificador **opaco** — `HttpOnly`,
`Secure`, `SameSite=Lax` — e o estado vive numa tabela de sessões. Expiração absoluta **e** por
inatividade. Trocar a senha derruba todas as sessões do usuário.

O que decidiu foi a **revogação**. Um app que guarda dinheiro de outras pessoas precisa que
*"tirar o acesso agora"* tenha efeito no clique seguinte — quando alguém sai de um ambiente
compartilhado, quando um dispositivo é perdido, quando uma senha vaza. Com estado, revogar é
apagar uma linha.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| **JWT stateless** | **Não se revoga.** Até expirar, quem tem o token entra — e o remédio de sempre é uma lista de bloqueio consultada a cada requisição, que é a tabela de sessões de volta, com outro nome e sem as vantagens dela. Fica-se com o custo do estado **e** com a complexidade do token |
| **JWT de vida muito curta + token de renovação** | Encurta a janela sem fechá-la, e o token de renovação **é** estado no servidor. Paga-se a complexidade dos dois desenhos para ter a garantia de um |
| **Sessão em memória do processo** | Some no restart e não sobrevive a um segundo processo. É a decisão que quebra exatamente no dia da migração para a nuvem |

O argumento que costuma salvar o JWT — escalar sem estado compartilhado — **não se aplica**:
uma tabela de sessões no Postgres acompanha o resto do sistema para a nuvem, e o volume dela é
uma linha por login. Com dezenas de usuários, o JWT seria o lado **caro** dessa conta, não o
barato: é onde a lista de revogação começa a ser consultada a cada requisição.

## Consequências

- **Ganhamos:** revogação imediata, sessão inspecionável (por onde alguém entrou, quando usou
  pela última vez) e uma porta só para fechar quando algo dá errado.
- **Perdemos:** uma leitura no banco por requisição autenticada e uma tabela a manter, com
  limpeza das expiradas. É barato, e é barato para sempre.
- **Passa a ser proibido:** token autocontido como prova de sessão; qualquer dado do usuário
  dentro do cookie; sessão que sobreviva à troca de senha.
- **Revisitar se:** a leitura de sessão aparecer em medição real como gargalo. E aí o que entra
  é **cache** da tabela, não JWT — o problema seria de leitura, e trocar por um token que não
  se revoga resolveria o gargalo criando o buraco.
