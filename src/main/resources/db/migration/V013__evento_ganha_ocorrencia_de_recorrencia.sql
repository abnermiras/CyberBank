-- V013 — o tipo de evento que a recorrencia trouxe (docs/02-dominio/evento.md).
--
-- O nome ja estava reservado no doc desde o cartao: "o ciclo lancou a ocorrencia (entra com a
-- recorrencia)". Ele entra agora porque o passo existe — o fechamento abre a fatura seguinte e
-- lanca nela a ocorrencia de cada recorrencia ativa do cartao.
--
-- E `SISTEMA`: ninguem pediu, a rotina fez. O Diario separa o que o sistema fez sozinho do que
-- a pessoa fez, e uma cobranca que aparece na fatura sem linha no Diario e exatamente a
-- pergunta "por que meu saldo mudou" sem resposta.
--
-- A licao da V008 pela quarta vez: o CHECK e lista fechada, e lista fechada se reescreve
-- inteira.

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
    'SERIE_CANCELADA'
));
