# Cyberbank — estado do projeto

> **Este doc não é documentação do sistema.** A documentação vive em `docs/`, com `CLAUDE.md`
> como roteador. Aqui ficam as **decisões tomadas, o que falta decidir e como se trabalha** —
> para uma sessão nova entender onde paramos sem ler o repositório inteiro.
>
> **Ele está fora do roteador de propósito** (`ADR-0008`): é doc de passagem entre sessões, não
> de tarefa, e não deve entrar no custo de rota nenhuma.

Última sessão: **2026-09-07**. Objetivo do Abner: *fechar a fatura de forma definitiva, sem
dúvida nenhuma sobrando para o código*. Feito, e depois nasceu o `ADR-0008`.

**Pendente: o push.** `origin/main` está em `151ed4a`; o local tem **oito commits à frente**.
Árvore limpa, check em 0 erros e 0 avisos.

| Sessão | O que saiu |
|---|---|
| **07/09** | A fatura fechou: **encerrada não abre** · **`CARTAO` não tem saldo de abertura** · **a janela é uma só** (`FECHADA` com `a pagar` > 0 é o que se abre **e** o que se paga) · limpeza dos resíduos do `d42e378` · **`ADR-0008`**, o roteador valendo para o código · este doc veio para o repositório · **`D1` fechado**: `seguranca.md` escrito e **`ADR-0009`** |
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
| Autenticação | E-mail e senha, sessão própria. Nenhuma dependência externa, nenhum provedor de identidade |
| **Senha** | Hash **Argon2id** (não bcrypt): custa memória além de tempo, e resiste melhor a GPU. Parâmetros **calibrados no host**, porque a memória do Pi é pouca |
| **Sessão — `ADR-0009`** | **No servidor.** Cookie com identificador **opaco** (`HttpOnly`, `Secure`, `SameSite=Lax`), estado em tabela, expiração absoluta **e** por inatividade. Quem decidiu foi a **revogação**: tirar acesso tem efeito no clique seguinte. Trocar a senha derruba todas as sessões |
| **O login é a superfície mais atacada** | Resposta e tempo **idênticos** para e-mail inexistente e senha errada · atraso progressivo **por conta e por origem** · bloqueio temporário. Vale igual para a recuperação de senha |
| **Exposição** | **HTTPS sempre**, não "quando sair da rede local" — sem ele o cookie viaja em claro. Só a aplicação escuta; o Postgres nunca é publicado. **O Pi não é fronteira de segurança** |
| **Ambiente sem acesso responde como inexistente** | `403` distinto de `404` conta ao curioso que aquele ambiente existe |
| **Log não tem RLS** | Por isso valor, descrição e categoria de lançamento **não vão para log** — o isolamento do `ADR-0002` para na borda do arquivo. Identificador pode ir |
| **Cadastro aberto** | Mantido em 07/09, com o gatilho já olhado. O que o sustenta é o modelo: **o cadastro não dá acesso a nada** — cria usuário, *Ambiente Pessoal* vazio e as categorias de sistema. Para chegar ao dinheiro de alguém é preciso ser **convidado** |
| **A pergunta virou "quando fechar"** | Não é mais *quando abrir* o cadastro: o gatilho para fechar é **sair da rede local** |
| O que o cadastro dispara | Um ato só, três efeitos: usuário · ambiente **"Ambiente Pessoal"** · as categorias de sistema dele |
| **E-mail — `ADR-0007`** | SMTP do Gmail com senha de app, custo zero. Escopo fechado: **só quando a pessoa não consegue entrar**. Convite, compartilhamento e aviso a quem já está dentro chegam **pelo sistema** |
| **Informação do sistema chega pelo sistema** | A razão durável no lugar da circunstancial ("não há e-mail no Pi"). Sobrevive a mudar de host, provedor ou rede |
| Ambiente ativo | No caminho da URL: `/api/v1/ambientes/{id}/...` — **decidido, e ainda não escrito em doc nenhum** |
| **Roteador vale para o código — `ADR-0008`** | O endereço do código é **derivável do nome do doc dono**; um assunto, um pacote; sem pasta de topo por camada; a rota tem orçamento e o `check` o cobra |
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
| Telas | Home · Extrato · Fatura · Séries · Reserva · **Diário** · Cadastro · (falta **Perfil**) |
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

Os achados 8 a 26 são o argumento do método inteiro: todas essas regras estavam escritas,
commitadas e plausíveis. Só quebraram quando alguém **somou os números** — leu duas linhas lado
a lado, tentou usar a tela, foi conferir no doc, perguntou para que a regra servia, olhou para a
tela que ninguém estava mexendo, fez o relógio andar, ou perguntou se a coisa existia.

## Próximo passo

**Não há mais decisão de domínio de Fase 1 esperando resposta.** O que falta é técnico, e a
ordem está fixada no `lacunas-para-codigo.md`:

