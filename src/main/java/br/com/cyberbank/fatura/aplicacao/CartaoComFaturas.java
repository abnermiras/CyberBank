package br.com.cyberbank.fatura.aplicacao;

import java.util.List;

import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.fatura.dominio.FaturaComNumeros;

public record CartaoComFaturas(
        Conta cartao,
        long dividaCentavos,
        List<FaturaComNumeros> faturas) {

    public Long limiteDisponivelCentavos() {
        return cartao.contrato().temLimite()
                ? cartao.limiteDisponivelCentavos(dividaCentavos)
                : null;
    }
}
