-- V001 — quem entra, e quem enxerga o que.
--
-- Quatro tabelas de DUAS familias diferentes (docs/03-dados/modelo-de-dados.md), e a
-- diferenca decide a protecao de cada uma:
--
--   usuario, sessao   familia DO USUARIO. Nao tem ambiente_id, e NAO leva politica de
--                     RLS: o login precisa achar o usuario pelo e-mail antes de existir
--                     sessao, e a sessao precisa ser achada pelo cookie antes de se saber
--                     quem e. A protecao delas e a sessao — a consulta e sempre pelo
--                     usuario autenticado.
--   ambiente, acesso  familia DE LIGACAO. Sao as tabelas que DEFINEM quem ve o que, e por
--                     isso a politica delas e por acesso do usuario, nunca por ambiente_id.
--
-- Nenhuma das quatro e tabela do ambiente: dado financeiro so aparece na V002 em diante.

-- ---------------------------------------------------------------------------
-- O contexto da requisicao, lido pelas politicas
-- ---------------------------------------------------------------------------
-- O filtro autenticado poe as duas variaveis na transacao com SET LOCAL
-- (docs/01-arquitetura/seguranca.md). A forma com o segundo argumento `true` devolve NULL
-- quando a variavel nao foi posta, em vez de estourar: sem contexto, a politica compara
-- com NULL e NAO ENXERGA NADA — que e o lado certo para errar.

CREATE FUNCTION app_usuario_id() RETURNS bigint
    LANGUAGE sql STABLE
    AS $$ SELECT NULLIF(current_setting('app.usuario_id', true), '')::bigint $$;

CREATE FUNCTION app_ambiente_id() RETURNS bigint
    LANGUAGE sql STABLE
    AS $$ SELECT NULLIF(current_setting('app.ambiente_id', true), '')::bigint $$;

-- ---------------------------------------------------------------------------
-- usuario — familia do usuario
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id          bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email       varchar(255) NOT NULL,
    nome        varchar(120) NOT NULL,
    senha_hash  varchar(255) NOT NULL,
    criado_em   timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT uq_usuario_email UNIQUE (email),
    -- O e-mail e o identificador de login, e "unico no sistema inteiro" so vale se
    -- Fulano@x.com e fulano@x.com forem a mesma linha. A normalizacao e da aplicacao; o
    -- CHECK e o que garante que ela aconteceu.
    CONSTRAINT ck_usuario_email_minusculo CHECK (email = lower(email)),
    CONSTRAINT ck_usuario_nome_nao_vazio  CHECK (length(btrim(nome)) > 0)
);

-- ---------------------------------------------------------------------------
-- sessao — familia do usuario (ADR-0009)
-- ---------------------------------------------------------------------------
CREATE TABLE sessao (
    id                 bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id         bigint      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    -- O cookie carrega o identificador opaco; aqui fica o HASH dele. Copia do banco em
    -- backup, dump ou suporte nao vira sessao viva de ninguem.
    identificador_hash varchar(64) NOT NULL,
    criado_em          timestamptz NOT NULL DEFAULT now(),
    ultimo_uso_em      timestamptz NOT NULL DEFAULT now(),
    -- Expiracao ABSOLUTA. A por inatividade sai de ultimo_uso_em, e as duas valem.
    expira_em          timestamptz NOT NULL,
    -- Por onde a pessoa entrou. Inspecionar sessao e metade do valor do ADR-0009.
    origem             varchar(200),

    CONSTRAINT uq_sessao_identificador UNIQUE (identificador_hash)
);

-- Revogar e apagar a linha, e "todas as sessoes deste usuario" e a consulta da troca de senha.
CREATE INDEX ix_sessao_usuario ON sessao (usuario_id);
-- A limpeza das expiradas (docs/03-dados/modelo-de-dados.md).
CREATE INDEX ix_sessao_expira_em ON sessao (expira_em);

-- ---------------------------------------------------------------------------
-- ambiente — familia de ligacao
-- ---------------------------------------------------------------------------
CREATE TABLE ambiente (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        varchar(80) NOT NULL,
    -- Quem criou, para auditoria — e nao e o dono: a propriedade se transfere, isto nao
    -- muda. A politica de SELECT abaixo depende dele.
    criado_por  bigint      NOT NULL REFERENCES usuario (id),
    criado_em   timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT ck_ambiente_nome_nao_vazio CHECK (length(btrim(nome)) > 0)
);

