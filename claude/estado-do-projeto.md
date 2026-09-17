# Cyberbank — estado do projeto

> **Este doc não é documentação do sistema.** A documentação vive em `docs/`, com `CLAUDE.md`
> como roteador. Aqui ficam as **decisões tomadas, o que falta decidir e como se trabalha** —
> para uma sessão nova entender onde paramos sem ler o repositório inteiro.
>
> **Ele está fora do roteador de propósito** (`ADR-0008`): é doc de passagem entre sessões, não
> de tarefa, e não deve entrar no custo de rota nenhuma.

Última sessão: **2026-09-17**, na máquina Linux, com o Claude Code no terminal.
**A fatia 3 está de pé: dá para abrir conta com saldo, cadastrar meio de pagamento, lançar,
transferir e ver o Extrato — e "em caixa", "guardado" e "patrimônio" saem do banco, somados
dos lançamentos.** O vale-refeição fica fora do caixa e a transferência não mexe no
patrimônio, os dois conferidos contra Postgres real.

**O esqueleto está commitado e no GitHub:** `4be103d` na branch `esqueleto-do-projeto`,
mergeado em `main` por `9e015a2`. O bloqueio do push acabou.
`./mvnw verify` verde — **137 testes de unidade e 76 de integração** —, check de docs em 0
erros e 0 avisos. **`main` está à frente de `origin/main`**, do `51edd8e` para cá: o push é
comando entregue ao Abner, e o de 17/09 ainda não saiu.

| Sessão | O que saiu |
|---|---|
| **17/09 (o detalhe do lançamento)** | A linha do Extrato **abre**: modal com o lançamento inteiro, e as ações saem da linha para ele · nasce `GET /lancamentos/{id}`, o **único lugar com os nomes resolvidos** — a URL que o `POST` já devolvia no `Location` desde a fatia 3 e não respondia · **`?alvo=` nos eventos**: o `listarDoAlvo` estava implementado no repositório, com índice, e **sem um único chamador** desde 17/09 — é o *"sempre com histórico"* do `lancamento.md` aparecendo pela primeira vez na tela · **`#/extrato/88`**: o roteiro do front passa a entender argumento, e o *abrir* do Diário leva ao **objeto**, não à tela dele · o item do evento passa a carregar o `dia`, porque no histórico cada um é de um dia diferente e **dia não se deriva de instante** · dois achados do navegador: o modal **não fechava ao trocar de tela**, e o de/para sumia quando a correção **preenchia** um campo vazio (`categoriaIdPara` sem `De`) |
| **17/09 (a Home vira cockpit)** | **Os dois últimos stubs de tela e de API saíram**: `06-interface/dashboard.md` e `04-api/endpoints-relatorios.md` · nasce `GET /relatorios/resumo`, **um endpoint só** — duas metades da tela discordando é pior que a tela demorar · a regra do gasto sai do protótipo e vira domínio testável (`RelatorioDoMes`): **quem manda é o sentido da categoria**, categoria de sistema fora, só conta de fluxo, agrupa pela raiz — e **categoria que zerou no mês some da lista** · a Home ganha **gasto por categoria com barra**, **pendências que se resolvem ali mesmo**, o bloco **reservado da fatura**, **o que vem por aí** com `T−n` e os totais a pagar/a receber, e **o mês em números** · a **sobra até o fim do mês deixou de ser um traço** e mostra a conta que a produziu · achado: o protótipo somava receita por `entraEmCaixa` e gasto por `entraNoFluxoDeCaixa` — aqui os dois usam **fluxo de caixa**, senão o benefício entraria num lado só |
| **17/09 (o perfil, de pé)** | Três commits, na ordem combinada. **O `ADR-0014` saiu do papel**: `usuario/` é pacote, e `CriarAmbientePessoalUseCase` passa a ser o encontro dos dois assuntos na aplicação · **`V007`**, `Avatar` com os dez nomes fechados, o sorteio recebendo a aleatoriedade de fora, e o backfill dando avatar a quem já existia · `GET`/`PATCH /usuarios/atual`, `PUT`/`DELETE /usuarios/atual/telegram` e `PUT /usuarios/atual/senha` · **o Telegram virou sub-recurso no meio do caminho**: o doc dizia campo do `PATCH` com `null` apagando, e o `PerfilIT` provou que ausente e `null` chegam iguais em JSON — o preço seria o vínculo sumir ao trocar só o nome · a **tela** nasce com os dez SVGs, os três blocos reservados e a porta no avatar do canto superior direito · **129 testes de unidade e 65 de integração**, e o navegador dirigido em 13 passos |
| **17/09 (o perfil, no papel)** | **Sessão só de documento, sem uma linha de código.** O usuário vira **assunto** (`ADR-0014`): `usuario.md` nasce, e com ele o pacote `usuario/` para onde `Usuario`, `Sessao`, `Senhas` e o login saem de `ambiente/` — o `ADR-0008` derivava o endereço de um doc que não possuía nem avatar nem Telegram · nasce a **tela de Perfil** (`06-interface/perfil.md`), com a porta no avatar do canto superior direito, e os blocos de **convite** e de **sessões** como espaço reservado que diz o que espera · **dez avatares monocromáticos**, e a razão saiu da regra que já existia: a paleta de identidade tem oito tons e o nono está proibido, então quem distingue é a **forma** · o **chat id do Telegram é declarado pela pessoa, sem pareamento** — e o doc nomeia o preço, que é poder mandar dado para o chat errado · `endpoints-usuario.md` leva cadastro, login e logout embora de `endpoints-ambientes.md` |
| **17/09 (lançar em qualquer tela)** | **O código alcançou o `navegacao.md`**, que desde sempre dizia *"um botão presente em qualquer tela (e a tecla `N`)"* e *"o formulário completo é a saída do quick-add, não um caminho paralelo"* — e a fatia 3 tinha construído o oposto: dois painéis sempre abertos dentro do Extrato, e nenhum jeito de lançar de fora dele. Nasce `Lancar`, global: FAB, tecla `N`, painel curto e modal completo com **GASTO · RECEITA · TRANSFERÊNCIA** e **CRÉDITO desabilitado dizendo o que falta**. **Não há aba de boleto**, e a ausência é a regra no lugar certo: boleto é um *meio*, e é o meio que decide se existem duas datas · o seletor de categoria do quick-add agrupa por `optgroup` em vez de achatar em "Raiz › Filha" |
| **17/09 (a tela do Diário)** | O Diário **saiu da Fase 2** e foi antecipado: evento gravado e nunca olhado é evento que ninguém sabe se está certo, e a tela é como se valida a gravação. Nasce `GET /eventos?dia=`, a **única lista do sistema sem cursor** — a pergunta é sobre um dia, e um dia fecha quando acaba. Dia no futuro é **422**, não lista vazia: vazio diria "nada aconteceu", e amanhã é um dia que não aconteceu. A frase de cada linha é montada na tela, por tipo |
| **17/09 (evento e a rotina)** | **`V006`**: a tabela `evento`, imutável por ausência de política de `UPDATE` e `DELETE` · nasce o pacote `evento/` e a **rotina diária**, que vira `PREVISTO` em `REALIZADO` pela data — a regra existia no domínio desde a fatia 3 e **ninguém a chamava** · **`ADR-0013`**: a rotina atravessa o RLS por função `SECURITY DEFINER` mais uma política escrita para o papel dono, porque `FORCE ROW LEVEL SECURITY` sujeita o dono às políticas · a lista fechada de tipos ganha os **sete** que o app já fazia e o doc não previa (renomear/reativar/excluir conta e meio, recolorir categoria) · treze casos de uso passam a gravar evento · e apareceu de brinde o **`CATEGORIA_COM_LANCAMENTO`**, que estava no catálogo de erros e em nenhum lugar do código |
| **17/09 (o calendário)** | O botão ▦ do campo de data abria o **seletor nativo do navegador**, cinza do Windows dentro de um app cyberpunk. Não era ajuste de CSS: aquele painel é pintado pelo sistema operacional e não aceita tema. Nasce o **`Calendario`**, desenhado no CSS do projeto — e ele mora no `body` em `position:fixed`, porque `.panel` usa `clip-path` e recortaria um popup absoluto pela metade. Os três `input[type=date]` escondidos morreram com o `showPicker` |
| **15/09 (fatia 2 e a cor)** | O front encosta na API: **cadastro de categorias** na tela. E a **decisão 0 fechou** — cor de categoria é **identidade, não semântica**, e por isso são **duas paletas** separadas, escritas em `direcao-visual.md`. A exceção que o seed já praticava (`LAZER` rosa sem alertar nada) virou regra em vez de continuar sendo desvio |
| **16/09 (o seletor de categoria)** | Bug do Abner: o combo de categoria do Extrato achatava a árvore e mostrava só as subcategorias, sem a raiz. Virou **dois combos** — categoria e subcategoria —, com o segundo aparecendo só quando a raiz deixa de ser escolhível. E ao conferir a regra apareceu a **segunda metade**: *"na hora de lançar, só aparecem as categorias compatíveis com o sentido"* **não estava implementada em lugar nenhum** — nem na tela, nem no servidor. Nasce `CATEGORIA_DE_OUTRO_SENTIDO` |
| **16/09 (UX de conta e meio)** | **O meio perdeu o nome**: só o `CREDITO` tem, e nele o nome é a identidade do cartão. Nos outros o par **`(conta, tipo)` identifica**, e a tela lê "Nubank · Pix" · **os meios são escolhidos no cadastro da conta**, num formulário só — o cadastro de meio à parte morreu · **`TED` e `DESCONTO_EM_FOLHA`** entram, e `meio-de-pagamento.md` passa a nomear as **três famílias de tipo** · **`V005`** · `GET /contas` passa a servir o **catálogo de tipos**, e a tabela duplicada no JavaScript morreu |
| **16/09 (fatia 3)** | **`V004`**: `conta`, `meio`, `lancamento` e **`vinculo` nascendo vazia**, numa migration só — as quatro se referenciam em ciclo, e a política de `conta` lê `vinculo`. **O `OR` do `ADR-0004` está valendo**, e o `WITH CHECK` **não** o leva · os três pacotes de domínio, com `AbrirContaUseCase` juntando conta + lançamento na `aplicacao` · Extrato **paginado por cursor** · `endpoints-contas`, `-meios-pagamento` e `-lancamentos` **saíram de stub** · `catalogo-tabelas` **quebrado em dois** por família · a tela de **Extrato** nasceu e o **Cadastro ganhou abas** (categorias · contas e meios) · **98 testes de unidade e 42 de integração**, verdes |
| **07/09 (fatia 1)** | **`V001`** (usuario, sessao, ambiente, acesso) e **`V002`** (categoria, e com ela o padrão de RLS que as outras oito copiam) · o **`catalogo-tabelas.md` nasceu** · **cadastro, login, logout e sessão** ponta a ponta, com as quatro regras de login · o **filtro do `{ambienteId}`** que valida acesso e faz o `SET LOCAL` · **`endpoints-ambientes.md`** nasceu e **`endpoints-categorias.md`** saiu de stub · `EMAIL_JA_CADASTRADO` entrou no catálogo de erros · 29 testes de domínio/arquitetura e 10 de integração, verdes |
| **07/09 (noite)** | **`/esqueleto` executado, uma vez só**: `pom.xml` (módulo único, Java 21, **Spring Boot 4.0.8**, Flyway, Argon2 do Spring Security, ArchUnit e Testcontainers), `mvnw`, `compose.yml` com **`postgres:18.4` na 5433** e o script dos **dois papéis**, `.env.exemplo`, `application.yml` com `ddl-auto: validate`, `CyberbankApplication` e o **teste de arquitetura com as três regras do `ADR-0010`** — passando vazio, que é o esperado. `./mvnw verify` verde e a aplicação sobe, com o Flyway conectando **como dono**. **Nada de domínio.** Depois, o **`PapeisDoBancoIT`**: a suíte de integração sobe o contêiner com o **mesmo script do compose** e exige que o papel da aplicação não seja superusuário, não tenha `BYPASSRLS` e não seja dono do `public` — a montagem de que o `ADR-0002` depende, e a única parte dele que quebrava em silêncio. E o **BouncyCastle** ficou registrado no `seguranca.md`, que é de onde ele vem (regra 3). |
| **07/09** | A fatura fechou: **encerrada não abre** · **`CARTAO` não tem saldo de abertura** · **a janela é uma só** (`FECHADA` com `a pagar` > 0 é o que se abre **e** o que se paga) · limpeza dos resíduos do `d42e378` · **`ADR-0008`**, o roteador valendo para o código · este doc veio para o repositório · **`D1` fechado**: `seguranca.md` escrito e **`ADR-0009`** · **`D2` fechado**: `04-api/convencoes.md` e `erros.md` · **`D3` fechado**: `modelo-de-dados.md` e `migrations.md` · **`01-arquitetura/` escrito** e **`ADR-0010`** · nasce o fluxo **`novo-caso-de-uso`** · **`07-operacao/build-e-run` e `testes`**, **`ambientes-de-execucao`**, **`ADR-0011`** e finalmente o **`.gitignore`** · o `CLAUDE.md` passa a apontar para este doc no início de sessão, e nascem os comandos `/esqueleto` e `/caso-de-uso` |
| **01/09** | Nasce **excluir lançamento** · **o sistema nunca reescreve um pagamento** · **o fechamento para de criar o pagamento previsto** · **`ADR-0007`** (e-mail só para recuperar senha) · cadastro aberto · dia local = horário de Brasília · **`B26`: nada do RaspyBank atravessa** |
| **30/08** | Cadastro de subcategoria · inativação · **mover morre** · o Extrato estava morto havia dois dias · nasce o **Evento** e o **Diário** |
| **29/08** | As 10 contradições, os buracos de regra de Fase 1 e as 5 decisões de negócio que faltavam |

