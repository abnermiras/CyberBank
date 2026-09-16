---
id: 04-api/erros
titulo: Contrato de erros
dono: o corpo de resposta de erro, o catalogo de codigos e a regra do que o erro nao conta
ler-junto: [04-api/convencoes, 01-arquitetura/seguranca]
status: ativo
---

# Contrato de erros

Duas regras sustentam tudo aqui:

1. **O cliente lê o código, nunca a mensagem.** A mensagem é para gente e pode ser reescrita a
   qualquer momento; o código é contrato e só muda com quebra registrada.
2. **O erro não conta o que a pessoa não podia saber.** Mensagem de erro é a fonte de
   informação mais barata que um curioso tem (`docs/01-arquitetura/seguranca.md`).

## O corpo

`application/problem+json` (RFC 7807), com dois acréscimos nossos — `codigo` e `erros`:

```json
{
  "type":     "https://cyberbank/erros/FATURA_NAO_RECEBE_PAGAMENTO",
  "title":    "Esta fatura não recebe pagamento",
  "status":   409,
  "detail":   "A fatura de agosto/2026 já foi encerrada.",
  "instance": "/api/v1/ambientes/7/faturas/42/pagamentos",
  "codigo":   "FATURA_NAO_RECEBE_PAGAMENTO"
}
```

`codigo` é o campo que o cliente usa. Ele repete o final do `type` de propósito: o `type` é o
que a RFC pede, e o `codigo` é o que se lê sem parsear URL.

**Validação de campo devolve `erros`**, e devolve **todos de uma vez** — validar um campo por
vez faz o usuário descobrir o formulário errado em quatro tentativas:

```json
{
  "type":   "https://cyberbank/erros/VALIDACAO",
  "title":  "Requisição inválida",
  "status": 422,
  "codigo": "VALIDACAO",
  "erros": [
    { "campo": "valor",     "codigo": "OBRIGATORIO",  "mensagem": "Informe o valor." },
    { "campo": "dataEvento","codigo": "FORMATO",      "mensagem": "Use AAAA-MM-DD." }
  ]
}
```

`campo` usa o **nome do campo no JSON** (`camelCase`, em português), não o nome interno.

## Os status, e o que cada um significa aqui

| Status | Quando |
|---|---|
| `400` | O corpo não é JSON válido, ou não dá para interpretar a requisição |
| `401` | Não autenticado, ou sessão expirada |
| `403` | Autenticado, **tem acesso ao ambiente**, e o papel não permite. Leitor tentando lançar |
| `404` | O recurso não existe **ou não é seu** — ver *O que o erro não conta* |
| `409` | Uma regra de domínio recusou o estado atual. É o status da maior parte do catálogo |
| `422` | A requisição foi entendida e os valores não passam na validação |
| `429` | Tentativas demais (login, recuperação de senha) |
| `500` | Falha nossa. **Nunca carrega `detail` com conteúdo** — ver abaixo |

**`409` e `422` não se confundem:** `422` é *"este valor não serve"* e se corrige mudando o
corpo; `409` é *"o mundo não está no estado que essa operação exige"* e o corpo está certo.
Pagar uma fatura encerrada é `409`; pagar com valor negativo é `422`.

## O catálogo

Erro previsível tem código, e **o código entra aqui antes de existir no código**
(`docs/08-fluxos/novo-endpoint.md`). Os que o domínio já força:

