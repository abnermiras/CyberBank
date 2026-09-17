---
id: 01-arquitetura/decisoes/ADR-0014-usuario-e-um-assunto
titulo: "ADR-0014: o usuario e um assunto proprio, e sai de dentro de ambiente"
dono: por que usuario, sessao e senha deixam de morar no pacote do ambiente
ler-junto: [01-arquitetura/decisoes/ADR-0008-roteador-vale-para-o-codigo, 02-dominio/usuario]
status: ativo
---

# ADR-0014: o usuário é um assunto próprio, e sai de dentro de `ambiente`

- **Status:** aceita
- **Data:** 2026-09-17
- **Afeta:** `02-dominio/usuario`, `02-dominio/ambiente-financeiro`, `04-api/endpoints-usuario`, `04-api/endpoints-ambientes`, `src/main/java/br/com/cyberbank/`

## Contexto

`Usuario`, `Sessao`, `Senhas` e a política de login nasceram na fatia 1 dentro do pacote
`ambiente/`, por uma razão de conveniência: eram as quatro tabelas da `V001` e ninguém tinha
escrito um doc de usuário. Pelo `ADR-0008`, **o pacote tem o nome do doc dono** — e o doc dono
de `ambiente/` se declara dono de *"papéis, acesso, convite, ciclo de vida e a regra de
isolamento"*, que não cobre nenhum dos quatro.

A tela de Perfil torna isso visível: avatar, chat do Telegram e troca de senha são fatos do
usuário, e não existe leitura de `ambiente-financeiro.md` em que eles caibam. O endereço do
código sairia de um doc que não os possui — que é exatamente o que o `ADR-0008` existe para
impedir.

## Decisão

**O usuário é um assunto.** Nasce `docs/02-dominio/usuario.md`, e com ele o pacote
`br.com.cyberbank.usuario`, para onde vão `Usuario`, `Sessao`, `Senhas`, `PoliticaDeLogin`,
`RegistroDeTentativas`, `IdentificadoresDeSessao`, os repositórios e entidades dos dois, o
cadastro, o login, o logout e os interceptadores de sessão.

Em `ambiente/` fica o que o doc dele possui: `Ambiente`, `Acesso`, `Papel`, a lista de
ambientes do usuário, o interceptador que resolve o `{ambienteId}` e o `AmbienteDaRotina`.

**O cadastro continua sendo um caso de uso da `aplicacao` de `usuario`**, e continua juntando
três assuntos — usuário, ambiente e categoria — chamando a `aplicacao` dos outros dois. É o
`ADR-0010` valendo como sempre: quem junta é a aplicação, e domínio nenhum importa domínio.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| Deixar tudo em `ambiente/` e escrever o perfil lá | O doc dono passaria a possuir avatar e Telegram, que não têm nada com ambiente. Um doc que possui tudo não roteia nada, e o `ADR-0008` morre por dentro sem ninguém notar |
| Criar `usuario/` só para o novo, e deixar login e sessão onde estão | Dois endereços para um assunto. A troca de senha derruba todas as sessões: a regra ficaria de um lado e a tabela do outro, que é a divisão que mais custa |
| Um pacote `identidade/` cobrindo usuário e sessão | Nome que não é o de nenhum doc. O `ADR-0008` derivaria o endereço errado na primeira tarefa |

## Consequências

- **Ganhamos:** o endereço do código volta a sair do nome do doc para os dois assuntos;
  `ambiente-financeiro.md` volta a caber no próprio `dono:`; e o perfil tem onde nascer.
- **Perdemos:** um commit de mudança mecânica em cerca de vinte arquivos, sem
  comportamento novo — e o histórico de `git blame` desses arquivos ganha uma camada.
- **Passa a ser proibido:** classe de usuário, senha ou sessão dentro de `ambiente/`; e
  `usuario/dominio` importar `ambiente/dominio`, como entre quaisquer dois assuntos
  (`ADR-0010`, e o teste de fronteira reprova o build).
- **Revisitar se:** o convite obrigar `usuario` e `acesso` a se lerem em transação —
  o encontro é na `aplicacao`, e se ele não couber lá, é sinal de que a fronteira está no
  lugar errado, não de que ela deva sumir.
