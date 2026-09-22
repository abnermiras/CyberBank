-- V014 — a sessao guarda o navegador de onde se entrou (docs/02-dominio/usuario.md).
--
-- A lista de sessoes do Perfil existe para a pessoa reconhecer cada uma e encerrar a que nao
-- reconhece. So com a origem (o endereco) numa rede de casa, toda linha diz a mesma coisa.
--
-- Anulavel: as sessoes abertas antes desta migration nao tem de onde tirar o valor, e a tela
-- diz "navegador desconhecido" ate elas expirarem.

ALTER TABLE sessao ADD COLUMN navegador varchar(200);
