package br.com.cyberbank.fatura.aplicacao;

import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;

public record FaturaNaHome(Conta cartao, FaturaComNumeros fatura, long dividaCentavos) {
}