O rastro item a item das contradições e buracos está em `claude/lacunas-para-codigo.md` (no
projeto do Claude); as sessões de 01/09 e 07/09 têm doc próprio lá.

## Onde fica

- Repositório local: `D:\Projetos\CyberBank` (máquina Windows "windowsdev")
- GitHub: `abnermiras/CyberBank`
- Protótipo navegável: `prototipo/index.html` — abre no navegador, sem build
- **`node prototipo/verificar.js`** roda todas as provas sem navegador. **Rodar antes de todo
  commit que mexa em `prototipo/`**
- Ferramenta de docs: `python3 docs/_tools/docs.py check | index | custo`
- Para trabalhar numa conversa nova: conectar a pasta pelo botão **"Adicionar pasta"** no app
- **O push sai da máquina do Abner**: o shell do Claude não tem as credenciais dele. Sempre
  entregar o comando, nunca tentar dar push daqui

## O que é

Gestão financeira pessoal auto-hospedada e multiusuário. Evolução do RaspyBank — **não é
refatoração**: domínio e arquitetura novos.

**Método:** planejamento antes de código, e o protótipo em `prototipo/` como banco de provas.
Ele já pegou **vinte e três** erros de modelo, de tela e de leitura de doc. Os instrumentos, e
o que cada um **não** vê:

| Instrumento | Pergunta que responde | Cego para |
|---|---|---|
| **`CB.conferir()`** | O **estado** viola alguma invariante escrita nos docs? | A tela. Nunca carrega o `app.js` |
| **Varredura de coerência** | Dois docs mandam coisas opostas? | O que está escrito, é coerente e é errado |
| **Usar a tela como usuário** (achado 19) | A funcionalidade existe mesmo? | A tela que a rodada **não** está mexendo |
| **`grep` antes de afirmar** (achado 20) | O doc já decidiu isso? | — |
| **"Para que essa regra existe?"** (achado 21) | Ela pertence a este sistema? | — |
| **`verificar.js`** (achado 22) | A tela chama só o que o modelo exporta? Toda tela renderiza? | Aparência e layout |
| **Escape escrito é escape exercitado?** (achado 23) | O caso previsto no doc chega a rodar? | — |
| **Somar os números da regra** (07/09) | A regra converge, ou se repete para sempre? | O que não tem aritmética dentro |

*Regra que ninguém executa é regra que ninguém verifica.*

**Duas regras de conversa, pedidas pelo Abner e que valem como método:**

1. **Um item por vez.** Decidir, fechar por completo, e só então ir para o próximo.
2. **Achado sem dúvida real não vira pergunta.** Se não existe uma segunda saída defensável, é
   redação: corrige e segue. Pergunta inflada gasta a atenção no item que menos precisa dela.

E uma terceira, aprendida em 07/09: **antes de decidir onde uma coisa encaixa, verificar se ela
existe.** A pergunta *"em que fatura entra o lançamento de abertura de uma `CARTAO`?"* teria
produzido uma regra correta para um lançamento que não devia existir.

## Decisões — projeto e arquitetura

