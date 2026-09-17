---
id: 01-arquitetura/modulos
titulo: Módulos e o grafo de dependências
dono: quais assuntos existem, quem pode depender de quem, e como dois assuntos conversam
ler-junto: [01-arquitetura/visao-geral, 01-arquitetura/estrutura-de-pastas]
status: ativo
---

# Módulos e o grafo de dependências

## A lista de assuntos não mora aqui

**Os assuntos são os docs donos de `docs/02-dominio/`** — é o `ADR-0008`: o nome do doc é o
nome do pacote. `conta`, `categoria`, `meio-de-pagamento`, `lancamento`, `fatura`,
`aplicacao-patrimonio`, `recorrencia`, `ambiente-financeiro`, `usuario`, `compartilhamento`,
`evento`, `orcamento`.

Repetir a lista aqui criaria a segunda cópia que a regra 1 do `CONVENTIONS` proíbe — e ela
divergiria no dia em que um assunto novo nascesse. **Assunto novo é doc novo em `02-dominio/`;
o pacote vem junto, e este doc não muda.**

Dois casos que valem dizer, porque o mapeamento não é um-para-um por arquivo:

- **`fatura`** tem dois docs (`fatura-cartao` e `fatura-pagamento`) e **um** pacote. Os docs se
  separaram por tamanho; o assunto é um só.
- **`ambiente-financeiro`** vira o pacote `ambiente`, dono de acesso, papel e convite — é ele
  que responde *quem pode o quê*. **`usuario` é um assunto à parte** (`ADR-0014`): quem entra,
  a senha, a sessão e o perfil. O ambiente é dono do dado; o usuário só tem acesso a ambientes,
  e a fronteira no código é a mesma da frase.

## O grafo entre assuntos é vazio

Não há seta nenhuma entre pacotes de domínio, e não é simplificação: é o `ADR-0010`. **Domínio
referencia domínio por `id`, nunca pelo objeto.** `Lancamento` guarda `faturaId`, `contaId` e
`categoriaId` — três números, nenhum import.

Por isso não existe diagrama de dependência para manter aqui. O que existe é a **regra de
camada**, que vale dentro de cada assunto e está no `visao-geral.md`: a seta aponta para dentro.

E é isso que resolve o ciclo. `fatura` e `lancamento` se referenciam nos docs porque a vida é
assim — a fatura cobra lançamentos, o lançamento entra numa fatura —, e no código **nenhum dos
dois importa o outro**.

## Como dois assuntos conversam: pela aplicação

Quem precisa dos dois é um **caso de uso**, e é ele que faz os dois passos.

> **Exemplo literal.** Pagar fatura toca `fatura` (a regra de qual fatura recebe), `conta` (a
> pagadora) e `lancamento` (o par da transferência). O `PagarFaturaUseCase` busca cada um pela
> sua porta e chama as três regras. Nenhuma das três sabe das outras.

**Não há evento de domínio, nem fila, nem barramento.** Chamada direta, dentro de uma
transação. São três usuários num Raspberry Pi: mensageria aqui compraria desacoplamento que
ninguém pediu e pagaria com entrega eventual — num sistema em que *saldo é a soma dos
lançamentos* e a tela mostra o número na hora.

**O `Evento` do domínio não é isso.** Ele é **registro** — *o que aconteceu, quando e por quem*
(`docs/02-dominio/evento.md`) —, não mecanismo de comunicação. Nada no sistema reage a um
evento gravado. Confundir os dois é o erro que transformaria o Diário em acoplamento
escondido.

## Quem é dono do quê

**O doc dono já responde**, e a pergunta se resolve pelo `dono:` do front-matter — não por uma
tabela aqui, que seria a terceira cópia da mesma informação.

A regra prática, quando não estiver óbvio: **o assunto que é dono de uma entidade é aquele cuja
invariante ela quebra.** A `Fatura` é de `fatura` porque as regras de ciclo são dela; o
`Lancamento` é de `lancamento` mesmo aparecendo na fatura, na conta e no relatório, porque
quem decide se ele é válido é o `lancamento.md`.

## O que trava a violação

Um teste no build (`ADR-0010`), que reprova três coisas:

1. import entre pacotes de domínio;
2. camada de dentro importando camada de fora (`dominio` que conhece `api`, `aplicacao` ou
   `persistencia`);
3. entidade JPA saindo de `persistencia`.

**O teste não tem lista de exceções**, e é de propósito. Exceção ali não significa que a regra
é rígida demais — significa que dois assuntos viraram um, e a resposta é mexer no modelo.

## Invariantes

- Os assuntos são os docs de `02-dominio/`. A lista não é mantida aqui.
- Nenhum pacote de domínio importa outro pacote de domínio.
- Domínio referencia domínio por `id`, nunca por objeto.
- Dois assuntos só se encontram num caso de uso da `aplicacao`.
- Não há evento de domínio, fila ou barramento entre assuntos.
- O teste de fronteira não tem exceções nomeadas.

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| As camadas e a regra de importação | `01-arquitetura/visao-geral` |
| Por que a fronteira é imposta por teste | `ADR-0010` |
| Por que o pacote espelha o doc | `ADR-0008` |
| A árvore de pastas e o nome dos pacotes | `01-arquitetura/estrutura-de-pastas` |
| O que cada assunto decide | o doc dele em `02-dominio/` |
