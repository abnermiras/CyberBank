---
id: 01-arquitetura/decisoes/ADR-0012-front-estatico-servido-pelo-spring
titulo: "ADR-0012: o front é estático e o próprio Spring Boot o serve"
dono: como o front é construído, onde ele mora e quem o entrega
ler-junto: []
status: ativo
---

# ADR-0012: O front é estático e o próprio Spring Boot o serve

- **Status:** aceita
- **Data:** 2026-09-15
- **Afeta:** `01-arquitetura/estrutura-de-pastas`, `06-interface/*`, `07-operacao/build-e-run`, `07-operacao/deploy`

## Contexto

A API JSON existe e a sessão viaja em cookie (`ADR-0009`). Falta a tela — e a escolha de
como construí-la é cara de reverter, porque ela decide o que o deploy no Pi precisa ter
instalado para o resto da vida do projeto.

Três restrições já escritas empurram a decisão sozinhas: **custo externo zero** e **roda no
Pi** (`visao.md`), e **dependência nova exige ADR** (regra 3 do `CLAUDE.md`). Some-se um fato:
o protótipo (`prototipo/`) já fechou a linguagem visual inteira em HTML, CSS e JavaScript
clássico, sem build e sem dependência — e ele é a única parte do projeto que já foi validada
navegando.

## Decisão

O front é **HTML, CSS e JavaScript clássico**, sem build e sem dependência, em
`src/main/resources/static/`. O próprio Spring Boot o serve, no mesmo processo e na mesma
porta da API. O front conversa com `/api/v1/...` por `fetch`, e a sessão é o cookie do
`ADR-0009` — que o navegador manda sozinho, porque é a mesma origem.

O CSS da linguagem visual (`assets/css/cyber.css`) é **o do protótipo**, copiado uma vez. Ele
é o dono dos tokens de `docs/06-interface/direcao-visual.md`.

## Alternativas descartadas

| Alternativa | Por que não |
|---|---|
| SPA com React/Vite | Dependência nova, `node_modules` e um passo de build no deploy do Pi — três coisas que o projeto existe para não ter. E CORS ou proxy a resolver, que a mesma origem dispensa |
| Thymeleaf (render no servidor) | Jogaria fora a API JSON que já existe para estas telas, e o protótipo — a única coisa validada — é client-side. Duas formas de montar tela no mesmo projeto é a divergência começando |
| Front servido por nginx, ao lado | Um segundo processo e um segundo artefato no Pi para entregar arquivo estático que o Tomcat já entrega |

## Consequências

- **Ganhamos:** um artefato só (o jar), um processo só, uma porta só. `./mvnw spring-boot:run`
  sobe API e tela juntas. O deploy do Pi não ganha passo nenhum. Mesma origem: sem CORS, e o
  cookie `HttpOnly` funciona sem nada no meio.
- **Perdemos:** componentização e reatividade de framework. Cada tela monta o próprio DOM à
  mão, e reuso é função em arquivo compartilhado — não componente.
- **Passa a ser proibido:** passo de build no front, `node_modules`, e CDN de JavaScript. Fonte
  vem de CDN com pilha de fallback, e isso já está escrito em `direcao-visual.md`; código, não.
- **Revisitar se:** uma tela precisar de estado compartilhado complexo o bastante para que
  montar DOM à mão vire a maior fonte de bug do projeto. O gatilho concreto: o dia em que duas
  telas precisarem reagir à mesma mudança de estado sem recarregar.

## Uma consequência que morde em desenvolvimento

O cookie de sessão é `Secure` sem interruptor (`ADR-0009`), e isso não muda aqui. Navegador
trata `http://localhost` como origem confiável e aceita o cookie — então **desenvolvimento
funciona em `http://localhost:8080`**. Abrir o app pelo IP do Pi em `http` **não** funciona: o
cookie não é gravado e o login parece falhar sem erro. No Pi é TLS, e é o que
`docs/07-operacao/deploy.md` já pede.
