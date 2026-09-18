package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class AgendaDoCiclo {

    public static final int PASSOS_POR_RODADA = 240;

    private AgendaDoCiclo() {
    }

    public static Optional<PassoDoCiclo> proximoPasso(List<FaturaComNumeros> faturas,
            LocalDate hoje) {

        PassoDoCiclo escolhido = null;

        for (FaturaComNumeros fatura : faturas) {
            PassoDoCiclo candidato = passoDe(fatura, hoje);
            if (candidato != null && (escolhido == null || candidato.vemAntesDe(escolhido))) {
                escolhido = candidato;
            }
        }
        return Optional.ofNullable(escolhido);
    }

    private static PassoDoCiclo passoDe(FaturaComNumeros fatura, LocalDate hoje) {
        if (fatura.venceuSemQuitar(hoje)) {
            return PassoDoCiclo.rolar(fatura, hoje);
        }
        if (fatura.encerrouEAindaNaoLiquidou()) {
            return PassoDoCiclo.encerrar(fatura, hoje);
        }
        if (fatura.chegouAoFechamento(hoje)) {
            return PassoDoCiclo.fechar(fatura, hoje);
        }
        return null;
    }
}
