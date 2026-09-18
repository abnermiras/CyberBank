package br.com.cyberbank.lancamento.aplicacao;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.categoria.dominio.Categoria;
import br.com.cyberbank.categoria.dominio.CategoriaRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.fatura.dominio.FaturaRepository;
import br.com.cyberbank.lancamento.dominio.Lancamento;
import br.com.cyberbank.lancamento.dominio.LancamentoRepository;
import br.com.cyberbank.meio.dominio.MeioRepository;
import br.com.cyberbank.recorrencia.dominio.Parcelamento;
import br.com.cyberbank.recorrencia.dominio.ParcelamentoRepository;
import br.com.cyberbank.usuario.dominio.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerLancamentoUseCase {

    private final LancamentoRepository lancamentos;
    private final ContaRepository contas;
    private final MeioRepository meios;
    private final CategoriaRepository categorias;
    private final FaturaRepository faturas;
    private final ParcelamentoRepository parcelamentos;
    private final UsuarioRepository usuarios;

    public VerLancamentoUseCase(LancamentoRepository lancamentos, ContaRepository contas,
            MeioRepository meios, CategoriaRepository categorias, FaturaRepository faturas,
            ParcelamentoRepository parcelamentos, UsuarioRepository usuarios) {
        this.lancamentos = lancamentos;
        this.contas = contas;
        this.meios = meios;
        this.categorias = categorias;
        this.faturas = faturas;
        this.parcelamentos = parcelamentos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public DetalheDoLancamento executar(Long ambienteId, Long lancamentoId) {
        Lancamento lancamento = lancamentos.buscarDoAmbiente(lancamentoId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        return new DetalheDoLancamento(
                lancamento,
                contaDe(lancamento, ambienteId),
                meioDe(lancamento, ambienteId),
                categoriaDe(lancamento, ambienteId),
                faturaDe(lancamento, ambienteId),
                serieDe(lancamento, ambienteId),
                usuarios.buscarPorId(lancamento.autorId())
                        .map(usuario -> usuario.nome()).orElse(null),
                outroLadoDe(lancamento, ambienteId),
                lancamentos.buscarEstornoDe(lancamentoId, ambienteId)
                        .map(Lancamento::id).orElse(null));
    }

    private DetalheDoLancamento.ContaDoLancamento contaDe(Lancamento lancamento, Long ambienteId) {
        return contas.buscarDoAmbiente(lancamento.contaId(), ambienteId)
                .map(conta -> new DetalheDoLancamento.ContaDoLancamento(
                        conta.id(), conta.nome(), conta.tipo().name()))
                .orElse(null);
    }

    private DetalheDoLancamento.MeioDoLancamento meioDe(Lancamento lancamento, Long ambienteId) {
        if (lancamento.meioId() == null) {
            return null;
        }
        return meios.buscarDoAmbiente(lancamento.meioId(), ambienteId)
                .map(meio -> new DetalheDoLancamento.MeioDoLancamento(
                        meio.id(), meio.tipo().name(), meio.nome()))
                .orElse(null);
    }

    private DetalheDoLancamento.CategoriaDoLancamento categoriaDe(
            Lancamento lancamento, Long ambienteId) {
        if (lancamento.categoriaId() == null) {
            return null;
        }
        Optional<Categoria> escolhida =
                categorias.buscarDoAmbiente(lancamento.categoriaId(), ambienteId);
        if (escolhida.isEmpty()) {
            return null;
        }
        Categoria categoria = escolhida.get();
        Categoria raiz = categoria.ehRaiz()
                ? categoria
                : categorias.buscarDoAmbiente(categoria.paiId(), ambienteId).orElse(categoria);

        return new DetalheDoLancamento.CategoriaDoLancamento(categoria.id(), categoria.nome(),
                new DetalheDoLancamento.RaizDaCategoria(raiz.id(), raiz.nome(),
                        raiz.cor() == null ? null : raiz.cor().name()));
    }

    private DetalheDoLancamento.FaturaDoLancamento faturaDe(Lancamento lancamento,
            Long ambienteId) {

        if (lancamento.faturaId() == null) {
            return null;
        }
        return faturas.buscarDoAmbiente(lancamento.faturaId(), ambienteId)
                .map(fatura -> new DetalheDoLancamento.FaturaDoLancamento(fatura.id(),
                        fatura.competencia().toString(), fatura.status().name(),
                        fatura.dataFechamento().toString(), fatura.dataVencimento().toString()))
                .orElse(null);
    }

    private DetalheDoLancamento.SerieDoLancamento serieDe(Lancamento lancamento,
            Long ambienteId) {

        if (!lancamento.ehParcela()) {
            return null;
        }
        Optional<Parcelamento> serie =
                parcelamentos.buscarDoAmbiente(lancamento.parcelamentoId(), ambienteId);
        if (serie.isEmpty()) {
            return null;
        }

        List<Lancamento> irmas =
                lancamentos.listarDoParcelamento(lancamento.parcelamentoId(), ambienteId);

        int numero = 1;
        for (int indice = 0; indice < irmas.size(); indice++) {
            if (irmas.get(indice).id().equals(lancamento.id())) {
                numero = indice + 1;
            }
        }

        Parcelamento parcelamento = serie.get();
        return new DetalheDoLancamento.SerieDoLancamento(parcelamento.id(), numero,
                parcelamento.parcelas(), parcelamento.valorDaCompraCentavos(),
                parcelamento.dataDaCompra().toString());
    }

    private Long outroLadoDe(Lancamento lancamento, Long ambienteId) {
        if (lancamento.transferenciaId() == null) {
            return null;
        }
        return lancamentos.listarDaTransferencia(lancamento.transferenciaId(), ambienteId).stream()
                .map(Lancamento::id)
                .filter(id -> !id.equals(lancamento.id()))
                .findFirst()
                .orElse(null);
    }
}
