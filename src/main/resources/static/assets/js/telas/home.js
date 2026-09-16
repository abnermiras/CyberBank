const Home = {
  async montar() {
    const alvo = document.getElementById('oQueExiste');
    alvo.innerHTML = '<span class="tele">CARREGANDO…</span>';

    let arvore;
    try {
      arvore = (await API.arvoreDeCategorias(Contexto.ambiente.id)).itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      alvo.innerHTML = `<span class="tele" style="color:var(--pink)">${erro.paraGente()}</span>`;
      return;
    }

    const todas = arvore.flatMap((r) => [r, ...r.filhas]);
    const ativas = todas.filter((c) => !c.inativa).length;

    alvo.innerHTML = `
      <div class="metric">
        <span class="lbl">Ambiente</span>
        <span class="val cy" style="font-size:22px">${Home.textoSeguro(Contexto.ambiente.nome)}</span>
        <span class="sub">SEU PAPEL: ${Contexto.ambiente.papel}</span>
      </div>
      <div class="metric">
        <span class="lbl">Categorias suas</span>
        <span class="val ac" style="font-size:22px">${ativas}</span>
        <span class="sub">${Home.legendaDeCategorias(todas.length, ativas)}</span>
      </div>
      <a class="atalho" href="#/cadastro">
        <span class="icone">⊕</span>
        <span>
          <span class="t">Cadastro</span>
          <span class="s">${todas.length ? 'Criar, renomear, inativar e excluir' : 'Comece criando a primeira categoria'}</span>
        </span>
        <span class="seta">→</span>
      </a>`;
  },

  legendaDeCategorias(total, ativas) {
    if (!total) return 'O SISTEMA NÃO SUGERE NENHUMA';
    const inativas = total - ativas;
    return inativas ? `${total} NO TOTAL · ${inativas} INATIVA${inativas > 1 ? 'S' : ''}` : 'TODAS ATIVAS';
  },

  textoSeguro(bruto) {
    return String(bruto).replace(/[&<>"']/g, (c) =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
  },
};
