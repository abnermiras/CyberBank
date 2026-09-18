package br.com.cyberbank.recorrencia.aplicacao;

import java.util.List;

import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;

public record ParcelamentoComParcelas(Parcelamento parcelamento, List<Lancamento> parcelas) {
}
