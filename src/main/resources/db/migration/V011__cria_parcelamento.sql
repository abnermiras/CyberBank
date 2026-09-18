-- V011 — o parcelamento (docs/02-dominio/recorrencia.md).
--
-- UMA COMPRA SO, DIVIDIDA EM N — e nao N eventos independentes, que e a recorrencia. Sao duas
-- TABELAS, e nao uma "serie" com coluna `tipo`: quando TODAS as regras mudam por tipo (como
-- nascem, como se editam, o que e cancelar, se existe total), nao e um tipo — sao duas coisas.
-- A `recorrencia` e Fase 2 e nao nasce aqui.
--
-- `valor_da_compra_centavos` e GUARDADO, e e a unica coisa guardada que a fatura tambem teria
-- podido derivar. A razao esta no doc: e ele que torna VERIFICAVEL a invariante da soma — "se a
-- soma das parcelas nao bate com o valor da compra, e bug". Derivar o total das parcelas faria a
-- invariante ser verdadeira por construcao, e invariante que nao pode ser falsa nao prova nada.

CREATE TABLE parcelamento (
    id                       bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id              bigint       NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,

    -- A conta CARTAO e o cartao com que se comprou: o que as parcelas herdam ao nascer.
    conta_id                 bigint       NOT NULL,
    meio_id                  bigint       NOT NULL,
    categoria_id             bigint,

    valor_da_compra_centavos bigint       NOT NULL,
    parcelas                 smallint     NOT NULL,

    -- Vira a `dataEvento` de TODAS as parcelas (ADR-0006): a compra aconteceu uma vez. Quem
    -- espalha a cobranca pelos meses e a FATURA de cada parcela, nunca a data delas.
    data_da_compra           date         NOT NULL,
    descricao                varchar(200) NOT NULL,
    criado_em                timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT uq_parcelamento_id_ambiente UNIQUE (id, ambiente_id),

    CONSTRAINT fk_parcelamento_conta FOREIGN KEY (conta_id, ambiente_id)
        REFERENCES conta (id, ambiente_id),
    CONSTRAINT fk_parcelamento_meio FOREIGN KEY (meio_id, ambiente_id)
        REFERENCES meio (id, ambiente_id),
    CONSTRAINT fk_parcelamento_categoria FOREIGN KEY (categoria_id, ambiente_id)
        REFERENCES categoria (id, ambiente_id),

    CONSTRAINT ck_parcelamento_valor_positivo CHECK (valor_da_compra_centavos > 0),
    -- Uma parcela nao e parcelamento: e uma compra a vista.
    CONSTRAINT ck_parcelamento_parcelas CHECK (parcelas BETWEEN 2 AND 99),
    CONSTRAINT ck_parcelamento_descricao_nao_vazia CHECK (length(btrim(descricao)) > 0)
);

CREATE INDEX ix_parcelamento_ambiente ON parcelamento (ambiente_id);

-- `recorrencia_id` continua FORA, e pela terceira vez a mesma frase: coluna com REFERENCES para
-- tabela que nao existe nao e schema. Ela entra com a recorrencia.
ALTER TABLE lancamento ADD COLUMN parcelamento_id bigint;

ALTER TABLE lancamento ADD CONSTRAINT fk_lancamento_parcelamento
    FOREIGN KEY (parcelamento_id) REFERENCES parcelamento (id);

-- "Editar altera TODAS as parcelas, sempre" — e esta e a consulta que as encontra.
CREATE INDEX ix_lancamento_parcelamento ON lancamento (parcelamento_id)
    WHERE parcelamento_id IS NOT NULL;

ALTER TABLE parcelamento ENABLE ROW LEVEL SECURITY;
ALTER TABLE parcelamento FORCE  ROW LEVEL SECURITY;

-- Sem o OR do ADR-0004, como `fatura` e `evento`: o que o destino de um cartao compartilhado
-- enxerga da serie alheia e decisao do compartilhamento, e ela esta em aberto.
CREATE POLICY parcelamento_do_ambiente ON parcelamento
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());

-- A licao da V008 pela terceira vez: os dois CHECK sao lista fechada.
ALTER TABLE evento DROP CONSTRAINT ck_evento_alvo_tipo;

ALTER TABLE evento ADD CONSTRAINT ck_evento_alvo_tipo CHECK (alvo_tipo IS NULL OR alvo_tipo IN (
    'LANCAMENTO',
    'CONTA',
    'MEIO',
    'CATEGORIA',
    'FATURA',
    'SERIE'
));

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
    'SERIE_CRIADA',
    'SERIE_ALTERADA',
    'SERIE_CANCELADA'
));
