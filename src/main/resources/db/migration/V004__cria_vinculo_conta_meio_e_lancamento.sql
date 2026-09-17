-- V004 — o vertical do dinheiro: conta, meio, lancamento. E o vinculo, que nasce vazio.
--
-- QUATRO TABELAS NUMA MIGRATION SO, e nao e economia de arquivo: as quatro se referenciam
-- em ciclo. A politica de `conta` le `vinculo` (o OR do ADR-0004), `vinculo` aponta para
-- `conta` e `meio`, e `lancamento` aponta para os dois. Separar em quatro arquivos criaria
-- justamente a janela que docs/03-dados/migrations.md manda nao criar — tabela existindo
-- sem politica. Aqui a ordem e: as tabelas primeiro, as politicas no fim, tudo na mesma
-- transacao do Flyway.
--
-- FORA DESTA MIGRATION, de proposito: `fatura`, `parcelamento` e `recorrencia`, e com elas
-- as colunas `fatura_id`, `parcelamento_id`, `recorrencia_id`, `pagamento_de_fatura` e
-- `rolagem_de_fatura` de `lancamento`. Coluna com REFERENCES para tabela que nao existe nao
-- e schema, e o cartao e fatia propria. O CHECK de `conta.tipo` ja aceita CARTAO: quem
-- recusa o tipo hoje e o caso de uso, e assim a fatia do cartao nao precisa de migration em
-- cima de dado real.

-- A chave estrangeira composta de `lancamento` para `categoria`, la embaixo, precisa do par
-- (id, ambiente_id) unico do lado de la. A V002 garante (id, ambiente_id, nivel), que serve
-- a arvore de dois niveis; o par sem o nivel e o que a referencia de ca consegue citar.
ALTER TABLE categoria ADD CONSTRAINT uq_categoria_id_ambiente UNIQUE (id, ambiente_id);

-- ---------------------------------------------------------------------------
-- conta — familia do ambiente
-- ---------------------------------------------------------------------------
CREATE TABLE conta (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    nome        varchar(80) NOT NULL,
    tipo        varchar(20) NOT NULL,

    -- Os eixos 2 e 3 do docs/02-dominio/conta.md. Sao COLUNA e nao derivacao do tipo, e o
    -- doc escreve por que: "e esse campo, e nao o tipo, que o dashboard de gasto consulta;
    -- tipo novo no futuro so precisa responder a esta pergunta". Travar o par no banco
    -- faria um tipo novo exigir migration para responder o que o dominio ja sabe.
    entra_no_fluxo_de_caixa boolean NOT NULL,
    entra_em_caixa          boolean NOT NULL,

    inativa     boolean     NOT NULL DEFAULT false,
    criada_em   timestamptz NOT NULL DEFAULT now(),

    -- O par (id, ambiente_id) e o que deixa `meio` e `vinculo` exigirem o MESMO ambiente
    -- por chave estrangeira composta, em vez de por confianca na aplicacao.
    CONSTRAINT uq_conta_id_ambiente UNIQUE (id, ambiente_id),

    CONSTRAINT ck_conta_nome_nao_vazio CHECK (length(btrim(nome)) > 0),
    CONSTRAINT ck_conta_tipo CHECK (tipo IN (
        'CORRENTE',
        'CARTEIRA',
        'APLICACAO',
        'BENEFICIO',
        'CARTAO'
    )),
    -- "entraEmCaixa = true implica entraNoFluxoDeCaixa = true. O contrario nao vale."
    -- O achado 9 do metodo: R$ 880 de vale-refeicao entravam em "em caixa" e o sistema
    -- dizia que dava para pagar um boleto com dinheiro que so compra comida.
    CONSTRAINT ck_conta_caixa_implica_fluxo CHECK (NOT entra_em_caixa OR entra_no_fluxo_de_caixa)
);

CREATE INDEX ix_conta_ambiente ON conta (ambiente_id);

