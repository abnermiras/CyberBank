---
id: 02-dominio/compartilhamento
titulo: Compartilhamento de conta e cartão
dono: o vinculo que da uso de uma conta ou cartao a outro ambiente, o mascaramento de categoria e as partes da fatura
ler-junto: [02-dominio/ambiente-financeiro, 02-dominio/conta, 02-dominio/fatura-cartao]
status: ativo
---

# Compartilhamento de conta e cartão

> **Quando.** O **modelo** deste doc entra na Fase 1 — ele contamina schema e política de
> acesso. A **funcionalidade** (criar o vínculo, categoria mascarada, partes da fatura) é
> liberada com a Fase 1 concluída (`docs/00-produto/roadmap.md`). O doc está ativo porque o
> schema depende dele desde a primeira migration.

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

Quem compartilha e quem revoga: **só o dono do ambiente de origem** — a mesma regra de
quem convida.

## Conta compartilhada

Exemplo literal. Usuário A tem os ambientes **CLT**, **PJ** e **CASA**. A conta `Nubank`
nasce no CLT e é compartilhada com o PJ.

| No ambiente PJ | O que acontece |
|---|---|
| Lista de contas | `Nubank` aparece marcada **"compartilhada de CLT"** |
| Meios de pagamento | Os meios da `Nubank` (débito, Pix, boleto) ficam disponíveis para lançar |
| Categoria do que ele lançar | **Do PJ.** O ambiente de origem não empresta categoria |
| Extrato | **Completo**: todos os lançamentos da conta, de qualquer ambiente |
| Saldo | O mesmo saldo, para os dois. Conta conjunta tem um saldo só |
| Patrimônio | A conta entra no patrimônio **dos dois** ambientes |
| Transferência | Pode transferir entre a `C6` do PJ e a `Nubank` do CLT |

Que o mesmo dinheiro apareça no patrimônio de dois ambientes é **certo, não bug**: é o que
conta conjunta significa. Somar os patrimônios de todos os ambientes de um usuário conta a
conta duas vezes — e por isso essa soma não é uma tela que o sistema oferece.

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
| Lançamento de outro ambiente, na conta ou fatura compartilhada | **Mascarada** |

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
| Quem revoga | Só o dono do ambiente de origem |
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
- Só o dono do ambiente de origem compartilha e revoga.

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

- [ ] Um ambiente de destino pode **repassar** o compartilhamento adiante? A resposta
      provável é não: quem empresta é quem tem a posse
- [ ] O destino pode **editar** lançamento que a origem fez na conta compartilhada, ou só o
      seu próprio? Conta conjunta sugere que sim; auditoria sugere que não
- [ ] O que aparece no lugar da categoria mascarada — traço, o nome do ambiente de origem,
      ou nada. É decisão de `docs/06-interface/`
- [ ] Compartilhar conta `APLICACAO` faz sentido na v1, ou é Fase 3 junto com metas?
