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
| [`ambientes-de-execucao.md`](01-arquitetura/ambientes-de-execucao.md) | quais ambientes existem, em que maquina cada um roda e o que muda entre eles | ativo |
| [`decisoes/ADR-0000-template.md`](01-arquitetura/decisoes/ADR-0000-template.md) | formato padrão de uma ADR | ativo |
| [`decisoes/ADR-0001-nome-ambiente-financeiro.md`](01-arquitetura/decisoes/ADR-0001-nome-ambiente-financeiro.md) | a decisão sobre a colisão do termo ambiente | ativo |
| [`decisoes/ADR-0002-isolamento-por-ambiente.md`](01-arquitetura/decisoes/ADR-0002-isolamento-por-ambiente.md) | como o isolamento entre ambientes financeiros e imposto | ativo |
| [`decisoes/ADR-0003-cartao-de-credito-e-conta.md`](01-arquitetura/decisoes/ADR-0003-cartao-de-credito-e-conta.md) | como a dívida de cartão é representada e o que isso faz com o pagamento de fatura | ativo |
| [`decisoes/ADR-0004-compartilhamento-entre-ambientes.md`](01-arquitetura/decisoes/ADR-0004-compartilhamento-entre-ambientes.md) | o que o compartilhamento faz com a regra de isolamento do ADR-0002 | ativo |
| [`decisoes/ADR-0005-rolagem-entre-faturas.md`](01-arquitetura/decisoes/ADR-0005-rolagem-entre-faturas.md) | como o saldo nao pago de uma fatura chega na fatura seguinte | ativo |
| [`decisoes/ADR-0006-situacao-provisionado.md`](01-arquitetura/decisoes/ADR-0006-situacao-provisionado.md) | por que a situacao do lancamento tem tres valores e nao dois | ativo |
| [`decisoes/ADR-0007-email-so-para-recuperar-senha.md`](01-arquitetura/decisoes/ADR-0007-email-so-para-recuperar-senha.md) | a dependencia de SMTP, e o escopo fechado de para que ela serve | ativo |
| [`decisoes/ADR-0008-roteador-vale-para-o-codigo.md`](01-arquitetura/decisoes/ADR-0008-roteador-vale-para-o-codigo.md) | a regra que mantem uma tarefa barata depois que houver codigo, e o orcamento por rota | ativo |
| [`decisoes/ADR-0009-sessao-no-servidor.md`](01-arquitetura/decisoes/ADR-0009-sessao-no-servidor.md) | como a sessao autenticada viaja e por que ela tem estado | ativo |
| [`decisoes/ADR-0010-fronteira-imposta-por-teste.md`](01-arquitetura/decisoes/ADR-0010-fronteira-imposta-por-teste.md) | quem impede a violacao do "um assunto, um pacote", e por que o grafo entre dominios e vazio | ativo |
| [`decisoes/ADR-0011-teste-contra-postgres-real.md`](01-arquitetura/decisoes/ADR-0011-teste-contra-postgres-real.md) | por que nao ha banco em memoria nos testes, e o que isso custa | ativo |
| [`decisoes/ADR-0012-front-estatico-servido-pelo-spring.md`](01-arquitetura/decisoes/ADR-0012-front-estatico-servido-pelo-spring.md) | como o front é construído, onde ele mora e quem o entrega | ativo |
| [`decisoes/ADR-0013-rotina-enxerga-ambientes-por-funcao.md`](01-arquitetura/decisoes/ADR-0013-rotina-enxerga-ambientes-por-funcao.md) | como uma rotina sem sessao atravessa o RLS sem virar bypass | ativo |
| [`decisoes/ADR-0014-usuario-e-um-assunto.md`](01-arquitetura/decisoes/ADR-0014-usuario-e-um-assunto.md) | por que usuario, sessao e senha deixam de morar no pacote do ambiente | ativo |
| [`decisoes/README.md`](01-arquitetura/decisoes/README.md) | índice das ADRs e regra de quando escrever uma | ativo |
| [`estrutura-de-pastas.md`](01-arquitetura/estrutura-de-pastas.md) | a arvore do projeto, a convencao de pacotes, onde criar cada arquivo novo e o que nao se versiona | ativo |
| [`modulos.md`](01-arquitetura/modulos.md) | quais assuntos existem, quem pode depender de quem, e como dois assuntos conversam | ativo |
| [`observabilidade.md`](01-arquitetura/observabilidade.md) | logs, metricas, health checks e alertas | stub |
| [`padroes-de-codigo.md`](01-arquitetura/padroes-de-codigo.md) | nomes de classe por camada, a conversao entre DTO, dominio e JPA, validacao, excecoes e o que e proibido | ativo |
| [`seguranca.md`](01-arquitetura/seguranca.md) | autenticacao, sessao, defesa do login, superficie exposta, segredos e dado sensivel em log | ativo |
| [`visao-geral.md`](01-arquitetura/visao-geral.md) | o estilo, as camadas, a regra de importacao e o caminho de um caso de uso ponta a ponta | ativo |