-- ---------------------------------------------------------------------------
-- meio — familia do ambiente
-- ---------------------------------------------------------------------------
CREATE TABLE meio (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ambiente_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    nome        varchar(80) NOT NULL,
    tipo        varchar(20) NOT NULL,
    conta_id    bigint      NOT NULL,
    inativo     boolean     NOT NULL DEFAULT false,
    criado_em   timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT uq_meio_id_ambiente UNIQUE (id, ambiente_id),

    -- "Nao existe meio orfao", e a conta e do MESMO ambiente do meio: o que atravessa
    -- ambiente e o USO, pelo vinculo, nunca a posse (ADR-0004).
    CONSTRAINT fk_meio_conta FOREIGN KEY (conta_id, ambiente_id)
        REFERENCES conta (id, ambiente_id),

    CONSTRAINT ck_meio_nome_nao_vazio CHECK (length(btrim(nome)) > 0),
    CONSTRAINT ck_meio_tipo CHECK (tipo IN (
        'DEBITO',
        'CREDITO',
        'PIX',
        'DINHEIRO',
        'BENEFICIO',
        'BOLETO'
    ))
);

CREATE INDEX ix_meio_ambiente ON meio (ambiente_id);
CREATE INDEX ix_meio_conta    ON meio (conta_id);

-- ---------------------------------------------------------------------------
-- vinculo — familia de ligacao (ADR-0004). NASCE VAZIA
-- ---------------------------------------------------------------------------
-- Ela existe agora porque a politica de `conta` e a de `meio` precisam do OR desde a
-- primeira migration: "escrever o OR depois e migration em cima de dado real; escrever
-- agora custa uma linha" (docs/03-dados/modelo-de-dados.md). Nenhum caso de uso escreve
-- aqui nesta fatia — a FUNCIONALIDADE do compartilhamento e liberada com a Fase 1
-- concluida, e so o MODELO entra agora.
--
-- `ambiente_origem_id` nao e denormalizacao que pode divergir: as chaves estrangeiras
-- compostas la embaixo exigem que ele seja o ambiente do proprio objeto.
CREATE TABLE vinculo (
    id                  bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    objeto              varchar(10) NOT NULL,
    conta_id            bigint,
    meio_id             bigint,
    ambiente_origem_id  bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    ambiente_destino_id bigint      NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    criado_por          bigint      NOT NULL REFERENCES usuario (id),
    criado_em           timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_vinculo_conta FOREIGN KEY (conta_id, ambiente_origem_id)
        REFERENCES conta (id, ambiente_id) ON DELETE CASCADE,
    CONSTRAINT fk_vinculo_meio FOREIGN KEY (meio_id, ambiente_origem_id)
        REFERENCES meio (id, ambiente_id) ON DELETE CASCADE,

    -- "O que se compartilha e conta e um cartao." Um vinculo empresta UM objeto, e o
    -- campo `objeto` e a coluna preenchida dizem a mesma coisa ou a linha nao entra.
    CONSTRAINT ck_vinculo_objeto CHECK (
        (objeto = 'CONTA' AND conta_id IS NOT NULL AND meio_id IS NULL)
        OR
        (objeto = 'MEIO'  AND meio_id  IS NOT NULL AND conta_id IS NULL)
    ),
    -- Emprestar para si mesmo nao e compartilhar: o ambiente ja e dono.
    CONSTRAINT ck_vinculo_ambientes_diferentes CHECK (ambiente_origem_id <> ambiente_destino_id)
);

-- "Um objeto tem no maximo um vinculo por ambiente de destino."
CREATE UNIQUE INDEX uq_vinculo_conta_destino
    ON vinculo (conta_id, ambiente_destino_id) WHERE conta_id IS NOT NULL;
CREATE UNIQUE INDEX uq_vinculo_meio_destino
    ON vinculo (meio_id, ambiente_destino_id) WHERE meio_id IS NOT NULL;

-- "Este objeto foi emprestado ao ambiente ativo?" e a subconsulta do OR, e ela roda em
-- toda leitura de conta, meio e lancamento.
CREATE INDEX ix_vinculo_destino ON vinculo (ambiente_destino_id);

