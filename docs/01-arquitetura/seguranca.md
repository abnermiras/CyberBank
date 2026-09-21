---
id: 01-arquitetura/seguranca
titulo: Segurança
dono: autenticacao, sessao, defesa do login, superficie exposta, segredos e dado sensivel em log
ler-junto: [02-dominio/ambiente-financeiro, 05-integracoes/vault-segredos, 07-operacao/deploy]
status: ativo
---

# Segurança

Este doc parte de duas premissas, e as duas são decisão, não circunstância:

1. **O sistema guarda dinheiro de outras pessoas.** Vazamento é falha crítica, não bug.
2. **O número de pessoas é para crescer.** *"Hoje são três"* não sustenta nada aqui — se o
   Raspberry Pi não aguentar, migra-se para a nuvem, e nenhuma decisão abaixo muda por isso.

E uma regra que organiza o resto: **defesa que depende de alguém lembrar não é defesa.** Cada
item existe porque a estrutura o impõe, não porque alguém vai revisar o código com atenção.

## Autenticação

| | |
|---|---|
| Identificador | O **e-mail**. Único no sistema inteiro, não por ambiente |
| Senha | Hash **Argon2id**, com parâmetros calibrados no host e revistos ao trocar de host |
| Cadastro | **Aberto** a quem alcança o sistema. Ver *O preço do cadastro aberto* |
| Recuperação | Por e-mail, e **só** para isso (`ADR-0007`) |
| Dependência externa | **Nenhuma.** Sem provedor de identidade, sem OAuth de terceiro |

**Argon2id e não bcrypt.** Os dois são aceitáveis; o Argon2id é a recomendação atual e resiste
melhor a quebra com GPU, porque custa **memória** além de tempo. É justamente a memória que
obriga a calibrar: o Pi tem pouca, e parâmetro copiado de tutorial ou trava o login ou não
protege nada. O número calibrado vive em `docs/07-operacao/deploy.md`, não aqui.

**O login regrava o hash que está abaixo do parâmetro atual.** Sem isso, calibrar um host
novo protegeria só quem se cadastrasse depois: o Argon2id guarda `m`, `t` e `p` dentro do
próprio hash e confere por eles, então a conta antiga continuaria no número antigo para
sempre. A regravação acontece **só no login que deu certo** — é o único instante em que o
sistema tem a senha em claro e já provou que ela é a da conta — e na mesma transação que abre
a sessão. Senha errada, conta bloqueada e e-mail inexistente não regravam nada: regravar antes
de conferir seria gravar o hash de uma senha que ninguém provou ser a certa.

Ela não abre oráculo de tempo: o custo extra só aparece **depois** de a senha conferir, e quem
não tem conta nunca chega ali. Quem nunca mais entrar fica no parâmetro antigo, e é o preço
aceito — a alternativa seria pedir troca de senha a todo mundo a cada recalibração.

No código é o `Argon2PasswordEncoder` do Spring Security, e ele **exige o BouncyCastle**
(`bcprov-jdk18on`) — a JDK não traz Argon2. É a única razão de a dependência existir no
`pom.xml`: ela é consequência desta decisão, não escolha de biblioteca de criptografia
(`CLAUDE.md`, regra 3).

**Tamanho mínimo da senha: 8 caracteres.** Escolhido ao implementar o cadastro, porque este
doc não tinha número e *cadastro aberto sem mínimo aceita senha de um caractere*. É o único
requisito de senha: nada de exigir símbolo, dígito ou maiúscula — regra de composição empurra
todo mundo para a mesma senha previsível com um `1!` no fim, e o que protege de verdade aqui é
o Argon2id mais o bloqueio por tentativa.

**O token de recuperação não loga ninguém.** Ele só autoriza **trocar a senha**: uso único,
validade curta, invalidado ao ser usado, ao expirar ou ao pedido de um novo. Token que
autentica é uma segunda porta de entrada, e uma porta a mais é uma porta a mais.

## Sessão

**Sessão no servidor, com identificador opaco em cookie** — `ADR-0009`.

