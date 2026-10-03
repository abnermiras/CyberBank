---
id: 02-dominio/compartilhamento
titulo: Compartilhamento de conta e cartão
dono: o vinculo que da uso de uma conta ou cartao a outro ambiente, o mascaramento de categoria e as partes da fatura
ler-junto: [02-dominio/ambiente-financeiro, 02-dominio/conta, 02-dominio/fatura-cartao]
status: ativo
---

# Compartilhamento de conta e cartão

> **Quando.** O **modelo** deste doc entrou na Fase 1 — ele contamina schema e política de
> acesso. A **funcionalidade da conta** (criar e revogar o vínculo, categoria mascarada) saiu em
> 2026-09-28, e entre pessoas diferentes em 2026-10-03. O que continua congelado é o **cartão**: partes da fatura, patrimônio parcial e
> quem fecha a fatura (`docs/00-produto/roadmap.md`).

Três coisas diferentes usam a palavra "compartilhar" e **não são a mesma**:

| | O que é | O que a pessoa ganha |
|---|---|---|
| **Acesso ao ambiente** | Convidar alguém para o seu ambiente inteiro | Vê e mexe em tudo daquele ambiente, conforme o papel (`docs/02-dominio/ambiente-financeiro.md`) |
| **Compartilhar conta ou cartão** | Dar **uso** de um objeto seu a outro ambiente | Usa aquela conta ou aquele cartão, e só |
| **Cartão adicional** | Um cartão do contrato emitido para outra pessoa | Tem o cartão dela, com a parte dela na fatura |

Este doc é dono do segundo. O terceiro é caso de `docs/02-dominio/meio-de-pagamento.md`.

## O que atravessa é o uso, não a posse

A conta compartilhada continua sendo do ambiente que a criou, para sempre. O que o vínculo
dá é **uso**: o outro ambiente pode lançar nela, ver o extrato e o saldo.

**A origem também vê o que outros lançaram na conta dela.** Sem isso o saldo da conta não
fecharia para quem a criou: o `Nubank` do CLT tem os lançamentos do PJ, e o dono precisa
enxergá-los para conferir o que o banco diz. É leitura, como no destino.

E a regra que sustenta tudo o resto, sem exceção:

> **Todo lançamento pertence ao ambiente em que foi feito**, mesmo quando a conta é de
> outro ambiente.

É ela que mantém categoria, orçamento e relatório de cada ambiente separados enquanto o
saldo da conta continua sendo um só. Ver `ADR-0004`.

## O que se compartilha, e com quem

| Objeto | Com quem | Observação |
|---|---|---|
| **Conta** (`CORRENTE`, `CARTEIRA`, `APLICACAO`, `BENEFICIO`) | Outro ambiente seu, ou de outro usuário | É a conta conjunta |
| **Cartão** (um meio `CREDITO`) | Outro ambiente seu, ou de outro usuário | É o mesmo plástico usado por dois — o cartão de gasolina do carro da casa |
| Conta `CARTAO` inteira | **Não se compartilha** | Compartilha-se um cartão dela. Dar a conta inteira seria entregar todos os cartões do contrato |

Quem compartilha: **só o dono do ambiente de origem** — a mesma regra de quem convida. Quem
revoga: **o dono de qualquer uma das duas pontas** (ver *Revogar*).

### O que a funcionalidade entrega hoje

| Recorte | Valor | Por quê |
|---|---|---|
| Objeto | **Conta**, com os meios dela. Conta `CARTAO` é recusada (`CONTA_CARTAO_NAO_SE_COMPARTILHA`) | O vínculo é da conta e os meios vão junto — não há vínculo de meio. Cartão traz partes da fatura e patrimônio parcial, em aberto |
| Destino | **Um ambiente em que quem compartilha altera o dado**: dono ou editor. Pode ser de outra pessoa — é o convite que dá o acesso (`AMBIENTE_DESTINO_INVALIDO` para qualquer outro) | Trazer uma conta muda as contas, o saldo e o patrimônio do destino: é escrita, e quem só lê o ambiente não escreve nele. O erro não distingue "não tenho acesso" de "só leio" nem de "é o mesmo ambiente" |
| `APLICACAO` | Pode ser compartilhada, mas **só a origem a vê na Reserva** | Informar o valor de uma aplicação é ato do dono; a Reserva do destino não teria o que oferecer. O saldo dela entra no patrimônio dos dois |

## Conta compartilhada

Exemplo literal. Usuário A tem os ambientes **CLT**, **PJ** e **CASA**. A conta `Nubank`
nasce no CLT e é compartilhada com o PJ.

