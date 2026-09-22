const SeletorDeAmbiente = {
  CHAVE: 'cyberbank.ambiente',
  caixa: null,

  escolher(itens) {
    const guardado = SeletorDeAmbiente.lerGuardado();
    return itens.find((ambiente) => ambiente.id === guardado) || itens[0];
  },

  lerGuardado() {
    try {
      return Number(window.localStorage.getItem(SeletorDeAmbiente.CHAVE)) || null;
    } catch (erro) {
      return null;
    }
  },

  guardar(id) {
    try {
      window.localStorage.setItem(SeletorDeAmbiente.CHAVE, String(id));
    } catch (erro) {
      void erro;
    }
  },

  ligar() {
    document.getElementById('chipAmbiente').addEventListener('click', () => {
      if (SeletorDeAmbiente.caixa) SeletorDeAmbiente.fechar();
      else SeletorDeAmbiente.abrir();
    });
  },

  async abrir() {
    let itens;
    try {
      itens = (await API.listarAmbientes()).itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      itens = [Contexto.ambiente];
    }

    const chip = document.getElementById('chipAmbiente');
    const caixa = document.createElement('div');
    caixa.className = 'menu-ambientes';
    caixa.setAttribute('role', 'listbox');
    caixa.innerHTML = `
      <div class="tele menu-titulo">TROCAR DE AMBIENTE</div>
      ${itens.map((ambiente) => `
        <button type="button" role="option" class="menu-amb${ambiente.id === Contexto.ambiente.id ? ' on' : ''}"
                aria-selected="${ambiente.id === Contexto.ambiente.id}" data-id="${ambiente.id}">
          <strong>${Formato.texto(ambiente.nome)}</strong>
          <span class="papel">${ambiente.papel}</span>
        </button>`).join('')}
      <a class="menu-gerenciar" href="#/perfil">CRIAR OU RENOMEAR, NO PERFIL →</a>`;

    const retangulo = chip.getBoundingClientRect();
    caixa.style.top = `${retangulo.bottom + 6}px`;
    caixa.style.left = `${Math.max(8, Math.min(retangulo.left, window.innerWidth - 288))}px`;

    caixa.addEventListener('click', SeletorDeAmbiente.aoClicar);
    document.body.appendChild(caixa);
    SeletorDeAmbiente.caixa = caixa;
    chip.setAttribute('aria-expanded', 'true');

    document.addEventListener('mousedown', SeletorDeAmbiente.aoClicarFora, true);
    document.addEventListener('keydown', SeletorDeAmbiente.aoTeclar, true);
    window.addEventListener('resize', SeletorDeAmbiente.fechar);
    caixa.querySelector('.menu-amb.on, .menu-amb')?.focus();
  },

  fechar() {
    if (!SeletorDeAmbiente.caixa) return;
    SeletorDeAmbiente.caixa.remove();
    SeletorDeAmbiente.caixa = null;
    document.getElementById('chipAmbiente').setAttribute('aria-expanded', 'false');
    document.removeEventListener('mousedown', SeletorDeAmbiente.aoClicarFora, true);
    document.removeEventListener('keydown', SeletorDeAmbiente.aoTeclar, true);
    window.removeEventListener('resize', SeletorDeAmbiente.fechar);
  },

  aoClicar(evento) {
    if (evento.target.closest('.menu-gerenciar')) {
      SeletorDeAmbiente.fechar();
      return;
    }
    const opcao = evento.target.closest('[data-id]');
    if (opcao) SeletorDeAmbiente.trocarPara(Number(opcao.dataset.id));
  },

  aoClicarFora(evento) {
    const dentro = SeletorDeAmbiente.caixa.contains(evento.target)
      || document.getElementById('chipAmbiente').contains(evento.target);
    if (!dentro) SeletorDeAmbiente.fechar();
  },

  aoTeclar(evento) {
    if (evento.key === 'Escape') {
      evento.stopPropagation();
      SeletorDeAmbiente.fechar();
      document.getElementById('chipAmbiente').focus();
    }
  },

  trocarPara(id) {
    if (id === Contexto.ambiente.id) {
      SeletorDeAmbiente.fechar();
      return;
    }
    SeletorDeAmbiente.guardar(id);
    const [tela] = (window.location.hash || '').replace(/^#\/?/, '').split('/');
    window.history.replaceState(null, '', tela ? `#/${tela}` : window.location.pathname);
    window.location.reload();
  },
};
