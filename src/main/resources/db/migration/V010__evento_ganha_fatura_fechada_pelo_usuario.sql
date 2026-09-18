-- V010 — fechar a fatura a mao e um ato do USUARIO, e o Diario tem que dizer isso.
--
-- O evento.md ja distinguia FATURA_ABERTA_PELO_CICLO de FATURA_ABERTA_PELO_USUARIO, e pela
-- razao que vale aqui tambem: quem fez importa. Fechar a mao nao tinha o par — o doc previa
-- so FATURA_FECHADA, de origem SISTEMA. Reusa-lo faria o Diario assinar "A ROTINA" embaixo de
-- uma acao que uma pessoa pediu, e responder "o que aconteceu, quando e POR QUEM" e a razao de
-- o evento existir.
--
-- Fechar e abrir a mao sao CONTINGENCIA, nao fluxo normal (docs/02-dominio/fatura-cartao.md):
-- o banco fechou em dia diferente, a rotina nao rodou quando devia. Justamente por serem raros
-- e que eles precisam aparecer no Diario com o autor certo.

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
    'FATURA_ENCERRADA'
));
