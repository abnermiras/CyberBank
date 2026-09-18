const Contas = {
  ROTULO_DE_TIPO: {
    CORRENTE: 'CORRENTE',
    CARTEIRA: 'CARTEIRA',
    APLICACAO: 'APLICAÇÃO',
    BENEFICIO: 'BENEFÍCIO',
    CARTAO: 'CARTÃO',
  },

  EXPLICA_TIPO: {
    CORRENTE: 'Conta bancária do dia a dia. O saldo dela paga qualquer coisa.',
    CARTEIRA: 'Dinheiro vivo — carteira, cofre, colchão. O nome é seu; o que o sistema sabe é que só sai dinheiro em espécie.',
    APLICACAO: 'Dinheiro guardado. Fica fora do fluxo de caixa: mover para cá não é gasto, é guardar.',
    BENEFICIO: 'Vale-refeição e afins. É gasto da vida, mas não é caixa: esse saldo só compra uma coisa.',
    CARTAO: 'O contrato de cartão de crédito. O saldo dele é a dívida, e por isso ele é gasto da vida sem ser caixa.',
  },

  ROTULO_DE_MEIO: {
    DEBITO: 'Débito',
    PIX: 'Pix',
    TED: 'TED',
    DESCONTO_EM_FOLHA: 'Desconto em folha',
    BOLETO: 'Boleto',
    DINHEIRO: 'Dinheiro',
    BENEFICIO: 'Cartão do benefício',
    CREDITO: 'Cartão de crédito',
  },

  identidadeDoMeio(meio, contas) {
    const conta = (contas || []).find((c) => c.id === meio.contaId);
    const tipo = Contas.ROTULO_DE_MEIO[meio.tipo] || meio.tipo;
    return `${conta ? conta.nome : '—'} · ${meio.nome || tipo}`;
  },

  contas: [],
  meios: [],
  tipos: [],
  meiosEscolhidos: new Set(),
  mostrarInativas: false,
  editando: null,
  confirmando: null,
  abrindoMeioEm: null,
  ligado: false,

  async montar() {
    if (!Contas.ligado) {
      Contas.ligarOuvintes();
      Contas.ligado = true;
    }
    await Contas.recarregar();
  },

  async recarregar() {
    try {
      const [contas, meios] = await Promise.all([
        API.listarContas(Contexto.ambiente.id),
        API.listarMeios(Contexto.ambiente.id),
      ]);
      Contas.contas = contas.itens;
      Contas.tipos = contas.tiposDisponiveis;
      Contas.meios = meios.itens;
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Contas.avisar(erro.paraGente());
      return;
    }
    Contas.montarTipos();
    Contas.desenhar();
  },

  tipoEscolhido() {
    const valor = document.getElementById('contaTipo').value;
    return Contas.tipos.find((t) => t.tipo === valor) || Contas.tipos[0];
  },

  montarTipos() {
    const seletor = document.getElementById('contaTipo');
    if (seletor.options.length !== Contas.tipos.length) {
      seletor.innerHTML = Contas.tipos
        .map((t) => `<option value="${t.tipo}">${Contas.ROTULO_DE_TIPO[t.tipo] || t.tipo}</option>`)
        .join('');
      Contas.aoTrocarTipo();
    }
  },

  aoTrocarTipo() {
    const tipo = Contas.tipoEscolhido();
    const cartao = tipo.tipo === 'CARTAO';

    Contas.meiosEscolhidos = new Set(!cartao && tipo.meios.length === 1 ? tipo.meios : []);
    document.getElementById('contaSaldo').closest('.field')
      .classList.toggle('hidden', !tipo.aceitaSaldoInicial);
    document.getElementById('cicloDoCartao').classList.toggle('hidden', !cartao);
    document.getElementById('campoDosMeios').classList.toggle('hidden', cartao);
    document.getElementById('campoDosCartoes').classList.toggle('hidden', !cartao);

    if (cartao) Contas.montarContasPagadoras();
    Contas.desenharMeiosDaAbertura();
  },

  montarContasPagadoras() {
    const seletor = document.getElementById('contaPagadora');
    const pagadoras = Contas.contas.filter((c) => c.entraEmCaixa && !c.inativa);

    seletor.innerHTML = ['<option value="">— escolher na hora de pagar —</option>']
      .concat(pagadoras.map((c) => `<option value="${c.id}">${Formato.texto(c.nome)}</option>`))
      .join('');
  },

  cicloDoCartao() {
    const dia = Number(document.getElementById('contaDiaVencimento').value);
    const antes = Number(document.getElementById('contaDiasAntes').value);
    return dia && antes ? { dia, antes } : null;
  },

  desenharMeiosDaAbertura() {
    const tipo = Contas.tipoEscolhido();
    const alvo = document.getElementById('meiosDaConta');

    if (tipo.tipo === 'CARTAO') {
      Contas.explicar();
      return;
    }

    if (!tipo.meios.length) {
      alvo.innerHTML = '<span class="tele">NÃO SE PAGA COM UMA APLICAÇÃO — RESGATA-SE ANTES</span>';
    } else if (tipo.meios.length === 1) {
      alvo.innerHTML = `<span class="opcao fixa"><b>${Contas.ROTULO_DE_MEIO[tipo.meios[0]]}</b>
        <i>é o único meio que cabe aqui</i></span>`;
    } else {
      alvo.innerHTML = tipo.meios.map((meio) => `
        <label class="opcao${Contas.meiosEscolhidos.has(meio) ? ' on' : ''}">
          <input type="checkbox" data-meio="${meio}"
                 ${Contas.meiosEscolhidos.has(meio) ? 'checked' : ''}>
          ${Contas.ROTULO_DE_MEIO[meio]}
        </label>`).join('');
    }

    Contas.explicar();
  },

  explicar() {
    const tipo = Contas.tipoEscolhido();

    document.getElementById('contaExplica').innerHTML =
      `${Contas.EXPLICA_TIPO[tipo.tipo] || ''} ${tipo.tipo === 'CARTAO'
        ? Contas.explicarCartao()
        : Contas.explicarAbertura(tipo)}`;
  },

  explicarAbertura(tipo) {
    const centavos = Formato.centavos(document.getElementById('contaSaldo').value);

    return centavos
      ? `Vai nascer um <b>lançamento de abertura</b> de ${Formato.dinheiro(centavos)}, já
         realizado, na categoria de sistema <b>Saldo de abertura</b>. Ele é do ciclo: você
         corrige o valor, mas não o exclui.`
      : 'Sem saldo informado a conta nasce zerada, e nenhum lançamento é criado.';
  },

  explicarCartao() {
    if (!Contas.cicloDoCartao()) {
      return `Informe <b>o dia do vencimento</b> e <b>quantos dias antes ele fecha</b>: é daí
              que saem as duas datas de toda fatura.`;
    }

    return `A conta nasce <b>zerada</b> — dívida de cartão não é um número, é um conjunto de
            faturas — e já com a <b>fatura ABERTA do ciclo corrente, vazia</b>. O que você
            comprar cai nela, e as datas dela aparecem aqui embaixo assim que ela existir. A
            conta pagadora é só o que vem preenchido na hora de pagar: <b>nada sai dela
            sozinho</b>.`;
  },

  ligarOuvintes() {
    document.getElementById('contaTipo').addEventListener('change', Contas.aoTrocarTipo);
    document.getElementById('contaSaldo').addEventListener('input', Contas.explicar);
    document.getElementById('contaDiaVencimento').addEventListener('input', Contas.explicar);
    document.getElementById('contaDiasAntes').addEventListener('input', Contas.explicar);

    document.getElementById('meiosDaConta').addEventListener('change', (evento) => {
      const caixa = evento.target.closest('[data-meio]');
      if (!caixa) return;
      if (caixa.checked) Contas.meiosEscolhidos.add(caixa.dataset.meio);
      else Contas.meiosEscolhidos.delete(caixa.dataset.meio);
      caixa.closest('.opcao').classList.toggle('on', caixa.checked);
    });

    document.getElementById('fConta').addEventListener('submit', async (evento) => {
      evento.preventDefault();
      await Contas.abrir();
    });

    document.getElementById('btnContasInativas').addEventListener('click', () => {
      Contas.mostrarInativas = !Contas.mostrarInativas;
      Contas.desenhar();
    });

    document.getElementById('listaContas').addEventListener('click', async (evento) => {
      const botao = evento.target.closest('[data-acao]');
      if (!botao) return;
      const id = Number(botao.dataset.id);
      const acoes = {
        editar: () => Contas.abrirEdicao(id),
        cancelar: () => Contas.fechar(),
        salvar: () => Contas.renomear(id),
        inativar: () => Contas.alterarAtivacao(id, true),
        reativar: () => Contas.alterarAtivacao(id, false),
        confirmar: () => Contas.confirmar(id),
        excluir: () => Contas.excluir(id),
        'abrir-meio': () => Contas.abrirMeio(id),
        'somar-cartao': () => Contas.somarCartao(id),
        'somar-meio': () => Contas.somarMeio(id, botao.dataset.meio),
        'inativar-meio': () => Contas.alterarAtivacaoMeio(id, true),
        'reativar-meio': () => Contas.alterarAtivacaoMeio(id, false),
        'excluir-meio': () => Contas.excluirMeio(id),
      };
      await acoes[botao.dataset.acao]();
    });
  },

  desenhar() {
    const visiveis = Contas.contas.filter((c) => Contas.mostrarInativas || !c.inativa);
    const inativas = Contas.contas.filter((c) => c.inativa).length;

    document.getElementById('contagemContas').textContent = Contas.contas.length
      ? `${Contas.contas.length} NO TOTAL${inativas ? ` · ${inativas} INATIVA${inativas > 1 ? 'S' : ''}` : ''}`
      : 'NENHUMA AINDA';

    document.getElementById('btnContasInativas').setAttribute('aria-pressed', String(Contas.mostrarInativas));

    document.getElementById('listaContas').innerHTML = visiveis.length
      ? visiveis.map(Contas.cartao).join('')
      : `<div class="vazio">Nenhuma conta ainda.${inativas ? ' Há inativas escondidas — use o interruptor.' : ''}</div>`;
  },

  cartao(conta) {
    const meus = Contas.meios.filter((m) => m.contaId === conta.id);
    const faltando = conta.tiposDeMeioDisponiveis.filter((t) => !meus.some((m) => m.tipo === t));
    const editando = Contas.editando === conta.id;
    const confirmando = Contas.confirmando === conta.id;
    const somandoMeio = Contas.abrindoMeioEm === conta.id;

    return `
      <article class="conta${conta.inativa ? ' inativa' : ''}">
        <div class="conta-head">
          ${editando ? `
            <div class="conta-edicao">
              <input id="contaNome${conta.id}" type="text" maxlength="80"
                     value="${Formato.texto(conta.nome)}">
              <button class="btn sm primary" type="button" data-acao="salvar" data-id="${conta.id}">Salvar</button>
              <button class="btn sm ghost" type="button" data-acao="cancelar" data-id="${conta.id}">Cancelar</button>
            </div>`
          : `
            <div class="conta-nome">
              <strong>${Formato.texto(conta.nome)}</strong>
              <span class="tag">${Contas.ROTULO_DE_TIPO[conta.tipo] || conta.tipo}</span>
              ${conta.entraEmCaixa ? '<span class="tag real">EM CAIXA</span>' : ''}
              ${conta.entraNoFluxoDeCaixa ? '' : '<span class="tag transf">FORA DO FLUXO</span>'}
              ${conta.inativa ? '<span class="tag">INATIVA</span>' : ''}
            </div>
            <div class="conta-saldo ${conta.saldoRealizadoCentavos < 0 ? 'neg' : ''}">
              ${Formato.dinheiro(conta.saldoRealizadoCentavos)}
            </div>`}
        </div>

        ${conta.tipo === 'CARTAO' ? `
          <div class="conta-ciclo">
            <span class="tele">FECHA ${conta.diasAntesFechamento} DIAS ANTES DE VENCER ·
              VENCE DIA ${conta.diaVencimento}</span>
            ${conta.limiteCentavos ? `<span class="tele">LIMITE
              ${Formato.dinheiro(conta.limiteCentavos)} · INFORMADO EM
              ${Formato.dia(conta.limiteInformadoEm)}</span>`
              : '<span class="tele">SEM LIMITE INFORMADO</span>'}
          </div>` : ''}

        <div class="conta-meios">
          ${meus.map((m) => `
            <span class="sub-chip${m.inativo ? ' ina' : ''}">
              ${Formato.texto(m.nome) || Contas.ROTULO_DE_MEIO[m.tipo] || m.tipo}
              <button class="chip-b" type="button"
                      data-acao="${m.inativo ? 'reativar-meio' : 'inativar-meio'}"
                      data-id="${m.id}">${m.inativo ? 'reativar' : 'inativar'}</button>
              <button class="chip-b perigo" type="button" data-acao="excluir-meio" data-id="${m.id}">excluir</button>
            </span>`).join('')}
          ${!meus.length && !conta.tiposDeMeioDisponiveis.length
            ? '<span class="tele">APLICAÇÃO NÃO TEM MEIO — PARA GASTAR, RESGATA-SE ANTES</span>' : ''}
          ${!meus.length && conta.tiposDeMeioDisponiveis.length
            ? '<span class="tele">SEM MEIO — ESTA CONTA AINDA NÃO LANÇA NADA</span>' : ''}
          ${conta.tipo === 'CARTAO'
            ? `<button class="chip-mais" type="button" data-acao="somar-cartao" data-id="${conta.id}">+ cartão</button>` : ''}
          ${conta.tipo !== 'CARTAO' && faltando.length && !somandoMeio
            ? `<button class="chip-mais" type="button" data-acao="abrir-meio" data-id="${conta.id}">+ meio</button>` : ''}
        </div>

        ${somandoMeio && faltando.length ? `
          <div class="conta-somar">
            ${faltando.map((t) => `
              <button class="btn sm ghost" type="button" data-acao="somar-meio"
                      data-id="${conta.id}" data-meio="${t}">${Contas.ROTULO_DE_MEIO[t] || t}</button>`).join('')}
            <button class="btn sm ghost" type="button" data-acao="cancelar" data-id="${conta.id}">Fechar</button>
          </div>` : ''}

        <div class="conta-acoes">
          ${confirmando ? `
            <span class="tele" style="color:var(--pink)">EXCLUIR APAGA A CONTA E OS MEIOS DELA. SÓ FUNCIONA SEM LANÇAMENTO.</span>
            <button class="btn sm danger" type="button" data-acao="excluir" data-id="${conta.id}">Confirmar</button>
            <button class="btn sm ghost" type="button" data-acao="cancelar" data-id="${conta.id}">Cancelar</button>`
          : `
            ${editando ? '' : `<button class="btn sm ghost" type="button" data-acao="editar" data-id="${conta.id}">Renomear</button>`}
            <button class="btn sm ghost" type="button"
                    data-acao="${conta.inativa ? 'reativar' : 'inativar'}"
                    data-id="${conta.id}">${conta.inativa ? 'Reativar' : 'Inativar'}</button>
            <button class="btn sm danger" type="button" data-acao="confirmar" data-id="${conta.id}">Excluir</button>`}
        </div>
      </article>`;
  },

  async abrir() {
    const tipo = Contas.tipoEscolhido();
    const bruto = document.getElementById('contaSaldo').value;
    const limite = document.getElementById('contaLimite').value;
    const pagadora = document.getElementById('contaPagadora').value;
    const ciclo = Contas.cicloDoCartao();
    const cartao = tipo.tipo === 'CARTAO';

    await Contas.tentar(() => API.abrirConta(Contexto.ambiente.id, {
      nome: document.getElementById('contaNome').value,
      tipo: tipo.tipo,
      saldoInicial: !tipo.aceitaSaldoInicial || bruto.trim() === ''
        ? null
        : Formato.centavos(bruto),
      meios: cartao ? [] : [...Contas.meiosEscolhidos],
      cartoes: cartao ? Contas.cartoesInformados() : [],
      diaVencimento: cartao && ciclo ? ciclo.dia : null,
      diasAntesFechamento: cartao && ciclo ? ciclo.antes : null,
      limite: cartao && limite.trim() !== '' ? Formato.centavos(limite) : null,
      contaPagadoraPadraoId: cartao && pagadora ? Number(pagadora) : null,
    }), () => {
      document.getElementById('fConta').reset();
      Contas.aoTrocarTipo();
      document.getElementById('contaNome').focus();
    });
  },

  cartoesInformados() {
    return document.getElementById('contaCartoes').value
      .split(',')
      .map((nome) => nome.trim())
      .filter(Boolean);
  },

  abrirMeio(contaId) {
    Contas.abrindoMeioEm = contaId;
    Contas.editando = null;
    Contas.confirmando = null;
    Contas.desenhar();
  },

  async somarCartao(contaId) {
    const nome = window.prompt('Nome do cartão — é ele que distingue físico, virtual e adicional');
    if (!nome || !nome.trim()) return;

    await Contas.tentar(() => API.cadastrarMeio(Contexto.ambiente.id,
      { tipo: 'CREDITO', contaId, nome: nome.trim() }));
  },

  async somarMeio(contaId, tipo) {
    await Contas.tentar(() => API.cadastrarMeio(Contexto.ambiente.id, { tipo, contaId }),
      () => { Contas.abrindoMeioEm = null; });
  },

  abrirEdicao(id) {
    Contas.editando = id;
    Contas.confirmando = null;
    Contas.abrindoMeioEm = null;
    Contas.desenhar();
    const campo = document.getElementById(`contaNome${id}`);
    if (campo) campo.focus();
  },

  fechar() {
    Contas.editando = null;
    Contas.confirmando = null;
    Contas.abrindoMeioEm = null;
    Contas.desenhar();
  },

  confirmar(id) {
    Contas.confirmando = id;
    Contas.editando = null;
    Contas.abrindoMeioEm = null;
    Contas.desenhar();
  },

  async renomear(id) {
    const nome = document.getElementById(`contaNome${id}`).value;
    await Contas.tentar(() => API.alterarConta(Contexto.ambiente.id, id, { nome }),
      () => { Contas.editando = null; });
  },

  async alterarAtivacao(id, inativa) {
    await Contas.tentar(() => API.alterarConta(Contexto.ambiente.id, id, { inativa }));
  },

  async excluir(id) {
    await Contas.tentar(() => API.excluirConta(Contexto.ambiente.id, id),
      () => { Contas.confirmando = null; });
  },

  async alterarAtivacaoMeio(id, inativo) {
    await Contas.tentar(() => API.alterarMeio(Contexto.ambiente.id, id, { inativo }));
  },

  async excluirMeio(id) {
    await Contas.tentar(() => API.excluirMeio(Contexto.ambiente.id, id));
  },

  async tentar(acao, aoDarCerto) {
    try {
      await acao();
      Contas.avisar(null);
      if (aoDarCerto) aoDarCerto();
    } catch (erro) {
      if (tratarFalha(erro)) return;
      Contas.avisar(erro.paraGente());
    }
    await Contas.recarregar();
  },

  avisar(mensagem) {
    const alvo = document.getElementById('avisoContas');
    alvo.innerHTML = mensagem ? `<div class="aviso err">${Formato.texto(mensagem)}</div>` : '';
  },
};