| Decisão | Valor |
|---|---|
| Stack | Java 21 + Spring Boot · PostgreSQL · Docker |
| Hospedagem | Raspberry Pi por ora (~3 usuários), nuvem depois — nada pode depender do Pi |
| Aposta do produto | Enxergar a vida financeira inteira: entrada, gasto, investido, guardado, metas |
| Conceito estruturante | **Ambiente financeiro** é o dono do dado; usuário só tem *acesso* a ambientes |
| Papéis | **Dono, editor, leitor.** Na tela são dois: "autorização completa" = editor; "somente leitura" = leitor. Convidar e excluir são só do dono |
| Isolamento | Filtro no repositório + RLS no Postgres — `ADR-0002`, estendido pelo `ADR-0004` |
| **A armadilha que anula o RLS** | O Postgres **não aplica política ao dono da tabela** nem a quem tem `BYPASSRLS`. Então: a aplicação conecta com papel **não-dono**, toda tabela leva `FORCE ROW LEVEL SECURITY`, e migration roda com o dono. Sem os três, a política existe, aparece no `psql` e **não filtra nada** |
| **Três famílias de tabela, três proteções** | Do **ambiente** (`ambiente_id` + RLS) · do **usuário** (`usuario`, `sessao`, `convite` — protegidas pela sessão) · de **ligação** (`ambiente`, `acesso`, `vinculo` — definem quem vê o quê). **Não há quarta**: tabela nova cai numa delas, e "abrir exceção" não é resposta |
| **Chave** | `bigint` `GENERATED ALWAYS AS IDENTITY`. Não é UUID, e a razão já estava escrita no `convencoes.md`: o identificador **não é segredo** — quem protege é o `ADR-0002` |
| **Enum é `varchar` com `CHECK`** | Não o tipo nativo. O projeto **já mudou um enum no meio do desenho** (`situacao` ganhou `PROVISIONADO`), e não há razão para achar que foi a última vez |
| **Lançamento é uma tabela só** | Sem herança, sem tabela por tipo, sem coluna `tipo`. É consequência de *saldo é a soma dos lançamentos*: tabela por tipo faria toda consulta de saldo unir dez tabelas, e a primeira esquecida daria saldo errado calado |
| **Nada derivado é coluna** | Saldo, os três números da fatura, dívida, limite, patrimônio, escolhível e pendente. Coluna a mais é fonte de verdade a mais para divergir — e num app de dinheiro ela diverge em silêncio |
| **Migration: Flyway com SQL puro** | O agnosticismo do Liquibase não vale nada aqui: RLS, `FORCE` e `current_setting` são SQL do Postgres. Migration **aplicada é imutável**, número nunca reaproveitado, e destrutiva vai em expand/contract |
| **Tabela do ambiente nasce inteira** | `ambiente_id`, RLS habilitado **e forçado**, política já com o `OR` do `ADR-0004` (mesmo com `vinculo` vazia) e índices — tudo na **mesma** migration. Separar cria uma janela em que a tabela existe sem política |
| Autenticação | E-mail e senha, sessão própria. Nenhuma dependência externa, nenhum provedor de identidade |
| **Senha** | Hash **Argon2id** (não bcrypt): custa memória além de tempo, e resiste melhor a GPU. Parâmetros **calibrados no host**, porque a memória do Pi é pouca |
| **Sessão — `ADR-0009`** | **No servidor.** Cookie com identificador **opaco** (`HttpOnly`, `Secure`, `SameSite=Lax`), estado em tabela, expiração absoluta **e** por inatividade. Quem decidiu foi a **revogação**: tirar acesso tem efeito no clique seguinte. Trocar a senha derruba todas as sessões |
| **O login é a superfície mais atacada** | Resposta e tempo **idênticos** para e-mail inexistente e senha errada · atraso progressivo **por conta e por origem** · bloqueio temporário. Vale igual para a recuperação de senha |
| **Exposição** | **HTTPS sempre**, não "quando sair da rede local" — sem ele o cookie viaja em claro. Só a aplicação escuta; o Postgres nunca é publicado. **O Pi não é fronteira de segurança** |
| **O meio não tem nome, menos o cartão** | Uma conta tem no máximo um Pix, um débito, um boleto: o par `(conta, tipo)` já responde *"como o dinheiro saiu"*. Nome ali produzia `PIX da Nubank` ao lado de `PIX`. **Só o `CREDITO` repete por conta e só ele tem nome** — físico, virtual e adicional dividem o contrato, e o compartilhamento empresta **um** cartão |
| **Meio se escolhe ao abrir a conta** | Não há cadastro de meio à parte. E é isso que torna o **dinheiro exclusivo sem regra própria**: `CARTEIRA` só aceita `DINHEIRO`, e nenhum outro tipo de conta o aceita |
| **A tabela de tipos de meio tem três famílias** | **Muda regra** (`BOLETO`, `CREDITO`) · **escolhe a conta** (`DINHEIRO`, `BENEFICIO`) · **vocabulário do extrato** (`DEBITO`, `PIX`, `TED`, `DESCONTO_EM_FOLHA`). O critério *"tipo novo só existe se uma regra mudar"* **não governa esta tabela**, e agora o doc diz isso em voz alta |
| **Desconto em folha: o salário entra bruto** | Cada desconto é uma `SAIDA` da mesma conta, e a soma com o que sobra é o líquido. Preço nomeado: **o extrato do app mostra o bruto onde o banco mostra o líquido** — o saldo é idêntico nos dois. Lançar o líquido e ainda descontar tiraria o dinheiro duas vezes |
| **A borda serve o dono do fato** | `GET /contas` devolve o catálogo de tipos com os meios de cada um, para a tela **não manter uma segunda cópia** da tabela de `meio-de-pagamento.md`. `CARTAO` não aparece nele enquanto a fatura não existir, e a tela some com a opção sem saber por quê |
| **O `OR` do vínculo vai no `USING`, nunca no `WITH CHECK`** | É a metade que torna *"o que atravessa é o uso, nunca a posse"* verdadeira no banco: o destino de um compartilhamento **lê** a conta emprestada e **não consegue alterá-la**, e nenhum lançamento nasce fora do ambiente de quem lançou. Provado com dois usuários e um vínculo |
| **`vinculo` nasceu com a primeira tabela ligada a conta** | Vazia, e sem política de escrita. `ambiente_origem_id` é **coluna** e não subconsulta: a política de `vinculo` não pode ler `conta`, porque a de `conta` lê `vinculo` — recursionaria |
| **Ambiente sem acesso responde como inexistente** | `403` distinto de `404` conta ao curioso que aquele ambiente existe |
| **Log não tem RLS** | Por isso valor, descrição e categoria de lançamento **não vão para log** — o isolamento do `ADR-0002` para na borda do arquivo. Identificador pode ir |
| **Cadastro aberto** | Mantido em 07/09, com o gatilho já olhado. O que o sustenta é o modelo: **o cadastro não dá acesso a nada** — cria usuário, *Ambiente Pessoal* vazio e as categorias de sistema. Para chegar ao dinheiro de alguém é preciso ser **convidado** |
| **A pergunta virou "quando fechar"** | Não é mais *quando abrir* o cadastro: o gatilho para fechar é **sair da rede local** |
| O que o cadastro dispara | Um ato só, três efeitos: usuário · ambiente **"Ambiente Pessoal"** · as categorias de sistema dele |
| **E-mail — `ADR-0007`** | SMTP do Gmail com senha de app, custo zero. Escopo fechado: **só quando a pessoa não consegue entrar**. Convite, compartilhamento e aviso a quem já está dentro chegam **pelo sistema** |
| **Informação do sistema chega pelo sistema** | A razão durável no lugar da circunstancial ("não há e-mail no Pi"). Sobrevive a mudar de host, provedor ou rede |
| Ambiente ativo | No caminho da URL: `/api/v1/ambientes/{id}/...`. **Escrito em 07/09** no `04-api/convencoes.md`, com a regra: se o dado tem `ambiente_id`, o endpoint dele está sob `/ambientes/{id}/`, sem exceção |
| **API — a borda não inventa vocabulário** | Recurso e campo em **português**, `camelCase`, com os nomes do domínio. Dinheiro **inteiro em centavos**; data de domínio `"AAAA-MM-DD"` sem fuso; instante em UTC. **Verbo em caminho é proibido** — ação de domínio vira sub-recurso (`POST .../faturas/42/fechamento`) |
| **`GET` nunca muda estado** | É segurança, não estilo: o `SameSite=Lax` barra CSRF nos outros métodos e **deixa o `GET` passar** |
| **Paginação por cursor**, não página | O Extrato **cresce por cima**: com página numerada, um lançamento novo faz um item repetir ou **sumir** enquanto a pessoa lê. O cursor aponta para uma linha, não para uma contagem — e a chave de ordenação é sempre única. Preço aceito: não existe "pular para a página 7", e o total é consulta separada |
| **Erro em `problem+json`** | RFC 7807 mais `codigo` (contrato) e `erros` (validação, **todos os campos de uma vez**). **O cliente lê o código, nunca a mensagem** |
| **O erro não conta o que a pessoa não podia saber** | `404` cobre "não existe" e "não é seu"; `403` só **dentro** de um ambiente a que ela já tem acesso. `500` nunca carrega stack trace, SQL nem valor informado |
| **Roteador vale para o código — `ADR-0008`** | O endereço do código é **derivável do nome do doc dono**; um assunto, um pacote; sem pasta de topo por camada; a rota tem orçamento e o `check` o cobra |
| **Monolito modular, cortado por assunto** | Um processo, um banco, um artefato. As quatro camadas (`dominio`, `aplicacao`, `api`, `persistencia`) vivem **dentro** de cada assunto, e a seta aponta para dentro |
| **A fronteira é imposta por teste — `ADR-0010`** | **Um módulo Maven**, não onze. O multi-módulo foi descartado porque o domínio tem **ciclo real** (fatura ↔ lançamento) e o build obrigaria a recortar o modelo para agradar a ferramenta |
| **Domínio referencia domínio por `id`** | A metade que torna a outra verdadeira: **o grafo entre domínios fica vazio**, e o ciclo some sem ninguém recortar nada. Não é regra nova — é o que o `lancamento.md` já fazia sem nomear. Virou a **regra dura 9** |
| **Quem junta dois assuntos é a `aplicacao`** | Sempre. Um caso de uso lê os dois; um domínio nunca lê o outro |
| **Sem evento de domínio, sem fila, sem barramento** | Chamada direta em transação. E o `Evento` do domínio **não é isso**: é registro, não mecanismo — nada no sistema reage a um evento gravado |
| **As bordas são clientes, não caminhos paralelos** | Bot, OFX, captura e painel entram **pela mesma API**. Caminho paralelo é regra duplicada, e o Diário contaria história diferente conforme por onde a pessoa entrou |
| **Três classes para a mesma coisa** | `Fatura` ≠ `FaturaEntity` ≠ `FaturaResponse`, com conversão **escrita à mão**. Compra teste de regra sem Spring e sem banco, e impede schema vazar para o cliente |
| **Nome: substantivo em português, sufixo em inglês** | `FaturaRepository`, `PagarFaturaUseCase`. O substantivo é vocabulário do domínio e não se traduz; o sufixo é do framework, e traduzir faz a doc do Spring parar de casar com o código |
| **Sem Lombok** | Java 21 tem `record`. É a regra 3 valendo para o caso mais fácil de aceitar sem pensar — o que ele economiza é digitação, que não é o custo deste projeto |
| **`@Transactional` só na `aplicacao`** | Não é arrumação: é a transação que faz o `SET LOCAL` do RLS. Transação no lugar errado é **RLS lendo o ambiente errado** |
| **Teste contra Postgres real — `ADR-0011`** | **O H2 não tem RLS.** Testar o isolamento nele não é teste mais fraco: é teste que **passa sempre**, inclusive com a política ausente. E o contêiner sobe com os **dois papéis**, senão o teste roda como dono e o RLS não se aplica |
| **O critério de teste não é cobertura, é consequência** | Percentual cobre `getter` e deixa passar a regra que muda dinheiro. Há uma lista do que é **obrigatório**: toda invariante de `02-dominio/`, o isolamento, todo passo automático (idempotência **e** recuperação de atraso), e a aritmética que já quebrou |
| **Dois ambientes, e o que NÃO muda entre eles** | Mesma versão de Postgres, mesmas migrations, os dois papéis, **HTTPS nos dois** e o dia local em Brasília. O que difere é **valor de variável** — nunca perfil com comportamento de domínio diferente, que é como nasce o bug que só aparece em produção |
| **Congelado é a funcionalidade, não o modelo** | Decisão de modelo que contamina schema ou política de acesso entra na fase em que o schema nasce. Valeu para `Aplicação`, compartilhamento e **Evento** |
| **Compartilhamento** | **Modelo na Fase 1**; **tela liberada com a Fase 1 concluída** |
| **Evento** | **Gravar na Fase 1, tela do Diário na Fase 2.** Schema tardio é migration em cima de dado real; **evento tardio é dado que nunca existiu** |
| **Recorrência** | **Fora da Fase 1.** O passo do fechamento que a lança fica escrito, marcado como *passo que entra com a recorrência* |
| **Data e fuso** | Data de domínio é **dia local, sem hora nem fuso**; UTC vale para **instante**. **Dia local = horário oficial de Brasília**, para todo usuário e em qualquer host |
| **Valor informado envelhece** | Regra 7 do `CLAUDE.md`: nunca extrapolado nem corrigido; carrega a data em que foi informado, e a tela mostra a idade |
| **O sistema não apaga o que não mandaram apagar** | Nada é inativado nem excluído por prazo, desuso ou critério do sistema |
| **O sistema não reescreve o passado** | Matou o "mover". Mas **o usuário corrige o que ele mesmo escreveu errado** — é o que sustenta o excluir |
| **E o que o sistema faz sozinho, ele registra** | Fecha com o Evento. **Preço invisível é preço que ninguém aceitou de verdade** |
| **Nada do RaspyBank atravessa (`B26`)** | Ele morre quando o Cyberbank subir. Um parcelamento em curso não existe; **os parcelamentos vivos são recadastrados** (a data da compra fica sendo hoje) |
| Fatia da v1 | v1 leva `Aplicação` + patrimônio (valor atual à mão, sem rentabilidade/cotação) |

