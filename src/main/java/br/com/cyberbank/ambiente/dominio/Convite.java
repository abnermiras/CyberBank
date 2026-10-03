package br.com.cyberbank.ambiente.dominio;

import java.time.Instant;
import java.util.List;

import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Convite(
        Long id,
        Long ambienteId,
        String email,
        Papel papel,
        SituacaoDoConvite situacao,
        Long convidadoPor,
        Instant criadoEm,
        Instant respondidoEm) {

    public static Convite novo(Acesso quemConvida, String emailNormalizado, Papel papel,
            boolean emailJaTemAcesso, boolean emailJaConvidado, Instant agora) {

        if (!quemConvida.papel().podeConvidar()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
        if (papel == null) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("papel", "OBRIGATORIO",
                    "Escolha a autorização: completa ou somente leitura.")));
        }
        if (!papel.cabeEmConvite()) {
            throw new ValidacaoException(List.of(new ErroDeValidacao("papel", "FORA_DA_LISTA",
                    "O convite é para editor ou leitor: o ambiente tem um dono só.")));
        }
        if (emailJaTemAcesso) {
            throw new RegraDeDominioException(CodigoDeErro.JA_TEM_ACESSO);
        }
        if (emailJaConvidado) {
            throw new RegraDeDominioException(CodigoDeErro.CONVITE_JA_PENDENTE);
        }
        return new Convite(null, quemConvida.ambienteId(), emailNormalizado, papel,
                SituacaoDoConvite.PENDENTE, quemConvida.usuarioId(), agora, null);
    }

    public Convite aceitoPor(String emailDeQuemAceita, boolean jaTemAcesso, Instant agora) {
        exigirDestinatario(emailDeQuemAceita);
        exigirPendente();
        if (jaTemAcesso) {
            throw new RegraDeDominioException(CodigoDeErro.JA_TEM_ACESSO);
        }
        return respondido(SituacaoDoConvite.ACEITO, agora);
    }

    public Convite recusadoPor(String emailDeQuemRecusa, Instant agora) {
        exigirDestinatario(emailDeQuemRecusa);
        exigirPendente();
        return respondido(SituacaoDoConvite.RECUSADO, agora);
    }

    public Convite canceladoPor(Acesso quemCancela, Instant agora) {
        if (!quemCancela.ambienteId().equals(ambienteId)
                || !quemCancela.papel().podeConvidar()) {
            throw new RegraDeDominioException(CodigoDeErro.SEM_PERMISSAO);
        }
        exigirPendente();
        return respondido(SituacaoDoConvite.CANCELADO, agora);
    }

    public Acesso acessoDe(Long usuarioId, Instant agora) {
        if (situacao != SituacaoDoConvite.ACEITO) {
            throw new RegraDeDominioException(CodigoDeErro.CONVITE_NAO_PENDENTE);
        }
        return new Acesso(null, usuarioId, ambienteId, papel, agora);
    }

    public boolean pendente() {
        return situacao == SituacaoDoConvite.PENDENTE;
    }

    private void exigirDestinatario(String email) {
        if (!this.email.equals(email)) {
            throw new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO);
        }
    }

    private void exigirPendente() {
        if (!pendente()) {
            throw new RegraDeDominioException(CodigoDeErro.CONVITE_NAO_PENDENTE);
        }
    }

    private Convite respondido(SituacaoDoConvite nova, Instant agora) {
        return new Convite(id, ambienteId, email, papel, nova, convidadoPor, criadoEm, agora);
    }
}
