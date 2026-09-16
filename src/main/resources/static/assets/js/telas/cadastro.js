const Cadastro = {
  PALETA: ['VIOLETA', 'AZUL', 'TEAL', 'OLIVA', 'OCRE', 'TERRACOTA', 'ARDOSIA', 'MALVA'],

  arvore: [],
  mostrarInativas: false,
  corEscolhida: 'OCRE',
  editandoRaiz: null,
  editandoSub: null,
  corEmEdicao: null,
  confirmando: null,
  ligado: false,
  apagarAviso: null,

  async montar() {
    if (!Cadastro.ligado) {
      Cadastro.montarSwatches();
      Cadastro.ligarOuvintes();
      Cadastro.ligado = true;
      Cadastro.carregarSistema();
    }
    await Cadastro.recarregar();
  },

  tom: (cor) => `var(--cat-${String(cor || 'ardosia').toLowerCase()})`,

  montarSwatches() {
    document.getElementById('swatches').innerHTML = Cadastro.PALETA.map((cor) =>
      `<button type="button" class="swatch${cor === Cadastro.corEscolhida ? ' on' : ''}"
         data-cor="${cor}" style="--sw:${Cadastro.tom(cor)}" title="${cor}"></button>`).join('');
  },

  ligarOuvintes() {
    document.getElementById('swatches').addEventListener('click', (evento) => {
      const alvo = evento.target.closest('[data-cor]');
      if (!alvo) return;
      Cadastro.corEscolhida = alvo.dataset.cor;
      Cadastro.montarSwatches();
    });

    const arvore = document.getElementById('arvore');

    arvore.addEventListener('click', async (evento) => {
      const paleta = evento.target.closest('[data-cor-edicao]');
      if (paleta) {
        Cadastro.corEmEdicao = paleta.dataset.corEdicao;
        Cadastro.desenhar();
        return;
      }

      const botao = evento.target.closest('[data-acao]');
      if (!botao) return;
      const id = Number(botao.dataset.id);
      const acoes = {
        'editar-raiz': () => Cadastro.abrirEdicaoDeRaiz(id),
        'editar-sub': () => Cadastro.abrir({ editandoSub: id }, `nomeSub${id}`),
        confirmar: () => Cadastro.abrir({ confirmando: id }),
        cancelar: () => Cadastro.abrir({}),
        'salvar-raiz': () => Cadastro.salvarRaiz(id),
        'salvar-sub': () => Cadastro.salvarSub(id),
        'criar-sub': () => Cadastro.criarSub(id),
        inativar: () => Cadastro.alterarAtivacao(id, true),
        reativar: () => Cadastro.alterarAtivacao(id, false),
        excluir: () => Cadastro.excluir(id),
      };
      await acoes[botao.dataset.acao]();
    });

    arvore.addEventListener('keydown', (evento) => {
      if (evento.key === 'Escape') { Cadastro.abrir({}); return; }
      if (evento.key !== 'Enter') return;
      evento.preventDefault();
      const campo = evento.target;
      if (campo.dataset.novaSub) Cadastro.criarSub(Number(campo.dataset.novaSub));
      if (campo.id === `nomeRaiz${Cadastro.editandoRaiz}`) Cadastro.salvarRaiz(Cadastro.editandoRaiz);
      if (campo.id === `nomeSub${Cadastro.editandoSub}`) Cadastro.salvarSub(Cadastro.editandoSub);
    });

    document.getElementById('btnInativas').addEventListener('click', () => {
      Cadastro.mostrarInativas = !Cadastro.mostrarInativas;
      Cadastro.desenhar();
    });

    document.getElementById('fRaiz').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      const botao = document.getElementById('btnRaiz');
      botao.disabled = true;
      try {
        await API.criarCategoria(Contexto.ambiente.id, {
          nome: document.getElementById('raizNome').value.trim(),
          sentido: document.getElementById('raizSentido').value,
          cor: Cadastro.corEscolhida,
        });
        document.getElementById('raizNome').value = '';
        await Cadastro.recarregar();
        Cadastro.avisar('Raiz criada.', 'ok');
      } catch (erro) {
        Cadastro.tratar(erro);
      } finally {
        botao.disabled = false;
        document.getElementById('raizNome').focus();
      }
    });
  },

  abrirEdicaoDeRaiz(id) {
    const raiz = Cadastro.arvore.find((r) => r.id === id);
    Cadastro.corEmEdicao = raiz ? raiz.cor : null;
    Cadastro.abrir({ editandoRaiz: id }, `nomeRaiz${id}`);
  },

  abrir(estado, focar) {
    Cadastro.editandoRaiz = estado.editandoRaiz ?? null;
    Cadastro.editandoSub = estado.editandoSub ?? null;
    Cadastro.confirmando = estado.confirmando ?? null;
    Cadastro.desenhar();
    if (!focar) return;
    const campo = document.getElementById(focar);
    if (campo) { campo.focus(); campo.select(); }
  },

  async recarregar() {
    try {
      Cadastro.arvore = (await API.arvoreDeCategorias(Contexto.ambiente.id)).itens;
      Cadastro.desenhar();
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async carregarSistema() {
    try {
      const todas = (await API.categoriasDeSistema(Contexto.ambiente.id)).itens;
      document.getElementById('listaSistema').innerHTML = todas
        .filter((c) => c.sistema)
        .map((c) => `<span class="tag ${c.sentido === 'ENTRADA' ? 'entrada' : 'saida'}">${Cadastro.esc(c.nome)} · ${c.sentido}</span>`)
        .join('');
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  desenhar() {
    const visiveis = Cadastro.mostrarInativas
      ? Cadastro.arvore
      : Cadastro.arvore.filter((r) => !r.inativa);

    Cadastro.desenharContagem();
    document.getElementById('btnInativas').setAttribute('aria-pressed', String(Cadastro.mostrarInativas));

    document.getElementById('arvore').innerHTML = visiveis.length
      ? `<div class="arvores">${visiveis.map(Cadastro.desenharCard).join('')}</div>`
      : Cadastro.desenharVazio();
  },

  desenharVazio() {
    return Cadastro.arvore.length
      ? `<div class="vazio">TODAS AS ${Cadastro.arvore.length} RAÍZES ESTÃO <b>INATIVAS</b> E ESCONDIDAS<br>
           USE O INTERRUPTOR ACIMA PARA REVELAR</div>`
      : `<div class="vazio">NENHUMA CATEGORIA NESTE AMBIENTE<br>
           O SISTEMA <b>NÃO CRIA</b> CATEGORIA DE USUÁRIO — A NOMENCLATURA É SUA<br>
           CRIE A PRIMEIRA ACIMA</div>`;
  },

  desenharContagem() {
    const todas = Cadastro.arvore.flatMap((r) => [r, ...r.filhas]);
    const inativas = todas.filter((c) => c.inativa).length;
    const visiveis = Cadastro.mostrarInativas ? todas.length : todas.length - inativas;
    const alvo = document.getElementById('contagem');

    if (!todas.length) { alvo.innerHTML = 'NENHUMA CATEGORIA'; return; }

    alvo.innerHTML = inativas
      ? `<b>${visiveis}</b> de ${todas.length} · <span class="escondidas">${inativas} inativa${inativas > 1 ? 's' : ''}</span>`
      : `<b>${todas.length}</b> categoria${todas.length > 1 ? 's' : ''}`;
  },

  desenharCard(raiz) {
    const tom = Cadastro.tom(raiz.cor);
    const ativas = raiz.filhas.filter((f) => !f.inativa).length;

    const cabeca = Cadastro.editandoRaiz === raiz.id
      ? Cadastro.edicaoDeRaiz(raiz)
      : `<div class="arv-head">
           <span class="arv-nome">${Cadastro.esc(raiz.nome)}</span>
           <span class="arv-sent">${raiz.sentido} · ${ativas}/${raiz.filhas.length} SUB</span>
         </div>`;

    const adicionar = raiz.inativa ? '' : `<div class="arv-add">
         <input data-nova-sub="${raiz.id}" maxlength="80" autocomplete="off"
                placeholder="+ subcategoria de ${Cadastro.esc(raiz.nome)}">
         <button class="btn sm" data-acao="criar-sub" data-id="${raiz.id}" title="criar subcategoria">+</button>
       </div>`;

    return `<div class="arv ${raiz.inativa ? 'ina' : ''}" style="--k:${tom}">
              ${cabeca}
              <div class="arv-corpo">${Cadastro.desenharFilhas(raiz)}</div>
              <div class="arv-estado">${Cadastro.desenharEstado(raiz, ativas)}</div>
              ${adicionar}
              <div class="arv-acoes">${Cadastro.desenharAcoesDaRaiz(raiz)}</div>
            </div>`;
  },

  edicaoDeRaiz(raiz) {
    const swatches = Cadastro.PALETA.map((cor) =>
      `<button type="button" class="swatch${cor === Cadastro.corEmEdicao ? ' on' : ''}"
         data-cor-edicao="${cor}" style="--sw:${Cadastro.tom(cor)}" title="${cor}"></button>`).join('');

    return `<div class="arv-edicao">
              <input id="nomeRaiz${raiz.id}" maxlength="80" value="${Cadastro.esc(raiz.nome)}">
              <div class="swatches">${swatches}</div>
            </div>`;
  },

  desenharFilhas(raiz) {
    const visiveis = Cadastro.mostrarInativas ? raiz.filhas : raiz.filhas.filter((f) => !f.inativa);

    const emEdicao = raiz.filhas.find((f) => f.id === Cadastro.editandoSub);
    if (emEdicao) {
      return `<div class="arv-edicao" style="width:100%">
                <input id="nomeSub${emEdicao.id}" maxlength="80" value="${Cadastro.esc(emEdicao.nome)}">
                <div class="hstack gap6 wrap">
                  <button class="btn primary sm" data-acao="salvar-sub" data-id="${emEdicao.id}">Salvar</button>
                  <button class="btn ghost sm" data-acao="cancelar">Cancelar</button>
                  <button class="btn danger sm" data-acao="confirmar" data-id="${emEdicao.id}">Excluir</button>
                </div>
              </div>`;
    }

    if (raiz.filhas.some((f) => f.id === Cadastro.confirmando)) {
      return `<div class="hstack gap6 wrap">${Cadastro.confirmacao(Cadastro.confirmando)}</div>`;
    }

    if (!visiveis.length) {
      return `<span class="dica">${raiz.filhas.length
        ? `${raiz.filhas.length} SUBCATEGORIA(S) INATIVA(S), ESCONDIDA(S)`
        : 'SEM SUBCATEGORIA'}</span>`;
    }

    return visiveis.map((f) => `<span class="sub-chip ${f.inativa ? 'ina' : ''}">${Cadastro.esc(f.nome)}
        <button class="chip-b" data-acao="editar-sub" data-id="${f.id}" title="renomear">&#9998;</button>
        <button class="chip-b perigo" data-acao="${f.inativa ? 'reativar' : 'inativar'}" data-id="${f.id}"
                title="${f.inativa ? 'reativar' : 'inativar'}">${f.inativa ? '&#8634;' : '&times;'}</button>
      </span>`).join('');
  },

  desenharEstado(raiz, ativas) {
    if (raiz.inativa) {
      return `<span class="tag inativa">INATIVA</span>
              <span class="dica">a árvore inteira saiu do seletor · o campo das filhas não mudou</span>`;
    }
    if (ativas) {
      return `<span class="tag">NÃO É DESTINO</span>
              <span class="dica">o lançamento escolhe uma subcategoria</span>`;
    }
    return `<span class="tag destino">DESTINO DE LANÇAMENTO</span>
            <span class="dica">${raiz.filhas.length
              ? 'todas as subcategorias estão inativas · a raiz voltou a ser destino'
              : 'sem subcategoria ainda'}</span>`;
  },

  desenharAcoesDaRaiz(raiz) {
    if (Cadastro.editandoRaiz === raiz.id) {
      return `<button class="btn primary sm" data-acao="salvar-raiz" data-id="${raiz.id}">Salvar</button>
              <button class="btn ghost sm" data-acao="cancelar">Cancelar</button>`;
    }
    if (Cadastro.confirmando === raiz.id) {
      return Cadastro.confirmacao(raiz.id);
    }
    return `<button class="btn ghost sm" data-acao="editar-raiz" data-id="${raiz.id}">Renomear</button>
            <button class="btn ghost sm" data-acao="${raiz.inativa ? 'reativar' : 'inativar'}"
                    data-id="${raiz.id}">${raiz.inativa ? 'Reativar' : 'Inativar'}</button>
            <button class="btn danger sm" data-acao="confirmar" data-id="${raiz.id}">Excluir</button>`;
  },

  confirmacao(id) {
    return `<span class="dica" style="color:var(--pink)">EXCLUIR DE VEZ?</span>
            <button class="btn danger sm" data-acao="excluir" data-id="${id}">Sim</button>
            <button class="btn ghost sm" data-acao="cancelar">Não</button>`;
  },

  async salvarRaiz(id) {
    const raiz = Cadastro.arvore.find((r) => r.id === id);
    const nome = document.getElementById(`nomeRaiz${id}`).value.trim();
    const mudou = {};
    if (nome !== raiz.nome) mudou.nome = nome;
    if (Cadastro.corEmEdicao !== raiz.cor) mudou.cor = Cadastro.corEmEdicao;

    if (!Object.keys(mudou).length) { Cadastro.abrir({}); return; }

    try {
      await API.alterarCategoria(Contexto.ambiente.id, id, mudou);
      Cadastro.abrir({});
      await Cadastro.recarregar();
      Cadastro.avisar('Raiz atualizada. O histórico acompanha o nome novo.', 'ok');
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async salvarSub(id) {
    try {
      await API.alterarCategoria(Contexto.ambiente.id, id, {
        nome: document.getElementById(`nomeSub${id}`).value.trim(),
      });
      Cadastro.abrir({});
      await Cadastro.recarregar();
      Cadastro.avisar('Subcategoria renomeada.', 'ok');
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async criarSub(paiId) {
    const campo = document.querySelector(`[data-nova-sub="${paiId}"]`);
    const nome = campo.value.trim();
    if (!nome) { campo.focus(); return; }

    try {
      await API.criarCategoria(Contexto.ambiente.id, { nome, paiId });
      await Cadastro.recarregar();
      const proximo = document.querySelector(`[data-nova-sub="${paiId}"]`);
      if (proximo) proximo.focus();
      Cadastro.avisar('Subcategoria criada.', 'ok');
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async alterarAtivacao(id, inativa) {
    try {
      await API.alterarCategoria(Contexto.ambiente.id, id, { inativa });
      Cadastro.abrir({});
      await Cadastro.recarregar();
      Cadastro.avisar(
        inativa
          ? 'Inativada. Some da escolha e continua somando no histórico.'
          : 'Reativada, com o mesmo jogo de subcategorias que estava ativo antes.',
        'ok',
      );
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async excluir(id) {
    try {
      await API.excluirCategoria(Contexto.ambiente.id, id);
      Cadastro.abrir({});
      await Cadastro.recarregar();
      Cadastro.avisar('Excluída.', 'ok');
    } catch (erro) {
      Cadastro.abrir({});
      Cadastro.tratar(erro);
    }
  },

  avisar(texto, tipo) {
    clearTimeout(Cadastro.apagarAviso);
    document.getElementById('aviso').innerHTML = `<div class="aviso ${tipo}">${Cadastro.esc(texto)}</div>`;
    Cadastro.apagarAviso = setTimeout(() => { document.getElementById('aviso').innerHTML = ''; }, 6000);
  },

  tratar(erro) {
    if (tratarFalha(erro)) return;
    Cadastro.avisar(erro.paraGente(), 'err');
  },

  esc(bruto) {
    return String(bruto).replace(/[&<>"']/g, (c) =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
  },
};
