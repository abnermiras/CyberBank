DROP POLICY conta_do_ambiente ON conta;

CREATE POLICY conta_visivel ON conta FOR SELECT
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.conta_id = conta.id AND v.ambiente_destino_id = app_ambiente_id()
        )
    );
CREATE POLICY conta_inserida_no_ambiente ON conta FOR INSERT
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY conta_alterada_no_ambiente ON conta FOR UPDATE
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY conta_excluida_no_ambiente ON conta FOR DELETE
    USING (ambiente_id = app_ambiente_id());

DROP POLICY meio_do_ambiente ON meio;

CREATE POLICY meio_visivel ON meio FOR SELECT
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.ambiente_destino_id = app_ambiente_id()
              AND (v.meio_id = meio.id OR v.conta_id = meio.conta_id)
        )
    );
CREATE POLICY meio_inserido_no_ambiente ON meio FOR INSERT
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY meio_alterado_no_ambiente ON meio FOR UPDATE
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY meio_excluido_no_ambiente ON meio FOR DELETE
    USING (ambiente_id = app_ambiente_id());

DROP POLICY lancamento_do_ambiente ON lancamento;

CREATE POLICY lancamento_visivel ON lancamento FOR SELECT
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM conta c
            WHERE c.id = lancamento.conta_id AND c.ambiente_id = app_ambiente_id()
        )
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.ambiente_destino_id = app_ambiente_id()
              AND (v.conta_id = lancamento.conta_id OR v.meio_id = lancamento.meio_id)
        )
    );
CREATE POLICY lancamento_inserido_no_ambiente ON lancamento FOR INSERT
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY lancamento_alterado_no_ambiente ON lancamento FOR UPDATE
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
CREATE POLICY lancamento_excluido_no_ambiente ON lancamento FOR DELETE
    USING (ambiente_id = app_ambiente_id());

CREATE POLICY vinculo_criado_pelo_dono_da_origem ON vinculo FOR INSERT
    WITH CHECK (
        criado_por = app_usuario_id()
        AND ambiente_origem_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO')
        AND ambiente_destino_id IN (
            SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id())
    );
CREATE POLICY vinculo_revogado_pelo_dono_da_origem ON vinculo FOR DELETE
    USING (
        ambiente_origem_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO')
    );

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
    'VINCULO_REVOGADO'
));
