-- V006 — o evento, e o caminho pelo qual a rotina diaria enxerga os ambientes.
--
-- DUAS COISAS NUMA MIGRATION SO porque elas nascem juntas por necessidade: a rotina que
-- vira PREVISTO em REALIZADO e o primeiro produtor de evento de origem SISTEMA, e sem a
-- funcao la embaixo ela nao tem por onde comecar. Tabela sem quem a escreva, ou funcao sem
-- o que registrar, sao meia mudanca cada uma.
--
-- FORA DESTA MIGRATION, de proposito: o OR do ADR-0004 na politica. Em `conta`, `meio` e
-- `lancamento` ele foi escrito desde o primeiro dia porque a regra ja estava decidida.
-- Aqui ela NAO esta: "o que o destino de um compartilhamento ve no Diario do ambiente
-- dele" e decisao em aberto, pos-Fase 1. Escrever agora um OR que a decisao depois
-- contradiga seria pior que escrever depois — seria migration em cima de dado real E uma
-- regra errada rodando no meio. A politica nasce so com `ambiente_id`, e o OR entra com o
-- compartilhamento.

-- ---------------------------------------------------------------------------
-- evento — familia do ambiente
-- ---------------------------------------------------------------------------
CREATE TABLE evento (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,

    -- A regra 5 do CLAUDE.md, nas duas colunas lado a lado: `dia` e data de DOMINIO — dia
    -- local de Brasilia, sem hora e sem fuso, e e por ele que o Diario agrupa. `instante`
    -- e carimbo de auditoria em UTC, e e o criterio de ordem DENTRO do dia. Um nunca se
    -- deriva do outro para exibir.
    dia         date        NOT NULL,
    instante    timestamptz NOT NULL DEFAULT now(),

    origem      varchar(10) NOT NULL,
    -- Todo evento tem autor, inclusive o de origem SISTEMA: nele o autor e o dono do
    -- ambiente, a mesma regra do lancamento que o ciclo cria.
    autor_id    bigint      NOT NULL REFERENCES usuario (id),
    tipo        varchar(40) NOT NULL,

    -- O alvo e o que faz a linha do Diario virar link. Sem chave estrangeira, e de
    -- proposito: LANCAMENTO_EXCLUIDO aponta para um id que NAO EXISTE MAIS, e uma
    -- referencia o impediria de ser gravado — justamente o evento que mais importa.
    alvo_tipo   varchar(20),
    alvo_id     bigint,

    -- O punhado de valores que a frase precisa. Nao e espelho do objeto, com uma excecao
    -- escrita no docs/02-dominio/evento.md: o evento de exclusao carrega a linha inteira,
    -- porque o alvo dele nao existe para ser lido.
    dados       jsonb,

    CONSTRAINT ck_evento_origem CHECK (origem IN ('SISTEMA', 'USUARIO')),
    CONSTRAINT ck_evento_alvo CHECK (
        (alvo_tipo IS NULL AND alvo_id IS NULL)
        OR
        (alvo_tipo IS NOT NULL AND alvo_id IS NOT NULL)
    ),
    CONSTRAINT ck_evento_alvo_tipo CHECK (alvo_tipo IS NULL OR alvo_tipo IN (
        'LANCAMENTO',
        'CONTA',
        'MEIO',
        'CATEGORIA'
    )),
    CONSTRAINT ck_evento_tipo CHECK (tipo IN (
        'LANCAMENTO_REALIZADO',
        'LANCAMENTO_CRIADO',
        'LANCAMENTO_EDITADO',
        'LANCAMENTO_ESTORNADO',
        'LANCAMENTO_EXCLUIDO',
        'CONTA_CRIADA',
        'CONTA_RENOMEADA',
        'CONTA_INATIVADA',
        'CONTA_REATIVADA',
        'CONTA_EXCLUIDA',
        'MEIO_CRIADO',
        'MEIO_RENOMEADO',
        'MEIO_INATIVADO',
        'MEIO_REATIVADO',
        'MEIO_EXCLUIDO',
        'CATEGORIA_CRIADA',
        'CATEGORIA_RENOMEADA',
        'CATEGORIA_RECOLORIDA',
        'CATEGORIA_INATIVADA',
        'CATEGORIA_REATIVADA',
        'CATEGORIA_EXCLUIDA'
    ))
);