## Decisões — domínio

| Decisão | Valor |
|---|---|
| O que é conta | **Se tem saldo próprio que o sistema acompanha, é conta** |
| Tipos de conta | `CORRENTE`, `CARTEIRA`, `APLICACAO`, `BENEFICIO`, `CARTAO`. Não existe `POUPANCA` |
| **`BENEFICIO`** | Confirmado pelo saldo ser **não fungível**: dele não sai transferência, e ele não é caixa |
| **Três eixos na conta** | `tipo` · `entraNoFluxoDeCaixa` (o movimento é gasto?) · **`entraEmCaixa`** (o saldo paga qualquer coisa?) |
| **Em caixa** | Soma do saldo realizado das contas com `entraEmCaixa = true` |
| `situacao` | Três valores (`ADR-0006`): `PREVISTO` · `PROVISIONADO` (**aconteceu, falta liquidar**) · `REALIZADO` |
| **Quem move a situação** | **A automação só anda para frente.** A **correção** do usuário anda nos dois sentidos: descreve o registro, não o dinheiro — sempre com histórico |
| **Entra no saldo?** | O teste é `situacao !== 'PREVISTO'`, **nunca** `=== 'REALIZADO'` |
| **Liquidar fora do cartão** | **A data realiza**, sem fila. É o único ponto em que o sistema afirma um fato que não observou — e a Fase 2 resolve pela raiz, com conciliação |
| Fluxo de caixa × patrimônio | `conta.entraNoFluxoDeCaixa` (não o tipo) decide se o movimento é gasto |
| Saldo | Sempre soma dos lançamentos, nunca armazenado |
| **`CARTAO` não tem saldo de abertura** (07/09) | **A única exceção da regra do saldo inicial.** Dívida de cartão **não é um número, é um conjunto de faturas** — com vencimentos, parcelas em curso, uma parte fechada no emissor e outra correndo. A `CARTAO` nasce zerada e a fatura nasce vazia. Preço nomeado: patrimônio e limite ficam otimistas por um ou dois ciclos |
| Valor do lançamento | Sempre positivo; o sinal vem de `sentido` |
| Duas datas | `dataEvento` e `dataEfeito`, as duas **data pura**. Só o boleto e o previsto as separam |
| **Três operações, não duas** (01/09) | **Correção** (o registro está errado mas descreve algo que aconteceu) · **estorno** (o dinheiro voltou) · **exclusão** (**nunca correspondeu a nada** — a duplicata) |
| **A fronteira do excluir** | **O que o usuário lançou, o usuário exclui; o que o ciclo criou não é dele para excluir.** Protege a parcela isolada, o par de rolagem e o lançamento de abertura |
| **O relatório de gasto é líquido** | **Quem manda é o `sentido` da categoria**, não o do lançamento. O estorno abate o mês em que aconteceu, e uma categoria pode ficar **negativa** |
| **A regra do `sentido` é de escolha** | Governa o que o usuário **pode escolher**, não o dado guardado. O estorno é a única divergência, de propósito |
| **Em que mês o gasto conta** | **Dois eixos de competência, padrão por fatura.** O modelo não muda — quem espalha é o relatório |
| Pendência | **`categoria IS NULL`**, sem exceção. Não é estado, é consulta |
| Transferência | Par com o mesmo `transferenciaId`. Aporte, resgate **e pagamento de fatura** são casos disso |
| **Lançamento que o ciclo cria** | `autor` = **o dono do ambiente da conta**; `ambiente` = o do **dono do objeto** |
| Patrimônio | Saldo **realizado de todas as contas**, sem exceção nenhuma |
| **Aplicação desatualizada** | O sistema **não estima nada**. Marca aos **30 dias** |
| Categoria | Árvore de **exatamente dois níveis**, com `sentido`, sempre do ambiente do lançamento |
| **Raiz escolhível** | Nasce escolhível; deixa de ser ao ganhar o primeiro filho **ativo**; volta quando o último filho ativo some. **Oscila, e é de propósito** |
| **Inativar categoria** | O **excluir de quem tem histórico**. Lógico, nunca físico. **Sempre ato do usuário** |
| **A raiz esconde a árvore** | Filho é escolhível se **ele está ativo E a raiz está ativa** — herança na leitura, **não** cascata na escrita |
| **Inativa barra a escolha, não o ciclo** | A parcela e a ocorrência nascem na categoria original. O escape `doCiclo` existe para isso (achado 23) |
| **NÃO EXISTE MOVER** | Quem reorganiza **inativa** onde está e **cria** na raiz certa. `pai` nunca muda |
| **Categoria de sistema** | Sete operações × dois sentidos = 14 por ambiente. Nunca aparecem no seletor, não se renomeia, não se move, **não se inativa** e não se exclui |
| **O sistema não cria categoria de usuário** | Não existe conjunto inicial. Tela vazia no primeiro lançamento é preço aceito |
| **EVENTO** | *O que aconteceu, quando e por quem* num ambiente. **É entidade, não consulta** — metade do que o Diário mostra não é derivável. Imutável: registro errado se corrige com evento novo |
| **O evento entrega o "histórico de alteração"** | `LANCAMENTO_EDITADO` com o de/para. Não é uma segunda estrutura |
| **O evento não guarda a frase pronta** | `tipo` + `dados` + `alvo` bastam; a tela monta o texto |
| **O que NÃO é evento** | Leitura · login e sessão · erro e exceção · **passo de ciclo que não fez nada** · rendimento com diferença zero |
| Meio e conta | Todo meio aponta para uma conta. Todo `CREDITO` aponta para uma `CARTAO` |
| **Referência entre ambientes** | **Conta e meio** podem ser de outro ambiente, e só com vínculo. **Categoria, nunca** |

