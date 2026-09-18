const Lancar = {
  contas: [],
  meios: [],
  arvore: [],
  aba: 'GASTO',
  sentidoRapido: 'SAIDA',

  SENTIDO_DA_ABA: { GASTO: 'SAIDA', RECEITA: 'ENTRADA' },

  async abrirRapido() {
    if (!(await Lancar.carregar())) return;

    Lancar.montarRapido();
    document.getElementById('qa').hidden = false;
    document.getElementById('qaValor').focus();
  },

  fecharRapido() {
    document.getElementById('qa').hidden = true;
    Lancar.avisar('qaAviso', null);
  },

  async abrirCompleto(aba) {
    if (!(await Lancar.carregar())) return;

    Lancar.fecharRapido();
    Lancar.trocarAba(aba || 'GASTO');
    if (!CampoDeData.valor('compData')) CampoDeData.definir('compData', Formato.hoje());
    document.getElementById('modalCompleto').hidden = false;
    document.getElementById('compValor').focus();
  },

  fecharCompleto() {
    document.getElementById('modalCompleto').hidden = true;
    Lancar.avisar('compAviso', null);
  },

  aberto() {
    return !document.getElementById('qa').hidden
      || !document.getElementById('modalCompleto').hidden
      || !document.getElementById('modalDetalhe').hidden;
  },

  async carregar() {
    try {
      const [contas, meios, arvore] = await Promise.all([
        API.listarContas(Contexto.ambiente.id),
        API.listarMeios(Contexto.ambiente.id),
        API.arvoreDeCategorias(Contexto.ambiente.id),
      ]);
      Lancar.contas = contas.itens;
      Lancar.meios = meios.itens.filter((m) => !m.inativo);
      Lancar.arvore = arvore.itens.filter((raiz) => !raiz.sistema);
      return true;
    } catch (erro) {
      if (tratarFalha(erro)) return false;
      Lancar.avisar('qaAviso', erro.paraGente());
      return false;
    }
  },

  ligar() {
    ['compData', 'compVencimento'].forEach(CampoDeData.ligar);

    document.getElementById('fab').addEventListener('click', Lancar.abrirRapido);
    document.getElementById('btnAbrirCompleto').addEventListener('click',
      () => Lancar.abrirCompleto(Lancar.SENTIDO_DA_ABA.GASTO === Lancar.sentidoRapido
        ? 'GASTO' : 'RECEITA'));
    document.getElementById('btnFecharCompleto').addEventListener('click', Lancar.fecharCompleto);

    document.getElementById('qa').addEventListener('click', (evento) => {
      if (evento.target.id === 'qa') Lancar.fecharRapido();
    });
    document.getElementById('modalCompleto').addEventListener('click', (evento) => {
      if (evento.target.id === 'modalCompleto') Lancar.fecharCompleto();
    });

    document.getElementById('qaSentido').addEventListener('click', (evento) => {
      const botao = evento.target.closest('[data-sentido]');
      if (!botao) return;
      Lancar.sentidoRapido = botao.dataset.sentido;
      document.getElementById('qaSemCategoria').querySelector('b').textContent =
        Lancar.sentidoRapido === 'SAIDA' ? 'saída' : 'entrada';
      Lancar.montarRapido();
    });

    document.getElementById('compAbas').addEventListener('click', (evento) => {
      const botao = evento.target.closest('[data-aba]:not([disabled])');
      if (botao) Lancar.trocarAba(botao.dataset.aba);
    });

    document.getElementById('qaMeio').addEventListener('change', Lancar.montarRapido);
    document.getElementById('compMeio').addEventListener('change', Lancar.explicarCompleto);
    document.getElementById('compCategoria').addEventListener('change',
      () => Lancar.montarSubcategorias('comp'));
    ['compDe', 'compPara'].forEach((id) =>
      document.getElementById(id).addEventListener('change', Lancar.explicarCompleto));

    document.getElementById('fRapido').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      await Lancar.lancarRapido();
    });
    document.getElementById('fCompleto').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      await Lancar.registrarCompleto();
    });

    document.addEventListener('keydown', Lancar.aoTeclar);
  },

  aoTeclar(evento) {
    if (evento.key === 'Escape' && Lancar.aberto()) {
      if (!document.getElementById('modalCompleto').hidden) Lancar.fecharCompleto();
      else Lancar.fecharRapido();
      return;
    }
    if (evento.key !== 'n' && evento.key !== 'N') return;
    if (evento.metaKey || evento.ctrlKey || evento.altKey) return;

    const foco = document.activeElement;
    if (foco && /^(INPUT|TEXTAREA|SELECT)$/.test(foco.tagName)) return;
    if (Lancar.aberto()) return;

    evento.preventDefault();
    Lancar.abrirRapido();
  },

  montarRapido() {
    document.querySelectorAll('#qaSentido [data-sentido]').forEach((botao) => {
      botao.classList.toggle('on', botao.dataset.sentido === Lancar.sentidoRapido);
    });

    const meio = document.getElementById('qaMeio');
    const escolhido = meio.value;
    meio.innerHTML = Lancar.meios.length
      ? Lancar.meios.map((m) =>
        `<option value="${m.id}">${Formato.texto(Lancar.rotuloDoMeio(m))}</option>`).join('')
      : '<option value="">— sem meio de pagamento —</option>';
    if (escolhido) meio.value = escolhido;

    const raizes = Lancar.raizesDoSentido(Lancar.sentidoRapido);
    const proprias = raizes.filter((raiz) => raiz.escolhivel);
    const comFilhas = raizes.filter((raiz) => !raiz.escolhivel);

    document.getElementById('qaCategoria').innerHTML =
      '<option value="">— sem categoria (fica pendente) —</option>'
      + proprias.map((raiz) =>
        `<option value="${raiz.id}">${Formato.texto(raiz.nome)}</option>`).join('')
      + comFilhas.map(Lancar.grupoDaRaiz).join('');

    document.getElementById('qaSemCategoria').classList.toggle('hidden', raizes.length > 0);

    document.getElementById('btnRapido').disabled = !Lancar.meios.length;
  },

  grupoDaRaiz(raiz) {
    const filhas = raiz.filhas.filter((filha) => filha.escolhivel);
    if (!filhas.length) return '';

    return `<optgroup label="${Formato.texto(raiz.nome)}">`
      + filhas.map((f) => `<option value="${f.id}">${Formato.texto(f.nome)}</option>`).join('')
      + '</optgroup>';
  },

  trocarAba(aba) {
    Lancar.aba = aba;
    document.querySelectorAll('#compAbas [data-aba]').forEach((botao) => {
      botao.classList.toggle('on', botao.dataset.aba === aba);
    });

    const transferencia = aba === 'TRANSFERENCIA';
    document.getElementById('linhaMeioCategoria').classList.toggle('hidden', transferencia);
    document.getElementById('linhaTransferencia').classList.toggle('hidden', !transferencia);
    document.getElementById('campoCompSubcategoria').classList.toggle('hidden', transferencia);

    if (transferencia) {
      const ativas = Lancar.contas.filter((c) => !c.inativa);
      const opcoes = ativas
        .map((c) => `<option value="${c.id}">${Formato.texto(c.nome)}</option>`).join('');
      document.getElementById('compDe').innerHTML = opcoes;
      document.getElementById('compPara').innerHTML = opcoes;
      if (ativas.length > 1) document.getElementById('compPara').selectedIndex = 1;
      document.getElementById('btnCompleto').disabled = ativas.length < 2;
    } else {
      const meio = document.getElementById('compMeio');
      meio.innerHTML = Lancar.meios.length
        ? Lancar.meios.map((m) =>
          `<option value="${m.id}">${Formato.texto(Lancar.rotuloDoMeio(m))}</option>`).join('')
        : '<option value="">— sem meio de pagamento —</option>';
      document.getElementById('btnCompleto').disabled = !Lancar.meios.length;
      Lancar.montarCategorias();
    }
    Lancar.explicarCompleto();
  },

  montarCategorias() {
    const raizes = Lancar.raizesDoSentido(Lancar.SENTIDO_DA_ABA[Lancar.aba]);
    document.getElementById('compSemCategoria').classList.toggle('hidden', raizes.length > 0);
    document.getElementById('compCategoria').innerHTML =
      '<option value="">— sem categoria (fica pendente) —</option>'
      + raizes.map((r) => `<option value="${r.id}">${Formato.texto(r.nome)}</option>`).join('');
    Lancar.montarSubcategorias('comp');
  },

  montarSubcategorias(prefixo) {
    const id = Number(document.getElementById(`${prefixo}Categoria`).value);
    const raiz = Lancar.arvore.find((r) => r.id === id);
    const filhas = raiz ? raiz.filhas.filter((f) => f.escolhivel) : [];

    document.getElementById('campoCompSubcategoria').classList.toggle('hidden', !filhas.length);
    document.getElementById(`${prefixo}Subcategoria`).innerHTML = filhas
      .map((f) => `<option value="${f.id}">${Formato.texto(f.nome)}</option>`).join('');
    Lancar.explicarCompleto();
  },

  raizesDoSentido(sentido) {
    return Lancar.arvore.filter((raiz) =>
      raiz.sentido === sentido
      && !raiz.inativa
      && (raiz.escolhivel || raiz.filhas.some((f) => f.escolhivel)));
  },

  rotuloDoMeio(meio) {
    return Contas.identidadeDoMeio(meio, Lancar.contas);
  },

  meioEscolhido() {
    return Lancar.meios.find((m) => String(m.id) === document.getElementById('compMeio').value);
  },

  explicarCompleto() {
    const alvo = document.getElementById('compExplica');

    if (Lancar.aba === 'TRANSFERENCIA') {
      document.getElementById('campoCompVencimento').classList.add('hidden');
      const de = Lancar.contaPorId('compDe');
      const para = Lancar.contaPorId('compPara');
      alvo.innerHTML = de && para && de.id === para.id
        ? 'Origem e destino são a mesma conta: isso não é transferência.'
        : `Vai criar <b>dois lançamentos</b> — saída de
           ${Formato.texto(de ? de.nome : '—')} e entrada em
           ${Formato.texto(para ? para.nome : '—')} —, com categoria de sistema.
           <b>O patrimônio não muda</b>, e nada disso entra no relatório de gasto.`;
      return;
    }

    const meio = Lancar.meioEscolhido();
    document.getElementById('campoCompVencimento')
      .classList.toggle('hidden', !meio || !meio.separaAsDuasDatas);

    if (!meio) {
      alvo.innerHTML = 'Cadastre uma conta e um meio de pagamento antes de lançar.';
      return;
    }
    const conta = Lancar.contas.find((c) => c.id === meio.contaId);
    alvo.innerHTML = meio.separaAsDuasDatas
      ? `Sai de <b>${Formato.texto(conta ? conta.nome : '—')}</b> no <b>vencimento</b>, não
         hoje: até lá o lançamento é <b>PREVISTO</b> e não entra no saldo realizado.`
      : `Sai de <b>${Formato.texto(conta ? conta.nome : '—')}</b> no dia do evento: as duas
         datas são a mesma, e o lançamento nasce <b>REALIZADO</b>.`;
  },

  contaPorId(seletor) {
    const id = Number(document.getElementById(seletor).value);
    return Lancar.contas.find((c) => c.id === id);
  },

  async lancarRapido() {
    const meioId = Number(document.getElementById('qaMeio').value);
    if (!meioId) return;

    await Lancar.enviar('qaAviso', () => API.lancar(Contexto.ambiente.id, {
      meioId,
      categoriaId: Number(document.getElementById('qaCategoria').value) || null,
      sentido: Lancar.sentidoRapido,
      valor: Formato.centavos(document.getElementById('qaValor').value),
      dataEvento: Formato.hoje(),
      dataEfeito: null,
      descricao: document.getElementById('qaDescricao').value,
    }), () => {
      document.getElementById('qaValor').value = '';
      document.getElementById('qaDescricao').value = '';
      document.getElementById('qaValor').focus();
    });
  },

  async registrarCompleto() {
    const dataEvento = CampoDeData.valor('compData');
    if (!dataEvento) {
      Lancar.avisar('compAviso', 'Data do evento inválida. Use dd/mm/aaaa, ou o calendário.');
      return;
    }
    const valor = Formato.centavos(document.getElementById('compValor').value);
    const descricao = document.getElementById('compDescricao').value;

    if (Lancar.aba === 'TRANSFERENCIA') {
      await Lancar.enviar('compAviso', () => API.transferir(Contexto.ambiente.id, {
        contaDeOrigemId: Number(document.getElementById('compDe').value),
        contaDeDestinoId: Number(document.getElementById('compPara').value),
        valor,
        dataEvento,
        descricao,
      }), Lancar.fecharCompleto);
      return;
    }

    const meio = Lancar.meioEscolhido();
    if (!meio) return;

    const subcategoria = document.getElementById('compSubcategoria');
    const raizId = Number(document.getElementById('compCategoria').value);
    const campoSub = document.getElementById('campoCompSubcategoria');
    const categoriaId = raizId
      ? (!campoSub.classList.contains('hidden') && subcategoria.value
        ? Number(subcategoria.value) : raizId)
      : null;

    const vencimento = CampoDeData.valor('compVencimento');

    await Lancar.enviar('compAviso', () => API.lancar(Contexto.ambiente.id, {
      meioId: meio.id,
      categoriaId,
      sentido: Lancar.SENTIDO_DA_ABA[Lancar.aba],
      valor,
      dataEvento,
      dataEfeito: meio.separaAsDuasDatas && vencimento ? vencimento : null,
      descricao,
    }), Lancar.fecharCompleto);
  },

  async enviar(aviso, acao, aoDarCerto) {
    try {
      await acao();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Lancar.avisar(aviso, erro.paraGente());
      return;
    }
    Lancar.avisar(aviso, null);
    if (aoDarCerto) aoDarCerto();
    await recarregarTelaAtual();
  },

  avisar(alvo, mensagem) {
    document.getElementById(alvo).innerHTML = mensagem
      ? `<div class="aviso err">${Formato.texto(mensagem)}</div>`
      : '';
  },
};
