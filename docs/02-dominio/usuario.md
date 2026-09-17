---
id: 02-dominio/usuario
titulo: Usuario
dono: o usuario como assunto: identidade de login, nome, avatar, o chat do Telegram e a troca de senha
ler-junto: [01-arquitetura/seguranca, 02-dominio/ambiente-financeiro, 06-interface/perfil]
status: ativo
---

# Usuário

Uma frase separa este doc do vizinho: **o usuário é quem entra; o ambiente é quem tem o
dinheiro.** Nada aqui tem `ambiente_id`, nada aqui é filtrado por `ADR-0002`, e nenhuma
regra daqui muda quando a pessoa troca de ambiente.

O que é decisão de **arquitetura** — hash da senha, forma da sessão, defesa do login — mora em
`docs/01-arquitetura/seguranca.md` e não se repete aqui. Este doc é dono do que o **usuário
tem** e do que ele pode mudar em si mesmo.

## O que o usuário tem

| Campo | Regra | Muda? |
|---|---|---|
| `email` | Identificador de login, único no sistema inteiro, sempre minúsculo | **Não** — ver abaixo |
| `nome` | Exibição. É o `autor` que o lançamento mostra em ambiente compartilhado. 1 a 120 caracteres | Sim, livremente |
| `senhaHash` | Argon2id (`docs/01-arquitetura/seguranca.md`) | Sim, pela troca de senha |
| `avatar` | O **nome** de um dos dez desta página. Nunca nulo | Sim, livremente |
| `telegramChatId` | Declarado pela pessoa, opcional, único. **Não é verificado** | Sim, e pode ser apagado |
| `criadoEm` | Instante do cadastro, em UTC | Nunca |

**Não existe usuário inativo.** Quem sai de um ambiente perde o acesso àquele ambiente
(`docs/02-dominio/ambiente-financeiro.md`); nada disso mexe no usuário. Uma conta desativada
seria estado novo com um caso a mais em cada tela, e ninguém pediu.

## O e-mail não muda

Ele é três coisas ao mesmo tempo, e é por isso que trocá-lo não é editar um campo:

1. **É o identificador de login** — trocar é trocar a chave de entrada.
2. **É a chave do convite pendente.** Convite para quem ainda não tem cadastro fica esperando
   naquele e-mail (`docs/02-dominio/ambiente-financeiro.md`); trocar abandona o convite num
   endereço que não é mais de ninguém.
3. **É a única porta da recuperação de senha** (`ADR-0007`).

Na tela ele aparece, e aparece **travado dizendo por quê** — campo que some é campo que o
usuário procura na tela errada.

## O cadastro: um ato, três efeitos

| Efeito | Detalhe |
|---|---|
| Cria o **usuário** | Com o avatar já sorteado, e sem Telegram |
| Cria o **"Ambiente Pessoal"** | Nome padrão, renomeável, do qual ele é dono (`docs/02-dominio/ambiente-financeiro.md`) |
| Cria as **categorias de sistema** dele | O jogo completo, dentro daquele ambiente (`docs/02-dominio/categoria.md`) |

Os três numa transação só: usuário sem ambiente, ou ambiente sem as categorias, é estado que
nenhuma tela sabe mostrar e nenhum caminho do sistema sabe consertar depois.

**O cadastro não dá acesso a nada além disso** — é o que sustenta o cadastro aberto
(`docs/01-arquitetura/seguranca.md`). Para chegar ao dinheiro de alguém é preciso ser
convidado.

## O avatar

Não há upload de foto. O que existe é uma **lista fechada de dez**, e o usuário escolhe um.

| Regra | Por quê |
|---|---|
| O domínio guarda o **nome**, nunca o desenho | Mesma regra da cor de categoria: `VISOR` é o dado, o SVG é decisão de `docs/06-interface/direcao-visual.md`, e trocá-lo é editar uma linha de CSS — não uma migration |
| **Nasce sorteado**, no cadastro | Ninguém escolhe avatar antes de entrar. E usuário sem avatar obrigaria **toda** tela a ter um caso "sem foto" — um caso a mais em cada lugar, para sempre |
| Nunca é nulo | Consequência da linha acima |
| Troca livre, sem consequência nenhuma | Não é identidade de segurança: é como a pessoa se reconhece na lista |
| A lista **não cresce por pedido** | Dez é o que cabe num seletor sem rolagem. Upload é outra decisão, e não foi tomada |

**O sorteio recebe a fonte de aleatoriedade de fora**, do mesmo jeito que o caso de uso já
recebe o `Clock`. Sorteio embutido no domínio faz o teste do cadastro não ser determinístico,
e teste que às vezes passa não é teste.

Os dez, e o que cada um é:

