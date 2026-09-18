package br.com.cyberbank.fatura.dominio;

import java.time.LocalDate;

public record PassoDoCiclo(
        TipoDePasso tipo,
        Fatura fatura,
        long valorCentavos,
        LocalDate dia,
        LocalDate ordem) {

    public enum TipoDePasso {
        ROLAR,
        ENCERRAR,
        FECHAR
    }

    public static PassoDoCiclo rolar(FaturaComNumeros comNumeros, LocalDate hoje) {
        Fatura fatura = comNumeros.fatura();

        return new PassoDoCiclo(TipoDePasso.ROLAR, fatura,
                comNumeros.numeros().aPagarCentavos(),
                comNumeros.numeros().rolada() ? hoje : fatura.dataVencimento(),
                fatura.dataVencimento());
    }

    public static PassoDoCiclo encerrar(FaturaComNumeros comNumeros, LocalDate hoje) {
        Fatura fatura = comNumeros.fatura();
        return new PassoDoCiclo(TipoDePasso.ENCERRAR, fatura, 0, hoje, fatura.dataVencimento());
    }

    public static PassoDoCiclo fechar(FaturaComNumeros comNumeros, LocalDate hoje) {
        Fatura fatura = comNumeros.fatura();
        return new PassoDoCiclo(TipoDePasso.FECHAR, fatura, 0, hoje, fatura.dataFechamento());
    }

    public boolean vemAntesDe(PassoDoCiclo outro) {
        if (!ordem.isEqual(outro.ordem)) {
            return ordem.isBefore(outro.ordem);
        }
        return tipo != TipoDePasso.FECHAR && outro.tipo == TipoDePasso.FECHAR;
    }
}
