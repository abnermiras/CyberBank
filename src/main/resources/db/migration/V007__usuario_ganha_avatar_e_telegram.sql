-- V007 — o usuario ganha uma cara e um canal.
--
-- As duas colunas nascem da mesma tela (docs/06-interface/perfil.md) e sao do USUARIO, nao do
-- ambiente: `usuario` continua sem ambiente_id e sem RLS, protegida pela sessao.

-- ---------------------------------------------------------------------------
-- avatar — enum e varchar com CHECK, nunca o tipo nativo
-- ---------------------------------------------------------------------------
-- Em TRES passos porque a tabela ja tem linha: coluna nula, sorteio para quem ja existia, e
-- so entao NOT NULL. Nao fica DEFAULT nenhum: quem sorteia o avatar de quem nasce e o dominio
-- (docs/02-dominio/usuario.md), e um DEFAULT aqui seria um segundo lugar decidindo a mesma
-- coisa, calado — e o dia em que a lista mudasse, os dois discordariam sem ninguem notar.

ALTER TABLE usuario ADD COLUMN avatar varchar(20);

UPDATE usuario
   SET avatar = (ARRAY['VISOR','OLHO','GATO','CAVEIRA','DRONE',
                       'CIRCUITO','ONDA','TORRE','PRISMA','ROBO'])[1 + floor(random() * 10)];

ALTER TABLE usuario ALTER COLUMN avatar SET NOT NULL;

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_avatar
    CHECK (avatar IN ('VISOR','OLHO','GATO','CAVEIRA','DRONE',
                      'CIRCUITO','ONDA','TORRE','PRISMA','ROBO'));

-- ---------------------------------------------------------------------------
-- telegram_chat_id — declarado pela pessoa, e nunca verificado
-- ---------------------------------------------------------------------------
-- E o `chat id`, nao o `@`: o `@` e trocavel pelo dono e nao identifica ninguem. O usuario
-- informa, o sistema NAO confere, e id errado e responsabilidade de quem informou
-- (docs/02-dominio/usuario.md).
--
-- A unicidade nao impede o erro de digitacao — impede a AMBIGUIDADE de dois usuarios
-- apontando para o mesmo chat. Varios NULL nao colidem no Postgres, e e isso que deixa a
-- coluna ser opcional e unica ao mesmo tempo.

ALTER TABLE usuario ADD COLUMN telegram_chat_id bigint;

ALTER TABLE usuario ADD CONSTRAINT uq_usuario_telegram_chat_id UNIQUE (telegram_chat_id);

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_telegram_chat_id_nao_zero
    CHECK (telegram_chat_id <> 0);
