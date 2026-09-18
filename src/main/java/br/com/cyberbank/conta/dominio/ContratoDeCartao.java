package br.com.cyberbank.conta.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record ContratoDeCartao(
        Long limiteCentavos,
        LocalDate limiteInformadoEm,
        int diaVencimento,
        int diasAntesFechamento,
        Long contaPagadoraPadraoId) {

    public ContratoDeCartao {
        List<ErroDeValidacao> erros = new ArrayList<>();
        if (limiteCentavos != null && limiteCentavos <= 0) {
            erros.add(new ErroDeValidacao("limite", "POSITIVO",
                    "O limite do cartão é um valor positivo."));
        }
        if ((limiteCentavos == null) != (limiteInformadoEm == null)) {
            erros.add(new ErroDeValidacao("limite", "SEM_DATA",
                    "O limite informado carrega a data em que foi informado."));
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    public static ContratoDeCartao semLimite(int diaVencimento, int diasAntesFechamento,
            Long contaPagadoraPadraoId) {
        return new ContratoDeCartao(null, null, diaVencimento, diasAntesFechamento,
                contaPagadoraPadraoId);
    }

    public ContratoDeCartao comLimite(long novoLimiteCentavos, LocalDate informadoEm) {
        return new ContratoDeCartao(novoLimiteCentavos, informadoEm, diaVencimento,
                diasAntesFechamento, contaPagadoraPadraoId);
    }

    public boolean temLimite() {
        return limiteCentavos != null;
    }
}
