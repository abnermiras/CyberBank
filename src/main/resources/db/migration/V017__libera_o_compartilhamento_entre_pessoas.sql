DROP POLICY vinculo_criado_pelo_dono_da_origem ON vinculo;

CREATE POLICY vinculo_criado_pelo_dono_da_origem ON vinculo FOR INSERT
    WITH CHECK (
        criado_por = app_usuario_id()
        AND ambiente_origem_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO')
        AND ambiente_destino_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel IN ('DONO', 'EDITOR'))
    );

DROP POLICY vinculo_revogado_pelo_dono_da_origem ON vinculo;

CREATE POLICY vinculo_revogado_por_uma_das_pontas ON vinculo FOR DELETE
    USING (
        ambiente_origem_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO')
        OR ambiente_destino_id IN (
            SELECT a.ambiente_id FROM acesso a
            WHERE a.usuario_id = app_usuario_id() AND a.papel = 'DONO')
    );

CREATE POLICY ambiente_visivel_a_quem_recebe_conta_dele ON ambiente FOR SELECT
    USING (id IN (
        SELECT v.ambiente_origem_id FROM vinculo v
        WHERE v.ambiente_destino_id IN (
            SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id())
    ));