### Cartão de crédito — `ADR-0003`

| Decisão | Valor |
|---|---|
| O contrato é uma conta | Tipo `CARTAO`, saldo = dívida |
| Os cartões | Físico, virtual e adicional são **meios `CREDITO`** apontando para a mesma conta |
| Compra no crédito | `SAIDA` na conta `CARTAO`, `PROVISIONADA`, `dataEfeito = dataEvento` |
| Parcelas | **Todas nascem juntas e provisionadas, na data da compra** |
| **Arredondamento** | **O centavo que sobra vai na primeira parcela** |
| Pagar | **Transferência** apontando para a fatura por `pagamentoDeFatura` |
| Dívida | **É o saldo da conta.** Sem cálculo próprio |
| **Limite** | `limite − dívida`. Informado pelo usuário, e **nunca trava um lançamento** — recusar a compra que o emissor aprovou seria o app discordando do banco |
| Mínimo e juros | **O sistema nunca calcula.** Não decide, observa: juros, IOF e multa entram como lançamentos comuns da fatura seguinte |

### Fatura — `fatura-cartao.md` (ciclo) e `fatura-pagamento.md` (dinheiro)

**Fechada em 07/09. Zero itens em aberto nos dois docs.**

| Decisão | Valor |
|---|---|
| O que é | **Recorte de um período da conta `CARTAO`**. Valor sempre derivado |
| Estados | `FUTURA` · `ABERTA` · `FECHADA`. Só **uma `ABERTA`** por conta `CARTAO`. **Não existe `REABERTA`** |
| **Os três números** | `total` (menos o crédito de rolagem) · `pago` · `a pagar = total − pago − rolado` |
| A que fatura o lançamento vai | **Ao status, nunca à data** |
| **Fechar ≠ encerrar** | Fechar não liquida nada. Quem liquida é o **encerramento**: quitada, ou vencida e rolada. **Pagamento parcial não liquida nada** |
| **A JANELA É UMA SÓ** (07/09) | **`FECHADA` com `a pagar` maior que zero** é o que se pode **abrir** e o que se pode **pagar**. Um número, duas operações — e é o mesmo que o encerramento já usa como gatilho |
| **Fatura encerrada não abre** (07/09) | Sem isso a rolagem se autoalimenta: abrir uma fatura rolada a torna a `ABERTA`, e a rolagem manda o que sobrou **para ela mesma** — o `a pagar` não muda, e um par nasce por dia, para sempre. **Fatura de total zero também não abre**, e não precisa: mover lançamento para dentro dela é editar o campo `fatura` |
| **Só a `FECHADA` que ainda deve recebe pagamento** (07/09) | `FUTURA` não (o emissor nem a emitiu) · `ABERTA` não (o ciclo corre e o valor ainda muda — pagar antes é **antecipar**, Fase 2) · encerrada não (`a pagar` já é zero). Sem a regra, a quitação encerraria a fatura cedo demais |
| **Encerrar vem antes de fechar** (07/09) | No dia em que os dois caem juntos. O vencimento é anterior à `dataFechamento`; rolar depois mandaria a dívida para a fatura errada |
| Fechamento | Automático, idempotente, recupera atraso, **duas coisas e nem uma a mais**. Cada passo que acontece grava evento; rodada que não fecha nada não grava nada |
| **O fechamento não cria pagamento** (01/09) | O sistema não sabe de qual conta nem em que dia você vai pagar — você pode sacar e pagar em dinheiro. **Quem cria é o usuário**: escolhe fatura, conta, dia e valor; dia à frente nasce `PREVISTO` e realiza pela data |
| **Rolagem** | Dia seguinte ao vencimento, idempotente, dia a dia em ordem. Par que **soma zero**; o débito nasce `PROVISIONADO`. **É o evento mais importante do Diário** — o único passo que o usuário não pediu, não previu e não vê em outra tela |
| **O que você agendou não é tocado** | Nem pelo encerramento, nem ao abrir a fatura. O sistema não apaga lançamento de usuário |
| **O sistema nunca reescreve um pagamento** (01/09) | Toda diferença que uma correção produza **vira lançamento**: total subiu, a fatura volta a dever e rola na passagem seguinte; total caiu, sobra crédito na conta `CARTAO`. Pagamento errado de verdade, o usuário edita direto |
| Total histórico | **Não cai** quando a fatura rola |
| **Moeda estrangeira não é pergunta** | Entra **já convertida, em centavos de real**. Multi-moeda é não-objetivo *sem fase* |

### Compartilhamento — `ADR-0004`

| Decisão | Valor |
|---|---|
| O que atravessa | **O uso, nunca a posse** |
| O que se compartilha | **Conta** e **um cartão**. Conta `CARTAO` inteira, não |
| A regra que sustenta tudo | **Todo lançamento pertence ao ambiente em que foi feito** |
| Categoria | A do ambiente de quem lançou. A de fora aparece **mascarada** |
| Partes da fatura | O sistema calcula a parte de cada um. **A parte orienta, não trava** |
| Revogar | **Fica tudo**; o destino só para de receber lançamento novo |

### Séries

| Decisão | Valor |
|---|---|
| Recorrência | N eventos independentes, sem fim. **Uma ocorrência por ciclo, sem horizonte** |
| Parcelamento | UMA compra dividida. Editar altera todas, sempre |
| Editar recorrência | **Pergunta**: só as futuras, ou o passado também? |
| **Duas entidades, não uma com `tipo`** | Quando *todas* as regras mudam por tipo, são duas coisas |

**Critério que já resolveu nove perguntas:** tipo novo (ou valor, campo, operação) só existe se
alguma **regra do sistema** mudar por causa dele. Derrubou `POUPANCA`, cartão virtual como tipo,
débito automático como meio, o subtipo de aplicação, o estado `REABERTA`, a categoria "Saque" e
o **"mover"** — e criou `CARTAO`, `PROVISIONADO`, `entraEmCaixa` e `inativa`. **O `Evento` é o
primeiro item que passa por fora dele**, de propósito: o critério é sobre *tipo*, e evento não
classifica — registra.

## Decisões — UX

| Decisão | Valor |
|---|---|
| Ambiente na tela | Seletor **fixo no header**, cor por ambiente. Trocar limpa filtros |
| Lançar | **Quick-add** é o padrão (tecla `N`), com saída para o formulário completo |
| Formulário completo | **Explica o que o modelo vai fazer** antes de fazer |
| Ação destrutiva ou retroativa | Mostra o **impacto numérico** antes de confirmar |
| Densidade | HUD denso. O número que importa é o maior elemento da tela |
| Telas | Home · Extrato · Fatura · Séries · Reserva · **Diário** · Cadastro · **Perfil**. **De pé: Home, Extrato, Cadastro, Diário e Perfil.** A **Home é o cockpit** (`06-interface/dashboard.md`); o Perfil fica fora do rail, com a porta no avatar do canto superior direito |
| **Dashboard da Home** | O do protótipo, agrupado por categoria, com a linha **"guardado"** separada do gasto |
| **Dado que envelhece na tela** | Aplicação mostra a data do último valor; o limite avisa quando a dívida passa dele |
| **Hierarquia mora no lugar, não num campo** | Um card por raiz, "+ subcategoria" **dentro** do card |
| **Regra do modelo vira estado na tela** | O card diz `ESCOLHÍVEL`, `NÃO ESCOLHÍVEL` ou `INATIVA`. Prosa ninguém executa |
| **Botão que só existe quando funciona** | Botão travado obriga a tela a explicar sete vezes; botão ausente explica uma vez |
| **Inativa apaga, nunca alerta** | Cinza com opacidade. **Rosa é alerta**, e inativa é escolha de propósito |
| **Dado inativo fica escondido, e a tela diz que escondeu** | Escondido é o padrão · interruptor na tela · **a contagem sempre inclui o escondido** |
| **O Diário responde "por que meu saldo mudou?"** | A única tela que mostra **o que aconteceu**. Um dia por vez, seletor só para trás, duas seções e **a do sistema vem primeiro**. **Dia sem evento é resposta, não erro** |

## Achados do método

