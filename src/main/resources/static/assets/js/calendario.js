const Calendario = {
  MESES: ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN',
    'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'],
  SEMANA: ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'],

  ancora: null,
  caixa: null,
  estado: null,

  abrir(ancora, iso, escolher) {
    if (Calendario.ancora === ancora) {
      Calendario.fechar();
      return;
    }
    Calendario.fechar();

    const caixa = document.createElement('div');
    caixa.className = 'calendario';
    caixa.setAttribute('role', 'dialog');
    caixa.setAttribute('aria-label', 'Escolher data');
    document.body.appendChild(caixa);

    Calendario.ancora = ancora;
    Calendario.caixa = caixa;
    Calendario.estado = {
      ano: Number(iso.slice(0, 4)),
      mes: Number(iso.slice(5, 7)) - 1,
      selecionado: iso,
      escolher,
    };

    caixa.addEventListener('mousedown', (evento) => evento.preventDefault());
    caixa.addEventListener('click', Calendario.aoClicar);
    Calendario.desenhar();
    Calendario.posicionar();

    document.addEventListener('pointerdown', Calendario.aoClicarFora, true);
    document.addEventListener('keydown', Calendario.aoTeclar, true);
    document.addEventListener('scroll', Calendario.fechar, true);
    window.addEventListener('resize', Calendario.fechar);
  },

  fechar() {
    if (!Calendario.caixa) return;
    Calendario.caixa.remove();
    Calendario.caixa = null;
    Calendario.ancora = null;
    Calendario.estado = null;
    document.removeEventListener('pointerdown', Calendario.aoClicarFora, true);
    document.removeEventListener('keydown', Calendario.aoTeclar, true);
    document.removeEventListener('scroll', Calendario.fechar, true);
    window.removeEventListener('resize', Calendario.fechar);
  },

  aoClicarFora(evento) {
    if (Calendario.caixa.contains(evento.target)) return;
    if (Calendario.ancora.contains(evento.target)) return;
    Calendario.fechar();
  },

  aoTeclar(evento) {
    if (evento.key !== 'Escape') return;
    evento.stopPropagation();
    const ancora = Calendario.ancora;
    Calendario.fechar();
    ancora.focus();
  },

  aoClicar(evento) {
    const alvo = evento.target.closest('[data-dia],[data-passo]');
    if (!alvo) return;

    if (alvo.dataset.passo) {
      Calendario.andar(Number(alvo.dataset.passo));
      return;
    }

    const escolher = Calendario.estado.escolher;
    const ancora = Calendario.ancora;
    const iso = alvo.dataset.dia;
    Calendario.fechar();
    escolher(iso);
    ancora.focus();
  },

  andar(passo) {
    const estado = Calendario.estado;
    const alvo = new Date(Date.UTC(estado.ano, estado.mes + passo, 1));
    estado.ano = alvo.getUTCFullYear();
    estado.mes = alvo.getUTCMonth();
    Calendario.desenhar();
    Calendario.posicionar();
  },

  desenhar() {
    const estado = Calendario.estado;
    const hoje = Formato.hoje();
    const primeiro = new Date(Date.UTC(estado.ano, estado.mes, 1));
    const inicio = new Date(Date.UTC(estado.ano, estado.mes, 1 - primeiro.getUTCDay()));

    const dias = [];
    for (let passo = 0; passo < 42; passo += 1) {
      const dia = new Date(Date.UTC(
        inicio.getUTCFullYear(), inicio.getUTCMonth(), inicio.getUTCDate() + passo));
      const iso = dia.toISOString().slice(0, 10);
      const marcas = ['cal-dia'];
      if (dia.getUTCMonth() !== estado.mes) marcas.push('fora');
      if (iso === hoje) marcas.push('hoje');
      if (iso === estado.selecionado) marcas.push('on');
      dias.push(`<button type="button" class="${marcas.join(' ')}" `
        + `data-dia="${iso}" tabindex="-1">${dia.getUTCDate()}</button>`);
    }

    Calendario.caixa.innerHTML = `
      <div class="cal-topo">
        <button type="button" class="cal-nav" data-passo="-1"
                aria-label="Mês anterior">‹</button>
        <span class="cal-mes">${Calendario.MESES[estado.mes]} ${estado.ano}</span>
        <button type="button" class="cal-nav" data-passo="1"
                aria-label="Próximo mês">›</button>
      </div>
      <div class="cal-semana">${Calendario.SEMANA.map((d) => `<i>${d}</i>`).join('')}</div>
      <div class="cal-grade">${dias.join('')}</div>
      <div class="cal-rodape">
        <button type="button" class="cal-hoje" data-dia="${hoje}">HOJE · ${Formato.dia(hoje)}</button>
      </div>`;
  },

  posicionar() {
    const caixa = Calendario.caixa;
    const alvo = Calendario.ancora.getBoundingClientRect();
    const margem = 8;

    const abaixo = window.innerHeight - alvo.bottom;
    const acima = caixa.offsetHeight + margem > abaixo && alvo.top > abaixo;
    caixa.style.top = acima
      ? `${Math.max(margem, alvo.top - caixa.offsetHeight - 6)}px`
      : `${alvo.bottom + 6}px`;

    const esquerda = Math.min(alvo.left, window.innerWidth - caixa.offsetWidth - margem);
    caixa.style.left = `${Math.max(margem, esquerda)}px`;
  },
};