-- O CHECK acima nao lista FATURA_*, SERIE_*, OCORRENCIA_DE_RECORRENCIA, ACESSO_*,
-- VINCULO_*, VALOR_DE_APLICACAO_INFORMADO e LIMITE_INFORMADO, que o evento.md ja preve:
-- eles entram por migration junto com a fatia que os produz. Tipo que nenhum codigo grava
-- nao e schema, e um CHECK que aceita o que ninguem escreve nao protege nada.

-- "O Diario de um dia sao os eventos daquele dia, do ambiente ativo, mais novos primeiro
-- pelo instante." A tela e Fase 2; o indice que ela vai usar nasce com a tabela, porque
-- criar indice depois em tabela que so cresce e mais caro.
CREATE INDEX ix_evento_diario ON evento (ambiente_id, dia DESC, instante DESC);
-- "O historico de alteracao de um lancamento" e esta consulta.
CREATE INDEX ix_evento_alvo ON evento (ambiente_id, alvo_tipo, alvo_id)
    WHERE alvo_tipo IS NOT NULL;

ALTER TABLE evento ENABLE ROW LEVEL SECURITY;
ALTER TABLE evento FORCE  ROW LEVEL SECURITY;

-- Sem politica de UPDATE nem de DELETE, e nao e esquecimento: "evento e imutavel — nao se
-- edita e nao se exclui. Registro errado se corrige com um evento novo". A ausencia das
-- duas politicas e a invariante escrita onde ela nao depende de ninguem lembrar.
-- Apagar o ambiente apaga os eventos dele, pelo ON DELETE CASCADE, e esse e o unico
-- caminho pelo qual um evento some.
CREATE POLICY evento_do_ambiente ON evento FOR SELECT
    USING (ambiente_id = app_ambiente_id());

CREATE POLICY evento_gravado_no_ambiente ON evento FOR INSERT
    WITH CHECK (ambiente_id = app_ambiente_id());

-- ---------------------------------------------------------------------------
-- A visao da rotina — ADR-0013
-- ---------------------------------------------------------------------------
-- A rotina diaria nao tem requisicao, nao tem sessao e nao tem ambiente: ela precisa
-- passar por TODOS. Com as duas variaveis vazias, nenhuma politica devolve linha — nem a
-- lista de ambientes por onde comecar.
--
-- SECURITY DEFINER sozinho nao resolveria: `acesso` tem FORCE ROW LEVEL SECURITY, que
-- sujeita o proprio dono as politicas, e o dono e NOBYPASSRLS. Por isso sao duas coisas:
-- a funcao, que a aplicacao pode chamar, e uma politica escrita PARA O PAPEL DONO, que so
-- enxerga linha de papel DONO. O privilegio nao e uma variavel que qualquer codigo seta —
-- e um papel que so a funcao assume, numa consulta que devolve dois numeros e nada mais.
--
-- O `TO` sai de current_user porque o nome do papel vem do ambiente (docker/postgres-init),
-- e migration nao le variavel de ambiente.
DO $$
BEGIN
    EXECUTE format(
        'CREATE POLICY acesso_visivel_para_a_rotina ON acesso FOR SELECT TO %I USING (papel = ''DONO'')',
        current_user);
END
$$;

CREATE FUNCTION ambientes_para_rotina()
    RETURNS TABLE (ambiente_id bigint, dono_id bigint)
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
    SET search_path = public, pg_temp
    AS $$
        SELECT a.ambiente_id, a.usuario_id
        FROM acesso a
        WHERE a.papel = 'DONO'
        ORDER BY a.ambiente_id
    $$;
