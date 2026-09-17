package br.com.cyberbank.lancamento.dominio;

import java.util.List;

public record Pagina(List<Lancamento> itens, Cursor proximo) {

    public static Pagina de(List<Lancamento> lidos, int limite) {
        if (lidos.size() <= limite) {
            return new Pagina(lidos, null);
        }
        List<Lancamento> pagina = lidos.subList(0, limite);
        return new Pagina(List.copyOf(pagina), Cursor.de(pagina.get(limite - 1)));
    }
}
