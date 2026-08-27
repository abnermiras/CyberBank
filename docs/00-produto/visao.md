---
id: 00-produto/visao
titulo: Visão de produto
dono: problema, aposta central, público e restrições
ler-junto: [00-produto/glossario, 00-produto/roadmap, 02-dominio/ambiente-financeiro]
status: ativo
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

Por isso a primeira versão já leva uma fatia de patrimônio, e não só o básico de gasto —
o porquê e o tamanho exato dessa fatia estão em `docs/00-produto/roadmap.md`.

## O conceito estruturante: ambiente financeiro

Um **ambiente financeiro** é um espaço de dados criado pelo usuário — com suas contas,
lançamentos, categorias, orçamentos e metas. Um usuário pode ter vários ("Pessoal",
"Casa", "Empresa") e pode compartilhar um ambiente com outras pessoas.

Duas frases que mudam o modelo inteiro e valem desde a primeira linha de código:

- **O ambiente é o dono do dado.** Conta, lançamento e orçamento pertencem a um
  ambiente, nunca diretamente a um usuário.
- **Vazamento entre ambientes é falha crítica**, não bug de lógica.

Papéis, convite, isolamento e invariantes: `docs/02-dominio/ambiente-financeiro.md`.
O nome do conceito foi decidido em `ADR-0001`.

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

## Escopo por fase

A ordem de construção, o corte mínimo da primeira versão e o critério de pronto de cada
fase vivem em `docs/00-produto/roadmap.md`. Este doc não repete a lista — ela mudaria
nos dois lugares e divergiria em um.

## Não-objetivos

Um produto sem não-objetivo não tem foco: se nada pode ser recusado, todo pedido novo
vira prioridade e nada termina.

Os não-objetivos do Cyberbank são a coluna **"Sem fase"** da tabela de congelados em
`docs/00-produto/roadmap.md`. A lista não é repetida aqui de propósito: repetida, ela
diverge. Recusar um pedido é mover uma linha lá, nunca abrir uma exceção aqui.

## Restrições

| Restrição | Consequência |
|---|---|
| Um desenvolvedor, tempo limitado | Escopo por fase, sem trabalho paralelo |
| Roda no Raspberry Pi hoje, nuvem depois | Nada pode depender do hardware ou da rede local |
| Custo externo zero enquanto estiver no Pi | Sem serviço pago, sem API cobrada |
| Dado financeiro de outras pessoas | Isolamento por ambiente é requisito, não recurso |

## Decisões em aberto

- [ ] Quando abrir cadastro para além do círculo próximo. A Fase 4 do roadmap lista o
      que precisa estar pronto antes (segurança, backup de dado de terceiro, LGPD), mas
      o gatilho de "agora dá" ainda não existe.
- [ ] Se o Cyberbank continua sendo software de uso pessoal ou vira produto para outros.
      A resposta muda suporte, migração de dado e o que pode quebrar entre versões.