-- ---------------------------------------------------------------------------
-- acesso — familia de ligacao. O par (usuario, ambiente) mais o papel
-- ---------------------------------------------------------------------------
CREATE TABLE acesso (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  bigint      NOT NULL REFERENCES usuario (id)  ON DELETE CASCADE,
    ambiente_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    papel       varchar(20) NOT NULL,
    criado_em   timestamptz NOT NULL DEFAULT now(),

    -- Enum e varchar com CHECK, nunca o tipo nativo (docs/03-dados/modelo-de-dados.md).
    CONSTRAINT ck_acesso_papel CHECK (papel IN ('DONO', 'EDITOR', 'LEITOR')),
    -- "Um usuario tem no maximo um acesso por ambiente — um papel, nao varios."
    CONSTRAINT uq_acesso_usuario_ambiente UNIQUE (usuario_id, ambiente_id)
);

-- "Todo ambiente tem exatamente um dono." O banco garante o NO MAXIMO UM; o PELO MENOS UM
-- e do dominio, porque nao existe forma declarativa de exigir linha que ainda nao nasceu.
CREATE UNIQUE INDEX uq_acesso_um_dono_por_ambiente ON acesso (ambiente_id) WHERE papel = 'DONO';
-- "Quem tem acesso a este ambiente?" — e a subconsulta que a politica de ambiente faz.
CREATE INDEX ix_acesso_ambiente ON acesso (ambiente_id);

-- ---------------------------------------------------------------------------
-- RLS das tabelas de ligacao
-- ---------------------------------------------------------------------------
-- ENABLE + FORCE nas duas: a aplicacao ja conecta com papel nao-dono, e o FORCE fecha o
-- caminho do dono (docs/03-dados/modelo-de-dados.md, "a armadilha que faz RLS nao valer nada").

ALTER TABLE ambiente ENABLE ROW LEVEL SECURITY;
ALTER TABLE ambiente FORCE  ROW LEVEL SECURITY;
ALTER TABLE acesso   ENABLE ROW LEVEL SECURITY;
ALTER TABLE acesso   FORCE  ROW LEVEL SECURITY;

-- Ver um ambiente e ter acesso a ele. A segunda metade da condicao existe por um motivo
-- mecanico: `INSERT ... RETURNING` aplica a politica de SELECT a linha recem-inserida, e
-- no instante em que o ambiente nasce o acesso dele AINDA NAO EXISTE — a chave estrangeira
-- exige essa ordem. A janela e exatamente essa: ambiente ORFAO, e so para quem o criou.
-- Ela se fecha sozinha no comando seguinte da mesma transacao, quando o acesso entra; e
-- ambiente orfao fora de transacao viola a invariante "todo ambiente tem exatamente um dono".
CREATE POLICY ambiente_visivel_por_acesso ON ambiente FOR SELECT
    USING (
        id IN (SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id())
        OR (
            criado_por = app_usuario_id()
            AND NOT EXISTS (SELECT 1 FROM acesso a WHERE a.ambiente_id = ambiente.id)
        )
    );

CREATE POLICY ambiente_criado_pelo_usuario ON ambiente FOR INSERT
    WITH CHECK (criado_por = app_usuario_id());

-- Renomear e do dono e do editor; a leitura da linha ja e barrada pela politica de SELECT.
CREATE POLICY ambiente_alterado_por_acesso ON ambiente FOR UPDATE
    USING      (id IN (SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id()))
    WITH CHECK (id IN (SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id()));

CREATE POLICY ambiente_excluido_pelo_dono ON ambiente FOR DELETE
    USING (id IN (
        SELECT a.ambiente_id FROM acesso a
        WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO'
    ));

-- O usuario enxerga os PROPRIOS acessos, e cria acesso so para si mesmo — que e o que o
-- cadastro faz. Convidar alguem (acesso de OUTRO usuario, criado pelo dono) e trocar papel
-- pedem politica nova: a condicao "sou dono deste ambiente" le a propria tabela acesso, e
-- politica que le a tabela que ela protege recursiona. A saida sera uma funcao
-- SECURITY DEFINER, e ela entra com o convite — que nao e desta fatia.
CREATE POLICY acesso_do_usuario ON acesso FOR SELECT
    USING (usuario_id = app_usuario_id());

CREATE POLICY acesso_criado_pelo_usuario ON acesso FOR INSERT
    WITH CHECK (usuario_id = app_usuario_id());

-- Sair de um ambiente e apagar o proprio acesso. Sem politica de UPDATE de proposito:
-- papel se troca com o convite, e ate la ninguem troca papel nenhum.
CREATE POLICY acesso_removido_pelo_usuario ON acesso FOR DELETE
    USING (usuario_id = app_usuario_id());
