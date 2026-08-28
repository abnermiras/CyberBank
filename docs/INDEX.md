---
id: INDEX
titulo: Mapa completo da documentacao
dono: o indice de todos os documentos
ler-junto: []
status: ativo
---

# Mapa da documentacao

> Gerado por `docs/_tools/docs.py index`. Nao edite a mao.

Leia este arquivo **so quando a tabela de roteamento do `CLAUDE.md` nao resolver**.
Regras de escrita: `docs/CONVENTIONS.md`.

## Raiz

| Documento | Dono do fato | Status |
|---|---|---|
| [`CONVENTIONS.md`](CONVENTIONS.md) | regras de escrita, nomeação, front-matter e manutenção dos docs | ativo |

## Produto — o que estamos construindo e por que

| Documento | Dono do fato | Status |
|---|---|---|
| [`glossario.md`](00-produto/glossario.md) | o significado canônico de cada termo do domínio e onde vivem as regras de cada um | ativo |
| [`jornadas.md`](00-produto/jornadas.md) | o passo a passo das jornadas principais e onde o sistema perde o usuario | stub |
| [`roadmap.md`](00-produto/roadmap.md) | ordem de construcao, criterio de pronto de cada fase e o que fica congelado | ativo |
| [`visao.md`](00-produto/visao.md) | problema, aposta central, público e restrições | ativo |

## Arquitetura — como o sistema e organizado

| Documento | Dono do fato | Status |
|---|---|---|
| [`ambientes-de-execucao.md`](01-arquitetura/ambientes-de-execucao.md) | quais ambientes existem, hosts, portas, diferencas de configuracao | stub |
| [`decisoes/ADR-0000-template.md`](01-arquitetura/decisoes/ADR-0000-template.md) | formato padrão de uma ADR | ativo |
| [`decisoes/ADR-0001-nome-ambiente-financeiro.md`](01-arquitetura/decisoes/ADR-0001-nome-ambiente-financeiro.md) | a decisão sobre a colisão do termo ambiente | ativo |
| [`decisoes/ADR-0002-isolamento-por-ambiente.md`](01-arquitetura/decisoes/ADR-0002-isolamento-por-ambiente.md) | como o isolamento entre ambientes financeiros e imposto | ativo |
| [`decisoes/ADR-0003-cartao-de-credito-e-conta.md`](01-arquitetura/decisoes/ADR-0003-cartao-de-credito-e-conta.md) | como a divida de cartao e representada e o que isso faz com o pagamento de fatura | ativo |
| [`decisoes/ADR-0004-compartilhamento-entre-ambientes.md`](01-arquitetura/decisoes/ADR-0004-compartilhamento-entre-ambientes.md) | o que o compartilhamento faz com a regra de isolamento do ADR-0002 | ativo |
| [`decisoes/README.md`](01-arquitetura/decisoes/README.md) | índice das ADRs e regra de quando escrever uma | ativo |
| [`estrutura-de-pastas.md`](01-arquitetura/estrutura-de-pastas.md) | onde cada tipo de arquivo mora no repositorio | stub |
| [`modulos.md`](01-arquitetura/modulos.md) | lista canonica dos modulos/bounded contexts e as dependencias permitidas entre eles | stub |
| [`observabilidade.md`](01-arquitetura/observabilidade.md) | logs, metricas, health checks e alertas | stub |
| [`padroes-de-codigo.md`](01-arquitetura/padroes-de-codigo.md) | convencoes de nomes, estilo, DTOs, validacao, tratamento de erro no codigo | stub |
| [`seguranca.md`](01-arquitetura/seguranca.md) | autenticacao, autorizacao, gestao de segredos, superficie exposta | stub |
| [`visao-geral.md`](01-arquitetura/visao-geral.md) | estilo arquitetural, camadas, fluxo de uma requisicao ponta a ponta | stub |

## Dominio — as regras de negocio

| Documento | Dono do fato | Status |
|---|---|---|
| [`ambiente-financeiro.md`](02-dominio/ambiente-financeiro.md) | o ambiente como agregado dono do dado: papeis, acesso, convite, ciclo de vida e a regra de isolamento | rascunho |
| [`aplicacao-patrimonio.md`](02-dominio/aplicacao-patrimonio.md) | aplicacao como conta, aporte e resgate, atualizacao do valor atual e o calculo do patrimonio | rascunho |
| [`categoria.md`](02-dominio/categoria.md) | a arvore de categorias, o sentido, e o que acontece ao renomear, mover ou excluir | rascunho |
| [`compartilhamento.md`](02-dominio/compartilhamento.md) | o vinculo que da uso de uma conta ou cartao a outro ambiente, o mascaramento de categoria e as partes da fatura | ativo |
| [`conta.md`](02-dominio/conta.md) | tipos de conta, saldo, a separacao entre fluxo de caixa e patrimonio, e o ciclo de vida | rascunho |
| [`fatura-cartao.md`](02-dominio/fatura-cartao.md) | ciclo e datas da fatura, estados, a que fatura um lancamento pertence, fechamento, pagamento e correcao | ativo |
| [`importacao-conciliacao.md`](02-dominio/importacao-conciliacao.md) | como fontes externas viram lancamentos sem duplicar | stub |
| [`lancamento.md`](02-dominio/lancamento.md) | campos, as duas datas, situacao, transferencia, correcao versus estorno e invariantes do lancamento | rascunho |
| [`meio-de-pagamento.md`](02-dominio/meio-de-pagamento.md) | tipos de meio, a regra da dataEfeito, os cartoes de um contrato e o limite | rascunho |
| [`orcamento.md`](02-dominio/orcamento.md) | limites por categoria/periodo e calculo de consumo | stub |
| [`recorrencia.md`](02-dominio/recorrencia.md) | as duas series de lancamentos: como nascem, como sao editadas e como sao canceladas | rascunho |
| [`regras-categorizacao.md`](02-dominio/regras-categorizacao.md) | como um lancamento recebe categoria automaticamente | stub |