| | |
|---|---|
| O que vai no cookie | Um identificador **opaco**, sem informação dentro. `HttpOnly`, `Secure`, `SameSite=Lax` |
| Onde vive o estado | Tabela de sessões: usuário, criação, último uso, e por onde entrou |
| Expiração | Duas, e as duas valem: **absoluta** desde a criação, e por **inatividade** |
| Revogação | Apagar a linha. **Tem efeito no clique seguinte**, sem esperar token nenhum expirar |
| Trocar a senha | **Derruba todas as sessões** daquele usuário, inclusive a que trocou |
| Quanto dura | **30 dias** de expiração absoluta, **7 dias** de inatividade |
| O que fica no banco | O **hash** do identificador, nunca ele — cópia do banco não vira sessão viva de ninguém |

Os dois prazos foram escolhidos ao implementar; o que não é escolha é haver **os dois**.

## O login é a superfície mais atacada

É a única porta que aceita tentativa de quem não tem nada. As quatro regras:

- **A resposta é sempre a mesma.** E-mail que não existe e senha errada dão a **mesma
  mensagem e o mesmo tempo de resposta**. Dizer *"e-mail não cadastrado"* entrega a lista de
  usuários antes de qualquer senha ser tentada.
- **Atraso progressivo**, contado por **conta** e por **origem**. Só por conta, um atacante
  varre mil contas com uma tentativa cada; só por origem, ele troca de origem.
- **Bloqueio temporário** depois de N falhas, com o desbloqueio pelo tempo — nunca por uma
  tela que o próprio atacante alcança.
- **A recuperação de senha segue as mesmas quatro.** Ela é login por outro nome: aceita
  e-mail de desconhecido e responde. A resposta é idêntica exista ou não a conta.

Os números, escolhidos ao implementar: o atraso dobra a cada falha, de 250 ms até um teto de
4 s, e **5 falhas** bloqueiam a chave por **15 minutos**.

**A troca de senha usa a mesma contagem, por conta.** Errar a senha atual conta como falha de
login (`docs/02-dominio/usuario.md`). Sem isso, quem alcança uma sessão aberta tenta a senha
quantas vezes quiser — e uma sessão roubada, que expira sozinha, viraria posse definitiva da
conta.

**O que faz o tempo ser igual é o Argon2id rodar dos dois lados.** Sem usuário para conferir, o
sistema confere contra um hash de mentira e responde falso — se voltasse na hora, a diferença
de milissegundos entregaria a lista de usuários antes de qualquer senha ser tentada. E a
contagem de falhas é mantida **também para e-mail que não existe**: contar só o que existe
seria o mesmo oráculo por outro caminho.

**A contagem vive na memória do processo**, e o preço está dito: reiniciar zera, e um segundo
processo teria a sua. É aceitável pelo que ela protege — ela atrasa e bloqueia, não autoriza
ninguém —, ao contrário da sessão, que em memória sumiria no restart e derrubaria todo mundo
(`ADR-0009`). Vira tabela quando a contenção do cadastro aberto entrar.

## O preço do cadastro aberto

Qualquer pessoa que alcance o sistema cria conta. **É decisão tomada**, e o gatilho para
fechar continua escrito: **sair da rede local.** O gatilho já foi olhado uma vez e a escolha
foi manter aberto, com a lógica de contenção para depois.

O que segura a decisão de pé enquanto isso é o modelo, não a boa vontade: **o cadastro não dá
acesso a nada.** Ele cria o usuário, o *Ambiente Pessoal* dele e as categorias de sistema
dele — tudo vazio, tudo isolado pelo `ADR-0002`. Um curioso que se cadastra fica sozinho numa
sala vazia; para chegar ao dinheiro de alguém, precisa ser **convidado**.

O que ele custa, dito por inteiro: consumo de recurso do host, uma tabela de usuários que
cresce sem controle, e o e-mail de recuperação virando ferramenta de incômodo. As três se
resolvem com contenção — limite por origem e verificação de e-mail no cadastro —, e é isso
que fica para depois.

## O ambiente entra pelo contexto, nunca pelo cliente

O `ADR-0002` exige o ambiente no contexto da requisição autenticada. Como isso acontece:

1. O ambiente ativo vem no **caminho da URL** (`/api/v1/ambientes/{id}/...`).
2. Um filtro resolve o `{id}`, **verifica o acesso do usuário autenticado** e só então põe o
   ambiente no contexto. Nenhum controller recebe `ambienteId` de corpo ou de query. Ele roda
   **depois** do filtro de sessão, e a ordem é a regra: sem saber quem está falando não há
   acesso nenhum a validar.
