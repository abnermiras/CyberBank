package br.com.cyberbank.ambiente.dominio;

/** Um ambiente e o papel de quem esta olhando. E o que o GET /ambientes devolve. */
public record AcessoAoAmbiente(Ambiente ambiente, Papel papel) {
}
