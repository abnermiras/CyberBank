-- V012 — a recorrencia (docs/02-dominio/recorrencia.md).
--
-- N EVENTOS INDEPENDENTES que se repetem por regra de tempo — e nao uma compra dividida, que e
-- o parcelamento da V011. Sao duas tabelas, e a razao esta la: quando TODAS as regras mudam por
-- tipo, nao e um tipo, sao duas coisas.
--
-- Nao ha `valor_total`, e a ausencia e a regra: perguntar "quanto custa a Netflix" so faz
-- sentido por ocorrencia. Junho a R$ 50 e julho a R$ 55 sao dois fatos verdadeiros.
--
-- `ativa` existe aqui e nao existe no parcelamento: cancelar uma recorrencia DESLIGA a regra e
-- o passado fica. Parcelamento nao se liga nem se desliga — ele acaba quando a ultima parcela
-- e paga.

CREATE TABLE recorrencia (
    id              bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id     bigint       NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,

    -- A conta CARTAO e o cartao: o que a ocorrencia herda ao nascer. Hoje so ha recorrencia no
    -- credito, porque o gatilho e o fechamento da fatura; fora do cartao o ciclo e o mes, e a
    -- rotina que o dispara nao existe.
    conta_id        bigint       NOT NULL,
    meio_id         bigint       NOT NULL,
    categoria_id    bigint,

    -- O valor da OCORRENCIA. Nao e total de nada.
    valor_centavos  bigint       NOT NULL,

    periodicidade   varchar(20)  NOT NULL,
    -- O dia do mes em que a cobranca cai. 29, 30 e 31 caem no ultimo dia dos meses curtos.
    dia             smallint     NOT NULL,
    inicio          date         NOT NULL,
    ativa           boolean      NOT NULL DEFAULT true,

    descricao       varchar(200) NOT NULL,
    criado_em       timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT uq_recorrencia_id_ambiente UNIQUE (id, ambiente_id),

    CONSTRAINT fk_recorrencia_conta FOREIGN KEY (conta_id, ambiente_id)
        REFERENCES conta (id, ambiente_id),
    CONSTRAINT fk_recorrencia_meio FOREIGN KEY (meio_id, ambiente_id)
        REFERENCES meio (id, ambiente_id),
    CONSTRAINT fk_recorrencia_categoria FOREIGN KEY (categoria_id, ambiente_id)
        REFERENCES categoria (id, ambiente_id),

    CONSTRAINT ck_recorrencia_valor_positivo CHECK (valor_centavos > 0),
    CONSTRAINT ck_recorrencia_dia CHECK (dia BETWEEN 1 AND 31),
    CONSTRAINT ck_recorrencia_descricao_nao_vazia CHECK (length(btrim(descricao)) > 0),
    -- Lista fechada, como todo vocabulario do dominio no schema. So MENSAL existe: o ciclo do
    -- cartao e mensal, e semanal cairia varias vezes na mesma fatura.
    CONSTRAINT ck_recorrencia_periodicidade CHECK (periodicidade IN ('MENSAL'))
);

CREATE INDEX ix_recorrencia_ambiente ON recorrencia (ambiente_id);

-- "O fechamento varre as recorrencias ativas daquele cartao" — e esta e a consulta que as acha.
-- Por CONTA e nao por meio: quem fecha e a fatura do contrato, e um contrato tem varios cartoes.
CREATE INDEX ix_recorrencia_ativa_da_conta ON recorrencia (conta_id) WHERE ativa;

ALTER TABLE lancamento ADD COLUMN recorrencia_id bigint;

ALTER TABLE lancamento ADD CONSTRAINT fk_lancamento_recorrencia
    FOREIGN KEY (recorrencia_id) REFERENCES recorrencia (id);

-- A invariante do doc virando schema: um lancamento tem parcelamento OU recorrencia, nunca os
-- dois. Uma parcela e pedaco de uma compra; uma ocorrencia e um evento inteiro.
ALTER TABLE lancamento ADD CONSTRAINT ck_lancamento_uma_serie_so
    CHECK (parcelamento_id IS NULL OR recorrencia_id IS NULL);

-- "O fechamento nunca lanca a mesma recorrencia duas vezes na mesma fatura": o indice unico e
-- quem garante isso quando duas rodadas da rotina correm juntas. A condicao exclui o que nao e
-- ocorrencia, para nao travar o resto do extrato.
CREATE UNIQUE INDEX uq_lancamento_recorrencia_por_fatura
    ON lancamento (recorrencia_id, fatura_id)
    WHERE recorrencia_id IS NOT NULL AND fatura_id IS NOT NULL;

ALTER TABLE recorrencia ENABLE ROW LEVEL SECURITY;
ALTER TABLE recorrencia FORCE  ROW LEVEL SECURITY;

-- Sem o OR do ADR-0004, igual a `parcelamento` da V011 e pela mesma razao: o que o destino de
-- um cartao compartilhado enxerga da serie alheia e decisao do compartilhamento, e ela esta em
-- aberto.
CREATE POLICY recorrencia_do_ambiente ON recorrencia
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