| Código | Status | Quando |
|---|---|---|
| `NAO_AUTENTICADO` | 401 | Sem sessão, ou sessão expirada |
| `CREDENCIAIS_INVALIDAS` | 401 | Login. **A mesma resposta para e-mail inexistente e senha errada** |
| `MUITAS_TENTATIVAS` | 429 | Login ou recuperação de senha barrados pelo atraso progressivo |
| `SEM_PERMISSAO` | 403 | O papel no ambiente não permite a operação |
| `NAO_ENCONTRADO` | 404 | Recurso inexistente, ou de um ambiente a que o usuário não tem acesso. **Cobre também rota e arquivo estático que não existem** — um `404` de asset não é falha nossa, e mandá-lo para o `500` enche o log de stack trace por erro de digitação |
| `EMAIL_JA_CADASTRADO` | 409 | Cadastro com e-mail que já existe. **É o único ponto do sistema que revela a existência de uma conta**, e não tem como não revelar: dois cadastros com o mesmo e-mail seriam o mesmo login. Login e recuperação continuam respondendo igual — a contenção do cadastro aberto é o que fecha esta porta, e está adiada de propósito (`docs/01-arquitetura/seguranca.md`) |
| `VALIDACAO` | 422 | Um ou mais campos inválidos. Traz `erros` |
| `CONTA_INATIVA` | 409 | Lançamento **do usuário** numa conta inativa. O que o ciclo cria não passa por aqui |
| `CATEGORIA_NAO_ESCOLHIVEL` | 409 | Categoria inativa, raiz com filho ativo, ou categoria de sistema |
| `CATEGORIA_COM_LANCAMENTO` | 409 | Excluir categoria cuja árvore tem lançamento. O caminho é inativar |
| `CATEGORIA_DE_SISTEMA_PROTEGIDA` | 409 | Renomear, inativar, excluir uma categoria de sistema — ou pendurar subcategoria nela. O sistema depende dela **por identidade**, e sem ela o ciclo não consegue lançar |
| `CATEGORIA_PAI_INVALIDO` | 409 | O `paiId` aponta para uma **subcategoria**. A árvore tem exatamente dois níveis: subcategoria não tem filhos |
| `CATEGORIA_COM_SUBCATEGORIA` | 409 | Excluir uma raiz que ainda tem subcategoria. Excluir a raiz orfanaria a filha; o caminho é esvaziar a árvore antes, ou inativar a raiz |
| `FATURA_NAO_RECEBE_PAGAMENTO` | 409 | A fatura não é `FECHADA` com `a pagar` maior que zero |
| `FATURA_NAO_ABRE` | 409 | Não é a última fechada, ou já encerrou |
| `LANCAMENTO_DO_CICLO` | 409 | Excluir o que o ciclo criou: parcela isolada, par de rolagem, lançamento de abertura |
| `CARTAO_SEM_SALDO_INICIAL` | 422 | Saldo inicial numa conta `CARTAO` |
| `TIPO_DE_CONTA_IMUTAVEL` | 409 | Trocar o tipo de uma conta que já tem lançamento |
| `AMBIENTE_INVALIDO` | 409 | Categoria de outro ambiente, ou conta sem vínculo (`ADR-0004`) |

**Cada linha aponta para uma invariante já escrita no domínio.** Código novo sem invariante
por trás é sinal de regra inventada no controller — e regra no controller é proibida
(regra 1 do `CLAUDE.md`).

## O que o erro não conta

- **Rota de `/api/**` sem sessão responde `401`, não `404`.** O interceptador de sessão roda
  **antes** do roteamento, então um anônimo recebe a mesma resposta para rota existente e
  inexistente — ele não descobre a superfície da API pelo código de erro. Com sessão válida, a
  rota inexistente responde `404` normalmente. Arquivo estático não passa pelo interceptador e
  responde `404` direto.
- **`404` cobre "não existe" e "não é seu".** Distinguir `403` de `404` num ambiente alheio
  conta ao curioso que aquele ambiente existe, e o identificador é sequencial. O `403` só
  aparece **dentro** de um ambiente a que a pessoa já tem acesso — ali ela já sabe que ele
  existe, e o que falta é papel.
- **O login responde igual em tudo.** Mesmo código, mesma mensagem e **mesmo tempo** para
  e-mail inexistente e senha errada. A recuperação de senha segue a mesma regra: responde
  igual exista ou não a conta.
- **`500` nunca carrega detalhe.** Sem stack trace, sem mensagem de exceção, sem SQL. O que
  vai para o cliente é o código e um identificador de ocorrência; o resto vai para o log — e
  o log obedece `docs/01-arquitetura/seguranca.md`, que proíbe valor e descrição de lançamento
  ali dentro.
- **Nenhuma mensagem repete o que o usuário mandou.** Ecoar entrada em mensagem de erro é como
  se planta *cross-site scripting* na tela que mostra o erro.

## Invariantes

- Todo erro previsível tem `codigo`, e o código está neste doc.
- O cliente decide pelo `codigo`; mensagem nunca é contrato.
- `VALIDACAO` devolve **todos** os campos inválidos de uma vez.
- Recurso de ambiente ao qual o usuário não tem acesso responde `404`, igual a inexistente.
- Login e recuperação de senha respondem o mesmo código, a mesma mensagem e no mesmo tempo,
  exista ou não a conta.
- `500` não expõe stack trace, mensagem de exceção, SQL nem valor informado.
- Código de erro só é removido ou renomeado com quebra registrada (`04-api/convencoes`).

## Fronteiras com outros docs

| Pergunta | Doc dono |
|---|---|
| Forma da URL, formatos, paginação e compatibilidade | `04-api/convencoes` |
| Por que o ambiente sem acesso responde como inexistente | `01-arquitetura/seguranca` |
| A invariante por trás de cada código | o doc do agregado em `02-dominio/` |
| Roteiro de criar ou mudar endpoint | `08-fluxos/novo-endpoint` |
