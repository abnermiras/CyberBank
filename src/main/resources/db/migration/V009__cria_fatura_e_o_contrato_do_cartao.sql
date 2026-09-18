-- V009 — o contrato de cartao e a fatura (docs/02-dominio/fatura-cartao.md).
--
-- QUATRO COISAS NUMA MIGRATION SO, pelo motivo da V004: elas se referenciam. `fatura` aponta
-- para `conta`, `lancamento` aponta para `fatura`, e a conta so vira contrato de cartao com
-- as colunas de ciclo que a fatura le. Separar criaria a janela que docs/03-dados/
-- migrations.md manda nao criar — tabela existindo sem politica, ou coluna apontando para
-- tabela que ainda nao nasceu.
--
-- A V004 deixou isto escrito e cumpriu a promessa: "o CHECK de `conta.tipo` ja aceita CARTAO;
-- quem recusa o tipo hoje e o caso de uso, e assim a fatia do cartao nao precisa de migration
-- em cima de dado real". Nenhum ALTER aqui mexe em linha existente.
--
-- FORA DESTA MIGRATION, de proposito: `parcelamento_id` e `recorrencia_id` de `lancamento`.
-- O parcelamento vem na V010, com a tabela dele; a recorrencia e Fase 2. Coluna com
-- REFERENCES para tabela que nao existe nao e schema — e a mesma frase da V004.

-- ---------------------------------------------------------------------------
-- conta — o que e do CONTRATO, e nao do cartao
-- ---------------------------------------------------------------------------
-- "Limite e ciclo sao da conta CARTAO, nunca do cartao" (docs/02-dominio/meio-de-pagamento.md).
-- Varios meios CREDITO — fisico, virtual, adicional — apontam para a mesma conta e dividem
-- tudo isto.
ALTER TABLE conta
    ADD COLUMN limite_centavos          bigint,
    ADD COLUMN limite_informado_em      date,
    ADD COLUMN dia_vencimento           smallint,
    ADD COLUMN dias_antes_fechamento    smallint,
    ADD COLUMN conta_pagadora_padrao_id bigint;

-- A conta pagadora e do MESMO ambiente: ela e so o que vem preenchido no formulario de
-- pagamento, e nada nasce dela sozinho (docs/02-dominio/fatura-cartao.md).
ALTER TABLE conta ADD CONSTRAINT fk_conta_pagadora_padrao
    FOREIGN KEY (conta_pagadora_padrao_id, ambiente_id) REFERENCES conta (id, ambiente_id);

-- O ciclo e obrigatorio na CARTAO e proibido fora dela: e ele que da as duas datas de toda
-- fatura, e conta sem ciclo nao teria como abrir a primeira.
ALTER TABLE conta ADD CONSTRAINT ck_conta_ciclo_pertence_ao_cartao CHECK (
    (tipo = 'CARTAO') = (dia_vencimento IS NOT NULL AND dias_antes_fechamento IS NOT NULL)
);

-- 31 em fevereiro cai no ultimo dia do mes, e quem resolve isso e o dominio. O CHECK so
-- recusa o que nao descreve dia nenhum.
ALTER TABLE conta ADD CONSTRAINT ck_conta_dia_vencimento
    CHECK (dia_vencimento IS NULL OR dia_vencimento BETWEEN 1 AND 31);

-- Um dia no minimo, senao fechamento e vencimento seriam o mesmo dia e a fatura fecharia
-- depois de vencer. Vinte e oito no maximo, porque mais que isso jogaria o fechamento para
-- antes do fechamento anterior.
ALTER TABLE conta ADD CONSTRAINT ck_conta_dias_antes_fechamento
    CHECK (dias_antes_fechamento IS NULL OR dias_antes_fechamento BETWEEN 1 AND 28);

ALTER TABLE conta ADD CONSTRAINT ck_conta_limite_so_no_cartao
    CHECK (limite_centavos IS NULL OR (tipo = 'CARTAO' AND limite_centavos > 0));

