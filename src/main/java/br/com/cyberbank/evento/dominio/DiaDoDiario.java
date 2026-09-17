package br.com.cyberbank.evento.dominio;

import java.time.LocalDate;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record DiaDoDiario(LocalDate dia) {

    public static DiaDoDiario de(LocalDate pedido, LocalDate hoje) {
        if (pedido == null) {
            return new DiaDoDiario(hoje);
        }
        if (pedido.isAfter(hoje)) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("dia", "FUTURO",
                    "O que ainda não aconteceu está no previsto, não no Diário.")));
        }
        return new DiaDoDiario(pedido);
    }
}