| No ambiente PJ | O que acontece |
|---|---|
| Lista de contas | `Nubank` aparece marcada **"compartilhada de CLT"** |
| Meios de pagamento | Os meios da `Nubank` (débito, Pix, boleto) ficam disponíveis para lançar |
| Categoria do que ele lançar | **Do PJ.** O ambiente de origem não empresta categoria |
| Extrato da conta | **Completo**: todos os lançamentos da conta, de qualquer ambiente |
| Extrato geral (`TODAS AS CONTAS`) | **Só o que o PJ lançou.** O que vem de outro ambiente aparece só quando se filtra a conta |
| Saldo | O mesmo saldo, para os dois. Conta conjunta tem um saldo só |
| Patrimônio | A conta entra no patrimônio **dos dois** ambientes |
| Transferência | Pode transferir entre a `C6` do PJ e a `Nubank` do CLT |

Que o mesmo dinheiro apareça no patrimônio de dois ambientes é **certo, não bug**: é o que
conta conjunta significa. Somar os patrimônios de todos os ambientes de um usuário conta a
conta duas vezes — e por isso essa soma não é uma tela que o sistema oferece.

## Entre pessoas

O caso que o convite abriu, em exemplo literal. Abner tem **Pessoal** e **Casa**; empresta a
`Nubank` do Pessoal à Casa e convida a Bia para a Casa com autorização completa. A Bia tem o
**Pessoal dela**, com o `Itaú`, e o empresta à Casa.

| Na Casa | O que acontece |
|---|---|
| Contas | `Nubank` e `Itaú`, as duas emprestadas. O `Itaú` diz **de onde e de quem**: *compartilhada de Ambiente Pessoal · Bia*. Com duas pessoas, dois ambientes podem ter o mesmo nome, e o nome da pessoa é o que desfaz a dúvida; a conta do próprio usuário não leva o nome dele |
| Quem lança em quê | Abner e Bia lançam nas duas, com as categorias da Casa |
| O extrato do `Itaú`, no Pessoal da Bia | Mostra o que o Abner lançou nele pela Casa, com **`DE CASA`** no lugar da categoria e **o nome do Abner** como autor. Quem empresta a conta precisa saber quem gastou nela para fechar o saldo com o banco |
| A Bia sai da Casa, ou o Abner a tira | **O `Itaú` sai junto**: o vínculo é revogado no mesmo ato. A Casa fica com o que lançou, como em toda revogação, e para de lançar nele |
| O Abner não quer mais o `Itaú` na Casa | Ele **devolve** a conta: é dono do destino. O editor não devolve |

A herança é real e é de propósito: quem entra num ambiente usa **todas** as contas que ele
recebeu, inclusive as de quem não é a pessoa que o convidou. A Bia lança na `Nubank` do Pessoal
do Abner porque ela está na Casa; quando ela sai da Casa, deixa de lançar.

Ninguém lê o ambiente de origem da outra pessoa — só o **nome** dele, para a conta poder dizer
de onde veio.

## Cartão compartilhado

Mesmo exemplo. A conta `CARTAO` chamada **UltraVioleta** nasce no CLT, com dois cartões: o
físico `****-1234` e o virtual `FREELANCE ****-0987` (`ADR-0003`). O virtual é
compartilhado com o PJ.

| No ambiente PJ | O que acontece |
|---|---|
| Meios de pagamento | Aparece `Nubank · UltraVioleta · FREELANCE ****-0987 — compartilhado de CLT` |
| Fatura | Vê a fatura **inteira daquele cartão**, com todos os lançamentos dele, de qualquer ambiente |
| Os outros cartões do contrato | **Não vê.** O físico `****-1234` não existe para o PJ |
| A parte dele | A soma dos lançamentos que o **PJ** fez naquele cartão, destacada na fatura |
| Limite | Vê o limite do contrato e quanto está consumido — o limite é global e não se divide |
| Pagar | Pode pagar, de qualquer conta que ele acesse, qualquer valor |

**A assimetria com a conta é de propósito.** Conta conjunta é conjunta: os dois veem tudo,
senão o saldo não fecha. Cartão compartilhado é **um** cartão dentro de um contrato — quem
recebeu o cartão de gasolina não tem por que ver o que os outros cartões gastaram.

## Categoria mascarada

O lançamento de outro ambiente aparece **por inteiro** — data, descrição, valor, autor — com
uma exceção: **categoria e subcategoria ficam mascaradas.**

Não é privacidade, é higiene: as árvores de categoria são de cada ambiente
(`docs/02-dominio/categoria.md`), e um chama de `Transporte / Gasolina` o que o outro chama
de `Carro / Combustível`. Misturar as duas estragaria os dois relatórios sem informar
ninguém — e a soma da fatura é a mesma de qualquer jeito.

| Onde | Categoria |
|---|---|
| Lançamento do próprio ambiente | Cheia, normal |
| Lançamento de outro ambiente, na conta ou fatura compartilhada | **Mascarada**: no lugar dela a tela mostra **o nome do ambiente que lançou** |

A máscara é feita **no servidor**: o `categoriaId` de outro ambiente nunca sai na resposta, e
a resposta traz `deOutroAmbiente` e `ambienteNome`. O RLS de `categoria` já esconde a árvore de
fora, e a máscara é o que impede a tela de confundir *"categoria que não posso ver"* com
*"pendência"*.