ALTER TABLE conta ADD CONSTRAINT ck_conta_pagadora_so_no_cartao
    CHECK (conta_pagadora_padrao_id IS NULL OR tipo = 'CARTAO');

-- A regra 7 do CLAUDE.md no schema: valor informado pelo usuario CARREGA A DATA em que foi
-- informado. Limite sem data seria um numero sem idade, e a tela nao teria o que envelhecer.
ALTER TABLE conta ADD CONSTRAINT ck_conta_limite_carrega_a_data
    CHECK ((limite_centavos IS NULL) = (limite_informado_em IS NULL));

-- ---------------------------------------------------------------------------
-- fatura — familia do ambiente
-- ---------------------------------------------------------------------------
-- "Fatura tem duas coisas que nao da para derivar: em que ponto do ciclo ela esta e as datas
-- desse ciclo. Isso e estado, e estado se guarda." O VALOR nao e uma delas, e por isso nao ha
-- coluna de total, de pago nem de a pagar: os tres sao soma de lancamento, calculada sempre.
CREATE TABLE fatura (
    id             bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id    bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    conta_id       bigint      NOT NULL,

    -- O eixo de ordem do ciclo, e o que torna "a parcela k na k-esima fatura a partir desta"
    -- uma conta e nao uma busca. Guardado como o primeiro dia do mes da fatura.
    competencia    date        NOT NULL,

    -- Calculadas no nascimento e NUNCA recalculadas: "usuario muda o ciclo -> vale da proxima
    -- fatura a nascer; fatura ja criada mantem suas datas".
    data_fechamento date       NOT NULL,
    data_vencimento date       NOT NULL,

    status         varchar(10) NOT NULL,
    criada_em      timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT uq_fatura_id_ambiente UNIQUE (id, ambiente_id),

    -- A fatura e do mesmo ambiente da conta CARTAO. O lancamento e que pode ser de outro
    -- (o ambiente dele e o de quem lancou), e por isso la a chave nao e composta.
    CONSTRAINT fk_fatura_conta FOREIGN KEY (conta_id, ambiente_id)
        REFERENCES conta (id, ambiente_id) ON DELETE CASCADE,

    CONSTRAINT ck_fatura_status CHECK (status IN ('FUTURA', 'ABERTA', 'FECHADA')),
    CONSTRAINT ck_fatura_competencia_e_o_primeiro_dia
        CHECK (extract(day from competencia) = 1),
    CONSTRAINT ck_fatura_fecha_antes_de_vencer CHECK (data_fechamento < data_vencimento)
);

-- "Toda fatura pertence a uma conta CARTAO" e cada mes tem uma so: duas faturas da mesma
-- competencia fariam a parcela k ter dois destinos.
CREATE UNIQUE INDEX uq_fatura_conta_competencia ON fatura (conta_id, competencia);

-- A INVARIANTE MAIS IMPORTANTE DESTE ARQUIVO: "exatamente uma ABERTA por conta CARTAO". Ela
-- e o que faz "a fatura vem do STATUS, nunca da data" devolver uma resposta so. Escrita como
-- indice porque confianca na aplicacao nao e invariante — e porque foi justamente ela que
-- derrubou a ideia de reabrir varias faturas para refazer pagamento.
CREATE UNIQUE INDEX uq_fatura_aberta_por_conta ON fatura (conta_id) WHERE status = 'ABERTA';

-- A varredura da rotina: fechar o que chegou na data_fechamento, encerrar o que venceu.
CREATE INDEX ix_fatura_ciclo ON fatura (ambiente_id, status, data_vencimento);

-- ---------------------------------------------------------------------------
-- lancamento — as tres colunas adiadas pela V004
-- ---------------------------------------------------------------------------
CREATE SEQUENCE rolagem_de_fatura_seq AS bigint;

