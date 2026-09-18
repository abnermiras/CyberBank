package br.com.cyberbank.conta.aplicacao;

import java.util.List;

import br.com.cyberbank.conta.dominio.TipoDeConta;
import br.com.cyberbank.meio.dominio.TipoDeMeio;

public record AberturaDeConta(
        String nome,
        TipoDeConta tipo,
        Long saldoInicialCentavos,
        List<TipoDeMeio> meios,
        List<String> cartoes,
        Integer diaVencimento,
        Integer diasAntesFechamento,
        Long limiteCentavos,
        Long contaPagadoraPadraoId) {

    public AberturaDeConta {
        meios = meios == null ? List.of() : meios;
        cartoes = cartoes == null ? List.of() : cartoes;
    }

    public boolean temCicloDeFatura() {
        return diaVencimento != null && diasAntesFechamento != null;
    }
}