## Dominio — as regras de negocio

| Documento | Dono do fato | Status |
|---|---|---|
| [`ambiente-financeiro.md`](02-dominio/ambiente-financeiro.md) | o ambiente como agregado dono do dado: papeis, acesso, convite, ciclo de vida e a regra de isolamento | rascunho |
| [`aplicacao-patrimonio.md`](02-dominio/aplicacao-patrimonio.md) | aplicacao como conta, aporte e resgate, atualizacao do valor atual e o calculo do patrimonio | rascunho |
| [`categoria.md`](02-dominio/categoria.md) | a arvore de categorias, o sentido, e o que acontece ao renomear, inativar ou excluir | rascunho |
| [`compartilhamento.md`](02-dominio/compartilhamento.md) | o vinculo que da uso de uma conta ou cartao a outro ambiente, o mascaramento de categoria e as partes da fatura | ativo |
| [`conta.md`](02-dominio/conta.md) | tipos de conta, saldo, a separacao entre fluxo de caixa e patrimonio, e o ciclo de vida | rascunho |
| [`evento.md`](02-dominio/evento.md) | o registro do que aconteceu num dia — o que o sistema fez sozinho e o que a pessoa fez | ativo |
| [`fatura-cartao.md`](02-dominio/fatura-cartao.md) | ciclo e datas da fatura, estados, a que fatura um lancamento pertence, fechamento e abertura | ativo |
| [`fatura-pagamento.md`](02-dominio/fatura-pagamento.md) | como a fatura e paga, a rolagem do que venceu sem ser pago e a correcao de fatura ja paga | ativo |
| [`importacao-conciliacao.md`](02-dominio/importacao-conciliacao.md) | como fontes externas viram lancamentos sem duplicar | stub |
| [`lancamento.md`](02-dominio/lancamento.md) | campos, as duas datas, situacao, transferencia, correcao versus estorno e invariantes do lancamento | rascunho |
| [`meio-de-pagamento.md`](02-dominio/meio-de-pagamento.md) | tipos de meio, a regra da dataEfeito, os cartoes de um contrato e o limite | rascunho |
| [`orcamento.md`](02-dominio/orcamento.md) | limites por categoria/periodo e calculo de consumo | stub |
| [`recorrencia.md`](02-dominio/recorrencia.md) | as duas series de lancamentos: como nascem, como sao editadas e como sao canceladas | rascunho |
| [`regras-categorizacao.md`](02-dominio/regras-categorizacao.md) | como um lancamento recebe categoria automaticamente | stub |
| [`usuario.md`](02-dominio/usuario.md) | o usuario como assunto: identidade de login, nome, avatar, o chat do Telegram e a troca de senha | ativo |

## Dados — schema e persistencia

| Documento | Dono do fato | Status |
|---|---|---|
| [`catalogo-tabelas-do-ambiente.md`](03-dados/catalogo-tabelas-do-ambiente.md) | definicao coluna a coluna das tabelas com ambiente_id, com constraints, indices e politicas | ativo |
| [`catalogo-tabelas.md`](03-dados/catalogo-tabelas.md) | indice das tabelas e definicao coluna a coluna das familias do usuario e de ligacao | ativo |
| [`migrations.md`](03-dados/migrations.md) | ferramenta, numeracao e nomes, o que nunca muda depois de aplicado, e como fazer mudanca destrutiva | ativo |
| [`modelo-de-dados.md`](03-dados/modelo-de-dados.md) | entidades, relacionamentos, estrategia de chaves, como valor e data sao guardados, e o padrao de RLS | ativo |

## API — contratos expostos