3. A transação faz o `SET LOCAL` para a política de RLS.

**Ambiente que existe e não é seu responde igual a ambiente que não existe.** Diferenciar
`403` de `404` conta ao curioso que aquele ambiente existe, e o número dele é sequencial ou
adivinhável — é enumeração pela porta dos fundos.

## Injeção

**Toda consulta é parametrizada, sem exceção** — inclusive query nativa e `JdbcTemplate`.
Concatenar valor em SQL é proibido, e não por convenção: é a regra que a revisão procura.

E existe rede embaixo. O RLS do `ADR-0002` vale **por conexão**, não por consulta: uma
injeção que passasse ainda assim não enxerga dado de outro ambiente. É a diferença entre
*"vazou tudo"* e *"não vazou nada"* — e é a razão de o RLS não ser redundante com o filtro da
aplicação.

## O que fica exposto

| | |
|---|---|
| **HTTPS sempre** | Não "quando sair da rede local". Sem ele o cookie de sessão viaja em claro, e nada acima importa. `Secure` no cookie **exige** HTTPS |
| Quem escuta na rede | **Só a aplicação.** O Postgres nunca é publicado — nem na rede local |
| O host | O Pi **não é fronteira de segurança**. Nada aqui vale por estar "dentro de casa": é o mesmo desenho que vai para a nuvem |

## Segredos

Doc dono: `docs/05-integracoes/vault-segredos.md`. O que vale desde já:

- Segredo entra por **variável de ambiente**, nunca por arquivo versionado.
- **Nenhum segredo no repositório**, em nenhuma forma — e o `.gitignore` é parte da defesa.
- A senha de app do Gmail (`ADR-0007`) é segredo como outro qualquer.
- Segredo vazado se **troca**, não se apaga do histórico: quem já leu, já leu.

## Dado sensível em log

**Nunca vão para log:** senha, hash, token de sessão ou de recuperação, cookie, e qualquer
segredo.

**Também não vão:** valor, descrição e categoria de lançamento. É o dinheiro do usuário, e a
razão é estrutural — **log de aplicação não tem RLS**. Todo o isolamento do `ADR-0002` para na
borda do arquivo de log, que é lido por quem tem o host e vai junto em qualquer suporte.

Identificador (de lançamento, de conta, de ambiente) pode ir: ele localiza a linha sem contar
o que ela diz.

## Invariantes

- Toda requisição autenticada carrega **um** ambiente, resolvido do caminho da URL e validado
  contra o acesso do usuário. Nenhum outro caminho põe ambiente no contexto.
- Ambiente sem acesso e ambiente inexistente têm **a mesma resposta**.
- E-mail inexistente e senha errada têm **a mesma resposta e o mesmo tempo**. Vale para o
  login e para a recuperação.
- Nenhuma consulta concatena valor em SQL.
- O cookie de sessão é sempre `HttpOnly`, `Secure` e `SameSite=Lax`, e o que ele carrega é
  **opaco**.
- Trocar a senha derruba **todas** as sessões do usuário.
- Hash abaixo do parâmetro atual é regravado no login que deu certo, e só nele.
- O token de recuperação **nunca** autentica: ele só autoriza a troca de senha, uma vez.
- Nenhum segredo é versionado, e nenhum valor monetário ou descrição de lançamento vai para
  log.

## Ainda em aberto

- [ ] **Como o bot do Telegram prova que a mensagem veio de quem diz ter vindo.** Fase 2, com
      `docs/05-integracoes/telegram-bot.md`
- [ ] **Contenção do cadastro aberto** — limite por origem e verificação de e-mail. Decidido
      que fica para depois; o gatilho para fechar de vez continua sendo sair da rede local

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Por que o isolamento tem duas camadas | `ADR-0002` |
| Por que sessão no servidor e não JWT | `ADR-0009` |
| Por que o sistema manda e-mail, e só para isso | `ADR-0007` |
| Papéis, convite e quem pode o quê num ambiente | `02-dominio/ambiente-financeiro` |
| O que o usuário tem, e o que ele muda em si mesmo | `02-dominio/usuario` |
| Onde os segredos ficam e como o sistema os lê | `05-integracoes/vault-segredos` |
| Parâmetros do Argon2id no host, e HTTPS na prática | `07-operacao/deploy` |
