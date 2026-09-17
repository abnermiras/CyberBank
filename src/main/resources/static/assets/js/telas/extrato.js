const Extrato = {
  LIMITE: 50,

  contas: [],
  arvore: [],
  itens: [],
  proximo: null,
  filtroConta: '',
  somentePendentes: false,
  ligado: false,

  async montar(lancamentoId) {
    if (!Extrato.ligado) {
      Extrato.ligarOuvintes();
      Extrato.ligado = true;
    }
    await Extrato.recarregarTudo();

    if (lancamentoId) {
      await Detalhe.abrir(Number(lancamentoId));
    } else {
      Detalhe.esconder();
    }
  },

  async recarregarTudo() {
    try {
      const [contas, arvore] = await Promise.all([
        API.listarContas(Contexto.ambiente.id),
        API.arvoreDeCategorias(Contexto.ambiente.id),
      ]);
      Extrato.contas = contas.itens;
      Extrato.arvore = arvore.itens.filter((raiz) => !raiz.sistema);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Extrato.avisar(erro.paraGente());
      return;
    }
    Extrato.montarFiltro();
    await Extrato.recarregarLista();
  },

  async recarregarLista(apos) {
    const parametros = { limite: Extrato.LIMITE };
    if (Extrato.filtroConta) parametros.contaId = Extrato.filtroConta;
    if (Extrato.somentePendentes) parametros.pendentes = true;
    if (apos) parametros.apos = apos;

    try {
      const pagina = await API.extrato(Contexto.ambiente.id, parametros);
      Extrato.itens = apos ? [...Extrato.itens, ...pagina.itens] : pagina.itens;
      Extrato.proximo = pagina.proximo || null;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Extrato.avisar(erro.paraGente());
      return;
    }
    Extrato.desenhar();
  },

  montarFiltro() {
    document.getElementById('filtroConta').innerHTML =
      '<option value="">TODAS AS CONTAS</option>'
      + Extrato.contas.map((c) => `<option value="${c.id}">${Formato.texto(c.nome)}</option>`).join('');
  },

  ligarOuvintes() {
    document.getElementById('filtroConta').addEventListener('change', async (evento) => {
      Extrato.filtroConta = evento.target.value;
      await Extrato.recarregarLista();
    });

    document.getElementById('btnPendentes').addEventListener('click', async () => {
      Extrato.somentePendentes = !Extrato.somentePendentes;
      await Extrato.recarregarLista();
    });

    document.getElementById('btnMais').addEventListener('click', async () => {
      if (Extrato.proximo) await Extrato.recarregarLista(Extrato.proximo);
    });

    document.getElementById('linhas').addEventListener('click', (evento) => {
      const linha = evento.target.closest('[data-lancamento]');
      if (linha) window.location.hash = `#/extrato/${linha.dataset.lancamento}`;
    });
  },

  desenhar() {
    document.getElementById('btnPendentes').setAttribute('aria-pressed', String(Extrato.somentePendentes));
    document.getElementById('btnPendentes').classList.toggle('on', Extrato.somentePendentes);
    document.getElementById('btnMais').classList.toggle('hidden', !Extrato.proximo);

    const pendentes = Extrato.itens.filter((l) => l.categoriaId == null).length;
    document.getElementById('contagemExtrato').textContent = Extrato.itens.length
      ? `${Extrato.itens.length} CARREGADO${Extrato.itens.length > 1 ? 'S' : ''}${pendentes ? ` · ${pendentes} SEM CATEGORIA` : ''}`
      : 'NADA AINDA';

    document.getElementById('linhas').innerHTML = Extrato.itens.length
      ? Extrato.itens.map(Extrato.linha).join('')
      : `<div class="vazio">${Extrato.somentePendentes
          ? 'Nenhuma pendência: todo lançamento tem categoria.'
          : 'Nenhum lançamento ainda. Comece abrindo uma conta com saldo.'}</div>`;
  },

  linha(l) {
    const conta = Extrato.contas.find((c) => c.id === l.contaId);
    const categoria = Extrato.nomeDaCategoria(l.categoriaId);
    const marca = { PREVISTO: 'prev', PROVISIONADO: 'prov', REALIZADO: 'real' }[l.situacao];

    return `
      <article class="linha abre${l.doCiclo ? ' do-ciclo' : ''}" data-lancamento="${l.id}"
               role="button" tabindex="0" aria-label="Abrir ${Formato.texto(l.descricao)}">
        <div class="linha-dia">
          <span class="d">${Formato.dia(l.dataEvento)}</span>
          ${l.dataEfeito !== l.dataEvento
            ? `<span class="tele">EFEITO ${Formato.dia(l.dataEfeito)}</span>` : ''}
        </div>

        <div class="linha-corpo">
          <strong>${Formato.texto(l.descricao)}</strong>
          <div class="hstack gap6 wrap">
            <span class="tele">${Formato.texto(conta ? conta.nome : '—')}</span>
            <span class="tag ${marca}">${l.situacao}</span>
            ${l.categoriaId == null
              ? '<span class="tag pend">SEM CATEGORIA</span>'
              : `<span class="tele">${Formato.texto(categoria)}</span>`}
            ${l.transferenciaId ? '<span class="tag transf">TRANSFERÊNCIA</span>' : ''}
            ${l.estornoDeId ? '<span class="tag transf">ESTORNO</span>' : ''}
            ${l.doCiclo ? '<span class="tag">DO SISTEMA</span>' : ''}
          </div>
        </div>

        <div class="linha-valor ${l.sentido === 'SAIDA' ? 'neg' : 'pos'}">
          ${l.sentido === 'SAIDA' ? '−' : '+'}${Formato.dinheiro(l.valor).replace('−', '')}
        </div>

      </article>`;
  },

  nomeDaCategoria(categoriaId) {
    for (const raiz of Extrato.arvore) {
      if (raiz.id === categoriaId) return raiz.nome;
      const filha = raiz.filhas.find((f) => f.id === categoriaId);
      if (filha) return `${raiz.nome} › ${filha.nome}`;
    }
    return 'DE SISTEMA';
  },

  impacto(l) {
    const conta = Extrato.contas.find((c) => c.id === l.contaId);

    const depois = conta
      ? conta.saldoRealizadoCentavos - (l.situacao === 'PREVISTO' ? 0 : (l.sentido === 'SAIDA' ? -l.valor : l.valor))
      : null;
    const par = l.transferenciaId ? ' E O OUTRO LADO DO PAR' : '';
    return depois == null
      ? `EXCLUIR APAGA ESTE LANÇAMENTO${par}.`
      : `SALDO DE ${Formato.texto(conta.nome)} VAI PARA ${Formato.dinheiro(depois)}${par}.`;
  },

  avisar(mensagem) {
    const alvo = document.getElementById('avisoExtrato');
    alvo.innerHTML = mensagem ? `<div class="aviso err">${Formato.texto(mensagem)}</div>` : '';
  },
};
