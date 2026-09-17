const Extrato = {
  LIMITE: 50,

  contas: [],
  meios: [],
  arvore: [],
  itens: [],
  proximo: null,
  filtroConta: '',
  somentePendentes: false,
  confirmando: null,
  ligado: false,

  async montar() {
    if (!Extrato.ligado) {
      Extrato.ligarOuvintes();
      Extrato.ligado = true;
    }
    await Extrato.recarregarTudo();
  },

  async recarregarTudo() {
    try {
      const [contas, meios, arvore] = await Promise.all([
        API.listarContas(Contexto.ambiente.id),
        API.listarMeios(Contexto.ambiente.id),
        API.arvoreDeCategorias(Contexto.ambiente.id),
      ]);
      Extrato.contas = contas.itens;
      Extrato.meios = meios.itens.filter((m) => !m.inativo);
      Extrato.arvore = arvore.itens.filter((raiz) => !raiz.sistema);
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Extrato.avisar(erro.paraGente());
      return;
    }
    Extrato.montarSeletores();
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

  ligarOuvintes() {
    ['lancData', 'lancDataEfeito', 'transfData'].forEach(CampoDeData.ligar);

    document.getElementById('fLancar').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      await Extrato.lancar();
    });

    document.getElementById('fTransferir').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      await Extrato.transferir();
    });

    document.getElementById('lancMeio').addEventListener('change', Extrato.explicarMeio);
    document.getElementById('lancSentido').addEventListener('change', Extrato.montarCategorias);
    document.getElementById('lancCategoria').addEventListener('change', Extrato.montarSubcategorias);

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

    document.getElementById('linhas').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-acao]');
      if (!botao) return;
      const id = Number(botao.dataset.id);
      const acoes = {
        confirmar: () => { Extrato.confirmando = id; Extrato.desenhar(); },
        cancelar: () => { Extrato.confirmando = null; Extrato.desenhar(); },
        excluir: () => Extrato.excluir(id),
        estornar: () => Extrato.estornar(id),
      };
      await acoes[botao.dataset.acao]();
    });
  },

  montarSeletores() {
    const ativas = Extrato.contas.filter((c) => !c.inativa);

    document.getElementById('filtroConta').innerHTML =
      '<option value="">TODAS AS CONTAS</option>'
      + Extrato.contas.map((c) => `<option value="${c.id}">${Formato.texto(c.nome)}</option>`).join('');

    document.getElementById('lancMeio').innerHTML = Extrato.meios.length
      ? Extrato.meios.map((m) => `<option value="${m.id}">${Formato.texto(Extrato.rotuloDoMeio(m))}</option>`).join('')
      : '<option value="">— sem meio de pagamento —</option>';


    const opcoesDeConta = ativas
      .map((c) => `<option value="${c.id}">${Formato.texto(c.nome)}</option>`).join('');
    document.getElementById('transfOrigem').innerHTML = opcoesDeConta;
    document.getElementById('transfDestino').innerHTML = opcoesDeConta;

    if (!CampoDeData.valor('lancData')) CampoDeData.definir('lancData', Formato.hoje());
    if (!CampoDeData.valor('transfData')) CampoDeData.definir('transfData', Formato.hoje());

    document.getElementById('btnLancar').disabled = !Extrato.meios.length;
    document.getElementById('btnTransferir').disabled = ativas.length < 2;

    Extrato.explicarMeio();
    Extrato.montarCategorias();
  },

  raizesEscolhiveis() {
    const sentido = document.getElementById('lancSentido').value;
    return Extrato.arvore.filter((raiz) =>
      raiz.sentido === sentido
      && !raiz.inativa
      && (raiz.escolhivel || raiz.filhas.some((f) => f.escolhivel)));
  },

  raizEscolhida() {
    const id = Number(document.getElementById('lancCategoria').value);
    return Extrato.arvore.find((raiz) => raiz.id === id);
  },

  montarCategorias() {
    const raizes = Extrato.raizesEscolhiveis();

    document.getElementById('lancCategoria').innerHTML =
      '<option value="">— sem categoria (fica pendente) —</option>'
      + raizes.map((r) => `<option value="${r.id}">${Formato.texto(r.nome)}</option>`).join('');

    Extrato.montarSubcategorias();
  },

  montarSubcategorias() {
    const raiz = Extrato.raizEscolhida();
    const filhas = raiz ? raiz.filhas.filter((f) => f.escolhivel) : [];
    const campo = document.getElementById('campoSubcategoria');
    const seletor = document.getElementById('lancSubcategoria');

    campo.classList.toggle('hidden', !filhas.length);
    seletor.innerHTML = filhas
      .map((f) => `<option value="${f.id}">${Formato.texto(f.nome)}</option>`).join('');

    Extrato.explicarCategoria(raiz, filhas);
  },

  explicarCategoria(raiz, filhas) {
    const alvo = document.getElementById('categoriaExplica');

    if (!raiz) {
      alvo.innerHTML = Extrato.raizesEscolhiveis().length
        ? 'Sem categoria o lançamento nasce <b>pendente</b>, e isso é um estado legítimo — a fila de pendências é exatamente esta consulta.'
        : 'Nenhuma categoria deste sentido ainda. Crie uma no <b>Cadastro</b>, ou lance sem categoria e resolva depois.';
      return;
    }

    alvo.innerHTML = filhas.length
      ? `<b>${Formato.texto(raiz.nome)}</b> tem subcategoria ativa, então ela deixou de ser
         destino de lançamento — existe um destino mais específico. Escolha qual.`
      : `<b>${Formato.texto(raiz.nome)}</b> não tem subcategoria ativa, então ela mesma é o
         destino. Ao ganhar a primeira, este campo vira obrigatório.`;
  },

  rotuloDoMeio(meio) {
    const conta = Extrato.contas.find((c) => c.id === meio.contaId);
    const tipo = Contas.ROTULO_DE_MEIO[meio.tipo] || meio.tipo;
    return meio.nome
      ? `${conta ? conta.nome : '—'} · ${meio.nome}`
      : `${conta ? conta.nome : '—'} · ${tipo}`;
  },

  explicarMeio() {
    const meio = Extrato.meios.find((m) => String(m.id) === document.getElementById('lancMeio').value);
    const campo = document.getElementById('campoDataEfeito');
    const explica = document.getElementById('lancExplica');

    if (!meio) {
      campo.classList.add('hidden');
      explica.innerHTML = 'Cadastre uma conta e um meio de pagamento antes de lançar.';
      return;
    }

    campo.classList.toggle('hidden', !meio.separaAsDuasDatas);
    explica.innerHTML = meio.separaAsDuasDatas
      ? `O boleto é o único meio com <b>duas datas de verdade</b>: enquanto o vencimento não
         chega ele é <b>PREVISTO</b> e não entra no saldo realizado.`
      : `Neste meio o dinheiro sai no dia do evento: as duas datas são a mesma, e o
         lançamento nasce <b>REALIZADO</b>.`;
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
    const confirmando = Extrato.confirmando === l.id;
    const marca = { PREVISTO: 'prev', PROVISIONADO: 'prov', REALIZADO: 'real' }[l.situacao];

    return `
      <article class="linha${l.doCiclo ? ' do-ciclo' : ''}">
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

        <div class="linha-acoes">
          ${l.doCiclo ? '<span class="tele">DO CICLO</span>' : confirmando ? `
            <span class="tele" style="color:var(--pink)">${Extrato.impacto(l)}</span>
            <button class="btn sm danger" type="button" data-acao="excluir" data-id="${l.id}">Confirmar</button>
            <button class="btn sm ghost" type="button" data-acao="cancelar" data-id="${l.id}">Cancelar</button>`
          : `
            ${l.estornoDeId ? '' : `<button class="btn sm ghost" type="button" data-acao="estornar" data-id="${l.id}">Estornar</button>`}
            <button class="btn sm danger" type="button" data-acao="confirmar" data-id="${l.id}">Excluir</button>`}
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

  async lancar() {
    const meioId = Number(document.getElementById('lancMeio').value);
    if (!meioId) return;

    const meio = Extrato.meios.find((m) => m.id === meioId);
    const dataEvento = CampoDeData.valor('lancData');
    const dataEfeito = CampoDeData.valor('lancDataEfeito');
    const subcategoria = document.getElementById('lancSubcategoria');

    if (!dataEvento) {
      Extrato.avisar('Data do evento inválida. Use dd/mm/aaaa, ou escolha no calendário.');
      return;
    }
    const raiz = Extrato.raizEscolhida();
    const destino = raiz
      ? (subcategoria.value ? Number(subcategoria.value) : raiz.id)
      : null;

    await Extrato.tentar(() => API.lancar(Contexto.ambiente.id, {
      meioId,
      categoriaId: destino,
      sentido: document.getElementById('lancSentido').value,
      valor: Formato.centavos(document.getElementById('lancValor').value),
      dataEvento,
      dataEfeito: meio.separaAsDuasDatas && dataEfeito ? dataEfeito : null,
      descricao: document.getElementById('lancDescricao').value,
    }), () => {
      document.getElementById('lancValor').value = '';
      document.getElementById('lancDescricao').value = '';
      document.getElementById('lancDescricao').focus();
    });
  },

  async transferir() {
    const dataEvento = CampoDeData.valor('transfData');
    if (!dataEvento) {
      Extrato.avisar('Data inválida. Use dd/mm/aaaa, ou escolha no calendário.');
      return;
    }

    await Extrato.tentar(() => API.transferir(Contexto.ambiente.id, {
      contaDeOrigemId: Number(document.getElementById('transfOrigem').value),
      contaDeDestinoId: Number(document.getElementById('transfDestino').value),
      valor: Formato.centavos(document.getElementById('transfValor').value),
      dataEvento,
      descricao: document.getElementById('transfDescricao').value,
    }), () => {
      document.getElementById('transfValor').value = '';
      document.getElementById('transfDescricao').value = '';
    });
  },

  async excluir(id) {
    await Extrato.tentar(() => API.excluirLancamento(Contexto.ambiente.id, id),
      () => { Extrato.confirmando = null; });
  },

  async estornar(id) {
    await Extrato.tentar(() => API.estornarLancamento(Contexto.ambiente.id, id,
      { dataEvento: Formato.hoje() }));
  },

  async tentar(acao, aoDarCerto) {
    try {
      await acao();
      Extrato.avisar(null);
      if (aoDarCerto) aoDarCerto();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Extrato.avisar(erro.paraGente());
    }
    await Extrato.recarregarTudo();
  },

  avisar(mensagem) {
    const alvo = document.getElementById('avisoExtrato');
    alvo.innerHTML = mensagem ? `<div class="aviso err">${Formato.texto(mensagem)}</div>` : '';
  },
};