-- ---------------------------------------------------------------------------
-- lancamento — familia do ambiente
-- ---------------------------------------------------------------------------
-- UMA TABELA SO, sem heranca e sem coluna `tipo`. E consequencia de "saldo e a soma dos
-- lancamentos": tabela por tipo faria toda consulta de saldo unir dez tabelas, e a
-- primeira esquecida daria saldo errado calado.
CREATE SEQUENCE transferencia_id_seq AS bigint;

CREATE TABLE lancamento (
    id             bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- O ambiente e o de QUEM LANCOU, nunca o da conta (ADR-0004, regra 3). Por isso nao ha
    -- chave composta com `conta_id` aqui: a conta pode ser de outro ambiente, emprestada
    -- pelo vinculo. Com `categoria_id` ha, logo abaixo, porque ali a regra e a oposta.
    ambiente_id    bigint       NOT NULL REFERENCES ambiente (id) ON DELETE CASCADE,
    conta_id       bigint       NOT NULL REFERENCES conta (id),
    meio_id        bigint       REFERENCES meio (id),
    categoria_id   bigint,
    autor_id       bigint       NOT NULL REFERENCES usuario (id),

    sentido        varchar(10)  NOT NULL,
    -- Dinheiro e inteiro em centavos, e o valor e SEMPRE POSITIVO: o sinal vem do sentido.
    -- Valor com sinal transforma todo relatorio em SUM(CASE WHEN ...) e faz um sinal
    -- trocado passar despercebido.
    valor_centavos bigint       NOT NULL,

    -- Data de DOMINIO: dia local, sem hora e sem fuso, por isso `date` e nao `timestamptz`.
    -- A compra das 22h de 25 de fevereiro e do dia 25 — e nao do dia 26, que e onde ela
    -- cairia convertida para UTC, junto com a fatura errada.
    data_evento    date         NOT NULL,
    data_efeito    date         NOT NULL,

    descricao      varchar(200) NOT NULL,
    situacao       varchar(20)  NOT NULL,

    transferencia_id bigint,
    estorno_de_id    bigint     REFERENCES lancamento (id),

    -- A fronteira do excluir: "o que o usuario lancou, o usuario exclui; o que o ciclo
    -- criou nao e dele para excluir". Hoje marca o lancamento de abertura; amanha marca o
    -- par de rolagem e a ocorrencia da recorrencia.
    do_ciclo       boolean      NOT NULL DEFAULT false,

    estabelecimento varchar(200),
    criado_em      timestamptz  NOT NULL DEFAULT now(),

    -- "A categoria e sempre do mesmo ambiente do lancamento", sem excecao — a de fora
    -- aparece mascarada, e mascarar e o oposto de apontar para ela.
    CONSTRAINT fk_lancamento_categoria FOREIGN KEY (categoria_id, ambiente_id)
        REFERENCES categoria (id, ambiente_id),

    CONSTRAINT ck_lancamento_sentido CHECK (sentido IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT ck_lancamento_valor_positivo CHECK (valor_centavos > 0),
    CONSTRAINT ck_lancamento_descricao_nao_vazia CHECK (length(btrim(descricao)) > 0),
    CONSTRAINT ck_lancamento_situacao CHECK (situacao IN ('PREVISTO', 'PROVISIONADO', 'REALIZADO')),
    CONSTRAINT ck_lancamento_efeito_nao_antecede_evento CHECK (data_efeito >= data_evento)
);

-- O indice que docs/03-dados/modelo-de-dados.md exige pelo nome: saldo e soma dos
-- lancamentos, e o cursor do Extrato ordena por (dataEvento, id).
CREATE INDEX ix_lancamento_extrato ON lancamento (ambiente_id, conta_id, data_evento, id);
-- "A fila de pendencias e exatamente essa consulta."
CREATE INDEX ix_lancamento_pendencia ON lancamento (ambiente_id) WHERE categoria_id IS NULL;
-- Saldo de uma conta ate uma data, que e a leitura de toda tela.
CREATE INDEX ix_lancamento_conta_efeito ON lancamento (conta_id, data_efeito);
-- "transferenciaId, quando existe, aparece em exatamente dois lancamentos" — e editar ou
-- apagar um lado age no par inteiro.
CREATE INDEX ix_lancamento_transferencia ON lancamento (transferencia_id)
    WHERE transferencia_id IS NOT NULL;
-- "Lancamento com estorno apontando para ele nao se exclui enquanto o estorno existir."
CREATE INDEX ix_lancamento_estorno_de ON lancamento (estorno_de_id)
    WHERE estorno_de_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
ALTER TABLE conta      ENABLE ROW LEVEL SECURITY;
ALTER TABLE conta      FORCE  ROW LEVEL SECURITY;
ALTER TABLE meio       ENABLE ROW LEVEL SECURITY;
ALTER TABLE meio       FORCE  ROW LEVEL SECURITY;
ALTER TABLE lancamento ENABLE ROW LEVEL SECURITY;
ALTER TABLE lancamento FORCE  ROW LEVEL SECURITY;
ALTER TABLE vinculo    ENABLE ROW LEVEL SECURITY;
ALTER TABLE vinculo    FORCE  ROW LEVEL SECURITY;

-- O vinculo e tabela DE LIGACAO: ela pergunta "qual usuario", como `acesso`, e nao "qual
-- ambiente". Le `acesso` e nada mais — ler `conta` aqui recursionaria, porque a politica de
-- `conta` le `vinculo`. E por isso que `ambiente_origem_id` e coluna.
CREATE POLICY vinculo_das_duas_pontas ON vinculo FOR SELECT
    USING (
        ambiente_origem_id  IN (SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id())
        OR
        ambiente_destino_id IN (SELECT a.ambiente_id FROM acesso a WHERE a.usuario_id = app_usuario_id())
    );

-- Sem politica de INSERT, UPDATE ou DELETE em `vinculo`, e nao e esquecimento: criar e
-- revogar vinculo e a FUNCIONALIDADE do compartilhamento, liberada com a Fase 1 concluida.
-- Ate la a tabela nasce vazia e nada escreve nela. A politica de escrita entra com o caso
-- de uso, junto com a regra de quem pode compartilhar.

-- O OR do ADR-0004, aqui e nas outras duas: a linha e visivel pelo ambiente_id OU porque um
-- vinculo emprestou aquele objeto ao ambiente ativo. O WITH CHECK NAO leva o OR, e e a
-- metade que importa: "o que atravessa e o uso, nunca a posse" — o destino usa a conta e
-- nunca a altera, e lancamento nenhum nasce fora do ambiente de quem lancou.
CREATE POLICY conta_do_ambiente ON conta
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.conta_id = conta.id AND v.ambiente_destino_id = app_ambiente_id()
        )
    )
    WITH CHECK (ambiente_id = app_ambiente_id());

CREATE POLICY meio_do_ambiente ON meio
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.meio_id = meio.id AND v.ambiente_destino_id = app_ambiente_id()
        )
    )
    WITH CHECK (ambiente_id = app_ambiente_id());

-- No lancamento o OR passa pelo OBJETO, nao pelo lancamento: o destino de um
-- compartilhamento enxerga o movimento da conta emprestada, inclusive o que a origem
-- lancou — e a categoria dele e mascarada na leitura, que e regra de aplicacao.
CREATE POLICY lancamento_do_ambiente ON lancamento
    USING (
        ambiente_id = app_ambiente_id()
        OR EXISTS (
            SELECT 1 FROM vinculo v
            WHERE v.ambiente_destino_id = app_ambiente_id()
              AND (v.conta_id = lancamento.conta_id OR v.meio_id = lancamento.meio_id)
        )
    )
    WITH CHECK (ambiente_id = app_ambiente_id());
