package br.com.cyberbank.recorrencia.aplicacao;

import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.recorrencia.dominio.Recorrencia;

public record RecorrenciaComOcorrencia(Recorrencia recorrencia, Lancamento ocorrenciaDoCiclo) {
}
