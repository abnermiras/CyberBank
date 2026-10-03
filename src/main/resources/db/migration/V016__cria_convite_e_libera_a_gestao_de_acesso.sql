CREATE TABLE convite (
    id            bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id   bigint       NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    email         varchar(255) NOT NULL,
    papel         varchar(20)  NOT NULL,
    situacao      varchar(20)  NOT NULL,
    convidado_por bigint       NOT NULL REFERENCES usuario (id),
    criado_em     timestamptz  NOT NULL DEFAULT now(),
    respondido_em timestamptz,

    CONSTRAINT ck_convite_email_minusculo CHECK (email = lower(email)),
    CONSTRAINT ck_convite_papel CHECK (papel IN ('EDITOR', 'LEITOR')),
    CONSTRAINT ck_convite_situacao
        CHECK (situacao IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'CANCELADO')),
    CONSTRAINT ck_convite_respondido CHECK ((situacao = 'PENDENTE') = (respondido_em IS NULL))
);

CREATE UNIQUE INDEX uq_convite_pendente ON convite (ambiente_id, email)
    WHERE situacao = 'PENDENTE';
CREATE INDEX ix_convite_email_pendente ON convite (email)
    WHERE situacao = 'PENDENTE';

CREATE FUNCTION app_usuario_email() RETURNS varchar
    LANGUAGE sql STABLE
    AS $$ SELECT u.email FROM usuario u WHERE u.id = app_usuario_id() $$;

ALTER TABLE convite ENABLE ROW LEVEL SECURITY;
ALTER TABLE convite FORCE  ROW LEVEL SECURITY;

CREATE POLICY convite_visivel ON convite FOR SELECT
    USING (
        ambiente_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO'
        )
        OR email = app_usuario_email()
    );

CREATE POLICY convite_criado_pelo_dono ON convite FOR INSERT
    WITH CHECK (
        convidado_por = app_usuario_id()
        AND situacao = 'PENDENTE'
        AND ambiente_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO'
        )
    );

CREATE POLICY convite_respondido ON convite FOR UPDATE
    USING (
        situacao = 'PENDENTE'
        AND (
            ambiente_id IN (
                SELECT a.ambiente_id FROM acesso a
                WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO'
            )
            OR email = app_usuario_email()
        )
    )
    WITH CHECK (
        (
            situacao = 'CANCELADO'
            AND ambiente_id IN (
                SELECT a.ambiente_id FROM acesso a
                WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO'
            )
        )
        OR (situacao IN ('ACEITO', 'RECUSADO') AND email = app_usuario_email())
    );

CREATE POLICY ambiente_visivel_ao_convidado ON ambiente FOR SELECT
    USING (id IN (
        SELECT c.ambiente_id FROM convite c
        WHERE c.situacao = 'PENDENTE' AND c.email = app_usuario_email()
    ));

CREATE FUNCTION ambiente_sem_dono_criado_pelo_usuario(p_ambiente bigint) RETURNS boolean
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
    SET search_path = public, pg_temp
    AS $$
        SELECT EXISTS (
            SELECT 1 FROM ambiente am
            WHERE am.id = p_ambiente
              AND am.criado_por = app_usuario_id()
              AND NOT EXISTS (SELECT 1 FROM acesso a WHERE a.ambiente_id = p_ambiente)
        )
    $$;

CREATE FUNCTION convite_pendente_do_usuario(p_ambiente bigint, p_papel varchar)
    RETURNS boolean
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
    SET search_path = public, pg_temp
    AS $$
        SELECT EXISTS (
            SELECT 1 FROM convite c
            WHERE c.ambiente_id = p_ambiente
              AND c.papel = p_papel
              AND c.situacao = 'PENDENTE'
              AND c.email = app_usuario_email()
        )
    $$;

DROP POLICY acesso_criado_pelo_usuario ON acesso;

CREATE POLICY acesso_criado_pelo_usuario ON acesso FOR INSERT
    WITH CHECK (
        usuario_id = app_usuario_id()
        AND (
            (papel = 'DONO' AND ambiente_sem_dono_criado_pelo_usuario(ambiente_id))
            OR (papel <> 'DONO' AND convite_pendente_do_usuario(ambiente_id, papel))
        )
    );

DROP POLICY acesso_visivel_para_a_rotina ON acesso;

DO $$
BEGIN
    EXECUTE format(
        'CREATE POLICY acesso_visivel_para_as_funcoes ON acesso FOR SELECT TO %I USING (true)',
        current_user);
    EXECUTE format(
        'CREATE POLICY acesso_removido_pelas_funcoes ON acesso FOR DELETE TO %I USING (papel <> ''DONO'')',
        current_user);
END
$$;

CREATE FUNCTION membros_do_ambiente(p_ambiente bigint)
    RETURNS TABLE (id bigint, usuario_id bigint, papel varchar, criado_em timestamptz)
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
    SET search_path = public, pg_temp
    AS $$
        SELECT a.id, a.usuario_id, a.papel, a.criado_em
        FROM acesso a
        WHERE a.ambiente_id = p_ambiente
          AND EXISTS (
              SELECT 1 FROM acesso eu
              WHERE eu.ambiente_id = p_ambiente AND eu.usuario_id = app_usuario_id()
          )
        ORDER BY a.criado_em, a.id
    $$;

CREATE FUNCTION remover_acesso(p_ambiente bigint, p_usuario bigint) RETURNS integer
    LANGUAGE plpgsql
    SECURITY DEFINER
    SET search_path = public, pg_temp
    AS $$
    DECLARE
        removidos integer;
    BEGIN
        DELETE FROM acesso a
        WHERE a.ambiente_id = p_ambiente
          AND a.usuario_id = p_usuario
          AND a.papel <> 'DONO'
          AND (
              p_usuario = app_usuario_id()
              OR EXISTS (
                  SELECT 1 FROM acesso d
                  WHERE d.ambiente_id = p_ambiente
                    AND d.usuario_id = app_usuario_id()
                    AND d.papel = 'DONO'
              )
          );
        GET DIAGNOSTICS removidos = ROW_COUNT;
        RETURN removidos;
    END
    $$;

ALTER TABLE evento DROP CONSTRAINT ck_evento_tipo;

ALTER TABLE evento ADD CONSTRAINT ck_evento_tipo CHECK (tipo IN (
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
    'VALOR_DE_APLICACAO_INFORMADO',
    'LIMITE_INFORMADO',
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
    'CATEGORIA_EXCLUIDA',
    'FATURA_FECHADA',
    'FATURA_FECHADA_PELO_USUARIO',
    'FATURA_ABERTA_PELO_CICLO',
    'FATURA_ABERTA_PELO_USUARIO',
    'FATURA_PAGA',
    'FATURA_ROLADA',
    'FATURA_ENCERRADA',
    'OCORRENCIA_DE_RECORRENCIA',
    'SERIE_CRIADA',
    'SERIE_ALTERADA',
    'SERIE_CANCELADA',
    'VINCULO_CRIADO',
    'VINCULO_REVOGADO',
    'ACESSO_CONCEDIDO',
    'ACESSO_REVOGADO'
));
