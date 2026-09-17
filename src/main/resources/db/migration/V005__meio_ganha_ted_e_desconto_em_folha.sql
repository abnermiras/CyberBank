-- V005 — o meio para de ter nome, e a tabela de tipos assume as tres familias
-- (docs/02-dominio/meio-de-pagamento.md).
--
-- POR QUE UMA V005 E NAO UMA CORRECAO NA V004: a V004 ja foi aplicada. Migration aplicada e
-- imutavel, "nem para corrigir um erro de digitacao, nem antes do push"
-- (docs/03-dados/migrations.md) — e o erro fica visivel no historico, que e o ponto.

-- `nome` passa a ser SO DO CREDITO, onde ele e a identidade do cartao: um contrato tem o
-- fisico, o virtual e o adicional, e o compartilhamento empresta UM cartao. Nos outros meios
-- nao ha o que distinguir, porque a conta tem no maximo um de cada tipo.
--
-- A coluna nao e removida, e nao e indecisao: ela continua obrigatoria para o CREDITO, que
-- nasce com a fatura. O CHECK `ck_meio_nome_nao_vazio` da V004 continua valendo e aceita
-- NULL sozinho — em SQL, `length(btrim(NULL)) > 0` e NULL, e constraint so reprova FALSE.
ALTER TABLE meio ALTER COLUMN nome DROP NOT NULL;

-- Os dois tipos novos sao da terceira familia: VOCABULARIO DO EXTRATO. Nenhuma regra do
-- sistema muda por causa deles — movem CORRENTE, `dataEfeito = dataEvento`, sem fatura e sem
-- parcela, exatamente como DEBITO e PIX, que ja eram identicos entre si desde a V004.
ALTER TABLE meio DROP CONSTRAINT ck_meio_tipo;

ALTER TABLE meio ADD CONSTRAINT ck_meio_tipo CHECK (tipo IN (
    'DEBITO',
    'CREDITO',
    'PIX',
    'TED',
    'DESCONTO_EM_FOLHA',
    'DINHEIRO',
    'BENEFICIO',
    'BOLETO'
));

-- "Uma conta tem no maximo um meio de cada tipo — exceto CREDITO." E a invariante que torna
-- o par (conta, tipo) suficiente para identificar o meio, e e ela que aposenta o nome.
-- CREDITO fica de fora porque um contrato tem quantos cartoes o emissor emitir.
CREATE UNIQUE INDEX uq_meio_conta_tipo ON meio (conta_id, tipo) WHERE tipo <> 'CREDITO';
