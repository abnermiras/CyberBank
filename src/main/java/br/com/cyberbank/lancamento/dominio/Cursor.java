package br.com.cyberbank.lancamento.dominio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Cursor(LocalDate dataEvento, Long id) {

    public String codificado() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((dataEvento + ":" + id).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static Cursor decodificar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            String cru = new String(Base64.getUrlDecoder().decode(texto),
                    java.nio.charset.StandardCharsets.UTF_8);
            int doisPontos = cru.lastIndexOf(':');
            if (doisPontos < 0) {
                throw new IllegalArgumentException(cru);
            }
            return new Cursor(LocalDate.parse(cru.substring(0, doisPontos)),
                    Long.valueOf(cru.substring(doisPontos + 1)));
        } catch (IllegalArgumentException | DateTimeParseException invalido) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("apos", "INVALIDO",
                    "Continue a listagem pelo cursor devolvido na página anterior.")));
        }
    }

    public static Cursor de(Lancamento lancamento) {
        if (lancamento.id() == null) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
        return new Cursor(lancamento.dataEvento(), lancamento.id());
    }
}
