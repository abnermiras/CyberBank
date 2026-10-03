---
id: 06-interface/perfil
titulo: Tela de Perfil
dono: a tela do proprio usuario: onde se entra, as secoes, o seletor de avatar, os convites e as pessoas de cada ambiente
ler-junto: [02-dominio/usuario, 06-interface/navegacao, 06-interface/direcao-visual]
status: ativo
---

# Tela de Perfil

A única tela do sistema que **não é sobre dinheiro**. Tudo o que o app mostra é de um
ambiente; aqui é da pessoa — e é por isso que ela não entra pelo rail lateral.

## A porta é o avatar, no canto superior direito

O header já tem o bloco `IDENTIDADE` com o nome e um quadrado de avatar. O quadrado **vira
botão** e abre o Perfil; o `Sair`, ao lado, continua sendo um botão solto.

| Decisão | Por quê |
|---|---|
| O avatar é a porta | É o elemento que já representa a pessoa na tela, e ele está no canto onde se procura por isso |
| **Não vira menu suspenso** | O menu esconderia o `Sair` atrás de um clique a mais — e sair é a mais frequente das duas ações. Dois botões visíveis custam menos que um menu |
| O Perfil **não entra no rail** | O rail é a navegação do dinheiro (`docs/06-interface/navegacao.md`). Enquanto o Perfil está aberto, nenhum item do rail fica marcado — e é isso mesmo: a pessoa não está em tela de dinheiro nenhuma |
| O seletor de ambiente continua no header, e **não muda nada aqui** | O perfil é o mesmo em todo ambiente. Trocar de ambiente com o Perfil aberto não recarrega a tela |

## As seções, nesta ordem

| # | Seção | O que tem | Estado |
|---|---|---|---|
| 1 | **IDENTIDADE** | Avatar, nome, e-mail e a data de cadastro | funciona |
| 2 | **CONVITES RECEBIDOS** | Os convites esperando resposta: aceitar ou recusar | funciona |
| 3 | **SENHA** | Senha atual, nova senha, e o aviso do que vai acontecer | funciona |
| 4 | **TELEGRAM** | O `chat id` declarado, e o botão de desvincular | funciona, e **nada o consome ainda** |
| — | **AMBIENTES** | Os ambientes a que a pessoa tem acesso, criar e renomear, e as **pessoas** de cada um — quem está, convidar, remover, sair | funciona; fica no **topo da coluna da direita** |
| 5 | **SESSÕES** | Onde a conta está aberta, encerrar uma e encerrar todas | funciona |

A ordem é a frequência: o que se mexe de vez em quando vem antes do que é raro. **Os convites
recebidos ficam logo abaixo da identidade** porque, quando existem, são o motivo de a pessoa
abrir esta tela.

**Convidar não é uma seção, é um ato do ambiente.** Convida-se para **um** ambiente, então o
formulário mora dentro do cartão dele, em *Pessoas* — e não num bloco solto que perguntaria
"para qual?".

## IDENTIDADE

- **Os dez avatares aparecem todos, num grid**, desenhados no tamanho em que serão vistos. Não
  há prévia separada: o grid já é a prévia.
- O escolhido fica marcado com a borda de seleção; escolher **não grava** — grava o `SALVAR`
  da seção, junto com o nome.
- **O e-mail aparece travado, e diz por quê** na própria linha: *"é o seu login e a chave dos
  convites — não muda"* (`docs/02-dominio/usuario.md`). Campo que some é campo que o usuário
  procura na tela errada.
- Não há upload de foto, e a tela **não finge** que há: nenhum botão de "enviar imagem"
  desabilitado. O que existe é a lista.

## SENHA

Três campos — atual, nova, e a nova de novo — e **o aviso antes do botão**, não depois:

> **Trocar a senha encerra todas as sessões, inclusive esta.** Você vai voltar para o login.

É a regra de `navegacao.md` — *ação destrutiva ou retroativa mostra o impacto antes de
confirmar* — aplicada ao único lugar do perfil onde ela aparece. A pessoa que descobre isso
**depois** acha que foi desconectada por erro.

Ao dar certo: a tela não tenta continuar. Ela leva para o login com a mensagem *"senha
trocada — entre de novo"*, porque a sessão que ela estava usando acabou de morrer
(`docs/04-api/endpoints-usuario.md`).

**Não há "esqueci a senha" nesta tela, e ela diz isso em uma linha**: sem a senha atual não há
caminho de volta hoje. Esconder a ausência faria a pessoa procurar o link por toda a tela.

## TELEGRAM

Um campo, um número, e três frases de verdade:

| A tela diz | Porque |
|---|---|
| Que o campo é o **`chat id`**, não o `@` | `docs/02-dominio/usuario.md` |
| Que **o sistema não confere** o número | Quem digita errado só descobre no dia em que o bot falar com outra pessoa |
| Que **nada é enviado ainda**, e o que falta é o bot | É a regra da aba desabilitada de `navegacao.md`: o que existe sem funcionar diz o que está esperando |

**Como a pessoa descobre o próprio `chat id` é instrução que nasce com o bot** — é ele que
sabe responder isso. Até lá a tela não inventa um passo a passo: ela diz que o vínculo só
passa a valer quando o bot existir.

