const Home = {
  async montar() {
    const alvo = document.getElementById('oQueExiste');
    alvo.innerHTML = '<span class="tele">CARREGANDO…</span>';

    let arvore;
    let contas;
    try {
      [arvore, contas] = await Promise.all([
        API.arvoreDeCategorias(Contexto.ambiente.id),
        API.listarContas(Contexto.ambiente.id),
      ]);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      alvo.innerHTML = `<span class="tele" style="color:var(--pink)">${erro.paraGente()}</span>`;
      return;
    }

    Home.preencherNumeros(contas);

    const todas = arvore.itens.flatMap((r) => [r, ...r.filhas]);
    const ativas = todas.filter((c) => !c.inativa).length;
    const comConta = contas.itens.length > 0;

    alvo.innerHTML = `
      <div class="metric">
        <span class="lbl">Ambiente</span>
        <span class="val cy" style="font-size:22px">${Formato.texto(Contexto.ambiente.nome)}</span>
        <span class="sub">SEU PAPEL: ${Contexto.ambiente.papel}</span>
      </div>
      <div class="metric">
        <span class="lbl">Contas suas</span>
        <span class="val ac" style="font-size:22px">${contas.itens.filter((c) => !c.inativa).length}</span>
        <span class="sub">${Home.legendaDeContas(contas.itens)}</span>
      </div>
      <div class="metric">
        <span class="lbl">Categorias suas</span>
        <span class="val ac" style="font-size:22px">${ativas}</span>
        <span class="sub">${Home.legendaDeCategorias(todas.length, ativas)}</span>
      </div>
      <a class="atalho" href="#/${comConta ? 'extrato' : 'cadastro'}">
        <span class="icone">${comConta ? '≡' : '⊕'}</span>
        <span>
          <span class="t">${comConta ? 'Extrato' : 'Cadastro'}</span>
          <span class="s">${comConta
            ? 'Lançar, transferir e ver todo o movimento'
            : 'Comece abrindo uma conta com o saldo de hoje'}</span>
        </span>
        <span class="seta">→</span>
      </a>`;
  },

  preencherNumeros(contas) {
    const guardado = contas.itens
      .filter((c) => c.tipo === 'APLICACAO')
      .reduce((soma, c) => soma + c.saldoRealizadoCentavos, 0);

    Home.numero('mEmCaixa', contas.emCaixaCentavos, 'cy',
      'CONTAS QUE PAGAM QUALQUER COISA');
    Home.numero('mGuardado', guardado, 'lm', 'SALDO DAS APLICAÇÕES');
    Home.numero('mPatrimonio', contas.patrimonioCentavos, 'ac',
      'TODAS AS CONTAS, SEM EXCEÇÃO');
  },

  numero(id, centavos, tom, legenda) {
    const campo = document.getElementById(id);
    campo.textContent = Formato.dinheiro(centavos);
    campo.className = `val ${tom}${centavos < 0 ? ' neg' : ''}`;
    campo.closest('.metric').classList.remove('semdado');
    campo.closest('.metric').querySelector('.sub').textContent = legenda;
  },

  legendaDeContas(contas) {
    if (!contas.length) return 'NENHUMA AINDA';
    const inativas = contas.filter((c) => c.inativa).length;
    return inativas ? `${contas.length} NO TOTAL · ${inativas} INATIVA${inativas > 1 ? 'S' : ''}` : 'TODAS ATIVAS';
  },

  legendaDeCategorias(total, ativas) {
    if (!total) return 'O SISTEMA NÃO SUGERE NENHUMA';
    const inativas = total - ativas;
    return inativas ? `${total} NO TOTAL · ${inativas} INATIVA${inativas > 1 ? 'S' : ''}` : 'TODAS ATIVAS';
  },
};
