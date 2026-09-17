package br.com.cyberbank.lancamento.dominio;

import java.time.LocalDate;
import java.util.List;

public record ResumoDoMes(
        LocalDate primeiroDia,
        LocalDate ultimoDia,
        LocalDate hoje,
        long emCaixaCentavos,
        long guardadoCentavos,
        long patrimonioCentavos,
        long aPagarCentavos,
        long aReceberCentavos,
        long entrouNoMesCentavos,
        long saiuNoMesCentavos,
        long guardadoNoMesCentavos,
        long pendencias,
        List<RelatorioDoMes.GastoDeCategoria> gastoPorCategoria,
        List<Lancamento> proximos) {

    public long sobraAteOFimDoMesCentavos() {
        return emCaixaCentavos + aReceberCentavos - aPagarCentavos;
    }

    public int diasAteOFimDoMes() {
        return hoje.isAfter(ultimoDia) ? 0 : ultimoDia.getDayOfMonth() - hoje.getDayOfMonth();
    }
}
