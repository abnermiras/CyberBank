const Cadastro = {
  arvore: [],
  mostrarInativas: false,
  renomeando: null,
  subEm: null,
  confirmando: null,
  ligado: false,
  apagarAviso: null,

  async montar() {
    if (!Cadastro.ligado) {
      Cadastro.ligarOuvintes();
      Cadastro.ligado = true;
      Cadastro.carregarSistema();
    }
    await Cadastro.recarregar();
  },

  ligarOuvintes() {
    const arvore = document.getElementById('arvore');

    arvore.addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-acao]');
      if (!botao) return;
      const id = Number(botao.dataset.id);
      const acoes = {
        sub: () => Cadastro.abrir({ subEm: id }, 'campoSub'),
        renomear: () => Cadastro.abrir({ renomeando: id }, 'campoRenome'),
        confirmar: () => Cadastro.abrir({ confirmando: id }),
        cancelar: () => Cadastro.abrir({}),
        'salvar-nome': () => Cadastro.salvarNome(id),
        'salvar-sub': () => Cadastro.salvarSub(id),
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
      if (evento.target.id === 'campoRenome') Cadastro.salvarNome(Cadastro.renomeando);
      if (evento.target.id === 'campoSub') Cadastro.salvarSub(Cadastro.subEm);
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

  abrir(estado, focar) {
    Cadastro.renomeando = estado.renomeando ?? null;
    Cadastro.subEm = estado.subEm ?? null;
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
        .map((c) => `<span class="tag ${c.sentido === 'ENTRADA' ? 'entrada' : 'saida'}">
            ${Cadastro.esc(c.nome)} · ${c.sentido}</span>`)
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

    if (!visiveis.length) {
      document.getElementById('arvore').innerHTML = Cadastro.arvore.length
        ? `<div class="vazio">Tudo o que existe aqui está <b>inativo</b>.<br>
             Use o interruptor acima para revelar.</div>`
        : `<div class="vazio">Nenhuma categoria ainda.<br>
             O sistema <b>não inventa</b> categoria nenhuma — a nomenclatura é sua.<br>
             Crie a primeira raiz acima.</div>`;
      return;
    }

    document.getElementById('arvore').innerHTML = visiveis.map(Cadastro.desenharRaiz).join('');
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

  desenharRaiz(raiz) {
    const filhas = Cadastro.mostrarInativas ? raiz.filhas : raiz.filhas.filter((f) => !f.inativa);
    const cor = raiz.sentido === 'ENTRADA' ? 'var(--lime)' : 'var(--pink)';

    const rodape = raiz.filhas.length
      ? `<div class="filhas">
           ${filhas.map((f) => `<div class="no-filha">${Cadastro.desenharLinha(f, true)}</div>`).join('')}
           ${Cadastro.desenharContagemDeFilhas(raiz, filhas)}
         </div>`
      : '';

    return `<div class="no raiz ${raiz.inativa ? 'inativa' : ''}" style="--cor-sentido:${cor}">
              ${Cadastro.desenharLinha(raiz, false)}
              ${Cadastro.subEm === raiz.id ? Cadastro.formularioDeSub(raiz) : ''}
              ${rodape}
            </div>`;
  },

  desenharContagemDeFilhas(raiz, visiveis) {
    const escondidas = raiz.filhas.length - visiveis.length;
    if (!escondidas) return '';
    return `<div class="linha"><span class="contagem">
              <b>${visiveis.length}</b> de ${raiz.filhas.length} ·
              <span class="escondidas">${escondidas} inativa${escondidas > 1 ? 's' : ''}</span>
            </span></div>`;
  },

  desenharLinha(categoria, ehFilha) {
    if (Cadastro.renomeando === categoria.id) return Cadastro.formularioDeRenome(categoria);

    const tags = [
      `<span class="tag ${categoria.sentido === 'ENTRADA' ? 'entrada' : 'saida'}">${categoria.sentido}</span>`,
      categoria.inativa ? '<span class="tag inativa">INATIVA</span>' : '',
      categoria.escolhivel ? '<span class="tag destino" title="Pode receber lançamento">DESTINO</span>' : '',
    ].join('');

    return `<div class="linha">
              <span class="nome ${ehFilha ? 'sub' : ''}">${Cadastro.esc(categoria.nome)}</span>
              ${tags}
              <span class="acoes">${Cadastro.desenharAcoes(categoria, ehFilha)}</span>
            </div>`;
  },

  desenharAcoes(categoria, ehFilha) {
    if (Cadastro.confirmando === categoria.id) {
      return `<span class="tele" style="color:var(--pink)">EXCLUIR DE VEZ?</span>
              <button class="btn danger sm" data-acao="excluir" data-id="${categoria.id}">Sim</button>
              <button class="btn ghost sm" data-acao="cancelar">Não</button>`;
    }

    const sub = ehFilha ? '' : `<button class="btn sm" data-acao="sub" data-id="${categoria.id}">+ Sub</button>`;

    return `${sub}
      <button class="btn ghost sm" data-acao="renomear" data-id="${categoria.id}">Renomear</button>
      <button class="btn ghost sm" data-acao="${categoria.inativa ? 'reativar' : 'inativar'}"
              data-id="${categoria.id}">${categoria.inativa ? 'Reativar' : 'Inativar'}</button>
      <button class="btn danger sm" data-acao="confirmar" data-id="${categoria.id}">Excluir</button>`;
  },

  formularioDeRenome(categoria) {
    return `<div class="linha renomeando">
              <input id="campoRenome" type="text" maxlength="80" value="${Cadastro.esc(categoria.nome)}">
              <span class="acoes">
                <button class="btn primary sm" data-acao="salvar-nome" data-id="${categoria.id}">Salvar</button>
                <button class="btn ghost sm" data-acao="cancelar">Cancelar</button>
              </span>
            </div>`;
  },

  formularioDeSub(raiz) {
    return `<div class="form-inline">
              <div class="field">
                <label for="campoSub">Nova subcategoria de ${Cadastro.esc(raiz.nome)}</label>
                <input id="campoSub" type="text" maxlength="80" placeholder="Gasolina">
              </div>
              <button class="btn primary sm" data-acao="salvar-sub" data-id="${raiz.id}">Criar</button>
              <button class="btn ghost sm" data-acao="cancelar">Cancelar</button>
              <p class="tele" style="flex-basis:100%">
                Herda o sentido <b>${raiz.sentido}</b> da raiz. Com uma subcategoria ativa, a raiz
                deixa de ser destino de lançamento.
              </p>
            </div>`;
  },

  async salvarNome(id) {
    try {
      await API.alterarCategoria(Contexto.ambiente.id, id, {
        nome: document.getElementById('campoRenome').value.trim(),
      });
      Cadastro.abrir({});
      await Cadastro.recarregar();
      Cadastro.avisar('Nome alterado. O histórico passa a exibir o nome novo.', 'ok');
    } catch (erro) {
      Cadastro.tratar(erro);
    }
  },

  async salvarSub(paiId) {
    try {
      await API.criarCategoria(Contexto.ambiente.id, {
        nome: document.getElementById('campoSub').value.trim(),
        paiId,
      });
      Cadastro.abrir({});
      await Cadastro.recarregar();
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
          ? 'Inativada. Ela some da escolha e continua somando no histórico.'
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