Com o campo preenchido, aparece **DESVINCULAR**, que apaga o número — é o `DELETE` do
sub-recurso (`docs/04-api/endpoints-usuario.md`), e ele não passa pelo botão de salvar do
nome e do avatar: são duas decisões diferentes, e juntá-las faria trocar de avatar mexer no
chat.

## AMBIENTES

Um cartão por ambiente a que a pessoa tem acesso, na ordem do `GET /ambientes` — o *Ambiente
Pessoal* primeiro. Cada um mostra o **nome**, o **papel** dela ali e o **dia em que foi
criado**, no horário de Brasília.

- **Renomear** abre o campo no lugar do nome, como a conta faz no Cadastro. `Enter` salva, `Esc`
  cancela. **O leitor não vê o botão**: é a regra do botão que só existe quando funciona. Se o
  ambiente renomeado é o que está em uso, o header acompanha na hora.
- **Criar** é um campo e um botão abaixo da lista. A linha de rodapé diz o que o ato faz: quem
  cria é o **dono**, e o ambiente nasce **vazio**, só com as categorias de sistema.
- **Não há ativo/inativo, desligar nem excluir** — não foram decididos
  (`docs/02-dominio/ambiente-financeiro.md`), e a tela não finge que existem.

- **O papel aparece com o nome da tela**: `DONO`, `COMPLETA` (o editor) e `SÓ LEITURA` (o
  leitor). O mesmo rótulo vale no seletor de ambiente e no chip do topo.

### Pessoas

O botão **Pessoas**, em todo cartão de ambiente e para qualquer papel, abre embaixo dele quem
tem acesso: avatar, nome, e-mail e papel, com `· você` na própria linha.

- **O dono** vê, além disso, os **convites pendentes** — um `?` no lugar do avatar, o e-mail e
  *CONVITE ESPERANDO RESPOSTA* — com **Cancelar convite**, e embaixo o formulário: e-mail, e
  **Autorização completa** ou **Somente leitura** num seletor de dois botões. Uma linha diz o
  que a escolha permite, e ela muda junto com o seletor; trocar a escolha não apaga o e-mail
  digitado. Depois de convidar, o aviso diz **onde** a pessoa vai ver o convite — inclusive se
  ela ainda não tiver cadastro.
- **Remover** (dono, na linha de outra pessoa) e **Sair do ambiente** (qualquer outro papel, na
  própria linha) pedem **confirmação no lugar**: o botão vira *Confirmar* e *Desistir*, e uma
  linha diz a consequência — a pessoa deixa de ver o ambiente, e **o que ela lançou fica**.
  A confirmação existe porque quem sai não consegue voltar sozinho: depende de um convite novo.
- **Sair do ambiente que está em uso** recarrega a página, e o seletor cai no primeiro da lista,
  como já fazia com um ambiente que deixou de existir.
- **O dono não tem botão na própria linha**: ele não sai sem transferir a propriedade, e a
  transferência não existe (`docs/02-dominio/ambiente-financeiro.md`).

## CONVITES RECEBIDOS

Um cartão por convite pendente, do mais antigo para o mais novo: o **nome do ambiente**, o
papel oferecido, **quem convidou** e quando, e uma linha que diz o que aquele papel faz —
*autorização completa: lança, corrige e cadastra; não convida nem exclui o ambiente*, ou
*somente leitura: vê tudo e não muda nada*. É tudo o que a pessoa lê do ambiente antes de
aceitar, e é o bastante para decidir.

- **Aceitar** e **Recusar**, sem confirmação — recusar custa só pedir outro convite.
- Ao aceitar, o aviso diz que o ambiente **já está no seletor do topo**, e a lista de ambientes
  ao lado ganha o cartão dele. A tela não troca de ambiente sozinha: a pessoa estava no Perfil, e
  continua nele.
- Sem convite, o bloco diz *Nenhum convite esperando por você* — e o rodapé diz que é **aqui**
  que eles chegam, nunca por e-mail.

## SESSÕES ATIVAS

Um cartão por sessão válida, o último uso mais recente primeiro. O título é o **aparelho**,
lido do navegador — *Chrome · Android*, *Firefox · Linux* —, com o texto inteiro no `title`
para quem quiser conferir; sessão sem navegador guardado diz *Navegador desconhecido*. Embaixo:
de onde entrou, quando entrou e o último uso, no horário de Brasília.

- **A sessão desta tela vem marcada** `ESTA SESSÃO`, e o botão dela é **Sair**, não
  *Encerrar*: é um logout, e leva ao login.
- **Encerrar outra** é um clique, sem confirmação — errar custa só entrar de novo naquele
  aparelho. A lista se refaz e o aviso diz que aquele aparelho sai no próximo clique.
- **Encerrar todas** tem o aviso **antes** do botão, como a troca de senha: inclui esta, e a
  pessoa volta para o login — que explica por que ela voltou.
- O rodapé diz o que encerrar **não** resolve: quem entrou uma vez sabe a senha. Sessão que a
  pessoa não reconhece pede **troca de senha**, não só encerrar.

## Fora desta tela, de propósito

| O quê | Onde vai |
|---|---|
| Trocar de e-mail, excluir a conta, upload de foto | Não foram decididos (`docs/02-dominio/usuario.md`) |
| Preferências de exibição, tema, idioma | Não existem. O app tem um tema só |
