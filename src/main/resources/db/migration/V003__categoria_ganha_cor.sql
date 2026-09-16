-- V003 — a cor de identidade da categoria raiz (docs/02-dominio/categoria.md).
--
-- A coluna guarda o NOME DO TOM, nao o hexadecimal. O valor de `VIOLETA` e decisao de
-- docs/06-interface/direcao-visual.md, e trocar um tom passa a ser uma linha de CSS em vez
-- de uma migration sobre dado de usuario. E o mesmo tratamento que `sentido` e `operacao`
-- ja recebem em V002.

ALTER TABLE categoria ADD COLUMN cor varchar(20);

-- Os oito tons da paleta de identidade. A lista mora aqui pelo mesmo motivo que a de
-- `operacao`: o banco e a ultima linha de defesa e nao depende de nenhuma aplicacao estar
-- correta. Tom novo exige migration, e e de proposito — a paleta nao cresce por pedido.
ALTER TABLE categoria ADD CONSTRAINT ck_categoria_cor CHECK (cor IS NULL OR cor IN (
    'VIOLETA',
    'AZUL',
    'TEAL',
    'OLIVA',
    'OCRE',
    'TERRACOTA',
    'ARDOSIA',
    'MALVA'
));

-- A RAIZ QUE JA EXISTE PRECISA DE UM VALOR, e o banco nao tem como perguntar a ninguem.
-- ARDOSIA e o cinza-azulado da paleta — o tom que menos afirma alguma coisa. Nao e o
-- sistema corrigindo valor informado (regra 7): nao ha valor informado nenhum aqui, porque
-- o campo acabou de nascer. E trocar a cor e livre e a qualquer momento.
--
-- O `NO FORCE` e obrigatorio e nao e atalho: a politica de V002 filtra por
-- app_ambiente_id(), que numa migration e NULL — e com FORCE valendo para o DONO da tabela,
-- este UPDATE acertaria ZERO linhas em silencio e a constraint seguinte estouraria. O
-- Flyway roda cada migration numa transacao, entao a janela sem FORCE fecha junto com ela.
ALTER TABLE categoria NO FORCE ROW LEVEL SECURITY;

UPDATE categoria SET cor = 'ARDOSIA'
 WHERE NOT sistema AND pai_id IS NULL AND cor IS NULL;

ALTER TABLE categoria FORCE ROW LEVEL SECURITY;

-- "Tem cor quem e raiz DO USUARIO, e so." Uma constraint para as tres invariantes: raiz do
-- usuario tem cor, subcategoria nao tem (herda a da raiz na leitura), e categoria de
-- sistema nao tem.
ALTER TABLE categoria ADD CONSTRAINT ck_categoria_cor_so_na_raiz_do_usuario
    CHECK ((cor IS NOT NULL) = (NOT sistema AND pai_id IS NULL));
