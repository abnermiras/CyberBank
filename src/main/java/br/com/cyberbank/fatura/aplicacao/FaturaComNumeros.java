package br.com.cyberbank.fatura.aplicacao;

import br.com.cyberbank.fatura.dominio.Fatura;
import br.com.cyberbank.fatura.dominio.NumerosDaFatura;

public record FaturaComNumeros(Fatura fatura, NumerosDaFatura numeros) {
}
