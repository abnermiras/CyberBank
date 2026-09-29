package br.com.cyberbank.compartilhamento.dominio;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Compartilhamento(
        Long id,
        Long contaId,
        Long ambienteOrigemId,
        Long ambienteDestinoId,
        Long criadoPor,
        Instant criadoEm) {

    public static Compartilhamento novo(Long contaId, boolean contaEhContratoDeCartao,
            Long ambienteOrigemId, Long ambienteDestinoId, Long usuarioId, Instant agora) {

        if (ambienteDestinoId == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("ambienteDestinoId",
                    "OBRIGATORIO", "Escolha o ambiente que vai receber a conta.")));
        }
        if (contaEhContratoDeCartao) {
            throw new RegraDeDominioException(CodigoDeErro.CONTA_CARTAO_NAO_SE_COMPARTILHA);
        }
        if (ambienteDestinoId.equals(ambienteOrigemId)) {
            throw new RegraDeDominioException(CodigoDeErro.AMBIENTE_DESTINO_INVALIDO);
        }
        return new Compartilhamento(null, contaId, ambienteOrigemId, ambienteDestinoId,
                usuarioId, agora);
    }
}