1. **Pendência** virou "lançamento que *espera* categoria".
2. **A série de parcelas terminava com valores diferentes**: regra de recorrência aplicada a um parcelamento.
3. **Estorno de compra parcelada se anula sozinho**, porque saldo é derivado.
4. `clip-path` recorta todos os descendentes.
5. **O pagamento mínimo não é regra do sistema, é do emissor.**
6. **O estado `REABERTA` durou uma hora.** Padrão: *estado inventado para duplicar uma proteção que já existe em outro lugar.*
7. **O compartilhamento derrubou a regra de pagamento de fatura — e o critério do projeto já tinha a resposta.**
8. **A dívida do cartão não era o saldo projetado.** *(Deixou de existir: ver 11.)*
9. **`entraNoFluxoDeCaixa` não responde "isso é caixa?".**
10. **O que não foi pago ficava preso numa fatura vencida.** Nasceu a rolagem (`ADR-0005`).
11. **`situacao` carregava duas perguntas ao mesmo tempo.** Nasceu `PROVISIONADO`, e com ele o achado 8 e o caso especial de patrimônio **deixaram de existir** — sumiram pela estrutura.
12. **Nove das dez contradições diretas eram texto que ficou para trás de um ADR novo.** Padrão: *doc vizinho que ninguém releu depois da decisão.*
13. **O roadmap estava proibindo um ADR aceito.** A saída foi separar **modelo** de **funcionalidade**.
14. **Um lado do par de rolagem `PREVISTO` apagaria a dívida.** Achado somando os números.
15. **O vale-refeição era o segundo caso do achado 9.** O `conta.md` **já tinha escrito a condição** e ninguém verificou contra o seed.
16. **"Sistêmica" foi lida como "o conjunto que vem de fábrica", e não era isso.** Padrão: *termo novo aceito sem alguém dizer em voz alta o que ele nomeia.*
17. **O estorno não abatia o gasto.** Os dois se compensavam **no saldo**, não no relatório.
18. **Uma invariante escrita como regra de dado era simplesmente falsa.** O `conferir()` acusou na primeira execução.
19. **Não existia caminho para cadastrar subcategoria.** *O seed provava um caminho que nenhum usuário podia percorrer.*
20. **A inativação já estava decidida em `categoria.md`, e a sessão anterior afirmou que não.** Padrão: *ausência no código lida como ausência no doc.*
21. **"Mover" era a única regra que reescrevia o passado — e ninguém tinha perguntado para que ela servia.** Padrão: *regra que sobrevive porque todos a leem como "está escrito e está aceito".*
22. **O Extrato e a Home estavam mortos havia dois dias, e três rodadas passaram por cima.** Padrão: *toda a bateria de provas olhando para um lado só.* Nasceu o `verificar.js`.
23. **"Inativa barra a escolha, não o ciclo" estava no doc, o escape estava no código, e nada exercitava.** Padrão: *quando o doc prevê um caso, o seed tem que conter esse caso* — senão a previsão nunca é executada.
24. **Uma decisão foi reaberta porque a razão anotada ao lado dela mudou de circunstância.** O `ambiente-financeiro.md` escrevia *"o convite chega dentro do sistema"* ao lado de *"não há e-mail no Pi"*; quando o `ADR-0007` trouxe e-mail, a decisão foi reaberta — e ela **não dependia daquela razão**. *Razão circunstancial anotada ao lado de uma decisão é uma reabertura esperando acontecer.*
25. **A rolagem podia rolar para si mesma, para sempre** (07/09). Abrir uma fatura já rolada a torna a `ABERTA`, e o destino da rolagem é *"a fatura `ABERTA`"*. Somando: o crédito sai do total e entra no `rolado`, o débito entra no total, e o `a pagar` **não muda** — um par e um evento por dia, indefinidamente. Padrão: *regra escrita como "para X" quando X podia ser o próprio sujeito.* Achado 14 outra vez, e de novo somando os números.
26. **A pergunta estava errada, não a resposta** (07/09). Correção do Abner: eu perguntei *"em que fatura entra o lançamento de abertura de uma `CARTAO`?"* quando a pergunta era ***"cartão de crédito tem saldo de abertura?"***. Responder a primeira teria produzido uma regra correta para um lançamento que não devia existir. Padrão: *herdar a existência de uma coisa da pergunta que alguém fez sobre ela.*

27. **A primeira página funcionava e a segunda não** (16/09). O cursor do Extrato ia numa
    consulta só, com `(:dataEvento is null or ...)` para a primeira página — e o Postgres
    respondeu *"could not determine data type of parameter $5"* **só quando o parâmetro deixou
    de ser nulo**. Nenhum teste de domínio podia pegar: não há SQL neles. Quem pegou foi o
    teste de integração contra Postgres real, que é o argumento do `ADR-0011` acontecendo pela
    segunda vez. A saída foi **duas consultas explícitas** — a primeira página não tem cursor
    para tipar. Padrão: *o caminho que só roda na segunda vez é o que nenhum teste de caminho
    feliz visita.*

28. **O critério nunca tinha sido aplicado à tabela que ele deveria governar** (16/09). O Abner
    propôs `TED`, e a resposta pronta era o critério do projeto — *"tipo novo só existe se
    alguma regra do sistema mudar por causa dele"*, o que matou `POUPANCA` e o débito
    automático. Só que ao conferir a tabela antes de responder: **`DEBITO` e `PIX` são
    idênticos nas cinco colunas** desde o primeiro dia. O critério já estava sendo violado por
    dois tipos que ninguém questionava, e aplicá-lo ao `TED` teria sido usá-lo como argumento
    de autoridade contra um caso novo enquanto os antigos passavam. A saída não foi abrir
    exceção: foi **nomear as três famílias** no doc. Padrão: *critério citado de memória, sem
    conferir se o que já está lá passa nele.* É o achado 20 outra vez — ausência lida como
    decisão —, agora do lado do critério.

29. **A regra estava escrita, e ninguém a tinha escrito em código** (16/09). O Abner reportou
    o combo de categoria achatado — um bug de tela. Ao abrir o `categoria.md` para conferir a
    regra da árvore, a frase seguinte era *"na hora de lançar, só aparecem as categorias
    compatíveis com o sentido do lançamento"*, com o motivo ao lado: *"sem isso, nada impede
    categorizar o salário como mercado"*. **Nada a implementava** — nem a tela, nem o caso de
    uso. Dava para fazer exatamente o que o doc dizia que não podia, pela API. Padrão: *o bug
    de tela levou ao doc, e o doc tinha uma regra que ninguém tinha lido até o fim.* É o
    achado 23 outra vez — regra escrita que nada exercita —, e a diferença é que ali existia
    um escape no código e aqui não existia nada.

Os achados 8 a 29 são o argumento do método inteiro: todas essas regras estavam escritas,
commitadas e plausíveis. Só quebraram quando alguém **somou os números** — leu duas linhas lado
a lado, tentou usar a tela, foi conferir no doc, perguntou para que a regra servia, olhou para a
tela que ninguém estava mexendo, fez o relógio andar, ou perguntou se a coisa existia.

## Próximo passo

**Não há mais decisão de domínio de Fase 1 esperando resposta.** O que falta é técnico, e a
ordem está fixada no `lacunas-para-codigo.md`:

1. ~~**`D1` — autenticação e sessão.**~~ ✅ **Fechado em 07/09.** `seguranca.md` está ativo e
   o `ADR-0009` registra o porquê da sessão com estado. Ficou aberto só o que é Fase 2 (a
   identidade no bot do Telegram) e a **contenção do cadastro aberto**, adiada de propósito.
2. ~~**`D2` — `04-api/convencoes.md`**~~ ✅ **Fechado em 07/09**, junto com o `erros.md` — o
   fluxo `novo-endpoint` lê os dois, e convenção sem contrato de erro é meia decisão. Falta
   escrever os **endpoints por agregado** (`endpoints-ambientes`, `-contas`, `-lancamentos`,
   `-categorias`, `-meios-pagamento`, `-relatorios`), que é trabalho de agregado, não de
   convenção.
3. ~~**`D3` — `03-dados/modelo-de-dados.md`**~~ ✅ **Fechado em 07/09**, com o `migrations.md`
   junto. Falta o **`catalogo-tabelas.md`** — coluna a coluna —, que nasce com a primeira
   migration e não antes: escrever o catálogo sem a migration é inventar duas vezes.
4. ~~**`01-arquitetura/`**~~ ✅ **Fechado em 07/09** — `visao-geral`, `modulos`,
   `estrutura-de-pastas` e `padroes-de-codigo`, mais o `ADR-0010`. Seguem stub o
   `observabilidade.md` e o `ambientes-de-execucao.md`, que são de operação.
5. ~~**`07-operacao/build-e-run.md` e `testes.md`**~~ ✅ **Fechado em 07/09**, com o
   `ambientes-de-execucao.md` e o `ADR-0011`. **Não há mais doc bloqueando a primeira linha de
   código.**
6. ~~**O esqueleto do projeto**~~ ✅ **Fechado em 07/09**, pelo `/esqueleto`. `./mvnw verify`
   passa com o Postgres do compose no ar, e a aplicação sobe com o Flyway rodando como dono.
   **O `/esqueleto` não se repete** — daqui em diante o caminho é `/caso-de-uso`.
7. ~~**A primeira migration** (`V001`), e com ela o `catalogo-tabelas.md`~~ ✅ **Fechado em
   07/09**, junto com a `V002` e a fatia 1 inteira. O `catalogo-tabelas.md` está ativo, com as
   cinco tabelas que existem.
8. ~~**Cadastro de conta e de meio, lançamento e Extrato**~~ ✅ **Fechado em 16/09**, pela
   fatia 3. Ver a linha da sessão lá em cima e a lista do que ficou de fora, logo abaixo.
9. ~~**`evento` e a rotina diária**~~ ✅ **Fechado em 17/09.** A gravação está de pé, em
   `V006`, com a rotina e o `ADR-0013`. **A tela do Diário continua sendo Fase 2** — o que
   entrou é o registro, que é o que não se reconstitui depois.