## Dados — schema e persistencia

| Documento | Dono do fato | Status |
|---|---|---|
| [`catalogo-tabelas.md`](03-dados/catalogo-tabelas.md) | definicao coluna a coluna de cada tabela | stub |
| [`migrations.md`](03-dados/migrations.md) | ferramenta, convencao de nomes e regras de alteracao de schema | stub |
| [`modelo-de-dados.md`](03-dados/modelo-de-dados.md) | diagrama logico, entidades, relacionamentos e cardinalidades | stub |

## API — contratos expostos

| Documento | Dono do fato | Status |
|---|---|---|
| [`convencoes.md`](04-api/convencoes.md) | estilo REST, versionamento, paginacao, formatos e nomes | stub |
| [`endpoints-categorias.md`](04-api/endpoints-categorias.md) | contrato dos endpoints de categoria e regras de categorizacao | stub |
| [`endpoints-contas.md`](04-api/endpoints-contas.md) | contrato dos endpoints de conta e saldo | stub |
| [`endpoints-lancamentos.md`](04-api/endpoints-lancamentos.md) | contrato dos endpoints de lancamento | stub |
| [`endpoints-meios-pagamento.md`](04-api/endpoints-meios-pagamento.md) | contrato dos endpoints de meio de pagamento | stub |
| [`endpoints-relatorios.md`](04-api/endpoints-relatorios.md) | contrato dos endpoints de agregacao e relatorio | stub |
| [`erros.md`](04-api/erros.md) | catalogo de codigos de erro e o corpo de resposta de erro | stub |

## Integracoes — bordas com o mundo externo

| Documento | Dono do fato | Status |
|---|---|---|
| [`captura-notificacao.md`](05-integracoes/captura-notificacao.md) | o caminho da notificacao do banco ate virar lancamento pendente | stub |
| [`ofx.md`](05-integracoes/ofx.md) | leitura de arquivos OFX/extrato e mapeamento para lancamento | stub |
| [`open-finance.md`](05-integracoes/open-finance.md) | avaliacao e eventual uso do Open Finance Brasil | stub |
| [`telegram-bot.md`](05-integracoes/telegram-bot.md) | comandos, fluxo de conversa e integracao tecnica com a API do Telegram | stub |
| [`vault-segredos.md`](05-integracoes/vault-segredos.md) | onde e como segredos sao guardados e lidos | stub |
| [`visao-geral.md`](05-integracoes/visao-geral.md) | lista de integracoes externas, status e o contrato comum (porta) que todas implementam | stub |
| [`voz-whisper.md`](05-integracoes/voz-whisper.md) | transcricao local de audio para lancamento | stub |

## Interface — o que o usuario ve

| Documento | Dono do fato | Status |
|---|---|---|
| [`bot-conversas.md`](06-interface/bot-conversas.md) | roteiro das conversas: texto exato, opcoes e caminhos alternativos | stub |
| [`dashboard.md`](06-interface/dashboard.md) | telas, indicadores e navegacao do painel | stub |
| [`direcao-visual.md`](06-interface/direcao-visual.md) | a linguagem visual do Cyberbank: paleta, tipografia, forma e o limite do efeito | rascunho |
| [`navegacao.md`](06-interface/navegacao.md) | estrutura de navegacao, onde o ambiente vive na tela e como se lanca | rascunho |

## Operacao — build, deploy e incidentes

| Documento | Dono do fato | Status |
|---|---|---|
| [`backup-restore.md`](07-operacao/backup-restore.md) | o que e salvo, com que frequencia e como restaurar | stub |
| [`build-e-run.md`](07-operacao/build-e-run.md) | comandos para compilar, rodar e subir dependencias | stub |
| [`deploy.md`](07-operacao/deploy.md) | como uma versao chega em producao | stub |
| [`runbook.md`](07-operacao/runbook.md) | sintomas conhecidos e o procedimento de resposta | stub |
| [`testes.md`](07-operacao/testes.md) | tipos de teste, o que cada um cobre e como rodar | stub |

## Fluxos — roteiros de tarefa (entre por aqui)

| Documento | Dono do fato | Status |
|---|---|---|
| [`correcao-de-bug.md`](08-fluxos/correcao-de-bug.md) | roteiro de diagnóstico e correção de defeito | ativo |
| [`nova-integracao-externa.md`](08-fluxos/nova-integracao-externa.md) | roteiro de implementação de integração com sistema externo | ativo |
| [`nova-migration.md`](08-fluxos/nova-migration.md) | roteiro de alteração de banco de dados | ativo |
| [`nova-regra-de-dominio.md`](08-fluxos/nova-regra-de-dominio.md) | roteiro de alteração de regra ou invariante de domínio | ativo |
| [`novo-endpoint.md`](08-fluxos/novo-endpoint.md) | roteiro de criação/alteração de endpoint REST | ativo |
| [`novo-meio-de-pagamento.md`](08-fluxos/novo-meio-de-pagamento.md) | roteiro de implementação de um novo meio de pagamento | ativo |

---

**59 documentos · 36 ainda em stub.**
Stub = conteudo inexistente: pergunte, nao deduza.
