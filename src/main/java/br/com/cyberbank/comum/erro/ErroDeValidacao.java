package br.com.cyberbank.comum.erro;

/**
 * Um campo invalido. A validacao devolve TODOS de uma vez — validar um por vez faz o usuario
 * descobrir o formulario errado em quatro tentativas (docs/04-api/erros.md).
 *
 * @param campo o nome do campo no JSON (camelCase, em portugues), nunca o nome interno
 */
public record ErroDeValidacao(String campo, String codigo, String mensagem) {
}
