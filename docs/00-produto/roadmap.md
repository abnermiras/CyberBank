---
id: 00-produto/roadmap
titulo: Roadmap e fases
dono: ordem de construcao, criterio de pronto de cada fase e o que fica congelado
ler-junto: [00-produto/visao, 02-dominio/ambiente-financeiro]
status: ativo
---

# Roadmap e fases

Este doc é o dono de **quando**. A visão diz o que o Cyberbank quer ser; aqui fica a
ordem, o critério de pronto e — principalmente — o que está proibido de começar.

## A tensão que este doc resolve

A aposta é "enxergar a vida financeira inteira". Uma primeira versão só com lançamento,
fatura e dashboard entrega um RaspyBank multiusuário: correto, mas não é a aposta.

**Decisão:** a Fase 1 leva uma fatia de patrimônio — `Aplicação` modelada e patrimônio
somado. Sem rentabilidade, sem cotação, sem meta. O motivo é de modelo, não de vitrine:
aplicação **sai do fluxo de caixa** e entra no patrimônio, e essa distinção contamina
saldo, dashboard e schema. Enfiar isso depois é migration em cima de dado real; fazer
agora custa uma entidade e dois tipos de lançamento.

## Regra das fases

Uma fase só começa quando a anterior está **pronta pelo critério escrito**, não "quase".
Sem trabalho paralelo entre fases — é um desenvolvedor. Se algo de uma fase futura
parecer urgente, ele entra nesta tabela numa fase, não no sprint atual.

## Fase 1 — Desligar o RaspyBank

O corte mínimo que substitui o sistema atual no dia a dia. Alvo: ~3 meses.

| Entra | Por quê é mínimo |
|---|---|
| Cadastro e autenticação | Há mais de um usuário; é pré-requisito de todo o resto |
| Ambiente financeiro com compartilhamento | O dono do dado. Ver `docs/02-dominio/ambiente-financeiro.md` |
| Conta, meio de pagamento, categoria | O esqueleto sem o qual lançamento não existe |
| Lançamento: entrada, saída, edição, estorno | A unidade central. Saldo tem que fechar |
| Receita como conceito próprio | Não é despesa negativa — decidir depois é retrabalho de schema |
| Fatura de cartão e parcelamento | O que o RaspyBank erra hoje e mais dói |
| Aplicação e patrimônio | A fatia da aposta (ver acima) |
| Dashboard de gasto por categoria | Mês a mês, com comparação |

**Aplicação na Fase 1 é exatamente isto:** aporte e resgate como lançamentos que movem
dinheiro entre conta e aplicação; valor atual atualizado **à mão**; patrimônio = soma de
contas + aplicações. Rentabilidade, cotação e qualquer consulta de mercado ficam fora —
custo externo zero é restrição, não preferência.

**Pronto quando:**

- [ ] Um mês inteiro fechado no Cyberbank com saldo batendo com o extrato do banco
- [ ] Uma fatura de cartão fechada e paga, com parcela caindo nos meses seguintes
- [ ] Dois usuários no mesmo ambiente, e um terceiro **sem acesso** que não enxerga nada
- [ ] O RaspyBank desligado — não "em paralelo por segurança"

## Fase 2 — Tirar a digitação do caminho

O usuário deixa de digitar lançamento; só confirma categoria.

Captura de notificação de compra · pendência de categorização · regras de categorização
automática · importação de OFX · conciliação sem duplicar.

**Pronto quando:** a maioria dos lançamentos do mês entra sem digitação e nenhum
lançamento duplicado sobrevive à conciliação.

## Fase 3 — Olhar para frente

O sistema para de só contar o passado.

Recorrência · orçamento por categoria com alerta · projeção de saldo até o fim do mês ·
meta com valor-alvo, prazo e progresso.

**Pronto quando:** o sistema responde "quanto sobra até o fim do mês" e "estourei o
orçamento" sem o usuário fazer conta.

> ☐ **Em aberto:** recorrência pode precisar subir para a Fase 2 — ela é barata e é
> pré-requisito da projeção. Decidir ao fechar a Fase 1, com o modelo de lançamento na mão.

## Fase 4 — Abrir para fora

Cadastro público, LGPD, backup de dado de terceiro, saída do Raspberry Pi para nuvem.

**Pronto quando:** existe restauração de backup testada e um dado de terceiro pode ser
apagado a pedido, sem cirurgia manual no banco.

## Congelado

Não se começa, não se prototipa, não se "deixa preparado" antes da fase indicada.
Preparar para o futuro sem o futuro definido é o custo que mata projeto de um dev só.

| Item | Liberado a partir de |
|---|---|
| Rentabilidade e cotação de investimento | Fase 3 concluída |
| Entrada por voz | Fase 2 concluída |
| Open Finance | Fase 4 concluída — depende de custo e de cadastro aberto |
| App mobile nativo | Sem fase. Web responsiva + bot resolvem |
| Multi-moeda | Sem fase. Real inteiro em centavos, e ponto |
| Relatório customizável pelo usuário | Sem fase. Dashboard fixo até alguém reclamar |
| Compartilhar dado **entre** ambientes | Sem fase. Quebra o isolamento, que é o requisito de segurança |

"Sem fase" não é "nunca" — é: ninguém abre isso sem antes mover a linha aqui.

## O que este roadmap manda na visão

Os não-objetivos de `docs/00-produto/visao.md` são a coluna "Sem fase" desta tabela.
Mudou aqui, muda lá.