10. **Cartão e fatura** — conta `CARTAO`, meio `CREDITO`, as cinco colunas adiadas de
   `lancamento`, fechamento, pagamento e rolagem. É a fatia grande, e os dois docs dela já
   estão fechados desde 07/09.
11. **Rodada de protótipo** — ver abaixo; o backlog do `dominio.js` está aberto desde 01/09,
   e agora o `prototipo/` está **atrás do app de verdade**, não só do modelo.
12. ~~**Tela de Perfil**~~ ✅ **Fechada em 17/09**, nos três commits combinados. Ficaram
    reservados na tela, dizendo o que esperam: **convites recebidos**, **convidar alguém** e
    **sessões ativas** — os três entram com o convite. **Renomear categoria** na tela continua
    aberto.
13. ~~**A tela do Diário**~~ ✅ **Fechada em 17/09**, antecipada da Fase 2 — e o **link para o
    objeto exato** fechou junto no fim do dia: `#/extrato/{id}` abre o detalhe. Conta, meio e
    categoria continuam levando à tela do alvo, porque nenhum dos três tem detalhe próprio.
14. ~~**Lançar fora do Extrato**~~ ✅ **Fechado em 17/09.** O quick-add, a tecla `N` e o
    formulário completo em modal. A **aba CRÉDITO existe desabilitada** e entra com o cartão.
15. **O quick-add lança sempre com a data de hoje**, sem campo de data — é o que o faz sumir
    em dois segundos. Se lançar coisa de ontem for frequente, ele precisa de um atalho.
    **Espera uso real para decidir.**

## Decisões em aberto

**Da Fase 1: uma, e é de conteúdo, não de regra.**

0. **A poda da lista de 11 raízes de categoria**, que espera o corte do Abner.

**Pós-Fase 1 (compartilhamento — `B15` a `B20`):**

1. **Patrimônio da parte do cartão compartilhado.** É o buraco mais caro
2. **Parte do adicional sem dono**, se o adicional puder ser só um rótulo
3. **Duas granularidades de visibilidade**: conta dá a conta inteira; cartão dá só os
   lançamentos daquele meio
4. **Agregado atravessa sem a linha**: o destino vê limite e consumo sem ler os lançamentos
5. **Quem fecha e abre a fatura** de um cartão compartilhado
6. **Herança de acesso**: quem entra no ambiente de destino passa a usar a conta de fora
7. **O destino pode editar lançamento que a origem fez?** · **compartilhamento se repassa?** ·
   **compartilhar `APLICACAO` na v1?** · **cartão adicional exige cadastro?**
8. **Categoria inativa num ambiente compartilhado** — provavelmente sem regra nova; confirmar
9. **O que o destino vê no Diário do ambiente dele?**

**Outras:**

10. **Volume de evento no Pi.** A conta a fazer antes de inventar arquivamento é que um ambiente
    ativo produz dezenas de eventos por mês
11. **Estorno parcelado: emissores divergem.** Observar o que vier na fatura
12. **Débito automático muda comportamento** ou é só rótulo? (vai junto com a recorrência)
13. **Recorrência é Fase 2 ou 3?** Decidir ao fechar a Fase 1
14. **Contenção do cadastro aberto** — limite por origem e verificação de e-mail no cadastro.
    O gatilho para **fechar de vez** continua sendo sair da rede local; ele foi olhado em 07/09
    e a escolha foi **manter aberto**, com a lógica de contenção para depois
15. **Uso pessoal ou produto**
16. **Integração contínua?** Não bloqueia nada — `./mvnw verify` já reprova localmente, e o
    push sai da máquina do Abner. A pergunta é se vale um GitHub Actions rodando a suíte a cada
    push, ou se isso é cerimônia para um desenvolvedor

*Saiu desta lista em 15/09:* **cor de categoria × "cada cor tem um significado"** — a
resposta foi (a), e está em `direcao-visual.md`: cor de categoria é **identidade**, a cor de
estado é **semântica**, e são **duas paletas** que nunca se misturam. A exceção do seed (`LAZER`
rosa sem alertar nada) deixou de ser exceção.

*Saíram desta lista em 07/09:* **antecipar parcelas** e **parcelamento da própria fatura**
(nunca foram dúvidas — são Fase 2, e agora estão sob *Fora desta fase* no `fatura-cartao.md`);
**fatura em moeda estrangeira** (já respondida pelo roadmap e pelo `conta.md`); e **onde vive
este doc** — a resposta é: aqui, em `claude/` no repositório, fora do roteador.

*(A pergunta "mover uma subcategoria muda o consumo de orçamento de meses já fechados?" deixou
de existir em 30/08, junto com o mover. Está riscada no `orcamento.md` em vez de apagada, porque
a razão de ela ter sumido vale mais que a pergunta.)*

## Estado da documentação

**82 documentos**, 16 stubs. Stub = conteúdo inexistente: **perguntar, nunca deduzir.**

Escritos: `CLAUDE.md`, `CONVENTIONS.md`, os 6 fluxos, todo o `00-produto/` menos `jornadas`,
**`06-interface/dashboard`** (a Home) e **`06-interface/extrato`** (a lista e o detalhe),
`02-dominio/` inteiro menos `orcamento`, `regras-categorizacao` e `importacao-conciliacao`,
`06-interface/` (navegacao, direcao-visual), **`01-arquitetura/seguranca`**,
**`04-api/` inteiro**, **`03-dados/` inteiro** (o
`catalogo-tabelas` virou **dois**, quebrado por família em 16/09),
**`01-arquitetura/` menos observabilidade**, **`07-operacao/build-e-run` e `testes`**, e as
ADRs **0001 a 0014**. Em 17/09 entraram **`02-dominio/usuario`**, **`04-api/endpoints-usuario`**
e **`06-interface/perfil`**.

`fatura-cartao.md` está em **300 linhas, no teto do `CONVENTIONS`** — a próxima coisa que entrar
ali obriga a quebrar por subdomínio, como o `fatura-pagamento.md` já nasceu. O
`catalogo-tabelas.md` **já passou por isso** em 16/09: virou ele mais o
`catalogo-tabelas-do-ambiente.md`, e o corte foi a família, que é o eixo do próprio doc.

**Custo de contexto** (`docs.py custo`, fim de 17/09): rotas entre ~2,6k e **~17,7k**; ler
tudo custaria ~125k. A rota que importa, `novo-caso-de-uso`, está em **~5,4k**.

**A inflação prevista continua, e agora dói.** `novo-meio-de-pagamento` está em **~16,3k**,
`nova-integracao-externa` em ~10,8k e `nova-migration` em ~9,7k — as três passaram do que o
`novo-caso-de-uso` (~5,4k) gasta para entregar uma funcionalidade inteira. Os números de 07/09
eram ~1.508 de base e ~6,6k na pior rota. O conteúdo é necessário; o que falta é o **teto por
rota** no `check`, que o `ADR-0008` decidiu e o `docs.py` ainda não implementa. **Enquanto ele
não existir, a inflação não avisa** — e a conta acima passou a ser a única forma de vê-la.

**A rota que importa é a mais barata das de código.** O fluxo **`novo-caso-de-uso`** custa
**~4,2k tokens** — é por onde uma funcionalidade nova entra, e ele carrega dois docs: a regra do
assunto e os padrões. O endereço do código não é procurado, é derivado (`ADR-0008`).

## Estado do protótipo

`prototipo/assets/js/dominio.js` implementa o modelo quase por inteiro: encerramento,
`entraEmCaixa`, categoria de sistema, os dois eixos do relatório de gasto, a regra 7 na tela, o
`conferir()`, o cadastro de categoria com inativação, e **Evento + Diário** com seletor de dia.

**Correções de 07/09:** `podeAbrir` e `podePagar` compartilham a janela (`FECHADA` com `a pagar`
> 0); `abrirFatura` parou de apagar o pagamento agendado; `criarConta` recusa `abertura` em
`CARTAO`.

**Backlog aberto desde 01/09 — o protótipo está atrás do modelo:**

- `pagarFatura` ainda reescreve o previsto (comportamento do `ajustarPagamento`, que **morreu**);
- `criarPagamentoPrevisto` e `sincronizarPagamentoPrevisto` não existem mais no modelo, e o seed
  conta com eles;
- `PAGAMENTO_PREVISTO_CRIADO` e `PAGAMENTO_PREVISTO_AJUSTADO` saem dos eventos gravados;
- falta `excluirLancamento`, com a fronteira usuário × ciclo;
- falta a rolagem disparada por correção, datada **no dia em que a rotina rodou**;
- falta o pagamento agendado pelo usuário, e o `avancar()` deixa de precisar do
  `!l.pagamentoDeFatura`.

**O cenário a rodar primeiro** é o do Abner: 10 parcelas de R$ 10 viradas para R$ 20 com 4
faturas já pagas — os R$ 40 têm que aparecer na fatura aberta, sem nada reescrito, e os
pagamentos das 4 têm que continuar valendo o que valiam.

### `prototipo/verificar.js` — as provas, executáveis

`node prototipo/verificar.js`, sem navegador, `exit 1` se qualquer prova falhar. **Sete blocos,
39 provas.** Substitui roteiros em prosa — roteiro escrito ninguém roda.

