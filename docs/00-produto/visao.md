---
id: 00-produto/visao
titulo: Visão de produto
dono: problema, aposta central, público, escopo da primeira versão e restrições
ler-junto: [00-produto/glossario, 00-produto/roadmap]
status: rascunho
---

# Visão de produto

## O que é

Cyberbank é um sistema de gestão financeira pessoal **auto-hospedado e multiusuário**.
É a evolução do RaspyBank — **não é refatoração**: modelo de domínio e arquitetura novos.

## A aposta

**Enxergar a vida financeira inteira em um lugar só.**

O RaspyBank mostra gasto. O Cyberbank precisa responder também: quanto entrou, quanto
está investido, quanto está guardado, e a que distância estão as metas. Se ao final o
sistema só souber dizer para onde o dinheiro foi, ele não se justificou.

## O conceito estruturante: ambiente financeiro

Um **ambiente financeiro** é um espaço de dados criado pelo usuário — com suas contas,
lançamentos, categorias, orçamentos e metas. Um usuário pode ter vários ("Pessoal",
"Casa", "Empresa") e pode **compartilhar** um ambiente com outros usuários segundo regras
a definir.

Isso muda o modelo inteiro e precisa valer desde a primeira linha de código:

| Consequência | Detalhe |
|---|---|
| O ambiente é o dono do dado | Conta, lançamento e orçamento pertencem a um **ambiente**, nunca diretamente a um usuário |
| Usuário é quem tem acesso | A relação usuário↔ambiente é N:N e carrega um papel/permissão |
| Toda consulta é filtrada por ambiente | Sem exceção. Consulta sem ambiente é bug de segurança, não de lógica |
| Vazamento entre ambientes é falha crítica | Nenhum usuário pode ver dado de ambiente ao qual não tem acesso, nem por bug |

> ☐ **A definir:** quais papéis existem (dono, editor, leitor?), o que cada um pode fazer,
> quem pode convidar, e o que acontece com o ambiente se o dono sair. Sem isso não há
> como implementar compartilhamento.

## O que o sistema precisa responder

| Pergunta do usuário | Exige |
|---|---|
| Onde meu dinheiro foi? | Lançamento categorizado + relatório por período |
| Quanto sobra até o fim do mês? | Projeção: fatura em aberto, parcelas e recorrências futuras |
| Estourei o orçamento? | Orçamento como conceito de primeira classe + alerta |
| Quanto entrou? | Receita modelada, não só despesa |
| Quanto investi e quanto tenho guardado? | Patrimônio e aplicação — **não é fluxo de caixa** |
| Estou perto das minhas metas? | Meta com valor-alvo, prazo e progresso |

As três últimas linhas **não existem no RaspyBank** e são o que diferencia o Cyberbank.

## Público

Qualquer pessoa pode se cadastrar. Na prática, enquanto rodar no Raspberry Pi, o
sistema atende **até ~3 usuários** — o limite é operacional, não de produto.

O código, porém, é escrito desde já como se fosse aberto: nada pode depender de "só o
Abner usa" nem de "roda no Pi". Migrar para nuvem deve ser mudança de infraestrutura,
nunca de modelo.

## Primeira versão

O que precisa estar rodando para desligar o RaspyBank (~3 meses):

- [ ] **Lançamentos corretos** — entrada, saída, edição e estorno sem erro de saldo
- [ ] **Fatura de cartão e parcelas corretas** — gasto no crédito não debita na hora, cai
      na fatura certa, parcela aparece nos meses seguintes
- [ ] **Ambientes financeiros funcionando** — criação, isolamento garantido e
      compartilhamento conforme as regras definidas
- [ ] **Dashboard de gasto por categoria** — mês a mês, com comparação
- [ ] **Cadastro e autenticação** — pré-requisito de tudo acima, já que há mais de um usuário

Fora da primeira versão (mas **dentro** da visão): captura automática de compras,
investimentos, metas, projeção de saldo, orçamento, entrada por voz, Open Finance.

> ⚠ **Tensão a resolver:** a aposta é "vida financeira inteira", mas a primeira versão
> não entrega nada de investimento, patrimônio ou meta — ela entrega um RaspyBank bem
> feito e multiusuário. Ou a primeira versão precisa de uma fatia disso, ou a aposta é
> outra. Decida em `docs/00-produto/roadmap.md` antes de começar a codar.

## Não-objetivos

Nada está permanentemente fora do escopo — a decisão é sempre *quando*, não *se*.
As exclusões por fase vivem em `docs/00-produto/roadmap.md`.

> ⚠ Um produto sem não-objetivo não tem foco. Revisite esta seção depois do roadmap:
> se ao final nada puder ser recusado, todo pedido novo vira prioridade e nada termina.

## Restrições

| Restrição | Consequência |
|---|---|
| Um desenvolvedor, tempo limitado | Escopo por fase, sem trabalho paralelo |
| Roda no Raspberry Pi hoje, nuvem depois | Nada pode depender do hardware ou da rede local |
| Custo externo zero enquanto estiver no Pi | Sem serviço pago, sem API cobrada |
| Dado financeiro de outras pessoas | Isolamento por ambiente é requisito, não recurso |

## Decisões em aberto

- [ ] Papéis e regras de compartilhamento de ambiente
- [ ] Se a primeira versão inclui alguma fatia de investimento/meta (ver Tensão acima)
- [ ] Quando abrir cadastro para além de você, e o que precisa estar pronto antes
      (segurança, backup do dado de terceiros, LGPD)
- [ ] Se "ambiente financeiro" é o termo final — ele vai aparecer em todo lugar do código