| Documento | Dono do fato | Status |
|---|---|---|
| [`convencoes.md`](04-api/convencoes.md) | forma da URL, versionamento, nomes, formatos no JSON, paginacao e compatibilidade | ativo |
| [`endpoints-ambientes.md`](04-api/endpoints-ambientes.md) | a lista de ambientes do usuario e a regra do {ambienteId} nas outras rotas | ativo |
| [`endpoints-categorias.md`](04-api/endpoints-categorias.md) | contrato dos endpoints de categoria e regras de categorizacao | ativo |
| [`endpoints-contas.md`](04-api/endpoints-contas.md) | contrato dos endpoints de conta e saldo | ativo |
| [`endpoints-eventos.md`](04-api/endpoints-eventos.md) | contrato do Diario — os eventos de um dia de um ambiente | ativo |
| [`endpoints-faturas.md`](04-api/endpoints-faturas.md) | contrato dos endpoints de fatura de cartao | ativo |
| [`endpoints-lancamentos.md`](04-api/endpoints-lancamentos.md) | contrato dos endpoints de lancamento, extrato, transferencia e estorno | ativo |
| [`endpoints-meios-pagamento.md`](04-api/endpoints-meios-pagamento.md) | contrato dos endpoints de meio de pagamento | ativo |
| [`endpoints-relatorios.md`](04-api/endpoints-relatorios.md) | contrato dos endpoints de agregacao e relatorio | ativo |
| [`endpoints-usuario.md`](04-api/endpoints-usuario.md) | cadastro, login, logout e o proprio perfil — rota, payload e erros | ativo |
| [`erros.md`](04-api/erros.md) | o corpo de resposta de erro, o catalogo de codigos e a regra do que o erro nao conta | ativo |

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
| [`dashboard.md`](06-interface/dashboard.md) | o que a Home mostra, em que ordem e por que — o cockpit de decidir | ativo |
| [`direcao-visual.md`](06-interface/direcao-visual.md) | a linguagem visual do Cyberbank: paleta, tipografia, forma e o limite do efeito | rascunho |
| [`extrato.md`](06-interface/extrato.md) | a lista de movimento, a linha resumida e o modal que abre um lancamento inteiro | ativo |
| [`navegacao.md`](06-interface/navegacao.md) | estrutura de navegacao, onde o ambiente vive na tela e como se lanca | rascunho |
| [`perfil.md`](06-interface/perfil.md) | a tela do proprio usuario: onde se entra, as secoes, o seletor de avatar e os espacos reservados | ativo |
| [`reserva.md`](06-interface/reserva.md) | a tela do patrimonio: as aplicacoes, a idade do valor informado e a distincao entre fluxo de caixa e patrimonio | ativo |

## Operacao — build, deploy e incidentes

| Documento | Dono do fato | Status |
|---|---|---|
| [`backup-restore.md`](07-operacao/backup-restore.md) | o que e salvo, com que frequencia e como restaurar | stub |
| [`build-e-run.md`](07-operacao/build-e-run.md) | pre-requisitos, comandos exatos, como subir o banco local e as variaveis de ambiente | ativo |
| [`deploy.md`](07-operacao/deploy.md) | como uma versao chega em producao | stub |
| [`runbook.md`](07-operacao/runbook.md) | sintomas conhecidos e o procedimento de resposta | stub |
| [`testes.md`](07-operacao/testes.md) | as quatro suites, o que e obrigatorio testar, e como rodar cada uma | ativo |

## Fluxos — roteiros de tarefa (entre por aqui)

| Documento | Dono do fato | Status |
|---|---|---|
| [`correcao-de-bug.md`](08-fluxos/correcao-de-bug.md) | roteiro de diagnóstico e correção de defeito | ativo |
| [`nova-integracao-externa.md`](08-fluxos/nova-integracao-externa.md) | roteiro de implementação de integração com sistema externo | ativo |
| [`nova-migration.md`](08-fluxos/nova-migration.md) | roteiro de alteração de banco de dados | ativo |
| [`nova-regra-de-dominio.md`](08-fluxos/nova-regra-de-dominio.md) | roteiro de alteração de regra ou invariante de domínio | ativo |
| [`novo-caso-de-uso.md`](08-fluxos/novo-caso-de-uso.md) | roteiro de escrever uma funcionalidade nova ponta a ponta | ativo |
| [`novo-endpoint.md`](08-fluxos/novo-endpoint.md) | roteiro de criação/alteração de endpoint REST | ativo |
| [`novo-meio-de-pagamento.md`](08-fluxos/novo-meio-de-pagamento.md) | roteiro de implementação de um novo meio de pagamento | ativo |

---

**84 documentos · 16 ainda em stub.**
Stub = conteudo inexistente: pergunte, nao deduza.