ALTER TABLE lancamento
    -- Em qual fatura o lancamento ENTRA. Nasce pelo status e depois e campo editavel do
    -- usuario, para qualquer fatura, aberta ou nao.
    ADD COLUMN fatura_id              bigint,
    -- Qual fatura este pagamento QUITA — distinto de `fatura_id`: o pagamento nao entra na
    -- fatura e nao conta no total dela. So o lado ENTRADA do par, na conta CARTAO, carrega
    -- este campo: com os dois lados apontando, a soma dos pagamentos dobraria.
    ADD COLUMN pagamento_de_fatura_id bigint,
    -- Amarra o PAR da rolagem, como `transferencia_id` faz com a transferencia. Nao e chave
    -- estrangeira: os dois lados apontam para faturas diferentes pelo `fatura_id`.
    ADD COLUMN rolagem_de_fatura      bigint;

-- Chave simples, e nao composta com `ambiente_id`, pela mesma razao que `conta_id`: num
-- cartao compartilhado o lancamento e do ambiente de quem comprou e a fatura e do dono do
-- contrato (ADR-0004).
ALTER TABLE lancamento
    ADD CONSTRAINT fk_lancamento_fatura
        FOREIGN KEY (fatura_id) REFERENCES fatura (id),
    ADD CONSTRAINT fk_lancamento_pagamento_de_fatura
        FOREIGN KEY (pagamento_de_fatura_id) REFERENCES fatura (id),
    ADD CONSTRAINT ck_lancamento_entra_ou_quita
        CHECK (fatura_id IS NULL OR pagamento_de_fatura_id IS NULL);

-- O total da fatura e esta consulta, e ela roda em toda tela do cartao.
CREATE INDEX ix_lancamento_fatura ON lancamento (fatura_id) WHERE fatura_id IS NOT NULL;
-- O pago da fatura.
CREATE INDEX ix_lancamento_pagamento_de_fatura ON lancamento (pagamento_de_fatura_id)
    WHERE pagamento_de_fatura_id IS NOT NULL;
-- "rolagemDeFatura, quando existe, aparece em exatamente dois — na mesma conta, somando zero."
CREATE INDEX ix_lancamento_rolagem ON lancamento (rolagem_de_fatura)
    WHERE rolagem_de_fatura IS NOT NULL;

-- ---------------------------------------------------------------------------
-- evento — os tipos que esta fatia passa a gravar
-- ---------------------------------------------------------------------------
-- A licao da V008, e a V006 ja a tinha escrito no proprio arquivo: os dois CHECK sao lista
-- fechada, e tipo novo no enum sem migration quebra a gravacao em tempo de execucao, so na
-- hora em que alguem usa. Aqui entram os seis FATURA_* e o LIMITE_INFORMADO, mais o alvo
-- FATURA — sem ele a linha do Diario nao vira link para a fatura.
ALTER TABLE evento DROP CONSTRAINT ck_evento_alvo_tipo;

ALTER TABLE evento ADD CONSTRAINT ck_evento_alvo_tipo CHECK (alvo_tipo IS NULL OR alvo_tipo IN (
    'LANCAMENTO',
    'CONTA',
    'MEIO',
    'CATEGORIA',
    'FATURA'
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
    'FATURA_ABERTA_PELO_CICLO',
    'FATURA_ABERTA_PELO_USUARIO',
    'FATURA_PAGA',
    'FATURA_ROLADA',
    'FATURA_ENCERRADA'
));

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
ALTER TABLE fatura ENABLE ROW LEVEL SECURITY;
ALTER TABLE fatura FORCE  ROW LEVEL SECURITY;

-- SEM O `OR` DO ADR-0004, e nao e esquecimento — e a mesma escolha que o `evento` fez na
-- V006. "Quem fecha e abre a fatura de um cartao compartilhado" e uma decisao em aberto
-- (claude/estado-do-projeto.md), e "partes da fatura" e Fase 2. O preco esta nomeado: o
-- destino de um cartao compartilhado ve o lancamento dele e NAO ve a fatura em que ele caiu.
-- O OR entra junto com o compartilhamento, que e quando a regra dele existir.
CREATE POLICY fatura_do_ambiente ON fatura
    USING (ambiente_id = app_ambiente_id())
    WITH CHECK (ambiente_id = app_ambiente_id());
