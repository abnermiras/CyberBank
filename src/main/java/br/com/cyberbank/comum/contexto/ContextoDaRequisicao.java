package br.com.cyberbank.comum.contexto;

/**
 * Quem esta falando, e de qual ambiente — pelo tempo de uma requisicao.
 *
 * <p>Os dois sao postos pelos filtros, depois de validados, e nunca vem do corpo nem de query
 * (ADR-0002). E deles que sai o {@code SET LOCAL} que a politica de RLS le
 * (docs/03-dados/modelo-de-dados.md).
 *
 * <p>E um {@code ThreadLocal} porque o modelo de execucao aqui e uma thread por requisicao, e
 * porque o gerenciador de transacao precisa ler isto ANTES de existir bean nenhum da requisicao.
 */
public final class ContextoDaRequisicao {

    private static final ThreadLocal<Long> USUARIO = new ThreadLocal<>();
    private static final ThreadLocal<Long> AMBIENTE = new ThreadLocal<>();

    private ContextoDaRequisicao() {
    }

    public static void definirUsuario(Long usuarioId) {
        USUARIO.set(usuarioId);
    }

    public static void definirAmbiente(Long ambienteId) {
        AMBIENTE.set(ambienteId);
    }

    public static Long usuarioId() {
        return USUARIO.get();
    }

    public static Long ambienteId() {
        return AMBIENTE.get();
    }

    /** Chamado no fim de toda requisicao. Thread de pool que guarda contexto vaza sessao. */
    public static void limpar() {
        USUARIO.remove();
        AMBIENTE.remove();
    }
}