Mascarado não some da soma: o valor conta no saldo, na fatura e no limite. Só a etiqueta
não aparece.

## As partes da fatura

Uma fatura de um contrato compartilhado tem **partes com dono**, e o sistema calcula cada
uma:

| Origem da parte | Dono da parte |
|---|---|
| Lançamentos de um **cartão adicional** | A pessoa para quem o adicional foi emitido |
| Lançamentos de um ambiente num **cartão compartilhado** | Aquele ambiente |
| O resto | O dono do contrato |

**A parte orienta, não trava.** Qualquer um paga qualquer valor, de qualquer conta que
acesse: quem paga pelo outro é metade da vida real. A soma dos pagamentos quita a fatura, e
o que sobrar continua como saldo devedor da conta `CARTAO` (`docs/02-dominio/fatura-cartao.md`).

> **Exemplo literal.** Fatura de R$ 1.000 do UltraVioleta. No CLT, A paga R$ 500 por Pix da
> `Nubank`. No PJ, A paga R$ 500 por boleto da `C6`. São duas transferências para a conta
> `UltraVioleta`; a fatura fica quitada, e cada extrato mostra o seu pagamento.

## Patrimônio e dívida

| Objeto compartilhado | No patrimônio do ambiente de destino |
|---|---|
| Conta | O **saldo inteiro** — é conta conjunta |
| Cartão | Só a **parte dele**: os lançamentos que ele fez e ainda não foram pagos |

A diferença tem a mesma razão da visibilidade: da conta ele é cotitular; do cartão ele é
usuário de um cartão, e o que ele deve é o que ele gastou.

## Revogar

| Regra | Valor |
|---|---|
| Quem revoga | O dono do ambiente de **origem** — parou de emprestar — **ou** o do ambiente de **destino** — **devolveu** a conta. O editor de nenhuma das pontas revoga |
| Revogação automática | Quem emprestou a conta **sai** do ambiente de destino, ou é removido dele: os vínculos que ele criou para aquele ambiente caem no mesmo ato. Conta de quem não está mais no ambiente não fica nele |
| O que acontece com o que já foi lançado | **Fica tudo.** Os lançamentos são do ambiente que os fez, e o saldo da conta depende deles |
| O que o destino perde | O objeto some da lista de meios e contas disponíveis: não recebe lançamento novo |
| O que o destino mantém | Seus lançamentos, seus relatórios e seu histórico. Nada é apagado nem movido |

É a mesma regra de remover acesso a um ambiente: **o dado é de quem lançou, não de quem
emprestou a conta.** Apagar quebraria o saldo de todo mundo que ficou.

## Invariantes

- Objeto compartilhado **nunca muda de ambiente**: a posse é de quem criou.
- Todo lançamento tem o ambiente de **quem lançou**, nunca o da conta.
- A categoria de um lançamento é sempre do **mesmo ambiente do lançamento**.
- Categoria de lançamento de outro ambiente é **sempre mascarada**, em qualquer tela.
- Uma conta `CARTAO` inteira nunca é compartilhada — só cartões dela.
- Um objeto tem no máximo **um vínculo por ambiente de destino**.
- Um objeto não é compartilhado com o próprio ambiente de origem.
- Revogar vínculo nunca apaga, move ou recategoriza lançamento.
- Só o dono do ambiente de origem compartilha, e só para um ambiente em que é dono ou editor.
- Revoga o dono da origem ou o dono do destino; ninguém mais.
- Quem emprestou e saiu do destino não deixa a conta lá.
- **Cada ambiente altera e exclui só o que lançou.** O destino lê o lançamento da origem — e a
  origem lê o do destino —, mas nenhum dos dois o corrige, estorna ou exclui. É o RLS que
  garante: o `OR` da leitura não chega ao `UPDATE` nem ao `DELETE`.
- O destino **não repassa**: a conta não é dele, e só o dono da origem compartilha.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Papéis, convite e acesso ao ambiente | `02-dominio/ambiente-financeiro` |
| Por que a dívida do cartão é uma conta | `ADR-0003` |
| O que o compartilhamento fez com o isolamento | `ADR-0004` |
| Fatura, fechamento e pagamento | `02-dominio/fatura-cartao` |
| Cartão adicional, virtual e limite | `02-dominio/meio-de-pagamento` |
| Como o patrimônio soma as contas | `02-dominio/aplicacao-patrimonio` |

## Ainda em aberto

- [ ] Compartilhar conta `APLICACAO` faz sentido na v1, ou é Fase 3 junto com metas? Hoje ela
      compartilha, e a Reserva do destino não a lista
- [ ] Pagar fatura **de uma conta emprestada**: hoje a conta pagadora é só do próprio ambiente
- [ ] O que cada ponta vê do **Diário** da outra: hoje o compartilhar fica na origem e o
      devolver, no destino, e nenhum dos dois aparece do outro lado
