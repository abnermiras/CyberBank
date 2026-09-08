-- V002 — categoria, e com ela o padrao de RLS das tabelas DO AMBIENTE.
--
-- Esta e a primeira tabela de dado do ambiente, e as outras oito copiam a forma daqui:
-- ambiente_id NOT NULL, ENABLE + FORCE ROW LEVEL SECURITY, politica e indices, tudo na
-- MESMA migration (docs/03-dados/migrations.md). Separar cria uma janela em que a tabela
-- existe sem politica, e janela e o que vaza.
--
-- SEM O `OR` DO VINCULO, e de proposito. O ADR-0004 diz que o `OR` e pago "em toda tabela
-- ligada a conta", e o que ele empresta e o uso de CONTA e de MEIO. Categoria nao atravessa
-- ambiente em hipotese nenhuma: `ambiente-financeiro.md` escreve "conta e meio podem ser de
-- outro ambiente... categoria, nunca", e o proprio ADR-0004 manda MASCARAR a categoria de
-- fora — mascarar e o oposto de enxergar. Um `OR` aqui daria acesso a exatamente o que a
-- decisao proibe. Ele entra nas tabelas ligadas a conta, quando elas nascerem.

CREATE TABLE categoria (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    pai_id      bigint,
    nome        varchar(80) NOT NULL,
    sentido     varchar(10) NOT NULL,

    -- Categoria de sistema: o sistema depende dela POR IDENTIDADE, e nome nao e identidade.
    -- `operacao` e como o ciclo acha a dele; NULL e categoria do usuario.
    sistema     boolean     NOT NULL DEFAULT false,
    operacao    varchar(30),

    inativa     boolean     NOT NULL DEFAULT false,
    criada_em   timestamptz NOT NULL DEFAULT now(),

    -- A arvore tem exatamente dois niveis, e quem garante isso e o banco: `nivel` e
    -- derivado de `pai_id`, `pai_nivel` e sempre 1, e a chave estrangeira composta la
    -- embaixo so aceita pai de nivel 1 — subcategoria nao tem filho, por construcao.
    nivel       smallint    GENERATED ALWAYS AS (CASE WHEN pai_id IS NULL THEN 1 ELSE 2 END) STORED,
    pai_nivel   smallint    GENERATED ALWAYS AS (1) STORED,

    CONSTRAINT uq_categoria_id_ambiente_nivel UNIQUE (id, ambiente_id, nivel),

    -- Uma constraint, duas garantias: o pai e RAIZ (nivel 1) e o pai e do MESMO AMBIENTE.
    -- "Lancamento e categoria sao sempre do mesmo ambiente" comeca por a arvore nao
    -- atravessar. MATCH SIMPLE: com `pai_id` nulo a chave nao e cobrada, que e o caso da raiz.
    CONSTRAINT fk_categoria_pai FOREIGN KEY (pai_id, ambiente_id, pai_nivel)
        REFERENCES categoria (id, ambiente_id, nivel),

    CONSTRAINT ck_categoria_nome_nao_vazio CHECK (length(btrim(nome)) > 0),
    CONSTRAINT ck_categoria_sentido CHECK (sentido IN ('ENTRADA', 'SAIDA')),

    -- As sete operacoes do `docs/02-dominio/categoria.md`. Nenhuma foi inventada: cada uma
    -- e uma operacao que o modelo ja distinguia por campo proprio ou por tipo de conta.
    CONSTRAINT ck_categoria_operacao CHECK (operacao IS NULL OR operacao IN (
        'SALDO_DE_ABERTURA',
        'TRANSFERENCIA',
        'APORTE',
        'RESGATE',
        'PAGAMENTO_DE_FATURA',
        'ROLAGEM_DE_FATURA',
        'RENDIMENTO'
    )),
    -- Uma e a outra: categoria de sistema tem operacao, categoria do usuario nao tem.
    CONSTRAINT ck_categoria_sistema_tem_operacao CHECK (sistema = (operacao IS NOT NULL)),
    -- "Categoria de sistema e sempre raiz e nunca tem subcategoria."
    CONSTRAINT ck_categoria_sistema_e_raiz CHECK (NOT sistema OR pai_id IS NULL),
    -- "Nao se renomeia, nao se move, nao se inativa e nao se exclui." O nao-inativa cabe
    -- no banco; o resto e do dominio.
    CONSTRAINT ck_categoria_sistema_nunca_inativa CHECK (NOT sistema OR NOT inativa)
);

-- Sete operacoes x dois sentidos = quatorze por ambiente, e exatamente uma de cada.
CREATE UNIQUE INDEX uq_categoria_sistema_por_ambiente
    ON categoria (ambiente_id, operacao, sentido) WHERE sistema;

-- A lista de categorias de um ambiente, que e a consulta de toda tela de lancamento.
CREATE INDEX ix_categoria_ambiente_pai ON categoria (ambiente_id, pai_id);

ALTER TABLE categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE categoria FORCE  ROW LEVEL SECURITY;

CREATE POLICY categoria_do_ambiente ON categoria
    USING      (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
