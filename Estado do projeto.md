# Cyberbank — estado do projeto

> **Este doc não contém documentação do sistema.** Toda a documentação vive no
> repositório, em `docs/`, com `CLAUDE.md` como roteador. Aqui ficam só as decisões
> tomadas e o que falta decidir — para uma sessão nova entender onde paramos sem ler
> o repositório inteiro.

Atualizado em: 2026-08-26

## O que é

Gestão financeira pessoal auto-hospedada e multiusuário. Evolução do RaspyBank —
**não é refatoração**: domínio e arquitetura novos.

## Decisões tomadas

| Decisão | Valor |
|---|---|
| Stack | Java 21 + Spring Boot · PostgreSQL · Docker |
| Hospedagem | Raspberry Pi por ora (~3 usuários), nuvem depois — nada pode depender do Pi |
| Público | Qualquer pessoa se cadastra |
| Aposta do produto | Enxergar a vida financeira inteira: entrada, gasto, investido, guardado, metas |
| Conceito estruturante | **Ambiente financeiro** é o dono do dado; usuário só tem *acesso* a ambientes |
| Documentação | Modular em `docs/`, `CLAUDE.md` roteia tarefa→documentos, fluxos em `docs/08-fluxos/` |
| Fonte da verdade | O repositório. Este Project guarda só decisões, nunca cópia de doc |

## Primeira versão (~3 meses, para desligar o RaspyBank)

Lançamentos corretos · fatura de cartão e parcelas corretas · ambientes financeiros com
compartilhamento · dashboard de gasto por categoria · cadastro e autenticação.

Fora da v1, dentro da visão: captura automática, investimentos, metas, projeção de
saldo, orçamento, voz, Open Finance. Nada está permanentemente fora — a decisão é
sempre *quando*, não *se*.

## Em aberto (na ordem em que precisam ser resolvidas)

1. **Roadmap** — resolver a tensão: a aposta é "vida financeira inteira" mas a v1 não
   entrega investimento nem meta. Ou a v1 muda, ou a aposta muda.
2. **Papéis e regras de compartilhamento** de ambiente (dono/editor/leitor? quem convida?)
3. **Nome do conceito** — "ambiente financeiro" colide com ambiente de execução
   (dev/homologação/produção). Manter e sempre qualificar, ou renomear?
4. **Núcleo do domínio** — `lancamento`, `conta`, `meio-de-pagamento`, `categoria`,
   os quatro na mesma sessão porque se referenciam.
5. **Quando abrir cadastro** para terceiros e o que precisa estar pronto antes
   (segurança, backup de dado de terceiro, LGPD).

## Estado da documentação

53 documentos, a maioria em `status: stub` de propósito. Stub = conteúdo inexistente:
perguntar, nunca deduzir. Escritos: router, convenções, 6 fluxos, visão, glossário,
meio-de-pagamento, ADRs.