1. ~~**`D1` — autenticação e sessão.**~~ ✅ **Fechado em 07/09.** `seguranca.md` está ativo e
   o `ADR-0009` registra o porquê da sessão com estado. Ficou aberto só o que é Fase 2 (a
   identidade no bot do Telegram) e a **contenção do cadastro aberto**, adiada de propósito.
2. **`D2` — `04-api/convencoes.md`** + `endpoints-ambientes.md`. É onde a decisão do *"ambiente
   ativo na URL"* finalmente vira doc.
3. **`D3` — `03-dados/modelo-de-dados.md`.** Entraram `entraEmCaixa` (conta), `sistemica` e
   `inativa` (categoria) e a tabela **`evento`** inteira. O `pai` da categoria nasce imutável, e
   a política de RLS já nasce com o `OR` do `ADR-0004`.
4. **`01-arquitetura/`** — visão geral, módulos, estrutura de pastas, padrões de código. É onde
   o `ADR-0008` vira layout concreto.
5. **`07-operacao/`** — build-e-run e testes, para o código poder rodar.
6. **Rodada de protótipo** — ver abaixo; o backlog do `dominio.js` está aberto desde 01/09.
7. **Decidir a cor de categoria** (decisão em aberto 0) e propagar para `direcao-visual.md`.
8. **Tela de Perfil** com a caixa de convites; **renomear categoria** na tela; cadastro de
   **conta** e de **meio**.

## Decisões em aberto

**Da Fase 1: uma, e é de interface.**

0. **Cor de categoria × "cada cor tem um significado".** `direcao-visual.md` diz que rosa é
   sempre alerta, verde é entrada, amarelo é ação — e que *"cor decorativa que não significa
   nada é o começo do fim da legibilidade"*. Categoria colorida quebra isso por construção, e
   **o seed já quebrava** (`LAZER` é rosa e não é alerta). As saídas: (a) o doc ganha um
   parágrafo — *cor de categoria é identidade, não semântica, e por isso nunca aparece em tag
   de estado*; (b) a categoria perde a cor. A divergência está anotada no `app.js`. Sobra
   também a **poda** da lista de 11 raízes, que espera o corte do Abner.

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

*Saíram desta lista em 07/09:* **antecipar parcelas** e **parcelamento da própria fatura**
(nunca foram dúvidas — são Fase 2, e agora estão sob *Fora desta fase* no `fatura-cartao.md`);
**fatura em moeda estrangeira** (já respondida pelo roadmap e pelo `conta.md`); e **onde vive
este doc** — a resposta é: aqui, em `claude/` no repositório, fora do roteador.

*(A pergunta "mover uma subcategoria muda o consumo de orçamento de meses já fechados?" deixou
de existir em 30/08, junto com o mover. Está riscada no `orcamento.md` em vez de apagada, porque
a razão de ela ter sumido vale mais que a pergunta.)*

## Estado da documentação

**69 documentos**, 34 stubs. Stub = conteúdo inexistente: **perguntar, nunca deduzir.**

Escritos: `CLAUDE.md`, `CONVENTIONS.md`, os 6 fluxos, todo o `00-produto/` menos `jornadas`,
`02-dominio/` inteiro menos `orcamento`, `regras-categorizacao` e `importacao-conciliacao`,
`06-interface/` (navegacao, direcao-visual), **`01-arquitetura/seguranca`** e as ADRs
**0001 a 0009**.

`fatura-cartao.md` está em **300 linhas, no teto do `CONVENTIONS`** — a próxima coisa que entrar
ali obriga a quebrar por subdomínio, como o `fatura-pagamento.md` já nasceu.

**Custo de contexto** (`docs.py custo`, 07/09): base ~1.420 tokens; rotas entre ~2.0k e ~11.7k;
ler tudo custaria ~73k. **A conta muda quando os stubs forem escritos** — e o `ADR-0008` manda
o `custo` passar a contar o código e entrar no `check`, com teto por rota.

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
criação de recorrência pela tela, cadastro de conta e de meio, renomear categoria, e os dois
eixos do relatório de gasto.

## Lacunas conhecidas

- **Sem doc dono** para `Meta` (Fase 3)
- **Sem doc de API nenhum**; a decisão do "ambiente ativo na URL" não está escrita no repositório
- Repositório ainda **sem `.gitignore`**
- O `ADR-0004` encareceu o isolamento: a política de RLS ganha um `OR` com subconsulta, e isso
  não foi escrito em `03-dados/` — a tabela `evento` também precisa de RLS
- `03-dados/modelo-de-dados.md` não conhece nada de hoje
- O protótipo não exercita os dois eixos do relatório de gasto nem renomear categoria

## Nota de ambiente

**Deletar arquivo na máquina do Abner precisa de permissão explícita**, e o git precisa disso
para `.git/index.lock` e `.git/HEAD.lock`. **Já derrubou três commits.** Existe ferramenta para
pedir (`device_request_delete_permission`, uma vez por sessão, e o Abner aprova no app); depois
disso `rm -f .git/*.lock` e a limpeza dos `tmp_obj_*` resolve. **A permissão se perde quando a
ponte reconecta.**

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