| Nome | Desenho |
|---|---|
| `VISOR` | Capacete de visor, faixa luminosa no lugar dos olhos |
| `OLHO` | Íris de câmera, anel e diafragma |
| `GATO` | Silhueta de gato, uma orelha virada em antena |
| `CAVEIRA` | Caveira em grade, estilo dos primeiros terminais |
| `DRONE` | Quadricóptero visto de cima |
| `CIRCUITO` | Trilhas de placa saindo de um núcleo |
| `ONDA` | Forma de onda de um sinal |
| `TORRE` | Antena de transmissão com os arcos do sinal |
| `PRISMA` | Prisma triangular refratando uma linha |
| `ROBO` | Cabeça de robô, dois olhos e uma haste |

A referência é o **gênero** cyberpunk, nunca arte, marca ou personagem de obra existente
(`docs/06-interface/direcao-visual.md`).

## O chat do Telegram

**A pessoa informa o `chat id`, e o sistema não tenta adivinhar nem parear.** Não há código de
confirmação, não há mensagem de teste, não há busca por `@`: o campo guarda o número que ela
digitou. **Informar o id errado é responsabilidade de quem informou.**

| Regra | Por quê |
|---|---|
| É o **`chat id`**, não o `@` | O `@` é trocável pelo dono a qualquer momento e não identifica ninguém. Quem identifica um destino no Telegram é o `chat id` |
| **Único no sistema** | Dois usuários com o mesmo id tornam ambígua qualquer mensagem que chegue daquele chat. A unicidade não impede o erro de digitação — impede a **ambiguidade** |
| Opcional, e apagável | Apagar o campo desfaz o vínculo. Ninguém é obrigado a ter Telegram |
| Inteiro diferente de zero | É tudo que se valida. O resto é do bot |
| **Nada é enviado hoje** | O bot é Fase 2 (`docs/05-integracoes/telegram-bot.md`, hoje stub) |

**O preço, dito por inteiro:** com o campo preenchido errado, o sistema é capaz de mandar dado
financeiro para o chat de outra pessoa, e nada escrito aqui o impede. O que impede é o bot
pedir uma confirmação no dia em que ele existir — e essa é decisão de
`docs/05-integracoes/telegram-bot.md`, que é stub e por isso não se deduz.

**O que este doc não decide:** se um chat de grupo (id negativo) serve, o que o bot faz com o
vínculo, e se ele vira prova de identidade. As três são do bot.

## A troca de senha

| Regra | Detalhe |
|---|---|
| **Exige a senha atual, sempre** | É o que impede que uma sessão esquecida aberta vire troca de dono da conta |
| A nova segue o mínimo de 8 | `docs/01-arquitetura/seguranca.md` é o dono do número |
| A nova **não pode ser igual à atual** | Trocar por si mesma derruba todas as sessões sem trocar nada |
| **Derruba todas as sessões, inclusive a que trocou** | `docs/01-arquitetura/seguranca.md`. Na prática: a pessoa volta para o login logo depois de trocar, e a tela avisa **antes** |
| Errar a senha atual **conta para o mesmo bloqueio do login** | Por conta. Sem isso, quem pegou uma sessão aberta tem tentativas infinitas para descobrir a senha e tomar a conta de vez |

**Não existe "esqueci a senha".** A recuperação por e-mail está decidida (`ADR-0007`) e **não
está construída** — quem perdeu a senha hoje não tem caminho de volta pelo sistema. O perfil
não substitui a recuperação: ele exige saber a senha atual, que é exatamente o que quem
esqueceu não sabe.

## Invariantes

- O e-mail é único no sistema inteiro, sempre minúsculo, e **nunca muda**.
- Todo usuário tem exatamente um avatar, e ele é um dos dez nomes desta página.
- Dois usuários nunca têm o mesmo `telegramChatId`; nenhum usuário tem mais de um.
- Trocar a senha exige a senha atual e derruba **todas** as sessões do usuário.
- Nenhum dado deste doc é filtrado por ambiente — o usuário não pertence a ambiente nenhum.
- O usuário só lê e altera **a si mesmo**. Não existe endpoint que leia o perfil de outro.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Hash da senha, forma da sessão, defesa do login e o mínimo de 8 | `01-arquitetura/seguranca` |
| Por que sessão no servidor | `ADR-0009` |
| Por que o usuário é um assunto, e não parte do ambiente | `ADR-0014` |
| Papéis, convite, saída e o que o ambiente é dono | `02-dominio/ambiente-financeiro` |
| Como os dez avatares são desenhados | `06-interface/direcao-visual` |
| Como a tela de Perfil se organiza | `06-interface/perfil` |
| Rota, payload e erros | `04-api/endpoints-usuario` |
| Colunas, constraints e migration | `03-dados/catalogo-tabelas` |