1. **A tela contra a API do modelo** — todo `CB.x` chamado existe no que `dominio.js` exporta.
2. **As consultas de cada tela respondem.**
3. **`conferir()` vazio.**
4. **Os quatro números do seed** (PESSOAL, 27/08/2026): em caixa `R$ 11.236,00` · dívida
   `R$ 3.560,80` · patrimônio `R$ 24.660,00` · limite disponível `R$ 11.439,20`.
5. Nenhum lançamento do ciclo sem categoria; a fila de pendências com **um** item (MEDTECH 24H).
6. **A inativação inteira.**
7. **Evento e Diário**, incluindo a prova do achado 23.

**Não cobre, e continua manual:** aparência e layout; o roteiro da **rolagem** (*cuidado:* há
quatro faturas `FECHADA` no seed e três somam `R$ 39,90` — "a primeira FECHADA" testa a fatura
errada); e dirigir a tela no Playwright.

### Marcas plantadas no seed, de propósito

- **Poupança**, `APLICACAO` nunca atualizada — a marca **DESATUALIZADA** precisa de algo para marcar.
- **Academia**, subcategoria de `LAZER`, inativada com 3 lançamentos — e, sem ninguém ter
  planejado, **a única coisa no seed que força o caso do `doCiclo`** (achado 23).
- **MEDTECH 24H**, o único lançamento sem categoria.
- **Quatro dias com evento**, para o seletor de dia ter o que navegar.

Pagar a fatura **não pode mudar o patrimônio** — se mudar, é bug.

Não simula: compartilhamento, categoria mascarada, partes da fatura, cartão virtual e adicional,
criação de recorrência pela tela, renomear categoria, e os dois eixos do relatório de gasto.

**Atenção, a partir de 16/09:** o `prototipo/` deixou de ser o lugar onde conta, meio e
lançamento acontecem primeiro — eles existem no app de verdade, em `src/main/resources/static`.
O protótipo continua valendo como banco de provas do **modelo** (o `conferir()` e o
`verificar.js` não têm equivalente no app ainda), mas quem quiser ver cadastro de conta ou o
Extrato funcionando abre o app, não ele.

## Lacunas conhecidas

- **Sem doc dono** para `Meta` (Fase 3)
- **O detalhe do lançamento não edita campo nenhum** — só resolve a pendência. O `PATCH`
  existe e é testado; o que falta é o **aviso de impacto** que o `lancamento.md` exige para
  ação retroativa, e ele é fatia própria
- **O Diário estoura a largura em 380px**: o botão `Hoje` do seletor de dia sai da tela. Achado ao conferir a Home no navegador, e não corrigido junto de propósito — bug tem fluxo próprio
- **`docs.py` ainda não tem o teto por rota** que o `ADR-0008` decidiu, nem conta o código
- **`deploy.md`, `runbook.md`, `backup-restore.md` e `observabilidade.md` seguem stub** — são
  de operação e nascem quando houver o que operar
- **Não há fatura nem patrimônio** no código: os assuntos são `usuario`, `ambiente`,
  `categoria`, `conta`, `meio`, `lancamento` e `evento`
- **O código tem sete assuntos** desde 17/09: `usuario` saiu de dentro de `ambiente` pelo
  `ADR-0014`, com `Usuario`, `Sessao`, `Senhas`, o login e os quatro casos de uso. O encontro
  dos dois no cadastro virou `CriarAmbientePessoalUseCase`, na `aplicacao` de `ambiente` —
  aplicação chama aplicação, como já era com `categoria`
- **O `LD_LIBRARY_PATH` do navegador dirigido é `~/.cache/cyberbank-driver/libs/raiz/usr/lib/x86_64-linux-gnu`** — com o `raiz/` no meio, que a nota de 17/09 tinha omitido. E `spring-boot:run` serve o front de `target/classes`: mexeu em `static/`, roda `./mvnw resources:resources` antes de recarregar, senão o navegador mostra a versão velha e a conclusão sai errada
- **Ninguém verifica papel em lugar nenhum.** Dono, editor e leitor estão no modelo e no banco;
  nenhum caso de uso os consulta. Não é buraco de segurança hoje — sem convite, todo ambiente
  tem exatamente um acesso, o do dono — mas **entra junto com o convite**, e não depois
- **`lancamento.md` está em 347 linhas**, acima do teto do `CONVENTIONS`. O `check` não acusa
  porque ele é `rascunho`, e o aviso só vale para `ativo`: quando ele virar `ativo`, quebra
- **Falta a gestão de papel e o convite**: a política de `acesso` não tem `UPDATE`, e criar
  acesso para *outro* usuário precisa de uma função `SECURITY DEFINER` (a condição "sou dono
  daqui" lê a própria tabela `acesso` e recursiona). Está escrito no `catalogo-tabelas.md`
- `evento` nasceu com RLS em 17/09, e é a **única tabela do ambiente sem o `OR` do
  `ADR-0004`**: o que o destino de um compartilhamento vê no Diário é a decisão 9, que está em
  aberto. A migration diz isso no arquivo, e o `OR` entra com o compartilhamento
- `03-dados/modelo-de-dados.md` não conhece nada de hoje
- O protótipo não exercita os dois eixos do relatório de gasto nem renomear categoria

## Conferir no navegador

**A partir de 17/09 há como dirigir o app de verdade**, e não só ler o código. O Chromium do
Playwright está em `~/.cache/ms-playwright`, e as bibliotecas de sistema que faltavam foram
baixadas com `apt-get download` e extraídas com `dpkg -x` em
`~/.cache/cyberbank-driver/libs` — **nada disso precisou de root**, e o `LD_LIBRARY_PATH`
aponta para lá.

Os roteiros vivem em `~/.cache/cyberbank-driver/` e **não estão versionados**: Playwright é
dependência nova, e a regra 3 do `CLAUDE.md` pede ADR até para ferramenta de desenvolvimento.
Enquanto a ADR não existir, a ferramenta é da máquina, não do projeto.

**Validação é por texto, não por imagem.** O roteiro imprime o que a tela renderizou, o que
saiu da viewport, erro de console, requisição falhada e resposta 4xx — e isso custa ~500
tokens. Uma captura de tela custa ~1.700, então ela só entra quando a pergunta for de olho
("ficou feio?", "o contraste está legível?").

**O que isso já pegou, e que nenhum teste pegaria:** o FAB e a tecla `N` não funcionavam na
primeira versão — os ouvintes estavam dentro da função que só era chamada ao abrir o painel,
que só abria pelo ouvinte que não existia. O código compilava e lia como certo.

## Como o trabalho acontece

**Documentação e decisão** vêm sendo feitas pelo Cowork, com a pasta do Windows conectada pela
ponte. **O código passa a ser escrito na máquina Linux, com o Claude Code no terminal** — que
clona do GitHub e entra sempre pelo `CLAUDE.md`.

Duas consequências práticas: **o push deixa de ser detalhe e vira o que sincroniza as duas
pontas**; e a *Nota de ambiente* abaixo é sobre a **ponte do Windows**, não sobre o terminal
Linux — lá o git funciona normalmente e nada daquilo se aplica.

Comandos em `.claude/commands/`: **`/esqueleto`** (uma vez só), **`/caso-de-uso`**, `/regra`,
`/endpoint`, `/migration`, `/bug`, `/meio-pagamento`, `/integracao`, `/docs-check`.

## Nota de ambiente (só na ponte do Windows)

**Deletar arquivo na máquina do Abner precisa de permissão explícita**, e o git precisa disso
para `.git/index.lock` e `.git/HEAD.lock`. **Já derrubou três commits.** Existe ferramenta para
pedir (`device_request_delete_permission`, e o Abner aprova no app); depois disso
`rm -f .git/*.lock` e a limpeza dos `tmp_obj_*` resolve. **A permissão se perde quando a ponte
reconecta** — aconteceu de novo no meio da sessão de 07/09, exatamente como esta nota previa.
Sintoma: `Operation not permitted` no `rm`. Basta pedir de novo.

`git commit -am` **não pega arquivo novo** — recém-criado precisa de `git add` explícito. E
`git commit -m` sem `-a` só commita o que está no index: conferir o `N files changed` contra o
`git status` de antes.

**Mensagem de commit longa vai por heredoc + `git commit -F -`**, nunca inline: crase dentro de
aspas duplas no bash vira substituição de comando e come pedaços da mensagem.

O git não enxerga `user.email` global dali: usar `-c user.name="Abner" -c user.email=...`. Não
há credencial de GitHub nesse shell — **push é sempre comando entregue ao Abner**.

Heredoc no shell da máquina estoura por volta de **20 KB** (`E2BIG`) — arquivo longo vai em
partes com `cat >>`.

**Editar doc e código pelo shell funciona bem com um script Python de substituição exata**,
afirmando com `assert` que cada trecho aparece **uma** vez antes de trocar — e escrevendo o
arquivo **só no fim**, para que um `assert` que falhe não deixe meia edição aplicada.
`node --check` depois de cada patch de `.js` pega erro de sintaxe, mas **não** pega símbolo que
não existe; para isso é o `verificar.js`.

**Dá para dirigir o protótipo sem o navegador do Abner:** subir `prototipo/` para o container e
abrir com o Chromium do Playwright em `file://`. A lição do achado 22 é **visitar todas as
telas**.
