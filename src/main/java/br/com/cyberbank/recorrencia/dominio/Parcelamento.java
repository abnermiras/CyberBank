package br.com.cyberbank.recorrencia.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cyberbank.comum.erro.ErroDeValidacao;
import br.com.cyberbank.comum.erro.ValidacaoException;

public record Parcelamento(
        Long id,
        Long ambienteId,
        Long contaId,
        Long meioId,
        Long categoriaId,
        long valorDaCompraCentavos,
        int parcelas,
        LocalDate dataDaCompra,
        String descricao,
        Instant criadoEm) {

    public static final int PARCELAS_MINIMAS = 2;
    public static final int PARCELAS_MAXIMAS = 99;
    public static final int TAMANHO_MAXIMO_DA_DESCRICAO = 200;

    public static Parcelamento novo(Long ambienteId, Long contaId, Long meioId, Long categoriaId,
            Long valorDaCompraCentavos, Integer parcelas, LocalDate dataDaCompra,
            String descricao, Instant agora) {

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valorDaCompraCentavos, erros);
        validarParcelas(parcelas, erros);
        validarDescricao(descricao, erros);
        if (dataDaCompra == null) {
            erros.add(new ErroDeValidacao("dataEvento", "OBRIGATORIO", "Informe a data da compra."));
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        return new Parcelamento(null, ambienteId, contaId, meioId, categoriaId,
                valorDaCompraCentavos, parcelas, dataDaCompra, descricao.trim(), agora);
    }

    public List<Long> valoresDasParcelas() {
        long base = valorDaCompraCentavos / parcelas;
        long sobra = valorDaCompraCentavos - base * parcelas;

        List<Long> valores = new ArrayList<>(parcelas);
        valores.add(base + sobra);
        for (int parcela = 2; parcela <= parcelas; parcela++) {
            valores.add(base);
        }
        return valores;
    }

    public Parcelamento corrigido(Long novoValorDaCompra, Long novaCategoriaId,
            String novaDescricao) {

        long valor = novoValorDaCompra == null ? valorDaCompraCentavos : novoValorDaCompra;
        String texto = novaDescricao == null ? descricao : novaDescricao;

        List<ErroDeValidacao> erros = new ArrayList<>();
        validarValor(valor, erros);
        validarDescricao(texto, erros);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        return new Parcelamento(id, ambienteId, contaId, meioId,
                novaCategoriaId == null ? categoriaId : novaCategoriaId,
                valor, parcelas, dataDaCompra, texto.trim(), criadoEm);
    }

    private static void validarValor(Long valorDaCompraCentavos, List<ErroDeValidacao> erros) {
        if (valorDaCompraCentavos == null || valorDaCompraCentavos <= 0) {
            erros.add(new ErroDeValidacao("valor", "POSITIVO",
                    "O valor da compra é sempre positivo."));
        }
    }

    private static void validarParcelas(Integer parcelas, List<ErroDeValidacao> erros) {
        if (parcelas == null || parcelas < PARCELAS_MINIMAS || parcelas > PARCELAS_MAXIMAS) {
            erros.add(new ErroDeValidacao("parcelas", "FORA_DA_FAIXA",
                    "Um parcelamento tem de " + PARCELAS_MINIMAS + " a " + PARCELAS_MAXIMAS
                            + " parcelas: uma parcela é uma compra à vista."));
        }
    }

    private static void validarDescricao(String descricao, List<ErroDeValidacao> erros) {
        if (descricao == null || descricao.isBlank()) {
            erros.add(new ErroDeValidacao("descricao", "OBRIGATORIO", "Informe a descrição."));
        } else if (descricao.trim().length() > TAMANHO_MAXIMO_DA_DESCRICAO) {
            erros.add(new ErroDeValidacao("descricao", "TAMANHO",
                    "A descrição tem no máximo " + TAMANHO_MAXIMO_DA_DESCRICAO + " caracteres."));
        }
    }
}
