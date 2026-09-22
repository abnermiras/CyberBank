---
id: 06-interface/perfil
titulo: Tela de Perfil
dono: a tela do proprio usuario: onde se entra, as secoes, o seletor de avatar e os espacos reservados
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
| 2 | **SENHA** | Senha atual, nova senha, e o aviso do que vai acontecer | funciona |
| 3 | **TELEGRAM** | O `chat id` declarado, e o botão de desvincular | funciona, e **nada o consome ainda** |
| — | **AMBIENTES** | Os ambientes a que a pessoa tem acesso, criar e renomear | funciona; fica no **topo da coluna da direita** |
| 4 | **CONVITES** | Recebidos, e os que a pessoa faz | **espaço reservado** |
| 5 | **SESSÕES** | Onde a conta está aberta, encerrar uma e encerrar todas | funciona |

A ordem é a frequência: o que se mexe de vez em quando vem antes do que é raro, e o que ainda
não funciona vem por último. **Quando o convite existir, ele sobe para logo abaixo da
identidade** — aí ele passa a ser o motivo de a pessoa abrir esta tela.

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

O bloco fica na coluna da direita, acima dos convites, porque é dali que o convite vai
depender: convida-se para **um** ambiente.

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

## CONVITES — o espaço reservado

Os blocos existem na tela, **com moldura e com título**, e cada um diz o que está
esperando. Nenhum mostra botão travado: *botão travado obriga a tela a explicar sete
vezes; botão ausente explica uma vez* (`docs/06-interface/navegacao.md`).

| Bloco | O que o texto diz |
|---|---|
| **CONVITES RECEBIDOS** | Que é aqui que o convite chega — dentro do sistema, nunca por e-mail (`docs/02-dominio/ambiente-financeiro.md`) — e que o convite ainda não existe |
| **CONVIDAR ALGUÉM** | Que convidar é do **dono** do ambiente, e que o endpoint ainda não existe |

**Por que reservar em vez de omitir:** são decisões já tomadas e escritas, e a tela
vazia sem explicação é a que ensina que o app está quebrado. O bloco reservado é o contrário:
ele diz que o lugar é aqui.

## Fora desta tela, de propósito

| O quê | Onde vai |
|---|---|
| O avatar ao lado do `autor` do lançamento | Exige o lançamento carregar o avatar de quem lançou. Entra com o ambiente compartilhado, que é quando a pergunta "quem lançou isso?" existe |
| Trocar de e-mail, excluir a conta, upload de foto | Não foram decididos (`docs/02-dominio/usuario.md`) |
| Preferências de exibição, tema, idioma | Não existem. O app tem um tema só |
