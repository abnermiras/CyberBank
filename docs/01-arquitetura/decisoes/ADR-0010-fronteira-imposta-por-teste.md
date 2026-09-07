---
id: 01-arquitetura/decisoes/ADR-0010-fronteira-imposta-por-teste
titulo: "ADR-0010: a fronteira entre assuntos é imposta por teste, e domínio referencia domínio por id"
dono: quem impede a violacao do "um assunto, um pacote", e por que o grafo entre dominios e vazio
ler-junto: [01-arquitetura/modulos, 01-arquitetura/visao-geral]
status: ativo
---

# ADR-0010: a fronteira entre assuntos é imposta por teste, e domínio referencia domínio por id

- **Status:** aceita
- **Data:** 2026-09-07
- **Afeta:** `01-arquitetura/visao-geral`, `01-arquitetura/modulos`,
  `01-arquitetura/padroes-de-codigo`, `CLAUDE.md`, dependências de teste

## Contexto

O `ADR-0008` decidiu **um assunto, um pacote**, e que regra precisando de dois pacotes de
domínio é erro de modelagem. Faltava a outra metade: **quem impede.** Regra de organização sem
quem a imponha dura até a primeira semana com pressa — e o próprio `seguranca.md` já escreveu a
versão geral disso: *defesa que depende de alguém lembrar não é defesa.*

A saída mais forte parecia ser **um módulo Maven por assunto**: importar o pacote errado nem
compila, sem teste nenhum. Ela caiu por um fato do domínio, não do build.

**O domínio tem um ciclo real.** A fatura cobra lançamentos; o lançamento aponta para a fatura
em que entra. Os dois docs se referenciam porque a vida é assim. Ciclo entre módulos Maven não
compila — então o multi-módulo obrigaria, **no dia 1**, um recorte artificial do modelo para
agradar a ferramenta de build. É a ferramenta mandando no domínio, que é exatamente a inversão
que este projeto passou dois meses evitando.

## Decisão

**Um módulo Maven, pacote por assunto, e duas travas:**

1. **Um teste guarda o grafo** (ArchUnit, dependência de teste). Ele reprova import entre
   pacotes de domínio, import de camada de fora para dentro, e entidade JPA saindo da
   persistência. Falha no build, igual ao compilador falharia.
2. **Domínio referencia domínio por `id`, nunca pelo objeto.** `Lancamento` guarda `faturaId`,
   não uma `Fatura`. É essa metade que torna a primeira verdadeira: **o grafo entre domínios
   fica vazio por construção**, e o ciclo deixa de existir sem ninguém ter recortado nada.

Quem junta dois assuntos é a **camada de aplicação**, sempre. Um caso de uso lê a fatura e lê o
lançamento; um domínio nunca lê o outro.

A regra 2 não é invenção deste ADR — é o que o projeto já fazia sem ter nomeado. `fatura`,
`pagamentoDeFatura`, `estornoDe`, `rolagemDeFatura` e `parcelamento` são todos **id** no
`lancamento.md`, e *"saldo é a soma dos lançamentos"* só é barato porque nada carrega o outro
dentro.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| **Um módulo Maven por assunto** | O compilador seria a trava ideal, mas o ciclo `lançamento ↔ fatura` é real: o build obrigaria a mudar o modelo. Mais onze `pom.xml` para um desenvolvedor, e a primeira consequência seria pior do que o problema |
| **Só revisão humana** | É a defesa que depende de alguém lembrar. Com um desenvolvedor, "alguém" é a mesma pessoa que escreveu o import |
| **Checagem no `docs.py`** | Tentador — a ferramenta já existe e não custa dependência. Mas ela entende docs, e detectar import em Java por expressão regular erra em import estático, curinga e classe aninhada. Trava que dá falso negativo é pior que trava nenhuma: passa a autorizar |
| **Referência por objeto, com o teste permitindo exceções** | A exceção viraria a regra. E um `Lancamento` que carrega uma `Fatura` que carrega lançamentos é o desenho que faz toda leitura de saldo trazer meia base junto |

## Consequências

- **Ganhamos:** o build reprova a violação, e o grafo entre domínios é **vazio** — não há o que
  desenhar, e nada para manter atualizado.
- **Perdemos:** o objeto relacionado não vem de graça. Quem precisa dos dois faz **dois passos
  na aplicação**, e é onde a transação já vive.
- **Passa a ser proibido:** import entre pacotes de domínio; campo de domínio tipado como objeto
  de outro domínio; camada de dentro importando camada de fora.
- **Revisitar se:** o teste começar a acumular exceções nomeadas. Exceção ali não é sinal de que
  a regra é rígida demais — é sinal de que dois assuntos viraram um, e a resposta é mexer no
  modelo, nunca na lista de exceções.
